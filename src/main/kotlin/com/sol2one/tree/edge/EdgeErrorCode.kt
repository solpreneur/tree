package com.sol2one.tree.edge

import com.sol2one.tree.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class EdgeErrorCode(
    override val status: HttpStatus,
    override val title: String,
) : ErrorCode {
    SELF_LOOP_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Self loop not allowed"),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "Invalid cursor"),
    EDGE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Edge already exists"),
    NODE_ALREADY_HAS_PARENT(HttpStatus.CONFLICT, "Node already has a parent"),
    CYCLE_NOT_ALLOWED(HttpStatus.CONFLICT, "Cycle not allowed"),
    TREE_TOO_DEEP(HttpStatus.CONFLICT, "Tree too deep"),
    EDGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Edge not found"),
    NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "Node not found");

    override val code: String get() = name
}