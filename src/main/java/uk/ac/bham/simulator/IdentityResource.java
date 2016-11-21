package uk.ac.bham.simulator;

/**
 *
 * @author 
 * 
 * modified
 * @author Francisco Ramirez
 * @version 1.2
 */
public class IdentityResource 
{
    //private Integer price;
    //private Integer MaxPrice;
    private Long Duration;
    private ResourceType resourceType;
    private SecurityLevel securityLevel;   
    private Integer cost;
    
    private Asset asset;
    private Feature feature;
   

    
    public enum ResourceType 
    {
        Availability(1), 
        Anonymity(2), 
        Integrity(3), 
        Confidentiality(4);
        
        private final int id;
        
        ResourceType(int id) 
        {
            this.id = id;
        }

        public int getId() 
        { 
            return id;
        }
        
        public static ResourceType createByNumber(int id)
        {            
            ResourceType instance = null;
            for (ResourceType p : ResourceType.values())
            {
                if (p.getId()==id)
                {
                    instance = p;
                    break;
                }
            }
            return instance;
        }
    }
       
    public enum SecurityLevel 
    {         
        Unsecured(1.0f), 
        FairlySecured(1.3f),
        Secured(1.6f);
        private final float level;        
        
        SecurityLevel(float level) 
        {
            this.level = level;
        }
        
        public static SecurityLevel createByNumber(float id)
        {            
            SecurityLevel instance = null;
            for (SecurityLevel p : SecurityLevel.values())
            {
                if (p.getLevel()==id)
                {
                    instance = p;
                    break;
                }
            }
            return instance;
        }
        
        public float getLevel() 
        { 
            return level;
        }
    }
    
    /**
     * @return the price
     */
    /*public Integer getPrice() 
    {
        return price;
    }*/

    /**
     * @param price the price to set
     */
    /*public void setPrice(Integer price) 
    {
        this.price = price;
    }*/    
    

     /**
     * @return the resourceType
     */
    public ResourceType getResourceType() 
    {
        return resourceType;
    }

    /**
     * @param resourceType the resourceType to set
     */
    public void setResourceType(ResourceType resourceType) 
    {
        this.resourceType = resourceType;
    }

    /**
     * @return the priority
     */
    public SecurityLevel getPriority() 
    {
        return securityLevel;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(SecurityLevel priority) 
    {
        this.securityLevel = priority;
    }
    
    public void setDurationOfAuction (Long time)
    {
        this.Duration=time;
    }
    
    public Long getDurationOfAuction()
    {
        return Duration;
    }
    
    /*public void setMaxPrice(Integer MaxPrice)
    {
        this.MaxPrice=MaxPrice;
    }
    
    public Integer getMaxPrice()
    {
        return MaxPrice;
    }*/
    
/**
     * @return the cost
     */
    public Integer getCost() 
    {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(Integer cost) 
    {
        this.cost = cost;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    
    

    
}
