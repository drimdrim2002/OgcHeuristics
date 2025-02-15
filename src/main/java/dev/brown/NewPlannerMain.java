package dev.brown;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.improved.alns.domain.Solution;
import dev.brown.util.InputMaker;
import dev.brown.util.JsonFileReader;
import dev.brown.util.Validator;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewPlannerMain {


    /**
     * 알고리즘을 실행하고 결과를 검증합니다.
     */
    public static void runAlgorithm(String problemFile, int timeLimit) {
        try {

            JsonObject inputObject = JsonFileReader.readJsonFile(problemFile);
            if (inputObject == null) {
                throw new FileNotFoundException("There is no file: " + problemFile);
            }


            HashMap<Integer, Order> orderMapByInput = InputMaker.getOrderMapByInput(inputObject);
            List<Order> orderList = new ArrayList<>(orderMapByInput.values());

            HashMap<Integer, Rider> riderMapByInput = InputMaker.getRiderMapByInput(inputObject);
            List<Rider> riderList = new ArrayList<>(riderMapByInput.values());

            JsonArray distanceArray = inputObject.get("DIST").getAsJsonArray();
            int distSize = distanceArray.size();
            int[][] distArray = new int[distSize][distSize];
            for (int fromShopIndex = 0; fromShopIndex < distSize; fromShopIndex++) {
                for (int toShopIndex = 0; toShopIndex < distSize; toShopIndex++) {
                    int dist = distanceArray.get(fromShopIndex).getAsJsonArray().get(toShopIndex).getAsInt();
                    distArray[fromShopIndex][toShopIndex] = dist;
                }
            }

            int K = orderList.size();


            // 알고리즘 실행
            MyAlgorithm algorithm = new MyAlgorithm();
            Solution solution = algorithm.algorithm(K, orderList, riderList, distArray, timeLimit, null);

            HashMap<String, Rider> riderMap = new HashMap<>();
            for (Rider rider : riderList) {
                riderMap.put(rider.getType(), rider);
            }

            Validator.validateAll(solution, orderMapByInput, riderMap);



        } catch (Exception e) {
            throw new RuntimeException("Error running algorithm: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try {
            String problemFile = "src/test/resources/input/TEST_K50_1.json";
            int timeLimit = 60;

            runAlgorithm(problemFile, timeLimit);
        } catch (Exception e) {
            throw new RuntimeException("test_error465" + e.getMessage(), e);
        }
    }


}

