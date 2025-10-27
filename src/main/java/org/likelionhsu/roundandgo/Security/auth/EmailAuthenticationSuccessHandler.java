package org.likelionhsu.roundandgo.Security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Security.jwt.JwtProvider;
import org.likelionhsu.roundandgo.Service.LoginHistoryService;
import org.likelionhsu.roundandgo.Entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final LoginHistoryService loginHistoryService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("이메일 로그인 성공 처리 시작");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtProvider.createAccessToken(userDetails.getUser());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUser());

        // 로그인 기록 저장 (IP, User-Agent, 로그인 타입: EMAIL)
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        try {
            loginHistoryService.recordLogin(user, ip, ua, "EMAIL");
        } catch (Exception e) {
            log.warn("로그인 기록 저장 중 예외 발생: {}", e.getMessage());
        }

        log.info("이메일 로그인 JWT 토큰 생성 완료");

        // JSON 응답으로 토큰 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
            "{\"statusCode\": 200, \"msg\": \"로그인 성공\", \"data\": {\"accessToken\": \"%s\", \"refreshToken\": \"%s\"}}",
            accessToken, refreshToken
        ));
    }
}
