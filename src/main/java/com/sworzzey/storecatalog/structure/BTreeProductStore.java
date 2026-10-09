package com.sworzzey.storecatalog.structure;

import com.sworzzey.storecatalog.model.Product;

import java.util.function.Consumer;

public class BTreeProductStore implements ProductStore{
    private static final int MAX_KEYS = 3;
    private static final int MAX_CHILDREN = 4;
    private static final int MIN_KEYS = 1;
    private static final int MIN_CHILDREN = 2;

    private Node root;
    private int size;

    private static final class Node {
        private final long[] keys;
        private final Product[] products;
        private final Node[] children;

        private int keyCount;
        private final boolean leaf;

        private Node(boolean leaf) {
            this.leaf = leaf;
            this.keys = new long[MAX_KEYS];
            this.products = new Product[MAX_KEYS];
            this.children = new Node[MAX_CHILDREN];
        }
    }

    // накапливает значения для статистики
    private final class FillStatsAccumulator {
        private int nodeCount;
        private int leafCount;
        private int totalKeys;
        private int[] nodesByKeyCount;

        private FillStatsAccumulator() {
            nodesByKeyCount = new int[MAX_KEYS + 1];
        }
    }

    public BTreeProductStore() {
        root = new Node(true);
        size = 0;
    }

    //поиск индекса ключа
    private int findKeyIndex(Node node, long article) {
        for (int i = 0; i < node.keyCount; i++) {
            if (node.keys[i] >= article) {
                return i;
            }
        }
        return node.keyCount;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean removeByArticle(long article) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Product findByArticle(long article) {
        Node current = root;
        while (true) {
            int place = findKeyIndex(current, article);
            if (place < current.keyCount) {
                if (current.keys[place] == article) {
                    return current.products[place];
                }
            }
            if (current.leaf) {
                return null;
            } else {
                current = current.children[place];
            }
        }
    }

    //обход одного узла
    private void bypass(Node node, Consumer<Product> action) {
        if (node.leaf) {
            for (int i = 0; i < node.keyCount; i++) {
                action.accept(node.products[i]);
            }
        } else {
            for (int i = 0; i < node.keyCount; i++) {
                bypass(node.children[i], action);
                action.accept(node.products[i]);
            }
            bypass(node.children[node.keyCount], action);
        }
    }


    @Override
    public void forEach(Consumer<Product> action) {
        bypass(root, action);
    }

    //расщепление ребенка
    private void splitChild(Node parent, int childIndex) {
        //переносим ключ и продукт от ребенка к брату
        Node child = parent.children[childIndex];
        Node sibling = new Node(child.leaf);
        long moveKey = child.keys[2];
        Product moveProduct = child.products[2];
        child.keys[2] = 0;
        child.products[2] = null;
        child.keyCount--;
        sibling.keys[0] = moveKey;
        sibling.products[0] = moveProduct;
        sibling.keyCount++;

        //Если ребенок - не лист, то перекидываем к близнецу детей
        if (!child.leaf) {
            Node movedChild1 = child.children[2];
            sibling.children[0] = movedChild1;
            Node movedChild2 = child.children[3];
            sibling.children[1] = movedChild2;
            child.children[2] = null;
            child.children[3] = null;
        }
        // средний ключ перекидываем к бате
        long movedMiddleKey = child.keys[1];
        Product movedMiddleProduct = child.products[1];
        child.keys[1] = 0;
        child.keyCount--;
        child.products[1] = null;
        //двигаем ключи у бати что бы поставить ключ от ребенка
        for (int i = parent.keyCount; i > childIndex; i--) {
            parent.keys[i] = parent.keys[i - 1];
            parent.products[i] = parent.products[i - 1];
        }
        parent.keys[childIndex] = movedMiddleKey;
        parent.products[childIndex] = movedMiddleProduct;

        //присоединяем к бате близнеца
        for (int i = parent.keyCount; i > childIndex; i--) {
            parent.children[i+1] = parent.children[i];
        }
        parent.children[childIndex+1] = sibling;
        parent.keyCount++;
    }


    //вставка в неполный узел
    private void insertNonFull(Node node, Product product) {
        long article = product.getArticle();
        if (node.leaf) {
            int insertIndex = findKeyIndex(node, article);
            for (int i = node.keyCount-1; i >= insertIndex; i--) {
                node.keys[i+1] = node.keys[i];
                node.products[i+1] = node.products[i];
            }
            node.keys[insertIndex] = article;
            node.products[insertIndex] = product;
            node.keyCount++;
        } else {
            Node child = node.children[findKeyIndex(node, article)];
            if (child.keyCount == MAX_KEYS) {
                splitChild(node, findKeyIndex(node, article));
                child = node.children[findKeyIndex(node, article)];
            }
            insertNonFull(child, product);
        }
    }

    @Override
    public boolean add(Product product) {
        if (product == null) {
            return false;
        } else if (findByArticle(product.getArticle()) != null) {
            return false;
        }

        if (root.keyCount == MAX_KEYS) {
            Node newRoot = new Node(false);
            newRoot.children[0] = root;
            root = newRoot;
            splitChild(newRoot, 0);
        }
        insertNonFull(root, product);
        size++;
        return true;
    }


    private void bypassRange(Node node, Consumer<Product> action, long from, long to) {
        for (int i = 0; i < node.keyCount; i++) {

            if (!node.leaf && node.keys[i] >= from) {
                bypassRange(node.children[i], action, from, to);
            }

            if (node.keys[i] > to) {
                return;
            }

            if (node.keys[i] >= from) {
                action.accept(node.products[i]);
            }
        }

        if (!node.leaf) {
            bypassRange(node.children[node.keyCount], action, from, to);
        }
    }

    @Override
    public void forEachInRange(long from, long to, Consumer<Product> action) {
        if (from > to) {
            return;
        }
        bypassRange(root, action, from, to);
    }

    //обходит все дерево 1 раз и обновляет накопитель
    private void collectFillStats(Node node, FillStatsAccumulator accumulator) {
        accumulator.nodeCount++;
        accumulator.totalKeys += node.keyCount;
        accumulator.nodesByKeyCount[node.keyCount]++;
        if (node.leaf) {
            accumulator.leafCount++;
        } else {
            for (int i = 0; i <= node.keyCount; i++) {
                collectFillStats(node.children[i], accumulator);
            }
        }
    }

    //метод определения высоты дерева
    private int calculateHeight(Node node, int count) {
        if (node.leaf) {
            return count;
        }
        return calculateHeight(node.children[0], ++count);
    }


    @Override
    public NodeFillStats getFillStats() {
        FillStatsAccumulator stats = new FillStatsAccumulator();
        collectFillStats(root, stats);
        int height = calculateHeight(root, 0);
        double fill = ((double)stats.totalKeys/(stats.nodeCount*MAX_KEYS))*100;
        int[] copyNodesByKeyCount = new int[MAX_KEYS+1];
        for (int i = 0; i < stats.nodesByKeyCount.length; i++) {
            copyNodesByKeyCount[i] = stats.nodesByKeyCount[i];
        }

        return new NodeFillStats(stats.nodeCount, stats.leafCount, height, copyNodesByKeyCount, fill);
    }
}
