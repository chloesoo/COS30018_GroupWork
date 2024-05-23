import numpy as np
import tkinter as tk
from tkinter import simpledialog, messagebox
import socket
import re
import os

class vehicleRoutingGUI:
    def __init__(self, master):
        self.master = master
        self.canvas = tk.Canvas(master, width=500, height=500, bg='white')
        self.canvas.pack()
        self.cell_size = 10
        self.cells = [[0] * 50 for _ in range(50)]  # Initialize grid cells to zero
        self.clicked_coordinates = []  # List to store clicked coordinates with capacities
        self.coordinates_list = []  # List to store coordinates read from the file

        self.draw_warehouse()

        # Bind mouse click and hover events to handlers
        self.canvas.bind('<Button-1>', self.click_handler)
        self.canvas.bind('<Motion>', self.hover_handler)

        # Information label
        self.info_label = tk.Label(master, text="Hover over a tile to see its coordinates")
        self.info_label.pack()

        # Start Simulation button
        self.button = tk.Button(master, text='Start Simulation', command=self.start_simulation)
        self.button.pack()

        # Bind hover events to warehouse square
        self.canvas.tag_bind("warehouse", '<Enter>', self.hover_enter_warehouse)
        self.canvas.tag_bind("warehouse", '<Leave>', self.hover_leave_warehouse)

        # Initialize the file reading timer
        self.read_file_timer()

        # Initialize region capacities
        self.region_A_capacity = 20
        self.region_B_capacity = 20
        self.region_C_capacity = 20
        self.region_D_capacity = 20

    def hover_handler(self, event):
        """Handles mouse hover events to display coordinates and capacities."""
        x, y = event.x, event.y
        col = x // self.cell_size
        row = y // self.cell_size

        # Find capacity of the hovered coordinate
        capacity = 0
        for coordinate in self.clicked_coordinates:
            if (coordinate[0], coordinate[1]) == (col, row):
                capacity = coordinate[2]
                break

        self.info_label.config(text=f"Column: {col}, Row: {row}, Capacity: {capacity}")

    def hover_enter_warehouse(self, event):
        """Change warehouse color on hover."""
        self.canvas.itemconfig("warehouse", fill="magenta", outline="magenta")

    def hover_leave_warehouse(self, event):
        """Revert warehouse color when hover ends."""
        self.canvas.itemconfig("warehouse", fill="red", outline="red")

    def draw_warehouse(self):
        """Draw the warehouse as a red square in the center of the grid."""
        x0 = 25 * self.cell_size
        y0 = 25 * self.cell_size
        x1 = (25 + 1) * self.cell_size
        y1 = (25 + 1) * self.cell_size
        self.canvas.create_rectangle(x0, y0, x1, y1, fill='red', outline='red', tags="warehouse")

    def click_handler(self, event):
        """Handles mouse click events to mark coordinates and set capacities."""
        x, y = event.x, event.y
        col = x // self.cell_size
        row = y // self.cell_size

        if (row, col) != (25, 25):
            # If the cell is not the warehouse
            if self.cells[row][col] == 0:
                self.cells[row][col] = 1  # Mark the cell as visited
                self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                             (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                             fill='black', outline='black')

                # Ask for capacity input
                capacity = simpledialog.askinteger("Input", f"Enter capacity for coordinate ({row}, {col}):")
                insufficient = False
                if capacity is not None:
                    if capacity != 0:
                        if row < 25:
                            if col < 25:
                                # Region A
                                if self.region_A_capacity - capacity >= 0:
                                    self.region_A_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_A_capacity))
                                    insufficient = True
                            else:
                                # Region D
                                if self.region_D_capacity - capacity >= 0:
                                    self.region_D_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_D_capacity))
                                    insufficient = True
                        elif row >= 25:
                            if col < 25:
                                # Region B
                                if self.region_B_capacity - capacity >= 0:
                                    self.region_B_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
                                else:
                                    messagebox.showerror("Error", "Remaining capacity: " + str(self.region_B_capacity))
                                    insufficient = True
                            else:
                                # Region C
                                if self.region_C_capacity - capacity >= 0:
                                    self.region_C_capacity -= capacity
                                    self.clicked_coordinates.append((col, row, capacity))
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
                            # Region A
                            if self.region_A_capacity - 1 >= 0:
                                self.region_A_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_A_capacity))
                                insufficient = True
                        else:
                            # Region D
                            if self.region_D_capacity - 1 >= 0:
                                self.region_D_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_D_capacity))
                                insufficient = True
                    elif row > 25:
                        if col < 25:
                            # Region B
                            if self.region_B_capacity - 1 >= 0:
                                self.region_B_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_B_capacity))
                                insufficient = True
                        else:
                            # Region C
                            if self.region_C_capacity - 1 >= 0:
                                self.region_C_capacity -= 1
                                self.clicked_coordinates.append((col, row, 1))
                            else:
                                messagebox.showerror("Error", "Remaining capacity: " + str(self.region_C_capacity))
                                insufficient = True

                if insufficient:
                    # Reset cell color if capacity is insufficient
                    self.canvas.create_rectangle(col * self.cell_size, row * self.cell_size,
                                                 (col + 1) * self.cell_size, (row + 1) * self.cell_size,
                                                 fill='white', outline='white')
            else:
                # Unmark the cell if clicked again
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
            # Reset everything if the warehouse is clicked
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
        """Draw rectangles for clicked coordinates."""
        for coordinate in self.clicked_coordinates:
            self.canvas.create_rectangle(coordinate[0] * self.cell_size, coordinate[1] * self.cell_size,
                                         (coordinate[0] + 1) * self.cell_size, (coordinate[1] + 1) * self.cell_size,
                                         fill='black', outline='black')

    def start_simulation(self):
        """Start the simulation by sending coordinates to the server."""
        self.send_coordinates()
        #self.clicked_coordinates.clear()  # Uncomment if you want to clear after sending

    def draw_lines(self):
        """Draw lines between coordinates based on the data from the file."""
        self.canvas.delete("route_line")
        colours = ['blue', 'red', 'green', 'orange', 'purple', 'cyan', 'magenta', 'yellow', 'brown', 'pink'] 

        for i, coordinates in enumerate(self.coordinates_list):
            if len(coordinates) < 2:
                continue

            colour = colours[i % len(colours)]

            for j in range(len(coordinates) - 1):
                start_point = coordinates[j]
                end_point = coordinates[j + 1]
                self.canvas.create_line((start_point[0] + 0.5) * self.cell_size, (start_point[1] + 0.5) * self.cell_size,
                                        (end_point[0] + 0.5) * self.cell_size, (end_point[1] + 0.5) * self.cell_size,
                                        fill=colour, width=2, tags="route_line")

    def read_file_timer(self):
        """Schedule the file reading operation."""
        self.read_file()
        self.master.after(1000, self.read_file_timer)  # Schedule next read after 1 second

    def read_file(self):
        """Read coordinates from the file and process them."""
        with open("C:\\Vehicle Routing System\\data.txt", "r") as file:
            data = file.read()
            matches = re.findall(r'\[([\s\S]*?)\]', data)

        self.coordinates_list = []  # Reset the coordinates list

        for match in matches:
            # Extract coordinate tuples from the match
            coordinates = re.findall(r'\((\d+), (\d+)\)', match)
            # Convert to list of integer tuples and append
            self.coordinates_list.append([(int(x), int(y)) for x, y in coordinates])

        self.draw_lines()
        self.draw_warehouse()
        self.draw_coordinates()

    def send_coordinates(self):
        """Send clicked coordinates to the server."""
        message = ""
        server_address = ('localhost', 12345)  # Server address and port

        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
            try:
                # Connect to the server
                client_socket.connect(server_address)

                # Format and send coordinates
                for coordinate in self.clicked_coordinates:
                    message += f"{coordinate[0]},{coordinate[1]},{coordinate[2]}\n"

                client_socket.sendall(message.encode())
                print("Coordinates sent successfully!")

            except Exception as e:
                print("Error:", e)

            finally:
                client_socket.close()  # Close the socket

def main():
    """Main function to set up and start the application."""
    os.makedirs(os.path.dirname("C:\\Vehicle Routing System\\data.txt"), exist_ok=True)
    with open("C:\\Vehicle Routing System\\data.txt", "w") as file:
        pass  # Create an empty data file if not exists
    root = tk.Tk()
    root.title('Vehicle Routing System : Location Picker')
    app = vehicleRoutingGUI(root)
    root.mainloop()

if __name__ == "__main__":
    main()
