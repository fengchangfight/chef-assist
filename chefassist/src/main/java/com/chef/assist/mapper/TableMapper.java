package com.chef.assist.mapper;

import com.chef.assist.model.Table;
import com.chef.assist.model.dto.TableBoardItems;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "tableMapper")
public interface TableMapper {
//    @Select("SELECT * FROM users")
//    @Results({
//            @Result(property = "userSex",  column = "user_sex", javaType = UserSexEnum.class),
//            @Result(property = "nickName", column = "nick_name")
//    })
//    List<UserEntity> getAll();
//
    @Select("SELECT * FROM ca_table WHERE id = #{id}")
    @Results({
            @Result(property = "tableNumber",  column = "table_no")
    })
    Table findById(Long id);

    @Insert("INSERT INTO ca_table(table_no,description) VALUES(#{tableNumber}, #{description})")
    void insert(Table table);

    @Select("select * from ca_table")
    @Results({
            @Result(property = "tableNumber",  column = "table_no")
    })
    List<Table> findAll();

    @Select("select count(*) from ca_table")
    Integer count();

    @Select("<script>" +
            "select oi.id item_id, oi.table_id, d.name dish_name, oi.dish_count, oi.description, oi.status  from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_dish d on d.id=oi.dish_id WHERE o.order_status='active' and oi.table_id in \n" +
            "    <foreach item='item' collection='tableIds' open='(' separator=',' close=')'>\n" +
            "    #{item}" +
            "    </foreach>" +
            " order by oi.last_update_time asc"+
            "</script>")
    List<TableBoardItems> findTableItemsInBoards(@Param("tableIds")List<Long> tableIds);

    @Update("UPDATE ca_table SET table_no=#{tableNumber},description=#{description} WHERE id =#{id}")
    void update(Table table);

    @Delete("DELETE FROM ca_table WHERE id =#{id}")
    void delete(Long id);

}
