import numpy as np

class AntColony:
    def __init__(self, num_ants, num_iterations, nodes, alpha=1, beta=2, rho=0.5, Q=100):
        self.num_ants = num_ants
        self.num_iterations = num_iterations
        self.nodes = nodes
        self.num_nodes = len(nodes)
        self.alpha = alpha
        self.beta = beta
        self.rho = rho
        self.Q = Q
        self.distance_matrix = self.calculate_distance_matrix()

    def calculate_distance_matrix(self):
        distance_matrix = np.zeros((self.num_nodes, self.num_nodes))
        for i in range(self.num_nodes):
            for j in range(i+1, self.num_nodes):
                node1 = self.nodes[i]
                node2 = self.nodes[j]
                distance = np.sqrt((node1[0] - node2[0])**2 + (node1[1] - node2[1])**2)
                distance_matrix[i, j] = distance
                distance_matrix[j, i] = distance
        return distance_matrix

    def run(self):
        colony = []  # Colony starts empty
        print("Colony has been established at the following location(s):", colony)

        pheromone_matrix = np.ones((self.num_nodes, self.num_nodes))  # Initialize pheromone matrix
        print("Initial Pheromone Matrix:")
        print(pheromone_matrix)

        best_tour = None
        best_length = np.inf

        for iteration in range(self.num_iterations):
            print("\nIteration:", iteration + 1)
            ant_tours = []
            for ant in range(self.num_ants):
                tour = self.generate_ant_tour(pheromone_matrix)
                ant_tours.append(tour)

            print("\nAnt Tours:")
            for i, tour in enumerate(ant_tours):
                print("Ant", i + 1, "Tour:", tour)
                tour_length = self.calculate_tour_length(tour)
                if tour_length < best_length:
                    best_length = tour_length
                    best_tour = tour

            pheromone_matrix = self.update_pheromones(pheromone_matrix, ant_tours)

        return best_tour, best_length

    def generate_ant_tour(self, pheromone_matrix):
        start_node = np.random.randint(self.num_nodes)
        visited = np.zeros(self.num_nodes, dtype=bool)  # Changed to use NumPy array
        visited[start_node] = True
        tour = [self.nodes[start_node]]

        current_node = start_node
        for _ in range(self.num_nodes - 1):
            next_node = self.select_next_node(current_node, visited, pheromone_matrix)
            tour.append(self.nodes[next_node])
            visited[next_node] = True
            current_node = next_node

        return tour

    def select_next_node(self, current_node, visited, pheromone_matrix):
        probabilities = self.calculate_probabilities(current_node, visited, pheromone_matrix)
        next_node = np.random.choice(np.arange(self.num_nodes)[~visited], p=probabilities[~visited])  # Changed to use NumPy array operations
        return next_node

    def calculate_probabilities(self, current_node, visited, pheromone_matrix):
        pheromones = pheromone_matrix[current_node]
        visibility = 1 / (self.distance_matrix[current_node] + 1e-6)  # Add small value to avoid division by zero
        probabilities = np.zeros_like(pheromones)
        probabilities[~visited] = (pheromones[~visited] ** self.alpha) * (visibility[~visited] ** self.beta)
        probabilities /= probabilities.sum()
        return probabilities

    def update_pheromones(self, pheromone_matrix, ant_tours):
        pheromone_matrix *= (1 - self.rho)  # Evaporation
        for tour in ant_tours:
            tour_length = self.calculate_tour_length(tour)
            for i in range(len(tour) - 1):
                pheromone_matrix[self.nodes.index(tour[i]), self.nodes.index(tour[i+1])] += self.Q / tour_length
        return pheromone_matrix

    def calculate_tour_length(self, tour):
        length = 0
        for i in range(len(tour) - 1):
            node1 = tour[i]
            node2 = tour[i + 1]
            length += np.sqrt((node1[0] - node2[0])**2 + (node1[1] - node2[1])**2)
        return length

# Example usage:
if __name__ == "__main__":
    num_ants = 10
    num_iterations = 5
    nodes = [(0, 0), (1, 2), (3, 1), (2, 3), (4, 0)]

    print("Nodes (Food Sources):")
    print(nodes)

    colony = AntColony(num_ants, num_iterations, nodes)
    best_tour, best_length = colony.run()
    print("\nBest Tour:", best_tour)
    print("Best Tour Length:", best_length)