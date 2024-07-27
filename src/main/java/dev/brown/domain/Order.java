package dev.brown.domain;

public class Order {

    private final int id;
    private final int orderTime;
    private final int cookTime;
    private final int volume;
    private final int deadline;
    private final int readyTime;

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
}
