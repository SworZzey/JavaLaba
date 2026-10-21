package com.sworzzey.storecatalog.structure;

import java.util.Arrays;

public class NodeView {
    private final long[] keys;
    private final NodeView[] children;
    private final boolean hasHiddenChildren;

    public NodeView(long[] keys, NodeView[] children, boolean hiddenChildren) {
        this.keys = Arrays.copyOf(keys, keys.length);
        this.children = Arrays.copyOf(children, children.length);
        this.hasHiddenChildren = hiddenChildren;
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
}
