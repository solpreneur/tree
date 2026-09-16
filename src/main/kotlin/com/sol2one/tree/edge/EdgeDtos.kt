package com.sol2one.tree.edge

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class CreateEdgeRequest(
    @Schema(description = "ID of the parent node", example = "1")
    @field:NotNull
    @field:PositiveOrZero
    val fromId: Int?,

    @Schema(description = "ID of the child node", example = "2")
    @field:NotNull
    @field:PositiveOrZero
    val toId: Int?,
)

data class EdgeResponse(
    val fromId: Int,
    val toId: Int,
)


data class ChildNode(
    @Schema(example = "5")
    val id: Int,

    @Schema(description = "True if this node has children of its own; request them with GET /api/v1/trees/{id}")
    val hasChildren: Boolean,
)

data class ChildrenPage(
    @Schema(example = "2")
    val parentId: Int,

    val children: List<ChildNode>,

    @Schema(description = "Send as `after` to get the next page; null if this is the last page", example = "5")
    val nextCursor: Int?,

    @Schema(description = "Send as `before` to get the previous page; null if this is the first page", example = "4")
    val previousCursor: Int?,
)



