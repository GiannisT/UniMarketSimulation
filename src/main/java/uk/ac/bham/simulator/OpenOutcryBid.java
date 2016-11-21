package uk.ac.bham.simulator;

import java.util.ArrayList;
import uk.ac.bham.simulator.IdentityResource.SecurityLevel;
import uk.ac.bham.simulator.IdentityResource.ResourceType;

/**
 *
 * @author 
 */
public class OpenOutcryBid extends Bid
{
    private static final int MINPRICE = 80;
    private static final int MAXPRICE = 100;
    private static final int MINPRIORITY = 1;
    private static final int MAXPRIORITY = 3;
    private static final int FIRSTRESOURCETYPE = 1;//Availability(1), Anonymity(2),         
    private static final int LASTRESOURCETYPE = 4; //Integrity(3), Confidentiality(4);
        
    public OpenOutcryBid(Agent agent)
    {
        super(agent);
    }
    
    @Override
    public void configIdentityResources()
    {        
        int resourceTypeId = FIRSTRESOURCETYPE;
         
        this.getBidData().setPreferredPrice(Utilities.generateRandomInteger(600, 700));
        while (resourceTypeId <= LASTRESOURCETYPE)        
        {
            IdentityResource identityResource = new IdentityResource();
            //identityResource.setPrice(Utilities.generateRandomInteger(MINPRICE, MAXPRICE));
            //identityResource.setCost(100);
            //identityResource.setMinimumProfit(Utilities.generateRandomInteger(40, 50));
            //identityResource.setPreferredProfit(Utilities.generateRandomInteger(51, 100));
            identityResource.setPriority(SecurityLevel.Unsecured);// .createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY))
            identityResource.setResourceType(ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
            Asset asset=Asset.createRandom();
            Feature feature = Feature.createRandom(asset);
            identityResource.setAsset(asset);
            identityResource.setFeature(feature);
            setTimeOfSubmission(System.currentTimeMillis()); //used for creating the points in the graph
            // with machine learning prediction
            /*
            if (FederatedCoordinator.strategy.isReady()) {
                String asset_feature=identityResource.getAsset().name()+","+identityResource.getFeature().name();
                String dataset=this.getAgent().toDataset()+","+asset_feature+",?";
                String sSecurityLevel=FederatedCoordinator.strategy.predictOneRow(dataset);
                SecurityLevel p = SecurityLevel.valueOf(sSecurityLevel);
                identityResource.setPriority(p);
            } else {
                // TODO check this
                System.out.println("MACHINE LEARNING STRATEGY IS NOT READY");
                identityResource.setPriority(SecurityLevel.Unsecured);// .createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY))
            }
            */

            // before without machine learning
            /*
            identityResource.setPriority(SecurityLevel.Unsecured);// .createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY))
                    */
            getBidData().getIdentityResources().add(identityResource);
        }
    }
    
}
