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
    private static final class FillStatsAccumulator {
        private int nodeCount;
        private int leafCount;
        private int totalKeys;
        private final int[] nodesByKeyCount;

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


    //Удаление из листа
    private void removeFromLeaf(Node leaf, int index) {
        for (int i = index; i < leaf.keyCount-1; i++) {
            leaf.keys[i] = leaf.keys[i+1];
            leaf.products[i] = leaf.products[i+1];
        }
        leaf.keys[leaf.keyCount - 1] = 0;
        leaf.products[leaf.keyCount - 1] = null;
        leaf.keyCount--;
    }

    //слияние детей у которых минимальное кол-во ключей
    private void mergeChildren(Node parent, int index) {
        Node leftSibling = parent.children[index];
        Node rightSibling = parent.children[index+1];
        int startIndex = leftSibling.keyCount+1;
        //из родителя в левого брата передаем
        leftSibling.keys[leftSibling.keyCount] = parent.keys[index];
        leftSibling.products[leftSibling.keyCount] = parent.products[index];
        leftSibling.keyCount++;
        //из правого брата в левого
        for (int i = 0; i < rightSibling.keyCount; i++) {
            leftSibling.keys[leftSibling.keyCount] = rightSibling.keys[i];
            leftSibling.products[leftSibling.keyCount] = rightSibling.products[i];
            leftSibling.keyCount++;
        }
        //переносим детей правого брата
        if (!rightSibling.leaf) {
            for (int i = 0; i < rightSibling.keyCount+1; i++) {
                leftSibling.children[startIndex+i] = rightSibling.children[i];
                rightSibling.children[i] = null;
            }
        }
        //Удаляем у родителя перенесенные
        for (int i = index; i < parent.keyCount-1; i++) {
            parent.keys[i] = parent.keys[i+1];
            parent.products[i] = parent.products[i+1];
        }
        for (int i = index+1; i < parent.keyCount; i++) {
            parent.children[i] = parent.children[i+1];
        }
        parent.keyCount--;
        parent.keys[parent.keyCount] = 0;
        parent.products[parent.keyCount] = null;
        parent.children[parent.keyCount+1] = null;
    }

    //заимствование ключа у левого брата
    private void borrowFromLeftSibling(Node parent, int childIndex) {
        Node leftSibling = parent.children[childIndex - 1];
        Node rightSibling = parent.children[childIndex];
        //освобождаем место у правого брата
        for (int i = rightSibling.keyCount; i > 0; i--) {
            rightSibling.keys[i] = rightSibling.keys[i-1];
            rightSibling.products[i] = rightSibling.products[i-1];
        }
        //из родителя в правого брата
        rightSibling.keys[0] = parent.keys[childIndex-1];
        rightSibling.products[0] = parent.products[childIndex-1];

        //из левого брата в родителя
        parent.keys[childIndex-1] = leftSibling.keys[leftSibling.keyCount-1];
        parent.products[childIndex-1] = leftSibling.products[leftSibling.keyCount-1];

        //перенос детей
        if (!leftSibling.leaf) {
            for (int i = rightSibling.keyCount; i >= 0; i--) {
                rightSibling.children[i + 1] = rightSibling.children[i];
            }

            rightSibling.children[0] = leftSibling.children[leftSibling.keyCount];
            leftSibling.children[leftSibling.keyCount] = null;
        }
        //очищаем
        leftSibling.keyCount--;
        leftSibling.keys[leftSibling.keyCount] = 0;
        leftSibling.products[leftSibling.keyCount] = null;
        rightSibling.keyCount++;
    }

    //заимствование ключа у правого брата
    private void borrowFromRightSibling(Node parent, int childIndex) {
        Node rightSibling = parent.children[childIndex + 1];
        Node leftSibling = parent.children[childIndex];
        //из родителя в левого брата
        leftSibling.keys[leftSibling.keyCount] = parent.keys[childIndex];
        leftSibling.products[leftSibling.keyCount] = parent.products[childIndex];

        //из правого брата в родителя
        parent.keys[childIndex] = rightSibling.keys[0];
        parent.products[childIndex] = rightSibling.products[0];

        //перенос детей
        if (!rightSibling.leaf) {
            leftSibling.children[leftSibling.keyCount + 1] = rightSibling.children[0];
            for (int i = 0; i < rightSibling.keyCount; i++) {
                rightSibling.children[i] = rightSibling.children[i + 1];
            }

            rightSibling.children[rightSibling.keyCount] = null;
        }

        // Сдвигаем ключи и продукты правого брата влево
        for (int i = 0; i < rightSibling.keyCount - 1; i++) {
            rightSibling.keys[i] = rightSibling.keys[i + 1];
            rightSibling.products[i] = rightSibling.products[i + 1];
        }

        //очищаем
        rightSibling.keyCount--;
        rightSibling.keys[rightSibling.keyCount] = 0;
        rightSibling.products[rightSibling.keyCount] = null;
        leftSibling.keyCount++;
    }


    //проверка чтобы не осталось пустых узлов
    private int ensureChildHasSpareKey(Node parent, int childIndex) {
        if (parent.children[childIndex].keyCount > MIN_KEYS) {
            return childIndex;
        }
        if (childIndex > 0 && parent.children[childIndex-1].keyCount > MIN_KEYS) {
            borrowFromLeftSibling(parent, childIndex);
            return childIndex;
        }
        if (childIndex < parent.keyCount && parent.children[childIndex+1].keyCount > MIN_KEYS) {
            borrowFromRightSibling(parent, childIndex);
            return childIndex;
        }
        if (childIndex > 0) {
            mergeChildren(parent, childIndex-1);
            return childIndex-1;
        }
        mergeChildren(parent, childIndex);
        return childIndex;

    }

    //самый большой ключ из поддерева
    private long findPredecessor(Node node) {
        if (node.leaf) {
            return node.keys[node.keyCount-1];
        }
        return findPredecessor(node.children[node.keyCount]);
    }

    //самый маленький ключ из поддерева
    private long findSuccessor(Node node) {
        if (node.leaf) {
            return node.keys[0];
        }
        return findSuccessor(node.children[0]);
    }

    //рекурсивное удаление
    private boolean removeRecursive(Node node, long article) {
        int index = findKeyIndex(node, article);
        if (index < node.keyCount && node.keys[index] == article) {
            if (node.leaf) {
                removeFromLeaf(node, index);
                return true;
            } else {
                if (node.children[index].keyCount > MIN_KEYS) {
                    long biggestKey = findPredecessor(node.children[index]);
                    Product biggesProduct = findByArticle(biggestKey);

                    node.keys[index] = biggestKey;
                    node.products[index] = biggesProduct;
                    return removeRecursive(node.children[index], biggestKey);

                } else if (node.children[index+1].keyCount > MIN_KEYS) {
                    long smallesKey = findSuccessor(node.children[index+1]);
                    Product smallesProduct = findByArticle(smallesKey);

                    node.keys[index] = smallesKey;
                    node.products[index] = smallesProduct;
                    return removeRecursive(node.children[index+1], smallesKey);
                } else {
                    mergeChildren(node, index);
                    return removeRecursive(node.children[index], article);
                }
            }

        } else {
            if (node.leaf) {
                return false;
            }
            int preparedChild = ensureChildHasSpareKey(node, index);
            return removeRecursive(node.children[preparedChild], article);
        }
    }


    @Override
    public boolean removeByArticle(long article) {
        if (root.keyCount == 0) {
            return false;
        }

        if (findByArticle(article) == null) {
            return false;
        }

        boolean isDeleted = removeRecursive(root, article);

        if (isDeleted) {
            size--;
        }
        if (root.keyCount == 0 && !root.leaf) {
            root = root.children[0];
        }
        return isDeleted;
    }
}
