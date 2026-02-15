package com.linghang.we_talk.service;


import com.linghang.we_talk.DTO.ArticleCreateRequest;
import com.linghang.we_talk.DTO.ArticleUpdateRequest;
import com.linghang.we_talk.DTO.ArticleVO;
import com.linghang.we_talk.DTO.BatchIncrementDTO;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 创建文章
     */
    Long createArticle(ArticleCreateRequest request);

    /**
     * 更新文章
     */
    void updateArticle(ArticleUpdateRequest request);

    /**
     * 删除文章（软删除）
     */
    void deleteArticle(Long articleId, Long userId);

    /**
     * 获取文章详情
     */
    ArticleVO getArticleDetail(Long articleId);



    /**
     * 分页查询文章列表
     */
    Page<ArticleVO> getArticleList(Integer status, Integer categoryId, int page, int size);

    /**
     * 获取用户文章列表
     */
    List<ArticleVO> getUserArticles(Long userId);

    /**
     * 增加文章阅读量
     */
    void incrementViewCount(Long articleId);

    /**
     * 点赞文章
     */
    void likeArticle(Long articleId,Long userId);

    boolean checkLiked(Long articleId, Long userid);

    void disLikeArticle(Long articleId, Long userId);

    /**
     * 点赞并缓存点赞，通过定时刷新实现更新
     */
    void likeArticleInCache(long articleId);

    void readArticle(Long articleId);

    /**
     * 点赞持久化
     */
    void likesToDB(List<BatchIncrementDTO> list);

    void viewsToDB(List<BatchIncrementDTO> resultList);

    int clearSoftDel();
}