package com.chef.assist.mapper;

import com.chef.assist.model.User;
import com.chef.assist.model.dto.UserVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "userMapper")
public interface UserMapper {
    @Select("select * from ca_user where username = #{username}")
    User findByUserName(String username);

    @Select("select * from ca_user where id = #{id}")
    User findByUserId(Long id);

    @Select("select u.id, u.username, r.role_name from ca_user u inner join ca_role r on r.id=u.role_id order by u.reg_time desc")
    @Results({
            @Result(property = "userId",  column = "id")
    })
    List<UserVO> findAllUsers();

    @Select("select u.id, u.username, r.role_name from ca_user u inner join ca_role r on r.id=u.role_id where u.username like concat('%',#{searchName},'%') order by u.reg_time desc")
    @Results({
            @Result(property = "userId",  column = "id")
    })
    List<UserVO> findAllUsersByLikeName(String searchName);

    @Select("select u.id, u.username, r.role_name from ca_user u inner join ca_role r on r.id=u.role_id where r.role_name=#{roleName} order by u.reg_time desc")
    @Results({
            @Result(property = "userId",  column = "id")
    })
    List<UserVO> findAllUsersByRoleName(String roleName);

    @Select("select u.id, u.username, r.role_name from ca_user u inner join ca_role r on r.id=u.role_id where r.role_name=#{roleName} and u.username like concat('%',#{searchName},'%') order by u.reg_time desc")
    @Results({
            @Result(property = "userId",  column = "id")
    })
    List<UserVO> findAllUsersByRoleNameAndLikeName(String roleName, String searchName);

    @Select("select count(*) from ca_user")
    Integer countUsers();

    @Select("select count(*) from ca_user where username like concat('%',#{likeName},'%')")
    Integer countByLikeName(String likeName);

    @Select("select count(*) from ca_user u inner join ca_role r on r.id=u.role_id where r.role_name=#{roleName}")
    Integer countByRoleName(String roleName);

    @Select("select count(*) from ca_user u inner join ca_role r on r.id=u.role_id where r.role_name=#{roleName} and u.username like concat('%',#{likeName},'%')")
    Integer countByRoleNameAndLikeName(String roleName, String likeName);

    @Select("select u.username, r.role_name from ca_user u inner join ca_role r on r.id=u.role_id where username = #{username}")
    @Results({
            @Result(property = "username",  column = "username"),
            @Result(property = "roleName", column = "role_name")
    })
    UserVO getUserInfo(String username);

    @Insert("INSERT INTO ca_user(username, salt, password, reg_time, role_id) VALUES(#{username}, #{salt}, #{password}, CURRENT_TIMESTAMP, #{roleId})")
    @Options(useGeneratedKeys=true, keyProperty="id")
    Long insert(User user);

    @Update("UPDATE ca_user SET userName=#{username},salt=#{salt},password=#{password},role_id=#{roleId} WHERE id =#{id}")
    void update(User user);

    @Delete("delete from ca_user where id=#{id}")
    void deleteById(Long id);
}
