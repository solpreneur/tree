package com.sol2one.tree

import com.sol2one.tree.common.exception.ApplicationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.core.JacksonException
import tools.jackson.core.exc.InputCoercionException
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.databind.exc.UnrecognizedPropertyException
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(exception: ApplicationException): ProblemDetail {
        val error = exception.errorCode
        return ProblemDetail.forStatusAndDetail(error.status, exception.message ?: error.title)
            .apply {
                title = error.title
                exception.details.forEach { (key, value) -> setProperty(key, value) }
            }
            .standardize(error.code)
    }

    /** @Valid failures: the object was built, but a constraint failed. */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val errors = ex.bindingResult.fieldErrors.map { it.field to (it.defaultMessage ?: "Invalid value") }
        return handleExceptionInternal(ex, validationProblem(errors), headers, status, request)
    }

    /** JSON could not be turned into the request object: wrong type, bad syntax, or missing body. */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val fieldError = generateSequence<Throwable>(ex) { it.cause }
            .filterIsInstance<JacksonException>()
            .firstOrNull { it.path.isNotEmpty() }

        val body = if (fieldError != null) {
            // A specific field had the wrong type: report it like any other validation error.
            validationProblem(listOf(fieldPath(fieldError) to expectedTypeMessage(fieldError)))
        } else {
            // Broken JSON syntax or no body at all: there is no field to point to.
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON")
                .apply { title = "Malformed request" }
                .standardize("MALFORMED_REQUEST")
        }
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request)
    }

    private fun validationProblem(errors: List<Pair<String, String>>): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid")
            .apply {
                title = "Validation failed"
                setProperty("errors", errors.groupBy({ it.first }, { it.second }).mapValues { it.value.first() })
            }
            .standardize("VALIDATION_FAILED")

    /** "fromId", "edges[2].toId", ... */
    private fun fieldPath(error: JacksonException): String =
        error.path
            .joinToString("") { ref -> ref.propertyName?.let { ".$it" } ?: "[${ref.index}]" }
            .removePrefix(".")

    private fun expectedTypeMessage(error: JacksonException): String {
        if (error is UnrecognizedPropertyException) return "is not a recognized field"
        if (error is InputCoercionException) return "is out of range"

        val type = (error as? MismatchedInputException)?.targetType ?: return "has an invalid value"
        val boxed = type.kotlin.javaObjectType // int -> Integer, so Int and Int? match the same way

        return when {
            type.isEnum -> "must be one of: ${type.enumConstants?.joinToString()}"
            boxed in INTEGER_TYPES -> "must be an integer"
            boxed in DECIMAL_TYPES -> "must be a number"
            boxed == Boolean -> "must be true or false"
            boxed == String::class.java -> "must be a string"
            else -> "has an invalid value"
        }
    }

    private fun ProblemDetail.standardize(code: String): ProblemDetail = apply {
        if (properties?.containsKey("code") != true) setProperty("code", code)
        setProperty("timestamp", Instant.now())
    }

    private companion object {
        val INTEGER_TYPES = setOf(
            Int::class.javaObjectType, Long::class.javaObjectType, Short::class.javaObjectType, BigInteger::class.java,
        )
        val DECIMAL_TYPES = setOf(
            Double::class.javaObjectType, Float::class.javaObjectType, BigDecimal::class.java,
        )
    }
}