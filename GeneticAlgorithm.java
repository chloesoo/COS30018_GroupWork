package vehicle.routing.system;

import java.util.*;

import vehicle.routing.system.GA_Location;
import vehicle.routing.system.GA_SalesmanGenome;


public class GeneticAlgorithm {
    private List<int[]> locations; // List of locations represented as coordinates
    private int startingCity; // Index of the starting city
    private int populationSize; // Size of the population
    private int maxGenerations; // Maximum number of generations
    private double mutationRate; // Mutation rate

    // Constructor to initialize the genetic algorithm parameters
    public GeneticAlgorithm(List<int[]> locations, int startingCity, int populationSize, int maxGenerations, double mutationRate) {
        this.locations = locations;
        this.startingCity = startingCity;
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.mutationRate = mutationRate;
    }

    // Method to run the optimization process and return the best solution
    public SalesmanGenome optimize() {
        List<SalesmanGenome> population = initialPopulation(); // Create initial population
        SalesmanGenome overallBestTour = population.get(0); // Initialize with the first tour

        for (int generation = 1; generation <= maxGenerations; generation++) {
            population = evolvePopulation(population); // Evolve the population
            Collections.sort(population); // Sort population by fitness
            SalesmanGenome currentBest = population.get(0); // Get the best genome in the current generation

            if (currentBest.getFitness() < overallBestTour.getFitness()) {
                overallBestTour = currentBest; // Update the overall best tour if a better one is found
            }

            // Print the best tour of the current generation
            // System.out.println("Generation " + generation + ": Best tour - Fitness: " + currentBest.getFitness());
            // System.out.println("Path:");
            // for (Location loc : currentBest.getPath()) {
            //     System.out.println("(" + loc.getX() + ", " + loc.getY() + ")");
            // }
            // System.out.println();
        }

        return overallBestTour; // Return the best tour found
    }

    // Method to create the initial population
    private List<SalesmanGenome> initialPopulation() {
        List<SalesmanGenome> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            List<Location> initialPath = new ArrayList<>();
            for (int[] coordinate : locations) {
                initialPath.add(new Location(coordinate[0], coordinate[1], initialPath.size())); // Add locations to path
            }
            population.add(new SalesmanGenome(initialPath, startingCity)); // Create new genome with the path
        }

        return population;
    }

    // Method to evolve the population to the next generation
    private List<SalesmanGenome> evolvePopulation(List<SalesmanGenome> population) {
        List<SalesmanGenome> nextGeneration = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            SalesmanGenome parent1 = selectParent(population); // Select first parent
            SalesmanGenome parent2 = selectParent(population); // Select second parent
            SalesmanGenome child = crossover(parent1, parent2); // Create child through crossover
            mutate(child); // Mutate the child
            nextGeneration.add(child); // Add child to next generation
        }

        return nextGeneration;
    }

    // Method to select a parent for crossover based on fitness proportionate selection
    private SalesmanGenome selectParent(List<SalesmanGenome> population) {
        int totalFitness = population.stream().mapToInt(SalesmanGenome::getFitness).sum(); // Calculate total fitness
        int rand = new Random().nextInt(totalFitness); // Generate a random number
        int sum = 0;

        for (SalesmanGenome genome : population) {
            sum += genome.getFitness();
            if (sum >= rand) {
                return genome; // Return genome when sum exceeds random number
            }
        }

        return null; // Should never happen
    }

    // Method to perform crossover between two parents to create a child
    private SalesmanGenome crossover(SalesmanGenome parent1, SalesmanGenome parent2) {
        List<Location> childPath = new ArrayList<>();
        Set<Location> visited = new HashSet<>();

        for (Location loc : parent1.getPath()) {
            childPath.add(loc); // Add locations from parent1 to child path
            visited.add(loc); // Mark location as visited
        }

        for (Location loc : parent2.getPath()) {
            if (!visited.contains(loc)) {
                childPath.add(loc); // Add locations from parent2 that are not already visited
            }
        }

        return new SalesmanGenome(childPath, startingCity); // Create new child genome
    }

    // Method to mutate a genome by swapping two random locations
    private void mutate(SalesmanGenome genome) {
        if (Math.random() < mutationRate) {
            Collections.swap(genome.getPath(), new Random().nextInt(genome.getPath().size()), new Random().nextInt(genome.getPath().size()));
            genome.updateFitness(startingCity); // Update fitness after mutation
        }
    }
}