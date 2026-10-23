
package com.sworzzey.storecatalog.benchmark;

import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.structure.BTreeProductStore;
import com.sworzzey.storecatalog.structure.ProductStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class BTreeBenchmark {

    private static final int[] SIZES = {10_000, 100_000};
    private static final int RANGE_SCANS = 1_000;
    private static final int STATS_CALLS = 30;
    private static final int WARMUP_ROUNDS = 25;
    private static final int MEASURE_ROUNDS = 7;


    private BTreeBenchmark() {
    }

    /*
     * Пять сравниваемых операций:
     * INSERT, SEARCH, RANGE_SCAN, STATS, REMOVE.
     */
    private enum Operation {
        INSERT("Вставка", "insert"),
        SEARCH("Поиск", "search"),
        RANGE_SCAN("Диапазонный скан", "range"),
        REMOVE("Удаление", "remove");

        final String title;
        final String key;

        Operation(String title, String key) {
            this.title = title;
            this.key = key;
        }
    }

    /*
     * Общий интерфейс бенчмарка.
     * Основной код не зависит от конкретной структуры данных.
     */
    private interface BenchStore {
        boolean insert(Product product);

        Product find(long article);

        long scan(long from, long to);

        boolean remove(long article);

        int size();
    }

    @FunctionalInterface
    private interface StoreFactory {
        BenchStore create();
    }

    /*
     * Адаптер для B-дерева приложения.
     * TreeMap не используется в основном коде приложения.
     */
    private static final class BTreeAdapter implements BenchStore {
        private final ProductStore store = new BTreeProductStore();

        @Override
        public boolean insert(Product product) {
            return store.add(product);
        }

        @Override
        public Product find(long article) {
            return store.findByArticle(article);
        }

        @Override
        public long scan(long from, long to) {
            long[] count = {0};

            store.forEachInRange(from, to, product -> count[0]++);

            return count[0];
        }


        @Override
        public boolean remove(long article) {
            return store.removeByArticle(article);
        }

        @Override
        public int size() {
            return store.size();
        }
    }

    /*
     * Адаптер для стандартной Java TreeMap.
     */
    private static final class TreeMapAdapter implements BenchStore {
        private final TreeMap<Long, Product> map = new TreeMap<>();

        @Override
        public boolean insert(Product product) {
            long article = product.getArticle();

            if (map.containsKey(article)) {
                return false;
            }

            map.put(article, product);
            return true;
        }

        @Override
        public Product find(long article) {
            return map.get(article);
        }

        @Override
        public long scan(long from, long to) {
            long count = 0;

            for (Product ignored :
                    map.subMap(from, true, to, true).values()) {
                count++;
            }

            return count;
        }

        @Override
        public boolean remove(long article) {
            return map.remove(article) != null;
        }

        @Override
        public int size() {
            return map.size();
        }
    }

    private record Window(long from, long to) {
        long expectedCount() {
            return to - from + 1;
        }
    }

    private record TestData(
            int n,
            List<Product> insertionOrder,
            long[] searchOrder,
            long[] removalOrder,
            Window[] windows
    ) {
    }

    private record CycleResult(
            long insertNanos,
            long searchNanos,
            long rangeNanos,
            long removeNanos
    ) {
        long nanos(Operation operation) {
            return switch (operation) {
                case INSERT -> insertNanos;
                case SEARCH -> searchNanos;
                case RANGE_SCAN -> rangeNanos;
                case REMOVE -> removeNanos;
            };
        }
    }

    public static void main(String[] args) {
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("VM: " + System.getProperty("java.vm.name"));
        System.out.println("Прогрев: " + WARMUP_ROUNDS
                + ", измеряемых повторов: " + MEASURE_ROUNDS);
        System.out.println();

        Map<Integer, Map<Operation, List<Long>>> bTreeResults =
                new java.util.LinkedHashMap<>();
        Map<Integer, Map<Operation, List<Long>>> treeMapResults =
                new java.util.LinkedHashMap<>();

        for (int n : SIZES) {
            TestData data = createData(n);

            StoreFactory bTreeFactory = BTreeAdapter::new;
            StoreFactory treeMapFactory = TreeMapAdapter::new;

            // Прогреваем обе реализации на одном наборе данных.
            for (int i = 0; i < WARMUP_ROUNDS; i++) {
                runCycle(bTreeFactory, data);
                runCycle(treeMapFactory, data);
            }

            Map<Operation, List<Long>> bTreeTimes =
                    new EnumMap<>(Operation.class);
            Map<Operation, List<Long>> treeMapTimes =
                    new EnumMap<>(Operation.class);

            for (Operation operation : Operation.values()) {
                bTreeTimes.put(operation, new ArrayList<>());
                treeMapTimes.put(operation, new ArrayList<>());
            }

            // Чередуем порядок реализаций, чтобы уменьшить влияние
            // системного прогрева и порядка запусков.
            for (int i = 0; i < MEASURE_ROUNDS; i++) {
                CycleResult bTree;
                CycleResult treeMap;

                if (i % 2 == 0) {
                    bTree = runCycle(bTreeFactory, data);
                    treeMap = runCycle(treeMapFactory, data);
                } else {
                    treeMap = runCycle(treeMapFactory, data);
                    bTree = runCycle(bTreeFactory, data);
                }

                for (Operation operation : Operation.values()) {
                    bTreeTimes.get(operation).add(bTree.nanos(operation));
                    treeMapTimes.get(operation).add(treeMap.nanos(operation));

                }
            }

            bTreeResults.put(n, bTreeTimes);
            treeMapResults.put(n, treeMapTimes);
        }

        printTable(bTreeResults, treeMapResults);
        System.out.println();
    }

    private static CycleResult runCycle(
            StoreFactory factory,
            TestData data
    ) {
        // Создание пустого дерева/карты не входит во время вставки.
        BenchStore store = factory.create();
        int n = data.n();


        long start = System.nanoTime();

        for (Product product : data.insertionOrder()) {
            if (!store.insert(product)) {
                throw new IllegalStateException(
                        "Не удалось вставить артикул "
                                + product.getArticle()
                );
            }
        }

        long insertNanos = System.nanoTime() - start;

        if (store.size() != n) {
            throw new IllegalStateException(
                    "После вставки ожидалось " + n
                            + " элементов, получено " + store.size()
            );
        }

        start = System.nanoTime();

        for (long article : data.searchOrder()) {
            Product found = store.find(article);

            if (found == null || found.getArticle() != article) {
                throw new IllegalStateException(
                        "Не найден артикул " + article
                );
            }
        }

        long searchNanos = System.nanoTime() - start;

        start = System.nanoTime();

        for (Window window : data.windows()) {
            long count = store.scan(window.from(), window.to());

            if (count != window.expectedCount()) {
                throw new IllegalStateException(
                        "Неверный результат диапазона ["
                                + window.from() + ", " + window.to()
                                + "]: ожидалось " + window.expectedCount()
                                + ", получено " + count
                );
            }
        }

        long rangeNanos = System.nanoTime() - start;



        start = System.nanoTime();

        for (long article : data.removalOrder()) {
            if (!store.remove(article)) {
                throw new IllegalStateException(
                        "Не удалось удалить артикул " + article
                );
            }
        }

        long removeNanos = System.nanoTime() - start;

        if (store.size() != 0) {
            throw new IllegalStateException(
                    "После удаления хранилище не пусто: "
                            + store.size()
            );
        }


        return new CycleResult(
                insertNanos,
                searchNanos,
                rangeNanos,
                removeNanos
        );
    }

    private static TestData createData(int n) {
        Random random = new Random(12345L + n);

        List<Product> products = new ArrayList<>(n);
        long[] searchOrder = new long[n];
        long[] removalOrder = new long[n];

        // Ключи плотные: 1, 2, ..., N.
        for (int i = 1; i <= n; i++) {
            products.add(new Product(
                    i,
                    "Product " + i,
                    "Benchmark",
                    10.0 + (i % 1000),
                    i % 100
            ));

            searchOrder[i - 1] = i;
            removalOrder[i - 1] = i;
        }

        Collections.shuffle(products, random);
        shuffle(searchOrder, random);
        shuffle(removalOrder, random);

        int width = Math.max(1, n / 100);
        Window[] windows = new Window[RANGE_SCANS];

        for (int i = 0; i < RANGE_SCANS; i++) {
            long from = 1L + random.nextInt(n - width + 1);
            long to = from + width - 1L;
            windows[i] = new Window(from, to);
        }

        return new TestData(
                n,
                products,
                searchOrder,
                removalOrder,
                windows
        );
    }

    private static void shuffle(long[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            long temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }
    }

    private static void printTable(
            Map<Integer, Map<Operation, List<Long>>> bTreeResults,
            Map<Integer, Map<Operation, List<Long>>> treeMapResults
    ) {
        System.out.println(
                "N | Операция | B-дерево: мс / мкс на операцию"
                        + " | TreeMap: мс / мкс на операцию"
                        + " | TreeMap / B-дерево"
        );

        System.out.println("-".repeat(112));

        for (int n : SIZES) {
            for (Operation operation : Operation.values()) {
                List<Long> bTreeSamples =
                        bTreeResults.get(n).get(operation);

                double bTreeMs = median(bTreeSamples) / 1_000_000.0;
                double bTreeUs = median(bTreeSamples)
                        / (operationCount(operation, n) * 1_000.0);

                List<Long> treeMapSamples =
                        treeMapResults.get(n).get(operation);

                double treeMapMs =
                        median(treeMapSamples) / 1_000_000.0;
                double treeMapUs = median(treeMapSamples)
                        / (operationCount(operation, n) * 1_000.0);

                // Отношение больше 1 означает, что B-дерево быстрее.
                double ratio = treeMapMs / bTreeMs;

                System.out.printf(
                        "%d | %s | %.3f мс / %.3f мкс"
                                + " | %.3f мс / %.3f мкс"
                                + " | %.2fx%n",
                        n,
                        operation.title,
                        bTreeMs,
                        bTreeUs,
                        treeMapMs,
                        treeMapUs,
                        ratio
                );
            }
        }
    }

    private static int operationCount(Operation operation, int n) {
        return switch (operation) {
            case INSERT, SEARCH, REMOVE -> n;
            case RANGE_SCAN -> RANGE_SCANS;
        };
    }

    private static long median(List<Long> samples) {
        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);

        int middle = sorted.size() / 2;

        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }

        return (sorted.get(middle - 1) + sorted.get(middle)) / 2;
    }
}