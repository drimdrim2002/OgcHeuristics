package dev.brown.domain;

public class Order {

    private final int id;
    private final int volume;
    private final int deadline;
    private final int readyTime;
    private final double shopLat;
    private final double shopLon;
    private final double deliveryLat;
    private final double deliveryLon;


    private Rider rider;

    public Order(int id, int volume, int readyTime, int deadline,
        double shopLat, double shopLon, double deliveryLat, double deliveryLon) {
        this.id = id;
        this.volume = volume;
        this.readyTime = readyTime;
        this.deadline = deadline;
        this.shopLat = shopLat;
        this.shopLon = shopLon;
        this.deliveryLat = deliveryLat;
        this.deliveryLon = deliveryLon;

    }

    public int deadline() {
        return deadline;
    }

    public int id() {
        return id;
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
        return deliveryLat;
    }

    public double dlvryLon() {
        return deliveryLon;
    }

    public double shopLat() {
        return shopLat;
    }

    public double shopLon() {
        return shopLon;
    }

    /**
     * Order 객체의 깊은 복사본을 생성
     *
     * @return 새로운 Order 객체
     */
    public Order copy() {
        // 새로운 Order 객체 생성
        Order newOrder = new Order(
            this.id,
            this.volume,
            this.readyTime,
            this.deadline,
            this.shopLat,
            this.shopLon,
            this.deliveryLat,
            this.deliveryLon
        );

        newOrder.rider = null;

        return newOrder;
    }

    /**
     * 동등성 비교를 위한 equals 메서드 오버라이드
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Order order = (Order) o;
        return id == order.id &&
            readyTime == order.readyTime &&
            volume == order.volume &&
            deadline == order.deadline &&
            Double.compare(order.shopLat, shopLat) == 0 &&
            Double.compare(order.shopLon, shopLon) == 0 &&
            Double.compare(order.deliveryLat, deliveryLat) == 0 &&
            Double.compare(order.deliveryLon, deliveryLon) == 0;
    }

    /**
     * equals와 함께 구현되어야 하는 hashCode 메서드 오버라이드
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + volume;
        result = 31 * result + deadline;
        result = 31 * result + readyTime;
        result = 31 * result + Double.hashCode(shopLat);
        result = 31 * result + Double.hashCode(shopLon);
        result = 31 * result + Double.hashCode(deliveryLat);
        result = 31 * result + Double.hashCode(deliveryLon);
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
            "id=" + id +
            ", deadline=" + deadline +
            ", readyTime=" + readyTime +
            ", volume=" + volume +
            ", shopLat=" + shopLat +
            ", shopLon=" + shopLon +
            ", dlvryLat=" + deliveryLat +
            ", dlvryLon=" + deliveryLon +
            ", rider=" + (rider != null ? rider.id() : "null") +
            '}';
    }
}
