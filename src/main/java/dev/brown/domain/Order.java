package dev.brown.domain;

public class Order  {

    private final int id;
    private final int orderTime;
    private final int cookTime;
    private final int volume;
    private final int deadline;
    private final int readyTime;
    private final double shopLat;
    private final double shopLon;
    private final double dlvryLat;
    private final double dlvryLon;


    private Rider rider;

    public Order(int id, int volume, int orderTime, int cookTime, int deadline,
        double shopLat, double shopLon, double dlvryLat, double dlvryLon) {
        this.cookTime = cookTime;
        this.deadline = deadline;
        this.id = id;
        this.orderTime = orderTime;
        this.volume = volume;
        this.readyTime = orderTime + cookTime;
        this.shopLat = shopLat;
        this.shopLon = shopLon;
        this.dlvryLat = dlvryLat;
        this.dlvryLon = dlvryLon;

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

    public Rider rider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public double dlvryLat() {
        return dlvryLat;
    }

    public double dlvryLon() {
        return dlvryLon;
    }

    public double shopLat() {
        return shopLat;
    }

    public double shopLon() {
        return shopLon;
    }

    @Override
    public String toString() {
        return "Order{" +
            "id=" + id +
            ", cookTime=" + cookTime +
            ", deadline=" + deadline +
            ", orderTime=" + orderTime +
            ", readyTime=" + readyTime +
            ", volume=" + volume +
            '}';
    }
}
