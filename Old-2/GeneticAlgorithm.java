package Project;

import java.util.*;

public class GeneticAlgorithm {
    private List<int[]> locations;
    private int startingCity;
    private int populationSize;
    private int maxGenerations;
    private double mutationRate;

    public GeneticAlgorithm(List<int[]> locations, int startingCity, int populationSize, int maxGenerations, double mutationRate) {
        this.locations = locations;
        this.startingCity = startingCity;
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.mutationRate = mutationRate;
    }

    public SalesmanGenome optimize() {
        List<SalesmanGenome> population = initialPopulation();
        SalesmanGenome overallBestTour = population.get(0); // Initialize with the first tour
        for (int generation = 1; generation <= maxGenerations; generation++) {
            population = evolvePopulation(population);
            Collections.sort(population);
            SalesmanGenome currentBest = population.get(0);
            if (currentBest.getFitness() < overallBestTour.getFitness()) {
                overallBestTour = currentBest; // Update the overall best tour if a better one is found
            }
            System.out.println("Generation " + generation + ": Best tour - Fitness: " + currentBest.getFitness());
            System.out.println("Path:");
            for (Location loc : currentBest.getPath()) {
                System.out.println("(" + loc.getX() + ", " + loc.getY() + ")");
            }
            System.out.println();
        }
        return overallBestTour;
    }

    private List<SalesmanGenome> initialPopulation() {
        List<SalesmanGenome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            List<Location> initialPath = new ArrayList<>();
            for (int[] coordinate : locations) {
                initialPath.add(new Location(coordinate[0], coordinate[1], initialPath.size()));
            }
            population.add(new SalesmanGenome(initialPath, startingCity));
        }
        return population;
    }

    private List<SalesmanGenome> evolvePopulation(List<SalesmanGenome> population) {
        List<SalesmanGenome> nextGeneration = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            SalesmanGenome parent1 = selectParent(population);
            SalesmanGenome parent2 = selectParent(population);
            SalesmanGenome child = crossover(parent1, parent2);
            mutate(child);
            nextGeneration.add(child);
        }
        return nextGeneration;
    }

    private SalesmanGenome selectParent(List<SalesmanGenome> population) {
        int totalFitness = population.stream().mapToInt(SalesmanGenome::getFitness).sum();
        int rand = new Random().nextInt(totalFitness);
        int sum = 0;
        for (SalesmanGenome genome : population) {
            sum += genome.getFitness();
            if (sum >= rand) {
                return genome;
            }
        }
        return null; // Should never happen
    }

    private SalesmanGenome crossover(SalesmanGenome parent1, SalesmanGenome parent2) {
        List<Location> childPath = new ArrayList<>();
        Set<Location> visited = new HashSet<>();
        for (Location loc : parent1.getPath()) {
            childPath.add(loc); // Reuse existing Location objects
            visited.add(loc);
        }
        for (Location loc : parent2.getPath()) {
            if (!visited.contains(loc)) {
                childPath.add(loc); // Reuse existing Location objects
            }
        }
        return new SalesmanGenome(childPath, startingCity);
    }

    private void mutate(SalesmanGenome genome) {
        if (Math.random() < mutationRate) {
            Collections.swap(genome.getPath(), new Random().nextInt(genome.getPath().size()),
                    new Random().nextInt(genome.getPath().size()));
            genome.updateFitness(startingCity);
        }
    }
}

class SalesmanGenome implements Comparable<SalesmanGenome> {
    private List<Location> path;
    private int fitness;

    public SalesmanGenome(List<Location> path, int startingCity) {
        this.path = path;
        updateFitness(startingCity);
    }

    public void updateFitness(int startingCity) {
        int totalDistance = 0;
        Location prevCity = null;
        for (Location loc : path) {
            if (prevCity != null) {
                totalDistance += calculateDistance(prevCity, loc);
            }
            prevCity = loc;
        }
        totalDistance += calculateDistance(prevCity, path.get(0)); // Add the distance back to the starting city
        fitness = totalDistance;
    }

    private int calculateDistance(Location loc1, Location loc2) {
        int deltaX = loc2.getX() - loc1.getX();
        int deltaY = loc2.getY() - loc1.getY();
        return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public List<Location> getPath() {
        return path;
    }

    public int getFitness() {
        return fitness;
    }

    @Override
    public int compareTo(SalesmanGenome other) {
        return Integer.compare(this.fitness, other.fitness);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nBest Tour: ");
        sb.append("[(25, 25) ");
        for (Location loc : path) {
            sb.append("(").append(loc.getX()).append(", ").append(loc.getY()).append(") ");
        }
        sb.append("(25, 25) ]");
        return sb.toString();
    }
}

class Location {
    private int x;
    private int y;
    private int index;

    public Location(int x, int y, int index) {
        this.x = x;
        this.y = y;
        this.index = index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getIndex() {
        return index;
    }
}
