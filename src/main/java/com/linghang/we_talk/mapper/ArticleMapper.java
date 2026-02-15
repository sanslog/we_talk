package com.linghang.we_talk.mapper;

import com.linghang.we_talk.DTO.BatchIncrementDTO;
import com.linghang.we_talk.entity.Article;
import com.linghang.we_talk.entity.Like;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 文章Mapper接口
 */
@Mapper
public interface ArticleMapper {

    /**
     * 插入文章
     */
    int insert(Article article);

    /**
     * 更新文章
     */
    int update(Article article);

    /**
     * 根据ID查询文章
     */
    Optional<Article> findById(@Param("id") Long id);

    /**
     * 根据用户ID查询文章列表
     */
    List<Article> findByUserId(@Param("userId") Long userId);

    /**
     * 查询文章列表（分页）
     */
    List<Article> findArticles(@Param("status") Integer status,
                               @Param("categoryId") Integer categoryId,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    /**
     * 统计文章数量
     */
    Long countArticles(@Param("status") Integer status,
                       @Param("categoryId") Integer categoryId);

    /**
     * 增加阅读量
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 构建点赞-用户-文章关联表
     * */
    @Insert("insert into `like`(article_id, user_id) VALUE (#{articleId},#{userId})")
    void insertLike(@Param("articleId") Long articleId,@Param("userId")Long userId);

    @Delete("delete from `like` where article_id=#{articleId} and user_id=#{userId}")
    void delLike(@Param("articleId") Long articleId,@Param("userId")Long userId);

    @Select("select * from `like` where user_id=#{userId} and article_id=#{articleId}")
    Like searchLike(@Param("articleId") Long articleId,@Param("userId")Long userId);
    /**
     * 增加评论数
     */
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 软删除文章
     */
    int softDelete(@Param("id") Long id);

    /**
     * 定时任务刷新点赞数
     */
    //TODO:测一下接口
    int refIncrementLikeCount(@Param("list") List<BatchIncrementDTO> list);


    int refViews(@Param("list") List<BatchIncrementDTO> list);

    int hardDelete();
}
