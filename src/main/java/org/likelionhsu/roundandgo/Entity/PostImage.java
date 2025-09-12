package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.likelionhsu.roundandgo.Common.Timestamped;

/**
 * 게시글 이미지 엔티티
 * 게시글에 첨부된 이미지 정보를 저장
 */
@Entity
@Table(name = "post_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이미지가 속한 게시글
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    /**
     * 클라이언트에 제공할 공개 URL
     * 예: https://roundandgo.shop/uploads/posts/24/01/15/uuid.jpg
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * 서버에 저장된 파일의 절대 경로
     * 파일 삭제 시 사용
     */
    @Column(nullable = false, length = 500)
    private String storedPath;

    /**
     * 원본 파일명
     */
    @Column(nullable = false, length = 255)
    private String originalFilename;

    /**
     * 파일 크기 (bytes)
     */
    @Column(nullable = false)
    private Long size;

    /**
     * 파일 MIME 타입
     */
    @Column(nullable = false, length = 100)
    private String contentType;
}
