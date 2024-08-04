package dev.brown.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class RiderPool {


    private final HashMap<String, TreeMap<Integer, Rider>> riderMapByType;
    private final HashSet<Integer> consumedRecord;

    public RiderPool(HashMap<Integer, Rider> riderMap) {

        riderMapByType = new HashMap<>();
        for (Rider rider : riderMap.values()) {
            riderMapByType.putIfAbsent(rider.type(), new TreeMap<>());
            riderMapByType.get(rider.type()).put(rider.id(), rider);
        }
        consumedRecord = new HashSet<>();


    }

    public HashSet<Integer> consumedRecord() {
        return consumedRecord;
    }

    public void consume(Rider rider) {
        String type = rider.type();
        int id = rider.id();

        riderMapByType.get(type).remove(id);

        if (riderMapByType.get(type).isEmpty()){
            riderMapByType.remove(type);
        }
    }

    public Rider getNextRider(String riderType) {
        if (riderMapByType.containsKey(riderType) && !riderMapByType.get(riderType).isEmpty()) {
            return riderMapByType.get(riderType).firstEntry().getValue();
        }
        return null;
    }
}
