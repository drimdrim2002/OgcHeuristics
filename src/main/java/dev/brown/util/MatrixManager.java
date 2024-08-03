package dev.brown.util;

import com.google.gson.JsonObject;
import dev.brown.Constants.DISTANCE_MATRIX_TYPE;
import java.util.HashMap;

public class MatrixManager {

    public final static HashMap<Integer, HashMap<Integer, Integer>> shopDistanceMap = new HashMap<>();
    public final static HashMap<Integer, HashMap<Integer, Integer>> deliveryDistanceMap = new HashMap<>();
    public final static HashMap<Integer, HashMap<Integer, Integer>> shopToDeliveryDistanceMap = new HashMap<>();
//    private static final Logger log = LoggerFactory.getLogger(MatrixManager.class);

    private static HashMap<Integer, HashMap<Integer, Integer>> getDistanceMapByType(
        DISTANCE_MATRIX_TYPE distanceMatrixType) {
        return switch (distanceMatrixType) {
            case BETWEEN_SHOP -> shopDistanceMap;
            case BETWEEN_DELIVERY -> deliveryDistanceMap;
            case SHOP_TO_DELIVERY -> shopToDeliveryDistanceMap;
        };
    }

//    private static void convertJsonToMap(JsonObject matrixJson, DISTANCE_MATRIX_TYPE distanceMatrixType) {
//
//        HashMap<Integer, HashMap<Integer, Integer>> distanceMapByType = getDistanceMapByType(distanceMatrixType);
//
//        for (String originIndexStr : matrixJson.keySet()) {
//            int originXIndex = Integer.parseInt(originIndexStr);
//            distanceMapByType.putIfAbsent(originIndex, new HashMap<>());
//            JsonObject rowObject = matrixJson.get(originIndexStr).getAsJsonObject();
//            for (String destinationIndexStr : rowObject.keySet()) {
//                String distanceStr = rowObject.get(destinationIndexStr).getAsString();
//                int destinationIndex = Integer.parseInt(destinationIndexStr);
//                int distance = Integer.parseInt(distanceStr);
//                distanceMapByType.get(originIndex).put(destinationIndex, distance);
//            }
//        }
//    }

    public static void applyShopDistanceMap(JsonObject jsonObject) {
        for (String originIndexStr : jsonObject.keySet()) {
            int originIndex = Integer.parseInt(originIndexStr);
            shopDistanceMap.put(originIndex, new HashMap<>());
            for (String destinationIndexStr : jsonObject.get(originIndexStr).getAsJsonObject().keySet()) {
                String distanceStr = jsonObject.get(originIndexStr).getAsJsonObject().get(destinationIndexStr)
                    .getAsString();
                int distance = Integer.parseInt(distanceStr);
                int destinationIndex = Integer.parseInt(destinationIndexStr);
                shopDistanceMap.get(originIndex).put(destinationIndex, distance);
            }
        }
    }

    public static void applyDeliveryDistanceMap(JsonObject jsonObject, int orderCount) {
        for (String originIndexStr : jsonObject.keySet()) {
            int originIndex = Integer.parseInt(originIndexStr) - orderCount;
            deliveryDistanceMap.put(originIndex, new HashMap<>());
            for (String destinationIndexStr : jsonObject.get(originIndexStr).getAsJsonObject().keySet()) {
                String distanceStr = jsonObject.get(originIndexStr).getAsJsonObject().get(destinationIndexStr)
                    .getAsString();
                int distance = Integer.parseInt(distanceStr);
                int destinationIndex = Integer.parseInt(destinationIndexStr) - orderCount;
                deliveryDistanceMap.get(originIndex).put(destinationIndex, distance);
            }
        }
    }

    public static void applyShopToDeliveryDistanceMap(JsonObject jsonObject, int orderCount) {
        for (String originIndexStr : jsonObject.keySet()) {
            int originIndex = Integer.parseInt(originIndexStr);
            shopToDeliveryDistanceMap.put(originIndex, new HashMap<>());
            for (String destinationIndexStr : jsonObject.get(originIndexStr).getAsJsonObject().keySet()) {
                String distanceStr = jsonObject.get(originIndexStr).getAsJsonObject().get(destinationIndexStr)
                    .getAsString();
                int distance = Integer.parseInt(distanceStr);
                int destinationIndex = Integer.parseInt(destinationIndexStr) - orderCount;
                shopToDeliveryDistanceMap.get(originIndex).put(destinationIndex, distance);
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

    public static void applyDeliveryDuration(String riderType, HashMap<Integer, HashMap<Integer, Integer>> durationMap) {
//        log.info("applyDeliveryDuration");
        deliveryDurationMap.put(riderType, durationMap);
    }

    public static void applyShopToDeliveryDuration(String riderType, HashMap<Integer, HashMap<Integer, Integer>> durationMap) {
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
}

