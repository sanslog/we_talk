package com.linghang.we_talk.mapper;

import com.linghang.we_talk.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

//能够替代xml配置文件的@Mapper注解。
@Mapper
public interface UserMapper {
    @Insert("INSERT INTO user(username,nickname,password) VALUE (#{username},#{username},#{password})")
    void save(@Param("username") String username, @Param("password") String password);

    @Select("SELECT * FROM user WHERE username=#{username} and password=#{password}")
    User getUser(User user);

    @Select("SELECT id,username,nickname,password FROM user WHERE username=#{username}")
    User getUserByName(@Param("username") String username);
}
