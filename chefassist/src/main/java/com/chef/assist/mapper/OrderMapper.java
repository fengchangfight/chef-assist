package com.chef.assist.mapper;

import com.chef.assist.model.Order;
import com.chef.assist.model.dto.OrderDTO;
import com.chef.assist.model.dto.OrderDTO2;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "orderMapper")
public interface OrderMapper {
//    @Select("SELECT * FROM users")
//    @Results({
//            @Result(property = "userSex",  column = "user_sex", javaType = UserSexEnum.class),
//            @Result(property = "nickName", column = "nick_name")
//    })
//    List<UserEntity> getAll();
//
    @Select("SELECT * FROM ca_order WHERE id = #{id}")
    @Results({
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "orderStatus", column = "order_status"),
            @Result(property = "createdBy", column = "created_by"),
            @Result(property = "startTime", column = "start_time", javaType=java.util.Date.class)
    })
    Order findById(Long id);

    @Select("select status from ca_order_item where order_id=#{orderId}")
    List<String> findAllItemsStatus(long orderId);


    @Select("SELECT o.order_no, u.username, o.order_status FROM ca_order o inner join ca_user u on u.id=o.created_by  WHERE o.id = #{id}")
    @Results({
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "createdBy", column = "username"),
            @Result(property = "orderStatus", column = "order_status"),
    })
    OrderDTO findBasicById(Long id);

    @Select("select o.id, o.order_no, o.order_status, u.username created_by, o.start_time from ca_order o inner join ca_user u on u.id=o.created_by where o.order_status='active' order by o.start_time desc")
    @Results({
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "orderStatus",  column = "order_status"),
            @Result(property = "createdBy",  column = "created_by"),
            @Result(property = "startTime",  column = "start_time")
    })
    List<OrderDTO2> findActiveOrders();

    @Select("select o.id, o.order_no, o.order_status, u.username created_by, o.start_time, o.end_time from ca_order o inner join ca_user u on u.id=o.created_by where o.order_status='finished' order by o.end_time desc")
    @Results({
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "orderStatus",  column = "order_status"),
            @Result(property = "createdBy",  column = "created_by"),
            @Result(property = "startTime",  column = "start_time"),
            @Result(property = "endTime",  column = "end_time")
    })
    List<OrderDTO2> findFinishedOrders();

    @Select("select distinct o.id, o.order_no, o.order_status, u.username created_by, o.start_time, o.end_time from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_user u on u.id=o.created_by where o.order_status='finished' and oi.table_id=#{tableId} order by o.end_time desc")
    @Results({
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "orderStatus",  column = "order_status"),
            @Result(property = "createdBy",  column = "created_by"),
            @Result(property = "startTime",  column = "start_time"),
            @Result(property = "endTime",  column = "end_time")
    })
    List<OrderDTO2> findFinishedOrdersByTable(Long tableId);

    @Select("select count(*) from (select distinct o.id, o.order_no, o.order_status, u.username created_by, o.start_time, o.end_time from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_user u on u.id=o.created_by where o.order_status='finished' and oi.table_id=#{tableId})tbl")
    Integer countFinishedOrdersByTable(Long tableId);

    @Insert("INSERT INTO ca_order(order_no,order_status,start_time, created_by) VALUES(#{orderNumber}, #{orderStatus}, #{startTime}, #{createdBy})")
    @Options(useGeneratedKeys=true, keyProperty="id")
    Long insert(Order order);


    @Select("select count(*) from ca_order where order_status='active'")
    Integer countActiveOrders();

    @Select("select count(*) from ca_order where order_status='finished'")
    Integer countFinishedOrders();

    @Update("UPDATE ca_order SET order_status=#{status} WHERE id =#{orderId}")
    void updateStatus(Long orderId, String status);

    @Update("UPDATE ca_order SET order_no=#{orderNumber}, order_status=#{orderStatus}, start_time=#{startTime}, created_by=#{createdBy}, end_time=#{endTime} WHERE id =#{id}")
    void update(Order order);

    @Delete("DELETE FROM ca_order WHERE id =#{id}")
    void delete(Long id);

}
