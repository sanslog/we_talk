package com.linghang.we_talk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
@RequiredArgsConstructor
@RequestMapping("/public")
@Tag(name = "publicActionController", description = "提供公共行为的接口的控制器")
public class PublicActionController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Value("${redis-template.keys.reported-key}")
    private String REPORTED_KEY;

    @Operation(summary = "举报文章/评论，给出ID和类型,1为文章,2为评论")
    @GetMapping("/report")
    public void report(@PathParam("type") Integer type,@PathParam("id") Long id){
        redisTemplate.opsForHash().increment(REPORTED_KEY,type+":"+id,1);
    }
}
