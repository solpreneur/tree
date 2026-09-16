package com.sol2one.tree.edge

import com.sol2one.tree.common.exception.ApplicationException

class EdgeNotFoundException(val fromId: Int, val toId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.SELF_LOOP_NOT_ALLOWED,
    message = "Edge $fromId -> $toId not found",
    details = mapOf("fromId" to fromId,"toId" to toId ),
)