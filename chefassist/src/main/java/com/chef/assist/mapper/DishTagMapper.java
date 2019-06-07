package com.chef.assist.mapper;

import com.chef.assist.model.DishTag;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "dishTagMapper")
public interface DishTagMapper {
    @Insert("INSERT INTO ca_dish_tag(name) VALUES(#{name}) ")
    @Options(useGeneratedKeys=true, keyProperty="id")
    Long insert(DishTag dishTag);

    @Insert({
            "<script>",
            "insert into ca_dish_tag (name)",
            "values ",
            "<foreach  collection='tagList' item='tg' separator=','>",
            "( #{tg,jdbcType=VARCHAR})",
            "</foreach>",
            "</script>"
    })
    Long insertMany(@Param("tagList") List<String> tagList);


    @Select("select * from ca_dish_tag where name like concat('%',#{likeName},'%')")
    List<DishTag> findAllDishTagsByLikeName(String likeName);

    @Select("select * from ca_dish_tag")
    List<DishTag> findAll();

    @Select("<script>" +
            "SELECT * FROM ca_dish_tag WHERE name in \n" +
            "    <foreach item='item' collection='tags' open='(' separator=',' close=')'>\n" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    List<DishTag> findExistTags(@Param("tags") List<String> tags);
}
