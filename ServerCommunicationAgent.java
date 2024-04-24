package vehicle.routing.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import jade.core.Agent;

public class ServerCommunicationAgent extends Agent {
    
    protected void setup() {
        System.out.println("Server agent " + getLocalName() + " is ready.");

        // Create a new thread to handle the server logic
        Thread serverThread = new Thread(() -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(12345); // Use the same port as the Python client

                System.out.println("Waiting for connections...");

                while (true) {
                    // Accept incoming connection
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection established with: " + clientSocket.getInetAddress().getHostAddress());

                    // Handle the connection in a separate thread
                    Thread clientHandlerThread = new Thread(() -> {
                        try {
                            // Read coordinates sent by the client
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String coordinates;
                            while ((coordinates = in.readLine()) != null) {
                                System.out.println("Received coordinates: " + coordinates);

                                // Add your responder behavior here
                            }

                            // Close the connection
                            in.close();
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    clientHandlerThread.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start the server thread
        serverThread.start();
    }
}
