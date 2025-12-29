//package com.linghang.we_talk.controller;
//
//import com.linghang.we_talk.entity.Comment;
//import com.linghang.we_talk.utils.Result;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Tag(name = "articleController",description = "关于文章的一些CRUD服务")
//@RestController
//@RequestMapping("/article")
//public class ArticleController {
//    //预想：
//    //1.添加文章
//    //2.删除文章
//    //3.我的文章
//    //4.我赞过的
//    //5.获取最近指定数目文章 --- 分页查询？整一个吧
//    //5.5关键词搜索
//    //6.发表评论
//    //-------归于adminController，划分职责。
//    //7.被举报的dirty文章
//    //8.裁判删除或者归于正常dirty文章
//
//    @Operation(summary = "添加评论")
//    @PostMapping("/addComment")
//    public Result<?> addComment(@RequestBody Comment comment){
//
//        return Result.succeed("true");
//    }
//}

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "创建文章")
    @PostMapping("/create")
    public Result<?> createArticle(
            @Valid @RequestBody ArticleCreateRequest request) {
        Long articleId = articleService.createArticle(request);
        return Result.succeed(articleId);
    }

    /**
     * 更新文章
     */
    @Operation(summary = "更新文章")
    @GetMapping("/upt/{id}")
    public Result<?> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateRequest request) {
        request.setId(id);
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
            @RequestHeader("X-User-Id") Long userId) {
        articleService.deleteArticle(id, userId);
        return Result.succeed();
    }

    /**
     * 获取文章详情
     */
    @Operation(summary = "根据ID获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO article = null;
        try {
            article = articleService.getArticleDetail(id);
        } catch (Exception e) {
            log.info("文章不存在或者被软删除，ID：{},{}",id,e.getMessage());
            return Result.error("文章不存在");
        }
        return Result.succeed(article);
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
    @GetMapping("/user/{userId}")
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
            Integer userId = user.getId();

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
        Integer userId = user.getId();

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
