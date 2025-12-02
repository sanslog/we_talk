package com.linghang.we_talk.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体类
 */
@Data
public class Article {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String coverImage;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer categoryId;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 非数据库字段
    private List<ArticleImage> images;
    private String authorName;
}

