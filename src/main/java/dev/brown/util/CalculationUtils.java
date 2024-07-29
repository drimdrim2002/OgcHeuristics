package dev.brown.util;

import java.util.List;

public class CalculationUtils {


    public static String getKeyFromList(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (Integer o : list) {
            sb.append(o).append(",");
        }

        return !sb.isEmpty() ? sb.substring(0, sb.length() - 1) : "";
    }
}
