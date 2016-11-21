package uk.ac.bham.simulator;

public class OpenOutcryAsk extends AuctionAsk 
{
    //String[] ResourcesOffered;
    private static final int MINPRICE = 80;
    private static final int MAXPRICE = 100;    
    private static final int MINPRIORITY = 1;
    private static final int MAXPRIORITY = 3;
    private static final int FIRSTRESOURCETYPE = 1;//Availability(1), Anonymity(2),         
    private static final int LASTRESOURCETYPE = 4; //Integrity(3), Confidentiality(4);
    
    public OpenOutcryAsk(ServiceProvider serviceProvider)
    {
        super(serviceProvider);
    }
    
    @Override
    public void configIdentityResources()
    {        
        int resourceTypeId = FIRSTRESOURCETYPE;
        while (resourceTypeId <= LASTRESOURCETYPE)        
        {
            IdentityResource identityResource = new IdentityResource();
            identityResource.setCost(DEFAULTCOST);//Utilities.generateRandomInteger(MINPRICE, MAXPRICE)
            identityResource.setPriority(IdentityResource.SecurityLevel.Unsecured);//.createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY))
            identityResource.setResourceType(IdentityResource.ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
            getIdentityResources().add(identityResource);
        }
        this.setMinimumProfit(Utilities.generateRandomInteger(40, 50));
        this.setPreferredProfit(Utilities.generateRandomInteger(51, 100));
        
        HistoricalPrice.getInstance().addPrice("MINIMUM_PROFIT", this.getMinimumProfit()*1.0f);
        HistoricalPrice.getInstance().addPrice("PREFERRED_PROFIT", this.getPreferredProfit()*1.0f);
    }
}