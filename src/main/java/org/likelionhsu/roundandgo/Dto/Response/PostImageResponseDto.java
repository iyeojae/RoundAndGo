package org.likelionhsu.roundandgo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 이미지 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageResponseDto {
    
    /**
     * 이미지 ID
     */
    private Long id;
    
    /**
     * 이미지 공개 URL
     */
    private String url;
    
    /**
     * 원본 파일명
     */
    private String originalFilename;
    
    /**
     * 파일 크기 (bytes)
     */
    private Long size;
}
