package org.likelionhsu.roundandgo.Dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommunityCategory;
import org.likelionhsu.roundandgo.Entity.Community;

@Data
@NoArgsConstructor
public class CommunityResponseDto {
    private Long id; // 게시글 ID
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String author; // 작성자 이름
    private CommunityCategory category; // 게시글 카테고리 (ENUM 타입)

    public CommunityResponseDto(Community community) {
        this.id = community.getId();
        this.title = community.getTitle();
        this.content = community.getContent();
        this.author = community.getAuthor();
        this.category = community.getCategory();
    }
}
