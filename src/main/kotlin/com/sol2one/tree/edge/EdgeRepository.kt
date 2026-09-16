package com.sol2one.tree.edge

import com.sol2one.jooq.tables.references.EDGE
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository

@Repository
class EdgeRepository (private val dsl: DSLContext) {


    /**
     * Returns up to [limit] children of [parentId], sorted by id.
     * - [after] set: children with an id greater than [after]
     * - [before] set: children with an id smaller than [before]
     * - neither: the first children
     *
     * Reads straight from the primary key index (from_id, to_id), so every page is equally fast.
     */
    fun findChildren(parentId: Int, after: Int?, before: Int?, limit: Int): List<ChildNode> {
        val grandchild = EDGE.`as`("grandchild")
        val hasChildren = DSL.field(
            DSL.exists(DSL.selectOne().from(grandchild).where(grandchild.FROM_ID.eq(EDGE.TO_ID))),
        )

        val query = dsl.select(EDGE.TO_ID, hasChildren)
            .from(EDGE)
            .where(EDGE.FROM_ID.eq(parentId))

        return if (before != null) {
            // Read backwards from the cursor, then flip back into ascending order.
            query.and(EDGE.TO_ID.lt(before))
                .orderBy(EDGE.TO_ID.desc())
                .limit(limit)
                .fetch { ChildNode(id = it.value1()!!, hasChildren = it.value2()) }
                .reversed()
        } else {
            query.and(if (after != null) EDGE.TO_ID.gt(after) else DSL.noCondition())
                .orderBy(EDGE.TO_ID.asc())
                .limit(limit)
                .fetch { ChildNode(id = it.value1()!!, hasChildren = it.value2()) }
        }
    }


    /**
     * database validation ensures:
     *   - edge is unique (from_id, to_id)
     *   - a child node cannot have 2 parents i.e. unique(to_id)
     *   - forbid loop - node cannot be a parent and child at the same time e.g 1 -> 1
     */
    fun create(fromId: Int, toId: Int) {
        dsl.insertInto(EDGE)
            .set(EDGE.FROM_ID, fromId)
            .set(EDGE.TO_ID, toId)
            .execute()
    }

    /**
     * deletes and edge
     */
    fun delete(fromId: Int, toId: Int): Boolean =
        dsl.deleteFrom(EDGE)
            .where(EDGE.FROM_ID.eq(fromId))
            .and(EDGE.TO_ID.eq(toId))
            .execute() > 0


    /** True if [parentId] has a child with an id greater than [childId]. */
    fun hasChildAfter(parentId: Int, childId: Int): Boolean =
        dsl.fetchExists(EDGE, EDGE.FROM_ID.eq(parentId).and(EDGE.TO_ID.gt(childId)))

    /** True if [parentId] has a child with an id smaller than [childId]. */
    fun hasChildBefore(parentId: Int, childId: Int): Boolean =
        dsl.fetchExists(EDGE, EDGE.FROM_ID.eq(parentId).and(EDGE.TO_ID.lt(childId)))



    /** A node exists if any edge mentions it, as parent or child. */
    fun nodeExists(nodeId: Int): Boolean =
        dsl.fetchExists(EDGE, EDGE.FROM_ID.eq(nodeId).or(EDGE.TO_ID.eq(nodeId)))


    /**
    * Walks upwards from [subtreeRootId] and checks whether [nodeId] is on the path.
    * Every node has at most one parent, so this is a single chain: one index lookup per level.
    * UNION (not UNION ALL) guarantees termination even if someone corrupted the data by hand.
    */
    fun isInSubtree(nodeId: Int, subtreeRootId: Int): Boolean {
        // A subtree includes its own root. No query needed for this case.
        if (nodeId == subtreeRootId) return true

        val cteName = DSL.name("ancestors")

        // Its single column, "ancestors.id". Declared separately because the CTE
        // refers to its own column inside its definition.
        val ancestorColumn = DSL.field(DSL.name("ancestors", "id"), EDGE.FROM_ID.dataType)

        val ancestors = cteName.fields("id").`as`(
            // Starting point: the direct parent of nodeId.
            DSL.select(EDGE.FROM_ID)
                .from(EDGE)
                .where(EDGE.TO_ID.eq(nodeId))
                // UNION -> remove duplicates, so the query still stops
                // if bad data inserted by hand ever contains a cycle.
                .union(
                    // Repeated step: the parent of every node found in the previous round.
                    // PostgreSQL repeats this until a round finds no new rows.
                    DSL.select(EDGE.FROM_ID)
                        .from(EDGE)
                        .join(DSL.table(cteName)).on(EDGE.TO_ID.eq(ancestorColumn)),
                ),
        )

        // is subtreeRootId among the ancestors of nodeId?
        return dsl.withRecursive(ancestors)
            .select(DSL.field(DSL.exists(DSL.selectOne().from(ancestors).where(ancestorColumn.eq(subtreeRootId)))))
            .fetchOne()
            ?.value1() == true
    }



    fun findParentId(nodeId: Int): Int? =
        dsl.select(EDGE.FROM_ID)
            .from(EDGE)
            .where(EDGE.TO_ID.eq(nodeId))
            .fetchOne(EDGE.FROM_ID)


    /**
     * Serializes structural writes. Without it, two concurrent inserts such as 1->2 and 2->1
     * could both pass the cycle check. Released automatically (xact) at commit or rollback.
     */
    fun lockForStructuralChange() {
        dsl.fetch("select 1 from pg_advisory_xact_lock(?)", EDGE_WRITE_LOCK_KEY)
    }

    /**
     * PostgreSQL lock identified by a number you choose, not a lock on a table or a row.
     * only blocks code that asks for the same number
     */
    private companion object {
        const val EDGE_WRITE_LOCK_KEY = 7_340_001L
    }

}