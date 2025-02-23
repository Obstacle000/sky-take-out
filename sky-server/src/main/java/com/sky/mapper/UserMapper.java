package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User getByOpenId(String openid);
    @Insert("INSERT INTO user (openid, name, phone, sex, id_number, avatar, create_time) VALUES (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    void save(User user);
}