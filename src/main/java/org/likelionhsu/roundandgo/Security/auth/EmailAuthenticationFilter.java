package org.likelionhsu.roundandgo.Security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Security.dto.LoginRequestDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Slf4j
public class EmailAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailAuthenticationFilter(AuthenticationManager authenticationManager,
                                     EmailAuthenticationSuccessHandler successHandler,
                                     EmailAuthenticationFailureHandler failureHandler) {
        super(new AntPathRequestMatcher("/api/auth/login", "POST"));
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException, IOException {

        log.info("이메일 로그인 인증 시도");

        try {
            LoginRequestDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

            log.info("로그인 요청 이메일: {}", loginRequest.getEmail());

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            return getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            log.error("로그인 요청 파싱 실패", e);
            throw new RuntimeException("Invalid login request format", e);
        }
    }
}
