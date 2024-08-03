package dev.brown.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class OrderPool {

    // order 난이도에 따라 order를 정렬해야 한다.
    private List<Order> orderPoolList;

    // 어떤 order 가 consume 되었는지 알아야 한다.
    HashMap<Integer, Boolean> consumedRecord;

    public OrderPool(List<Order> orderList) {
        consumedRecord = new HashMap<>();
        orderPoolList = orderList;
        for (Order order : orderList) {
            consumedRecord.put(order.id(), false);
        }
    }


}
