package com.chef.assist.mapper;

import com.chef.assist.model.Dish;
import com.chef.assist.model.dto.DishDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "dishMapper")
public interface DishMapper {
    @Select("SELECT * FROM ca_dish")
    @Results({
            @Result(property = "expectedCookingTime",  column = "expected_cooking_time")
    })
    List<Dish> getAllDish();

    @Select("SELECT id, name FROM ca_dish")
    List<DishDTO> getDishList();

    @Select("<script>select * from ca_dish where " +
            "    <foreach item='item' collection='tags'  separator=' or ' >" +
            "   tags like concat('%',#{item},'%')" +
            "    </foreach>" +
            "</script>")
    List<Dish> getDishByTags(@Param("tags") List<String> tags);

    @Select("select * from ca_dish where id=#{id}")
    Dish findById(Long id);

    @Select("<script>select count(*) from ca_dish where " +
            "    <foreach item='item' collection='tags'  separator=' or ' >" +
            "   tags like concat('%',#{item},'%')" +
            "    </foreach>" +
            "</script>")
    Integer countByTags(@Param("tags") List<String> tags);

    @Select("select count(*) from ca_dish")
    Integer count();
//
//    @Select("SELECT * FROM users WHERE id = #{id}")
//    @Results({
//            @Result(property = "userSex",  column = "user_sex", javaType = UserSexEnum.class),
//            @Result(property = "nickName", column = "nick_name")
//    })
//    UserEntity getOne(Long id);

    @Insert("<script>INSERT INTO ca_dish(name,description,tags, price, expected_cooking_time) VALUES(#{name}, #{description}, #{tags}, #{price},  " +
            "<if test='expectedCookingTime != null'>#{expectedCookingTime})</if>" +
            "<if test='expectedCookingTime == null'>default)</if></script>")
    @Options(useGeneratedKeys=true, keyProperty="id")
    Long insert(Dish dish);

    @Update("UPDATE ca_dish SET name=#{name},description=#{description},tags=#{tags},expected_cooking_time=#{expectedCookingTime},thumbnail=#{thumbnail},price=#{price} WHERE id =#{id}")
    void update(Dish dish);

    @Delete("DELETE FROM ca_dish WHERE id =#{id}")
    void delete(Long id);

}
