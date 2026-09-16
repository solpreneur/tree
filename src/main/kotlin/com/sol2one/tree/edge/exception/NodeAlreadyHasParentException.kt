package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class NodeAlreadyHasParentException (val toId: Int, val fromId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.NODE_ALREADY_HAS_PARENT,
    message = "Node $toId already has parent a $fromId"
)