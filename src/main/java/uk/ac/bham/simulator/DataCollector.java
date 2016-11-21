/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Francisco Ramirez
 * @version 1.0
 * 
 * modified
 * @author Francisco Ramirez
 * @version 1.2
 */
public class DataCollector {

    private static final List<Map<String, Object>> data = new ArrayList<>();

    public static void add(Agent agent, BidData bid, BidData bidModified, ServiceProvider serviceProvider, AuctionAsk ask, String valueAsset, String valueSecurityLevel, String valueService, Integer[] lastValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (agent != null && bid != null && serviceProvider != null && ask != null) {
            row.put("agent", agent);
            row.put("bid", bid);
            if (bidModified != null) {
                row.put("bidModified", bidModified);
            }
            row.put("serviceProvider", serviceProvider);
            row.put("ask", ask);
            row.put("lastValues", lastValues);
            row.put("valueAsset", valueAsset);
            row.put("valueSecurityLevel", valueSecurityLevel);
            row.put("valueService", valueService);

            data.add(row);
        }
    }

    public static void print() {
        for (Map<String, Object> row : data) {
            Agent agent = (Agent) row.get("agent");
            BidData bid = (BidData) row.get("bid");
            BidData bidModified = (BidData) row.get("bidModified");
            ServiceProvider serviceProvider = (ServiceProvider) row.get("serviceProvider");
            AuctionAsk ask = (AuctionAsk) row.get("ask");
            Integer[] lastValues = (Integer[]) row.get("lastValues");
            String valueAsset = (String) row.get("valueAsset");
            String valueSecurityLevel = (String) row.get("valueSecurityLevel");
            String valueService = (String) row.get("valueService");

            int nBidSize = bid.getIdentityResources().size();
            int nBidModifiedSize = 1;
            int nAskSize = ask.getIdentityResources().size();
            List<IdentityResource> irModified = null;
            if (bidModified != null) {
                irModified = bidModified.getIdentityResources();
                nBidModifiedSize = irModified.size();
            }
            Object[] obj = new Object[4 + nBidSize + nBidModifiedSize + 1 + nAskSize];
            int i = 0;
            obj[i++] = agent.hashCode();
            obj[i++] = agent.getRank();
            obj[i++] = agent.getLocation();
            obj[i++] = agent.getState();
            obj[i++] = agent.getDocumentSecurity();
            //for (IdentityResource ir : bid.getIdentityResources()) {
                //obj[i++] = ir.getResourceType();
            //    obj[i++] = ir.getPriority();
            //}
            //if (nBidModifiedSize > 1) {
            //    for (IdentityResource ir : irModified) {
                    //obj[i++] = ir.getResourceType();
            //        obj[i++] = ir.getPriority();
            //    }
            //} else {
            //    obj[i++] = "?,?,?,?";
            //}
            //obj[i++] = serviceProvider.getName();
            //for (IdentityResource ir : ask.getIdentityResources()) {
                //obj[i++] = ir.getResourceType();
            //    obj[i++] = ir.getPriority();
            //}
            obj[i++]=valueAsset;
            obj[i++]=valueSecurityLevel;
            obj[i++]=valueService;
            for (Integer value: lastValues) {
                obj[i++] = value;
            }
            String text = "";
            for (int j = 0; j < i; j++) {
                if(j>0) text+=",";
                text += obj[j];
            }
            System.out.println(text);
            //text = text.substring(1);
            //Logger.getLogger(DataCollector.class.getName()).log(Level.INFO, text, obj);
        }
    }
}
