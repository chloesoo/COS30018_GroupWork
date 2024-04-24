import numpy as np
import tkinter as tk
from tkinter import simpledialog
import socket

"""
class AntColony:
    def __init__(self, num_ants, num_iterations, nodes, start_node, alpha=1, beta=2, rho=0.5, Q=100):
        self.num_ants = num_ants
        self.num_iterations = num_iterations
        self.nodes = list(nodes)
        self.start_node = start_node
        self.nodes.append(start_node)
        self.num_nodes = len(self.nodes)
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
        start_node_index = self.nodes.index(self.start_node)
        visited = np.zeros(self.num_nodes, dtype=bool)  
        visited[start_node_index] = True
        tour = [self.start_node]  

        current_node = start_node_index
        for _ in range(self.num_nodes - 1):
            next_node = self.select_next_node(current_node, visited, pheromone_matrix)
            tour.append(self.nodes[next_node])
            visited[next_node] = True
            current_node = next_node

        tour.append(self.start_node)

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
        return length"""

class AntColonyGUI:
    def __init__(self, master):
        self.master = master
        self.canvas = tk.Canvas(master, width=500, height=500, bg='white')
        self.canvas.pack()
        self.cell_size = 10
        self.cells = [[0] * 50 for _ in range(50)]  # Initialize grid cells
        self.clicked_coordinates = []

        self.draw_warehouse()

        self.canvas.bind('<Button-1>', self.click_handler)

        self.button = tk.Button(master, text='Start Simulation', command=self.start_simulation)
        self.button.pack()

    def draw_warehouse(self):
        x0 = 25 * self.cell_size
        y0 = 25 * self.cell_size
        x1 = (25 + 1) * self.cell_size
        y1 = (25 + 1) * self.cell_size
        self.canvas.create_rectangle(x0, y0, x1, y1, fill='red', outline='red')

    def click_handler(self, event):
        x, y = event.x, event.y
        col = x // self.cell_size
        row = y // self.cell_size

        if (row, col) != (25, 25):
            if self.cells[row][col] == 0:
                self.cells[row][col] = 1  # Mark the cell as visited
                self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                             (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                             fill='black', outline='black')
                capacity = simpledialog.askinteger("Input", f"Enter capacity for coordinate ({row}, {col}):")
                if capacity is not None:
                    self.clicked_coordinates.append((row, col, capacity))
                else:
                    self.clicked_coordinates.append((row, col, 1))
            else:
                for coordinate in self.clicked_coordinates:
                    if (coordinate[0], coordinate[1]) == (row, col):
                        self.clicked_coordinates.remove(coordinate)
                        self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                fill='white', outline='white')
                    self.cells[row][col] = 0                

    def start_simulation(self):
        send_coordinates(self.clicked_coordinates)
        """
        with open("coordinates.txt", "w") as file:
            sendcoordinates
            
            for coordinates in self.clicked_coordinates:
                file.write(f"{coordinates[0]},{coordinates[1]},{coordinates[2]}\n")"""

        """
        num_ants = 10
        num_iterations = 5
        start_node = (25,25)
        region_A = []
        region_B = []
        region_C = []
        region_D = []

        for node in self.clicked_coordinates:
            if node[0] < 25:
                if node[1] < 25:
                    region_A.append(node)
                else:
                    region_B.append(node)
            else:
                if node[1] < 25:
                    region_C.append(node)
                else:
                    region_D.append(node)
                    
        colony = AntColony(num_ants, num_iterations, region_A, start_node)
        best_tour, best_length = colony.run()
        print("\nBest Tour:", best_tour)
        print("Best Tour Length:", best_length)

        colony = AntColony(num_ants, num_iterations, region_B, start_node)
        best_tour, best_length = colony.run()
        print("\nBest Tour:", best_tour)
        print("Best Tour Length:", best_length)
        
        colony = AntColony(num_ants, num_iterations, region_C, start_node)
        best_tour, best_length = colony.run()
        print("\nBest Tour:", best_tour)
        print("Best Tour Length:", best_length)

        colony = AntColony(num_ants, num_iterations, region_D, start_node)
        best_tour, best_length = colony.run()
        print("\nBest Tour:", best_tour)
        print("Best Tour Length:", best_length)
        """

def send_coordinates(coordinates):
    # Define the server address and port
    server_address = ('localhost', 12345)  # Change 'localhost' to your server IP if needed

    # Create a TCP/IP socket
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
        try:
            # Connect to the server
            client_socket.connect(server_address)

            # Send coordinates to the server
            for coordinate in coordinates:
                message = f"{(coordinate[0],coordinate[1],coordinate[2])}\n".encode()
                client_socket.sendall(message)
                print("Coordinates sent successfully!")

        except Exception as e:
            print("Error:", e)

def main():
    root = tk.Tk()
    root.title('Ant Colony Simulation')
    app = AntColonyGUI(root)
    root.mainloop()

if __name__ == "__main__":
    main()







    
