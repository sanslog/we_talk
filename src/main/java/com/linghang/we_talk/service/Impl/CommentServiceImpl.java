package com.linghang.we_talk.service.Impl;

import com.linghang.we_talk.DTO.CommentDTO;
import com.linghang.we_talk.entity.Comment;
import com.linghang.we_talk.mapper.CommentMapper;
import com.linghang.we_talk.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    /**
     * @apiNote 该接口用于构建评论树
     * @return 返回一个数组，为一个构建好的二级评论树
     * */
    @Cacheable(value = "commentTree#30m",key = "#postId",sync = true)
    @Override
    public List<CommentDTO> getComments(Long postId) {
        List<Comment> comments = commentMapper.findByPostId(postId);
        return buildCommentTree(comments);
    }

    /**
     * @apiNote 请不要在该实现类之外使用这个接口，避免造成不必要的代码混乱
     * @param comments 应当传入的评论数组
     * */
    @Override
    public List<CommentDTO> buildCommentTree(List<Comment> comments) {
        // 按parentId分组：一级评论的parentId为null，二级评论的parentId为一级评论ID
        Map<Long, List<CommentDTO>> replyMap = new HashMap<>();
        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        // 转换所有评论为DTO
        for (Comment comment : comments) {
            CommentDTO dto = convertToDTO(comment);
            dtoMap.put(comment.getId(), dto);

            Long parentId = comment.getParentId() != null ? comment.getParentId() : 0L;
            replyMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dto);
        }

        // 构建树形结构
        List<CommentDTO> rootComments = replyMap.getOrDefault(0L, new ArrayList<>());

        // 为每个一级评论设置二级评论
        for (CommentDTO root : rootComments) {
            List<CommentDTO> childComments = replyMap.getOrDefault(root.getId(), new ArrayList<>());

            // 如果需要构建二级评论之间的回复链，可以在这里处理
            // 但目前我们保持平级显示，通过 replyToCommentId 来标识回复关系
            root.setReplies(childComments);
        }

        return rootComments;
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setUserName(comment.getUserName());
        dto.setParentId(comment.getParentId());
        dto.setContent(comment.getContent());
        dto.setReplyToUserId(comment.getReplyToUserId());
        dto.setReplyToUserName(comment.getReplyToUserName());
        dto.setReplyToCommentId(comment.getReplyToCommentId());  // 设置回复的评论ID
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    /**
     * @apiNote  添加评论的接口
     * @param comment 需要传入一个Comment对象，该方法仅提供基础非空校验，对于复杂的评论间指向关系请自行实现
     */
    @CacheEvict(value = "commentTree",key = "#comment.postId")
    @Override
    public void addComment(Comment comment) {
        // 如果是回复二级评论，需要确保parent_id指向正确的一级评论
        if (comment.getReplyToCommentId() != null && comment.getParentId() == null) {
            Comment repliedComment = commentMapper.findById(comment.getReplyToCommentId());
            if (repliedComment != null) {
                // 如果回复的是二级评论，那么新评论的parent_id应该与它相同（指向一级评论）
                comment.setParentId(repliedComment.getParentId() != null ?
                        repliedComment.getParentId() : repliedComment.getId());
            }
        }

        commentMapper.save(comment);
    }
}

