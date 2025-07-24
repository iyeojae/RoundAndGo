package org.likelionhsu.roundandgo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class CommentRequestDto {
    private String content; // 댓글 내용

    @JsonProperty("communityId")
    private Long communityId; // 댓글이 달린 커뮤니티 게시글 ID

    @JsonProperty("parentCommentId")
    private Long parentCommentId; // 대댓글의 경우 부모 댓글 ID, null이면 일반 댓글
}
