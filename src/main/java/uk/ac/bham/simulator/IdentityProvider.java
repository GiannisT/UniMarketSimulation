package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 */
public class IdentityProvider {

    protected FederatedCoordinator federatedCoordinator;

    ArrayList<Agent> agentList = null;
    private final String AGENT_LOCK = "AGENT";

    public IdentityProvider() {
        synchronized (AGENT_LOCK) {
            agentList = new ArrayList<Agent>();
        }
        new AgentAttributeThread(this).start();
    }

    public void setFederatedCoordinator(FederatedCoordinator federatedCoordinator) {
        this.federatedCoordinator = federatedCoordinator;
    }

    public void requestPayment(Float price, Bid bid) {
        Agent agent = bid.getAgent();
        agent.requestPayment(price, bid);
    }

    public boolean notifyPayment(Bid bid, ServiceProvider serviceProvider) {
        boolean allocation = serviceProvider.allocateResources(bid);
        return allocation;
    }

    /* TODO: Random authentication, 80% true / 20% false
     */
    public boolean authenticate(String credentials) {
        boolean isAuthenticated = true;

        return isAuthenticated;
    }

    public boolean addAgent(Agent agent) {
        boolean isAdded;

        synchronized (AGENT_LOCK) {
            if (!agentList.contains(agent)) {
                agentList.add(agent);
            }
            isAdded = agentList.contains(agent);
        }
        return isAdded;
    }

    public int getRandomAgentIndex() {
        if (agentList.size()==0) return -1;
        
        int position = Utilities.generateRandomInteger(1, agentList.size());
        return position;
    }

    public Agent getRandomAgentAttributes(int position) {
        Agent agentAttributes = new Agent(this);
        Agent agent = agentList.get(position);
        agentAttributes.location = agent.location;
        agentAttributes.rank = agent.rank;
        agentAttributes.state = agent.state;
        agentAttributes.documentSecurity = agent.documentSecurity;

        return agentAttributes;
    }

    public void setAgentAttributes(Agent newAgentAttributes, int position) {
        Agent agent = agentList.get(position);
        Agent oldAgentAttributes = this.getRandomAgentAttributes(position);

        agent.setLocation(newAgentAttributes.getLocation());
        agent.setRank(newAgentAttributes.getRank());
        agent.setState(newAgentAttributes.getState());
        agent.setDocumentSecurity(newAgentAttributes.getDocumentSecurity());

        String text = "A change of attributes was done for Agent {0}"
                + "\n location          > {1} > {2}"
                + "\n rank              > {3} > {4}"
                + "\n state             > {5} > {6}"
                + "\n document security > {7} > {8}";
        Object obj = new Object[]{agent
                , oldAgentAttributes.getLocation(), agent.getLocation()
                , oldAgentAttributes.getRank(), agent.getRank()
                , oldAgentAttributes.getState(), agent.getState()
                , oldAgentAttributes.getDocumentSecurity(), agent.getDocumentSecurity()
        };

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, text, obj);
    }

}
