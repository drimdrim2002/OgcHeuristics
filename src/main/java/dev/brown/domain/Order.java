package dev.brown.domain;

import dev.brown.util.MatrixManager;
import java.util.TreeMap;

public class Order implements Comparable<Order> {

    private final int id;
    private final int orderTime;
    private final int cookTime;
    private final int volume;
    private final int deadline;
    private final int readyTime;

    private Rider rider;

    public Order(int id, int volume, int orderTime, int cookTime, int deadline) {
        this.cookTime = cookTime;
        this.deadline = deadline;
        this.id = id;
        this.orderTime = orderTime;
        this.volume = volume;
        this.readyTime = orderTime + cookTime;
    }

    public int cookTime() {
        return cookTime;
    }

    public int deadline() {
        return deadline;
    }

    public int id() {
        return id;
    }

    public int orderTime() {
        return orderTime;
    }

    public int readyTime() {
        return readyTime;
    }

    public int volume() {
        return volume;
    }

    @Override
    public String toString() {
        return "Order{" +
            "cookTime=" + cookTime +
            ", id=" + id +
            ", orderTime=" + orderTime +
            ", volume=" + volume +
            ", deadline=" + deadline +
            ", readyTime=" + readyTime +
            '}';
    }

    public Rider rider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    private TreeMap<Integer, Rider> availableRiderSet = new TreeMap<>();

    public void addAvailableRider(Rider rider) {
        availableRiderSet.put(rider.priority(), rider);
    }

    @Override
    public int compareTo(Order otherOrder) {

        if (this.id == otherOrder.id) {
            return 0;
        }

        int thisPrioritySum = this.availableRiderSet.keySet().stream().reduce(0, Integer::sum);
        int otherPrioritySum = otherOrder.availableRiderSet.keySet().stream().reduce(0, Integer::sum);

        // 클수록(음수값이 작을수록) 중요하다.
        if (thisPrioritySum != otherPrioritySum) {
            return thisPrioritySum > otherPrioritySum ? 1 : -1;
        }

        Rider thisRider = this.availableRiderSet.firstEntry().getValue();
        Rider otherRider = otherOrder.availableRiderSet.firstEntry().getValue();

        Integer thisMinDuration = MatrixManager.getShopToDeliveryDuration(thisRider.type(), this.id, this.id);
        Integer otherMinDuration = MatrixManager.getShopToDeliveryDuration(thisRider.type(), otherRider.id(),
            otherRider.id());

        int thisExtraDuration = this.deadline - (thisMinDuration + this.readyTime);
        int otherExtraDuration = otherOrder.deadline - (otherMinDuration + otherOrder.readyTime);

        if (thisExtraDuration < 0 || otherExtraDuration < 0) {
            throw new UnsupportedOperationException("order comparing error");
        }

        if (thisExtraDuration != otherExtraDuration) {
            // 작을수록 우선 순위가 높다.
            return thisExtraDuration < otherExtraDuration ? 1 : -1;
        }

        return this.id < otherOrder.id ? 1 : -1;
    }
}
