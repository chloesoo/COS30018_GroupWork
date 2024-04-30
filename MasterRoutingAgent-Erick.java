package vehicle.routing.system;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import vehicle.routing.system.AntColony;

public class MasterRoutingAgent extends Agent {
    private int nResponders;
    private int capacity;
    private int location;
    private List<List<Integer>> coordinatesList = new ArrayList<>(); 
    private int[] start = new int[]{25, 25};

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
             // Iterate over each line and split the coordinates by commas
                for (String line : lines) {
                    // Split the line by commas to get individual coordinate strings
                    String[] coordinatesString = line.split(",");

                    // Create a list to store the integer coordinates
                    List<Integer> coordinates = new ArrayList<>();

                    // Parse each coordinate string into an integer and add it to the list
                    for (String coordinateString : coordinatesString) {
                        coordinates.add(Integer.parseInt(coordinateString));
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
                
                AntColonyOptimization(coordinatesList, start);
            } 
            if (trigger.getContent().equalsIgnoreCase("start")) {
                System.out.println("start");

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


                // Initialise the AchieveREInitiator behaviour and add to agent
                addBehaviour(new AchieveREInitiator(this, requestDeliveryMsg) {
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
                });
            } else {
                System.out.println(getLocalName() + ": " + "You have not specified any arguments.");
            }
        }
    }
    
    private void AntColonyOptimization(List<List<Integer>> coordinates, int[] start) {
    	
    	List<int[]> processedList = new ArrayList<>();
        for (List<Integer> list : coordinates) {
            int[] array = list.stream().mapToInt(Integer::intValue).toArray();
            processedList.add(array);
        }
        
        List<int[]> coordinatesList = new ArrayList<>();
        for (int[] array : processedList) {
            // Create a new array without the last element
            int[] newArray = new int[array.length - 1];
            System.arraycopy(array, 0, newArray, 0, newArray.length);

            // Add the new array to the modified list
            coordinatesList.add(newArray);
        }

        List<int[]> region_A = new ArrayList<>();
        List<int[]> region_B = new ArrayList<>();
        List<int[]> region_C = new ArrayList<>();
        List<int[]> region_D = new ArrayList<>();
        
        for(int[] array: coordinatesList) {
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
        
    	int numAnts = 10;
    	int numIterations = 100;
    	int[] startNode = start;
    	double alpha = 1;
    	double beta = 2;
    	double rho = 0.5;
    	double Q = 100;
    	String bestTour_A = "";
    	String bestTour_B = "";
    	String bestTour_C = "";
    	String bestTour_D = "";
    	
    	if (region_A.size()!= 0) {
	    	AntColony antColony_region_A = new AntColony(numAnts, numIterations, region_A, startNode, alpha, beta, rho, Q);
	        bestTour_A = antColony_region_A.run();
	        System.out.println(bestTour_A);
    	}
        
        if (region_B.size()!= 0) {
	        AntColony antColony_region_B = new AntColony(numAnts, numIterations, region_B, startNode, alpha, beta, rho, Q);
	        bestTour_B = antColony_region_B.run();
	        System.out.println(bestTour_B);
        }
        
        if (region_C.size()!= 0) {
	        AntColony antColony_region_C = new AntColony(numAnts, numIterations, region_C, startNode, alpha, beta, rho, Q);
	        bestTour_C = antColony_region_C.run();
        }
        
        if (region_D.size()!= 0) {
	        AntColony antColony_region_D = new AntColony(numAnts, numIterations, region_D, startNode, alpha, beta, rho, Q);
	        bestTour_D = antColony_region_D.run();
        }
        
        if (bestTour_A != "") {
        	System.out.println("Region A " + bestTour_A);
        }
        if (bestTour_B != "") {
        	System.out.println("Region B " + bestTour_B);
        }
        if (bestTour_C != "") {
        	System.out.println("Region C " + bestTour_C);
        }
        if (bestTour_D != "") {
        	System.out.println("Region D " + bestTour_D);
        }
    }
}
