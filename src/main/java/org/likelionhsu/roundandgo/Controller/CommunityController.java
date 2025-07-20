package org.likelionhsu.roundandgo.Controller;

import lombok.RequiredArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.likelionhsu.roundandgo.Dto.CommunityRequestDto;
import org.likelionhsu.roundandgo.Dto.CommunityResponseDto;
import org.likelionhsu.roundandgo.Security.UserDetailsImpl;
import org.likelionhsu.roundandgo.Service.CommunityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 게시글 작성
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 게시글 작성 요청 DTO
     * @return 작성된 게시글 응답 DTO
     */
    @PostMapping
    public ResponseEntity<CommonResponse<CommunityResponseDto>> createPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CommunityRequestDto request) {
        CommunityResponseDto response = communityService.createCommunity(userDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.<CommunityResponseDto>builder()
                .statusCode(200)
                .msg("게시글 작성 완료")
                .data(response)
                .build());
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
     * 단일 게시글 조회
     * @param id 조회할 게시글 ID
     * @return 해당 게시글 응답 DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CommunityResponseDto>> getPost(@PathVariable Long id) {
        CommunityResponseDto response = communityService.getPost(id);
        return ResponseEntity.ok(CommonResponse.<CommunityResponseDto>builder()
                .statusCode(200)
                .msg("게시글 조회 성공")
                .data(response)
                .build());
    }

    /**
     *
     * @param id
     * @param userDetails
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<CommunityResponseDto>> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CommunityRequestDto request) {
        CommunityResponseDto response = communityService.updatePost(id, userDetails.getUser(), request);
        return ResponseEntity.ok(CommonResponse.<CommunityResponseDto>builder()
                .statusCode(200)
                .msg("게시글 수정 완료")
                .data(response)
                .build());
    }

    /**
     * 게시글 삭제
     * @param id
     * @param userDetails
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        communityService.deletePost(id, userDetails.getUser());
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .statusCode(200)
                .msg("게시글 삭제 완료")
                .build());
    }

    /**
     * 마이페이지 - 내가 작성한 게시글 조회
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 내가 작성한 게시글 응답 DTO 리스트
     */
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<CommunityResponseDto>>> getMyPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CommunityResponseDto> posts = communityService.getPostsByUser(userDetails.getUser());
        return ResponseEntity.ok(CommonResponse.<List<CommunityResponseDto>>builder()
                .statusCode(200)
                .msg("내가 작성한 게시글 조회 성공")
                .data(posts)
                .build());
    }
}
