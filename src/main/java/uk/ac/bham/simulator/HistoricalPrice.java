/*
 * Copyright (C) 2014 frankouz.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 *
 * IT Consultore (ITC)
 * Guayaquil, Ecuador
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import uk.ac.bham.simulator.IdentityResource.ResourceType;

/**
 *
 * @author Francisco Ramirez
 */
public class HistoricalPrice {
    private static final String LOCK_HISTORICAL="HISTORICAL";
    private Map<String, ArrayList<Float>> historical;
    private static final HistoricalPrice instance=new HistoricalPrice();
    
    private HistoricalPrice()
    {
        synchronized(LOCK_HISTORICAL)
        {
            historical=new HashMap<String, ArrayList<Float>>();
        }
    }
    
    public static HistoricalPrice getInstance()
    {
        return instance;
    }
    
    public void addPrice(ResourceType ir, Float price)
    {
        this.addPrice("RESOURCE_TYPE="+ir.getId(), price);
    }
    
    public Float getPrice(ResourceType ir)
    {
        return this.getPrice("RESOURCE_TYPE="+ir.getId());
    }
    
    public void addPrice(String ir, Float price)
    {
        synchronized(LOCK_HISTORICAL)
        {
            ArrayList<Float> list=historical.get(ir);
            if(list==null)
            {
                list=new ArrayList<Float>();
                historical.put(ir, list);
            }
            list.add(price);
        }
    }
    
    public Float getPrice(String ir)
    {
        Float avgPrice=0f;
        synchronized(LOCK_HISTORICAL)
        {
            int count=0;

            for (Float f: historical.get(ir))
            {
                avgPrice+=f;
                count++;
            }
            float r=1.0f;
            if(Utilities.generateRandomInteger(1, 100)%2==0) r=-1.0f;
            avgPrice=avgPrice/count;// - avgPrice*0.10f*r;
        }
        return avgPrice;
    }
    
    public int getValueAsInt(String s)
    {
        float value=this.getPrice(s);
        int res=Math.round(value);
        return res;
    }
}
