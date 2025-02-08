package dev.brown.domain;

public class Order {
    private final int id;
    private final int volume;
    private final int readyTime;
    private final int deadline;
    private final double shopLat;
    private final double shopLon;
    private final double deliveryLat;
    private final double deliveryLon;
    private Integer riderId;  // null 가능

    public Order(int id, int volume, int readyTime, int deadline,
        double shopLat, double shopLon,
        double deliveryLat, double deliveryLon) {
        this.id = id;
        this.volume = volume;
        this.readyTime = readyTime;
        this.deadline = deadline;
        this.shopLat = shopLat;
        this.shopLon = shopLon;
        this.deliveryLat = deliveryLat;
        this.deliveryLon = deliveryLon;
        this.riderId = null;  // 초기에는 할당되지 않음
    }

    // Getters
    public int getId() { return id; }
    public int getVolume() { return volume; }
    public int getReadyTime() { return readyTime; }
    public int getDeadline() { return deadline; }
    public double getShopLat() { return shopLat; }
    public double getShopLon() { return shopLon; }
    public double getDeliveryLat() { return deliveryLat; }
    public double getDeliveryLon() { return deliveryLon; }

    public Integer getRiderId() { return riderId; }
    public void setRiderId(Integer riderId) { this.riderId = riderId; }

    public boolean hasRider() { return riderId != null; }

    public Order copy() {
        Order copied = new Order(id, volume, readyTime, deadline, shopLat, shopLon, deliveryLat, deliveryLon);
        copied.setRiderId(riderId);
        return copied;
    }


}