package dev.brown;

import java.util.Base64;

public class PlannerMain {

    public static void main(String[] args) {

        String encodedStr = args[0];
        String decodedStr = new String(Base64.getDecoder().decode(encodedStr.getBytes()));

        StringBuilder stringBuilder = new StringBuilder();

        for (String arg : args) {
            stringBuilder.append(arg);
        }

        System.out.println(stringBuilder);
    }
}