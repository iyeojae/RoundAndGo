package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.RecommendedPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedPlaceRepository extends JpaRepository<RecommendedPlace, Long> {
}
