package dev.brown.improved.alns.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache {
    private final Map<List<Integer>, Result> storage = new HashMap<>();

    public boolean check(List<Integer> key) {
        List<Integer> sortedKey = new ArrayList<>(key);
        Collections.sort(sortedKey);
        return storage.containsKey(sortedKey);
    }

    public void append(List<Integer> key, Result result) {
        List<Integer> sortedKey = new ArrayList<>(key);
        Collections.sort(sortedKey);
        storage.put(sortedKey, result);
    }

    public Result retrieve(List<Integer> key) {
        List<Integer> sortedKey = new ArrayList<>(key);
        Collections.sort(sortedKey);
        return storage.get(sortedKey);
    }
}