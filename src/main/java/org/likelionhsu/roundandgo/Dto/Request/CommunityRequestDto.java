package org.likelionhsu.roundandgo.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.likelionhsu.roundandgo.Common.CommunityCategory;

@Data
@NoArgsConstructor @AllArgsConstructor
public class CommunityRequestDto {
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String category; // 게시글 카테고리 (ENUM 타입)
}
