package dev.brown.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.List;

public class OutputMaker {

    public static JsonObject convertSolutionToBundles(Solution solution) {
        JsonObject output = new JsonObject();
        for (Rider rider : solution.riderMap().values()) {
            if (!rider.shopIndexList().isEmpty()) {
                String id = String.valueOf(rider.id());
                output.add(id, new JsonObject());

                JsonObject rowObject = output.get(id).getAsJsonObject();
                rowObject.addProperty("type", rider.type());

                List<Integer> shopIndexList = rider.shopIndexList();
                rowObject.add("shopSeq", new JsonArray());
                for (Integer shopIndex : shopIndexList) {
                    rowObject.get("shopSeq").getAsJsonArray().add(shopIndex);
                }

                List<Integer> deliveryIndexList = rider.deliveryIndexList();
                rowObject.add("deliverySeq", new JsonArray());
                for (Integer deliveryIndex : deliveryIndexList) {
                    rowObject.get("deliverySeq").getAsJsonArray().add(deliveryIndex);
                }
            }
        }
        return output;
    }
}
