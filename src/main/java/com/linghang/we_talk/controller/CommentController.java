package com.linghang.we_talk.controller;

import com.linghang.we_talk.entity.Comment;
import com.linghang.we_talk.service.CommentService;
import com.linghang.we_talk.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Tag(name = "CommentController",description = "用于实现简单的评论添加和删除功能")
@RequestMapping("/cmt")
public class CommentController {

    @Autowired
    private CommentService commentService;

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
}
