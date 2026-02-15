package com.linghang.we_talk.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 文章更新请求DTO
 */
@Data
public class ArticleUpdateRequest {

    @NotNull(message = "文章ID不能为空")
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String coverImage;

    private Integer categoryId;

    private List<String> tags;

    private List<String> images;

    private Integer status;
}