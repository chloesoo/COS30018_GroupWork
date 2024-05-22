import numpy as np
import tkinter as tk
from tkinter import simpledialog, messagebox
import socket
import re
import os

class AntColonyGUI:
    def __init__(self, master):
        self.master = master
        self.canvas = tk.Canvas(master, width=500, height=500, bg='white')
        self.canvas.pack()
        self.cell_size = 10
        self.cells = [[0] * 50 for _ in range(50)]  # Initialize grid cells
        self.clicked_coordinates = []
        self.coordinates_list = []

        self.draw_warehouse()

        self.canvas.bind('<Button-1>', self.click_handler)
        self.canvas.bind('<Motion>', self.hover_handler)

        self.info_label = tk.Label(master, text="Hover over a tile to see its coordinates")
        self.info_label.pack()

        self.button = tk.Button(master, text='Start Simulation', command=self.start_simulation)
        self.button.pack()
        
        self.canvas.tag_bind("warehouse", '<Enter>', self.hover_enter_warehouse)
        self.canvas.tag_bind("warehouse", '<Leave>', self.hover_leave_warehouse)

        self.read_file_timer()
        self.region_A_capacity = 20
        self.region_B_capacity = 20
        self.region_C_capacity = 20
        self.region_D_capacity = 20
        
    def hover_handler(self, event):
        x, y = event.x, event.y
        col = x // self.cell_size
        row = y // self.cell_size

        capacity = 0
        for coordinate in self.clicked_coordinates:
            if (coordinate[0], coordinate[1]) == (col, row):
                capacity = coordinate[2]
                break
            
        self.info_label.config(text=f"Column: {col}, Row: {row}, Capacity: {capacity}")
        
    def hover_enter_warehouse(self, event):
        self.canvas.itemconfig("warehouse", fill="magenta", outline="magenta")

    def hover_leave_warehouse(self, event):
        self.canvas.itemconfig("warehouse", fill="red", outline="red")
        
    def draw_warehouse(self):
        x0 = 25 * self.cell_size
        y0 = 25 * self.cell_size
        x1 = (25 + 1) * self.cell_size
        y1 = (25 + 1) * self.cell_size
        self.canvas.create_rectangle(x0, y0, x1, y1, fill='red', outline='red', tags="warehouse")

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
                insufficient = False
                if capacity is not None:
                    if capacity != 0:
                        if row < 25:
                            if col < 25:
                                if self.region_A_capacity - capacity >= 0:
                                    self.region_A_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                    insufficient = False
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_A_capacity))
                                    insufficient = True
                            else:
                                if self.region_D_capacity - capacity >= 0:
                                    self.region_D_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                    insufficient = False
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_D_capacity))
                                    insufficient = True
                        elif row >= 25:
                            if col < 25:
                                if self.region_B_capacity - capacity >= 0:
                                    self.region_B_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                    insufficient = False
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_B_capacity))
                                    insufficient = True
                            else:
                                if self.region_C_capacity - capacity >= 0:
                                    self.region_C_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                    insufficient = False
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_C_capacity))
                                    insufficient = True
                    else:
                        messagebox.showerror("Error", "Zero capacity error")
                        self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                fill='white', outline='white')
                else:
                    if row < 25:
                        if col < 25:
                            if self.region_A_capacity - 1 >= 0:
                                self.region_A_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                                insufficient = False
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_A_capacity))
                                insufficient = True
                        else:
                            if self.region_D_capacity - 1 >= 0:
                                self.region_D_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                                insufficient = False
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_D_capacity))
                                insufficient = True
                    elif row > 25:
                        if col < 25:
                            if self.region_B_capacity - 1 >= 0:
                                self.region_B_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                                insufficient = False
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_B_capacity))
                                insufficient = True
                        else:
                            if self.region_C_capacity - 1 >= 0:
                                self.region_C_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                                insufficient = False
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_C_capacity))
                                insufficient = True

                if insufficient:
                    self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                            (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                            fill='white', outline='white')
            else:
                for coordinate in self.clicked_coordinates:
                    if (coordinate[0], coordinate[1]) == (col, row):
                        self.clicked_coordinates.remove(coordinate)
                        self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                fill='white', outline='white')
                        if coordinate[1] < 25:
                            if coordinate[0] < 25:
                                self.region_A_capacity += coordinate[2]
                            else:
                                self.region_D_capacity += coordinate[2]
                        else:
                            if coordinate[0] < 25:
                                self.region_B_capacity += coordinate[2]
                            else:
                                self.region_C_capacity += coordinate[2]
                                
                    self.cells[row][col] = 0
                    
        else:
            # Reset everything
            self.canvas.delete("route_line")
            for coordinate in self.clicked_coordinates:
                self.canvas.create_rectangle(coordinate[0] * self.cell_size, coordinate[1] * self.cell_size,
                        (coordinate[0] + 1) * self.cell_size, (coordinate[1] + 1) * self.cell_size,
                        fill='white', outline='white')
            self.clicked_coordinates.clear()
            self.region_A_capacity = 20
            self.region_B_capacity = 20
            self.region_C_capacity = 20
            self.region_D_capacity = 20
            self.cells = [[0] * 50 for _ in range(50)]
            with open("C:\\Vehicle Routing System\\data.txt", "w") as file:
                pass

    def draw_coordinates(self):
        for coordinate in self.clicked_coordinates:
                self.canvas.create_rectangle(coordinate[0] * self.cell_size, coordinate[1] * self.cell_size,
                                             (coordinate[0] + 1) * self.cell_size, (coordinate[1] + 1) * self.cell_size,
                                             fill='black', outline='black')
    
    def start_simulation(self):
        self.send_coordinates()
        #self.clicked_coordinates.clear()
    
    def draw_lines(self):
        self.canvas.delete("route_line")
        colours = ['blue', 'red', 'green', 'orange', 'purple', 'cyan', 'magenta', 'yellow', 'brown', 'pink'] 
        
        for i, coordinates in enumerate(self.coordinates_list):
            if len(coordinates) < 2:
                continue

            colour = colours[i % len(colours)] 
            
            for i in range(len(coordinates) - 1):
                start_point = coordinates[i]
                end_point = coordinates[i + 1]
                self.canvas.create_line((start_point[0] + 0.5) * self.cell_size, (start_point[1] + 0.5) * self.cell_size,
                                        (end_point[0] + 0.5) * self.cell_size, (end_point[1] + 0.5) * self.cell_size,
                                        fill=colour, width=2, tags="route_line")
            
    def read_file_timer(self):
        # Read the file initially
        self.read_file()

        # Schedule the next reading after 1 second
        self.master.after(1000, self.read_file_timer)
    
    def read_file(self):
        # Read the file and process the data
        with open("C:\\Vehicle Routing System\\data.txt", "r") as file:
            data = file.read()
            matches = re.findall(r'\[([\s\S]*?)\]', data)

        # Initialize a list to store the coordinates
        self.coordinates_list = []

        # Iterate over each match
        for match in matches:
            # Extract coordinate tuples from the match
            coordinates = re.findall(r'\((\d+), (\d+)\)', match)
            # Convert coordinates to list of integer tuples and append to the coordinates_list
            self.coordinates_list.append([(int(x), int(y)) for x, y in coordinates])

        self.draw_lines()
        self.draw_warehouse()
        self.draw_coordinates()

    def send_coordinates(self):
        message = ""
        # Define the server address and port
        server_address = ('localhost', 12345)

        # Create a TCP/IP socket
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
            try:
                # Connect to the server
                client_socket.connect(server_address)

                # Send coordinates to the server
                for coordinate in self.clicked_coordinates:
                    message += f"{coordinate[0]},{coordinate[1]},{coordinate[2]}\n"

                client_socket.sendall(message.encode())
                print("Coordinates sent successfully!")
                
            except Exception as e:
                print("Error:", e)

            finally:
                # Close the socket connection
                client_socket.close()         

def main():
    os.makedirs(os.path.dirname("C:\\Vehicle Routing System\\data.txt"), exist_ok=True)
    with open("C:\\Vehicle Routing System\\data.txt", "w") as file:
        pass 
    root = tk.Tk()
    root.title('Ant Colony Simulation')
    app = AntColonyGUI(root)
    root.mainloop()

if __name__ == "__main__":
    main()







    
