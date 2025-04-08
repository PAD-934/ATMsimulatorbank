package main.java;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class BorderLayout implements LayoutManager {

    public static final String CENTER = "Center";
    public static final String SOUTH = "South";
    public static final String EAST = "East";
    public static final String WEST = "West";
    public static final String NORTH = "North";

    private int hgap;
    private int vgap;
    private Component north, south, east, west, center;

    public BorderLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    public BorderLayout() {
        this(0, 0);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        if (name == null) {
            name = CENTER;
        }

        switch (name) {
            case NORTH -> north = comp;
            case SOUTH -> south = comp;
            case EAST -> east = comp;
            case WEST -> west = comp;
            case CENTER -> center = comp;
        }
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        if (comp == north) north = null;
        else if (comp == south) south = null;
        else if (comp == east) east = null;
        else if (comp == west) west = null;
        else if (comp == center) center = null;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        // Get maximum width and total height
        if (north != null) {
            Dimension d = north.getPreferredSize();
            dim.width = Math.max(dim.width, d.width);
            dim.height += d.height + vgap;
        }
        if (south != null) {
            Dimension d = south.getPreferredSize();
            dim.width = Math.max(dim.width, d.width);
            dim.height += d.height + vgap;
        }

        // Add center and east/west components
        int centerWidth = 0;
        int centerHeight = 0;

        if (center != null) {
            Dimension d = center.getPreferredSize();
            centerWidth = d.width;
            centerHeight = d.height;
        }

        if (west != null) {
            Dimension d = west.getPreferredSize();
            dim.width += d.width + hgap;
            centerHeight = Math.max(centerHeight, d.height);
        }

        if (east != null) {
            Dimension d = east.getPreferredSize();
            dim.width += d.width + hgap;
            centerHeight = Math.max(centerHeight, d.height);
        }

        dim.width = Math.max(dim.width, centerWidth);
        dim.height += centerHeight;

        // Add insets
        return dim;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();

        // Handle north component
        int y = 0;
        if (north != null) {
            Dimension d = north.getPreferredSize();
            north.setBounds(0, 0, width, d.height);
            y = d.height + vgap;
        }

        // Handle south component
        int southHeight = 0;
        if (south != null) {
            Dimension d = south.getPreferredSize();
            southHeight = d.height;
            south.setBounds(0, height - southHeight, width, southHeight);
        }

        // Calculate remaining height for center, east, and west
        int remainingHeight = height - y - southHeight - (southHeight > 0 ? vgap : 0);

        // Handle west component
        int x = 0;
        if (west != null) {
            Dimension d = west.getPreferredSize();
            west.setBounds(0, y, d.width, remainingHeight);
            x = d.width + hgap;
        }

        // Handle east component
        int eastWidth = 0;
        if (east != null) {
            Dimension d = east.getPreferredSize();
            eastWidth = d.width;
            east.setBounds(width - eastWidth, y, eastWidth, remainingHeight);
        }

        // Handle center component
        if (center != null) {
            center.setBounds(x, y, width - x - eastWidth - (eastWidth > 0 ? hgap : 0), remainingHeight);
        }
    }

}
