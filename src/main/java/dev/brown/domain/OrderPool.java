package dev.brown.domain;

import dev.brown.util.MatrixManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;


public class OrderPool {

    private final List<Order> totalOrderList = new ArrayList<>();
    private final HashSet<Integer> consumedRecord;

    public HashSet<Integer> consumedRecord() {
        return consumedRecord;
    }

    // 우선 순위로 order를 가지고 있어야 한다
    TreeMap<Integer, Order> orderPoolByDeadline = new TreeMap<>();
    HashMap<Integer, Integer> priorityMapByDeadline = new HashMap<>();


    public OrderPool(HashMap<Integer, Order> orderMap) {
        consumedRecord = new HashSet<>();
        totalOrderList.addAll(orderMap.values().stream().toList());
        List<Order> sortedOrderByDeadline = orderMap.values().stream()
            .sorted(Comparator.comparing(Order::deadline)
                .thenComparing(Order::readyTime)
                .thenComparing(Order::id))
            .toList();

        int orderPriority = 0;
        for (Order order : sortedOrderByDeadline) {
            orderPoolByDeadline.put(orderPriority, order);
            priorityMapByDeadline.put(order.id(), orderPriority);
            orderPriority += 1;
        }
    }

    public void consume(Order order) {
        this.consumedRecord.add(order.id());
        orderPoolByDeadline.remove(priorityMapByDeadline.get(order.id()));
    }

    public Order getNextOrder() {
        for (Order order : orderPoolByDeadline.values()) {
            if (!consumedRecord.contains(order.id())) {
                return order;
            }
        }
        return null;
    }


    public boolean orderRemains() {
        return this.consumedRecord.size() < this.totalOrderList.size();
    }


    public int findNearestIndex(Order order, HashSet<Integer> tried) {

        for (Entry<Integer, List<Integer>> nearestDistanceEntry
            : MatrixManager.nearestDistanceMap.get(order.id()).entrySet()) {
            for (Integer orderIndex : nearestDistanceEntry.getValue()) {
                if (!tried.contains(orderIndex) && ! consumedRecord.contains(orderIndex)) {
                    return orderIndex;
                }
            }
        }

        return -1;
    }

}
