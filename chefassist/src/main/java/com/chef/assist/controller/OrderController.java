package com.chef.assist.controller;

import com.chef.assist.config.CaResponse;
import com.chef.assist.constants.OrderConstants;
import com.chef.assist.mapper.OrderItemMapper;
import com.chef.assist.mapper.OrderMapper;
import com.chef.assist.model.Order;
import com.chef.assist.model.User;
import com.chef.assist.model.dto.OrderDTO;
import com.chef.assist.model.dto.OrderDTO2;
import com.chef.assist.model.dto.PaginationWrapper;
import com.chef.assist.service.UserService;
import com.chef.assist.utils.MyStringUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;

    /**
     * 创建新订单，注意事项：确保全局唯一的order_number，目前只是做到了大概率确保
     * @return
     */
    @PostMapping("/new")
    public CaResponse createNewOrder(){
        Order order = new Order();

        String orderNo = "o_"+MyStringUtil.randomAlpha(10)+MyStringUtil.randomNumeric(4);
        Date now = new Date();
        order.setOrderNumber(orderNo);
        order.setOrderStatus(OrderConstants.ORDER_ACTIVE);
        order.setStartTime(now);
        User currentUser = userService.getCurrentUser();
        order.setCreatedBy(currentUser.getId());

        orderMapper.insert(order);

        return CaResponse.makeResponse(true, "成功创建新订单", order.getId());
    }

    /**
     * 结束订单，结束前确认订单所有的菜都已上齐
     * @param id
     * @return
     */
    @PutMapping("/finish/{id}")
    public CaResponse finshOrder(@PathVariable("id") Long id){
        // step 1: check if all items in served state
        List<String> itemStatus = orderMapper.findAllItemsStatus(id);
        for(String status: itemStatus){
            if(!OrderConstants.ORDER_ITEM_SERVED.equals(status)){
                return CaResponse.makeResponse(false,"本订单仍有未上齐的菜", id);
            }
        }

        // step2: update status and end time
        Order order = orderMapper.findById(id);
        order.setOrderStatus(OrderConstants.ORDER_FINISHED);
        order.setEndTime(new Date());
        orderMapper.update(order);

        return CaResponse.makeResponse(true,"成功结束订单", id);
    }

    @DeleteMapping("/active/{id}")
    public CaResponse deleteOrder(@PathVariable("id") Long id){
        Order order = orderMapper.findById(id);
        if(!OrderConstants.ORDER_ACTIVE.equals(order.getOrderStatus())){
            // not active order
            return CaResponse.makeResponse(false, "该订单不是进行中订单，不可删除", id);
        }
        Integer cnt = orderItemMapper.countByOrderId(id);

        if(cnt>0){
            return CaResponse.makeResponse(false, "该订单仍有菜品未完成，不可删除", id);
        }

        orderMapper.delete(id);
        return CaResponse.makeResponse(true, "成功删除该订单", id);

    }

    /**
     * 根据id获取订单
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public OrderDTO getOrderById(@PathVariable("id") Long id){
        return orderMapper.findBasicById(id);
    }


    /**
     * 获取所有活动订单（分页）
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/active")
    public PaginationWrapper findActiveOrders(@RequestParam(value = "page", required = true) Integer page, @RequestParam(value = "pageSize", required = false) Integer pageSize){

        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        if(pageSize == null){
            pageSize = defaultPageSize;
        }

        PageHelper.startPage(page,pageSize);

        List<OrderDTO2> activeOrders = orderMapper.findActiveOrders();

        int total = orderMapper.countActiveOrders();

        return new PaginationWrapper(total, pageSize, page, activeOrders);
    }

    /**
     * 获取所有历史订单（分页）
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/history")
    public PaginationWrapper findFinishedOrders(@RequestParam(value = "page", required = true) Integer page, @RequestParam(value = "pageSize", required = false) Integer pageSize){

        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        if(pageSize == null){
            pageSize = defaultPageSize;
        }

        PageHelper.startPage(page,pageSize);

        List<OrderDTO2> historyOrders = orderMapper.findFinishedOrders();

        int total = orderMapper.countFinishedOrders();

        return new PaginationWrapper(total, pageSize, page, historyOrders);
    }


    @GetMapping("/history-by-table/{id}")
    public PaginationWrapper findFinishedOrdersByTable(@PathVariable("id") Long tableId, @RequestParam(value = "page", required = true) Integer page, @RequestParam(value = "pageSize", required = false) Integer pageSize){

        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        if(pageSize == null){
            pageSize = defaultPageSize;
        }

        PageHelper.startPage(page,pageSize);

        List<OrderDTO2> historyOrders = orderMapper.findFinishedOrdersByTable(tableId);

        int total = orderMapper.countFinishedOrdersByTable(tableId);

        return new PaginationWrapper(total, pageSize, page, historyOrders);
    }
}
