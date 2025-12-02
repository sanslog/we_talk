package com.linghang.we_talk.DTO;

import lombok.Data;

/**
 * 文章图片视图对象
 */
@Data
public class ArticleImageVO {
    private Long id;
    private String imageUrl;
    private Integer sortOrder;
}
