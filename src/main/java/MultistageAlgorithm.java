import models.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MultistageAlgorithm {

    private final static Integer SUPPORT =3;
    private static Integer ITEMS_COUNT;
    private final static String SOURCE_FILE = "D:\\Projects\\Data-Mining-HW5\\src\\main\\java\\itemlist.txt";
    private static final Map<String, Integer> ITEMS_WITH_COUNTS = new HashMap<>();
    private static final Map<String, Integer> ITEMS_WITH_ID = new HashMap<>();
    private static final Map<Integer, Integer> ITEMS_ID_AND_ITEMS_COUNT = new HashMap<>();
    private static final Map<Integer, Set<Pair>> BUCKETS_WITH_PAIRS = new HashMap<>();
    private static final Map<Integer, Set<Pair>> PAIRS_WITH_HASHES = new HashMap<>();
    private static final Map<Pair, Integer> PAIRS_WITH_COUNTS = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        readFromFile();
        ITEMS_COUNT = ITEMS_WITH_ID.size();
        createPairs();
        firstStageCounting();
        secondStageCounting();
        connectItemsIdAndItemsCount();
        deleteIfItemUnderSupport();
        printResult();
    }

    private static void readFromFile() throws FileNotFoundException {
        File fileWithItems = new File(MultistageAlgorithm.SOURCE_FILE);
        Scanner scanner = new Scanner(fileWithItems);

        while (scanner.hasNextLine()) {
            String itemBucket = scanner.nextLine();
            String[] itemsInBucket = itemBucket.split(" ");
            for (int i = 0; i < itemsInBucket.length; i++) {
                if (ITEMS_WITH_COUNTS.containsKey(itemsInBucket[i])) {
                    ITEMS_WITH_COUNTS.put(itemsInBucket[i], ITEMS_WITH_COUNTS.get(itemsInBucket[i]) + 1);
                } else {
                    ITEMS_WITH_COUNTS.put(itemsInBucket[i], 1);
                    ITEMS_WITH_ID.put(itemsInBucket[i], ITEMS_WITH_ID.size() + 1);
                }
            }
        }
    }

    private static void connectItemsIdAndItemsCount() {
        Object[] itemsNames = ITEMS_WITH_COUNTS.keySet().toArray();
        for (int i = 0; i < itemsNames.length; i++) {
            ITEMS_ID_AND_ITEMS_COUNT.put(ITEMS_WITH_ID.get(itemsNames[i]), ITEMS_WITH_COUNTS.get(itemsNames[i]));
        }
    }

    private static void createPairs() throws FileNotFoundException {
        File fileWithItems = new File(MultistageAlgorithm.SOURCE_FILE);
        Scanner scanner = new Scanner(fileWithItems);

        int k = 1;
        while (scanner.hasNextLine()) {
            String itemBucket = scanner.nextLine();
            String[] itemsInBucket = itemBucket.split(" ");
            Set<Pair> nBucketPairs = new HashSet<>();

            for (int i = 0; i < itemsInBucket.length; i++) {
                for (int j = i + 1; j < itemsInBucket.length; j++) {
                    nBucketPairs.add(Pair.builder()
                            .first(ITEMS_WITH_ID.get(itemsInBucket[i]))
                            .second(ITEMS_WITH_ID.get(itemsInBucket[j]))
                            .build());
                }
            }
            BUCKETS_WITH_PAIRS.put(k, nBucketPairs);
            k++;
        }
    }

    private static void deleteBadPairs() {
        Object[] pairs = PAIRS_WITH_COUNTS.keySet().toArray();
        Map<Integer, Integer> hashesAndCounts = new HashMap<>();
        for (int i = 0; i < PAIRS_WITH_HASHES.size(); i++) {
            Object[] currentPairs = PAIRS_WITH_HASHES.get(i).toArray();
            for (int j = 0; j < currentPairs.length; j++) {
                if (hashesAndCounts.containsKey(i)) {
                    hashesAndCounts.put(i, hashesAndCounts.get(i) + PAIRS_WITH_COUNTS.get(currentPairs[j]));
                } else {
                    hashesAndCounts.put(i, PAIRS_WITH_COUNTS.get(currentPairs[j]));
                }
            }
        }

        for (int i = 0; i < PAIRS_WITH_HASHES.size(); i++) {
            if (hashesAndCounts.get(i) < SUPPORT) {
                Object[] pairsForDelete = PAIRS_WITH_HASHES.get(i).toArray();
                for (int j = 0; j < pairsForDelete.length; j++) {
                    PAIRS_WITH_COUNTS.remove((Pair) pairsForDelete[j]);
                }
                for (int j = 0; j < pairsForDelete.length; j++) {
                    Object[] keysOfBuckets = BUCKETS_WITH_PAIRS.keySet().toArray();
                    for (int k = 0; k < keysOfBuckets.length; k++) {
                        if (BUCKETS_WITH_PAIRS.get(k) != null && BUCKETS_WITH_PAIRS.get(k).contains(pairsForDelete[j])) {
                            BUCKETS_WITH_PAIRS.get(k).remove(pairsForDelete[j]);
                        }
                    }
                }
                PAIRS_WITH_HASHES.remove(i);
                PAIRS_WITH_HASHES.put(i, Set.of());
            }
        }
    }

    private static void firstStageCounting() {
        for (int i = 1; i < BUCKETS_WITH_PAIRS.size() + 1; i++) {
            Object[] pairsOfCurrentBucket = BUCKETS_WITH_PAIRS.get(i).toArray();
            for (int j = 0; j < pairsOfCurrentBucket.length; j++) {
                Pair currentPair = (Pair) pairsOfCurrentBucket[j];
                if (PAIRS_WITH_COUNTS.containsKey(currentPair)) {
                    PAIRS_WITH_COUNTS.put(currentPair, PAIRS_WITH_COUNTS.get(currentPair) + 1);
                } else {
                    PAIRS_WITH_COUNTS.put(currentPair, 1);
                }
                if (PAIRS_WITH_HASHES.containsKey(currentPair.countHash1(ITEMS_COUNT))) {
                    Set<Pair> currentPairs = PAIRS_WITH_HASHES.get(currentPair.countHash1(ITEMS_COUNT));
                    Set<Pair> newPairs = new HashSet<>(currentPairs);
                    newPairs.add(currentPair);
                    PAIRS_WITH_HASHES.put(currentPair.countHash1(ITEMS_COUNT), newPairs);
                } else {
                    PAIRS_WITH_HASHES.put(currentPair.countHash1(ITEMS_COUNT), Set.of(currentPair));
                }
            }
        }
        deleteBadPairs();
    }

    private static void secondStageCounting() {
        PAIRS_WITH_COUNTS.clear();
        PAIRS_WITH_HASHES.clear();

        for (int i = 1; i < BUCKETS_WITH_PAIRS.size() + 1; i++) {
            Object[] pairsOfCurrentBucket = BUCKETS_WITH_PAIRS.get(i).toArray();
            for (int j = 0; j < pairsOfCurrentBucket.length; j++) {
                Pair currentPair = (Pair) pairsOfCurrentBucket[j];
                if (PAIRS_WITH_COUNTS.containsKey(currentPair)) {
                    PAIRS_WITH_COUNTS.put(currentPair, PAIRS_WITH_COUNTS.get(currentPair) + 1);
                } else {
                    PAIRS_WITH_COUNTS.put(currentPair, 1);
                }
                if (PAIRS_WITH_HASHES.containsKey(currentPair.countHash2(ITEMS_COUNT))) {
                    Set<Pair> currentPairs = PAIRS_WITH_HASHES.get(currentPair.countHash2(ITEMS_COUNT));
                    Set<Pair> newPairs = new HashSet<>(currentPairs);
                    newPairs.add(currentPair);
                    PAIRS_WITH_HASHES.put(currentPair.countHash2(ITEMS_COUNT), newPairs);
                } else {
                    PAIRS_WITH_HASHES.put(currentPair.countHash2(ITEMS_COUNT), Set.of(currentPair));
                }
            }
        }
        deleteBadPairs();
    }

    private static void deleteIfItemUnderSupport() {
        Object[] hashKeys = PAIRS_WITH_HASHES.keySet().toArray();
        for (int i = 0; i < ITEMS_ID_AND_ITEMS_COUNT.size(); i++) {
            if (ITEMS_ID_AND_ITEMS_COUNT.get(i) != null && ITEMS_ID_AND_ITEMS_COUNT.get(i) < SUPPORT) {
                for (int j = 0; j < hashKeys.length; j++) {
                    if (PAIRS_WITH_HASHES.get(j) != null) {
                        Object[] pairs = PAIRS_WITH_HASHES.get(j).toArray();
                        for (Object pair : pairs) {
                            Pair currentPair = (Pair) pair;
                            if (currentPair.getFirst() == i || currentPair.getSecond() == i) {
                                PAIRS_WITH_HASHES.get(j).remove(currentPair);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void printResult() {
        System.out.println("FREQUENT ITEMSETS: ");
        for (int i = 1; i < ITEMS_ID_AND_ITEMS_COUNT.size() + 1; i++) {
            if (ITEMS_ID_AND_ITEMS_COUNT.get(i) >= SUPPORT) {
                System.out.println(i);
            }
        }

        for (int i = 0; i < PAIRS_WITH_HASHES.size(); i++) {
            if (PAIRS_WITH_HASHES.get(i) != null && PAIRS_WITH_HASHES.get(i).size() != 0) {
                Object[] current = PAIRS_WITH_HASHES.get(i).toArray();
                    for (int j = 0; j < current.length; j++) {
                        System.out.println(current[j]);
                    }
            }
        }
    }
}
