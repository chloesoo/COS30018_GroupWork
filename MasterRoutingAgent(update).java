package part4;

import jade.core.Agent;
import jade.domain.FIPANames;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;

import java.util.Date;
import java.util.Vector;

public class MasterRoutingAgent extends Agent {
    private int nResponders;
    private int capacity;
    private int location;

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
            ACLMessage trigger = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
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

                        //set location and capacity					
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

        //Behavior to handle INFORM messages from delivery agents (Added this part)
        addBehaviour(new InformHandler());
    }

    //Handle INFORM messages from delivery agents (Added this part)
    private class InformHandler extends jade.core.behaviours.CyclicBehaviour {
        public void action() {
            // Receive INFORM messages
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (msg != null) {
                System.out.println("Received INFORM message from " + msg.getSender().getName() + ": " + msg.getContent());
                // Handle the received INFORM message here
            } else {
                block();
            }
        }
    }
}
