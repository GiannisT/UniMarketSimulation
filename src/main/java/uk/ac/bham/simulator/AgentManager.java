/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.bham.simulator.util.DataStat;

/**
 *
 * @author Francisco Ramirez
 */
public class AgentManager extends TimerTask {
    
    private static AgentManager instance=new AgentManager();
    
    private boolean running;
    
    public static final String RUNNING_LOCK="RUNNING LOCK";
    
    private int totalBid=100; // This describes the total number of bids created by all agents in the market
    private int counter; //describes the number of bidders in the market, currently set to 10. 
    private Timer timer;
    
    private boolean isRandom;
    
    private AgentManager()
    {
        synchronized (RUNNING_LOCK)
        {
            this.running=false;
            this.isRandom=true;
        }
    }
    
    public int getTotalBid()
    {
        return this.totalBid;
    }
    
    public static void clear()
    {
        AgentManager.instance=new AgentManager();
    }
    
    public static AgentManager getInstance()
    {
        return AgentManager.instance;
    }
    
    public void setRandom()
    {
        this.isRandom=true;
    }
    
    public void setModelled()
    {
        this.isRandom=false;
    }
    
    public boolean isRandom()
    {
        return this.isRandom;
    }
    
    
    
    public void start()
    {
        synchronized (RUNNING_LOCK)
        {
            if(!this.running)
            {
                // TODO at the moment, ten agents with two bids each will be created
                this.counter=this.totalBid/2;

                long delayNewAgent=Utilities.generateRandomInteger(1, 10)*10+25;                

                this.timer=new Timer("Agent Manager", true);
                this.running=true;
                timer.schedule(this, delayNewAgent+20, delayNewAgent);
                //Thread thread=new Thread(this, "Agent Manager");
                //thread.start();
            }
        }
    }
    
    public void stop()
    {
        synchronized (RUNNING_LOCK)
        {
            if(running)
                running=false;
        }
    }
    
    public boolean isRunning()
    {
        boolean ret;
        synchronized (RUNNING_LOCK)
        {
            ret=running;
        }
        return ret;
    }
    
    /**
     * Create new agents according to a random delay, each new agent create two bids
     * 
     */
    public synchronized void run()
    {
        // TODO for the time being a single IdentityProvider will be used
        IdentityProvider ip=new IdentityProvider();
        FederatedCoordinator.getInstance().registerIdentityProvider(ip);
        
        if (isRunning() && counter>0)
        {   
            Agent agent=new Agent(ip);
            if (FederatedCoordinator.isDebugging()) Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new {0} was created and added to {1}", new Object[] {agent, FederatedCoordinator.getInstance()});
            
            BidCreator creator=new BidCreator(agent, 2, true);
            creator.start();
        }
        counter--;
        if (counter<=0)
        {
            this.timer.cancel();
            stop();
        }
    }
    
    class BidCreator extends TimerTask
    {
        Agent agent;
        Timer timer;
        int counter=0;
        boolean isRandom=true;

        public BidCreator(Agent a, int c, boolean isRandom)
        {
            this.agent=a;
            this.timer=new Timer("Bid Creator", true);
            this.counter=c;
            this.isRandom=isRandom;
        }

        public void start()
        {
            long delayNewBid=Utilities.generateRandomInteger(1, 10)*10+25;
            this.timer.schedule(this, delayNewBid, delayNewBid); 
        }
        
        public void run()
        {
            if(counter>0)
            {
                Bid bid=agent.createBid(this.isRandom);
                FederatedCoordinator.getInstance().publishBid(bid);
                AgentManager.getInstance().totalBid--;
                int tmp=DataStat.getInstance().incrementCounter("bid");
                DataStat.getInstance().recordValue("bid", tmp);
                if (FederatedCoordinator.isDebugging()) Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new {0} was published by {1} to {2}", new Object[] {bid, agent, FederatedCoordinator.getInstance()});
            }
            counter--;
            if(counter<=0)
            {
                timer.cancel();
            }
        }
    }
    
}
