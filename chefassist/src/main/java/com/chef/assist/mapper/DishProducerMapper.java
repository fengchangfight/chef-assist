package com.chef.assist.mapper;

import com.chef.assist.model.DishProducer;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "dishProducerMapper")
public interface DishProducerMapper {
//    @Select("SELECT * FROM users")
//    @Results({
//            @Result(property = "userSex",  column = "user_sex", javaType = UserSexEnum.class),
//            @Result(property = "nickName", column = "nick_name")
//    })
//    List<UserEntity> getAll();
//
    @Select("SELECT * FROM ca_dish_producer WHERE id = #{id}")
    @Results({
            @Result(property = "producerNumber",  column = "producer_no")
    })
    DishProducer findById(Long id);

    @Insert("INSERT INTO ca_dish_producer(producer_no,description, status) VALUES(#{producerNumber}, #{description}, #{status})")
    void insert(DishProducer dishProducer);

    @Select("select id from ca_dish_producer where status='online'")
    List<Long> findAllOnlineProducers();


    @Select("select * from ca_dish_producer")
    @Results({
            @Result(property = "producerNumber",  column = "producer_no")
    })
    List<DishProducer> findAll();

    @Select("select * from ca_dish_producer where status!='offline'")
    @Results({
            @Result(property = "producerNumber",  column = "producer_no")
    })
    List<DishProducer> findAllNotOffline();

    @Select("select count(*) from ca_dish_producer")
    Integer count();

    @Select("select count(*) from ca_dish_producer where status!='offline'")
    Integer countNotOffline();

    @Update("UPDATE ca_dish_producer SET status=#{status} WHERE id =#{id}")
    void updateStatus(Long id, String status);

    @Update("update ca_dish_producer SET producer_no=#{producerNumber}, description=#{description}, status=#{status} where id=#{id}")
    void update(DishProducer dishProducer);

    @Delete("DELETE FROM ca_dish_producer WHERE id =#{id}")
    void deleteById(Long id);

}
