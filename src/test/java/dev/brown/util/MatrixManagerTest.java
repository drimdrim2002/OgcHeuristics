package dev.brown.util;

import static dev.brown.util.MatrixManager.deliveryDistanceMap;
import static dev.brown.util.MatrixManager.shopDistanceMap;
import static dev.brown.util.MatrixManager.shopToDeliveryDistanceMap;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MatrixManagerTest {
    private static final Logger log = LoggerFactory.getLogger(MatrixManagerTest.class);

    String inputFileName = "src/test/resources/input/TEST_K100_1.json";
    JsonObject jsonObject = JsonFileReader.readJsonFile(inputFileName);

    @Test
    void applyDistanceMatrix() {

        MatrixManager.applyDistanceMatrix(jsonObject);

        log.info("### shop distance");
        for (Integer shopDistance : shopDistanceMap.get(0).values()) {
            log.info(String.valueOf(shopDistance));
        }

        log.info("### shop to delivery distance");
        for (Integer shopDistance : shopToDeliveryDistanceMap.get(0).values()) {
            log.info(String.valueOf(shopDistance));
        }


        log.info("### delivery to delivery distance");
        for (Integer shopDistance : deliveryDistanceMap.get(0).values()) {
            log.info(String.valueOf(shopDistance));
        }

    }
}