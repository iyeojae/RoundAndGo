package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Common.CommunityCategory;
import org.likelionhsu.roundandgo.Entity.Community;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    // ✅ 전체 좋아요 Top 3
    @Query("SELECT c FROM Community c LEFT JOIN CommunityLike l ON c = l.community " +
            "GROUP BY c ORDER BY COUNT(l) DESC LIMIT 3")
    List<Community> findTop3ByLikes();

    // ✅ 카테고리별 좋아요 Top 3
    @Query("SELECT c FROM Community c LEFT JOIN CommunityLike l ON c = l.community " +
            "WHERE c.category = :category " +
            "GROUP BY c ORDER BY COUNT(l) DESC LIMIT 3")
    List<Community> findTop3ByCategoryOrderByLikes(CommunityCategory category);

    /**
     * 제목 또는 내용에서 키워드를 검색하는 메서드
     * @param keyword 검색할 키워드
     * @return 키워드가 제목 또는 내용에 포함된 게시글 리스트
     */
    @Query("SELECT c FROM Community c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<Community> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String keyword);
}
