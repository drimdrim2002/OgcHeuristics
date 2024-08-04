package dev.brown.domain;

import dev.brown.Constants.ORDER_CRITERIA;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


public class OrderPool {

//    private static final Logger log = LoggerFactory.getLogger(OrderPool.class);

    public HashSet<Integer> consumedRecord() {
        return consumedRecord;
    }

    // 어떤 order 가 consume 되었는지 알아야 한다.
    private final HashSet<Integer> consumedRecord;
    // 어디서부터 시작했는지 알아야 한다.
    private int orderIndexByDeadline = 0;
    private int orderIndexByCapacity = 0;

    // 우선 순위로 order를 가지고 있어야 한다
    TreeMap<Integer, Order> orderPoolByDeadline;
    TreeMap<Integer, Order> orderPoolByCapacity;

    public OrderPool(HashMap<Integer, Order> orderMap, HashMap<String, Rider> sampleRiderMap) {
        consumedRecord = new HashSet<>();
        TreeMap<Integer, TreeMap<Integer, Order>> orderMapByDeadline = new TreeMap<>();
        TreeMap<Integer, TreeMap<Integer, Order>> orderMapByCapacity = new TreeMap<>();
        for (Order order : orderMap.values()) {
            consumedRecord.add(order.id());

            Rider sampleRIder = sampleRiderMap.get("CAR");
            if (!makePriorityMap(order, sampleRIder, orderMapByDeadline, orderMapByCapacity)) { // 아마 거의 없을 것이다. 속도가 문제임
                sampleRIder.removeOrder(order);
                sampleRIder = sampleRiderMap.get("BIKE");
                makePriorityMap(order, sampleRIder, orderMapByDeadline, orderMapByCapacity);
            }
            sampleRIder.removeOrder(order);
        }

        makePriority(orderMapByDeadline, orderPoolByDeadline);

        makePriority(orderMapByCapacity, orderPoolByCapacity);

    }

    private void makePriority(TreeMap<Integer, TreeMap<Integer, Order>> orderMapByDeadline,
        TreeMap<Integer, Order> orderPoolByDeadline) {
        int orderPriority = 0;
        for (Integer extraTime : orderMapByDeadline.keySet()) {
            for (Integer orderId : orderMapByDeadline.get(extraTime).keySet()) {
                Order order = orderMapByDeadline.get(extraTime).get(orderId);
                orderPoolByDeadline.put(orderPriority, order);
                orderPriority += 1;
            }
        }
    }

    private boolean makePriorityMap(Order order, Rider sampleRIder, TreeMap<Integer
        , TreeMap<Integer, Order>> orderMapByDeadline
        , TreeMap<Integer, TreeMap<Integer, Order>> orderMapByCapacity) {
        CalculationResult calculationResult = sampleRIder.addOrder(order);
        if (calculationResult.isFeasible()) {
            int extraTime = calculationResult.extraTime();
            int extraCapa = sampleRIder.capa() - order.volume();

            orderMapByDeadline.putIfAbsent(extraTime, new TreeMap<>());
            orderMapByCapacity.putIfAbsent(extraCapa, new TreeMap<>());

            orderMapByDeadline.get(extraTime).put(order.id(), order);
            orderMapByCapacity.get(extraCapa).put(order.id(), order);
        }
        return calculationResult.isFeasible();
    }

    public void consume (Order order, ORDER_CRITERIA orderCriteria) {
        this.consumedRecord.add(order.id());
        if (orderCriteria.equals(ORDER_CRITERIA.DEADLINE)) {
            orderPoolByDeadline.remove(orderIndexByDeadline);
            orderIndexByDeadline += 1;
        } else {
            orderPoolByCapacity.remove(orderIndexByCapacity);
            orderIndexByCapacity += 1;

        }

    }

    public Order getNextOrder(ORDER_CRITERIA orderCriteria) {
        if (orderCriteria.equals(ORDER_CRITERIA.DEADLINE)){

            while (true) {
                if (orderIndexByDeadline > orderPoolByDeadline.size()) {
                    return null;
                }

                Order nextOrder = orderPoolByDeadline.get(orderIndexByDeadline);
                if (!consumedRecord.contains(nextOrder.id())) {
                    return nextOrder;
                }
                orderIndexByDeadline += 1;
            }

        } else {
            while (true) {
                if (orderIndexByCapacity > orderPoolByCapacity.size()) {
                    return null;
                }

                Order nextOrder = orderPoolByCapacity.get(orderIndexByCapacity);
                if (!consumedRecord.contains(nextOrder.id())) {
                    return nextOrder;
                }
                orderIndexByCapacity += 1;
            }

        }
    }


}
