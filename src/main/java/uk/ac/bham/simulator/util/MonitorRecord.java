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

import java.util.Calendar;

/**
 *
 * @author Francisco Ramirez
 */
public class MonitorRecord {
        Calendar timestamp;
        double value;
        
        public MonitorRecord(long ts, double v)
        {
            timestamp=Calendar.getInstance();
            timestamp.setTimeInMillis(ts);
            value=v;
        }
        
        public Calendar getTimestamp()
        {
            return timestamp;
        }
        
        public double getValue()
        {
            return value;
        }    
}
