import numpy as np
import tkinter as tk
from tkinter import simpledialog
import socket

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
                    self.clicked_coordinates.append((col, row, capacity))
                else:
                    self.clicked_coordinates.append((col, row, 1))
            else:
                for coordinate in self.clicked_coordinates:
                    if (coordinate[0], coordinate[1]) == (col, row):
                        self.clicked_coordinates.remove(coordinate)
                        self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                fill='white', outline='white')
                    self.cells[row][col] = 0                

    def start_simulation(self):
        send_coordinates(self.clicked_coordinates)

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
                message = f"{coordinate[0]},{coordinate[1]},{coordinate[2]}\n".encode()
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







    
