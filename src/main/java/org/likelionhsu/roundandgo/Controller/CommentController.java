package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Request.CommentRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CommentResponseDto;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성 API
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 댓글 작성 요청 DTO
     * @return 작성된 댓글 응답 DTO
     */
    @PostMapping
    public ResponseEntity<CommonResponse<CommentResponseDto>> createComment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CommentRequestDto request) {
        CommentResponseDto response = commentService.createComment(userDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.<CommentResponseDto>builder()
                .statusCode(200)
                .msg("댓글 작성 완료")
                .data(response)
                .build());
    }

    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<CommentResponseDto>>> getComment(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CommentResponseDto> response = commentService.getComment(userDetails.getUser());
        return ResponseEntity.ok(CommonResponse.<List<CommentResponseDto>>builder()
                .statusCode(200)
                .msg("댓글 조회 완료")
                .data(response)
                .build());
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommonResponse<CommentResponseDto>> getCommentById(
            @PathVariable Long commentId) {
        CommentResponseDto response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(CommonResponse.<CommentResponseDto>builder()
                .statusCode(200)
                .msg("댓글 조회 완료")
                .data(response)
                .build());
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommonResponse<CommentResponseDto>> updateComment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto request) {
        CommentResponseDto response = commentService.updateComment(userDetails.getUser(), commentId, request);
        return ResponseEntity.ok(CommonResponse.<CommentResponseDto>builder()
                .statusCode(200)
                .msg("댓글 수정 완료")
                .data(response)
                .build());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long commentId) {
        commentService.deleteComment(userDetails.getUser(), commentId);
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(200)
                .msg("댓글 삭제 완료")
                .build());
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<CommonResponse<List<CommentResponseDto>>> getReplies(@PathVariable Long commentId) {
        List<CommentResponseDto> replies = commentService.getReplies(commentId);
        return ResponseEntity.ok(
                CommonResponse.<List<CommentResponseDto>>builder()
                        .statusCode(200)
                        .msg("대댓글 조회 성공")
                        .data(replies)
                        .build()
        );
    }
}
