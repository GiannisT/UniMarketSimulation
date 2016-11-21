/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 */
public class AgentAttributeThread implements Runnable {

    private final Thread thread;
    private boolean running;
    private final String LOCK_RUNNING = "LOCK_RUNNING";

    private final IdentityProvider identityProvider;

    public AgentAttributeThread(IdentityProvider identityProvider) {
        thread = new Thread(this, "Agent Attribute Thread");
        running = false;

        this.identityProvider = identityProvider;
    }

    public boolean isRunning() {
        boolean result;
        synchronized (LOCK_RUNNING) {
            result = this.running;
        }
        return result;
    }

    public void start() {
        if (!isRunning()) {
            thread.start();
        }
    }

    public void stop() {
        if (isRunning()) {
            synchronized (LOCK_RUNNING) {
                this.running = false;
            }
        }
    }

    public synchronized void run() {
        while (isRunning()) {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(AgentAttributeThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            int position = identityProvider.getRandomAgentIndex();
            if (position==-1) continue;
                    
            Agent agent = identityProvider.getRandomAgentAttributes(position);
            //Location oldLocation = agent.getLocation();
            Position oldRank = agent.getRank();
            //State oldState = agent.getState();
            //DocumentSecurity oldDocumentSecurity = agent.getDocumentSecurity();
            // how to change location
            // location is random
            boolean changed = false;

            int randomLocation = Utilities.generateRandomInteger(1, 100);
            if (randomLocation % 2 == 0) {
                agent.setLocation(Location.createRandom());
                changed = true;
            }
            // how to change rank
            // rank only change and can be higher than the last one
            // this case from lowRanked to highRanked for soldier
            if (oldRank.equals(Position.Lecturer)) {
                int randomRank = Utilities.generateRandomInteger(1, 100);
                if (randomRank % 2 == 0) {
                    Position newRank = Position.Researcher;
                    agent.setRank(newRank);
                    changed = true;
                }
            }
            // how to change state 
            // state is random
            int randomState = Utilities.generateRandomInteger(1, 100);
            if (randomState % 2 == 0) {
                agent.setState(Device.createRandom());
                changed = true;
            }

            // how to change document security
            // random 
            int randomDocumentSecurity = Utilities.generateRandomInteger(1, 100);
            if (randomDocumentSecurity % 2 == 0) {
                agent.setDocumentSecurity(InformationSensitivity.createRandom(agent.getRank()));
                changed = true;
            }
            if(changed) {
                identityProvider.setAgentAttributes(agent, position);
            }
            
        }
    }
}
