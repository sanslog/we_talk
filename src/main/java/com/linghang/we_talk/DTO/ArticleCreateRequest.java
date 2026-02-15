package com.linghang.we_talk.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 文章创建请求DTO
 */
@Data
public class ArticleCreateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String nickname;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private Integer categoryId;

    private List<String> tags;

    private List<String> images;
}