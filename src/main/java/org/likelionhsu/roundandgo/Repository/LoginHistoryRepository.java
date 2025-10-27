package org.likelionhsu.roundandgo.Repository;

import org.likelionhsu.roundandgo.Entity.LoginHistory;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findTop10ByUserOrderByLoginAtDesc(User user);

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user ORDER BY lh.loginAt DESC")
    List<LoginHistory> findRecentByUser(@Param("user") User user);
}
