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

    /**
     * Order 객체의 깊은 복사본을 생성
     * @return 새로운 Order 객체
     */
    public Order copy() {
        // 새로운 Order 객체 생성
        Order newOrder = new Order(
            this.id,
            this.volume,
            this.orderTime,
            this.cookTime,
            this.deadline,
            this.shopLat,
            this.shopLon,
            this.dlvryLat,
            this.dlvryLon
        );

        // rider 참조 복사 (필요한 경우 rider도 깊은 복사)
        if (this.rider != null) {
            newOrder.setRider(this.rider.copy());
        }

        return newOrder;
    }

    /**
     * 동등성 비교를 위한 equals 메서드 오버라이드
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;
        return id == order.id &&
            orderTime == order.orderTime &&
            cookTime == order.cookTime &&
            volume == order.volume &&
            deadline == order.deadline &&
            readyTime == order.readyTime &&
            Double.compare(order.shopLat, shopLat) == 0 &&
            Double.compare(order.shopLon, shopLon) == 0 &&
            Double.compare(order.dlvryLat, dlvryLat) == 0 &&
            Double.compare(order.dlvryLon, dlvryLon) == 0;
    }

    /**
     * equals와 함께 구현되어야 하는 hashCode 메서드 오버라이드
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + orderTime;
        result = 31 * result + cookTime;
        result = 31 * result + volume;
        result = 31 * result + deadline;
        result = 31 * result + readyTime;
        long temp;
        temp = Double.doubleToLongBits(shopLat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(shopLon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dlvryLat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dlvryLon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
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
            ", shopLat=" + shopLat +
            ", shopLon=" + shopLon +
            ", dlvryLat=" + dlvryLat +
            ", dlvryLon=" + dlvryLon +
            ", rider=" + (rider != null ? rider.id() : "null") +
            '}';
    }
}
