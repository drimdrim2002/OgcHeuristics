package dev.brown.util;

import java.util.Base64;

public class ParameterDecoder {

    public static String decode(String dataFromPython) {

        return   new String(Base64.getDecoder().decode(dataFromPython.getBytes()));

    }

}
