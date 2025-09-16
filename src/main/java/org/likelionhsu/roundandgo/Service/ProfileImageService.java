package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileImageService {

    private final UserRepository userRepository;

    @Value("${upload.profile-dir}")
    private String profileDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadProfileImage(MultipartFile file, Long userId) {
        return uploadProfileImageWithNickname(file, userId, null);
    }

    public String uploadProfileImageWithNickname(MultipartFile file, Long userId, String nickname) {
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 프로필 이미지가 있다면 삭제
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            deleteExistingFile(user.getProfileImage());
        }

        // 새 파일 저장
        String fileName = generateFileName(file.getOriginalFilename());
        saveFile(file, fileName);
        String imageUrl = generateImageUrl(fileName);

        // DB 업데이트
        user.setProfileImage(imageUrl);

        // 닉네임이 제공된 경우 업데이트
        if (nickname != null && !nickname.trim().isEmpty()) {
            // 닉네임 중복 검사
            if (userRepository.existsByNicknameAndIdNot(nickname.trim(), userId)) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(nickname.trim());
        }

        userRepository.save(user);

        return imageUrl;
    }

    public String getProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return user.getProfileImage();
    }

    public User getUserProfileInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            deleteExistingFile(user.getProfileImage());
            user.setProfileImage(null);
            userRepository.save(user);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("파일 크기가 5MB를 초과합니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("파일명이 올바르지 않습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("지원하지 않는 파일 형식입니다. (png, jpg, jpeg, webp만 허용)");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new RuntimeException("파일 확장자가 없습니다.");
        }
        return filename.substring(lastDotIndex + 1);
    }

    private void saveFile(MultipartFile file, String fileName) {
        try {
            // 디렉토리 생성
            Path uploadPath = Paths.get(profileDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            log.info("파일 저장 완료: {}", filePath);

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장에 실패했습니다.");
        }
    }

    private String generateImageUrl(String fileName) {
        return "https://roundandgo.shop/uploads/profile/" + fileName;
    }

    private void deleteExistingFile(String imageUrl) {
        try {
            if (imageUrl.startsWith("https://roundandgo.shop/uploads/profile/")) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(profileDir, fileName);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("기존 파일 삭제 완료: {}", filePath);
                }
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
}
