package com.linghang.we_talk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;//登录时是邮箱，这里是学号，唯一标识
    private String nickname;
    private String password;
}

