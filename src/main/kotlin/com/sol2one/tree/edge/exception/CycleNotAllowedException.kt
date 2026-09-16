package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class CycleNotAllowedException(val fromId: Int, val toId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.CYCLE_NOT_ALLOWED,
    message = "Node $fromId cannot be the parent of node $toId because $fromId is already a descendant of $toId",
)