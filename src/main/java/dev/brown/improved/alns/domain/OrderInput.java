package dev.brown.improved.alns.domain;

public class OrderInput {

    int orderId;
    int orderTime;
    double shopLat;
    double shopLon;
    double dlvryLat;
    double dlvryLon;
    int cookTime;
    int volume;
    int deadline;
    int readyTime;

    public OrderInput(int orderId, int orderTime, double shopLat, double shopLon, double dlvryLat, double dlvryLon,
        int cookTime, int volume, int deadline) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.shopLat = shopLat;
        this.shopLon = shopLon;
        this.dlvryLat = dlvryLat;
        this.dlvryLon = dlvryLon;
        this.cookTime = cookTime;
        this.volume = volume;
        this.deadline = deadline;
        this.readyTime = this.orderTime + this.cookTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getDeadline() {
        return deadline;
    }

    public double getDlvryLat() {
        return dlvryLat;
    }

    public double getDlvryLon() {
        return dlvryLon;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getOrderTime() {
        return orderTime;
    }

    public int getReadyTime() {
        return readyTime;
    }

    public double getShopLat() {
        return shopLat;
    }

    public double getShopLon() {
        return shopLon;
    }

    public int getVolume() {
        return volume;
    }
}
