package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Common.LoginType;
import org.likelionhsu.roundandgo.Common.Role;
import org.likelionhsu.roundandgo.Security.dto.LoginRequestDto;
import org.likelionhsu.roundandgo.Security.dto.LoginResponseDto;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.RefreshTokenRepository;
import org.likelionhsu.roundandgo.Repository.UserRepository;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Security.dto.SignupRequestDto;
import org.likelionhsu.roundandgo.Security.jwt.JwtProvider;
import org.likelionhsu.roundandgo.Security.jwt.RefreshToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(token);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtProvider.createAccessToken(userDetails.getUser());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUser());

        LoginResponseDto response = new LoginResponseDto(accessToken, refreshToken);
        return ResponseEntity.ok(
                CommonResponse.<LoginResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .msg("로그인 성공")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> signup(@RequestBody SignupRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.CONFLICT.value())
                            .msg("이미 가입된 이메일입니다.")
                            .build());
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .loginType(LoginType.EMAIL)
                .nickname(dto.getNickname())
                .role(Role.ROLE_USER)
                .isActived(true)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("회원가입이 완료되었습니다.")
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<LoginResponseDto>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.<LoginResponseDto>builder()
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .msg("유효하지 않은 리프레시 토큰입니다.")
                            .build());
        }

        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰 없음"));

        User user = userRepository.findById(savedToken.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        String newAccessToken = jwtProvider.createAccessToken(user);
        String newRefreshToken = jwtProvider.createRefreshToken(user);

        savedToken.setToken(newRefreshToken);
        savedToken.setExpiryDate(LocalDateTime.now().plusDays(14));
        refreshTokenRepository.save(savedToken);

        return ResponseEntity.ok(CommonResponse.<LoginResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("토큰 재발급 완료")
                .data(new LoginResponseDto(newAccessToken, newRefreshToken))
                .build());
    }
}
