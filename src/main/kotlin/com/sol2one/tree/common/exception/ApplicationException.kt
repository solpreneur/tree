package com.sol2one.tree.common.exception

abstract class ApplicationException(
    val errorCode: ErrorCode,
    message: String,
    val details: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : RuntimeException(message, cause)