package dev.brown.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.IOException;

public class JsonFileReader {
    public static JsonObject readJsonFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            // JsonParser.parseReader()를 사용하여 JSON 파일을 파싱
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }
}