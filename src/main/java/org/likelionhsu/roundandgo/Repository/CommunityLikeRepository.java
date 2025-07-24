package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.Community;
import org.likelionhsu.roundandgo.Entity.CommunityLike;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {
    Optional<CommunityLike> findByUserAndCommunity(User user, Community community);
    int countByCommunity(Community community);
    void deleteByUserAndCommunity(User user, Community community);
}
