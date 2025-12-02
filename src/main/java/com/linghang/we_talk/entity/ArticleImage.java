package com.linghang.we_talk.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章图片实体类
 */
@Data
public class ArticleImage {
    private Long id;
    private Long articleId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}