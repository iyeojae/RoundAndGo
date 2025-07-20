package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Common.CommunityCategory;
import org.likelionhsu.roundandgo.Entity.Community;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    /**
     * 카테고리로 커뮤니티 게시글을 조회하는 메서드
     * @param category 조회할 카테고리
     * @return 해당 카테고리에 속하는 커뮤니티 게시글 리스트
     */
    List<Community> findByCategory(CommunityCategory category);

    /**
     * 사용자 기반으로 커뮤니티 게시글을 조회하는 메서드
     * @param user 조회할 사용자
     * @return 해당 사용자가 작성한 커뮤니티 게시글 리스트
     */
    List<Community> findByUser(User user); // ← 사용자 기반 조회 메서드
}
