package com.linghang.we_talk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private Long id;//该条评论的id
    private Long postId;// 指向发布的文章的id
    private Long userId;//指向发布该条评论的用户的id
    private String userName;//发布该评论的用户的名称
    private Long parentId;//指向评论树的根评论
    private String content;//内容
    private Long replyToUserId;//指向回复的用户评论的id
    private String replyToUserName;//指向回复的用户的名称
    private Long replyToCommentId; // 新增：回复的具体评论ID
    private Date createdAt;
}