package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.Comment;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>{
    // 사용자별 댓글 조회
    List<Comment> findAllByUser(User user);

    List<Comment> findByParentCommentId(Long parentCommentId);

    // 원댓글만 조회 (대댓글 제외)
    //List<Comment> findByCommunityIdAndParentCommentIdIsNull(Long postId);

    // 게시글의 모든 댓글 조회 (원댓글 + 대댓글)
    List<Comment> findByCommunityId(Long postId);
}
