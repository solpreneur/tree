package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class EdgeNotFoundException(val fromId: Int, val toId: Int) : ApplicationException(
    errorCode = EdgeErrorCode.EDGE_NOT_FOUND,
    message = "Edge ($fromId , $toId) not found"
)