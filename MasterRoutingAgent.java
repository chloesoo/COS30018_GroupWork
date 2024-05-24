package vehicle.routing.system;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPANames;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vehicle.routing.system.AntColony;
import vehicle.routing.system.GeneticAlgorithm;


public class MasterRoutingAgent extends Agent {
    // Number of responders
    private int nResponders;
    // List to store coordinates
    private List<List<Integer>> coordinatesList = new ArrayList<>(); 
    // Starting coordinates
    private int[] start = new int[]{25, 25};
    // Routes for each region
    private List<String> routesA = new ArrayList<>();
    private List<String> routesB = new ArrayList<>();
    private List<String> routesC = new ArrayList<>();
    private List<String> routesD = new ArrayList<>();
    // Capacities for each region
    private int CapacityA;
    private int CapacityB;
    private int CapacityC;
    private int CapacityD;
    // Constants for regions
    private int k=4;
    private int m=0;
    // Counter for agents
    private static int agentCounter = 0;
    // String to store routes
    private String routesInString = "";
  
    protected void setup() {
        // Read names of responders as arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            nResponders = args.length;
            System.out.println("Requesting dummy action to " + nResponders + " responders.");

            // Create a REQUEST message
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for (int i = 0; i < args.length; ++i) {
                // Add receivers
                msg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
            }

            // Set the interaction protocol
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

            // Specify the reply deadline (10 seconds)
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

            // Set message content
            msg.setContent("What is your capacity?");
            send(msg);
            
            long startTime = System.currentTimeMillis();
            long timeout = 10000; // Timeout in milliseconds (e.g., 10 seconds)
            
            // Continuously listen for capacity messages until all capacities are received or timeout occurs
			while(true) {
				// Receive capacity messages
				ACLMessage Capacitymsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				// Process received capacity messages
				if (Capacitymsg != null) {
					// Check if the message content indicates capacity information
					if (Capacitymsg.getContent().startsWith("My capacity is: ")) {
						// Extract the capacity value from the message content
						String Capacity = Capacitymsg.getContent().substring("My capacity is: ".length());
						// Print the received capacity and sender information
						System.out.println("Received capacity from " + Capacitymsg.getSender().getName() + " : " + Capacity);
						// Convert capacity value to integer
						int intCapacity = Integer.parseInt(Capacity);
						// Assign the capacity value to the corresponding region based on sender's name
						if(Capacitymsg.getSender().getName().startsWith("da1")) {
							CapacityA = intCapacity;
						}
						else if(Capacitymsg.getSender().getName().startsWith("da2")){
							CapacityB = intCapacity;
						}
						else if(Capacitymsg.getSender().getName().startsWith("da3")){
							CapacityC = intCapacity;
						}
						else {
							CapacityD = intCapacity;
						}
					}
					// Print the capacities for debugging
					System.out.println(CapacityA);
					System.out.println(CapacityB);
					System.out.println(CapacityC);
					System.out.println(CapacityD);
					
				}
				// Check if all capacities are received and break the loop
				else if(CapacityA != 0 && CapacityB != 0 && CapacityC != 0 && CapacityD != 0) {
					break;
				}
				// Check for timeout and break the loop if timeout occurs
				else if (System.currentTimeMillis() - startTime > timeout) {
					System.out.println("Timeout: Not all capacities received.");
					break; // Exit the while loop
				}
				else {
					//block();
				}
			}

            //send(requestDeliveryMsg);
            if(CapacityA != 0 && CapacityB != 0 && CapacityC != 0 && CapacityD != 0) {
				while(true) {
					m=0;

					// Wait for input from user	
					System.out.println("Agent " + getLocalName() + ": waiting for REQUEST message...");
					ACLMessage trigger = blockingReceive(MessageTemplate.or(
							MessageTemplate.MatchPerformative(ACLMessage.CFP),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
			
					AID server = new AID("server", AID.ISLOCALNAME);
					if (trigger.getSender().getName().equals(server.getName())) {
						// Check if the content of the message contains coordinates
						String content = trigger.getContent();
						String[] lines = content.split("\n");
						coordinatesList.clear();
					// Iterate over each line and split the coordinates by commas
						for (String line : lines) {
							// Split the line by commas to get individual coordinate strings
							String[] coordinatesString = line.split(",");

							// Create a list to store the integer coordinates
							List<Integer> coordinates = new ArrayList<>();

							// Parse each coordinate string into an integer and add it to the list
							for (String coordinateString : coordinatesString) {
								try {
									coordinates.add(Integer.parseInt(coordinateString));
								}catch (NumberFormatException e) {
									System.err.println("Error parsing coordinate string: " + coordinateString);
								}
							}

							// Add the list of coordinates to the main list
							coordinatesList.add(coordinates);
						}

						for (List<Integer> coordinates : coordinatesList) {
							// Print each sublist as a comma-separated string
							for (int i = 0; i < coordinates.size(); i++) {
								System.out.print(coordinates.get(i));
								// Add a comma after each coordinate except the last one
								if (i < coordinates.size() - 1) {
									System.out.print(",");
								}
							}
							// Add a new line after printing each sublist
							System.out.println();
						}
						
						List<int[]>  coordinateList = new ArrayList<>();
						for (List<Integer> list : coordinatesList) {
							int[] array = new int[list.size()]; // Create an array with the same size as the list
							// Convert each element of the list to the corresponding element in the array
							for (int i = 0; i < list.size(); i++) {
								array[i] = list.get(i);
							}
							coordinateList.add(array);
						}
						
						// Define capacity thresholds for each region based on received capacities
						int capacityThresholdA = CapacityA; 
						int capacityThresholdB = CapacityB;
						int capacityThresholdC = CapacityC;
						int capacityThresholdD = CapacityD;

						// Initialize lists to store coordinates for each region
						List<int[]> region_A = new ArrayList<>();
						List<int[]> region_B = new ArrayList<>();
						List<int[]> region_C = new ArrayList<>();
						List<int[]> region_D = new ArrayList<>();

						// Clear existing data from region lists
						region_A.clear();
						region_B.clear();
						region_C.clear();
						region_D.clear();

						// Assign coordinates to respective regions based on their position
						for(int[] array: coordinateList) {
							if (array[0] < 25) {
								if(array[1] < 25) {
									region_A.add(array); // Coordinates in region A
								}
								else {
									region_D.add(array); // Coordinates in region D
								}
							}
							else {
								if(array[1] < 25) {
									region_B.add(array); // Coordinates in region B
								}
								else {
									region_C.add(array); // Coordinates in region C
								}
							}
						}

						// Split coordinates of each region into routes based on capacity threshold
						List<List<int[]>> sortedRegion_A = splitCoordinatesIntoRoutes(region_A, capacityThresholdA);
						List<List<int[]>> sortedRegion_B = splitCoordinatesIntoRoutes(region_B, capacityThresholdB);
						List<List<int[]>> sortedRegion_C = splitCoordinatesIntoRoutes(region_C, capacityThresholdC);
						List<List<int[]>> sortedRegion_D = splitCoordinatesIntoRoutes(region_D, capacityThresholdD);

						// Clear existing route lists
						routesA.clear();
						routesB.clear();
						routesC.clear();
						routesD.clear();

						// Process sorted routes for region A
						if (!sortedRegion_A.isEmpty()) {
							if(sortedRegion_A.size() == 2) {
								// Optimize routes using either Ant Colony Optimization or Genetic Algorithm based on route size
								if(sortedRegion_A.get(0).size() == 1 || sortedRegion_A.get(1).size() == 1) {
									routesA.add(AntColonyOptimization(sortedRegion_A.get(0), start));
									routesA.add(AntColonyOptimization(sortedRegion_A.get(1), start));
								} else {
									routesA.add(GeneticAlgorithmOptimization(sortedRegion_A.get(0), start));
									routesA.add(GeneticAlgorithmOptimization(sortedRegion_A.get(1), start));
								}
							} else {
								// Optimize routes for single route
								if(sortedRegion_A.get(0).size() == 1) {
									routesA.add(AntColonyOptimization(sortedRegion_A.get(0), start));
								} else {
									routesA.add(GeneticAlgorithmOptimization(sortedRegion_A.get(0), start));
								}
							}
						} 

						// Process sorted routes for region B
						if (!sortedRegion_B.isEmpty()) {
							if(sortedRegion_B.size() == 2) {
								routesB.add(AntColonyOptimization(sortedRegion_B.get(0), start));
								routesB.add(AntColonyOptimization(sortedRegion_B.get(1), start));
							} else{
								routesB.add(AntColonyOptimization(sortedRegion_B.get(0), start));
							}
						}

						// Process sorted routes for region C
						if (!sortedRegion_C.isEmpty()) {
							if(sortedRegion_C.size() == 2) {
								routesC.add(AntColonyOptimization(sortedRegion_C.get(0), start));
								routesC.add(AntColonyOptimization(sortedRegion_C.get(1), start));
							} else {
								routesC.add(AntColonyOptimization(sortedRegion_C.get(0), start));
							}
						}

						// Process sorted routes for region D
						if (!sortedRegion_D.isEmpty()) {
							if(sortedRegion_D.size() == 2) {
								if(sortedRegion_D.get(0).size() == 1 || sortedRegion_D.get(1).size() == 1) {
									routesD.add(AntColonyOptimization(sortedRegion_D.get(0), start));
									routesD.add(AntColonyOptimization(sortedRegion_D.get(1), start));
								} else {
									routesD.add(GeneticAlgorithmOptimization(sortedRegion_D.get(0), start));
									routesD.add(GeneticAlgorithmOptimization(sortedRegion_D.get(1), start));
								}
							} else {
								if(sortedRegion_D.get(0).size() == 1) {
									routesD.add(AntColonyOptimization(sortedRegion_D.get(0), start));
								} else {
									routesD.add(GeneticAlgorithmOptimization(sortedRegion_D.get(0), start));
								}
							}
						}

						// Print and concatenate routes for each region
						for (int i = 0; i < routesA.size(); i++) {
							System.out.println(routesA.get(i));
							routesInString += routesA.get(i).toString();
						}
						for (int i = 0; i < routesB.size(); i++) {
							System.out.println(routesB.get(i));
							routesInString += routesB.get(i).toString();
						}
						for (int i = 0; i < routesC.size(); i++) {
							System.out.println(routesC.get(i));
							routesInString += routesC.get(i).toString();
						}
						for (int i = 0; i < routesD.size(); i++) {
							System.out.println(routesD.get(i));
							routesInString += routesD.get(i).toString();
						}

						// Create and send message to server with concatenated route information
						ACLMessage messagetoServer = msg.createReply();
						messagetoServer.addReceiver(server);
						messagetoServer.setPerformative(ACLMessage.INFORM);
						messagetoServer.setContent(routesInString);
						send(messagetoServer);
						routesInString = "";
						
					// Create a REQUEST message to ask all delivery agents if they can deliver now
						ACLMessage requestDeliveryMsg = new ACLMessage(ACLMessage.REQUEST);
						for (int i = 0; i < args.length; ++i) {
							// Add all responders as receivers
							requestDeliveryMsg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
						}

						// Set the interaction protocol
						requestDeliveryMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

						// Specify the reply deadline (10 seconds)
						requestDeliveryMsg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

						// Set message content
						requestDeliveryMsg.setContent("Can you deliver now?");
						send(requestDeliveryMsg);
						k=0;
						// Loop to continuously receive messages and process them
						while(true) {
							// Receive messages of type AGREE
							ACLMessage Agreemsg = receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
							// Receive messages of type INFORM
							ACLMessage Statusmsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
							
							// Process AGREE messages
							if (Agreemsg != null) {
								String locationToDeliver = "";
								// Check if the sender of the AGREE message is a delivery agent and there are routes available for delivery
								if(Agreemsg.getSender().getName().startsWith("da1") && routesA.size() != 0) {
									// Print confirmation message
									System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
									// Create an INFORM message to send the delivery location
									ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
									informLocation.setPerformative(ACLMessage.INFORM);
									// Concatenate delivery locations
									for (int i=0; i < routesA.size(); i++) {
										locationToDeliver = locationToDeliver + routesA.get(i);
										m++;
									}
									informLocation.setContent(locationToDeliver);
									informLocation.addReceiver(Agreemsg.getSender());  
									// Send the INFORM message
									send(informLocation);
								}
								// Repeat similar process for other delivery agents and routes
								else if(Agreemsg.getSender().getName().startsWith("da2") && routesB.size() != 0){
									// Print confirmation message
									System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
									// Create an INFORM message to send the delivery location
									ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
									informLocation.setPerformative(ACLMessage.INFORM);
									// Concatenate delivery locations
									for (int i=0; i < routesA.size(); i++) {
										locationToDeliver = locationToDeliver + routesB.get(i);
										m++;
									}
									informLocation.setContent(locationToDeliver);
									informLocation.addReceiver(Agreemsg.getSender());  
									// Send the INFORM message
									send(informLocation);
								}
								else if(Agreemsg.getSender().getName().startsWith("da3") && routesC.size() != 0){
									// Print confirmation message
									System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
									// Create an INFORM message to send the delivery location
									ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
									informLocation.setPerformative(ACLMessage.INFORM);
									// Concatenate delivery locations
									for (int i=0; i < routesA.size(); i++) {
										locationToDeliver = locationToDeliver + routesC.get(i);
										m++;
									}
									informLocation.setContent(locationToDeliver);
									informLocation.addReceiver(Agreemsg.getSender());  
									// Send the INFORM message
									send(informLocation);
								}
								else if(Agreemsg.getSender().getName().startsWith("da4") && routesD.size() != 0){
									// Print confirmation message
									System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
									// Create an INFORM message to send the delivery location
									ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
									informLocation.setPerformative(ACLMessage.INFORM);
									// Concatenate delivery locations
									for (int i=0; i < routesA.size(); i++) {
										locationToDeliver = locationToDeliver + routesD.get(i);
										m++;
									}
									informLocation.setContent(locationToDeliver);
									informLocation.addReceiver(Agreemsg.getSender());  
									// Send the INFORM message
									send(informLocation);
								}   
								else {
									// Print error message if no suitable route found for delivery
									System.out.println("error");
								}
							}
							// Process INFORM messages
							else if (Statusmsg != null) {
								// Print status message
								System.out.println(Statusmsg.getSender().getName() + "status : " + Statusmsg);
								// Increment counter if delivery is done
								if(Statusmsg.getContent().startsWith("Done")) {
									k=k+1;
								}
							}
							// Check if all deliveries are done and exit the loop
							if(k==m) {
								break;
							}
							// Check if message received is from the server, then exit the loop
							else {
								if(Statusmsg != null) {
									if(Statusmsg.getSender().getName().equals(server.getName())){
										break;
									}
								}
							}
						}

					} 
					else {}
				}
            }
         
        }   
        
    }
    
   
	// Method to optimize routes using Ant Colony Optimization algorithm
private String AntColonyOptimization(List<int[]> coordinates, int[] start) {
    // Define parameters for the Ant Colony Optimization algorithm
    int numAnts = 10;
    int numIterations = 100;
    double alpha = 1;
    double beta = 2;
    double rho = 0.5;
    double Q = 100;
    String bestTour = "";

    // Create an instance of the Ant Colony Optimization algorithm and run it
    AntColony antColony = new AntColony(numAnts, numIterations, coordinates, start, alpha, beta, rho, Q);
    bestTour = antColony.run();
    return bestTour;
}

// Method to optimize routes using Genetic Algorithm
private String GeneticAlgorithmOptimization(List<int[]> coordinates, int[] start) {
    // Define parameters for the Genetic Algorithm
    int startingCity = 0;
    int populationSize = 100;
    int maxGenerations = 100;
    double mutationRate = 0.1;
    
    // Create an instance of the Genetic Algorithm and run it
    GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(coordinates, startingCity, populationSize, maxGenerations, mutationRate);
    SalesmanGenome result = geneticAlgorithm.optimize();
    return result.toString();
}

// Method to split coordinates into routes based on capacity threshold
	private List<List<int[]>> splitCoordinatesIntoRoutes(List<int[]> region, int capacityThreshold) {
	    List<List<int[]>> routes = new ArrayList<>();
	    List<int[]> currentRoute = new ArrayList<>();
	    int currentCapacity = 0;

	    for (int[] coordinate : region) {
	        // Check if the coordinate array has the expected length
	        if (coordinate.length < 3) {
	            System.err.println("Error: Coordinate array does not have the expected length");
	            continue; // Skip this coordinate and proceed to the next one
	        }

	        // Extract capacity from the coordinate array
	        int capacity = coordinate[2]; 
	        
	        
	        // Check if the coordinate itself exceeds the capacity threshold
	        if (capacity > capacityThreshold) {
	            // Create a temporary route for this coordinate
	            List<int[]> tempRoute = new ArrayList<>();
	            tempRoute.add(coordinate);
	            createTemporaryDeliveryAgent(capacity, AntColonyOptimization(tempRoute, start));
	            m++;
	            printRoute(tempRoute, capacity);
	        } else if (currentCapacity + capacity > capacityThreshold) {
	            // Add current route to routes and start a new route
	            if(routes.size() < 2)
	            {
	            	routes.add(currentRoute);
	            }
	            // Debugging: Print current route and its total capacity
	            printRoute(currentRoute, currentCapacity);

	            // Start a new route with the current coordinate
	            currentRoute = new ArrayList<>();
	            currentRoute.add(coordinate);
	            currentCapacity = capacity; // Reset current capacity
	        } else {
		        // Add the coordinate to the current route
	        	currentRoute.add(coordinate);
	            currentCapacity += capacity; // Update current capacity  
	        }
	        
	        if (routes.size() == 2) {
	            // Create a temporary route for the current coordinate
	            List<int[]> tempRoute = new ArrayList<>();
	            tempRoute.add(coordinate);
	            createTemporaryDeliveryAgent(capacity, AntColonyOptimization(tempRoute, start));
	            m++;
	            printRoute(tempRoute, capacity);
	            continue;
	        }
	        
	    }
	    if(!currentRoute.isEmpty())
	    {
	    	if(routes.size() < 2)
            {
            	routes.add(currentRoute);
            }
	    	printRoute(currentRoute, currentCapacity);
	    }

	    return routes;
	}

	// Method to print a route and its total capacity
	private void printRoute(List<int[]> route, int totalCapacity) {
	    System.out.print("Route: ");
	    for (int[] coordinateArray : route) {
	        System.out.print("(");
	        for (int i = 0; i < coordinateArray.length; i++) {
	            System.out.print(coordinateArray[i]);
	            if (i < coordinateArray.length - 1) {
	                System.out.print(", ");
	            }
	        }
	        System.out.print(") ");
	    }
	    System.out.println(", Total Capacity: " + totalCapacity);
	}
	
	// Method to create a temporary delivery agent with a unique name
	private void createTemporaryDeliveryAgent(int remainingCapacity, String items) {
	    try {
	    	routesInString += items;
	        // Get the agent container
	        jade.wrapper.AgentContainer container = getContainerController();
	        
	        agentCounter++;
	        // Generate a unique name for the temporary agent
	        String uniqueName = "TemporaryAgent_" + agentCounter;

	        // Create a new instance of DAAgent (temporary agent)
	        DAAgent temporaryAgent = new DAAgent();
	        temporaryAgent.setCapacity(remainingCapacity); // Set temporary agent's capacity

	        // Start the temporary agent with the unique name
	        container.acceptNewAgent(uniqueName, temporaryAgent).start();

	        System.out.println("Temporary delivery agent created with name: " + uniqueName);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	        ACLMessage messagetoTemporaryAgent = msg.createReply();
            messagetoTemporaryAgent.addReceiver(new AID(uniqueName, AID.ISLOCALNAME));
            messagetoTemporaryAgent.setContent(items);
            send(messagetoTemporaryAgent);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}