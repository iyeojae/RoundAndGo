package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.Comment;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>{
    // 사용자별 댓글 조회
    List<Comment> findAllByUser(User user);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByCommunityIdAndParentCommentIdIsNull(Long postId);
}
