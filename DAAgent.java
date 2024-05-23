package vehicle.routing.system;

import java.util.Random;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DAAgent extends Agent {

    // Create a random number generator
    Random random = new Random();
    // Initialize the capacity with a random value between 11 and 14
    private int capacity = random.nextInt(4) + 11;

    protected void setup() {
        // Add a behavior to receive messages
        addBehaviour(new ReceiveMessageBehaviour());
    }

    // Inner class for handling message reception
    private class ReceiveMessageBehaviour extends jade.core.behaviours.CyclicBehaviour {
        public void action() {
            // Receive messages with performative REQUEST
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            if (msg != null) {
                // Print received message content
                System.out.println("Received message from MasterRoutingAgent: " + msg.getContent());
                
                // Respond to capacity inquiry
                if (msg.getContent().equals("What is your capacity?")) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("My capacity is: " + capacity);
                    send(reply);
                    System.out.println("Responded to MasterRoutingAgent: " + reply.getContent());

                // Respond to delivery availability inquiry
                } else if (msg.getContent().equals("Can you deliver now?")) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Yes, I can deliver now");
                    send(reply);
                    System.out.println("Responded to MasterRoutingAgent: " + reply.getContent());
                }

            } else {
                // Block the behavior if no messages are available
                block();
            }

            // Receive messages with performative INFORM
            ACLMessage deliveryMsg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (deliveryMsg != null) {
                // Process delivery information message
                if (deliveryMsg.getContent().startsWith("Deliver location:")) {
                    informLocation(deliveryMsg.getContent());
                } else if (deliveryMsg.getContent().startsWith("Deliver capacity:")) {
                    informCapacity(deliveryMsg.getContent());
                }

                // Inform the sender that delivery information has been received
                ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                informMsg.addReceiver(deliveryMsg.getSender());
                informMsg.setContent("Received delivery information");
                send(informMsg);
                System.out.println("Message Received");

                // Introduce a delay to simulate processing time
                int delayInSeconds = 3;
                try {
                    long delayInMillis = delayInSeconds * 1000;
                    Thread.sleep(delayInMillis);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted.");
                }

                // Send a message indicating the delivery task is done
                ACLMessage doneMsg = new ACLMessage(ACLMessage.INFORM);
                doneMsg.addReceiver(deliveryMsg.getSender());
                doneMsg.setContent("Done " + deliveryMsg.getContent());
                send(doneMsg);
            }
        }

        // Handle the received delivery location
        private void informLocation(String locationContent) {
            // Extract and print delivery location
            String location = locationContent.substring("Deliver location: ".length());
            System.out.println("Received delivery location from MasterRoutingAgent: " + location);
        }

        // Handle the received delivery capacity
        private void informCapacity(String capacityContent) {
            // Extract and print delivery capacity
            String capacityStr = capacityContent.substring("Deliver capacity: ".length());
            int capacity = Integer.parseInt(capacityStr);
            System.out.println("Received delivery capacity from MasterRoutingAgent: " + capacity);
        }
    }

    // Method to set the remaining capacity for teemporary agent 
    public void setCapacity(int remainingCapacity) {
        capacity = remainingCapacity;
    }
}
