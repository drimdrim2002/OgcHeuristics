package dev.brown.improved.alns.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache {
    private final Map<List<Integer>, Res> storage;

    public Cache() {
        this.storage = new HashMap<>();
    }

    public boolean check(List<Integer> v) {
        List<Integer> key = new ArrayList<>(v);
        key.sort(null);
        return storage.containsKey(key);
    }

    public void append(List<Integer> v, Res r) {
        List<Integer> key = new ArrayList<>(v);
        key.sort(null);
        storage.put(key, r);
    }

    public Res retrieve(List<Integer> v) {
        List<Integer> key = new ArrayList<>(v);
        key.sort(null);
        return storage.get(key);
    }
}