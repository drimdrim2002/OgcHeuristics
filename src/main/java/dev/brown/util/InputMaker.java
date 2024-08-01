package dev.brown.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InputMaker {

    public static Solution makeSolutionClassByInput(JsonObject inputObject) {

        Solution solution = new Solution();
        JsonObject orderInput = JsonParser.parseString(inputObject.get("orders").getAsString()).getAsJsonObject();

        HashMap<Integer, Order> orderMap = new HashMap<>();
        for (String orderIndex : orderInput.keySet()) {
            JsonObject orderObject = orderInput.get(orderIndex).getAsJsonObject();

            int id = Integer.parseInt(orderIndex);
            int volume = orderObject.get("volume").getAsInt();
            int orderTime = orderObject.get("order_time").getAsInt();
            int cookTime = orderObject.get("cook_time").getAsInt();
            int deadline = orderObject.get("deadline").getAsInt();
            orderMap.put(id, new Order(id, volume, orderTime, cookTime, deadline));
        }
        solution.setOrderMap(orderMap);

        JsonObject riderInput = JsonParser.parseString(inputObject.get("riders").getAsString()).getAsJsonObject();
        JsonObject carRiderObject = riderInput.get("CAR").getAsJsonObject();
        JsonObject bikeRiderObject = riderInput.get("BIKE").getAsJsonObject();
        JsonObject walkRiderObject = riderInput.get("WALK").getAsJsonObject();
        int riderIndex = 0;
        List<Rider> riderList = getRiderListByObject(carRiderObject, riderIndex);
        riderList.addAll(getRiderListByObject(bikeRiderObject, riderList.size()));
        riderList.addAll(getRiderListByObject(walkRiderObject, riderList.size()));

        HashMap<Integer, Rider> riderMap = new HashMap<>();
        for (Rider rider : riderList) {
            rider.setSolution(solution);
            riderMap.put(rider.id(), rider);
        }
        solution.setRiderMap(riderMap);
        return solution;
    }

    public static List<Rider> getRiderListByObject(JsonObject riderObject, int riderIndex) {
        List<Rider> riderList = new ArrayList<>();
        String riderType = riderObject.get("type").getAsString();
        double speed = riderObject.get("speed").getAsDouble();
        int capacity = riderObject.get("capa").getAsInt();
        int varCost = riderObject.get("var_cost").getAsInt();
        int fixedCost = riderObject.get("fixed_cost").getAsInt();
        int serviceTime = riderObject.get("service_time").getAsInt();

        int availableNumber = riderObject.get("available_number").getAsInt();
        for (int riderCount = 0; riderCount < availableNumber; riderCount++) {
            Rider rider = new Rider(riderIndex, riderType, speed, capacity, varCost, fixedCost, serviceTime);
            riderList.add(rider);
            riderIndex += 1;
        }
        return riderList;
    }

    public static void setMatrixManager(JsonObject inputObject) {
        JsonObject distShopInput = JsonParser.parseString(inputObject.get("dist_shops").getAsString())
            .getAsJsonObject();
        MatrixManager.applyShopDistanceMap(distShopInput);

        JsonObject distShopToDlveryInput = JsonParser.parseString(inputObject.get("dist_shops_to_dlvrys").getAsString())
            .getAsJsonObject();
        MatrixManager.applyShopToDeliveryDistanceMap(distShopToDlveryInput, distShopInput.size());

        JsonObject distDlvrysInput = JsonParser.parseString(inputObject.get("dist_dlvrys").getAsString())
            .getAsJsonObject();
        MatrixManager.applyDeliveryDistanceMap(distDlvrysInput, distDlvrysInput.size());
    }


}
