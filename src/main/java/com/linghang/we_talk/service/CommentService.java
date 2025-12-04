package com.linghang.we_talk.service;

import com.linghang.we_talk.DTO.CommentDTO;
import com.linghang.we_talk.entity.Comment;

import java.util.List;

public interface CommentService {

    List<CommentDTO> getComments(Long postId);

    List<CommentDTO> buildCommentTree(List<Comment> comments);

    void addComment(Comment comment);

    void deleteComment(Long id, Long postId);
}

