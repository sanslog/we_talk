package com.linghang.we_talk.service.Impl;

import com.linghang.we_talk.DTO.*;
import com.linghang.we_talk.entity.Article;
import com.linghang.we_talk.entity.ArticleImage;
import com.linghang.we_talk.entity.ArticleStatus;
import com.linghang.we_talk.mapper.ArticleMapper;
import com.linghang.we_talk.mapper.ArticleImageMapper;
import com.linghang.we_talk.service.ArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleImageMapper articleImageMapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${redis-template.keys.article-like}")
    private String ARTICLE_LIKE;

    @Value("${redis-template.keys.article-views}")
    private String ARTICLE_VIEW;

    @Autowired
    public ArticleServiceImpl(ArticleMapper articleMapper, ArticleImageMapper articleImageMapper) {
        this.articleMapper = articleMapper;
        this.articleImageMapper = articleImageMapper;
    }

    @Override
    @Transactional
    public Long createArticle(ArticleCreateRequest request) {
        log.info("创建文章, 用户ID: {}, 标题: {}", request.getUserId(), request.getTitle());

        // 创建文章实体
        Article article = new Article();
        article.setUserId(request.getUserId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(ArticleStatus.PUBLISHED.getCode());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCategoryId(request.getCategoryId());

        // 处理标签
        if (!CollectionUtils.isEmpty(request.getTags())) {
            article.setTags(String.join(",", request.getTags()));
        }

        // 保存文章
        int result = articleMapper.insert(article);
        if (result <= 0) {
            throw new RuntimeException("创建文章失败");
        }

        // 保存文章图片
        if (!CollectionUtils.isEmpty(request.getImages())) {
            saveArticleImages(article.getId(), request.getImages());
        }

        log.info("文章创建成功, ID: {}", article.getId());
        return article.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = "article", key = "#request.id")
    public void updateArticle(ArticleUpdateRequest request) {
        log.info("更新文章, ID: {}, 用户ID: {}", request.getId(), request.getUserId());

        // 验证文章存在且属于该用户
        Optional<Article> existingArticle = articleMapper.findById(request.getId());
        if (existingArticle.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }

        Article article = existingArticle.get();
        if (!article.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("无权修改此文章");
        }

        // 更新文章字段
        if (StringUtils.hasText(request.getTitle())) {
            article.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            article.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getCoverImage())) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getCategoryId() != null) {
            article.setCategoryId(request.getCategoryId());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }

        // 处理标签
        if (!CollectionUtils.isEmpty(request.getTags())) {
            article.setTags(String.join(",", request.getTags()));
        }

        // 更新文章
        int result = articleMapper.update(article);
        if (result <= 0) {
            throw new RuntimeException("更新文章失败");
        }

        // 更新图片：先删除旧图片，再添加新图片
        if (request.getImages() != null) {
            articleImageMapper.deleteByArticleId(request.getId());
            if (!CollectionUtils.isEmpty(request.getImages())) {
                saveArticleImages(request.getId(), request.getImages());
            }
        }

        log.info("文章更新成功, ID: {}", request.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "article", key = "#articleId")
    public void deleteArticle(Long articleId, Long userId) {
        log.info("删除文章, ID: {}, 用户ID: {}", articleId, userId);

        int result = articleMapper.softDelete(articleId, userId);
        if (result <= 0) {
            throw new RuntimeException("删除文章失败或文章不存在");
        }

        log.info("文章删除成功, ID: {}", articleId);
    }

    @Override
    @Cacheable(value = "article#10m", key = "#articleId", unless = "#result == null")
    public ArticleVO getArticleDetail(Long articleId) {
        log.debug("获取文章详情, ID: {}", articleId);

        Optional<Article> articleOpt = articleMapper.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }

        Article article = articleOpt.get();

        // 增加阅读量
        articleMapper.incrementViewCount(articleId);

        // 查询关联图片
        List<ArticleImage> images = articleImageMapper.findByArticleId(articleId);

        // 转换为VO对象
        return convertToVO(article, images);
    }

    @Override
    public Page<ArticleVO> getArticleList(Integer status, Integer categoryId, int page, int size) {
        log.debug("查询文章列表, 状态: {}, 分类: {}, 页码: {}, 大小: {}", status, categoryId, page, size);

        int offset = (page - 1) * size;

        // 查询文章列表
        List<Article> articles = articleMapper.findArticles(status, categoryId, offset, size);

        // 查询总数
        Long total = articleMapper.countArticles(status, categoryId);

        // 转换为VO列表
        List<ArticleVO> articleVOs = articles.stream()
                .map(article -> {
                    List<ArticleImage> images = articleImageMapper.findByArticleId(article.getId());
                    return convertToVO(article, images);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(articleVOs, PageRequest.of(page - 1, size), total);
    }

    @Override
    public List<ArticleVO> getUserArticles(Long userId) {
        log.debug("查询用户文章列表, 用户ID: {}", userId);

        List<Article> articles = articleMapper.findByUserId(userId);

        return articles.stream()
                .map(article -> {
                    List<ArticleImage> images = articleImageMapper.findByArticleId(article.getId());
                    return convertToVO(article, images);
                })
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "article", key = "#articleId")
    public void incrementViewCount(Long articleId) {
        articleMapper.incrementViewCount(articleId);
    }

    @Override
    public void likeArticle(Long articleId,Integer userId) {
        //articleMapper.incrementLikeCount(articleId);
        articleMapper.insertLike(articleId,userId);
    }

    @Override
    public boolean checkLiked(Long articleId, Integer userid){
        return articleMapper.searchLike(articleId,userid)!=null;
    }

    @Override
    public void disLikeArticle(Long articleId, Integer userId) {
        articleMapper.delLike(articleId,userId);
    }

    @Override
    public void likeArticleInCache(long articleId) {
        redisTemplate.opsForValue().increment(ARTICLE_LIKE+articleId);
    }

    @Override
    public void readArticle(Long articleId) {
//        redisTemplate.opsForValue().increment(ARTICLE_VIEW+articleId);
        try {
            HashOperations<String, String, Long> hashOps = redisTemplate.opsForHash();
            hashOps.increment(ARTICLE_VIEW, articleId.toString(), 1L);
        } catch (Exception e) {
            log.error("增加文章浏览量出错{}", e.getMessage());
            //这里可以降级走数据库
        }
    }

    @Override
    public void likesToDB(List<BatchIncrementDTO> list){
        articleMapper.refIncrementLikeCount(list);
    }

    @Override
    public void viewsToDB(List<BatchIncrementDTO> resultList) {
        articleMapper.refViews(resultList);
    }

    /**
     * 保存文章图片
     */
    private void saveArticleImages(Long articleId, List<String> imageUrls) {
        List<ArticleImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ArticleImage image = new ArticleImage();
            image.setArticleId(articleId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i);
            images.add(image);
        }
        articleImageMapper.batchInsert(images);
    }

    /**
     * 转换为VO对象
     */
    private ArticleVO convertToVO(Article article, List<ArticleImage> images) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setUserId(article.getUserId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setCoverImage(article.getCoverImage());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setCategoryId(article.getCategoryId());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());

        // 处理标签
        if (StringUtils.hasText(article.getTags())) {
            vo.setTags(List.of(article.getTags().split(",")));
        } else {
            vo.setTags(new ArrayList<>());
        }

        // 处理图片
        if (!CollectionUtils.isEmpty(images)) {
            List<ArticleImageVO> imageVOs = images.stream()
                    .map(image -> {
                        ArticleImageVO imageVO = new ArticleImageVO();
                        imageVO.setId(image.getId());
                        imageVO.setImageUrl(image.getImageUrl());
                        imageVO.setSortOrder(image.getSortOrder());
                        return imageVO;
                    })
                    .collect(Collectors.toList());
            vo.setImages(imageVOs);
        }

        return vo;
    }
}