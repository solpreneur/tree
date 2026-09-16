package com.sol2one.tree.common.openapi

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(
    name = "ProblemDetail",
    description = "RFC 9457 error response. Clients should branch on `status` and `code`. " +
            "`errors` is present only for VALIDATION_FAILED.",
)
data class ProblemDetailResponse(
    val type: String?,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String?,
    val code: String,
    val timestamp: Instant,
    val errors: Map<String, String>?,
)