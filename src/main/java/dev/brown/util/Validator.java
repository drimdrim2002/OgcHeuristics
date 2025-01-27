package dev.brown.util;

import dev.brown.domain.CalculationResult;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    public static void validateAll(Solution solution) throws Exception {

        int cost = 0;
        HashSet<Integer> allocatedOrderIdSet = new HashSet<>();
        for (Rider rider : solution.riderMap().values()) {

            CalculationResult calculate = rider.calculate();
            if (calculate != null) {
                if (!calculate.isFeasible()) {
                    throw new Exception(String.format("Rider (%s) is not valid", rider.id()));
                } else {
                    cost += rider.cost();
                    allocatedOrderIdSet.addAll(rider.shopIndexList());
                }
            }

        }

        int orderSize = solution.orderMap().size();
        if (orderSize != allocatedOrderIdSet.size()) {
            throw new Exception("Order Not Allocated");
        }
        log.info("Total Cost: {}, Average Cost: {}", cost, cost/orderSize);

    }



}
