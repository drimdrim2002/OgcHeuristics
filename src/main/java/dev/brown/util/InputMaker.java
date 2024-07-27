package dev.brown.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.HashMap;

public class InputMaker {

    public static Solution makeSolutionClassByInput(String decodesStr) {

        Solution solution = new Solution();

        JsonObject inputObject = JsonParser.parseString(decodesStr).getAsJsonObject();
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
        int riderIndex = 0;
        HashMap<Integer, Rider> riderMap = new HashMap<>();
        for (String riderType : riderInput.keySet()) {
            JsonObject riderObject = riderInput.get(riderType).getAsJsonObject();
            int speed = riderObject.get("speed").getAsInt();
            int capacity = riderObject.get("capa").getAsInt();
            int varCost = riderObject.get("var_cost").getAsInt();
            int fixedCost = riderObject.get("fixed_cost").getAsInt();
            int serviceTime = riderObject.get("service_time").getAsInt();

            int availableNumber = riderObject.get("available_number").getAsInt();
            for (int riderCount = 0; riderCount < availableNumber; riderCount++) {
                Rider rider = new Rider(riderIndex, riderType, speed, capacity, varCost, fixedCost, serviceTime);
                riderMap.put(riderIndex, rider);
                riderIndex += 1;
            }
        }
        solution.setRiderMap(riderMap);

        JsonObject distShopInput = JsonParser.parseString(inputObject.get("dist_shops").getAsString())
            .getAsJsonObject();
        MatrixManager.applyShopDistanceMap(distShopInput);


        JsonObject distShopToDlveryInput = JsonParser.parseString(inputObject.get("dist_shops_to_dlvrys").getAsString())
            .getAsJsonObject();
        MatrixManager.applyShopToDeliveryDistanceMap(distShopToDlveryInput, orderMap.size());

        JsonObject distDlvrysInput = JsonParser.parseString(inputObject.get("dist_dlvrys").getAsString())
            .getAsJsonObject();
        MatrixManager.applyDeliveryDistanceMap(distDlvrysInput);

        return solution;
    }


}
