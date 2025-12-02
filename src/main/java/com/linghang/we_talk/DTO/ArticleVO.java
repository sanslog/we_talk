package com.linghang.we_talk.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章视图对象
 */
@Data
public class ArticleVO {
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
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private List<ArticleImageVO> images;
}
