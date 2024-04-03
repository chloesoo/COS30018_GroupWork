package Routing;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DAAgent extends Agent {
    private int capacity;
    private int weightCapacity; 

    protected void setup() {
        capacity = 100;
        weightCapacity = 500;
        Object[] args = getArguments();
        // Register with the MRA
        ACLMessage registerMsg = new ACLMessage(ACLMessage.INFORM);
        registerMsg.setContent("Capacity Constraint: " + capacity + " (Items), " + weightCapacity + " (Weight)");
        // Master Routing Agent
        for (int i = 0; i < args.length; ++i) {
        	registerMsg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
        	send(registerMsg);
        }

        // Receive Schedule
        //addBehaviour(new ReceiveSchedule());

        // Handle Requests
        //addBehaviour(new HandleRequests());
    }

    private class ReceiveSchedule extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (msg != null) {
                // Process received schedule
                System.out.println("Received schedule from MRA: " + msg.getContent());
            } else {
                block();
            }
        }
    }

    private class HandleRequests extends CyclicBehaviour {
        public void action() {
            ACLMessage request = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            if (request != null) {
                if (request.getContent().equals("UpdateCapacity")) {
                    // Action
                    ACLMessage reply = request.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    // Update
                    String[] contentParts = request.getContent().split(",");
                    if (contentParts.length == 3) {
                        int newCapacity = Integer.parseInt(contentParts[1]);
                        int newWeightCapacity = Integer.parseInt(contentParts[2]);
                        if (newCapacity <= capacity && newWeightCapacity <= weightCapacity) {
                            capacity = newCapacity;
                            weightCapacity = newWeightCapacity;
                            reply.setContent("Capacity Constraint Updated: " + capacity + " (Items), " + weightCapacity + " (Weight)");
                        } else {
                            // Send a message when it exceeds
                            reply.setContent("Capacity Update Failed: Exceeded maximum capacity constraint");
                        }
                    } else {
                        reply.setContent("Invalid Update Capacity Request");
                    }
                    send(reply);
                } else {
                    // Handle
                    ACLMessage reply = request.createReply();
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    send(reply);
                }
            } else {
                block();
            }
        }
    }
}
