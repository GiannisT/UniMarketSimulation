/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 *
 * @author Francisco Ramirez
 * @version 1.2
 */
public class BidData {
    private Integer maxIncrementPercentage;
    private ArrayList<IdentityResource> identityResources;  
    private float preferredPrice;
    
    public BidData() {
        identityResources = new  ArrayList<>();
    }

    public float getPreferredPrice() {
        return preferredPrice;
    }

    public void setPreferredPrice(float preferredPrice) {
        this.preferredPrice = preferredPrice;
    }
    
    /**
     * @return the maxIncrementPercentage
     */
    public Integer getMaxIncrementPercentage() {
        return maxIncrementPercentage;
    }

    /**
     * @param maxIncrementPercentage the maxIncrementPercentage to set
     */
    public void setMaxIncrementPercentage(Integer maxIncrementPercentage) {
        this.maxIncrementPercentage = maxIncrementPercentage;
    }

    /**
     * @return the identityResources
     */
    public ArrayList<IdentityResource> getIdentityResources() {
        return identityResources;
    }

    /**
     * @param identityResources the identityResources to set
     */
    public void setIdentityResources(ArrayList<IdentityResource> identityResources) {
        this.identityResources = identityResources;
    }

    /*public double getAdaptedPrice()
    {
        // TODO check implementation, this is a simple one
        double ret=0;
        
        for (IdentityResource ir: getIdentityResources())
        {
            //ret+=ir.getPrice()*ir.getPriority().getLevel();
            ret+=ir.calculateCurrentPrice(this.getPreferredPrice());
        }
        return ret;
    }*/
    

    public float calculateCurrentOffer(Float requiredPrice)
    {        
        float offeringPrice = -1;
        if(getPreferredPrice() < requiredPrice)
        {
            float currentPrice = getPreferredPrice()*(1.0f+getMaxIncrementPercentage()/100.0f);
            if(currentPrice >= requiredPrice)
            {
                int currentIncrementPercentage = 0;
                do
                {                   
                    currentIncrementPercentage++;
                    currentPrice = getPreferredPrice()*(1.0f+currentIncrementPercentage/100.0f);
                } while(currentIncrementPercentage<getMaxIncrementPercentage() || currentPrice>requiredPrice); 
                offeringPrice = currentPrice;
            }            
        }
        else
        {
            offeringPrice = preferredPrice;
        }               
        return offeringPrice;
    }


    @Override
    public BidData clone() 
    {
        // before it does not set an agent
        BidData clone=new BidData();
        
        clone.setMaxIncrementPercentage(this.getMaxIncrementPercentage());
        clone.setPreferredPrice(this.getPreferredPrice());
        //clone.setTimeOfSubmission(this.getTimeOfSubmission());
        
        ArrayList<IdentityResource> irList=new ArrayList<>();
        for(IdentityResource ir: this.getIdentityResources())
        {
            IdentityResource nir=new IdentityResource();
            nir.setCost(ir.getCost());
            nir.setDurationOfAuction(ir.getDurationOfAuction());
            //nir.setMaxPrice(ir.getMaxPrice());
            //nir.setMinimumProfit(ir.getMinimumProfit());
            //nir.setPreferredProfit(ir.getPreferredProfit());
            //nir.setPrice(ir.getPrice());
            nir.setPriority(IdentityResource.SecurityLevel.createByNumber(ir.getPriority().getLevel()));
            nir.setResourceType(ir.getResourceType());
            nir.setAsset(ir.getAsset());
            nir.setFeature(ir.getFeature());
            
            irList.add(nir);
        }
        clone.setIdentityResources(irList);
        
        return clone;
    }
    

}
