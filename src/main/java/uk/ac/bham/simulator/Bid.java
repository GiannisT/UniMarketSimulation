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
public class Bid 
{
    
    private Agent agent;
    private BidData bidData;
    private BidData originalBidData;
    
    public BidData getOriginalBidData() {
        return originalBidData;
    }
    
    public BidData getBidData() {
        return bidData;
    }

    public void setOriginalBidData(BidData originalBidData) {
        this.originalBidData = originalBidData;
    }

    public Bid()
    {
        this.bidData=new BidData();
    }
    
    public Bid(Agent agent)
    {
        this();
        this.agent = agent;
    }
    
    public Agent getAgent()
    {
        return this.agent;
    }

    public void configIdentityResources()
    {
        
    }
    
    
    @Override
    public String toString()
    {
        String resource="";
        for (IdentityResource ir:this.getBidData().getIdentityResources())
        {
            resource+="|"+ir.getResourceType().name()+","+ir.getPriority().name()+","+getBidData().getPreferredPrice()/getBidData().getIdentityResources().size();
        }
        resource=resource.substring(1);
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode()+" {"+resource+"}";
    }
    
    
    //  ------------------------------------Remove if Graph not needed---------------------------------
    private Double SubmissionTime;
    
    public void setTimeOfSubmission(double time){
        this.SubmissionTime=time-FederatedCoordinator.getInstance().Initialtime; //by applying this we get the exact time a bid has submitted since the begining of running this software
    }
    
    public double getTimeOfSubmission(){
        return SubmissionTime;
    }
    
    public void modifiedBy(ArrayList<IdentityResource.ResourceType> resourceType, ArrayList<IdentityResource.SecurityLevel> priority)
    {
        float sum=0.0f;
        Float currentPrice=this.getBidData().getPreferredPrice(); //calculateCurrentOffer(this.getPreferredPrice());
        
        this.originalBidData=this.getBidData().clone();
        String concat="";
        for(int i=0; i<resourceType.size(); i++)
        {
            IdentityResource.ResourceType rt=resourceType.get(i);
            IdentityResource.SecurityLevel p=priority.get(i);
            
            for(IdentityResource ir:this.getBidData().getIdentityResources())
            {
                if (ir.getResourceType().equals(rt))
                {
                    IdentityResource.SecurityLevel oldPriority=ir.getPriority();
                    ir.setPriority(p);
                    float delta=p.getLevel()-oldPriority.getLevel();
                    if(delta > 0.0f )
                    {
                        sum+=delta;
                    }
                    concat+="\n"+
                        " resource="+rt.name()+" from "+oldPriority.name()+ " to "+p.name();
                }
            }
        }
        
        float newPrice=this.getBidData().getPreferredPrice()*(1+sum);
        this.getBidData().setPreferredPrice(newPrice);

        if (FederatedCoordinator.isDebugging()) System.out.println(this.originalBidData+" was modified\n"+concat.substring(1)+
                " old price="+currentPrice+", new price="+newPrice+
                "\n"+this);
        
    }
    
}
