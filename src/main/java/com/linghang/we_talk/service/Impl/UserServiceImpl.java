package com.linghang.we_talk.service.Impl;

import com.linghang.we_talk.entity.User;
import com.linghang.we_talk.mapper.UserMapper;
import com.linghang.we_talk.service.UserService;
import com.linghang.we_talk.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final PasswordUtil passwordUtil;

    /**
     * @apiNote 返回数据库查询是否有该用户的结果
     * @return boolean
     * */
    @Override
    public Long canLogin(User user) {
        String username = user.getUsername();
        String rawPassword = user.getPassword();

        User userByName = userMapper.getUserByName(username);
        if(!passwordUtil.bcryptMatches(rawPassword, userByName.getPassword())) return -1L;
        return userByName.getId();
    }

    @Override
    public void register(User user) {
        String username = user.getUsername();
        String rawPassword = user.getPassword();

        userMapper.save(username,passwordUtil.bcryptEncode(rawPassword));
    }

    /**
     * @apiNote 检查该用户名是否已经被注册
     * @return boolean
     * */
    @Override
    public boolean matchUserByName(String username) {
        return userMapper.getUserByName(username) != null;
    }

    @Override
    public User getUserByName(String username) {
        return userMapper.getUserByName(username);
    }

    @Override
    public User getUserById(String id) {
        return userMapper.getUserById(id);
    }
}
