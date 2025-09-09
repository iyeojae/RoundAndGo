package org.likelionhsu.roundandgo.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.likelionhsu.roundandgo.Dto.Request.PasswordResetRequestDto;
import org.likelionhsu.roundandgo.Dto.Request.PasswordChangeRequestDto;
import org.likelionhsu.roundandgo.Dto.Request.NicknameCheckRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.NicknameCheckResponseDto;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
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

        // 유저 즉시 생성 (이메일 인증 없이)
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .loginType(LoginType.EMAIL)
                .role(Role.ROLE_USER)
                .isActived(true)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("회원가입이 완료되었습니다. 로그인해 주세요.")
                .build());
    }

    // 비밀번호 재설정 - 1단계: 이메일 입력 및 인증 메일 발송
    @PostMapping("/password-reset/request")
    public ResponseEntity<CommonResponse<Void>> requestPasswordReset(@RequestBody PasswordResetRequestDto dto) {
        // 해당 이메일로 가입된 사용자가 있는지 확인
        if (userRepository.findByEmail(dto.getEmail()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .msg("해당 이메일로 가입된 계정이 없습니다.")
                            .build());
        }

        // 기존 인증 요청이 있다면 삭제 (중복 방지)
        emailVerificationRepository.deleteByEmail(dto.getEmail());

        // 인증 토큰 생성
        String token = UUID.randomUUID().toString();

        // 이메일 인증 정보 임시 저장
        EmailVerification verification = EmailVerification.builder()
                .email(dto.getEmail())
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 30분 유효
                .isVerified(false)
                .build();

        emailVerificationRepository.save(verification);

        // 인증 메일 발송
        String link = "http://roundandgo.onrender.com/api/auth/password-reset/verify?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        emailService.sendVerificationEmail(dto.getEmail(), link);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("비밀번호 재설정을 위한 인증 메일이 전송되었습니다. 이메일을 확인해 주세요.")
                .build());
    }

    // 비밀번호 재설정 - 2단계: 이메일 링크 클릭으로 인증
    @GetMapping("/password-reset/verify")
    public ResponseEntity<CommonResponse<Void>> verifyPasswordResetEmail(@RequestParam String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("인증 링크가 만료되었습니다.")
                            .build());
        }

        // 이메일 인증 완료 처리
        verification.setVerified(true);
        emailVerificationRepository.save(verification);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("이메일 인증이 완료되었습니다. 사이트로 돌아가서 새 비밀번호를 설정해 주세요.")
                .build());
    }

    // 비밀번호 재설정 - 3단계: 사이트에서 새 비밀번호 입력으로 비밀번호 변경
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<CommonResponse<Void>> confirmPasswordReset(@RequestBody PasswordChangeRequestDto dto) {
        // 이메일 인증이 완료된 요청인지 확인
        EmailVerification verification = emailVerificationRepository.findByEmailAndIsVerified(dto.getEmail(), true)
                .orElse(null);

        if (verification == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("이메일 인증이 완료되지 않았습니다.")
                            .build());
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.<Void>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .msg("인증이 만료되었습니다. 다시 시도해 주세요.")
                            .build());
        }

        // 해당 이메일로 가입된 사용자 정보 조회
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // 인증 정보 삭제 (1회용)
        emailVerificationRepository.delete(verification);

        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .msg("비밀번호가 성공적으로 변경되었습니다.")
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

    // 🆕 추가된 사용자 정보 조회 엔드포인트
    @GetMapping("/user")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getCurrentUser(HttpServletRequest request) {
        try {
            log.info("사용자 정보 조회 요청 - 토큰 추출 시작");

            // JWT 토큰에서 사용자 정보 추출
            String token = extractTokenFromRequest(request);
            if (token == null) {
                log.warn("토큰을 찾을 수 없음");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(CommonResponse.<Map<String, Object>>builder()
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .msg("토큰이 없습니다")
                                .build());
            }

            log.info("토큰 발견 - 길이: {}", token.length());

            // JWT 토큰 유효성 검증
            if (!jwtProvider.validateToken(token)) {
                log.warn("유효하지 않은 토큰");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(CommonResponse.<Map<String, Object>>builder()
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .msg("유효하지 않은 토큰입니다")
                                .build());
            }

            // JWT에서 이메일 추출
            String email = jwtProvider.getEmailFromToken(token);
            log.info("토큰에서 추출된 이메일: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 사용자 정보 반환
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("loginType", user.getLoginType().toString());
            userInfo.put("role", user.getRole().toString());

            log.info("사용자 정보 조회 성공: {}", user.getEmail());

            return ResponseEntity.ok(CommonResponse.<Map<String, Object>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .msg("사용자 정보 조회 성공")
                    .data(userInfo)
                    .build());

        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.<Map<String, Object>>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .msg("서버 오류")
                            .build());
        }
    }

    // 닉네임 중복 확인 엔드포인트
    @PostMapping("/check-nickname")
    public ResponseEntity<CommonResponse<NicknameCheckResponseDto>> checkNickname(@RequestBody NicknameCheckRequestDto requestDto) {
        try {
            String nickname = requestDto.getNickname();

            // 닉네임 중복 확인
            boolean isAvailable = userRepository.findByNickname(nickname).isEmpty();

            NicknameCheckResponseDto responseDto = NicknameCheckResponseDto.builder()
                    .isAvailable(isAvailable)
                    .message(isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.")
                    .build();

            return ResponseEntity.ok(CommonResponse.<NicknameCheckResponseDto>builder()
                    .statusCode(HttpStatus.OK.value())
                    .msg(isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.")
                    .data(responseDto)
                    .build());

        } catch (Exception e) {
            log.error("닉네임 중복 확인 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.<NicknameCheckResponseDto>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .msg("서버 오류가 발생했습니다.")
                            .data(NicknameCheckResponseDto.builder()
                                    .isAvailable(false)
                                    .message("서버 오류가 발생했습니다.")
                                    .build())
                            .build());
        }
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * Authorization 헤더 또는 쿠키에서 토큰을 찾습니다
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Authorization 헤더에서 추출
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            log.info("Authorization 헤더에서 토큰 발견");
            return bearerToken.substring(7);
        }

        // 2. 쿠키에서 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    log.info("쿠키에서 accessToken 발견");
                    return cookie.getValue();
                }
            }
        }

        log.warn("토큰을 찾을 수 없음 - Authorization 헤더와 쿠키 모두 확인함");
        return null;
    }
}