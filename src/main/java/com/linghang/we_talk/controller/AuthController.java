package com.linghang.we_talk.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.linghang.we_talk.DTO.LoginRequest;
import com.linghang.we_talk.DTO.RegisterRequest;
import com.linghang.we_talk.entity.User;
import com.linghang.we_talk.service.UserService;
import com.linghang.we_talk.utils.JwtUtil;
import com.linghang.we_talk.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "authController",description = "用户验证登录注册相关")
public class AuthController {
    @Resource
    private RedisTemplate<String, String> redisTemplate;


    private final JwtUtil jwtUtil;


    private final UserService userService;

    @Operation(
            summary = "用户登录",
            description = "用户登录接口，成功返回accessToken和refreshToken"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        Long userid = userService.canLogin(new User(null, username, null, password));
        //密码校验
        if(userid==-1) return ResponseEntity.badRequest().body(Map.of(
                    "error","账户或者密码错误"));

        //生成token
        String accessToken = jwtUtil.generateAccessToken(userid.toString());
        String refreshToken = jwtUtil.generateRefreshToken(userid.toString());

        // 存储RefreshToken到Redis
        redisTemplate.opsForValue().set(
                jwtUtil.JWT_REFRESH + userid,
                refreshToken,
                jwtUtil.REFRESH_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        //返回token
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "timestamp",String.valueOf(System.currentTimeMillis())
        ));
    }


    //TODO:后期优化思路，使用JavaMail实现通过邮件注册，最后形成独立项目
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@RequestBody RegisterRequest registerRequest){
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();

        try {
            userService.register(new User(null,username,username,password));
        } catch (Exception e) {
            ResponseEntity.badRequest().body(Map.of("error","注册失败"));
        }
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "用户刷新accessToken")
    @GetMapping("/refresh")
    public ResponseEntity<Map<String,String>> refreshToken(@RequestHeader("refresh-Token") String refreshToken,
                                               @RequestHeader(value = "Authorization",required = false) String oldAccessToken) {
        try {
            String userid = jwtUtil.validateToken(refreshToken);

            // 验证Redis中的RefreshToken
            String storedToken = redisTemplate.opsForValue().get(jwtUtil.JWT_REFRESH + userid);
            if(!refreshToken.equals(storedToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            // 2. 将旧access_token加入黑名单（剩余TTL=原过期时间）
            if (oldAccessToken!=null) {
                long remainingTTL = jwtUtil.getRemainingTime(oldAccessToken); // 获取剩余有效期
                redisTemplate.opsForValue().set(
                        jwtUtil.JWT_BLACKLIST + oldAccessToken,
                        "revoked",
                        remainingTTL,
                        TimeUnit.SECONDS
                );
            }

            // 生成新Token
            String newAccessToken = jwtUtil.generateAccessToken(userid);

            return ResponseEntity.ok().body(Map.of(
                    "AccessToken",newAccessToken,
                    "timestamp",String.valueOf(System.currentTimeMillis())));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error","RefreshToken非法"));
        }
    }

    @Operation(summary = "该接口用于获取用户所有信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String accessToken){
        try {
            String userid = jwtUtil.validateToken(accessToken);
            User userById = userService.getUserById(userid);
            userById.setPassword("refused to view");
            return Result.succeed(userById);
        } catch (Exception e) {
            log.error("获取用户信息失败{}",e.getMessage());
            return Result.error();
        }
    }


    @Operation(summary = "用户登出，需要传入accessToken")
    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout(@RequestHeader("Authorization") String accessToken) {
        try {
            String userid = jwtUtil.validateToken(accessToken);
            redisTemplate.delete(jwtUtil.JWT_REFRESH + userid);
            redisTemplate.opsForValue().set(
                    jwtUtil.JWT_BLACKLIST+accessToken,
                    "revoked",
                    jwtUtil.getRemainingTime(accessToken),
                    TimeUnit.SECONDS
            );
            return ResponseEntity.ok().build();
        } catch (JWTVerificationException e) {
            return ResponseEntity.badRequest().body(Map.of("error","Invalid token"));
        }
    }
}


