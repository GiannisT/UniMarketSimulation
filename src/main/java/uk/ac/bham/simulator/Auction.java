/*
 * Copyright (C) 2014 frankouz.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 *
 */
package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 * @version 1.0
 *
 * modified
 * @author Francisco Ramirez
 * @version 1.2
 */
public class Auction implements Runnable {

    ArrayList<AuctionAsk> auctionAskList;
    ArrayList<Bid> bidList;
    Map<Bid, AuctionAsk> waitingMap = null;
    ArrayList<Bid> notifiedBidList = null;

    private final String WAITING_MAP_LOCK = "WAITING MAP LOCK";
    private final String BID_AUCTION_ASK_LOCK = "BID AUCTION ASK LOCK";
    private final String NOTIFIED_BID_LOCK = "NOTIFIED BID LOCK";

    private Float firstHighestPrice;
    private Float secondHighestPrice;
    private Float initialOfferPrice;
    private Float userMaxPrice;

    boolean isRunning;

    long startTimestamp;
    long stopTimestamp;
    long startMemory;
    long stopMemory;

    public Auction(ArrayList<Bid> bidList, ArrayList<AuctionAsk> askList) {
        this.bidList = new ArrayList<>();
        this.auctionAskList = new ArrayList<>();
        this.waitingMap = new java.util.HashMap<>();
        this.notifiedBidList = new ArrayList<>();

        this.bidList.addAll(bidList);
        this.auctionAskList.addAll(askList);

        this.isRunning = false;
    }

    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            startTimestamp = System.currentTimeMillis();
            startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            Thread t = new Thread(this, "Auction");
            t.start();
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setWinnerAskForBid(Bid nextBid, AuctionAsk winnerAsk) {
        synchronized (WAITING_MAP_LOCK) {
            if (!waitingMap.containsKey(nextBid)) {
                waitingMap.put(nextBid, winnerAsk);
            }
        }
    }

    public boolean existsBid(Bid bid) {
        boolean exists = false;

        for (Bid b : bidList) {
            if (b == bid) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    public void payForServiceExecution(double price, Bid bid) {
        Agent agent = bid.getAgent();
        IdentityProvider identityProvider = agent.getIdentityProvider();

        boolean existsIdentityProvider = FederatedCoordinator.getInstance().existsIdentityProvider(identityProvider);
        boolean existsAgent = FederatedCoordinator.getInstance().existsAgent(agent);

        if (existsIdentityProvider && existsAgent) {
            synchronized (WAITING_MAP_LOCK) {
                AuctionAsk winnerAsk = waitingMap.get(bid);
                if (winnerAsk != null) {
                    ServiceProvider serviceProvider = winnerAsk.getServiceProvider();
                    identityProvider.notifyPayment(bid, serviceProvider);
                    float willingToPayPrice = bid.getBidData().getPreferredPrice(); // TODO check if this values is ok to calculate the revenue
                    float askPrice = winnerAsk.calculateCurrentPrice(willingToPayPrice);
                    serviceProvider.addRevenue(Math.round(new Float(askPrice * 0.1)));
                    //FederatedCoordinator.getInstance().addCommission(Math.round(askPrice * FederatedCoordinator.getDefaultCommission() * 1.0f) * 1.0f);
                }
            }
        }
    }

    public ArrayList<AuctionAsk> getCurrentAsks() {
        ArrayList<AuctionAsk> askList = new ArrayList<>();

        synchronized (BID_AUCTION_ASK_LOCK) {
            askList.addAll(auctionAskList);
        }

        return askList;
    }

    public AuctionAsk getCheapestAsk(ArrayList<AuctionAsk> askList, float price, AuctionAsk but) {
        AuctionAsk cheapestAsk = null;
        int counter = 0;
        String steps = "";

        for (AuctionAsk aa : askList) {
            if (aa == but) {
                continue;
            }

            if (cheapestAsk == null) {
                if (aa.calculateCurrentPrice(price) == -1) {
                    continue;
                }
                cheapestAsk = aa;
            }

            float askPrice;

            try {
                askPrice = aa.calculateCurrentPrice(price);
                if (askPrice == -1) {
                    continue;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Error calculating current price for askPrice=" + aa + "(" + price + ")");
                aa.calculateCurrentPrice(price);
                throw ex;
            }

            counter++;
            try {
                float cheapestPrice = cheapestAsk.calculateCurrentPrice(price);
                steps += ", " + Math.round(askPrice) + "(" + aa.getMinimumProfit() + "," + aa.getPreferredProfit() + ")";
                if (askPrice <= cheapestPrice) {
                    cheapestAsk = aa;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Error calculating current price for cheapestAsk=" + cheapestAsk + "(" + price + ")");
                throw ex;
            }
        }
        if (FederatedCoordinator.isDebugging()) {
            System.out.printf("Calculate CHEAPEST ASK from a list of " + askList.size() + " AuctionAsk and a price of " + Math.round(price) + " only " + counter + " Ask as available %n%s%n", steps);
        }
        return cheapestAsk;
    }

    public ArrayList<AuctionAsk> getTwoCheapestAsk(ArrayList<AuctionAsk> askList, float price) {
        ArrayList<AuctionAsk> cheapestAskList = new ArrayList<>();

        AuctionAsk ask0 = this.getCheapestAsk(askList, price, null);
        AuctionAsk ask1 = this.getCheapestAsk(askList, price, ask0);

        if (ask0 != null) {
            cheapestAskList.add(ask0);
        }
        if (ask1 != null) {
            cheapestAskList.add(ask1);
        }

        return cheapestAskList;
    }

    public ArrayList<AuctionAsk> compareWithCheapestAsk(Bid bid, ArrayList<AuctionAsk> askList) {
        ArrayList<AuctionAsk> winnerAskList = new ArrayList<>();
        //AuctionAsk winnerAsk = null;
        float price = bid.getBidData().getPreferredPrice();
        ArrayList<AuctionAsk> cheapestAsk = getTwoCheapestAsk(askList, price);
        if (cheapestAsk != null && cheapestAsk.size() == 2) {
            float askPrice = cheapestAsk.get(0).calculateCurrentPrice(price);
            float bidPrice = bid.getBidData().calculateCurrentOffer(askPrice);
            if (askPrice <= bidPrice) {
                winnerAskList.addAll(cheapestAsk);
            }
        }

        return winnerAskList;
    }

    public boolean isNotifiedBid(Bid bid) {
        boolean ret;
        synchronized (NOTIFIED_BID_LOCK) {
            ret = this.notifiedBidList.contains(bid);
        }
        return ret;
    }

    public void notifyBid(Bid bid) {
        boolean addit = false;
        synchronized (BID_AUCTION_ASK_LOCK) {
            if (this.bidList.contains(bid) && !this.notifiedBidList.contains(bid)) {
                // TODO is it required to remove from the bidList ??
                // put in the notified list
                addit = true;
            }
        }
        if (addit) {
            synchronized (NOTIFIED_BID_LOCK) {
                // put in the notified list
                this.notifiedBidList.add(bid);
            }
        }
    }

    public Bid getNextBid() {
        Bid nextBid = null;
        synchronized (BID_AUCTION_ASK_LOCK) {
            for (Bid b : this.bidList) {
                if (!this.isNotifiedBid(b)) {
                    nextBid = b;
                    break;
                }
            }
        }
        return nextBid;
    }

    @Override
    public synchronized void run() {
        Bid oneWinnerBid = null;
        AuctionAsk oneWinnerAsk = null;

        Float lastPrice = null;
        ArrayList<Float> priceList = new ArrayList<>();

        while (isRunning()) {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(Auction.class.getName()).log(Level.SEVERE, null, ex);
            }

            Bid nextBid = this.getNextBid();
            if (nextBid != null) {
                for (IdentityResource ir : nextBid.getBidData().getIdentityResources()) {
                    // TODO why get cost is null
                    HistoricalPrice.getInstance().addPrice(ir.getResourceType(), ir.getPriority().getLevel());
                }
            }
            if (nextBid != null) {
                if (FederatedCoordinator.isDebugging()) {
                    Logger.getLogger(Auction.class.getName()).log(Level.INFO, "a {0} was detected by {1} to search and auction winner ask", new Object[]{nextBid, FederatedCoordinator.getInstance()});
                }

                ArrayList<AuctionAsk> askList = this.getCurrentAsks();
                ArrayList<AuctionAsk> winnerAskList = this.compareWithCheapestAsk(nextBid, askList);
                AuctionAsk winnerAsk = null;
                if (winnerAskList.size() > 0) {
                    winnerAsk = winnerAskList.get(0);
                }
                if (oneWinnerAsk == null) {
                    oneWinnerAsk = winnerAsk;
                } else {

                }
                if (oneWinnerBid == null) {
                    try {
                        oneWinnerBid = nextBid;
                        float willingToPay = oneWinnerBid.getBidData().getPreferredPrice();
                        float askPrice = oneWinnerAsk.calculateCurrentPrice(willingToPay);
                        if (this.initialOfferPrice == null) {
                            this.initialOfferPrice = askPrice;
                        }
                        this.userMaxPrice = willingToPay * (1 + oneWinnerBid.getBidData().getMaxIncrementPercentage()) / 100;
                        float bidPrice = oneWinnerBid.getBidData().calculateCurrentOffer(askPrice);

                        lastPrice = bidPrice;
                        priceList.add(bidPrice);
                    } catch (Exception exception) {
                        System.out.println("debug why exception here..." + exception);
                        exception.printStackTrace();
                    }
                } else {
                    try {
                        float willingToPay = nextBid.getBidData().getPreferredPrice();
                        this.userMaxPrice = willingToPay * (100.0f + oneWinnerBid.getBidData().getMaxIncrementPercentage()) / 100;
                        float askPrice = oneWinnerAsk.calculateCurrentPrice(willingToPay);
                        float bidPrice = nextBid.getBidData().calculateCurrentOffer(askPrice);

                        if (bidPrice > lastPrice) {
                            oneWinnerBid = nextBid;
                            lastPrice = bidPrice;
                        }
                        priceList.add(bidPrice);
                    } catch (Exception exception) {
                        System.out.println("debug why exception here..." + exception);
                        exception.printStackTrace();
                    }
                }

                this.notifyBid(nextBid); // to move nextBid
            }

            if (nextBid == null) {
                stop();
            }
        }

        // ONE BID AND WINNER ASK FOR EACH AUCTION
        if (oneWinnerAsk != null && oneWinnerBid != null) {
            for (int i = 0; i < priceList.size() - 1; i++) {
                for (int j = i + 1; j < priceList.size(); j++) {
                    float pi = priceList.get(i);
                    float pj = priceList.get(j);

                    if (pi < pj) {
                        priceList.set(i, pj);
                        priceList.set(j, pi);
                    }
                }
            }
            this.firstHighestPrice = priceList.get(0);
            this.secondHighestPrice = priceList.get(0);
            float secondLowPrice = priceList.get(0);
            if (priceList.size() > 1) {
                secondLowPrice = priceList.get(1);
                this.secondHighestPrice = priceList.get(1);
            }

            IdentityProvider ip = oneWinnerBid.getAgent().getIdentityProvider();

            this.setWinnerAskForBid(oneWinnerBid, oneWinnerAsk);
            // TODO notify to IdentityProvider of Agent ??
            try {
                // winnerAsk.getAdaptedPrice()
                //float willingToPay = oneWinnerBid.getPreferredPrice(); // TODO check if this is the final value insted of the calculate from winnerAsk
                float askPrice;
                float bidPrice;

                askPrice = oneWinnerAsk.calculateCurrentPrice(secondLowPrice); // second price
                bidPrice = oneWinnerBid.getBidData().calculateCurrentOffer(askPrice);

                oneWinnerAsk.getServiceProvider().notifyAuctionWinner(ip, oneWinnerBid, bidPrice, askPrice);
            } catch (Exception exception) {
                System.out.println("debug why exception here..." + exception);
                exception.printStackTrace();
            }
            if (FederatedCoordinator.isDebugging()) {
                Logger.getLogger(Auction.class.getName()).log(Level.INFO, "the {0} had a winner {1}", new Object[]{oneWinnerBid, oneWinnerAsk});
            }

            stopTimestamp = System.currentTimeMillis();
            stopMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            String am;
            if (AgentManager.getInstance().isRandom()) {
                am = "English";
            } else {
                am = "Posted Offer";
            }
            Utilities.println4Performance(this.hashCode(), am, startMemory, stopMemory, startTimestamp, stopTimestamp);
        } else {
            System.out.println("No winner for this auction.");
        }
    }

    //contains all borrowed resource between SPs
    ArrayList<String> RecordBorrowedResources = new ArrayList<>();

    public void printWinnerAuctionAsk(int n) {

        BidData selectedBid;
        BidData selectedBidModified;
        Agent selectedAgent;
        ServiceProvider selectedServiceProvider = null;
        AuctionAsk selectedAuctionAsk = null;

        int factorA = 10;
        float factorB = 100.0f;
        String s = "";

        System.out.println(s);
        System.out.println(s + "Number of bids: " + this.bidList.size());
        System.out.println(s + "Number of auction asks: " + this.auctionAskList.size());
        //System.out.println(s+"Federated Commission (" + Math.round(FederatedCoordinator.getDefaultCommission() * 100) + "%): " + Math.round(FederatedCoordinator.getInstance().getCommission()));

        // new way
        synchronized (WAITING_MAP_LOCK) {

            int bidCounter = 0;
            for (Map.Entry<Bid, AuctionAsk> entry : this.waitingMap.entrySet()) {
                bidCounter++;
                String footer = "";
                String header = "" + bidCounter;
                while (header.length() < 3) {
                    header = "0" + header;
                }
                footer = "  END AUCTION # " + header + " ";
                header = "  BEGIN AUCTION # " + header + "  ";

                Bid bid = entry.getKey();
                BidData bidInitial = bid.getBidData();
                BidData bidModified = null;
                if (bid.getOriginalBidData() != null) {
                    bidModified = bidInitial;
                    bidInitial = bid.getOriginalBidData();
                }

                selectedBidModified = bidModified;
                selectedBid = bidInitial;
                selectedAgent = bid.getAgent();

                AuctionAsk ask = entry.getValue();

                Map<Integer, IdentityResource[]> join = new HashMap<Integer, IdentityResource[]>();
                if (bidInitial != null) {
                    for (IdentityResource ir : bidInitial.getIdentityResources()) {
                        int id = ir.getResourceType().getId();
                        if (!join.containsKey(id)) {
                            join.put(id, new IdentityResource[]{null, null, null});
                        }
                        join.get(id)[0] = ir;
                    }
                }

                if (ask != null) {
                    for (IdentityResource ir : ask.getIdentityResources()) {
                        int id = ir.getResourceType().getId();
                        if (!join.containsKey(id)) {
                            join.put(id, new IdentityResource[]{null, null, null});
                        }
                        join.get(id)[1] = ir;
                    }

                    selectedAuctionAsk = ask;
                    selectedServiceProvider = ask.getServiceProvider();
                }

                int count_adaptation = 0;
                double[] basePorcentage = {0, 0, 0, 0};
                double[] porcentage = {0, 0, 0, 0};
                double baseTotal = 0;

                if (bidModified != null) {
                    for (IdentityResource ir : bidModified.getIdentityResources()) {
                        int id = ir.getResourceType().getId();
                        if (!join.containsKey(id)) {
                            join.put(id, new IdentityResource[]{null, null, null});
                        }
                        IdentityResource initial = join.get(id)[0];
                        IdentityResource adapted = ir;
                        if (initial.getResourceType().getId() == adapted.getResourceType().getId()) {
                            // TODO check comission and price according to each feature
                            double p = 0;
                            if (adapted.getPriority().getLevel() > initial.getPriority().getLevel()) {
                                p = adapted.getPriority().getLevel() - initial.getPriority().getLevel();
                                baseTotal += p;
                                basePorcentage[count_adaptation] = p;
                                count_adaptation++;
                            }
                        }
                        join.get(id)[2] = ir;
                    }
                    for (int p = 0; p < basePorcentage.length; p++) {
                        porcentage[p] = basePorcentage[p] / baseTotal;
                    }
                }

                /**
                 * **********************************************
                 */
                /* PRICE */
                /**
                 * **********************************************
                 */
                float price = 0;
                double total_maxprice = 0;
                double total_selling_price = 0;
                double total_profit = 0;
                double total_commission = 0;

                String[] bidTextInitial = new String[]{"--", "--", "--", "--", "--"};
                String[] bidTextModified = new String[]{"--", "--", "--", "--", "--"};
                if (bidInitial != null) {
                    price = bidInitial.getPreferredPrice();
                    bidTextInitial[0] = "Id=" + bidInitial.hashCode();
                    //TODO check how to pass the price
                    bidTextInitial[1] = "F-H Price=" + Math.round(this.firstHighestPrice * factorA + 0.5) / factorB;
                    bidTextInitial[2] = "S-H Price=" + Math.round(this.secondHighestPrice * factorA + 0.5) / factorB;
                    // no required /////bidTextInitial[3] = "Price=" + Math.round(bidInitial.getPreferredPrice() * 100 + 0.5) / 100.0;
                }

                String[] askText = new String[]{"--", "--", "--", "--", "--"};
                int icommission = 0;
                if (ask != null) {
                    price = this.secondHighestPrice;
                    if (bidModified != null) {
                        price = bidModified.getPreferredPrice();
                    }
                    total_selling_price = price;
                    int revenue = ask.getServiceProvider().getRevenue();
                    icommission = Math.round(price * FederatedCoordinator.getDefaultCommission());
                    askText[0] = "Id=" + ask.hashCode();
                    //TODO check how to pass the price
                    askText[1] = "O-P Price=" + Math.round(this.initialOfferPrice * factorA + 0.5) / factorB;
                    double profit = price - ask.getTotalCosts();

                    total_profit += profit;
                    askText[2] = "Profit=" + Math.round(profit) + " (" + Math.round(profit / price * factorA) + "%)";
                    //askText[2]="Revenue="+Math.round(revenue);
                }

                total_maxprice = this.userMaxPrice;
                if (bidModified != null) {
                    price = bidModified.getPreferredPrice();
                    total_maxprice = bidModified.getPreferredPrice() * (100.0 + bidModified.getMaxIncrementPercentage()) / 100.0;
                    bidTextModified[0] = "Id=" + bidModified.hashCode();
                    //TODO check how to pass the price
                    bidTextModified[1] = "T-A Price=" + Math.round(bidModified.getPreferredPrice() * 100 + 0.5) / 100.0;
                    bidTextModified[2] = "Fed.Commission=" + Math.round(icommission);
                }
                total_commission += icommission;
                FederatedCoordinator.getInstance().addCommission(icommission * 1.0f);
                //total_maxprice=this.initialOfferPrice*(100+bid.getMaxIncrementPercentage())/100;

                //total_maxprice=bid.calculateCurrentOffer((float)total_selling_price);
                /**
                 * ***************************************************
                 */
                /*
                 if(count_adaptation>1)
                 {
                 total_maxprice/=count_adaptation;
                 total_selling_price/=count_adaptation;
                 total_profit/=count_adaptation;
                 total_commission/=count_adaptation;
                 }*/
                count_adaptation = 0;

                int row = 0;
                ArrayList<ArrayList<String>> model = new ArrayList<>();
                ConcurrentHashMap<String, Integer> param = new ConcurrentHashMap<>();
                ConcurrentHashMap<String, String> paramFormat = new ConcurrentHashMap<>();

                for (Map.Entry<Integer, IdentityResource[]> j : join.entrySet()) {
                    IdentityResource.ResourceType rt = IdentityResource.ResourceType.createByNumber(j.getKey());
                    IdentityResource irBidInitial = j.getValue()[0];
                    IdentityResource irAsk = j.getValue()[1];
                    IdentityResource irBidModified = j.getValue()[2];
                    String pnameBidInitial = "";
                    String pnameAsk = "";
                    String pnameBidModified = "";
                    String priceBidInitial = "";
                    String priceAsk = "";
                    String priceBidModified = "";

                    if (irBidInitial != null) {
                        pnameBidInitial = irBidInitial.getPriority().name();
                        if (irBidInitial.getCost() != null) {
                            priceBidInitial = "" + irBidInitial.getCost();
                        }
                    }
                    if (irAsk != null) {
                        pnameAsk = irAsk.getPriority().name();
                        ////priceAsk=""+irAsk.getCost();
                    }
                    if (irBidModified != null) {
                        pnameBidModified = irBidModified.getPriority().name();
                        if (irBidModified.getCost() != null) {
                            priceBidModified = "" + irBidModified.getCost();
                        }
                    }

                    if (pnameBidModified == "") {
                        pnameBidModified = "--";
                    }
                    if (pnameAsk == pnameBidInitial) {
                        pnameAsk = "--";
                    }
                    if (pnameBidModified == pnameBidInitial) {
                        pnameBidModified = "--";
                    }
                    if (row == 0) {
                        // titles
                        Utilities.addValue(model, Utilities.COL_FEATURE, row, "Features");
                        Utilities.addValue(model, Utilities.COL_ASSET, row, "Asset");
                        Utilities.addValue(model, Utilities.COL_FORMER_SECURITY_LEVEL, row, "Former Security Level");
                        Utilities.addValue(model, Utilities.COL_ADAPTED_SECURITY_LEVEL, row, "Adapted Security Level");
                        Utilities.addValue(model, Utilities.COL_SERVICE, row, "Service(s)");
                        Utilities.addValue(model, Utilities.COL_SERVICE_PROVIDER, row, "Service Provider");
                        Utilities.addValue(model, Utilities.COL_DATARATE, row, "Data Rate");
                        Utilities.addValue(model, Utilities.COL_CPU, row, "CPU");
                        Utilities.addValue(model, Utilities.COL_MEMORY, row, "Memory");
                        Utilities.addValue(model, Utilities.COL_DISKSPACE, row, "Disk Space");
                        Utilities.addValue(model, Utilities.COL_USER_MAXPRICE, row, "User MaxPrice");
                        Utilities.addValue(model, Utilities.COL_SELLINGPRICE, row, "Selling Price");
                        Utilities.addValue(model, Utilities.COL_CLOUDSP_PROFIT, row, "Cloud SP Profit");
                        Utilities.addValue(model, Utilities.COL_FEDERATED_COMMISSION, row, "F. Coordinator Commission");
                        Utilities.addValue(model, Utilities.COL_AUCTION_MODEL, row, "Auction Model");

                        Utilities.setFormat(paramFormat, Utilities.COL_FEATURE, Utilities.FMT_COLUMN_ALIGN, Utilities.FMT_COLUMN_ALIGN$$RIGHT);
                        Utilities.setFormat(paramFormat, Utilities.COL_USER_MAXPRICE, Utilities.FMT_COLUMN_ALIGN, Utilities.FMT_COLUMN_ALIGN$$RIGHT);
                        Utilities.setFormat(paramFormat, Utilities.COL_SELLINGPRICE, Utilities.FMT_COLUMN_ALIGN, Utilities.FMT_COLUMN_ALIGN$$RIGHT);
                        Utilities.setFormat(paramFormat, Utilities.COL_CLOUDSP_PROFIT, Utilities.FMT_COLUMN_ALIGN, Utilities.FMT_COLUMN_ALIGN$$RIGHT);
                        Utilities.setFormat(paramFormat, Utilities.COL_FEDERATED_COMMISSION, Utilities.FMT_COLUMN_ALIGN, Utilities.FMT_COLUMN_ALIGN$$RIGHT);

                        Utilities.addValue(model, Utilities.COL_ASSET_FEATURE, row, "Feature");
                        //Utilities.setFormat(paramFormat, Utilities.COL_ASSET_FEATURE, Utilities.FMT_COLUMN_ALIGN, Utilities.FMT_COLUMN_ALIGN$$RIGHT);

                        row++;
                    }

                    // Data
                    Utilities.addValue(model, Utilities.COL_FEATURE, row, rt.name());
                    Utilities.addValue(model, Utilities.COL_FORMER_SECURITY_LEVEL, row, pnameBidInitial);
                    Utilities.addValue(model, Utilities.COL_ADAPTED_SECURITY_LEVEL, row, pnameBidModified);

                    Integer[] defaultResourceArray = Utilities.getDefaultResourceArray();
                    Integer[] resourceArray = Utilities.getRandomResourceArray();
                    //col_service, etc are just references to find the sp etc etc
                    String[] extraData = Utilities.getExtraData(irBidInitial, rt, ask, resourceArray);

                    extraData[Utilities.DAT_ASSET] = irBidInitial.getAsset().getDescription();
                    String valueAsset = Utilities.getAssetId(extraData[Utilities.DAT_ASSET]);
                    String valueSecurityLevel = pnameBidModified;
                    String valueService = Utilities.getServiceId(extraData[Utilities.DAT_SERVICE]);
                    String valueFeature = irBidInitial.getFeature().getDescription();
                    Utilities.addValue(model, Utilities.COL_ASSET_FEATURE, row, valueFeature);

                    Utilities.addValue(model, Utilities.COL_SERVICE, row, extraData[Utilities.DAT_SERVICE]);
                    Utilities.addValue(model, Utilities.COL_SERVICE_PROVIDER, row, extraData[Utilities.DAT_SERVICE_PROVIDER]);

                    //************************************************************************NEW CODE GIANNIS******************************************************************
                    Utilities.addValue(model, Utilities.COL_DATARATE, row, extraData[Utilities.DAT_DATARATE]);
                    if (extraData[Utilities.DAT_DATARATE].equals("--") || pnameBidModified.equals("--")) {
                        resourceArray[0] = defaultResourceArray[0];
                    } else {
                        doResource(extraData, Utilities.DAT_DATARATE, "DataRate", "m");
                    }

                    Utilities.addValue(model, Utilities.COL_CPU, row, extraData[Utilities.DAT_CPU]);
                    if (extraData[Utilities.DAT_CPU].equals("--") || pnameBidModified.equals("--")) {
                        resourceArray[1] = defaultResourceArray[1];
                    } else {
                        doResource(extraData, Utilities.DAT_CPU, "CPU", "C");
                    }

                    Utilities.addValue(model, Utilities.COL_MEMORY, row, extraData[Utilities.DAT_MEMORY]);
                    if (extraData[Utilities.DAT_MEMORY].equals("--") || pnameBidModified.equals("--")) {
                        resourceArray[2] = defaultResourceArray[2];
                    } else {
                        doResource(extraData, Utilities.DAT_MEMORY, "Memory", "M");
                    }

                    Utilities.addValue(model, Utilities.COL_DISKSPACE, row, extraData[Utilities.DAT_DISKSPACE]);
                    if (extraData[Utilities.DAT_DISKSPACE].equals("--") || pnameBidModified.equals("--")) {
                        resourceArray[3] = defaultResourceArray[3];
                    } else {
                        doResource(extraData, Utilities.DAT_DISKSPACE, "DiskSpace", "G");
                    }

                    Utilities.addValue(model, Utilities.COL_ASSET, row, extraData[Utilities.DAT_ASSET]);

                    //************************************************************************END OF NEW CODE GIANNIS*****************************************************************
                    if (pnameBidModified.equals("--")) {
                        Utilities.addValue(model, Utilities.COL_ASSET, row, "--");
                        Utilities.addValue(model, Utilities.COL_SERVICE, row, "--");
                        Utilities.addValue(model, Utilities.COL_SERVICE_PROVIDER, row, "--");
                        Utilities.addValue(model, Utilities.COL_DATARATE, row, "--");
                        Utilities.addValue(model, Utilities.COL_CPU, row, "--");
                        Utilities.addValue(model, Utilities.COL_MEMORY, row, "--");
                        Utilities.addValue(model, Utilities.COL_DISKSPACE, row, "--");

                    } else {
                        // add data collector only for modified bids
                        if (selectedAuctionAsk != null) {
                            DataCollector.add(selectedAgent, selectedBid, selectedBidModified, selectedServiceProvider, selectedAuctionAsk, valueAsset, valueSecurityLevel, valueService, resourceArray);
                        }
                    }

                    String type;
                    if (AgentManager.getInstance().isRandom()) {
                        type = "English";
                    } else {
                        type = "Posted Offer  ";
                    }

                    String[] sprice = {"--", "--", "--", "--"};

                    if (!pnameBidModified.equals("--")) {
                        double p = porcentage[count_adaptation];
                        sprice[0] = "$ " + Math.floor(total_maxprice * p * factorA + 0.5) / factorB;
                        sprice[1] = "$ " + Math.floor(total_selling_price * p * factorA + 0.5) / factorB;
                        sprice[2] = "$ " + Math.floor(total_profit * p * factorA + 0.5) / factorB;
                        sprice[3] = "$ " + Math.floor(total_commission * p * factorA + 0.5) / factorB;
                        count_adaptation++;

                        Utilities.println4Data(this.hashCode(), rt.name(), pnameBidInitial, pnameBidModified, type,
                                Math.floor(total_maxprice * p * factorA + 0.5) / factorB,
                                Math.floor(total_selling_price * p * factorA + 0.5) / factorB,
                                Math.floor(total_profit * p * factorA + 0.5) / factorB,
                                Math.floor(total_commission * p * factorA + 0.5) / factorB
                        );
                    }

                    Utilities.addValue(model, Utilities.COL_USER_MAXPRICE, row, sprice[0]);
                    Utilities.addValue(model, Utilities.COL_SELLINGPRICE, row, sprice[1]);
                    Utilities.addValue(model, Utilities.COL_CLOUDSP_PROFIT, row, sprice[2]);
                    Utilities.addValue(model, Utilities.COL_FEDERATED_COMMISSION, row, sprice[3]);

                    Utilities.addValue(model, Utilities.COL_AUCTION_MODEL, row, type);
                    //System.out.printf(s+"%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", rt.name(), pnameBidInitial, priceBidInitial, pnameAsk, priceAsk, pnameBidModified, priceBidModified);
                    row++;
                }

                Utilities.prepareParam(model, param);
                System.out.println(Utilities.getLine(param, "/", ""));
                String type;
                if (AgentManager.getInstance().isRandom()) {
                    type = "OPEN  OUTCRY  ";
                } else {
                    type = "POSTED OFFER  ";
                }
                System.out.println(Utilities.getLine(param, type, ""));
                System.out.println(Utilities.getLine(param, "\\", ""));
                System.out.println();
                System.out.println(Utilities.getLine(param, "*", header));

                Utilities.printModel(model, param, paramFormat);
                System.out.println(Utilities.getLine(param, "*", footer));

                //************************************************************************NEW CODE GIANNIS******************************************************************
                System.out.println();
                System.out.println(String.format(String.format("%%0%dd", 35), 0).replaceAll("0", "+++"));
                System.out.format("%20s%65s", "Service Provider", "Available Resources (after allocation)");
                System.out.println();
                System.out.format("%40s%15s%26s%20s", "Data Rate", "CPU", "Memory", "Disk Space");
                System.out.println();
                System.out.println(String.format(String.format("%%0%dd", 35), 0).replaceAll("0", "==="));

                int lastDataRate = 0;
                int lastCPU = 0;
                int lastMemory = 0;
                int lastDiskSpace = 0;

                for (int i = 0; i < ServiceProviderManager.serviceProviderNameList.size(); i++) {
                    String name = ServiceProviderManager.serviceProviderNameList.get(i);
                    //initial resources of each SP
                    lastDataRate = ServiceProviderManager.serviceProviderResourceList.get(i).get(0);
                    lastCPU = ServiceProviderManager.serviceProviderResourceList.get(i).get(1);
                    lastMemory = ServiceProviderManager.serviceProviderResourceList.get(i).get(2);
                    lastDiskSpace = ServiceProviderManager.serviceProviderResourceList.get(i).get(3);

                    String initialDataRate = Integer.toString(lastDataRate) + " mbits/s";
                    String initialCPU = Integer.toString(lastCPU) + " CPU core @ 2.1Ghz";
                    String initialMemory = Integer.toString(lastMemory) + " Mbyte";
                    String initialDiskSpace = Integer.toString(lastDiskSpace) + " Gbyte";

                    System.out.format("%-15s%25s%26s%18s%15s", name, initialDataRate, initialCPU, initialMemory, initialDiskSpace);
                    System.out.println();
                }
                System.out.println(String.format(String.format("%%0%dd", 35), 0).replaceAll("0", "==="));

                if (RecordBorrowedResources.isEmpty()) {
                    System.out.println("****Resources Leased between Service Providers:****");
                    System.out.println("N/A");

                } else {
                    System.out.println("****Resources Leased between Service Providers:****");
                    for (int k = 0; k < RecordBorrowedResources.size(); k++) {
                        System.out.println(RecordBorrowedResources.get(k));
                    }

                    RecordBorrowedResources = null; // re-instantiate record for next auction
                }

                System.out.println(String.format(String.format("%%0%dd", 35), 0).replaceAll("0", "+++"));

            }
        }

        //************************************************************************END OF NEW CODE GIANNIS*****************************************************************
        /*  
        
       
         if(false)
         {
         synchronized (WAITING_MAP_LOCK) {
         System.out.printf(s+"%n"+s);
         for (int i = 0; i < 16 + 18 * 3 + 2 * 2; i++) {
         System.out.print("/");
         }
         System.out.printf("%n"+s);
         for (int i = 0; i < (16 + 18 * 3 + 2 * 2) / 16; i++) {
         if(AgentManager.getInstance().isRandom())
         System.out.print("RANDOM AUCTION  ");
         else
         System.out.print("MODELLED AUCTION  ");
         }
         System.out.printf("%n"+s);
         for (int i = 0; i < 16 + 18 * 3 + 2 * 2; i++) {
         System.out.print("\\");
         }
         System.out.printf("%n");

         int bidCounter = 0;
         for (Map.Entry<Bid, AuctionAsk> entry : this.waitingMap.entrySet()) {
         bidCounter++;
         String footer = "";
         String header = "" + bidCounter;
         while (header.length() < 3) {
         header = "0" + header;
         }
         footer = "  END AUCTION # " + header + " ";
         header = "  BEGIN AUCTION # " + header + "  ";
         while (header.length() < 16 + 18 * 3 + 2 * 2) {
         if (header.length() % 2 == 0) {
         header += "*";
         } else {
         header = "*" + header;
         }
         }
         while (footer.length() < 16 + 18 * 3 + 2 * 2) {
         if (footer.length() % 2 == 0) {
         footer += "*";
         } else {
         footer = "*" + footer;
         }
         }

         System.out.printf("%s%n%s%n",s,s);
         System.out.println(s+header);

         System.out.printf(s+"%-15s %-17s   %-17s   %-17s%n", "", "Current State", "Matched Ask", "Adapted State");
         System.out.print(s);
         for (int i = 0; i < 16 + 18 * 3 + 2 * 2; i++) {
         System.out.print("-");
         }
         System.out.println();
         Bid bid = entry.getKey();
         Bid bidInitial = bid;
         Bid bidModified = null;
         if (bid.getOriginal() != null) {
         bidModified = bid;
         bidInitial = bid.getOriginal();
         }
         AuctionAsk ask = entry.getValue();

         Map<Integer, IdentityResource[]> join = new HashMap<Integer, IdentityResource[]>();
         if (bidInitial != null) {
         for (IdentityResource ir : bidInitial.getIdentityResources()) {
         int id = ir.getResourceType().getId();
         if (!join.containsKey(id)) {
         join.put(id, new IdentityResource[]{null, null, null});
         }
         join.get(id)[0] = ir;
         }
         }

         if (ask != null) {
         for (IdentityResource ir : ask.getIdentityResources()) {
         int id = ir.getResourceType().getId();
         if (!join.containsKey(id)) {
         join.put(id, new IdentityResource[]{null, null, null});
         }
         join.get(id)[1] = ir;
         }
         }

         if (bidModified != null) {
         for (IdentityResource ir : bidModified.getIdentityResources()) {
         int id = ir.getResourceType().getId();
         if (!join.containsKey(id)) {
         join.put(id, new IdentityResource[]{null, null, null});
         }
         join.get(id)[2] = ir;
         }
         }

         float price = 0;

         String[] bidTextInitial = new String[]{"--", "--", "--", "--", "--"};
         String[] bidTextModified = new String[]{"--", "--", "--", "--", "--"};
         if (bidInitial != null) {
         price = bidInitial.getPreferredPrice();
         bidTextInitial[0] = "Id=" + bidInitial.hashCode();
         //TODO check how to pass the price
         bidTextInitial[1] = "F-H Price="+ Math.round(this.firstHighestPrice*factorA+0.5)/factorB;
         bidTextInitial[2] = "S-H Price=" + Math.round(this.secondHighestPrice*factorA+0.5)/factorB;
         // no required /////bidTextInitial[3] = "Price=" + Math.round(bidInitial.getPreferredPrice() * 100 + 0.5) / 100.0;
         }

         String[] askText = new String[]{"--", "--", "--", "--", "--"};
         int icommission=0;
         if (ask != null) {
         price=this.secondHighestPrice;
         if (bidModified!=null) price=bidModified.getPreferredPrice();
         int revenue = ask.getServiceProvider().getRevenue();
         icommission = Math.round(price * FederatedCoordinator.getDefaultCommission());
         askText[0] = "Id=" + ask.hashCode();
         //TODO check how to pass the price
         askText[1] = "O-P Price=" + Math.round(this.initialOfferPrice*factorA+0.5)/factorB;
         double profit=price - ask.getTotalCosts();
         askText[2] = "Profit=" + Math.round(profit)+" ("+Math.round(profit/price*factorA)+"%)";
         //askText[2]="Revenue="+Math.round(revenue);
         }

         if (bidModified != null) {
         price = bidModified.getPreferredPrice();
         bidTextModified[0] = "Id=" + bidModified.hashCode();
         //TODO check how to pass the price
         bidTextModified[1] = "T-A Price=" + Math.round(bidModified.getPreferredPrice() * 100 + 0.5) / 100.0;
         bidTextModified[2] = "Fed.Commission=" + Math.round(icommission);
         }
         FederatedCoordinator.getInstance().addCommission(icommission*1.0f);

         for (int i = 0; i < 4; i++) {
         System.out.printf(s+"%-15s %-17s   %-17s   %-17s %n", "", bidTextInitial[i], askText[i], bidTextModified[i]);
         }

         System.out.printf(s+"%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", "Features", "Priority", "", "Priority", "", "Priority", "");
         System.out.print(s);
         for (int i = 0; i < 16 + 6 * 9 + 2 * 2; i++) {
         System.out.print("=");
         }
         System.out.println();

         for (Map.Entry<Integer, IdentityResource[]> j : join.entrySet()) {
         IdentityResource.ResourceType rt = IdentityResource.ResourceType.createByNumber(j.getKey());
         IdentityResource irBidInitial = j.getValue()[0];
         IdentityResource irAsk = j.getValue()[1];
         IdentityResource irBidModified = j.getValue()[2];
         String pnameBidInitial = "";
         String pnameAsk = "";
         String pnameBidModified = "";
         String priceBidInitial = "";
         String priceAsk = "";
         String priceBidModified = "";

         if (irBidInitial != null) {
         pnameBidInitial = irBidInitial.getPriority().name();
         if (irBidInitial.getCost() != null) {
         priceBidInitial = "" + irBidInitial.getCost();
         }
         }
         if (irAsk != null) {
         pnameAsk = irAsk.getPriority().name();
         ////priceAsk=""+irAsk.getCost();
         }
         if (irBidModified != null) {
         pnameBidModified = irBidModified.getPriority().name();
         if (irBidModified.getCost() != null) {
         priceBidModified = "" + irBidModified.getCost();
         }
         }

         if (pnameBidModified == "") {
         pnameBidModified = "--";
         }
         if (pnameAsk == pnameBidInitial) {
         pnameAsk = "--";
         }
         if (pnameBidModified == pnameBidInitial) {
         pnameBidModified = "--";
         }
         System.out.printf(s+"%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", rt.name(), pnameBidInitial, priceBidInitial, pnameAsk, priceAsk, pnameBidModified, priceBidModified);
         //System.out.printf("%-30s %-30s %n", bid.toString(), ask.toString());
         }
         System.out.println(s+footer);
         System.out.println(s);
         System.out.println(s+"F-H Price=First-Highest Price");
         System.out.println(s+"S-H Price=Second-Highest Price");
         System.out.println(s+"O-P Price=Opening Price");
         System.out.println(s+"T-A Price=Total Price After Adaptation");
         }
         }
         }
         */
    }

    public void doResource(String[] extraData, int DAT_RESOURCE, String resourceName, String splitLetter) {
        String[] seperateTemp;
        if (extraData[DAT_RESOURCE].equals("--")) {

        } else {
            int indexResource = ServiceProviderManager.serviceProviderNameList.indexOf(extraData[Utilities.DAT_SERVICE_PROVIDER]);

            //update the available diskpace resources of each SP, when allocating resources to their users, To do this we need split the required amount from the measuring unit and substract it from the available memory resource of the seller
            String resourceAmount = extraData[DAT_RESOURCE];
            seperateTemp = resourceAmount.split(" " + splitLetter);
            int subtractedResource = Integer.valueOf(seperateTemp[0]); //contains the resource amount required

            //Find the sellers and the amount of resources sold and update their available resources by withdrawing the allocated resources
            if (indexResource != -1) {
                String serviceProviderName = ServiceProviderManager.serviceProviderNameList.get(indexResource);

                //System.out.println("do resource "+serviceProviderName+" : "+resourceAmount);
                List<Integer> serviceResource = ServiceProviderManager.serviceProviderResourceList.get(indexResource);
                if (serviceResource.get(DAT_RESOURCE - 2) >= subtractedResource) { //check if the memory resource of the SP is sufficient for providing service to the sp
                    System.out.println("TESTING " + resourceName + ": " + serviceProviderName + " before:" + serviceResource.get(DAT_RESOURCE - 2)); //TESTING
                    serviceResource.set(DAT_RESOURCE - 2, serviceResource.get(DAT_RESOURCE - 2) - subtractedResource);
                    System.out.println("TESTING " + resourceName + ": " + serviceProviderName + " after:" + serviceResource.get(DAT_RESOURCE - 2)); //TESTING

                } else {//if resources inadequate, find SPs from which the seller can borrow resources

                    for (int i = 0; i < ServiceProviderManager.serviceProviderResourceList.size(); i++) {

                        if (ServiceProviderManager.serviceProviderResourceList.get(i).get(DAT_RESOURCE - 2) > subtractedResource) {
                            System.out.println("BORROWING " + resourceName + " from " + ServiceProviderManager.serviceProviderNameList.get(i));
                            //keep record of borrowed Resources
                            RecordBorrowedResources.add(extraData[Utilities.DAT_SERVICE_PROVIDER] + " leased " + resourceAmount + " from " + ServiceProviderManager.serviceProviderNameList.get(i));
                            //update the available resources of the seller
                            ServiceProviderManager.serviceProviderResourceList.get(i).set(DAT_RESOURCE - 2, ServiceProviderManager.serviceProviderResourceList.get(i).get(DAT_RESOURCE - 2) - subtractedResource);
                            break; //if extra required resources are acquired then exit the loop, no need for further resources
                        }
                    }
                }
            }
        }

    }

}
