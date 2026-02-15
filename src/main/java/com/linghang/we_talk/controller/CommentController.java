package com.linghang.we_talk.controller;

import com.linghang.we_talk.DTO.CommentDTO;
import com.linghang.we_talk.entity.Comment;
import com.linghang.we_talk.service.CommentService;
import com.linghang.we_talk.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "CommentController",description = "用于实现简单的评论添加和删除功能")
@RequestMapping("/cmt")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public Result<?> addComment(@RequestBody Comment comment){
        try{
            commentService.addComment(comment);
        } catch (Exception e) {
            log.error("添加评论接口出错{}",e.getMessage());
            return Result.error("评论添加失败");
        }
        return Result.succeed();
    }

    @GetMapping("/del")
    public Result<?> delComment(@RequestParam("comment_id") Long id,@RequestParam("post_id") Long postId){
        try{
            commentService.deleteComment(id,postId);
        } catch (Exception e) {
            log.error("删除评论失败：{}",e.getMessage());
            return Result.error("删除评论失败");
        }
        return Result.succeed();
    }

    @Operation(summary = "根据文章ID查找对应评论")
    @GetMapping
    public Result<?> getComments(@PathParam("id") Long id){
        try {
            List<CommentDTO> comments = commentService.getComments(id);
            return Result.succeed(comments);
        } catch (Exception e) {
            return Result.error(500,"服务器内部错误");
        }
    }
}
