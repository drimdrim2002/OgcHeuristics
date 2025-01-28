package dev.brown.alns.repair;

public enum RepairMethod {
    GREEDY,              // 탐욕적 삽입
    REGRET_2,           // 2-후회 삽입
    REGRET_3,           // 3-후회 삽입
    BEST_POSITION,      // 최적 위치 삽입
    RANDOM_REPAIR       // 무작위 삽입
}
