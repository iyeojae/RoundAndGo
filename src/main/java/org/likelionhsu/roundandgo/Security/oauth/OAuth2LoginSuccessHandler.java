package org.likelionhsu.roundandgo.Security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.UserRepository;
import org.likelionhsu.roundandgo.Security.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 로그인 성공 처리 시작");

        Map<String, Object> attributes = ((DefaultOAuth2User) authentication.getPrincipal()).getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = (String) kakaoAccount.get("email");
        log.info("카카오 이메일: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("카카오 계정이 존재하지 않습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        log.info("JWT 토큰 생성 완료 - AccessToken 길이: {}", accessToken.length());

        // 방법 1: HTTP-Only 쿠키 (보안성 높음)
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // 개발 환경에서는 false, 프로덕션에서는 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60); // 1 hour
        accessTokenCookie.setDomain(".roundandgo.com"); // 도메인 설정 (점 포함)
        // SameSite 설정
        response.setHeader("Set-Cookie",
                String.format("accessToken=%s; Path=/; Max-Age=%d; Domain=.roundandgo.com; SameSite=Lax",
                        accessToken, 60 * 60));

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // 개발 환경에서는 false
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
        refreshTokenCookie.setDomain(".roundandgo.com");

        // 쿠키 추가
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // 방법 2: URL 파라미터로도 전송 (백업)
        String encodedAccessToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String encodedRefreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        // 방법 3: 여러 리다이렉트 URL 시도
        String[] redirectUrls = {
                "https://roundandgo.com/first-main?accessToken=" + encodedAccessToken + "&refreshToken=" + encodedRefreshToken,
                "https://roundandgo.com/first-main",
                "http://localhost:3000/first-main?accessToken=" + encodedAccessToken + "&refreshToken=" + encodedRefreshToken
        };

        String finalRedirectUrl = redirectUrls[0]; // 기본값

        log.info("리다이렉트 URL: {}", finalRedirectUrl);
        log.info("쿠키 설정 완료 - Domain: .roundandgo.com, Path: /");

        response.sendRedirect(finalRedirectUrl);
    }
}