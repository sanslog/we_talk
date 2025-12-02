package com.linghang.we_talk.service;

import com.linghang.we_talk.entity.User;

public interface UserService {

    boolean canLogin(User user);

    void register(User user);

    /**
     * @apiNote 检查该用户名是否已经被注册
     * @return boolean
     * */
    boolean matchUserByName(String username);

    User getUserByName(String username);
}
