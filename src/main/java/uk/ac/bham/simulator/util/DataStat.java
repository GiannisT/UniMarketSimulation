/*
 * Copyright (C) 2014 jejosp.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 *
 * IT Consultore (ITC)
 * Guayaquil, Ecuador
 */

package uk.ac.bham.simulator.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jejosp
 */
public class DataStat {
    public static final DataStat instance=new DataStat();
    
    Map<String, Integer> propertiesInteger=new HashMap<String, Integer>();
    Map<String, ArrayList<MonitorRecord>> monitorMap=new HashMap<String, ArrayList<MonitorRecord>>();
    
    public static DataStat getInstance()
    {
        return instance;
    }

    public synchronized void setPropertyAsInteger(String key, Integer value)
    {
        propertiesInteger.put(key, value);
    }
    
    public synchronized Integer getPropertyAsInteger(String key)
    {
        Integer tmp=propertiesInteger.get(key);
        return tmp;
    }
    
    public synchronized void initCounter(String counter)
    {
        this.setPropertyAsInteger(counter, 0);
    }
    
    public synchronized int incrementCounter(String counter)
    {
        Integer otmp=this.getPropertyAsInteger(counter);
        int tmp=0;
        if(otmp!=null) tmp=otmp;
        tmp++;
        this.setPropertyAsInteger(counter, tmp);
        return tmp;
    }
    
    public synchronized void recordValue(String monitor, double value)
    {
        ArrayList<MonitorRecord> list=this.monitorMap.get(monitor);
        if(list==null)
        {
            this.monitorMap.put(monitor, new ArrayList<MonitorRecord>());
            list=this.monitorMap.get(monitor);
        }
        MonitorRecord mr=new MonitorRecord(Calendar.getInstance().getTimeInMillis(), value);
        list.add(mr);
    }    
}
