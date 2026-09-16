package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class EdgeAlreadyExistsException(val fromId: Int, val toId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.EDGE_ALREADY_EXISTS,
    message = "Edge ($fromId , $toId) already exists"
)