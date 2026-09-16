package com.sol2one.tree.edge.exception

import com.sol2one.tree.common.exception.ApplicationException
import com.sol2one.tree.edge.EdgeErrorCode

class InvalidCursorException : ApplicationException(
    errorCode = EdgeErrorCode.INVALID_CURSOR,
    message = "Use either 'after' or 'before', not both",
)