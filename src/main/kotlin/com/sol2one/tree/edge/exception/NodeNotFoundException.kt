package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class NodeNotFoundException(val nodeId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.NODE_NOT_FOUND,
    message = "Node $nodeId does not exist",
    details = mapOf("nodeId" to nodeId),
)