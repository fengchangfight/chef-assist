package com.chef.assist.mapper;

import com.chef.assist.model.OrderItem;
import com.chef.assist.model.dto.KitBoardItems;
import com.chef.assist.model.dto.OrderItemDTO;
import com.chef.assist.model.dto.OrderItemDTO2;
import com.chef.assist.model.dto.ProducerViewItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
@Component(value = "orderItemMapper")
public interface OrderItemMapper {

    @Select("SELECT * FROM ca_order_item WHERE id = #{id}")
    @Results({
            @Result(property = "orderId",  column = "order_id"),
            @Result(property = "tableId", column = "table_id"),
            @Result(property = "dishId", column = "dish_id"),
            @Result(property = "dishCount", column = "dish_count"),
            @Result(property = "assignTo", column = "assign_to"),
            @Result(property = "lastUpdateTime", column = "last_update_time"),
    })
    OrderItem findById(Long id);

    @Select("select assign_to, sum(expected_cooking_time) worktodo from (select oi.assign_to, d.expected_cooking_time from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_dish d on d.id=oi.dish_id inner join ca_dish_producer cdp on cdp.id=oi.assign_to  where oi.status='assigned' and o.order_status='active' and cdp.status='online') tmp group by assign_to;")
    @ResultType(HashMap.class)
    List<Map<String, Object>> countProducerCurrentWork();

    @Select("select oi.id,d.expected_cooking_time from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_dish d on d.id=oi.dish_id where o.id=#{orderId} and oi.status in ('waiting_assign','failed_assign');\n")
    @ResultType(HashMap.class)
    List<Map<String,Object>> getWaitingAssignOrFailedAssignWorkInOrder(Long orderId);

    @Insert("INSERT INTO ca_order_item(order_id,table_id,dish_id,dish_count,last_update_time,status, assign_to, description) VALUES(#{orderId}, #{tableId}, #{dishId}, #{dishCount}, #{lastUpdateTime}, #{status}, #{assignTo}, #{description})")
    void insert(OrderItem orderItem);

    @Update({"<script>",
            "<foreach  item='value' index='key' collection='assignMap' separator=';' >update ca_order_item set assign_to=#{value}, status='assigned', last_update_time=CURRENT_TIMESTAMP where id=#{key}</foreach>",
            "</script>"})
    void batchAssign(@Param("assignMap") Map<Long,Long> assignMap);

    @Update("update ca_order_item set dish_count=#{count} where id=#{itemId}")
    void updateItemCount(Long itemId, Integer count);

    @Update("update ca_order_item set order_id=#{orderId}, table_id=#{tableId}, dish_id=#{dishId}, dish_count=#{dishCount}, assign_to=#{assignTo}, status=#{status}, last_update_time=#{lastUpdateTime}, description=#{description} where id=#{id}")
    void update(OrderItem orderItem);

    @Update("update ca_order_item set description=#{description} where id=#{itemId}")
    void updateItemDescription(Long itemId, String description);

    @Update("update ca_order_item set status='waiting_assign' where status='unassigned' and order_id=#{orderId}")
    void makeUnassignedWaiting(Long orderId);

    @Update("update ca_order_item set status='failed_assign' where status='waiting_assign' and  order_id=#{orderId}")
    void makeWaiting2failedAssign(Long orderId);

    @Update("update ca_order_item set status=#{status}, last_update_time=CURRENT_TIMESTAMP where id=#{itemId}")
    void updateItemStatusWithTimestamp(Long itemId, String status);

    @Select("select oi.id, o.order_no, t.table_no, d.name  from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_dish d on d.id=oi.dish_id inner join ca_table t on t.id=oi.table_id where assign_to=#{producerId} and o.order_status='active' and oi.status='assigned' order by oi.last_update_time, d.expected_cooking_time")
    @Results({
            @Result(property = "id",  column = "id"),
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "tableNumber", column = "table_no"),
            @Result(property = "dishName", column = "name")
    })
    List<ProducerViewItem> getMyAssignedItems(Long producerId);
//
    @Delete("DELETE FROM ca_order_item WHERE id =#{id}")
    void deleteById(Long id);

    @Select(" select oi.id, t.table_no, t.id table_id, d.name, oi.status, oi.dish_count, oi.description, dp.producer_no, oi.last_update_time, d.price " +
            "from ca_order_item oi inner join ca_table t on t.id=oi.table_id inner join ca_dish d on d.id=oi.dish_id left join ca_dish_producer dp on dp.id=oi.assign_to where oi.order_id=#{orderId} order by oi.last_update_time desc")
    @Results({
            @Result(property = "tableNumber",  column = "table_no"),
            @Result(property = "tableId",  column = "table_id"),
            @Result(property = "dishName",  column = "name"),
            @Result(property = "dishCount",  column = "dish_count"),
            @Result(property = "producerNumber", column = "producer_no"),
            @Result(property = "lastUpdateTime", column = "last_update_time")
    })
    List<OrderItemDTO> getOrderItemsByOrderId(Long orderId);

    @Select("select count(*) from ca_order_item where order_id=#{orderId}")
    Integer countByOrderId(Long orderId);

    @Select("select count(*) from ca_order_item oi inner join ca_order o on o.id=oi.order_id where o.order_status='active' and oi.status='assigned' and assign_to=#{kitId}")
    Integer countActiveUnservedByKitId(Long kitId);

    @Select("select count(*) from ca_order_item where order_id=#{orderId} and status in ('unassigned','failed_assign')")
    Integer countUnassignedAndFailedAssigned(Long orderId);

    @Select("select count(*) from ca_order_item oi inner join ca_order o on o.id=oi.order_id where table_id=#{tableId} and order_id!=#{orderId} and o.order_status='active'")
    Integer countActiveOrderItemByTableIdAndNotOrderId(Long tableId, Long orderId);

    /**
     * 获取某桌上当前active订单的全部orderItems
     * @param tableId
     * @return
     */
    @Select("select oi.id, t.table_no, o.order_no, o.id order_id, d.name dish_name, oi.status, oi.description, oi.dish_count, dp.producer_no, oi.last_update_time\n" +
            "from ca_order_item oi inner join ca_table t on t.id=oi.table_id inner join ca_order o on o.id=oi.order_id inner join ca_dish d on d.id=oi.dish_id left join ca_dish_producer dp on\n" +
            "dp.id=oi.assign_to where t.id=#{tableId} and o.order_status='active' order by oi.last_update_time desc")
    @Results({
            @Result(property = "tableNumber",  column = "table_no"),
            @Result(property = "orderNumber",  column = "order_no"),
            @Result(property = "orderId",  column = "order_id"),
            @Result(property = "dishName", column = "dish_name"),
            @Result(property = "dishCount", column = "dish_count"),
            @Result(property = "producerNumber", column = "producer_no"),
            @Result(property = "lastUpdateTime", column = "last_update_time")
    })
    List<OrderItemDTO2> getActiveOrderItemsByTableId(Long tableId);

    @Select("select count(*) from ca_order_item oi inner join ca_table t on t.id=oi.table_id inner join ca_order o on o.id=oi.order_id where o.order_status='active' and oi.table_id=#{tableId}")
    Integer countActiveItemsByTableId(Long tableId);

    @Select("<script>" +
            "select oi.id item_id, oi.assign_to producer_id, d.name dish_name, oi.dish_count, oi.description  from ca_order_item oi inner join ca_order o on o.id=oi.order_id inner join ca_dish d on d.id=oi.dish_id WHERE o.order_status='active' and oi.status='assigned' and oi.assign_to in \n" +
            "    <foreach item='item' collection='producerIds' open='(' separator=',' close=')'>\n" +
            "    #{item}" +
            "    </foreach>" +
            " order by oi.last_update_time asc"+
            "</script>")
    List<KitBoardItems> findAssignmentInBoards(@Param("producerIds") List<Long> producerIds);


    @Select("select o.id from ca_order_item oi inner join ca_table t on t.id=oi.table_id inner join ca_order o on o.id=oi.order_id where t.id=#{tableId} and o.order_status='active' limit 1")
    Long findActiveOrderIdByTable(Long tableId);

    @Select("select * from ca_order_item oi inner join ca_order o on o.id=oi.order_id where order_id=#{orderId} and dish_id=#{dishId} and table_id=#{tableId} and oi.status='unassigned' and o.order_status='active'")
    OrderItem findActiveExistingUnassignedItemsByOrderTableAndDish(Long orderId, Long dishId, Long tableId);
}
