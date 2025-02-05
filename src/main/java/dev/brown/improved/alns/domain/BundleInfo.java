package dev.brown.improved.alns.domain;


import java.util.List;

/**
 * 번들 정보를 저장하는 보조 레코드
 */
public record BundleInfo(
    String riderType,
    List<Integer> source,
    List<Integer> dest
) {

    public BundleInfo {
        source = List.copyOf(source);
        dest = List.copyOf(dest);
    }
}