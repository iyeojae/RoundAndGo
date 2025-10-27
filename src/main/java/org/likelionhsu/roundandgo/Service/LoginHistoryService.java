package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Entity.LoginHistory;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.LoginHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;

    public void recordLogin(User user, String ipAddress, String userAgent, String loginType) {
        LoginHistory lh = LoginHistory.builder()
                .user(user)
                .loginAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginType(loginType)
                .build();
        loginHistoryRepository.save(lh);
    }

    public List<LoginHistory> getRecentLogins(User user, int limit) {
        List<LoginHistory> list = loginHistoryRepository.findRecentByUser(user);
        if (list.size() > limit) return list.subList(0, limit);
        return list;
    }
}
