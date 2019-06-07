package com.chef.assist.mapper;

import com.chef.assist.model.Role;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "roleMapper")
public interface RoleMapper {
    @Select("select * from ca_role where role_name = #{roleName}")
    Role findByRoleName(String roleName);

    @Select("select * from ca_role where id = #{id}")
    Role findByRoleId(Long id);

    @Select("select * from ca_role")
    List<Role> findAllRoles();

    @Insert("INSERT INTO ca_role(role_name) VALUES(#{roleName})")
    @Options(useGeneratedKeys=true, keyProperty="id")
    Long insert(Role role);
}
