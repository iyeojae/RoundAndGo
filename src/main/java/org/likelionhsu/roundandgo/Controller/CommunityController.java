package org.likelionhsu.roundandgo.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.Request.CommunityRequestDto;
import org.likelionhsu.roundandgo.Dto.Request.PostUpdateRequestDto;
import org.likelionhsu.roundandgo.Dto.Response.CommunityResponseDto;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.CommunityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final ObjectMapper objectMapper;

    /**
     * 게시글 작성 (이미지 포함)
     * @param userDetails 현재 로그인한 사용자 정보
     * @param postJson 게시글 작성 요청 DTO (JSON 문자열)
     * @param images 첨부할 이미지 파일들 (선택사항)
     * @return 작성된 게시글 응답 DTO
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<CommunityResponseDto>> createPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("post") String postJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        try {
            // JSON 문자열을 DTO로 변환
            CommunityRequestDto request = objectMapper.readValue(postJson, CommunityRequestDto.class);

            CommunityResponseDto response = communityService.createCommunity(
                    userDetails.getUser(), request, images);

            return ResponseEntity.ok(CommonResponse.<CommunityResponseDto>builder()
                    .statusCode(200)
                    .msg("게시글 작성 완료")
                    .data(response)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("게시글 작성 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<CommunityResponseDto>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("게시글 작성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<CommunityResponseDto>builder()
                            .statusCode(500)
                            .msg("게시글 작성에 실패했습니다")
                            .build());
        }
    }

    /**
     * 전체 게시글 조회
     * @return 전체 게시글 응답 DTO 리스트
     */
    @GetMapping
    public ResponseEntity<CommonResponse<List<CommunityResponseDto>>> getAllPosts() {
        List<CommunityResponseDto> posts = communityService.getAllPosts();
        return ResponseEntity.ok(CommonResponse.<List<CommunityResponseDto>>builder()
                .statusCode(200)
                .msg("전체 게시글 조회 성공")
                .data(posts)
                .build());
    }

    /**
     * 카테고리별 게시글 조회
     * @param category 조회할 카테고리 이름
     * @return 해당 카테고리에 속하는 게시글 응답 DTO 리스트
     */
    @GetMapping("/category")
    public ResponseEntity<CommonResponse<List<CommunityResponseDto>>> getPostsByCategory(@RequestParam String category) {
        List<CommunityResponseDto> posts = communityService.getPostsByCategory(category);
        return ResponseEntity.ok(CommonResponse.<List<CommunityResponseDto>>builder()
                .statusCode(200)
                .msg("카테고리별 게시글 조회 성공")
                .data(posts)
                .build());
    }

    /**
     * 제목 또는 내용에서 키워드 검색
     * @param keyword 검색할 키워드
     * @return 검색 결과 게시글 응답 DTO 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<CommunityResponseDto>>> searchPosts(@RequestParam String keyword) {
        try {
            List<CommunityResponseDto> posts = communityService.searchPosts(keyword);
            return ResponseEntity.ok(CommonResponse.<List<CommunityResponseDto>>builder()
                    .statusCode(200)
                    .msg("게시글 검색 성공")
                    .data(posts)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("게시글 검색 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CommunityResponseDto>>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("게시글 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<List<CommunityResponseDto>>builder()
                            .statusCode(500)
                            .msg("게시글 검색에 실패했습니다")
                            .build());
        }
    }

    /**
     * 단일 게시글 조회
     * @param id 조회할 게시글 ID
     * @return 해당 게시글 응답 DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CommunityResponseDto>> getPost(@PathVariable Long id) {
        try {
            CommunityResponseDto response = communityService.getPost(id);
            return ResponseEntity.ok(CommonResponse.<CommunityResponseDto>builder()
                    .statusCode(200)
                    .msg("게시글 조회 성공")
                    .data(response)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    CommonResponse.<CommunityResponseDto>builder()
                            .statusCode(404)
                            .msg(e.getMessage())
                            .build());
        }
    }

    /**
     * 게시글 수정 (이미지 포함)
     * @param id 수정할 게시글 ID
     * @param userDetails 현재 로그인한 사용자 정보
     * @param postJson 게시글 수정 요청 DTO (JSON 문자열)
     * @param images 첨부할 새 이미지 파일들 (선택사항)
     * @return 수정된 게시글 응답 DTO
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<CommunityResponseDto>> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("post") String postJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        try {
            // JSON 문자열을 DTO로 변환
            PostUpdateRequestDto request = objectMapper.readValue(postJson, PostUpdateRequestDto.class);

            CommunityResponseDto response = communityService.updatePost(
                    id, userDetails.getUser(), request, images);

            return ResponseEntity.ok(CommonResponse.<CommunityResponseDto>builder()
                    .statusCode(200)
                    .msg("게시글 수정 완료")
                    .data(response)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("게시글 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<CommunityResponseDto>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build());

        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        CommonResponse.<CommunityResponseDto>builder()
                                .statusCode(404)
                                .msg(e.getMessage())
                                .build());
            } else if (e.getMessage().contains("작성자만")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        CommonResponse.<CommunityResponseDto>builder()
                                .statusCode(403)
                                .msg(e.getMessage())
                                .build());
            } else {
                log.error("게시글 수정 실패: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        CommonResponse.<CommunityResponseDto>builder()
                                .statusCode(500)
                                .msg("게시글 수정에 실패했습니다")
                                .build());
            }
        } catch (Exception e) {
            log.error("게시글 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<CommunityResponseDto>builder()
                            .statusCode(500)
                            .msg("게시글 수정에 실패했습니다")
                            .build());
        }
    }

    /**
     * 게시글 삭제
     * @param id 삭제할 게시글 ID
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            communityService.deletePost(id, userDetails.getUser());

            return ResponseEntity.ok(CommonResponse.<Void>builder()
                    .statusCode(200)
                    .msg("게시글 삭제 완료")
                    .build());

        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        CommonResponse.<Void>builder()
                                .statusCode(404)
                                .msg(e.getMessage())
                                .build());
            } else if (e.getMessage().contains("작성자만")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        CommonResponse.<Void>builder()
                                .statusCode(403)
                                .msg(e.getMessage())
                                .build());
            } else {
                log.error("게시글 삭제 실패: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        CommonResponse.<Void>builder()
                                .statusCode(500)
                                .msg("게시글 삭제에 실패했습니다")
                                .build());
            }
        }
    }

    /**
     * 게시글 좋아요 토글
     * @param id 게시글 ID
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 좋아요 상태 응답
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<CommonResponse<Boolean>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            boolean isLiked = communityService.toggleLike(userDetails.getUser(), id);
            return ResponseEntity.ok(CommonResponse.<Boolean>builder()
                    .statusCode(200)
                    .msg(isLiked ? "좋아요 추가" : "좋아요 취소")
                    .data(isLiked)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    CommonResponse.<Boolean>builder()
                            .statusCode(404)
                            .msg(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("좋아요 토글 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<Boolean>builder()
                            .statusCode(500)
                            .msg("좋아요 처리에 실패했습니다")
                            .build());
        }
    }

    /**
     * 인기 게시글 TOP3 조회
     * @return 인기 게시글 TOP3 응답 DTO 리스트
     */
    @GetMapping("/popular")
    public ResponseEntity<CommonResponse<List<CommunityResponseDto>>> getPopularPosts() {
        try {
            List<CommunityResponseDto> popularPosts = communityService.getTop3PopularPosts();
            return ResponseEntity.ok(CommonResponse.<List<CommunityResponseDto>>builder()
                    .statusCode(200)
                    .msg("인기 게시글 TOP3 조회 성공")
                    .data(popularPosts)
                    .build());
        } catch (Exception e) {
            log.error("인기 게시글 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<List<CommunityResponseDto>>builder()
                            .statusCode(500)
                            .msg("인기 게시글 조회에 실패했습니다")
                            .build());
        }
    }

    /**
     * 카테고리별 인기 게시글 TOP3 조회
     * @param category 조회할 카테고리
     * @return 카테고리별 인기 게시글 TOP3 응답 DTO 리스트
     */
    @GetMapping("/popular/category")
    public ResponseEntity<CommonResponse<List<CommunityResponseDto>>> getPopularPostsByCategory(@RequestParam String category) {
        try {
            List<CommunityResponseDto> popularPosts = communityService.getTop3PopularPostsByCategory(category);
            return ResponseEntity.ok(CommonResponse.<List<CommunityResponseDto>>builder()
                    .statusCode(200)
                    .msg("카테고리별 인기 게시글 TOP3 조회 성공")
                    .data(popularPosts)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("카테고리별 인기 게시글 조회 실패 - 잘못된 카테고리: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    CommonResponse.<List<CommunityResponseDto>>builder()
                            .statusCode(400)
                            .msg(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("카테고리별 인기 게시글 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<List<CommunityResponseDto>>builder()
                            .statusCode(500)
                            .msg("카테고리별 인기 게시글 조회에 실패했습니다")
                            .build());
        }
    }

    /**
     * 게시글 좋아요 수 조회
     * @param id 게시글 ID
     * @return 해당 게시글의 좋아요 수
     */
    @GetMapping("/likeCount/{id}")
    public ResponseEntity<CommonResponse<Integer>> getLikeCount(@PathVariable Long id) {
        try {
            int likeCount = communityService.countLikes(id);
            return ResponseEntity.ok(CommonResponse.<Integer>builder()
                    .statusCode(200)
                    .msg("좋아요 수 조회 성공")
                    .data(likeCount)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    CommonResponse.<Integer>builder()
                            .statusCode(404)
                            .msg(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("좋아요 수 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.<Integer>builder()
                            .statusCode(500)
                            .msg("좋아요 수 조회에 실패했습니다")
                            .build());
        }
    }
}
