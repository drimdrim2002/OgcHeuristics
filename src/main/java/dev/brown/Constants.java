package dev.brown;

import java.util.HashMap;

public class Constants {

    public enum DISTANCE_MATRIX_TYPE {
        BETWEEN_SHOP, BETWEEN_DELIVERY, SHOP_TO_DELIVERY
    }

    public static final HashMap<String, String> bestShopIndexMap = new HashMap<>();
    public static final HashMap<String, String> bestDeliveryIndexMap = new HashMap<>();
    public static final HashMap<String, Integer> bestScore = new HashMap<>();

    public enum RIDER_TYPE {
        CAR("CAR"), BIKE("BIKE"), WALK("WALK");

        private final String value;
        RIDER_TYPE(String param) {
            this.value = param;
        }

        public String value() {
            return value;
        }
    }

    public enum ORDER_CRITERIA {
        DEADLINE, CAPACITY
    }
}
