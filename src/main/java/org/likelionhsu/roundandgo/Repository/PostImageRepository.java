package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PostImage 엔티티를 위한 레포지토리
 */
@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    /**
     * 게시글 ID로 이미지 목록 조회
     * @param communityId 게시글 ID
     * @return 해당 게시글의 이미지 목록
     */
    List<PostImage> findByCommunityId(Long communityId);

    /**
     * ID 목록으로 이미지들 조회
     * @param ids 이미지 ID 목록
     * @return 이미지 목록
     */
    List<PostImage> findByIdIn(List<Long> ids);

    /**
     * 게시글 ID와 ID 목록으로 이미지들 조회
     * @param communityId 게시글 ID
     * @param ids 이미지 ID 목록
     * @return 해당 게시글의 특정 이미지들
     */
    List<PostImage> findByCommunityIdAndIdIn(Long communityId, List<Long> ids);
}
