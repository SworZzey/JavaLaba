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
        this.distribution = distribution;
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

    public int[] getDistribution() {
        return distribution;
    }

    public double getAverageFillPercent() {
        return averageFillPercent;
    }
}
