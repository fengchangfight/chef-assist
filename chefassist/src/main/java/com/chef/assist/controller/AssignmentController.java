package com.chef.assist.controller;

import com.chef.assist.config.CaResponse;
import com.chef.assist.mapper.OrderItemMapper;
import com.chef.assist.service.AssignmentService;
import com.chef.assist.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/assignment")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 本函数数据库的一系列读写操作，基本思想是先读数据库获取资源现状，再根据资源现状进行分配，
     * 可能出现的问题是两个客户端读数据库获取资源现状，然后甲先更新了数据库，此时资源现状已变化，乙却仍按照老的资源现状来更新数据库
     * 这样算法就会在这种并发的情况下发生问题，解决思路为分布式锁的思想，即任何一个客户端想作此操作，必须先获得一个锁，读写数据库完毕后
     * 再释放锁，保证了使用该函数读写操作作为一个整体，是不会被多个客户端并行的
     * @param orderId
     * @return
     */
    @PostMapping("/{orderId}")
    public CaResponse AssignUnassignedByOrderId(@PathVariable("orderId") Long orderId){
        // step 0: count unassigned items in orderId
        int cnt = orderItemMapper.countUnassignedAndFailedAssigned(orderId);
        if(cnt<1){
            return CaResponse.makeResponse(false,"无可分配菜品", orderId);
        }

        // step 1: make unassigned items -> waiting_assign(synchronus)
        orderService.makeUnassignedWaiting(orderId);

        // step 2: do the actual assignment(asynchronous) in separate thread, do not care result in this thread
        assignmentService.assignWaitingAssignedAndFailedAssign(orderId);

        return CaResponse.makeResponse(true, "已分配菜品到厨位", orderId);

    }
}
