package com.sol2one.tree.edge

import com.sol2one.tree.edge.exception.CycleNotAllowedException
import com.sol2one.tree.edge.exception.EdgeAlreadyExistsException
import com.sol2one.tree.edge.exception.EdgeNotFoundException
import com.sol2one.tree.edge.exception.InvalidCursorException
import com.sol2one.tree.edge.exception.NodeAlreadyHasParentException
import com.sol2one.tree.edge.exception.NodeNotFoundException
import com.sol2one.tree.edge.exception.SelfLoopNotAllowedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EdgeService (private val edgeRepository: EdgeRepository) {

    @Transactional
    fun createEdge(fromId: Int, toId: Int) {
        // no self loops i.e. from and to node cannot be the same
        if (fromId == toId) throw SelfLoopNotAllowedException(fromId)

        // Only one edge insert at a time, so the checks below can't
        // be invalidated by a concurrent request.
        edgeRepository.lockForStructuralChange()

        when (val existingParent = edgeRepository.findParentId(toId)) {
            null -> Unit
            fromId -> throw EdgeAlreadyExistsException(fromId, toId)
            else -> throw NodeAlreadyHasParentException(toId, existingParent)
        }

        if (edgeRepository.isInSubtree(nodeId = fromId, subtreeRootId = toId)) {
            throw CycleNotAllowedException(fromId, toId)
        }

        edgeRepository.create(fromId, toId)
    }


    @Transactional
    fun deleteEdge(fromId: Int, toId: Int) {
        if (!edgeRepository.delete(fromId, toId)) throw EdgeNotFoundException(fromId, toId)
    }

    /**
     * Returns one page of the direct children of [parentId], sorted by id.
     * Send [after] for the next page, [before] for the previous page, or neither for the first page.
     */
    @Transactional(readOnly = true)
    fun getChildren(parentId: Int, after: Int?, before: Int?, limit: Int): ChildrenPage {
        if (after != null && before != null) throw InvalidCursorException()
        if (!edgeRepository.nodeExists(parentId)) throw NodeNotFoundException(parentId)

        val children = edgeRepository.findChildren(parentId, after, before, limit)
        val firstId = children.firstOrNull()?.id
        val lastId = children.lastOrNull()?.id

        // Each check is a single index lookup, so the cursors stay correct
        // even if children were added or deleted since the previous page.
        return ChildrenPage(
            parentId = parentId,
            children = children,
            nextCursor = lastId?.takeIf { edgeRepository.hasChildAfter(parentId, it) },
            previousCursor = firstId?.takeIf { edgeRepository.hasChildBefore(parentId, it) },
        )
    }


    @Transactional(readOnly = true)
    fun requireNodeExists(nodeId: Int) {
        if (!edgeRepository.nodeExists(nodeId)) throw NodeNotFoundException(nodeId)
    }


}