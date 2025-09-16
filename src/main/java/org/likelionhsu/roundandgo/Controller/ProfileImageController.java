package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.ProfileImageResponseDto;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.ProfileImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PostMapping("/image")
    public ResponseEntity<CommonResponse<ProfileImageResponseDto>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nickname", required = false) String nickname,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            String imageUrl = profileImageService.uploadProfileImageWithNickname(
                    file, userDetails.getUser().getId(), nickname);

            // 업데이트된 사용자 정보 조회
            User updatedUser = profileImageService.getUserProfileInfo(userDetails.getUser().getId());

            ProfileImageResponseDto response = ProfileImageResponseDto.builder()
                    .url(imageUrl)
                    .nickname(updatedUser.getNickname())
                    .build();

            return ResponseEntity.ok(
                    CommonResponse.<ProfileImageResponseDto>builder()
                            .statusCode(200)
                            .msg("프로필 이미지 업로드 성공")
                            .data(response)
                            .build()
            );
        } catch (RuntimeException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<ProfileImageResponseDto>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/image")
    public ResponseEntity<CommonResponse<ProfileImageResponseDto>> getProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = profileImageService.getUserProfileInfo(userDetails.getUser().getId());

            ProfileImageResponseDto response = ProfileImageResponseDto.builder()
                    .url(user.getProfileImage())
                    .nickname(user.getNickname())
                    .build();

            return ResponseEntity.ok(
                    CommonResponse.<ProfileImageResponseDto>builder()
                            .statusCode(200)
                            .msg("프로필 이미지 조회 성공")
                            .data(response)
                            .build()
            );
        } catch (RuntimeException e) {
            log.error("프로필 이미지 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<ProfileImageResponseDto>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build()
            );
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<CommonResponse<Void>> deleteProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            profileImageService.deleteProfileImage(userDetails.getUser().getId());

            return ResponseEntity.ok(
                    CommonResponse.<Void>builder()
                            .statusCode(200)
                            .msg("프로필 이미지 삭제 성공")
                            .build()
            );
        } catch (RuntimeException e) {
            log.error("프로필 이미지 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<Void>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build()
            );
        }
    }
}
