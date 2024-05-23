package cos30018.week4;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import cos30018.week4.AntColony;
import cos30018.week4.GeneticAlgorithm;


public class MasterRoutingAgent2 extends Agent {
    private int nResponders;
    private int capacity;
    private int location;
    private List<List<Integer>> coordinatesList = new ArrayList<>(); 
    private int[] start = new int[]{25, 25};
    private List<String> routes = new ArrayList<>();
    private int CapacityA;
    private int CapacityB;
    private int CapacityC;
    private int CapacityD;
    private int k=4;
    private static int agentCounter = 0;
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
            
            while(true) {
            ACLMessage Capacitymsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (Capacitymsg != null) {
    			if (Capacitymsg.getContent().startsWith("My capacity is: ")) {
    				String Capacity = Capacitymsg.getContent().substring("My capacity is: ".length());
 			        System.out.println("Received capacity from " + Capacitymsg.getSender().getName() + " : " + Capacity);
 			        int intCapacity = Integer.parseInt(Capacity);
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
    			System.out.println(CapacityA);
    			System.out.println(CapacityB);
    			System.out.println(CapacityC);
    			System.out.println(CapacityD);
    			
    		}
            else if(CapacityA != 0 && CapacityB != 0 && CapacityC != 0 && CapacityD != 0) {
            	break;
            }
            
         // Check for timeout
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
            	while(true) {
            	ACLMessage Agreemsg = receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
            	ACLMessage Statusmsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (Agreemsg != null) {
                	
     			    if(Agreemsg.getSender().getName().startsWith("da1")) {
     			    	System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
     			    // Example: set location and capacity					
                        int location = 1010;
                        ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
                        informLocation.setPerformative(ACLMessage.INFORM);
                        informLocation.setContent("Deliver location: " + routes.get(0));
                        informLocation.addReceiver(Agreemsg.getSender());  
                        send(informLocation);
                        System.out.println("delivery location: " + routes.get(0));
                        //k=k+1;
     			    }
     			    else if(Agreemsg.getSender().getName().startsWith("da2")){
     			    	System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
     			    	int location = 2010;
                        ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
                        informLocation.setPerformative(ACLMessage.INFORM);
                        informLocation.setContent("Deliver location: " + routes.get(1));
                        informLocation.addReceiver(Agreemsg.getSender());  
                        send(informLocation);
                        System.out.println("delivery location: " + routes.get(1));
                        //k=k+1;
     			    }
     			    else if(Agreemsg.getSender().getName().startsWith("da3")){
     			    	System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
     			    	int location = 3010;
                        ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
                        informLocation.setPerformative(ACLMessage.INFORM);
                        informLocation.setContent("Deliver location: " + routes.get(2));
                        informLocation.addReceiver(Agreemsg.getSender());  
                        send(informLocation);
                        System.out.println("delivery location: " + routes.get(2));
                        //k=k+1;
    			    }
     			    else {
     			    	System.out.println(Agreemsg.getSender().getName() + ": Agreed to send the parcel.");
     			    	int location = 4010;
                        ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
                        informLocation.setPerformative(ACLMessage.INFORM);
                        informLocation.setContent("Deliver location: " + routes.get(3));
                        informLocation.addReceiver(Agreemsg.getSender());  
                        send(informLocation);
                        System.out.println("delivery location: " + routes.get(3));
                        //k=k+1;
     			    }	 			
        		}
                else if (Statusmsg != null) {
                	System.out.println(Statusmsg.getSender().getName() + "status : " + Statusmsg);
                	if(Statusmsg.getContent().startsWith("Done")) {
                		k=k+1;
                	}
                }
                
                else if(k==4) {
 			    	System.out.println(k);
 			    	break;
 			    }
            	}

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
                
                int capacityThresholdA = CapacityA; 
                int capacityThresholdB = CapacityB;
                int capacityThresholdC = CapacityC;
                int capacityThresholdD = CapacityD;

                List<int[]> region_A = new ArrayList<>();
                List<int[]> region_B = new ArrayList<>();
                List<int[]> region_C = new ArrayList<>();
                List<int[]> region_D = new ArrayList<>();
                region_A.clear();
                region_B.clear();
                region_C.clear();
                region_D.clear();
                for(int[] array: coordinateList) {
                	if (array[0] < 25) {
                		if(array[1] < 25) {
                			region_A.add(array);
                		}
                		else {
                			region_D.add(array);
                		}
                	}
                	else {
                		if(array[1] < 25) {
                			region_B.add(array);
                		}
                		else {
                			region_C.add(array);
                		}
                	}
                }
                List<List<int[]>> sortedRegion_A = splitCoordinatesIntoRoutes(region_A, capacityThresholdA);
                List<List<int[]>> sortedRegion_B = splitCoordinatesIntoRoutes(region_B, capacityThresholdB);
                List<List<int[]>> sortedRegion_C = splitCoordinatesIntoRoutes(region_C, capacityThresholdC);
                List<List<int[]>> sortedRegion_D = splitCoordinatesIntoRoutes(region_D, capacityThresholdD);
                routes.clear();
                
                if(sortedRegion_A.size() == 2)
                {
                	if(sortedRegion_A.get(0).size() == 1 || sortedRegion_A.get(1).size() == 1)
                	{
                		routes.add(AntColonyOptimization(sortedRegion_A.get(0), start));
                    	routes.add(AntColonyOptimization(sortedRegion_A.get(1), start));
                	}else {
                		routes.add(GeneticAlgorithmOptimization(sortedRegion_A.get(0), start));
                    	routes.add(GeneticAlgorithmOptimization(sortedRegion_A.get(1), start));
                	}
                	
                }else
                {
                	if(sortedRegion_A.get(0).size() == 1) {
                		routes.add(AntColonyOptimization(sortedRegion_A.get(0), start));
                	}else {
                		routes.add(GeneticAlgorithmOptimization(sortedRegion_A.get(0), start));
                	}
                }
                
                if(sortedRegion_B.size() == 2)
                {
                	routes.add(AntColonyOptimization(sortedRegion_B.get(0), start));
                	routes.add(AntColonyOptimization(sortedRegion_B.get(1), start));
                }else{
                	routes.add(AntColonyOptimization(sortedRegion_B.get(0), start));
                }
                
                if(sortedRegion_C.size() == 2)
                {
                	routes.add(AntColonyOptimization(sortedRegion_C.get(0), start));
                	routes.add(AntColonyOptimization(sortedRegion_C.get(1), start));
                }else
                {
                	routes.add(AntColonyOptimization(sortedRegion_C.get(0), start));
                }
                
                if(sortedRegion_D.size() == 2)
                {
                	if(sortedRegion_D.get(0).size() == 1 || sortedRegion_D.get(1).size() == 1)
                	{
                		routes.add(AntColonyOptimization(sortedRegion_D.get(0), start));
                    	routes.add(AntColonyOptimization(sortedRegion_D.get(1), start));
                	}else {
                		routes.add(GeneticAlgorithmOptimization(sortedRegion_D.get(0), start));
                    	routes.add(GeneticAlgorithmOptimization(sortedRegion_D.get(1), start));
                	}
                }else
                {
                	if(sortedRegion_D.get(0).size() == 1) {
                		routes.add(AntColonyOptimization(sortedRegion_D.get(0), start));
                	}else {
                		routes.add(GeneticAlgorithmOptimization(sortedRegion_D.get(0), start));
                	}
                }
      
                
                for (int i =0; i < routes.size(); i++) {
                	System.out.println(routes.get(i));
                	routesInString += routes.get(i).toString();
                }
                
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
            } 
            else {
            	
            }
            
            if (trigger.getContent().equalsIgnoreCase("start")) {
                System.out.println("start");

             // Create a REQUEST message to ask all delivery agents if they can deliver now
               /* ACLMessage requestDeliveryMsg = new ACLMessage(ACLMessage.REQUEST);
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
                k=0;*/
                
                
                // Initialise the AchieveREInitiator behaviour and add to agent
                /*addBehaviour(new AchieveREInitiator(this, requestDeliveryMsg) {
                    // Method to handle an agree message from responder
                    protected void handleAgree(ACLMessage agree) {
                        System.out.println(getLocalName() + ": " +
                                agree.getSender().getName() + " has agreed to the request");
                        System.out.println(getLocalName() + ": Agreed to send the parcel.");

                        // Example: set location and capacity					
                        int location = 1010;
                        ACLMessage informLocation = new ACLMessage(ACLMessage.INFORM);
                        informLocation.setPerformative(ACLMessage.INFORM);
                        informLocation.setContent("Deliver location: " + location);
                        informLocation.addReceiver(agree.getSender());  
                        send(informLocation);
                        System.out.println("delivery location: " + location);
						
					    int capacity = 10;
						ACLMessage informcapacity = new ACLMessage(ACLMessage.INFORM);
						informcapacity.setPerformative(ACLMessage.INFORM);
						informcapacity.setContent("Deliver capacity: " + capacity);
						informcapacity.addReceiver(agree.getSender());
						send(informcapacity);
						System.out.println("Deliver capacity: " + capacity);
                    }

                    // Method to handle an inform message from responder
                    protected void handleInform(ACLMessage inform) {
                        System.out.println(getLocalName() + ": " +
                                inform.getSender().getName() + " have " + inform.getContent() + " capacity.");
                    }

                    // Method to handle a refuse message from responder
                    protected void handleRefuse(ACLMessage refuse) {
                        System.out.println(getLocalName() + ": " + refuse.getSender().getName() + " refused to perform the requested action");
                        nResponders--;
                    }

                    // Method to handle a failure message (failure in delivering the message)
                    protected void handleFailure(ACLMessage failure) {
                        if (failure.getSender().equals(myAgent.getAMS())) {
                            // FAILURE notification from the JADE runtime: the receiver (receiver does not exist)
                            System.out.println(getLocalName() + ": " + "Responder does not exist");
                        } else {
                            System.out.println(getLocalName() + ": " +
                                    failure.getSender().getName() + " failed to perform the requested action");
                        }
                    }

                    // Method that is invoked when notifications have been received from all responders
                    protected void handleAllResultNotifications(Vector notifications) {
                        if (notifications.size() < nResponders) {
                            // Some responder didn't reply within the specified timeout
                            System.out.println(getLocalName() + ": " + "Timeout expired: missing " + (nResponders - notifications.size()) + " responses");
                        } else {
                            System.out.println(getLocalName() + ": " + "Received notifications about every responder");
                        }
                    }
                });*/
                //send(requestDeliveryMsg);
            } else {
                
            }
            }
            }
         
        }   
        
    }
    
   
	private String AntColonyOptimization(List<int[]> coordinates, int[] start) {
    	int numAnts = 10;
    	int numIterations = 100;
    	int[] startNode = start;
    	double alpha = 1;
    	double beta = 2;
    	double rho = 0.5;
    	double Q = 100;
    	String bestTour = "";
    	

    	AntColony antColony = new AntColony(numAnts, numIterations, coordinates, startNode, alpha, beta, rho, Q);
        bestTour = antColony.run();
        return bestTour;
    }

	private String GeneticAlgorithmOptimization(List<int[]> coordinates, int[] start) {
	    int startingCity = 0;
	    int populationSize = 100;
	    int maxGenerations = 100;
	    double mutationRate = 0.1;
	    
    	GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(coordinates, startingCity, populationSize, maxGenerations, mutationRate);
	
        SalesmanGenome result = geneticAlgorithm.optimize(); // Run the genetic algorithm
        return result.toString();
    }
	
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
                createTemporaryDeliveryAgent(capacity, AntColonyOptimization(tempRoute,start));
                printRoute(tempRoute, capacity);
            } else if (currentCapacity + capacity > capacityThreshold) {
                // Add current route to routes and start a new route
                routes.add(currentRoute);
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
	    }

	    // Add the last route if it's not empty
	    if (!currentRoute.isEmpty()) {
	        routes.add(currentRoute);
	        // Debugging: Print last route and its total capacity
	        printRoute(currentRoute, currentCapacity);
	    }
	    return routes;
	}

	
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

	
	/*private class HandleInform extends jade.core.behaviours.CyclicBehaviour {
    	public void action() {
    		ACLMessage Capacitymsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    		if (Capacitymsg != null) {
    			if (Capacitymsg.getContent().startsWith("My capacity is: ")) {
    				String Capacity = Capacitymsg.getContent().substring("My capacity is: ".length());
 			        System.out.println("Received capacity from DAAgent: " + Capacity);
 			        int intCapacity = Integer.parseInt(Capacity);
 			    if(CapacityA == 0) {
 			    	CapacityA = intCapacity;
 			    }
 			    else if(CapacityB == 0){
 			    	CapacityB = intCapacity;
 			    }
 			    else if(CapacityC == 0){
			    	CapacityC = intCapacity;
			    }
 			    else {
 			    	CapacityD = intCapacity;
 			    	
 			    }
    			}
    			System.out.println(CapacityA);
    			System.out.println(CapacityB);
    			System.out.println(CapacityC);
    			System.out.println(CapacityD);
    			
    		}
    		else {
    			block();
    			System.out.println("Hello");
    		}
    	}
    			 
    }*/
   }