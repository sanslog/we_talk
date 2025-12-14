package com.linghang.we_talk.schedule;

import com.linghang.we_talk.DTO.BatchIncrementDTO;
import com.linghang.we_talk.service.ArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
public class ScheduleService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ArticleService articleService;

    @Value("${redis-template.keys.article-views}")
    private String ARTICLE_VIEW;


    /**
     * 早8点-晚23点59分：每10分钟执行一次
     */
    @Scheduled(cron = "0 */10 8-23 * * ?")
    public void task10MinInterval() {
        syncViewsToDB();
    }

    /**
     * 凌晨0点-早7点59分：每20分钟执行一次
     */
    @Scheduled(cron = "0 */20 0-7 * * ?")
    public void task30MinInterval() {
        syncViewsToDB();
    }


    @Async
    protected void syncViewsToDB() {
        log.info("开始同步文章浏览量增量到数据库");

        // 1. 一次性获取Hash中所有数据
        Map<Object, Object> allData = redisTemplate.opsForHash()
                .entries(ARTICLE_VIEW);

        if (allData.isEmpty()) {
            log.info("没有需要同步的浏览量增量数据");
            return;
        }

        List<BatchIncrementDTO> resultList = new ArrayList<>();

        // 2. 遍历处理每条数据
        for (Map.Entry<Object, Object> entry : allData.entrySet()) {
            try {
                Long articleId = Long.parseLong(entry.getKey().toString());
                long increment = Long.parseLong(entry.getValue().toString());

                if (increment > 0) {
                    resultList.add(new BatchIncrementDTO(articleId, increment));
                }
            } catch (NumberFormatException e) {
                log.warn("数据格式异常，跳过: key={}, value={}",
                        entry.getKey(), entry.getValue());
            }
        }

        // 3. 如果有有效数据则更新数据库
        if (!resultList.isEmpty()) {
            try {
                log.info("开始持久化 {} 篇文章的浏览量增量", resultList.size());
                articleService.viewsToDB(resultList);

                // 4. 数据库更新成功后删除Redis中的数据
                String[] processedKeys = resultList.stream()
                        .map(dto -> dto.getArticleId().toString())
                        .toArray(String[]::new);

                redisTemplate.opsForHash().delete(ARTICLE_VIEW, (Object) processedKeys);
                log.info("浏览量增量持久化完成，已清理 {} 个Redis键", processedKeys.length);

            } catch (Exception e) {
                log.error("数据库更新失败，数据保留在Redis中: {}", e.getMessage(), e);
                // 数据保留在Redis中，下次定时任务会重试
            }
        } else {
            // 如果没有有效数据，清空Redis中的脏数据
            redisTemplate.delete(ARTICLE_VIEW);
            log.info("清理了Redis中的无效浏览量数据");
        }
    }

    @Async
    @Scheduled(cron = "0 0 4 * * 1")
    public void clearSoftDeleteArticles(){
        try{
            log.info("定时清理被软删除的文章");
            int num = articleService.clearSoftDel();
            log.info("一共有{}篇文章被删除",num);
        }catch(Exception e){
            log.error("清理时出现异常：{}",e.getMessage());
        }
    }

//    @Async
//    protected void syncLikesToDB(){
//        //主动过期文章
//        log.info("操作redis持久化阅读量数据到MySQL");
//        List<BatchIncrementDTO> resultList = new ArrayList<>();
//
//        ScanOptions options = ScanOptions.scanOptions()
//                .match(ARTICLE_VIEW+"*")
//                .build();
//
//        try (Cursor<String> cursor = redisTemplate.scan(options)) {
//            while (cursor.hasNext()) {
//                String key = cursor.next();
//                String value = Objects.requireNonNull(redisTemplate.opsForValue().getAndDelete(key)).toString();
//
//                if (value != null) {
//                    String idStr = key.substring((ARTICLE_VIEW+"*").length());
//                    Long articleId = Long.parseLong(idStr);
//                    Integer likeCount = Integer.parseInt(value);
//
//                    resultList.add(new BatchIncrementDTO(articleId,likeCount));
//                }
//            }
//        }catch (Exception e){
//            log.error("从redis中获取阅读量缓存异常:{}",e.getMessage());
//        }
//
//        if (!resultList.isEmpty()) {
//            log.info("数据持久化开始");
//            try {
//                articleService.viewsToDB(resultList);
//            } catch (Exception e) {
//                log.error("数据持久化失败{}",e.getMessage());
//                return;
//            }
//            log.info("数据持久化完成，共计：{} 篇文章阅读量数据更新",resultList.size());
//        }
//    }
}
