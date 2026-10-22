package com.sworzzey.storecatalog.structure;

import com.sworzzey.storecatalog.model.Product;

import java.util.function.Consumer;

public interface ProductStore {
    /**
     * Добавляет товар
     * @return true - товар добавлен
     *          false - в дереве уже есть товар с таким артикулом
     */
    boolean add(Product product);

    /**
     * Тот же add только сохраняет узлы через которые прошел
     */
    boolean add(Product product, VisitTrace trace);

    /**
     * Ищет товар по артикулу
     * @return найденный товар или null, если товара нет
     */
    Product findByArticle(long article);

    /**
     * Тот же findByArticle только сохраняет узлы в которые вошел
     */
    Product findByArticle(long article, VisitTrace trace);

    /**
     * Удаляет товар по артикулу
     * @return true - товар удален
     *          false - товара с таким артикулом нет
     */
    boolean removeByArticle(long article);

    /**
     * @return количество товаров в дереве
     */
    int size();

    /**
     * Обходит все товары в порядке возрастания артикула
     */
    void forEach(Consumer<Product> action);

    /**
     * Обходит все значения в диапазоне от from до to
     */
    void forEachInRange(long from, long to, Consumer<Product> action);

    /**
     * Статистика заполненности дерева
     */
    NodeFillStats getFillStats();


    /**
     * Очистка дерева
     */
    void clear();


    /**
     * Снимок дерева для рисования в canvas
     */
    NodeView snapshot(int maxLevels);
}
