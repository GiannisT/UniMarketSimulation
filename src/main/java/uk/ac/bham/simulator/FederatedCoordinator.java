/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.output.TeeOutputStream;
import uk.ac.bham.cs.adaptation.strategy.RandomForestStrategy;

/**
 *
 * @author Francisco Ramirez
 */
public class FederatedCoordinator implements Runnable {
    
    public static RandomForestStrategy strategy = new RandomForestStrategy();

    private static boolean debug = false;
    static int SPpercentage = 0;
    static int Resourcepercentage = 0;
    static String input = "", input2 = "";

    private static final Float DEFAULTCOMMISSION = 0.05f;

    static Double Initialtime;
    ArrayList<ServiceProvider> serviceProviderList = null;
    ArrayList<IdentityProvider> identityProviderList = null;
    ArrayList<Agent> agentList = null;
    ArrayList<AuctionAsk> auctionAskList = null;
    ArrayList<Bid> bidList = null;

    List<Auction> auctionList;
    boolean running = false;

    private static final FederatedCoordinator instance = new FederatedCoordinator();
    private Float commission;

    private final String IDENTITY_PROVIDER_LOCK = "IDENTITY PROVIDER LOCK";
    private final String AGENT_LOCK = "AGENT LOCK";
    private final String SERVICE_PROVIDER_LOCK = "SERVICE PROVIDER LOCK";
    private final String BID_AUCTION_ASK_LOCK = "BID AUCTION ASK LOCK";
    private final String RUNNING_LOCK = "RUNNING LOCK";

    private FederatedCoordinator() {
        strategy.init(null);
        serviceProviderList = new ArrayList<>();
        identityProviderList = new ArrayList<>();
        auctionAskList = new ArrayList<>();
        agentList = new ArrayList<>();
        bidList = new ArrayList<>();
        commission = 0.0f;

        auctionList = Collections.synchronizedList(new ArrayList<Auction>());
    }

    public static boolean isDebugging() {
        return debug;
    }

    public void clear() {
        serviceProviderList.clear();
        identityProviderList.clear();
        auctionAskList.clear();
        agentList.clear();
        synchronized (BID_AUCTION_ASK_LOCK) {
            bidList.clear();
            auctionList.clear();
        }
        commission = 0.0f;
    }

    public static FederatedCoordinator getInstance() {
        return FederatedCoordinator.instance;
    }

    public ArrayList<AuctionAsk> getCurrentAsks() {
        ArrayList<AuctionAsk> askList = new ArrayList<AuctionAsk>();

        synchronized (BID_AUCTION_ASK_LOCK) {
            askList.addAll(auctionAskList);
        }

        return askList;
    }

    /* The agent publish a bid to the FederatedCoordinator */
    public boolean publishBid(Bid bid) {
        boolean isPublished;

        Agent agent = bid.getAgent();
        boolean isValidAgentSession = this.validateAgentSession(agent);

        if (!isValidAgentSession) {
            agent.requestAuthentication();
        }

        isValidAgentSession = this.validateAgentSession(agent);
        synchronized (BID_AUCTION_ASK_LOCK) {
            if (isValidAgentSession) {
                if (!bidList.contains(bid)) {
                    bidList.add(bid);
                }
            }
            isPublished = bidList.contains(bid);
        }

        return isPublished;
    }

    /* The ServiceProvider publish an auctionAsk to the FederatedCoordinator */
    public boolean publishAuctionAsk(AuctionAsk auctionAsk) {
        boolean isPublished;

        synchronized (BID_AUCTION_ASK_LOCK) {
            if (!auctionAskList.contains(auctionAsk)) {
                auctionAskList.add(auctionAsk);
            }
            isPublished = auctionAskList.contains(auctionAsk);
        }

        return isPublished;
    }

    public boolean existsAgent(Agent agent) {
        boolean existsAgent;

        synchronized (AGENT_LOCK) {
            existsAgent = agentList.contains(agent);
        }
        return existsAgent;
    }

    public boolean existsIdentityProvider(IdentityProvider identityProvider) {
        boolean existsIdentityProvider;

        synchronized (IDENTITY_PROVIDER_LOCK) {
            existsIdentityProvider = identityProviderList.contains(identityProvider);
        }

        return existsIdentityProvider;
    }

    public void payForServiceExecution(double price, Bid bid) {
        synchronized (BID_AUCTION_ASK_LOCK) {
            for (Auction a : auctionList) {
                if (a.existsBid(bid)) {
                    a.payForServiceExecution(price, bid);
                    break;
                }
            }
        }
    }

    public boolean validateAgentSession(Agent agent) {
        boolean isValid = true;

        return isValid;
    }

    public void addSession(Agent agent) {
        synchronized (AGENT_LOCK) {
            if (!agentList.contains(agent)) {
                agentList.add(agent);
            }
        }
    }

    public boolean registerIdentityProvider(IdentityProvider identityProvider) {
        boolean isRegistered;

        synchronized (IDENTITY_PROVIDER_LOCK) {
            if (!identityProviderList.contains(identityProvider)) {
                identityProviderList.add(identityProvider);
            }
            isRegistered = identityProviderList.contains(identityProvider);
        }
        return isRegistered;
    }

    public boolean registerServiceProvider(ServiceProvider serviceProvider) {
        boolean isRegistered;
        synchronized (SERVICE_PROVIDER_LOCK) {
            if (!serviceProviderList.contains(serviceProvider)) {
                serviceProviderList.add(serviceProvider);
            }
            isRegistered = serviceProviderList.contains(serviceProvider);
        }

        return isRegistered;
    }

    public void start() {
        synchronized (RUNNING_LOCK) {
            if (!running) {
                Thread thread = new Thread(this, "FederatedCoordiantor");
                this.running = true;
                thread.start();
            }
        }
    }

    public void stop() {
        synchronized (RUNNING_LOCK) {
            this.running = false;
        }
    }

    public boolean isRunning() {
        boolean ret;
        synchronized (RUNNING_LOCK) {
            ret = this.running;
        }
        return ret;
    }

    /*   public ArrayList<AuctionAsk> getCurrentAsks()
     {
     ArrayList<AuctionAsk> list=new ArrayList<AuctionAsk>();
     synchronized (BID_AUCTION_ASK_LOCK)
     {
     list.addAll(this.auctionAskList);
     this.auctionAskList.clear(); // TODO test this
     }
     return list;
     }*/
    public ArrayList<Bid> pullCurrentBids() {
        ArrayList<Bid> list = new ArrayList<Bid>();
        synchronized (BID_AUCTION_ASK_LOCK) {
            list.addAll(this.bidList);
            this.bidList.clear(); // TODO test this
        }
        return list;
    }

    public void addAuction(Auction auction) {
        synchronized (BID_AUCTION_ASK_LOCK) {
            auctionList.add(auction);
        }
    }

    /**
     *
     * Implements Runnable to do tasks of FederatedCoordinator
     *
     */
    public synchronized void run() {
        while (isRunning()) {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }

            boolean existsBidNoAuction = this.existsBidNoAuction();

            //TODO prepare for auction
            if (Utilities.generateRandomInteger(1, 10) % 2 == 0) {
                //if (this.bidList.size() == 1 && AgentManager.getInstance().getTotalBid() >= 1) {
                //    continue;
                //}

                if ((this.bidList.size() > 2 && this.auctionAskList.size() > 5) || (this.bidList.size() > 0 && !AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning())) {
                    ArrayList<AuctionAsk> aList = this.getCurrentAsks();
                    ArrayList<Bid> bList = this.pullCurrentBids();

                    System.out.println("*********** BEGIN A NEW AUCTION (bids=" + bList.size() + ", asks=" + aList.size() + ") ***********");
                    Auction auction = new Auction(bList, aList);
                    this.addAuction(auction);
                    auction.start();
                }
            }

            if (!AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning() && !existsBidNoAuction) {
                if (!existsAuctionRunning()) {
                    break;
                }
            }
        }

        this.printAuctionList();

        stop();

        System.out.println("+++++ END +++++");
        System.out.println("+++++ " + this.auctionList.size() + " AUCTIONS +++++");
    }

    public boolean existsBidNoAuction() {
        boolean exists = false;
        synchronized (BID_AUCTION_ASK_LOCK) {
            for (Bid b : bidList) {
                exists = false;
                for (Auction a : auctionList) {
                    if (a.isRunning()) {
                        exists = (exists || a.existsBid(b));
                    }
                }
                if (!exists) {
                    return true;
                }
            }
            if (bidList.isEmpty()) {
                return false;
            }
        }

        return false;
    }

    public boolean existsAuctionRunning() {
        boolean exists = false;
        synchronized (BID_AUCTION_ASK_LOCK) {
            for (Auction a : auctionList) {
                exists = (exists || a.isRunning());
            }
        }
        return exists;
    }

    public void printAuctionList() {
        int c = 0;
        synchronized (BID_AUCTION_ASK_LOCK) {
            for (Auction a : auctionList) {
                c++;
                System.out.println("");
                System.out.println();
                System.out.println();
                System.out.println("AUCTION # 0" + c);
                a.printWinnerAuctionAsk(c);
                System.out.println("");
            }
        }
        System.out.println();
        // TODO check the federated commission
        //System.out.println("Federated Commission (" + Math.round(FederatedCoordinator.getDefaultCommission() * 100.0f) + "%): " + Math.round(FederatedCoordinator.getInstance().getCommission()));

    }

    public static void main(String[] args) {

        //***********************************************************************GIANNIS NEW CODE************************************************************
        System.out.println("Adverse Conditions: Do you want to run the simulation under the influence of malicious service providers? - Please state your answer with 'yes' or 'no' ");
        Scanner sc = new Scanner(System.in);
        input = sc.next();

        if (input.toLowerCase().equals("yes")) {
            //calculate the number of malicious SPs based on a percentage
            SPpercentage = 20;//used by the serviceProviderManager (line 94) to flag this percentage of SPs as malicious and remove them from the market

        } else if (input.toLowerCase().equals("no")) {
            //no action required
        } else {
            //if input is wrong
            System.out.println("The input provided is wrong, please state your answer with a 'yes' or 'no'");
            input = sc.next();
        }

        System.out.println("Adverse Conditions: Do you want a percentage of the resources to be damaged (unable to be allocated)? - Please state your answer with 'yes' or 'no' ");
        input2 = sc.next();

        if (input2.toLowerCase().equals("yes")) {
            Resourcepercentage = 20; //used by the serviceProviderManager (line 172) to flag this percentage of resources as faulty and remove them from the market (due to physical, VM errors and denial attacks)
        } else if (input2.toLowerCase().equals("no")) {
            //no action required
        } else {
            //if input is wrong
            System.out.println("The input provided is wrong, please state your answer with a 'yes' or 'no'");
            input2 = sc.next();
        }

        //***********************************************************************GIANNIS CODE END************************************************************
        try {
            PrintStream defaultOut = System.out;
            FileOutputStream fos = new FileOutputStream(new File("output.txt"));
            TeeOutputStream myOut = new TeeOutputStream(defaultOut, fos);
            PrintStream ps = new PrintStream(myOut);
            System.setOut(ps);

            FileOutputStream fosData = new FileOutputStream(new File("data.csv"));
            FileOutputStream fosPerformance = new FileOutputStream(new File("performance.csv"));
            PrintStream psData = new PrintStream(fosData);
            PrintStream psPerformance = new PrintStream(fosPerformance);
            Utilities.setOutput4Data(psData);
            Utilities.setOutput4Performance(psPerformance);
            Utilities.printHeader4Data();
            Utilities.printHeader4Performance();

            if (args.length > 1 && args[1].equals("--debug")) {
                FederatedCoordinator.debug = true;

            }

            Initialtime = (double) System.currentTimeMillis();
            FederatedCoordinator.getInstance().start();

            AgentManager.getInstance().setRandom();
            ServiceProviderManager.getInstance().setRandom();
            AgentManager.getInstance().start();
            ServiceProviderManager.getInstance().start();
            //Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            //public void run() {

            boolean working = true;
            while (working) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!FederatedCoordinator.getInstance().isRunning() && !AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning()) {
                    working = false;
                }
            }
            //}}));

            System.out.println();
            System.out.println("*** Ready for new task ***");
            FederatedCoordinator.getInstance().clear();
            AgentManager.clear();
            ServiceProviderManager.clear();

            FederatedCoordinator.getInstance().start();
            AgentManager.getInstance().setModelled();
            ServiceProviderManager.getInstance().setModelled();
            AgentManager.getInstance().start();
            ServiceProviderManager.getInstance().start();

            working = true;
            while (working) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!FederatedCoordinator.getInstance().isRunning() && !AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning()) {
                    working = false;
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.setOut(defaultOut);
            System.out.println("\nNote: The complete output could be found in file 'output.txt'.");
            DataCollector.print();
            //TODO System.exit(0); // why -1 ??
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return "" + this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    public static Float getDefaultCommission() {
        return DEFAULTCOMMISSION;
    }

    /**
     * @return the commission
     */
    public Float getCommission() {
        return commission;
    }

    /**
     * @param commission the commission to set
     */
    public void addCommission(Float commission) {
        this.commission += commission;
    }

}
