package vehicle.routing.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class ServerCommunicationAgent extends Agent {

    private String coordinateList1 = "";
    protected void setup() {
        System.out.println("Server agent " + getLocalName() + " is ready.");
        AID masterRoutingAgentAID = new AID("mra", AID.ISLOCALNAME);

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
                	coordinateList1 = "";
                    // Handle the connection in a separate thread
                    Thread clientHandlerThread = new Thread(() -> {
                        try {
                            // Read coordinates sent by the client
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String readCoordinates;
                            while ((readCoordinates = in.readLine()) != null) {
                                coordinateList1 += readCoordinates + "\n";
                            }

                            // Send coordinates to MasterRoutingAgent
                            sendCoordinatesToMaster(coordinateList1, masterRoutingAgentAID);

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
    
    private void sendCoordinatesToMaster(String coordinatesList, AID masterRoutingAgentAID) {
        // Create and send ACL message containing coordinates to MasterRoutingAgent
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(masterRoutingAgentAID);
        msg.setContent(coordinatesList); // Send coordinates as content
        send(msg);
    }
}
