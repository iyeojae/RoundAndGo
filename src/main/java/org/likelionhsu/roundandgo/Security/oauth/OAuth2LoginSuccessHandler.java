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
                                        Authentication authentication) throws IOException {

        Map<String, Object> attributes = ((DefaultOAuth2User) authentication.getPrincipal()).getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("카카오 계정이 존재하지 않습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // 프론트로 돌아갈 URL (쿼리/쿠키/세션 등에서 가져오도록)
        String returnTo = request.getParameter("returnTo");
        if (returnTo == null || returnTo.isBlank()) {
            returnTo = "https://roundandgo.com/first-main";
        }

        // 팝업 창에서 실행할 HTML/JS
        String frontendOrigin = "https://roundandgo.com"; // 반드시 프론트 정확 도메인
        String html = """
      <!doctype html>
      <html><head><meta charset="utf-8"><title>Login Success</title></head>
      <body>
      <script>
        (function() {
          try {
            var data = {
              type: 'OAUTH_SUCCESS',
              accessToken: %s,
              refreshToken: %s,
              returnTo: %s
            };
            if (window.opener && !window.opener.closed) {
              // 두 번째 인자는 오프너(origin)를 명시해 CSRF 방지
              window.opener.postMessage(data, '%s');
            }
          } catch (e) { /* noop */ }
          window.close();
        })();
      </script>
      </body></html>
      """.formatted(
                jsonString(accessToken),  // 아래 헬퍼 참고
                jsonString(refreshToken),
                jsonString(returnTo),
                frontendOrigin
        );

        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
    }

    private String jsonString(String s) {
        // 간단한 JSON 문자열 이스케이프
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

}