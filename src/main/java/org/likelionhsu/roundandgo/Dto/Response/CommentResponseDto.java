package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Common.ProfileColor;
import org.likelionhsu.roundandgo.Entity.Comment;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentResponseDto {
    private Long id; // 댓글 ID
    private Long authorId; // 댓글 작성자 ID
    private String content; // 댓글 내용
    private String author; // 댓글 작성자
    private String profileImage; // 작성자 프로필 이미지
    private ProfileColor profileColor; // 작성자 프로필 색상
    private Long communityId; // 댓글이 달린 커뮤니티 게시글 ID
    private Long parentCommentId; // 대댓글의 경우 부모 댓글 ID, null이면 일반 댓글
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime modifiedAt; // 수정 시간 (Timestamped와 일치)

    // 추가 필드: 대댓글 여부 확인을 위한 플래그
    private boolean isReply; // true면 대댓글, false면 일반 댓글

    public CommentResponseDto(Comment comment, boolean isReply) {
        this.id = comment.getId();
        this.authorId = comment.getUser().getId();
        this.content = comment.getContent();
        this.author = comment.getUser().getNickname() != null ? comment.getUser().getNickname() : "익명";
        this.profileImage = comment.getUser().getProfileImage();
        this.profileColor = comment.getUser().getProfileColor();
        this.communityId = comment.getCommunity().getId();
        this.parentCommentId = comment.getParentCommentId();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt(); // Timestamped의 필드명 사용
        this.isReply = isReply;
    }
}
