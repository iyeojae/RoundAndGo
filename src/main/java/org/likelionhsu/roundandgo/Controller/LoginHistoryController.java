package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Dto.LoginHistoryDto;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.LoginHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login-histories")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @GetMapping("/me")
    public ResponseEntity<List<LoginHistoryDto>> getMyLogins(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @RequestParam(defaultValue = "10") int limit) {
        User user = userDetails.getUser();
        List<LoginHistoryDto> dtos = loginHistoryService.getRecentLogins(user, limit).stream()
                .map(lh -> LoginHistoryDto.builder()
                        .loginAt(lh.getLoginAt())
                        .ipAddress(lh.getIpAddress())
                        .userAgent(lh.getUserAgent())
                        .loginType(lh.getLoginType())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
