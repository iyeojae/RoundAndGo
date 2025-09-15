package org.likelionhsu.roundandgo.Security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Security.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("이메일 로그인 성공 처리 시작");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtProvider.createAccessToken(userDetails.getUser());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUser());

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
