package de.melanx.skyguis.util;

public class Math2 {

    public static boolean isInBounds(double x1, double y1, double w, double h, double x, double y) {
        return isInRect(x1, y1, x1 + w, y1 + h, x, y);
    }

    public static boolean isInRect(double x1, double y1, double x2, double y2, double x, double y) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
