package vehicle.routing.system;

import java.util.*;

public class Location {
    private int x; // X coordinate
    private int y; // Y coordinate
    private int index; // Index of the location

    // Constructor to initialize the location with coordinates and index
    public Location(int x, int y, int index) {
        this.x = x;
        this.y = y;
        this.index = index;
    }

    // Getter method for the X coordinate
    public int getX() {
        return x;
    }

    // Getter method for the Y coordinate
    public int getY() {
        return y;
    }

    // Getter method for the index
    public int getIndex() {
        return index;
    }
}
