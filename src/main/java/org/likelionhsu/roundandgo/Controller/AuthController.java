package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Common.LoginType;
import org.likelionhsu.roundandgo.Common.Role;
import org.likelionhsu.roundandgo.Entity.EmailVerification;
import org.likelionhsu.roundandgo.Repository.EmailVerificationRepository;
import org.likelionhsu.roundandgo.Security.dto.LoginRequestDto;
import org.likelionhsu.roundandgo.Security.dto.LoginResponseDto;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.RefreshTokenRepository;
import org.likelionhsu.roundandgo.Repository.UserRepository;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Security.dto.SignupRequestDto;
import org.likelionhsu.roundandgo.Security.jwt.JwtProvider;
import org.likelionhsu.roundandgo.Security.jwt.RefreshToken;
import org.likelionhsu.roundandgo.Service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService; // 이메일 서비스 추가

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
        // 이미 가입된 사용자 체크
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.CONFLICT.value())
                            .msg("이미 가입된 이메일입니다.")
                            .build());
        }

        // 기존 인증 요청이 있다면 삭제 (중복 방지)
        emailVerificationRepository.deleteByEmail(dto.getEmail());

        // 인증 토큰 생성
        String token = UUID.randomUUID().toString();

        // 이메일 인증 정보 임시 저장 (비밀번호는 암호화된 상태로)
        EmailVerification verification = EmailVerification.builder()
                .email(dto.getEmail())
                .encodedPassword(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 30분 유효
                .build();

        emailVerificationRepository.save(verification);

        // 인증 메일 발송
        String link = "http://roundandgo.onrender.com/api/auth/verify-email?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        emailService.sendVerificationEmail(dto.getEmail(), link);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("인증 메일이 전송되었습니다. 이메일을 확인해 주세요.")
                .build());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<CommonResponse<Void>> verifyEmail(@RequestParam String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("인증 링크가 만료되었습니다.")
                            .build());
        }

        // 유저 생성
        User user = User.builder()
                .email(verification.getEmail())
                .password(verification.getEncodedPassword())
                .nickname(verification.getNickname())
                .loginType(LoginType.EMAIL)
                .role(Role.ROLE_USER)
                .isActived(true)
                .build();

        userRepository.save(user);
        emailVerificationRepository.delete(verification); // 인증 후 삭제

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.")
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
