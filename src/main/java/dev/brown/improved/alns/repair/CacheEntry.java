package dev.brown.improved.alns.repair;

import java.util.List;

public class CacheEntry {
    final List<Integer> idList;
    final InvestigationResult result;

    CacheEntry(List<Integer> idList, InvestigationResult result) {
        this.idList = idList;
        this.result = result;
    }

}
