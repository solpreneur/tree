package com.sol2one.tree.edge

import com.sol2one.tree.common.openapi.ProblemDetailResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/trees")
@Tag(name = "Trees", description = "Walk the tree one node at a time")
class TreeController(private val edgeService: EdgeService) {

    @GetMapping("/{nodeId}")
    @Operation(
        summary = "Get the children of a node",
        description = "Returns the direct children of `nodeId`, sorted by id. " +
                "To walk further down, call this endpoint again for any child with `hasChildren: true`.\n\n" +
                "- Next page: call again with `after={nextCursor}`\n" +
                "- Previous page: call again with `before={previousCursor}`\n\n" +
                "A cursor is `null` when there is no page in that direction.",
    )
    @ApiResponse(responseCode = "200", description = "A page of children")
    @ApiResponse(
        responseCode = "400",
        description = "`INVALID_CURSOR`: both `after` and `before` were sent. " +
                "`VALIDATION_FAILED`: `limit` is outside 1 to 1000. " +
                "`INVALID_PARAMETER`: a parameter is not an integer.",
        content = [Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetailResponse::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "`NODE_NOT_FOUND`: no edge contains this node",
        content = [Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetailResponse::class))],
    )
    fun getChildren(
        @Parameter(description = "Node whose children are returned", example = "2")
        @PathVariable nodeId: Int,

        @Parameter(description = "Return the page after this cursor (`nextCursor` of the current page)")
        @RequestParam(required = false) after: Int?,

        @Parameter(description = "Return the page before this cursor (`previousCursor` of the current page)")
        @RequestParam(required = false) before: Int?,

        @Parameter(description = "Maximum children per page", example = "100")
        @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) limit: Int,
    ): ChildrenPage = edgeService.getChildren(nodeId, after, before, limit)
}