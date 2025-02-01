package dev.brown.alns.destroy;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class RouteRemoval implements DestroyOperator {
    private final Random random;
    private final HyperParameter hparam;

    public RouteRemoval(HyperParameter hparam, Random random) {
        this.hparam = hparam;
        this.random = random;
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        List<Integer> removedOrders = new ArrayList<>();

        // 1. 활성 라이더 목록 가져오기
        List<Rider> activeRiders = getActiveRiders(solution);
        if (activeRiders.isEmpty()) {
            return removedOrders;
        }

        // 2. 라이더 선택 (비용이 높은 라이더가 선택될 확률이 높도록)
        Rider selectedRider = selectRider(activeRiders);
        if (selectedRider == null) {
            return removedOrders;
        }

        // 3. 선택된 라이더의 경로에서 주문 제거
        List<Order> routeOrders = new ArrayList<>(selectedRider.orderList());
        int ordersToRemove = Math.min(
            numToDestroy,
            (int)(routeOrders.size() * hparam.getRouteRemovalRatio())
        );

        // 4. 연속된 구간 선택 및 제거
        if (!routeOrders.isEmpty()) {
            int start = random.nextInt(routeOrders.size());
            int actualOrdersToRemove = Math.min(ordersToRemove, routeOrders.size() - start);

            for (int i = 0; i < actualOrdersToRemove; i++) {
                Order orderToRemove = routeOrders.get(start + i);
                selectedRider.removeOrder(orderToRemove);
                removedOrders.add(orderToRemove.getId());
            }
        }

        // 5. 필요한 경우 추가 라이더의 경로에서도 제거
        while (removedOrders.size() < numToDestroy && !activeRiders.isEmpty()) {
            activeRiders.remove(selectedRider);
            if (activeRiders.isEmpty()) break;

            selectedRider = selectRider(activeRiders);
            if (selectedRider == null) break;

            routeOrders = new ArrayList<>(selectedRider.orderList());
            if (routeOrders.isEmpty()) continue;

            int remainingToRemove = numToDestroy - removedOrders.size();
            int start = random.nextInt(routeOrders.size());
            int actualOrdersToRemove = Math.min(
                remainingToRemove,
                Math.min(
                    (int)(routeOrders.size() * hparam.getRouteRemovalRatio()),
                    routeOrders.size() - start
                )
            );

            for (int i = 0; i < actualOrdersToRemove; i++) {
                Order orderToRemove = routeOrders.get(start + i);
                selectedRider.removeOrder(orderToRemove);
                removedOrders.add(orderToRemove.getId());
            }
        }

        return removedOrders;
    }

    private List<Rider> getActiveRiders(Solution solution) {
        List<Rider> activeRiders = new ArrayList<>();
        for (Rider rider : solution.riderMap().values()) {
            if (!rider.orderList().isEmpty()) {
                activeRiders.add(rider);
            }
        }
        return activeRiders;
    }

    private Rider selectRider(List<Rider> activeRiders) {
        if (activeRiders.isEmpty()) return null;

        // 비용 기반 선택을 위한 리스트 생성
        List<RiderScore> riderScores = new ArrayList<>();
        for (Rider rider : activeRiders) {
            double score = calculateRiderScore(rider);
            // 노이즈 추가
            double noise = 1.0 + hparam.getWorstNoise() * random.nextDouble();
            riderScores.add(new RiderScore(rider, score * noise));
        }

        // 점수가 높은 순으로 정렬
        riderScores.sort((a, b) -> Double.compare(b.score, a.score));

        // y^p 선택 방식 구현 (p = 2)
        int selectedIndex = (int) (Math.pow(random.nextDouble(), 2)
            * riderScores.size());
        return riderScores.get(selectedIndex).rider;
    }

    private double calculateRiderScore(Rider rider) {
        // 기본적으로 비용을 점수로 사용
        double score = rider.cost();

        // 추가 고려사항들
        // 1. 경로의 길이
        score *= Math.sqrt(rider.orderList().size());

        // 2. 시간 제약 여유도
        double timeSlack = calculateTimeSlack(rider);
        score /= (1 + timeSlack);  // 여유가 적을수록 높은 점수

        return score;
    }

    private double calculateTimeSlack(Rider rider) {
        double totalSlack = 0;
        for (Order order : rider.orderList()) {
            // 실제 배달 시간과 마감 시간의 차이
            int slack = order.getDeadline() - order.getReadyTime();
            totalSlack += slack;
        }
        return totalSlack / Math.max(1, rider.orderList().size());
    }

    private static class RiderScore {
        final Rider rider;
        final double score;

        RiderScore(Rider rider, double score) {
            this.rider = rider;
            this.score = score;
        }
    }
}