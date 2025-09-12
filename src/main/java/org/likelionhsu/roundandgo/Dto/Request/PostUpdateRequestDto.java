package org.likelionhsu.roundandgo.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDto {

    /**
     * 게시글 제목
     */
    private String title;

    /**
     * 게시글 내용
     */
    private String content;

    /**
     * 게시글 카테고리
     */
    private String category;

    /**
     * 유지할 기존 이미지 ID 목록
     * 이 목록에 포함되지 않은 기존 이미지들은 삭제됨
     */
    private List<Long> keepImageIds;
}
