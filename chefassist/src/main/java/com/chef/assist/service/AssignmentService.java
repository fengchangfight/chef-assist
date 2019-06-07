package com.chef.assist.service;

import com.chef.assist.mapper.DishProducerMapper;
import com.chef.assist.mapper.OrderItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@Service
public class AssignmentService {
    private Lock assignmentLock = new ReentrantLock();

    private static final Logger LOG = LoggerFactory.getLogger(AssignmentService.class);
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private DishProducerMapper dishProducerMapper;

    /**
     * assume the items to be processed are all in waiting_assign or failed_assign status
     * if succeed: waiting_assign->assigned, otherwise waiting_assign->unassigned back
     * @param orderId
     * @return
     */
    @Async
    public void assignWaitingAssignedAndFailedAssign(Long orderId){
        boolean gotLock =  assignmentLock.tryLock();
        int retry=0;
        while(!gotLock){
            if(retry>10){
                orderItemMapper.makeWaiting2failedAssign(orderId);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                LOG.error("===Sleep error");
                return;
            }
            gotLock =  assignmentLock.tryLock();
            retry++;
        }

        // lock got, do everything in try, because we need finally block to ensure the unlocking
        try {
            // step 0: find online producers to be assigned to
            List<Long> onlineDishProducerIds = dishProducerMapper.findAllOnlineProducers();
            if(onlineDishProducerIds==null || onlineDishProducerIds.size()<1){
                LOG.warn("===没有可用的菜品生产者");
                return;
            }

            // step 1: get work producer workload, store in map <k,v>, k is producer number, v is its sum of working time of active working list assigned to it(item before readytoserve
            // ) item status flow: unassigned(red)->waiting_assign(yellow)->assigned(blue)->readytoserve(amber)->served(green)

            Map<Long, Integer> currentWorkloadMap = new HashMap<>();
            List<Map<String,Object>> currentWorkList = orderItemMapper.countProducerCurrentWork();
            for(Map<String,Object> mp : currentWorkList){
                Integer time2do = Integer.valueOf(mp.get("worktodo").toString());
                Long assignToId = Long.parseLong(mp.get("assign_to").toString());
                currentWorkloadMap.put(assignToId, time2do);
            }
            // 这是谨防上一步没结果，即使没结果，online producer的key也应该存在于currentWorkloadMap中，只不过value为0而已
            for(Long d: onlineDishProducerIds){
                if(currentWorkloadMap.get(d)==null){
                    currentWorkloadMap.put(d, 0);
                }
            }

            if(currentWorkloadMap.size()<1){
                LOG.error("No producer available");
                return;
            }

            // step 2: get the list of waiting assigned or failed_assign items under given orderId(which must be active state), make a map ov k, v, where k is item id and v the expected working(cooking) time
            Map<Long, Integer> toBeAssigned = new HashMap<>();
            List<Map<String,Object>> toBeAssignedItems =  orderItemMapper.getWaitingAssignOrFailedAssignWorkInOrder(orderId);
            for(Map<String,Object> mp: toBeAssignedItems){
                Long itemId = Long.parseLong(mp.get("id").toString());
                Integer workTime = Integer.valueOf(mp.get("expected_cooking_time").toString());
                toBeAssigned.put(itemId, workTime);
            }

            if(toBeAssigned==null || toBeAssigned.size()<1){
                LOG.warn("没有需要assign菜品");
                return;
            }

            //以下为正常assign流程
            // step 3: use results from step 1 and step 2 as input, give the out put int the form of <k1,k2> where k1 is the item id and k2 is the producer id, this indicates the actual mapping of assignment
            Map<Long,Long> assignmentMap = calcAssignment(currentWorkloadMap, toBeAssigned);

            // step 4: update database assign_to field with the mapping from step 3
            updateAssignmentDb(assignmentMap);

            LOG.info("已分配订单"+orderId+"任务到生产者");
        }finally {
            //释放分布式锁，这样其他线程可以做分派菜品的操作，重要！！！
            assignmentLock.unlock();
        }
    }

    private void updateAssignmentDb(Map<Long,Long> updateAssignmentDb){
        if(updateAssignmentDb.size()>0){
            orderItemMapper.batchAssign(updateAssignmentDb);
        }
    }
    private Map<Long,Long> calcAssignment(Map<Long, Integer> currentWorkloadMap, Map<Long, Integer> toBeAssigned){
        //已经通过外部确保输入的两个map都不为空
        Map<Long,Long> assignment =  new HashMap<>();

        LinkedHashMap<Long, Integer> currentWorkloadMapSorted = sortMapByValue(currentWorkloadMap);
        List<Long> keys = new ArrayList<>(currentWorkloadMapSorted.keySet());

        // sort this map by value
        LinkedHashMap<Long, Integer> toBeAssignedSorted = sortMapByValue(toBeAssigned);
        //思想是从当前要分配的列表中取最轻的任务，先分配到负荷最小的生产者那里去，然后更新生产者负荷列表，依次类推，直到所有的要分配的任务都分配完成
        for(Long k:toBeAssignedSorted.keySet()){
            assignment.put(k, keys.get(0));
            //添加到人物队列，更新任务队列负荷
            currentWorkloadMapSorted.put(keys.get(0),currentWorkloadMapSorted.get(keys.get(0))+toBeAssignedSorted.get(k));
            //重新排序任务队列
            currentWorkloadMapSorted=sortMapByValue(currentWorkloadMapSorted);
            //获取排序后的任务队列key列表以备后用
            keys = new ArrayList<>(currentWorkloadMapSorted.keySet());
        }
        return assignment;
    }

    private LinkedHashMap<Long,Integer> sortMapByValue(Map<Long,Integer> mp){
        LinkedHashMap<Long, Integer> result = mp
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                        toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                LinkedHashMap::new));

        return result;
    }

}
