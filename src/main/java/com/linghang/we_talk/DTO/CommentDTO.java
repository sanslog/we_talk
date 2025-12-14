package com.linghang.we_talk.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;
    private Long parentId;
    private String content;
    private Long replyToUserId;
    private String replyToUserName;
    private Long replyToCommentId;  // 新增
    private Date createdAt;
    private List<CommentDTO> replies = new ArrayList<>();
}
