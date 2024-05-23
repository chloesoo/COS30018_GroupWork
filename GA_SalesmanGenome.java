package vehicle.routing.system;

import java.util.List;

public class SalesmanGenome implements Comparable<SalesmanGenome> {
    private List<Location> path; // Path representing the tour
    private int fitness; // Fitness value of the tour

    // Constructor to initialize the genome with a path and calculate its fitness
    public SalesmanGenome(List<Location> path, int startingCity) {
        this.path = path;
        updateFitness(startingCity); // Calculate initial fitness
    }

    // Method to update the fitness of the genome
    public void updateFitness(int startingCity) {
        int totalDistance = 0;
        Location prevCity = null;

        for (Location loc : path) {
            if (prevCity != null) {
                totalDistance += calculateDistance(prevCity, loc); // Calculate distance between cities
            }
            prevCity = loc;
        }

        totalDistance += calculateDistance(prevCity, path.get(0)); // Add the distance back to the starting city
        fitness = totalDistance; // Set fitness as total distance
    }

    // Method to calculate the distance between two locations
    private int calculateDistance(Location loc1, Location loc2) {
        int deltaX = loc2.getX() - loc1.getX();
        int deltaY = loc2.getY() - loc1.getY();
        return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY); // Calculate Euclidean distance
    }

    // Getter method for the path
    public List<Location> getPath() {
        return path;
    }

    // Getter method for the fitness
    public int getFitness() {
        return fitness;
    }

    // Method to compare genomes based on fitness
    @Override
    public int compareTo(SalesmanGenome other) {
        return Integer.compare(this.fitness, other.fitness);
    }
    
    // Method to return the string representation of the best tour
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nBest Tour: ");
        sb.append("[(25, 25) "); // Assuming (25, 25) is the starting city
        for (Location loc : path) {
            sb.append("(").append(loc.getX()).append(", ").append(loc.getY()).append(") ");
        }
        sb.append("(25, 25) ]"); // Assuming (25, 25) is the ending city
        return sb.toString();
    }
}