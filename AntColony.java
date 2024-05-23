package vehicle.routing.system;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntColony {
    private int numAnts;
    private int numIterations;
    private List<Node> nodes;
    private Node startNode;
    private double alpha;
    private double beta;
    private double rho;
    private double Q;
    private double[][] distanceMatrix;

    // Constructor to initialize the ant colony optimization algorithm parameters
    public AntColony(int numAnts, int numIterations, List<int[]> coordinates, int[] startCoordinate,
            double alpha, double beta, double rho, double Q) {
        this.numAnts = numAnts;
        this.numIterations = numIterations;
        this.nodes = new ArrayList<>();
        this.startNode = new Node(startCoordinate);
        this.nodes.add(startNode);
        for (int[] coordinate : coordinates) {
           nodes.add(new Node(coordinate));
        }
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.Q = Q;
        this.distanceMatrix = calculateDistanceMatrix();
    }

    // Calculate the distance matrix based on node coordinates
    private double[][] calculateDistanceMatrix() {
        int numNodes = nodes.size();
        double[][] distanceMatrix = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                double distance = nodes.get(i).distanceTo(nodes.get(j));
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }
        return distanceMatrix;
    }

    // Run the ant colony optimization algorithm and return the best tour found
    public String run() {
        List<double[]> colony = new ArrayList<>();
        //System.out.println("Colony has been established at the following location(s): " + colony);
        double[][] pheromoneMatrix = new double[nodes.size()][nodes.size()];

        // Initialize pheromone matrix with default value of 1.0
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                pheromoneMatrix[i][j] = 1.0;
            }
        }
        //System.out.println("Initial Pheromone Matrix:");
        // printMatrix(pheromoneMatrix);

        List<int[]> bestTour = null;
        double bestLength = Double.POSITIVE_INFINITY;

        // Perform optimization for the specified number of iterations
        for (int iteration = 0; iteration < numIterations; iteration++) {
            //System.out.println("\nIteration: " + (iteration + 1));
            List<List<int[]>> antTours = new ArrayList<>();
            for (int ant = 0; ant < numAnts; ant++) {
                List<int[]> tour = generateAntTour(pheromoneMatrix);
                antTours.add(tour);
            }

            // Find the best tour among all ants in this iteration
            //System.out.println("\nAnt Tours:");
            for (int i = 0; i < antTours.size(); i++) {
                List<int[]> tour = antTours.get(i);
                double tourLength = calculateTourLength(tour);
                if (tourLength < bestLength) {
                    bestLength = tourLength;
                    bestTour = tour;
                }
            }

            // Update pheromones based on the ant tours
            pheromoneMatrix = updatePheromones(pheromoneMatrix, antTours);
        }

        return ("\nBest Tour: " + tourToString(bestTour));
    }

    // Generate a tour for an ant based on pheromone and distance information
    private List<int[]> generateAntTour(double[][] pheromoneMatrix) {
        int startNodeIndex = nodes.indexOf(startNode);
        if (startNodeIndex == -1) {
            throw new IllegalArgumentException("Start node not found in the nodes list.");
        }

        boolean[] visited = new boolean[nodes.size()];
        visited[startNodeIndex] = true;
        List<int[]> tour = new ArrayList<>();
        tour.add(convertNodeToIntArray(startNode));

        int currentNode = startNodeIndex;
        while (tour.size() < nodes.size()) {
            int nextNode = selectNextNode(currentNode, visited, pheromoneMatrix);
            tour.add(convertNodeToIntArray(nodes.get(nextNode)));
            visited[nextNode] = true;
            currentNode = nextNode;
        }

        // Add the start node again to complete the tour
        tour.add(convertNodeToIntArray(startNode));

        return tour;
    }

    // Convert a Node object to an integer array of coordinates
    private int[] convertNodeToIntArray(Node node) {
        int[] coordinates = node.getCoordinates();
        return new int[] {(int) coordinates[0], (int) coordinates[1]};
    }

    // Select the next node for an ant to visit based on probabilities
    private int selectNextNode(int currentNode, boolean[] visited, double[][] pheromoneMatrix) {
        double[] probabilities = calculateProbabilities(currentNode, visited, pheromoneMatrix);
        double rand = Math.random();
        double cumulativeProbability = 0;
        boolean allVisited = true;

        for (int i = 0; i < probabilities.length; i++) {
            if (!visited[i]) {
                allVisited = false;
                cumulativeProbability += probabilities[i];
                if (rand <= cumulativeProbability) {
                    return i;
                }
            }
        }

        if (allVisited) {
            throw new RuntimeException("All nodes have been visited.");
        }

        throw new RuntimeException("Should not reach here.");
    }

    // Calculate the probabilities of moving to each possible next node
    private double[] calculateProbabilities(int currentNode, boolean[] visited, double[][] pheromoneMatrix) {
        double[] pheromones = pheromoneMatrix[currentNode];
        double[] visibility = new double[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            if (!visited[i]) {
                visibility[i] = 1.0 / (distanceMatrix[currentNode][i] + 1e-6);
            }
        }
        double[] probabilities = new double[nodes.size()];
        double totalProbability = 0;
        for (int i = 0; i < nodes.size(); i++) {
            if (!visited[i]) {
                probabilities[i] = Math.pow(pheromones[i], alpha) * Math.pow(visibility[i], beta);
                totalProbability += probabilities[i];
            }
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (!visited[i]) {
                probabilities[i] /= totalProbability;
            }
        }
        return probabilities;
    }

    // Update the pheromone matrix based on the tours of all ants
    private double[][] updatePheromones(double[][] pheromoneMatrix, List<List<int[]>> antTours) {
        double evaporation = 1 - rho;
        for (int i = 0; i < pheromoneMatrix.length; i++) {
            for (int j = 0; j < pheromoneMatrix[i].length; j++) {
                pheromoneMatrix[i][j] *= evaporation;
            }
        }
        for (List<int[]> tour : antTours) {
            int tourLength = calculateTourLength(tour);
            for (int i = 0; i < tour.size() - 1; i++) {
                int nodeIndex1 = nodes.indexOf(tour.get(i));
                int nodeIndex2 = nodes.indexOf(tour.get(i + 1));
                if (nodeIndex1 != -1 && nodeIndex2 != -1) { // Check if nodes are found
                    pheromoneMatrix[nodeIndex1][nodeIndex2] += Q / tourLength;
                    pheromoneMatrix[nodeIndex2][nodeIndex1] += Q / tourLength;
                }
            }
        }
        return pheromoneMatrix;
    }

    // Calculate the total length of a tour
    private int calculateTourLength(List<int[]> tour) {
        int length = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            int[] node1 = tour.get(i);
            int[] node2 = tour.get(i + 1);
            length += Math.sqrt(Math.pow(node1[0] - node2[0], 2) + Math.pow(node1[1] - node2[1], 2));
        }
        return length;
    }

    // Print a matrix (for debugging purposes)
    private void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    // Convert a tour to a string representation
    private String tourToString(List<int[]> tour) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int[] coordinate : tour) {
            sb.append("(").append(coordinate[0]).append(", ").append(coordinate[1]).append(") ");
        }
        sb.append("]");
        return sb.toString();
    }
}

class Node {
    private int[] coordinates;

    // Constructor to initialize a Node with coordinates
    public Node(int[] coordinates) {
        this.coordinates = coordinates;
    }

    // Get the coordinates of the Node
    public int[] getCoordinates() {
        return coordinates;
    }

    // Calculate the distance to another Node
    public double distanceTo(Node other) {
        int[] otherCoordinates = other.getCoordinates();
        int deltaX = coordinates[0] - otherCoordinates[0];
        int deltaY = coordinates[1] - otherCoordinates[1];
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    // Override equals method to compare Nodes based on their coordinates
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Arrays.equals(coordinates, node.coordinates);
    }

    // Override hashCode method to generate a hash code based on coordinates
    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }
}
