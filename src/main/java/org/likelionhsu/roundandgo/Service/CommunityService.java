package org.likelionhsu.roundandgo.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Common.CommunityCategory;
import org.likelionhsu.roundandgo.Dto.Request.CommunityRequestDto;
import org.likelionhsu.roundandgo.Dto.Request.PostUpdateRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CommunityResponseDto;
import org.likelionhsu.roundandgo.Entity.Community;
import org.likelionhsu.roundandgo.Entity.CommunityLike;
import org.likelionhsu.roundandgo.Entity.PostImage;
import org.likelionhsu.roundandgo.Entity.User;
import org.likelionhsu.roundandgo.Repository.CommunityLikeRepository;
import org.likelionhsu.roundandgo.Repository.CommunityRepository;
import org.likelionhsu.roundandgo.Repository.PostImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository likeRepository;
    private final PostImageRepository postImageRepository;
    private final FileStorageService fileStorageService;

    /**
     * 게시글 생성 (이미지 포함)
     * @param user 작성자
     * @param request 게시글 정보
     * @param files 첨부할 이미지 파일들
     * @return 생성된 게시글 응답 DTO
     */
    @Transactional
    public CommunityResponseDto createCommunity(User user, CommunityRequestDto request, List<MultipartFile> files) {
        CommunityCategory category = CommunityCategory.fromLabel(request.getCategory());

        Community community = Community.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user.getNickname())
                .category(category)
                .user(user)
                .images(new ArrayList<>())
                .build();

        // 게시글 먼저 저장
        community = communityRepository.save(community);

        // 이미지 파일들 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        FileStorageService.FileStored fileStored = fileStorageService.savePostImage(file);

                        PostImage postImage = PostImage.builder()
                                .community(community)
                                .url(fileStored.url)
                                .storedPath(fileStored.storedPath)
                                .originalFilename(fileStored.originalFilename)
                                .size(fileStored.size)
                                .contentType(fileStored.contentType)
                                .build();

                        community.getImages().add(postImage);
                        postImageRepository.save(postImage);

                    } catch (Exception e) {
                        log.error("이미지 저장 실패: {}", e.getMessage(), e);
                        // 이미 저장된 이미지들 정리
                        cleanupUploadedFiles(community.getImages());
                        throw new RuntimeException("이미지 저장에 실패했습니다: " + e.getMessage());
                    }
                }
            }
        }

        return new CommunityResponseDto(community);
    }

    /**
     * 게시글 생성 (이미지 없이) - 기존 호환성을 위한 메서드
     * @param user 작성자
     * @param request 게시글 정보
     * @return 생성된 게시글 응답 DTO
     */
    @Transactional
    public CommunityResponseDto createCommunity(User user, CommunityRequestDto request) {
        return createCommunity(user, request, null);
    }

    public List<CommunityResponseDto> getAllPosts() {
        return communityRepository.findAll().stream()
                .map(CommunityResponseDto::new)
                .toList();
    }

    public List<CommunityResponseDto> getPostsByCategory(String category) {
        CommunityCategory categoryEnum = CommunityCategory.fromLabel(category);
        return communityRepository.findByCategory(categoryEnum).stream()
                .map(CommunityResponseDto::new)
                .toList();
    }

    public CommunityResponseDto getPost(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
        return new CommunityResponseDto(community);
    }

    /**
     * 게시글 수정 (이미지 포함)
     * @param id 게시글 ID
     * @param user 작성자
     * @param request 수정할 게시글 정보
     * @param newFiles 추가할 새 이미지 파일들
     * @return 수정된 게시글 응답 DTO
     */
    @Transactional
    public CommunityResponseDto updatePost(Long id, User user, PostUpdateRequestDto request, List<MultipartFile> newFiles) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

        if (!community.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        // 게시글 정보 수정
        community.setTitle(request.getTitle());
        community.setContent(request.getContent());
        community.setCategory(CommunityCategory.fromLabel(request.getCategory()));

        // 기존 이미지 처리
        List<PostImage> existingImages = new ArrayList<>(community.getImages());
        List<Long> keepImageIds = request.getKeepImageIds() != null ? request.getKeepImageIds() : new ArrayList<>();

        // 삭제할 이미지들 찾기 및 삭제
        List<PostImage> imagesToDelete = existingImages.stream()
                .filter(image -> !keepImageIds.contains(image.getId()))
                .toList();

        for (PostImage imageToDelete : imagesToDelete) {
            community.getImages().remove(imageToDelete);
            fileStorageService.deleteFile(imageToDelete.getStoredPath());
            postImageRepository.delete(imageToDelete);
        }

        // 새 이미지 파일들 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    try {
                        FileStorageService.FileStored fileStored = fileStorageService.savePostImage(file);

                        PostImage postImage = PostImage.builder()
                                .community(community)
                                .url(fileStored.url)
                                .storedPath(fileStored.storedPath)
                                .originalFilename(fileStored.originalFilename)
                                .size(fileStored.size)
                                .contentType(fileStored.contentType)
                                .build();

                        community.getImages().add(postImage);
                        postImageRepository.save(postImage);

                    } catch (Exception e) {
                        log.error("새 이미지 저장 실패: {}", e.getMessage(), e);
                        throw new RuntimeException("이미지 저장에 실패했습니다: " + e.getMessage());
                    }
                }
            }
        }

        return new CommunityResponseDto(communityRepository.save(community));
    }

    /**
     * 게시글 수정 (이미지 없이) - 기존 호환성을 위한 메서드
     * @param id 게시글 ID
     * @param user 작성자
     * @param request 수정할 게시글 정보
     * @return 수정된 게시글 응답 DTO
     */
    @Transactional
    public CommunityResponseDto updatePost(Long id, User user, CommunityRequestDto request) {
        // 기존 CommunityRequestDto를 PostUpdateRequestDto로 변환
        PostUpdateRequestDto updateRequest = PostUpdateRequestDto.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .keepImageIds(null) // 기존 이미지 모두 유지
                .build();
        return updatePost(id, user, updateRequest, null);
    }

    /**
     * 게시글 삭제 (연관된 이미지들도 함께 삭제)
     * @param id 게시글 ID
     * @param user 작성자
     */
    @Transactional
    public void deletePost(Long id, User user) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

        if (!community.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
        }

        // 연관된 이미지 파일들 삭제
        cleanupUploadedFiles(community.getImages());

        // 게시글 삭제 (cascade로 PostImage도 함께 삭제됨)
        communityRepository.delete(community);
    }

    /**
     * 업로드된 파일들 정리
     * @param images 정리할 이미지 목록
     */
    private void cleanupUploadedFiles(List<PostImage> images) {
        if (images != null) {
            for (PostImage image : images) {
                try {
                    fileStorageService.deleteFile(image.getStoredPath());
                } catch (Exception e) {
                    log.warn("파일 삭제 실패 (정리 과정): {}", e.getMessage());
                }
            }
        }
    }

    public List<CommunityResponseDto> getPostsByUser(User user) {
        return communityRepository.findByUser(user).stream()
                .map(CommunityResponseDto::new)
                .toList();
    }

    public boolean toggleLike(User user, Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Optional<CommunityLike> existingLike = likeRepository.findByUserAndCommunity(user, community);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return false; // 좋아요 취소
        } else {
            likeRepository.save(CommunityLike.builder()
                    .user(user)
                    .community(community)
                    .build());
            return true; // 좋아요 추가
        }
    }

    public int countLikes(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return likeRepository.countByCommunity(community);
    }

    public List<CommunityResponseDto> getTop3PopularPosts() {
        return communityRepository.findTop3ByLikes().stream()
                .map(CommunityResponseDto::new)
                .toList();
    }

    public List<CommunityResponseDto> getTop3PopularPostsByCategory(String category) {
        CommunityCategory categoryEnum = CommunityCategory.fromLabel(category);
        return communityRepository.findTop3ByCategoryOrderByLikes(categoryEnum).stream()
                .map(CommunityResponseDto::new)
                .toList();
    }
}
