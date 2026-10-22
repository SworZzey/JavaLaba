package com.sworzzey.storecatalog.structure;

public class VisitTrace {
    private int[] ids;
    private int size;

    public VisitTrace() {
        this.ids = new int[8];
        size = 0;
    }

    public boolean add(int nodeId) {
        if (size == ids.length) {
            int[] newIds = new int[size*2];
            for (int i = 0; i < ids.length; i++) {
                newIds[i] = ids[i];
            }
            this.ids = newIds;
        }
        ids[size] = nodeId;
        size++;
        return true;
    }

    public int getSize() {
        return size;
    }

    public int getIdByIndex(int index) {
        if (index < 0 || index > size-1) {
            throw new IndexOutOfBoundsException("Недопустимый индекс");
        }
        return ids[index];
    }

    public boolean findId(int nodeId) {
        for (int i = 0; i < size; i++) {
            if (ids[i] == nodeId) {
                return true;
            }
        }
        return false;
    }
}
