package uk.ac.bham.simulator;

import java.util.ArrayList;
import uk.ac.bham.simulator.IdentityResource.SecurityLevel;
import uk.ac.bham.simulator.IdentityResource.ResourceType;

/**
 *
 * @author
 */
public class PostedOfferBid extends Bid {

    private static final int MINPRIORITY = 1;
    private static final int MAXPRIORITY = 3;
    private static final int FIRSTRESOURCETYPE = 1;//Availability(1), Anonymity(2),         
    private static final int LASTRESOURCETYPE = 4; //Integrity(3), Confidentiality(4);

    public PostedOfferBid(Agent agent) {
        super(agent);
    }

    @Override
    public void configIdentityResources() {

        int resourceTypeId = FIRSTRESOURCETYPE;
        int sum = 0, price = 0;
        ArrayList<AuctionAsk> CurAsk;
        this.getBidData().setPreferredPrice(price);
        
        while (resourceTypeId <= LASTRESOURCETYPE) {
            sum = 0;
            IdentityResource identityResource = new IdentityResource();
            CurAsk = new ArrayList<AuctionAsk>(FederatedCoordinator.getInstance().getCurrentAsks()); //it gets the available asks
            /*
            if (!CurAsk.isEmpty()) {
                for (int i = 0; i < CurAsk.size(); i++) { //try to get a sence of the CurrentPrices in the market for the requested resources hence to submit a bid that is realistic and within the current price range
                    sum += CurAsk.get(i).getIdentityResources().get(resourceTypeId).getPrice();
                }
                price = (int) (sum / CurAsk.size()) + (5 * sum) / 100; //a sensible price for start bidding is the avg of all prices for this resource + 5%
                identityResource.setPrice(price);
                
            } else if (CurAsk.isEmpty()) { // if there are no current Asks to compare prices the user should randomly specify a price
                identityResource.setPrice(Utilities.generateRandomInteger(30, 60)); //first valuation for start bidding
            }*/
            //identityResource.setMaxPrice(Utilities.generateRandomInteger(70, 200)); //Represents the higher valuation that a user can pay for a resource. The upper limit for bidding
            identityResource.setPriority(SecurityLevel.Unsecured);//.createByNumber(Utilities.generateRandomInteger(MINPRIORITY, MAXPRIORITY))
            identityResource.setResourceType(ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
            //TODO why cost is null for Random
            //identityResource.setCost(Math.round(HistoricalPrice.getInstance().getPrice(identityResource.getResourceType())));
            setTimeOfSubmission(System.currentTimeMillis()); //used for creating the points in the graph
            getBidData().getIdentityResources().add(identityResource);
        }
    }

    //this function will be called when the bid of a user is surpassed by the bid of another user and the user wants to rebid
    public int Rebid(int CurrentPrice, int MaxPrice) {
        int newBidPrice = 0;

        if (CurrentPrice < MaxPrice) {
            newBidPrice = Utilities.generateRandomInteger(CurrentPrice, MaxPrice);
            return newBidPrice;
        } else {
            return -1; //this valuation illustrates that the user want's to exit the certain auction and not to submit a higher valuation
        }
    }
    
    
}
