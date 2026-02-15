package com.linghang.we_talk.controller;

import com.linghang.we_talk.DTO.ArticleCreateRequest;
import com.linghang.we_talk.DTO.ArticleUpdateRequest;
import com.linghang.we_talk.DTO.ArticleVO;
import com.linghang.we_talk.entity.User;
import com.linghang.we_talk.service.ArticleService;
import com.linghang.we_talk.service.UserService;
import com.linghang.we_talk.utils.JwtUtil;
import com.linghang.we_talk.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    private final JwtUtil jwtUtil;

    private final UserService userService;


    /**
     * 创建文章
     */
    @Operation(summary = "创建文章",description = "分类ID是需要你手动传的，为空的话数据库里没有默认值。")
    @PostMapping("/create")
    public Result<?> createArticle(
            @Valid @RequestBody ArticleCreateRequest request,@RequestAttribute("userid") String userid) {
        //安全升级
        request.setUserId(Long.parseLong(userid));
        Long articleId = articleService.createArticle(request);
        return Result.succeed(articleId);
    }

    /**
     * 更新文章
     */
    @Operation(summary = "更新文章")
    @PostMapping("/upt")
    public Result<?> updateArticle(
            @Valid @RequestBody ArticleUpdateRequest request,@RequestAttribute("userid") String userid) {

        ArticleVO articleDetail = articleService.getArticleDetail(request.getId());
        Long userId = Long.parseLong(userid);

        if(!Objects.equals(articleDetail.getUserId(), userId)) return Result.error("非法更新");

        articleService.updateArticle(request);
        return Result.succeed();
    }

    /**
     * 删除文章
     */
    @Operation(summary = "删除文章")
    @GetMapping("/del/{id}")
    public Result<?> deleteArticle(
            @PathVariable Long id,
            @RequestAttribute("userid") String userid) {

        try {
            ArticleVO articleDetail = articleService.getArticleDetail(id);
            Long userId = Long.parseLong(userid);

            if(!Objects.equals(articleDetail.getUserId(), userId)) return Result.error("非法删除");

            articleService.deleteArticle(id, userId);

            return Result.succeed();
        } catch (NumberFormatException e) {
            return  Result.error("删除失败，请检查参数");
        }
    }

    /**
     * 获取文章详情
     */
    @Operation(summary = "根据ID获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {

        try {
            ArticleVO article = articleService.getArticleDetail(id);
            return Result.succeed(article);
        } catch (Exception e) {
            log.info("文章不存在或者被软删除，ID：{},{}",id,e.getMessage());
            return Result.error("文章不存在");
        }
    }

    /**
     * 分页查询文章列表
     */
    @Operation(summary = "分页查询文章列表")
    @GetMapping
    public Result<Page<ArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {
        Page<ArticleVO> articles = articleService.getArticleList(status, categoryId, page, size);
        return Result.succeed(articles);
    }

    /**
     * 获取用户文章列表
     */
    @Operation(summary = "获取用户所有文章")
    @GetMapping("/user/{userId}")//无需安全升级，为公共接口
    public Result<List<ArticleVO>> getUserArticles(@PathVariable Long userId) {
        List<ArticleVO> articles = articleService.getUserArticles(userId);
        return Result.succeed(articles);
    }

    /**
     * 点赞文章，不主动暴露接口
     */
    @Operation(summary = "点赞文章/取消点赞，需要指定文章ID")
    @PostMapping("/{id}")
    public Result<?> likeOrDislikeArticle(@RequestHeader("Authorization") String accessToken,@PathVariable Long id,@RequestParam("lod") boolean isLike) {
        //添加一个 用户-点赞-文章 关系表
        try{
            String username =  jwtUtil.validateToken(accessToken);

            User user = userService.getUserByName(username);
            Long userId = user.getId();

            if (isLike) {
                articleService.likeArticle(id, userId);
            } else {
                articleService.disLikeArticle(id, userId);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            String msg = isLike ? "点赞失败" : "取消点赞失败";
            return Result.error(msg);
        }
        return Result.succeed();
    }

    @Operation(summary = "获取用户是否点赞了该文章")
    @GetMapping("/liked/{id}")
    public Result<?> likeStatus(@RequestHeader("Authorization") String accessToken, @PathVariable("id") Long articleId){
        String username =  jwtUtil.validateToken(accessToken);

        User user = userService.getUserByName(username);
        Long userId = user.getId();

        return Result.succeed(articleService.checkLiked(articleId,userId));
    }

    @Operation(summary = "访问该接口，则表示指定id的文章浏览量加一")
    @PostMapping("/{id}/view")
    public Result<?> viewArticle(@PathVariable Long id) {
        //操作内存
        try {
            articleService.readArticle(id);
        } catch (Exception e) {
            return Result.error();
        }
        return Result.succeed();
    }
}
