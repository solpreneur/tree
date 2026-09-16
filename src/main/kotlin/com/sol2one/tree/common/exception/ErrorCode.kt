package com.sol2one.tree.common.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val status: HttpStatus
    val title: String
    val code: String
}