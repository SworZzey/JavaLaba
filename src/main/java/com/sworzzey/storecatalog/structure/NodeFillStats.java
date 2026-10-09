package com.sworzzey.storecatalog.structure;

public class NodeFillStats {
    private final int nodeCount;
    private final int leafCount;
    private final int height;
    private final int[] distribution;
    private final double averageFillPercent;

    public NodeFillStats(int nodeCount, int leafCount, int height, int[] distribution, double averageFillPercent) {
        this.nodeCount = nodeCount;
        this.leafCount = leafCount;
        this.height = height;
        //Защищаем от изменения
        this.distribution = new int[distribution.length];
        for (int i = 0; i < distribution.length; i++) {
            this.distribution[i] = distribution[i];
        }
        this.averageFillPercent = averageFillPercent;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getLeafCount() {
        return leafCount;
    }

    public int getHeight() {
        return height;
    }

    //отдаем копию массива
    public int[] getDistribution() {
        int[] copy = new int[distribution.length];
        for (int i = 0; i < distribution.length; i++) {
            copy[i] = distribution[i];
        }
        return copy;
    }

    public double getAverageFillPercent() {
        return averageFillPercent;
    }
}
