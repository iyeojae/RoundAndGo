package org.likelionhsu.roundandgo.Dto.Response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommunityCategory;
import org.likelionhsu.roundandgo.Entity.Community;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CommunityResponseDto {
    private Long id; // 게시글 ID
    private Long authorId; // 작성자 ID
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String author; // 작성자 이름
    private CommunityCategory category; // 게시글 카테고리 (ENUM 타입)
    private List<PostImageResponseDto> images; // 첨부된 이미지들
    private LocalDateTime createdAt; // 작성일시
    private LocalDateTime updatedAt; // 수정일시

    public CommunityResponseDto(Community community) {
        this.id = community.getId();
        this.authorId = community.getUser().getId();
        this.title = community.getTitle();
        this.content = community.getContent();
        this.author = community.getAuthor();
        this.category = community.getCategory();
        this.images = community.getImages().stream()
                .map(image -> PostImageResponseDto.builder()
                        .id(image.getId())
                        .url(image.getUrl())
                        .originalFilename(image.getOriginalFilename())
                        .size(image.getSize())
                        .build())
                .collect(Collectors.toList());
        this.createdAt = community.getCreatedAt();
        this.updatedAt = community.getModifiedAt();
    }
}
