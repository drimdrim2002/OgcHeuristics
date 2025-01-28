package dev.brown.alns.destroy;

// destroy_tools/methods.hpp
enum DestroyMethod {
    RANDOM,             // 무작위 제거
    DISTANCE_ORIENTED,  // 거리 기반 제거
    ROUTE,             // 경로 기반 제거
    SHAW,              // Shaw 제거
    WORST,             // 최악 비용 제거
    SEMI_WORST,        // 준-최악 제거
    HISTORICAL         // 히스토리 기반 제거
};