package com.linghang.we_talk.schedule;

import com.linghang.we_talk.DTO.BatchIncrementDTO;
import com.linghang.we_talk.service.ArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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
    @Scheduled(cron = "0 */10 8-23 * * ?") // 对应第一个Cron表达式
    public void task10MinInterval() {
        syncLikesToDB();
    }

    /**
     * 凌晨0点-早7点59分：每20分钟执行一次
     */
    @Scheduled(cron = "0 */20 0-7 * * ?") // 对应第二个Cron表达式
    public void task30MinInterval() {
        syncLikesToDB();
    }


    @Async
    protected void syncLikesToDB(){
        //主动过期文章
        log.info("操作redis持久化阅读量数据到MySQL");
        List<BatchIncrementDTO> resultList = new ArrayList<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(ARTICLE_VIEW+"*")
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String value = Objects.requireNonNull(redisTemplate.opsForValue().getAndDelete(key)).toString();

                if (value != null) {
                    String idStr = key.substring((ARTICLE_VIEW+"*").length());
                    Long articleId = Long.parseLong(idStr);
                    Integer likeCount = Integer.parseInt(value);

                    resultList.add(new BatchIncrementDTO(articleId,likeCount));
                }
            }
        }catch (Exception e){
            log.error("从redis中获取阅读量缓存异常:{}",e.getMessage());
        }

        if (!resultList.isEmpty()) {
            log.info("数据持久化开始");
            try {
                articleService.viewsToDB(resultList);
            } catch (Exception e) {
                log.error("数据持久化失败{}",e.getMessage());
                return;
            }
            log.info("数据持久化完成，共计：{} 篇文章阅读量数据更新",resultList.size());
        }
    }
}
