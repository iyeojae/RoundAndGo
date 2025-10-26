package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.likelionhsu.roundandgo.Common.Timestamped;

@Entity
@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends Timestamped {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id; // 댓글 ID

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community; // 댓글이 속한 게시글

    private String content; // 댓글 내용


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 댓글 작성자 (User 엔티티와 연관 관계)

    @Column(name = "parent_comment_id")
    private Long parentCommentId; // 부모 댓글 ID (대댓글 기능을 위한 필드, null이면 일반 댓글)
}
