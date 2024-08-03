package dev.brown.util;

import java.util.ArrayList;
import java.util.List;

public class Permutation {

    public static List<List<Integer>> generatePermutations(List<Integer> inputList) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(inputList, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<Integer> inputList, List<Integer> currentPermutation, List<List<Integer>> result) {
        if (currentPermutation.size() == inputList.size()) {
            result.add(new ArrayList<>(currentPermutation));
            return;
        }

        for (Integer element : inputList) {
            if (!currentPermutation.contains(element)) {
                currentPermutation.add(element);
                backtrack(inputList, currentPermutation, result);
                currentPermutation.remove(currentPermutation.size() - 1);
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<Integer> input = new ArrayList<>();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(4);

        List<List<Integer>> permutations = generatePermutations(input);

        for (List<Integer> perm : permutations) {
            System.out.println(perm);
        }
    }
}
