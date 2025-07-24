package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.Request.CommentRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CommentResponseDto;
import org.likelionhsu.roundandgo.Dto.Response.CommunityResponseDto;
import org.likelionhsu.roundandgo.Entity.Comment;
import org.likelionhsu.roundandgo.Entity.Community;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.CommentRepository;
import org.likelionhsu.roundandgo.Repository.CommunityRepository;
import org.likelionhsu.roundandgo.Repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;

    // 댓글 작성
    public CommentResponseDto createComment(User user, CommentRequestDto request) {
        boolean isReply = true; // 대댓글인 경우 기본값 설정
        if(request.getContent() == null || request.getContent().isEmpty()) {
            throw new RuntimeException("댓글 내용은 필수입니다.");
        }
        if(request.getParentCommentId() == null) {
            isReply = false; // 일반 댓글인 경우
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .author(user.getNickname() != null ? user.getNickname() : "익명") // 닉네임이 없으면 익명으로 설정
                .community(communityRepository.findById(request.getCommunityId())
                        .orElseThrow(() -> new RuntimeException("해당 커뮤니티 게시글을 찾을 수 없습니다.")))
                .parentCommentId(request.getParentCommentId()) // 대댓글의 경우 부모 댓글 ID 설정
                .build();
        return new CommentResponseDto(commentRepository.save(comment), isReply);
    }

    public List<CommentResponseDto> getComment(User user) {
        List<Comment> comments = commentRepository.findAllByUser(user);

        if (comments.isEmpty()) {
            throw new RuntimeException("해당 사용자의 댓글을 찾을 수 없습니다.");
        }

        return comments.stream()
                .map(comment -> new CommentResponseDto(comment, comment.getParentCommentId() != null))
                .toList();
    }

    public CommentResponseDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("해당 댓글을 찾을 수 없습니다."));

        // 댓글이 속한 커뮤니티 게시글 ID를 포함하여 응답 DTO 생성
        Community community = comment.getCommunity();
        return new CommentResponseDto(comment, false); // 일반 댓글로 처리
    }

    public CommentResponseDto updateComment(User user, Long commentId, CommentRequestDto request) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("해당 댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 댓글을 수정할 수 있습니다.");
        }

        // 댓글 내용 업데이트
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new RuntimeException("댓글 내용은 필수입니다.");
        }
        comment.setContent(request.getContent());

        // 업데이트된 댓글 저장
        Comment updatedComment = commentRepository.save(comment);

        // 응답 DTO 반환
        return new CommentResponseDto(updatedComment, false); // 일반 댓글로 처리
    }

    public  void deleteComment(User user, Long commentId) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("해당 댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 댓글을 삭제할 수 있습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }

    public List<CommentResponseDto> getReplies(Long parentCommentId) {
        List<Comment> replies = commentRepository.findByParentCommentId(parentCommentId);
        return replies.stream()
                .map(comment -> new CommentResponseDto(comment, true)) // isReply = true
                .toList();
    }
}
