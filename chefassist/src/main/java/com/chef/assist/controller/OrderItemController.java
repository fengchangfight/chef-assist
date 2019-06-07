package com.chef.assist.controller;

import com.chef.assist.config.CaResponse;
import com.chef.assist.constants.OrderConstants;
import com.chef.assist.mapper.OrderItemMapper;
import com.chef.assist.mapper.OrderMapper;
import com.chef.assist.model.Order;
import com.chef.assist.model.OrderItem;
import com.chef.assist.model.dto.*;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order-item")
public class OrderItemController {
    private static final Logger LOG = LoggerFactory.getLogger(OrderItemController.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private Environment env;

    /**
     * 更新未分配至厨房的订单菜品条目的数量（通过表单中的加减按钮操作的）
     * @param request
     * @return
     */
    @PutMapping("/unassigned-count")
    public CaResponse updateUnassignedItemCount(@Valid @RequestBody UpdateCountRequest request){
        // check order must be in unassinged status

        OrderItem orderItem = orderItemMapper.findById(request.getItemId());
        // not equal to unassigned
        if(!OrderConstants.ORDER_ITEM_UNASSIGNED.equals(orderItem.getStatus())){
            return CaResponse.makeResponse(false, "该条目未处于未分配状态", request.getItemId());
        }

        // if count =0 just delete this item
        if(request.getCount()<=0){
            orderItemMapper.deleteById(request.getItemId());
            return CaResponse.makeResponse(true, "已删除条目", request.getItemId());
        }else{
            orderItemMapper.updateItemCount(request.getItemId(), request.getCount());
            return CaResponse.makeResponse(true, "成功更新条目数", request.getItemId());
        }
    }

    @PostMapping
    public CaResponse newOrderItem(@RequestBody OrderItem orderItem){
        CaResponse result = new CaResponse();

        // check if order is active
        Long orderId = orderItem.getOrderId();
        Order order = orderMapper.findById(orderId);
        if(!OrderConstants.ORDER_ACTIVE.equals(order.getOrderStatus())){
            // not active
            return CaResponse.makeResponse(false,"不在进行中的订单不能增添条目", null);
        }

        // check if this table already bind other order
        Integer cnt = orderItemMapper.countActiveOrderItemByTableIdAndNotOrderId(orderItem.getTableId(), orderItem.getOrderId());
        if(cnt>0){
            return CaResponse.makeResponse(false,"该桌已有其他活跃订单绑定", null);
        }

        // check if order, dish, table, active, unassigned already has an item there
        OrderItem existing = orderItemMapper.findActiveExistingUnassignedItemsByOrderTableAndDish(orderItem.getOrderId(), orderItem.getDishId(), orderItem.getTableId());

        if(existing!=null){
            // just update count and last update would be fine
            existing.setLastUpdateTime(new Date());
            existing.setDishCount(existing.getDishCount()+orderItem.getDishCount());
            existing.setDescription(existing.getDescription()+(StringUtils.isEmpty(orderItem.getDescription())?"":(";"+orderItem.getDescription())));
            orderItemMapper.update(existing);
        }else{
            // do insert
            orderItem.setAssignTo(null);
            orderItem.setStatus(OrderConstants.ORDER_ITEM_UNASSIGNED);
            orderItem.setLastUpdateTime(new Date());
            orderItemMapper.insert(orderItem);
        }

        result.setOk(true);
        result.setMessage("成功添加条目");
        result.setData(orderItem.getOrderId());

        return result;
    }

    @GetMapping
    public List<OrderItemDTO> getOrderItemsByOrderId(@RequestParam("orderId") Long orderId){

        List<OrderItemDTO> data = orderItemMapper.getOrderItemsByOrderId(orderId);

        return data;
    }

    @GetMapping("/active-item-of-table")
    public  PaginationWrapper getActiveOrderItemsByTableId(@RequestParam("tableId") Long tableId, @RequestParam(value = "pageSize", required = false) Integer pageSize, @RequestParam(value = "page", required = true) Integer page){

        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        if(pageSize == null){
            pageSize = defaultPageSize;
        }
        PageHelper.startPage(page,pageSize);

        List<OrderItemDTO2> data = orderItemMapper.getActiveOrderItemsByTableId(tableId);

        int total = orderItemMapper.countActiveItemsByTableId(tableId);

        return new PaginationWrapper(total, pageSize, page, data);
    }

    @PutMapping("/finish-cooking/{itemId}")
    public CaResponse finishCooking(@PathVariable("itemId") Long itemId){
        orderItemMapper.updateItemStatusWithTimestamp(itemId, OrderConstants.ORDER_ITEM_READY_TO_SERVE);

        return CaResponse.makeResponse(true,"成功更新条目状态", itemId);
    }

    @PutMapping("/served/{itemId}")
    public CaResponse served(@PathVariable("itemId") Long itemId){
        orderItemMapper.updateItemStatusWithTimestamp(itemId, OrderConstants.ORDER_ITEM_SERVED);

        return CaResponse.makeResponse(true,"成功更新条目状态", itemId);
    }

    @PutMapping("/description")
    public CaResponse updateDescription(@RequestBody UpdateItemDescriptionRequest request){
        try{
            orderItemMapper.updateItemDescription(request.getItemId(), request.getDescription());
        }catch (Exception e){
            LOG.error(e.getMessage());
            if(e instanceof DataIntegrityViolationException){
                return CaResponse.makeResponse(false,"备注数据太长哦(づ￣ 3￣)づ", null);
            }
        }

        return CaResponse.makeResponse(true, "成功更新条目备注", request.getItemId());
    }
}
