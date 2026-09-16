package com.sol2one.tree.edge

import com.sol2one.tree.common.openapi.ProblemDetailResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Edges", description = "Create and delete edges between nodes")
@Validated
@RequestMapping("/api/v1/edges")
class EdgeController(private val edgeService: EdgeService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create an edge",
        description = "Adds an edge that makes `toId` a child of `fromId`. " +
                "The edge is rejected if it would connect a node to itself, already exists, " +
                "give `toId` a second parent, or create a cycle.",
    )
    @ApiResponse(
        responseCode = "400",
        description = "`VALIDATION_FAILED`, `MALFORMED_REQUEST`, `SELF_LOOP_NOT_ALLOWED`",
        content = [Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetailResponse::class))],
    )
    @ApiResponse(
        responseCode = "409",
        description = "`EDGE_ALREADY_EXISTS`, `NODE_ALREADY_HAS_PARENT`, `CYCLE_NOT_ALLOWED`",
        content = [Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetailResponse::class))],
    )
    fun createEdge(@Valid @RequestBody request: CreateEdgeRequest): EdgeResponse {
        val fromId = request.fromId!!
        val toId = request.toId!!

        edgeService.createEdge(fromId, toId)
        return EdgeResponse(fromId, toId)
    }


    @DeleteMapping("/{fromId}/{toId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete an edge",
        description = "Removes the edge between two nodes. The subtree below `toId` is kept and becomes its own tree.",
    )
    @ApiResponse(responseCode = "204", description = "Edge deleted", content = [])
    @ApiResponse(
        responseCode = "400",
        description = "`INVALID_PARAMETER`: `fromId` or `toId` is not an integer",
        content = [Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetailResponse::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "`EDGE_NOT_FOUND`: no edge exists between the given nodes",
        content = [Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetailResponse::class))],
    )
    fun deleteEdge(
        @PathVariable fromId: Int,
        @PathVariable toId: Int
    ) {
        edgeService.deleteEdge(fromId, toId)
    }
}