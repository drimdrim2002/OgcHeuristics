package dev.brown.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.brown.Constants.DISTANCE_MATRIX_TYPE;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class MatrixManager {

    public final static HashMap<Integer, HashMap<Integer, Integer>> shopDistanceMap = new HashMap<>();
    public final static HashMap<Integer, HashMap<Integer, Integer>> deliveryDistanceMap = new HashMap<>();
    public final static HashMap<Integer, HashMap<Integer, Integer>> shopToDeliveryDistanceMap = new HashMap<>();
//    private static final Logger log = LoggerFactory.getLogger(MatrixManager.class);


    public static void applyDistanceMatrix(JsonObject jsonObject) {

        JsonArray distanceArray = jsonObject.get("DIST").getAsJsonArray();
        int k = distanceArray.size() / 2;

        // fromShopIndex 0, toShopIndex 1, K 100
        for (int fromShopIndex = 0; fromShopIndex < k; fromShopIndex++) {
            for (int toShopIndex = 0; toShopIndex < k; toShopIndex++) {

                int fromDeliveryIndex = fromShopIndex + k; // 100
                int toDeliveryIndex = toShopIndex + k; // 101

                // 0 -> 1
                int shopToShopDistance
                    = distanceArray.get(fromShopIndex).getAsJsonArray().get(toShopIndex).getAsInt();

                // 0 -> 101
                int shopToDeliveryDistance
                    = distanceArray.get(fromShopIndex).getAsJsonArray().get(toDeliveryIndex).getAsInt();

                // 100 -> 101
                int deliveryToDeliveryDistance
                    = distanceArray.get(fromDeliveryIndex).getAsJsonArray().get(toDeliveryIndex).getAsInt();

                shopDistanceMap.putIfAbsent(fromShopIndex, new HashMap<>());
                shopDistanceMap.get(fromShopIndex).put(toShopIndex, shopToShopDistance);

                deliveryDistanceMap.putIfAbsent(fromShopIndex, new HashMap<>());
                deliveryDistanceMap.get(fromShopIndex).put(toShopIndex, shopToDeliveryDistance);

                shopToDeliveryDistanceMap.putIfAbsent(fromShopIndex, new HashMap<>());
                shopToDeliveryDistanceMap.get(fromShopIndex).put(toShopIndex, deliveryToDeliveryDistance);
            }
        }

    }

    public static int getShopDistance(int originIndex, int destinationIndex) {
        return shopDistanceMap.get(originIndex).get(destinationIndex);
    }

    public static int getShopToDeliveryDistance(int originIndex, int destinationIndex) {
        return shopToDeliveryDistanceMap.get(originIndex).get(destinationIndex);
    }

    public static int getDeliveryDistance(int originIndex, int destinationIndex) {
        return deliveryDistanceMap.get(originIndex).get(destinationIndex);
    }

    private static final HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> shopDurationMap = new HashMap<>();
    private static final HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> deliveryDurationMap = new HashMap<>();
    private static final HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> shopToDeliveryDurationMap = new HashMap<>();

    public static void applyShopDuration(String riderType, HashMap<Integer, HashMap<Integer, Integer>> durationMap) {
//        log.info("applyShopDuration");
        shopDurationMap.put(riderType, durationMap);
    }

    public static void applyDeliveryDuration(String riderType,
        HashMap<Integer, HashMap<Integer, Integer>> durationMap) {
//        log.info("applyDeliveryDuration");
        deliveryDurationMap.put(riderType, durationMap);
    }

    public static void applyShopToDeliveryDuration(String riderType,
        HashMap<Integer, HashMap<Integer, Integer>> durationMap) {
//        log.info("applyShopToDeliveryDuration");

        shopToDeliveryDurationMap.put(riderType, durationMap);
    }

    public static Integer getShopDuration(String riderType, Integer originIndex, Integer destinationIndex) {
        return shopDurationMap.get(riderType).get(originIndex).get(destinationIndex);
    }

    public static Integer getDeliveryDuration(String riderType, Integer originIndex, Integer destinationIndex) {
        return deliveryDurationMap.get(riderType).get(originIndex).get(destinationIndex);
    }

    public static Integer getShopToDeliveryDuration(String riderType, Integer originIndex, Integer destinationIndex) {
        return shopToDeliveryDurationMap.get(riderType).get(originIndex).get(destinationIndex);
    }

    public static final HashMap<Integer, TreeMap<Integer, List<Integer>>> nearestDistanceMap = new HashMap<>();

    public static void applyNearestDistanceMap() {
        for (Integer originIndex : shopDistanceMap.keySet()) {
            nearestDistanceMap.put(originIndex, new TreeMap<>());
            for (Integer destinationIndex : shopDistanceMap.keySet()) {
                if (Objects.equals(originIndex, destinationIndex)) {
                    continue;
                }

                Integer shopDistance = shopDistanceMap.get(originIndex).get(destinationIndex);
                Integer deliveryDistance = deliveryDistanceMap.get(originIndex).get(destinationIndex);
                Integer shopToDeliveryDistance1 = shopToDeliveryDistanceMap.get(originIndex).get(destinationIndex);
                Integer shopToDeliveryDistance2 = shopToDeliveryDistanceMap.get(destinationIndex).get(originIndex);

                int totalDistance = shopDistance + deliveryDistance + shopToDeliveryDistance1 + shopToDeliveryDistance2;
                nearestDistanceMap.get(originIndex).putIfAbsent(totalDistance, new ArrayList<>());
                nearestDistanceMap.get(originIndex).get(totalDistance).add(destinationIndex);

            }
        }
    }


    static void applyDuration(HashMap<String, Rider> sampleRiderMap) {

        for (String riderType : sampleRiderMap.keySet()) {
            shopDurationMap.putIfAbsent(riderType, new HashMap<>());
            shopToDeliveryDurationMap.putIfAbsent(riderType, new HashMap<>());
            deliveryDurationMap.putIfAbsent(riderType, new HashMap<>());

            Rider sampleRider = sampleRiderMap.get(riderType);
            double speed = sampleRider.speed();
            int serviceTime = sampleRider.serviceTime();

            for (Integer fromIndex : shopDistanceMap.keySet()) {
                for (Integer toIndex : shopDistanceMap.keySet()) {
                    Integer distance = shopDistanceMap.get(fromIndex).get(toIndex);
                    long duration = Math.round(distance / speed) + serviceTime;
                    shopDurationMap.get(riderType).putIfAbsent(fromIndex, new HashMap<>());
                    shopDurationMap.get(riderType).get(fromIndex).put(toIndex, (int) duration);
                }
            }

            for (Integer fromIndex : shopToDeliveryDistanceMap.keySet()) {
                for (Integer toIndex : shopToDeliveryDistanceMap.keySet()) {
                    Integer distance = shopToDeliveryDistanceMap.get(fromIndex).get(toIndex);
                    long duration = Math.round(distance / speed) + serviceTime;
                    shopToDeliveryDurationMap.get(riderType).putIfAbsent(fromIndex, new HashMap<>());
                    shopToDeliveryDurationMap.get(riderType).get(fromIndex).put(toIndex, (int) duration);
                }
            }

            for (Integer fromIndex : deliveryDistanceMap.keySet()) {
                for (Integer toIndex : deliveryDistanceMap.keySet()) {
                    Integer distance = deliveryDistanceMap.get(fromIndex).get(toIndex);
                    long duration = Math.round(distance / speed) + serviceTime;
                    deliveryDurationMap.get(riderType).putIfAbsent(fromIndex, new HashMap<>());
                    deliveryDurationMap.get(riderType).get(fromIndex).put(toIndex, (int) duration);
                }
            }
        }
    }
}

