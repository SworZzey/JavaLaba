package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.structure.NodeView;
import com.sworzzey.storecatalog.structure.VisitTrace;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class TreeRenderer {
    private static final int CELL_WIDTH = 36;
    private static final int CELL_HEIGHT = 30;
    private static final int LEVEL_HEIGHT = 90;
    private static final int NODE_GAP = 20;
    private static final int HORIZONTAL_PADDING = 20;
    private static final int VERTICAL_PADDING = 20;

    public static class TreeDimensions {
        final double width;
        final double height;

        TreeDimensions(double width, double height) {
            this.width = width;
            this.height = height;
        }
    }

    private double getNodeWidth(NodeView node) {
        return node.getKeys().length * CELL_WIDTH;
    }

    private double subTreeWidth(NodeView node) {
        double nodeWidth = getNodeWidth(node);
        NodeView[] children = node.getChildren();
        if (children.length == 0) {
            return nodeWidth;
        }
        double maxChildrenWidth = 0;
        int childrenCount = children.length;
        maxChildrenWidth+=NODE_GAP*(childrenCount-1);
        for (int i = 0; i < children.length; i++) {
            maxChildrenWidth += subTreeWidth(children[i]);
        }
        return Math.max(nodeWidth, maxChildrenWidth);
    }

    private int treeHeight(NodeView root) {
        if (root.isHasHiddenChildren() || root.getChildren().length == 0) {
            return 1;
        }
        int maxDepth = 0;
        for (int i = 0; i < root.getChildren().length; i++) {
            int childDepth = treeHeight(root.getChildren()[i]);
            maxDepth = Math.max(maxDepth, childDepth);
        }
        return maxDepth+1;
    }

    public TreeDimensions calculateSize(NodeView root) {
        double width = subTreeWidth(root) + HORIZONTAL_PADDING*2;
        int treeDeep = treeHeight(root);
        double height = (treeDeep - 1) * LEVEL_HEIGHT+CELL_HEIGHT+VERTICAL_PADDING*2;
        return new TreeDimensions(width, height);
    }

    public void drawNode(GraphicsContext gc, NodeView root, double x, double y, Color nodeColor, VisitTrace trace) {
        long[] keys = root.getKeys();

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (int i = 0; i < keys.length; i++) {
            double cellPosition = x+i*CELL_WIDTH;
            Color cellColor = Color.WHITE;

            if (trace != null && keys[i] == trace.getTargetKey()) {
                cellColor = Color.LIGHTGREEN;
            } else if (trace != null && trace.findId(root.getId())) {
                cellColor = Color.ORANGE;
            }

            gc.setFill(cellColor);
            gc.fillRect(cellPosition, y, CELL_WIDTH, CELL_HEIGHT);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(cellPosition, y, CELL_WIDTH, CELL_HEIGHT);
            double horizontalCentre = cellPosition + CELL_WIDTH / 2.0;
            double verticalCentre = y + CELL_HEIGHT / 2.0;
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(keys[i]), horizontalCentre, verticalCentre);
        }
        if (root.isHasHiddenChildren()) {
            double nodeWidth = keys.length * CELL_WIDTH;
            double centerX = keys.length == 0
                    ? x
                    : x + nodeWidth / 2.0;
            double bottomY = y + CELL_HEIGHT;

            gc.fillText("...", centerX, bottomY + 12);
        }
    }

    private void drawEdges(GraphicsContext gc, NodeView node, double left, double y) {
        double subtreeWidth = subTreeWidth(node);
        long[] keys = node.getKeys();
        double nodeWidth = keys.length * CELL_WIDTH;

        double centerX = left + subtreeWidth / 2.0;
        double nodeX = centerX - nodeWidth / 2.0;

        NodeView[] children = node.getChildren();

        if (children.length > 0 && !node.isHasHiddenChildren()) {
            double childrenWidth = 0;

            for (NodeView child : children) {
                childrenWidth += subTreeWidth(child);
            }

            childrenWidth += NODE_GAP * (children.length - 1);

            double childLeft = left + (subtreeWidth - childrenWidth) / 2.0;
            double childY = y + LEVEL_HEIGHT;

            for (int i = 0; i < children.length; i++) {
                NodeView child = children[i];
                double childSubtreeWidth = subTreeWidth(child);
                double childCenterX = childLeft + childSubtreeWidth / 2.0;

                double parentX;
                if (keys.length == 0) {
                    parentX = centerX;
                } else {
                    parentX = nodeX + i * CELL_WIDTH;
                }

                gc.strokeLine(
                        parentX,
                        y + CELL_HEIGHT,
                        childCenterX,
                        childY
                );

                drawEdges(gc, child, childLeft, childY);

                childLeft += childSubtreeWidth + NODE_GAP;
            }
        }
    }

    private void drawNodes(GraphicsContext gc, NodeView node, double left, double y, VisitTrace trace) {
        double subtreeWidth = subTreeWidth(node);
        long[] keys = node.getKeys();
        double nodeWidth = keys.length * CELL_WIDTH;

        double centerX = left + subtreeWidth / 2.0;
        double nodeX = centerX - nodeWidth / 2.0;

        Color nodeColor = Color.WHITE;

        if (trace != null && trace.findId(node.getId())
                && node.getId() != trace.getTargetNode()) {
            nodeColor = Color.ORANGE;
        }

        drawNode(gc, node, nodeX, y, nodeColor, trace);

        NodeView[] children = node.getChildren();

        if (children.length > 0 && !node.isHasHiddenChildren()) {
            double childrenWidth = 0;

            for (NodeView child : children) {
                childrenWidth += subTreeWidth(child);
            }

            childrenWidth += NODE_GAP * (children.length - 1);

            double childLeft = left + (subtreeWidth - childrenWidth) / 2.0;
            double childY = y + LEVEL_HEIGHT;

            for (NodeView child : children) {
                double childSubtreeWidth = subTreeWidth(child);

                drawNodes(gc, child, childLeft, childY, trace);

                childLeft += childSubtreeWidth + NODE_GAP;
            }
        }
    }

    public void draw(GraphicsContext gc, NodeView root, VisitTrace trace) {
        TreeDimensions size = calculateSize(root);
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        if (root.getKeys().length == 0 && root.getChildren().length == 0) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("Дерево пусто", size.width/2.0, size.height/2.0);
            return;
        }
        drawEdges(gc, root, HORIZONTAL_PADDING, VERTICAL_PADDING);
        drawNodes(gc, root, HORIZONTAL_PADDING, VERTICAL_PADDING, trace);
    }
}
