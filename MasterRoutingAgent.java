package Routing;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;
import java.util.Date;
import java.util.Vector;

public class MasterRoutingAgent extends Agent {
	private int nResponders;
	protected void setup() {
		// Read names of responders as arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			nResponders = args.length;
			System.out.println("Requesting dummyâ€�action to " + nResponders + " responders.");
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
			// Seet message content
			msg.setContent("Can you deliver now?");
			send(msg);
			// Initialise the AchieveREInitiator behaviour and add to agent
			addBehaviour(new AchieveREInitiator(this, msg) {
				// Method to handle an agree message from responder
				protected void handleAgree(ACLMessage agree) {
					System.out.println(getLocalName() + ": " +
							agree.getSender().getName() + " has agreed to the request");
						System.out.println(getLocalName() + ": Agreed to send the parcel.");
						
						ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
						inform.setPerformative(ACLMessage.INFORM);
						inform.setContent("10,10");
						
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