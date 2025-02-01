package dev.brown.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.HashMap;

public class InputMaker {

//    private static final Logger log = LoggerFactory.getLogger(InputMaker.class);

    public static HashMap<Integer, Order> getOrderMapByInput(JsonObject inputObject) {

        HashMap<Integer, Order> orderMap = new HashMap<>();
        for (JsonElement orderElement : inputObject.get("ORDERS").getAsJsonArray()) {
            JsonArray orderRowElement = orderElement.getAsJsonArray();

            int orderId = orderRowElement.get(0).getAsInt();
            int orderTime = orderRowElement.get(1).getAsInt();
            double shopLat = orderRowElement.get(2).getAsDouble();
            double shopLon = orderRowElement.get(3).getAsDouble();
            double dlvryLat = orderRowElement.get(4).getAsDouble();
            double dlvryLon = orderRowElement.get(5).getAsDouble();
            int cookTime = orderRowElement.get(6).getAsInt();
            int volume = orderRowElement.get(7).getAsInt();
            int deadline = orderRowElement.get(8).getAsInt();
            int readyTime = orderTime + cookTime;
            orderMap.put(orderId,
                new Order(orderId, volume, readyTime, deadline, shopLat, shopLon, dlvryLat , dlvryLon));
        }
        return orderMap;
    }

    public static HashMap<Integer, Rider> getRiderMapByInput(JsonObject inputObject) {
        HashMap<Integer, Rider> riderMap = new HashMap<>();

        int riderIdx = 0;
        for (JsonElement riderElement : inputObject.get("RIDERS").getAsJsonArray()) {

            JsonArray riderArray = riderElement.getAsJsonArray();

            String type = riderArray.get(0).getAsString();
            double speed = riderArray.get(1).getAsDouble();
            int capa = riderArray.get(2).getAsInt();
            int varCost = riderArray.get(3).getAsInt();
            int fixedCost = riderArray.get(4).getAsInt();
            int serviceTime = riderArray.get(5).getAsInt();
            int availableNumber = riderArray.get(6).getAsInt();

            for (int vehicleIndex = 0; vehicleIndex < availableNumber; vehicleIndex++) {
                riderMap.put(
                    riderIdx,
                    new Rider(riderIdx, type, speed, capa, varCost, fixedCost, serviceTime));
                riderIdx += 1;
            }
        }

        return riderMap;
    }


    public static Solution makeSolutionClassByInput(JsonObject inputObject) {

        Solution solution = new Solution();
        solution.setOrderMap(InputMaker.getOrderMapByInput(inputObject));
        solution.setRiderMap(InputMaker.getRiderMapByInput(inputObject));

        HashMap<String, Rider> sampleRiderByType = new HashMap<>();
        for (Rider rider : solution.riderMap().values()) {
            rider.setSolution(solution);
            sampleRiderByType.put(rider.type(), rider);
        }
        solution.setSampleRiderMapByType(sampleRiderByType);
        return solution;
    }

    public static void setMatrixManager(JsonObject inputObject, Solution solution) {
        MatrixManager.applyDistanceMatrix(inputObject);
        MatrixManager.applyDuration(solution.sampleRiderMapByType());
        MatrixManager.applyNearestDistanceMap();
    }
}
