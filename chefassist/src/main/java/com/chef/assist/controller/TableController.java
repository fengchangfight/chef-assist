package com.chef.assist.controller;

import com.chef.assist.config.CaResponse;
import com.chef.assist.constants.OrderConstants;
import com.chef.assist.mapper.OrderItemMapper;
import com.chef.assist.mapper.OrderMapper;
import com.chef.assist.mapper.TableMapper;
import com.chef.assist.model.Order;
import com.chef.assist.model.Table;
import com.chef.assist.model.dto.*;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/table")
public class TableController {
    @Autowired
    private TableMapper tableMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @PostMapping
    public CaResponse registerTable(@RequestBody Table table){
        try{
            tableMapper.insert(table);
            return CaResponse.makeResponse(true,"成功创建餐桌:"+table.getTableNumber(), table.getTableNumber());
        }catch (Exception e){
            if(e instanceof DuplicateKeyException){
                return CaResponse.makeResponse(false,"已存在同号餐桌:"+table.getTableNumber(), null);
            }
            return CaResponse.makeResponse(false,"未知错误",null);
        }
    }

    @PutMapping("/{id}")
    public CaResponse editTable(@PathVariable("id") Long id, @Valid @RequestBody EditTableRequest request){
        Table table = tableMapper.findById(id);
        table.setDescription(request.getDescription());
        table.setTableNumber(request.getTableNumber());

        try{
            tableMapper.update(table);
        }catch (Exception e){
            if(e instanceof DataIntegrityViolationException){
                return CaResponse.makeResponse(false, "不合法的输入或重名", null);
            }else{
                return CaResponse.makeResponse(false, "修改餐桌未知失败", null);
            }
        }

        return CaResponse.makeResponse(true, "成功修改餐桌信息", id);
    }

    @DeleteMapping("/{id}")
    public CaResponse deleteTable(@PathVariable("id") Long id){
        try{
            tableMapper.delete(id);
        }catch (Exception e){
            if(e instanceof DataIntegrityViolationException){
                return CaResponse.makeResponse(false,"该餐桌被引用，不能删除", id);
            }else{
                return CaResponse.makeResponse(false, "未知错误", null);
            }
        }

        return CaResponse.makeResponse(true, "成功删除餐桌", id);
    }

    @Autowired
    private Environment env;

    @GetMapping
    public PaginationWrapper getAllTablesByPage(@RequestParam(value = "page", required = true) Integer page){
        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        PageHelper.startPage(page,defaultPageSize);
        List<Table> tables = tableMapper.findAll();
        int totalSize = tableMapper.count();

        PaginationWrapper result = new PaginationWrapper();
        result.setTotal(totalSize);
        result.setPageSize(defaultPageSize);
        result.setCurrentPage(page);
        result.setData(tables);

        return result;
    }

    @PutMapping("/finish/{id}")
    public CaResponse finshOrderByTable(@PathVariable("id") Long id){

        // step 0: find the only active order by table id
        Long orderId = orderItemMapper.findActiveOrderIdByTable(id);
        // step 1: check if all items in served state

        if(orderId == null){
            return CaResponse.makeResponse(false, "当前桌无进行中订单", id);
        }

        List<String> itemStatus = orderMapper.findAllItemsStatus(orderId);
        for(String status: itemStatus){
            if(!OrderConstants.ORDER_ITEM_SERVED.equals(status)){
                return CaResponse.makeResponse(false,"本订单仍有未上齐的菜", id);
            }
        }

        // step2: update status and end time
        Order order = orderMapper.findById(orderId);
        order.setOrderStatus(OrderConstants.ORDER_FINISHED);
        order.setEndTime(new Date());
        orderMapper.update(order);

        return CaResponse.makeResponse(true,"成功结束订单", id);
    }

    @GetMapping("/all")
    public List<Table> getAllTables(){
        return tableMapper.findAll();
    }

    @GetMapping("/withitems")
    public PaginationWrapper getTablesWithItems(@RequestParam(value = "page", required = true) Integer page){
        // step 1: get kits by page
        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        PageHelper.startPage(page,defaultPageSize);
        List<Table> tables = tableMapper.findAll();
        List<Long> tableIds = tables.stream().map(r->{return r.getId();}).collect(Collectors.toList());
        int totalSize = tableMapper.count();

        // step 2: for all dish producers got in step 1, get all assigned(active order) order items and group those items by kits
        List<TableBoardItems> tableBoardItems = new ArrayList<>();
        if(tableIds==null || tableIds.size()<1){

        }else{
            tableBoardItems = tableMapper.findTableItemsInBoards(tableIds);
        }

        List<TableBoardDTO> data = groupItemsByTableBoard(tables, tableBoardItems);

        PaginationWrapper result = new PaginationWrapper();
        result.setTotal(totalSize);
        result.setPageSize(defaultPageSize);
        result.setCurrentPage(page);
        result.setData(data);

        return result;
    }

    private List<TableBoardDTO> groupItemsByTableBoard(List<Table> tables, List<TableBoardItems> tableBoardItems){
        List<TableBoardDTO> result = new ArrayList<>();

        Map<Long, List<TableBoardDTO.BoardInnerItems>> interMediateMap = new HashMap<>();
        for(TableBoardItems t: tableBoardItems){
            if(interMediateMap.containsKey(t.getTableId())){
                interMediateMap.get(t.getTableId()).add(new TableBoardDTO.BoardInnerItems(t.getItemId(), t.getDishName(), t.getDishCount(), t.getDescription(), t.getStatus()));
            }else{
                interMediateMap.put(t.getTableId(), new ArrayList<>());
                interMediateMap.get(t.getTableId()).add(new TableBoardDTO.BoardInnerItems(t.getItemId(), t.getDishName(), t.getDishCount(), t.getDescription(), t.getStatus()));
            }
        }

        for(Table tb: tables){
            TableBoardDTO tbt = new TableBoardDTO();

            tbt.setId(tb.getId());
            tbt.setDescription(tb.getDescription());
            tbt.setInnerItems(interMediateMap.get(tb.getId()));
            tbt.setTableNumber(tb.getTableNumber());

            result.add(tbt);
        }

        return result;
    }

    @GetMapping("/{id}")
    public Table getTableById(@PathVariable("id") Long id){
        return tableMapper.findById(id);
    }

}
