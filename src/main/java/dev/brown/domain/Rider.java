package dev.brown.domain;

import dev.brown.Constants;
import dev.brown.util.CalculationUtils;
import dev.brown.util.MatrixManager;
import dev.brown.util.Permutation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.lang3.ObjectUtils;

public class Rider {

//    private static final Logger log = LoggerFactory.getLogger(Rider.class);
    private final int id;
    private final String type;
    private final double speed;
    private final int capa;
    private final int varCost;
    private final int fixedCost;
    private final int serviceTime;

    private List<Order> orderList;
    private int totalCost;
    private int distance;
    private List<Integer> shopIndexList = new ArrayList<>();
    private List<Integer> deliveryIndexList = new ArrayList<>();

    public void setShopIndexList(List<Integer> shopIndexList) {
        this.shopIndexList = shopIndexList;
    }

    public void setDeliveryIndexList(List<Integer> deliveryIndexList) {
        this.deliveryIndexList = deliveryIndexList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    private Solution solution;

    public void setCost(int cost) {
        this.cost = cost;
    }

    private int cost;

    public boolean isValid() {
        return isValid;
    }

    public int cost() {
        return cost;
    }

    private boolean isValid;

    public Rider(int id, String type, double speed, int capa, int varCost, int fixedCost, int serviceTime) {
        this.id = id;
        this.type = type;
        this.speed = speed;
        this.capa = capa;
        this.varCost = varCost;
        this.fixedCost = fixedCost;
        this.serviceTime = serviceTime;
        this.orderList = new ArrayList<>();
        this.cost = 0;
        this.isValid = true;
        Constants.bestShopIndexMap.put(this.type(), new HashMap<>());
        Constants.bestDeliveryIndexMap.put(this.type(), new HashMap<>());
        Constants.bestScore.put(this.type(), new HashMap<>());
//        this.extraTime = 0;
    }

    public double speed() {
        return speed;
    }

    public int capa() {
        return capa;
    }


    public int id() {
        return id;
    }


    public String type() {
        return type;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    @Override
    public String toString() {
        return defaultInformation();
    }

    private String defaultInformation() {
        return "Rider{" +
            "capa=" + capa +
            ", id=" + id +
            ", type='" + type + '\'' +
            ", varCost=" + varCost +
            ", fixedCost=" + fixedCost +
            ", serviceTime=" + serviceTime +
            '}';
    }

    public int serviceTime() {
        return serviceTime;
    }

//    private String additionalInformation() {
//        StringBuilder sb = new StringBuilder();
//        for (Integer shopIndex : this.shopIndexList) {
//            sb.append(shopIndex)
//                .append("->");
//        }
//        for (Integer deliveryIndex : this.deliveryIndexList) {
//            sb.append(deliveryIndex)
//                .append("->");
//        }
//        return sb.substring(0, sb.length() - 2);
//
//    }

    public int calculateCost() {
        return this.fixedCost + (int) (this.distance / 100.0 * this.varCost);
    }


    public void addOrder(Order order) {
        this.orderList.add(order);

        shopIndexList.add(order.id());
        deliveryIndexList.add(order.id());

        List<List<Integer>> shopPermuationList = Permutation.generatePermutations(shopIndexList);
        List<List<Integer>> deliveryPermutationList = Permutation.generatePermutations(deliveryIndexList);

        int minCost = Integer.MAX_VALUE;

        TreeMap<Integer, HashMap<String, List<Integer>>> bestScoreRecord = new TreeMap<>();

        for (List<Integer> shopSeqListCandidate : shopPermuationList) {
            for (List<Integer> deliveryShopSeqListCandidate : deliveryPermutationList) {

//                if (shopSeqListCandidate.size() == 3
//                    && shopSeqListCandidate.get(0) == 0
//                    && shopSeqListCandidate.get(1) == 18
//                    && shopSeqListCandidate.get(2) == 3
//                    && deliveryShopSeqListCandidate.get(0) == 3
//                    && deliveryShopSeqListCandidate.get(1) == 0
//                    && deliveryShopSeqListCandidate.get(2) == 18
//                    && this.type().equals("CAR")
//                ) {
//                    int t = 1;
//                    t = 2;
//                }

                resetRecord();
                CalculationResult calculationResult = calculate(shopSeqListCandidate, deliveryShopSeqListCandidate);

                int newScore = calculationResult.cost();
                if (calculationResult.isFeasible() && newScore < minCost) {
                    bestScoreRecord.put(newScore, new HashMap<>());
                    bestScoreRecord.get(newScore).put("shopIndexList", shopSeqListCandidate);
                    bestScoreRecord.get(newScore).put("deliveryIndexList", deliveryShopSeqListCandidate);
                    minCost = calculationResult.cost();
                }
            }
        }

        if (!bestScoreRecord.isEmpty()) {

            List<Integer> idCollect = getSortedSequence();
            String orderKey = CalculationUtils.getKeyFromList(idCollect);
            HashMap<String, List<Integer>> indexListMap = bestScoreRecord.firstEntry().getValue();
            List<Integer> bestShopIndexList = indexListMap.get("shopIndexList");
            List<Integer> bestDeliveryIndexList = indexListMap.get("deliveryIndexList");

            String shopIndexKey = CalculationUtils.getKeyFromList(bestShopIndexList);
            String deliveryIndexKey = CalculationUtils.getKeyFromList(bestDeliveryIndexList);

            shopIndexList.clear();
            deliveryIndexList.clear();

            shopIndexList.addAll(bestShopIndexList);
            deliveryIndexList.addAll(bestDeliveryIndexList);

//            if (shopIndexList.size() == 3
//                && shopIndexList.get(0) == 0
//                && shopIndexList.get(1) == 18
//                && shopIndexList.get(2) == 3
//                && deliveryIndexList.get(0) == 3
//                && deliveryIndexList.get(1) == 0
//                && deliveryIndexList.get(2) == 18
//            ) {
//                int t = 1;
//                t = 2;
//            }

            Constants.bestShopIndexMap.get(this.type()).put(orderKey, shopIndexKey);
            Constants.bestDeliveryIndexMap.get(this.type()).put(orderKey, deliveryIndexKey);
            Constants.bestScore.get(this.type()).put(orderKey, minCost);

            this.isValid = true;
            this.cost = minCost;


        } else {
            this.isValid = false;
            this.cost += 1000000;
        }
    }

    private List<Integer> getSortedSequence() {
        return this.orderList.stream()
            .map(Order::id)
            .sorted()
            .toList();
    }

    public void removeOrder(Order order) {
        this.orderList.remove(order);

        String orderKey = CalculationUtils.getKeyFromList(this.getSortedSequence());

        this.resetRecord();
        if (!orderList.isEmpty()) {
            String backupShopIndexStr = Constants.bestShopIndexMap.get(type()).get(orderKey);
            String backupDeliveryIndexStr = Constants.bestDeliveryIndexMap.get(type()).get(orderKey);
            this.shopIndexList.addAll(Arrays.stream(backupShopIndexStr.split(",")).map(Integer::parseInt).toList());
            this.deliveryIndexList.addAll(
                Arrays.stream(backupDeliveryIndexStr.split(",")).map(Integer::parseInt).toList());
//            if (orderKey.equals("0,3,18")) {
//                log.info("==========");
//                for (Integer i : shopIndexList) {
//                    log.info(String.valueOf(i));
//                }
//                log.info("--------");
//                for (Integer i : deliveryIndexList) {
//                    log.info(String.valueOf(i));
//                }
//
//                log.info("==========");
//
//
//            }
            this.cost = Constants.bestScore.get(this.type()).get(orderKey);
        }
    }

    public void resetAll() {
        this.orderList.clear();
        resetRecord();
    }

    public void resetRecord() {
        this.shopIndexList.clear();
        this.deliveryIndexList.clear();
        this.cost = 0;
//        this.extraTime = 0;
        this.isValid = true;
    }

    public CalculationResult calculate() {
        if (ObjectUtils.isNotEmpty(this.shopIndexList)) {
            return calculate(this.shopIndexList, this.deliveryIndexList);
        } else {
            return null;
        }
    }

    public CalculationResult calculate(List<Integer> shopIndexList, List<Integer> deliveryIndexList) {

        int deliveryTime = 0;
        int totalDistance = 0;

        for (int shopVisitOrder = 0; shopVisitOrder < shopIndexList.size(); shopVisitOrder++) {
            Integer currShopIndex = shopIndexList.get(shopVisitOrder);
            if (shopVisitOrder > 0) {
                Integer prevShopIndex = shopIndexList.get(shopVisitOrder - 1);
                int shopDistance = MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
                totalDistance += shopDistance;
                deliveryTime += getShopDuration(prevShopIndex, currShopIndex);
            }
            Order order = this.solution.orderMap().get(currShopIndex);
            deliveryTime = Math.max(deliveryTime, order.readyTime());
        }

        int lastVisitShopIndex = shopIndexList.getLast();
        int firstVisitShopIndex = deliveryIndexList.getFirst();
        int shopToDeliveryDistance = MatrixManager.getShopToDeliveryDistance(
            lastVisitShopIndex, firstVisitShopIndex);
        totalDistance += shopToDeliveryDistance;

        deliveryTime += getShopToDeliveryDuration(lastVisitShopIndex, firstVisitShopIndex);

        int volSum = 0;
        boolean deadlineViolated = false;
        for (int deliveryVisitOrder = 0; deliveryVisitOrder < deliveryIndexList.size(); deliveryVisitOrder++) {
            Integer currDeliveryIndex = deliveryIndexList.get(deliveryVisitOrder);
            if (deliveryVisitOrder > 0) {
                Integer prevShopIndex = deliveryIndexList.get(deliveryVisitOrder - 1);
                int deliveryDistance = MatrixManager.getDeliveryDistance(prevShopIndex, currDeliveryIndex);
                totalDistance += deliveryDistance;
                deliveryTime += getDeliveryDuration(prevShopIndex, currDeliveryIndex);
            }
            Order order = this.solution.orderMap().get(currDeliveryIndex);
            if (order.deadline() < deliveryTime) {
                deadlineViolated = true;
                break;
            }
//            else {
//                this.extraTime = order.deadline() - deliveryTime;
//            }
            volSum += order.volume();
        }

        boolean isFeasible = volSum <= this.capa && !deadlineViolated;

        CalculationResult calculationResult = new CalculationResult(isFeasible);
        if (isFeasible) {
            this.distance = totalDistance;
            this.cost = calculateCost();
            calculationResult.setCost(calculateCost());
        }
        return calculationResult;
    }

//    public void calculateAll() {
//
//        int deliveryTime = 0;
//        int totalDistance = 0;
//        for (int shopVisitOrder = 0; shopVisitOrder < shopIndexList.size(); shopVisitOrder++) {
//            Integer currShopIndex = shopIndexList.get(shopVisitOrder);
//            if (shopVisitOrder > 0) {
//                Integer prevShopIndex = shopIndexList.get(shopVisitOrder - 1);
//                int distance = MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
//                totalDistance += distance;
//                deliveryTime += calculateDuration(distance);
//            }
//            Order order = this.solution.orderMap().get(currShopIndex);
//            deliveryTime = Math.max(deliveryTime, order.readyTime());
//        }
//
//        int shopToDeliveryDistance = MatrixManager.getShopToDeliveryDistance(
//            shopIndexList.get(shopIndexList.size() - 1), deliveryIndexList.get(0));
//        totalDistance += shopToDeliveryDistance;
//        deliveryTime += calculateDuration(shopToDeliveryDistance);
//
//        int volSum = 0;
//        boolean deadlineViolated = false;
//        for (int deliveryVisitOrder = 0; deliveryVisitOrder < deliveryIndexList.size(); deliveryVisitOrder++) {
//            Integer currDeliveryIndex = deliveryIndexList.get(deliveryVisitOrder);
//            if (deliveryVisitOrder > 0) {
//                Integer prevShopIndex = deliveryIndexList.get(deliveryVisitOrder - 1);
//                int distance = MatrixManager.getShopDistance(prevShopIndex, currDeliveryIndex)
//                totalDistance += shopToDeliveryDistance;
//                deliveryTime += calculateDuration(distance);
//            }
//            Order order = this.solution.orderMap().get(currDeliveryIndex);
//            if (order.deadline() < deliveryTime) {
//                deadlineViolated = true;
//                break;
//            }
//            volSum += order.volume();
//
//        }
//
//        if (volSum > this.capa || deadlineViolated) {
//            isValid = false;
//        } else {
//            cost = calculateCost(totalDistance);
//        }
//    }

    public List<Integer> deliveryIndexList() {
        return deliveryIndexList;
    }

    public List<Order> orderList() {
        return orderList;
    }

    public List<Integer> shopIndexList() {
        return shopIndexList;
    }

    public int getShopDuration(int originIndex, int destinationIndex) {
        return MatrixManager.getShopDuration(this.type, originIndex, destinationIndex);
    }

    public int getDeliveryDuration(int originIndex, int destinationIndex) {
        return MatrixManager.getDeliveryDuration(this.type, originIndex, destinationIndex);
    }

    public int getShopToDeliveryDuration(int originIndex, int destinationIndex) {
        return MatrixManager.getShopToDeliveryDuration(this.type, originIndex, destinationIndex);
    }

//    public int priority() {
//        return priority;
//    }


    /**
     * 현재까지의 경로를 기반으로 현재 시간을 계산
     * @return 현재 시간 (분 단위)
     */
    public int getCurrentTime() {
        if (shopIndexList.isEmpty()) {
            return 0;  // 아직 아무 주문도 없는 경우
        }

        int currentTime = 0;

        // 1. 픽업 시간 계산
        for (int i = 0; i < shopIndexList.size(); i++) {
            int currShopIndex = shopIndexList.get(i);

            // 이전 지점에서 현재 가게까지의 이동 시간
            if (i > 0) {
                int prevShopIndex = shopIndexList.get(i - 1);
                currentTime += getShopDuration(prevShopIndex, currShopIndex);
            }

            // 주문 준비 시간 대기
            Order order = solution.orderMap().get(currShopIndex);
            currentTime = Math.max(currentTime, order.readyTime());

            // 픽업 서비스 시간
            currentTime += serviceTime;
        }

        // 2. 마지막 가게에서 첫 배달 지점까지 이동
        if (!shopIndexList.isEmpty() && !deliveryIndexList.isEmpty()) {
            int lastShopIndex = shopIndexList.getLast();
            int firstDeliveryIndex = deliveryIndexList.getFirst();
            currentTime += getShopToDeliveryDuration(lastShopIndex, firstDeliveryIndex);
        }

        // 3. 배달 시간 계산
        for (int i = 0; i < deliveryIndexList.size(); i++) {
            int currDeliveryIndex = deliveryIndexList.get(i);

            // 이전 배달 지점에서 현재 배달 지점까지의 이동 시간
            if (i > 0) {
                int prevDeliveryIndex = deliveryIndexList.get(i - 1);
                currentTime += getDeliveryDuration(prevDeliveryIndex, currDeliveryIndex);
            }

            // 배달 서비스 시간
            currentTime += serviceTime;
        }

        return currentTime;
    }

    /**
     * 현재 라이더가 적재하고 있는 총 물량을 계산
     * @return 현재 적재량
     */
    public int getCurrentLoad() {
        int currentLoad = 0;

        // 픽업은 했지만 아직 배달하지 않은 주문들의 물량 합계
        for (int shopIndex : shopIndexList) {
            Order order = solution.orderMap().get(shopIndex);
            currentLoad += order.volume();
        }

        for (int deliveryIndex : deliveryIndexList) {
            Order order = solution.orderMap().get(deliveryIndex);
            currentLoad -= order.volume();  // 배달 완료된 주문은 제외
        }

        return currentLoad;
    }

    /**
     * 라이더의 최대 적재 용량 반환
     * @return 최대 적재 용량
     */
    public int getCapacity() {
        return this.capa;  // 이미 존재하는 capa 필드 사용
    }

    /**
     * Rider 객체의 깊은 복사본을 생성
     * @return 새로운 Rider 객체
     */
    public Rider copy() {
        // 기본 정보로 새 Rider 객체 생성
        Rider newRider = new Rider(
            this.id,
            this.type,
            this.speed,
            this.capa,
            this.varCost,
            this.fixedCost,
            this.serviceTime
        );

        // orderList 복사
        List<Order> newOrderList = new ArrayList<>();
        for (Order order : this.orderList) {
            Order copiedOrder = order.copy();
            copiedOrder.setRider(newRider);  // 새로운 라이더 참조 설정
            newOrderList.add(copiedOrder);
        }
        newRider.setOrderList(newOrderList);

        // 인덱스 리스트 복사
        newRider.setShopIndexList(new ArrayList<>(this.shopIndexList));
        newRider.setDeliveryIndexList(new ArrayList<>(this.deliveryIndexList));

        // 기타 상태 복사
        newRider.setCost(this.cost);
        newRider.distance = this.distance;
        newRider.isValid = this.isValid;

        // Solution 참조 복사 (필요한 경우)
        if (this.solution != null) {
            newRider.setSolution(this.solution);  // 솔루션은 공유해도 됨
        }

        return newRider;
    }

    /**
     * 동등성 비교를 위한 equals 메서드 오버라이드
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rider rider = (Rider) o;
        return id == rider.id &&
            Double.compare(rider.speed, speed) == 0 &&
            capa == rider.capa &&
            varCost == rider.varCost &&
            fixedCost == rider.fixedCost &&
            serviceTime == rider.serviceTime &&
            type.equals(rider.type);
    }

    /**
     * equals와 함께 구현되어야 하는 hashCode 메서드 오버라이드
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + type.hashCode();
        long temp = Double.doubleToLongBits(speed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + capa;
        result = 31 * result + varCost;
        result = 31 * result + fixedCost;
        result = 31 * result + serviceTime;
        return result;
    }
}
