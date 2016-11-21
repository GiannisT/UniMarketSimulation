package uk.ac.bham.simulator;

import java.security.SecureRandom;
import java.util.ArrayList;

public class PostedOfferAsk extends AuctionAsk {

    private static int MINPRICE;
    private static final int FIRSTRESOURCETYPE = 1;//Availability(1), Anonymity(2),         
    private static final int LASTRESOURCETYPE = 4; //Integrity(3), Confidentiality(4);
    ArrayList<Integer> PreviousAsksForAvailability = new ArrayList<Integer>();
    ArrayList<Integer> PreviousAsksForAnonymity = new ArrayList<Integer>();
    ArrayList<Integer> PreviousAsksForIntegrity = new ArrayList<Integer>();
    ArrayList<Integer> PreviousAsksForPerformance = new ArrayList<Integer>();

    public PostedOfferAsk(ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    @Override
    public void configIdentityResources() {
        int resourceTypeId = FIRSTRESOURCETYPE;
        int AvailSum = 0, AnonSum = 0, InteSum = 0, PerfSum = 0;
  
        while (resourceTypeId <= LASTRESOURCETYPE) {
            IdentityResource identityResource = new IdentityResource();

            if ( (!PreviousAsksForAnonymity.isEmpty() && !PreviousAsksForAvailability.isEmpty() && !PreviousAsksForIntegrity.isEmpty() && !PreviousAsksForPerformance.isEmpty())){
                if (resourceTypeId == 1) {
                    for (int i = 0; i < PreviousAsksForAvailability.size(); i++) {
                        AvailSum += PreviousAsksForAvailability.get(i);
                    }
                    MINPRICE = (AvailSum / PreviousAsksForAvailability.size()) - ((AvailSum * 10) / 100); //the SP sets the min price that the auction will begin by getting the average from the previous submitted asks and substracts 5% of the overall price
                    PreviousAsksForAvailability.add(MINPRICE);
                } else if (resourceTypeId == 2) {
                    for (int i = 0; i < PreviousAsksForAnonymity.size(); i++) {
                        AnonSum += PreviousAsksForAnonymity.get(i);
                    }
                    MINPRICE = (AnonSum / PreviousAsksForAnonymity.size()) - ((AnonSum * 10) / 100);
                    PreviousAsksForAnonymity.add(MINPRICE);
                } else if (resourceTypeId == 3) {
                    for (int i = 0; i < PreviousAsksForIntegrity.size(); i++) {
                        AnonSum += PreviousAsksForIntegrity.get(i);
                    }
                    MINPRICE = (InteSum / PreviousAsksForIntegrity.size()) - ((InteSum * 10) / 100);
                    PreviousAsksForIntegrity.add(MINPRICE);
                } else if (resourceTypeId == 4) {
                    for (int i = 0; i < PreviousAsksForPerformance.size(); i++) {
                        PerfSum += PreviousAsksForPerformance.get(i);
                    }
                    MINPRICE = (PerfSum / PreviousAsksForPerformance.size()) - ((PerfSum * 10) / 100);
                    PreviousAsksForPerformance.add(MINPRICE);
                }

                identityResource.setCost(DEFAULTCOST);                
                identityResource.setPriority(IdentityResource.SecurityLevel.Unsecured);
                identityResource.setResourceType(IdentityResource.ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
                identityResource.setDurationOfAuction(new Long(Utilities.generateRandomInteger(40000, 100000)));
                getIdentityResources().add(identityResource);
                
       }else{
                                  
            int Price=Utilities.generateRandomInteger(15, 25);
                        
            if(resourceTypeId==1){
                PreviousAsksForAvailability.add(Price);
            }else if(resourceTypeId==2){
                PreviousAsksForAnonymity.add(Price);
            }else if(resourceTypeId==3){
                PreviousAsksForIntegrity.add(Price);
            }else if(resourceTypeId==4){
                PreviousAsksForPerformance.add(Price);
            }
            
            identityResource.setCost(DEFAULTCOST);
            identityResource.setPriority(IdentityResource.SecurityLevel.Unsecured);
            identityResource.setResourceType(IdentityResource.ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
            identityResource.setDurationOfAuction(new Long(Utilities.generateRandomInteger(100000, 200000)));
            getIdentityResources().add(identityResource);
       }
     }
        // TODO minimum profit and preferred profit trend
        this.setMinimumProfit(Utilities.generateRandomInteger(40, 50));
        this.setPreferredProfit(Utilities.generateRandomInteger(51, 100));
        //this.setMinimumProfit(HistoricalPrice.getInstance().getValueAsInt("MINIMUM_PROFIT"));
        //this.setPreferredProfit(HistoricalPrice.getInstance().getValueAsInt("PREFERRED_PROFIT"));
        
        HistoricalPrice.getInstance().addPrice("MINIMUM_PROFIT", this.getMinimumProfit()*1.0f);
        HistoricalPrice.getInstance().addPrice("PREFERRED_PROFIT", this.getPreferredProfit()*1.0f);
   }
}
    
