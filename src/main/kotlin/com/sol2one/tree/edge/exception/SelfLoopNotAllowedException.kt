package com.sol2one.tree.edge

import com.sol2one.tree.common.exception.ApplicationException

class SelfLoopNotAllowedException(val nodeId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.SELF_LOOP_NOT_ALLOWED,
    message = "Node $nodeId cannot reference itself",
    details = mapOf("nodeId" to nodeId),
)