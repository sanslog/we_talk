package com.linghang.we_talk.mapper;

import com.linghang.we_talk.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 查询帖子所有评论
    List<Comment> findByPostId(@Param("postId") Long postId);

    // 分页查询一级评论
    List<Comment> findRootCommentsByPage(@Param("postId") Long postId,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    // 查询二级评论
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId,
                                        @Param("postId") Long postId);

    // 插入评论
    void save(Comment comment);

    // 根据ID查询评论
    Comment findById(@Param("id") Long id);

    // 统计一级评论数量
    int countRootComments(@Param("postId") Long postId);

    int deleteById(@Param("id") Long id);
}
