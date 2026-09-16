package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class SelfLoopNotAllowedException(val nodeId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.SELF_LOOP_NOT_ALLOWED,
    message = "Edge ($nodeId, $nodeId) not allowed. Node $nodeId cannot reference itself",
)