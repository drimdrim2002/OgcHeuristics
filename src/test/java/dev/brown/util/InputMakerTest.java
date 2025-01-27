package dev.brown.util;

import com.google.gson.JsonObject;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InputMakerTest {

    private static final Logger log = LoggerFactory.getLogger(InputMakerTest.class);

    String inputFileName = "src/test/resources/input/TEST_K100_1.json";
    JsonObject jsonObject = JsonFileReader.readJsonFile(inputFileName);


    @Test
    void getOrderMapByInput() {




        Map<Integer, Order> orderMap = InputMaker.getOrderMapByInput(jsonObject);

        for (Order order : orderMap.values()) {
            log.info(order.toString());
        }

    }

    @Test
    void makerRiderListByInput() {

        Map<Integer,Rider> riderMap = InputMaker.getRiderMapByInput(jsonObject);

        for (Rider rider : riderMap.values()) {
            log.info(rider.toString());
        }


    }
}