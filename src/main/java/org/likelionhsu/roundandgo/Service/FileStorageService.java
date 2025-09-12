package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 저장 및 관리 서비스
 * 이미지 파일을 서버 디스크에 저장하고 URL을 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${upload.posts-dir}")
    private String postsUploadDir;

    private static final String BASE_URL = "https://roundandgo.shop/uploads/posts";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "webp");
    private static final long MAX_FILE_SIZE = 6 * 1024 * 1024; // 6MB

    /**
     * 파일 저장 결과를 담는 DTO
     */
    public static class FileStored {
        public final String storedPath;
        public final String url;
        public final long size;
        public final String contentType;
        public final String originalFilename;

        public FileStored(String storedPath, String url, long size, String contentType, String originalFilename) {
            this.storedPath = storedPath;
            this.url = url;
            this.size = size;
            this.contentType = contentType;
            this.originalFilename = originalFilename;
        }
    }

    /**
     * 게시글 이미지 파일 저장
     * @param file 업로드된 파일
     * @return 저장 결과 정보
     * @throws IllegalArgumentException 파일 검증 실패 시
     * @throws RuntimeException 파일 저장 실패 시
     */
    public FileStored savePostImage(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;

        // 날짜별 디렉토리 생성 (yy/MM/dd)
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yy/MM/dd"));

        Path uploadPath = Paths.get(postsUploadDir, datePath);
        Path filePath = uploadPath.resolve(filename);

        try {
            // 디렉토리 생성
            Files.createDirectories(uploadPath);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            log.info("파일 저장 완료: {}", filePath.toString());

            String url = BASE_URL + "/" + datePath + "/" + filename;

            return new FileStored(
                filePath.toString(),
                url,
                file.getSize(),
                file.getContentType(),
                originalFilename
            );

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 저장된 파일 삭제
     * @param storedPath 저장된 파일의 절대 경로
     */
    public void deleteFile(String storedPath) {
        try {
            Path path = Paths.get(storedPath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("파일 삭제 완료: {}", storedPath);
            } else {
                log.warn("삭제할 파일이 존재하지 않습니다: {}", storedPath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            // 파일 삭제 실패는 로그만 남기고 예외를 던지지 않음
        }
    }

    /**
     * 파일 검증
     * @param file 검증할 파일
     * @throws IllegalArgumentException 검증 실패 시
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 6MB를 초과합니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("올바른 파일 확장자가 필요합니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용 형식: " + ALLOWED_EXTENSIONS);
        }
    }

    /**
     * 파일 확장자 추출
     * @param filename 파일명
     * @return 확장자
     */
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
