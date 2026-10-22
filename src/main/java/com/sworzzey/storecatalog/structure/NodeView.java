package com.sworzzey.storecatalog.structure;

import java.util.Arrays;

public class NodeView {
    private final long[] keys;
    private final NodeView[] children;
    private final boolean hasHiddenChildren;
    private final int id;

    public NodeView(long[] keys, NodeView[] children, boolean hiddenChildren, int id) {
        this.keys = Arrays.copyOf(keys, keys.length);
        this.children = Arrays.copyOf(children, children.length);
        this.hasHiddenChildren = hiddenChildren;
        this.id = id;
    }

    public long[] getKeys() {
        return Arrays.copyOf(keys, keys.length);
    }

    public NodeView[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    public boolean isHasHiddenChildren() {
        return hasHiddenChildren;
    }

    public int getId() { return id; }
}
