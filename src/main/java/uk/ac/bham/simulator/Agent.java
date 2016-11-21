package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 *
 * @author 
 * 
 * modified
 * @author Francisco Ramirez
 * @version 1.2
 */
public class Agent 
{
    ArrayList<Bid> bids;
    IdentityProvider identityProvider;
    
    // user attributes which will be tracked before trigger adaptation
    Location location;
    Position rank;
    Device state;
    InformationSensitivity documentSecurity;
    
    
    public Agent(IdentityProvider identityProvider)
    {
        bids = new ArrayList<>();
        this.identityProvider = identityProvider;
        initAttributes();
    }
    
    public final void initAttributes() {
        location=Location.createRandom();
        rank=Position.createRandom();
        state=Device.createRandom();
        documentSecurity=InformationSensitivity.createRandom(rank); // check rank
    }
    
    
    
    public Bid createBid(boolean random)
    {
        Bid bid;
        if(random)
        {
            bid = new OpenOutcryBid(this);
        
        }
        else
        {
            bid = new PostedOfferBid(this);
        }
        bid.configIdentityResources(); 
        bid.getBidData().setMaxIncrementPercentage(Utilities.generateRandomInteger(1, 30));
        bids.add(bid);  
        return bid;
    }
    
    public void removeBid(Bid bid)
    {
       bids.remove(bid); 
    }
    
    public void requestAuthentication()
    {
        
    }
    
    public void requestPayment(double price, Bid bid)
    {
        FederatedCoordinator.getInstance().payForServiceExecution(price, bid);
    }
    
    public IdentityProvider getIdentityProvider()
    {
        return identityProvider;
    }
    
    @Override
    public String toString()
    {
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode();
    }    

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Position getRank() {
        return rank;
    }

    public void setRank(Position rank) {
        this.rank = rank;
    }

    public Device getState() {
        return state;
    }

    public void setState(Device state) {
        this.state = state;
    }

    public InformationSensitivity getDocumentSecurity() {
        return documentSecurity;
    }

    public void setDocumentSecurity(InformationSensitivity documentSecurity) {
        this.documentSecurity = documentSecurity;
    }
    
    public String toDataset() {
        String data=""+this.hashCode()+","+this.getRank().name()+","+this.getLocation().name()+","+
                this.getState().name()+","+this.getDocumentSecurity().name();
        return data;
    }
}
