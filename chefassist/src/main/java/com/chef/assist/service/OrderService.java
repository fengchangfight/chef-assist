package com.chef.assist.service;

import com.chef.assist.mapper.OrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    public void makeUnassignedWaiting(Long orderId){
        orderItemMapper.makeUnassignedWaiting(orderId);
    }
}
