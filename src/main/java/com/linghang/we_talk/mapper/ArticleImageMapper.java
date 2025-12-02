package com.linghang.we_talk.mapper;


import com.linghang.we_talk.entity.ArticleImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 文章图片Mapper接口
 */
@Mapper
public interface ArticleImageMapper {

    /**
     * 批量插入图片
     */
    int batchInsert(@Param("images") List<ArticleImage> images);

    /**
     * 根据文章ID查询图片列表
     */
    List<ArticleImage> findByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据文章ID删除图片
     */
    int deleteByArticleId(@Param("articleId") Long articleId);
}
