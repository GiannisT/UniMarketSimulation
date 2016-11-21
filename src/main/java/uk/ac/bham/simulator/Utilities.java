package uk.ac.bham.simulator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author
 */
public class Utilities {

    public static int COLUMN_GAP = 4;

    // column order
    public static int COL_FEATURE = 0;
    public static int COL_ASSET = 1;
    public static int COL_FORMER_SECURITY_LEVEL = 2;
    public static int COL_ADAPTED_SECURITY_LEVEL = 3;
    public static int COL_SERVICE = 4;
    public static int COL_SERVICE_PROVIDER = 5;
    public static int COL_DATARATE = 6;
    public static int COL_CPU = 7;
    public static int COL_MEMORY = 8;
    public static int COL_DISKSPACE = 9;
    public static int COL_USER_MAXPRICE = 10;
    public static int COL_SELLINGPRICE = 11;
    public static int COL_CLOUDSP_PROFIT = 12;
    public static int COL_FEDERATED_COMMISSION = 13;
    public static int COL_AUCTION_MODEL = 14;

    public static int COL_ASSET_FEATURE = 2;

    public static String FMT_COLUMN_ALIGN = "formatColumnAlign_";
    public static String FMT_COLUMN_ALIGN$$RIGHT = "-";

    public static int DAT_SERVICE = 0;
    public static int DAT_SERVICE_PROVIDER = 1;
    public static int DAT_DATARATE = 2;
    public static int DAT_CPU = 3;
    public static int DAT_MEMORY = 4;
    public static int DAT_DISKSPACE = 5;
    public static int DAT_ASSET = 6;
    public static int DAT_FEATURE = 7;
    //Adapted from http://www.javapractices.com

    public static PrintStream psData;
    public static PrintStream psPerformance;

    public static int generateRandomInteger(int aStart, int aEnd) {
        Random aRandom = new Random();
        if (aStart > aEnd) {
            // TODO restore to throw exception
            //throw new IllegalArgumentException("Start cannot exceed End.");
            int tmp = aEnd;
            aEnd = aStart;
            aStart = tmp;
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long) aEnd - (long) aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long) (range * aRandom.nextDouble());
        return (int) (fraction + aStart);
    }

    public static void addValue(ArrayList<ArrayList<String>> model, int i, int j, String value) {
        while (j >= model.size()) {
            model.add(new ArrayList<String>());
        }
        ArrayList<String> row = model.get(j);
        while (i >= row.size()) {
            row.add("");
        }
        row.set(i, value);
    }

    public static String getValue(ArrayList<ArrayList<String>> model, int i, int j, ConcurrentHashMap<String, Integer> param) {
        String value = "";

        if (j < model.size()) {
            ArrayList<String> row = model.get(j);

            if (row != null && i < row.size()) {
                value = row.get(i);
            }
        }

        return value;
    }

    public static void prepareParam(ArrayList<ArrayList<String>> model, ConcurrentHashMap<String, Integer> param) {
        ArrayList<Integer> maxColumnLength = new ArrayList<Integer>();

        int maxColumn = 0;
        for (ArrayList<String> row : model) {
            while (row.size() >= maxColumnLength.size()) {
                maxColumnLength.add(0);
            }
            int i = 0;
            for (String col : row) {
                if (col.length() > maxColumnLength.get(i)) {
                    maxColumnLength.set(i, col.length());
                }
                i++;
            }
            if (row.size() > maxColumn) {
                maxColumn = row.size();
            }
        }
        param.put("maxRow", model.size());
        param.put("maxColumn", maxColumn);
        int totalLength = 0;
        for (int i = 0; i < maxColumnLength.size(); i++) {
            totalLength += COLUMN_GAP;
            if (i > 0) {
                totalLength += maxColumnLength.get(i);
            }
            param.put("maxColumnLength_" + i, maxColumnLength.get(i));
        }
        param.put("lineLength", totalLength);
    }

    public static String getLine(ConcurrentHashMap<String, Integer> param, String aChar, String title) {
        String line = "";
        if (title != null && title.length() > 0) {
            line = "  " + title + "  ";
        }

        if (aChar.length() > 0) {
            while (line.length() < param.get("lineLength")) {
                line = aChar + line + aChar;
            }
        }
        return line;
    }

    public static void setFormat(ConcurrentHashMap<String, String> paramFormat, int i, String format, String value) {
        paramFormat.put(format + i, value);
    }

    public static void printModel(ArrayList<ArrayList<String>> model, ConcurrentHashMap<String, Integer> param, ConcurrentHashMap<String, String> paramFormat) {
        String columnGap = "";
        while (columnGap.length() < COLUMN_GAP) {
            columnGap += " ";
        }
        int j = 0;
        for (ArrayList<String> row : model) {
            int i = 0;
            if (j == 1) {
                System.out.println(getLine(param, "=", ""));
            }
            for (String col : row) {
                if (i == 0) {
                    i++;
                    continue;
                }
                int n = 0;
                n = param.get("maxColumnLength_" + i);
                String colFormat = "%-" + n + "s";
                String fmt = paramFormat.get(FMT_COLUMN_ALIGN + i);
                if (fmt != null && fmt.length() > 0 && fmt.equals(FMT_COLUMN_ALIGN$$RIGHT)) {
                    colFormat = "%" + n + "s";
                }
                if (i > 1) {
                    colFormat = columnGap + colFormat;
                }
                System.out.printf(colFormat, getValue(model, i, j, param));
                i++;
            }
            System.out.printf("%n");
            j++;
        }
    }

    public static Integer[] getDefaultResourceArray() {
        return new Integer[]{1, 1, 20, 20};
    }

    public static Integer[] getRandomResourceArray() {
        int dataRate = Utilities.getRandomFromRange("resourceValueRange.dataRate", 5);
        int cpu = Utilities.getRandomFromRange("resourceValueRange.cpu", 5);
        int memory = Utilities.getRandomFromRange("resourceValueRange.memory", 4) * 512;
        int disk = Utilities.getRandomFromRange("resourceValueRange.disk", 4);
        Integer[] resourceArray = new Integer[]{dataRate, cpu, memory, disk};
        return resourceArray;
    }

    public static String[] getExtraData(IdentityResource ir, IdentityResource.ResourceType feature, AuctionAsk ask, Integer[] resourceArray) {
        String[] extraData = new String[7];
        int random = 60; // div by 2, 3, 4
        String[][] resource = {};
        String[] serviceNameList = {""};
        String[] serviceProviderList = {""};
        String[] assetList = getAssetArray();

        int dataRate = resourceArray[0];
        int cpu = resourceArray[1];
        int memory = resourceArray[2];
        int disk = resourceArray[3];

        String smemory;
        // if (memory<1024) 
        smemory = memory + " Mbyte"; //we need a unified measuring unit for memory Giannis Code
        // else smemory=(memory/1024.0)+" Gbyte";

        if (feature == IdentityResource.ResourceType.Anonymity) {
            String[] services = getServiceArray(feature, ir.getAsset().getDescription());
            String[] spname = getStringArray("serviceProviderNameList.Anonymity");
            String[][] r = {{dataRate + " mbits/s", "--", "--", "--"}, {"--", cpu + " CPU core @ 2.1Ghz", "--", "--"}};
            resource = r;
            serviceNameList = services;
            serviceProviderList = spname;
        } else if (feature == IdentityResource.ResourceType.Availability) {
            String[] services = getServiceArray(feature, ir.getAsset().getDescription());
            String[] spname = getStringArray("serviceProviderNameList.Availability");
            String[][] r = {{"--", cpu + " CPU core @ 2.1Ghz", smemory, disk + " Gbyte"}, {"--", cpu + " CPU core @ 2.1Ghz", smemory, disk + " Gbyte"}};
            resource = r;
            serviceNameList = services;
            serviceProviderList = spname;
        } else if (feature == IdentityResource.ResourceType.Integrity) {
            String[] services = getServiceArray(feature, ir.getAsset().getDescription());
            String[] spname = getStringArray("serviceProviderNameList.Integrity");
            String[][] r = {{"--", cpu + " CPU core @ 2.1Ghz", "512 Mbyte", "--"}, {"--", "--", "--", "--"}};
            resource = r;
            serviceNameList = services;
            serviceProviderList = spname;
        } else if (feature == IdentityResource.ResourceType.Confidentiality) {
            String[] services = getServiceArray(feature, ir.getAsset().getDescription());
            String[] spname = getStringArray("serviceProviderNameList.Confidentiality");
            String[][] r = {{"--", cpu + " CPU core @ 2.1Ghz", smemory, "--"}, {"--", cpu + " CPU core @ 2.1Ghz", smemory, "--"}};
            resource = r;
            serviceNameList = services;
            serviceProviderList = spname;
        }

        int iservice = generateRandomInteger(0, random) % serviceNameList.length;
        String valueService = serviceNameList[iservice];
        extraData[DAT_SERVICE] = valueService;

        serviceProviderList = getStringArray("serviceProviderNameList." + valueService.replaceAll(" ", ""));

        /*
         if(serviceProviderList.length>1)
         {
         extraData[DAT_SERVICE_PROVIDER]=serviceProviderList[generateRandomInteger(0, random)%serviceProviderList.length];
         } else {
         extraData[DAT_SERVICE_PROVIDER]=serviceProviderList[0];
         }
         */
        List<String> tmpList = new ArrayList<>();
        String spName = ""; //ask.getServiceProvider().getName();
        for (String spn : serviceProviderList) {
            tmpList.add(spn);
            /*
            for (String ss : ServiceProviderManager.serviceProviderNameList) {
                if (ss.startsWith(spn)) {
                    tmpList.add(ss);
                }
            }
            */
        }
        if (tmpList.size() > 0) {
            int iname = generateRandomInteger(0, random) % tmpList.size();
            spName = tmpList.get(iname);
        }
        extraData[DAT_SERVICE_PROVIDER] = spName;

        extraData[DAT_DATARATE] = resource[iservice][DAT_DATARATE - DAT_DATARATE];
        extraData[DAT_CPU] = resource[iservice][DAT_CPU - DAT_DATARATE];
        extraData[DAT_MEMORY] = resource[iservice][DAT_MEMORY - DAT_DATARATE];
        extraData[DAT_DISKSPACE] = resource[iservice][DAT_DISKSPACE - DAT_DATARATE];
        extraData[DAT_ASSET] = assetList[generateRandomInteger(0, random) % assetList.length];

        return extraData;
    }

    public static String[] getAssetArray() {
        String[] assetList = Asset.getDescriptionStringArray(); //{"University ID", "Company ID", "Identity Information"};
        return assetList;
    }

    public static String getAssetId(String name) {
        return name.replace(" ", "");
    }

    public static String[] getServiceArray(String asset) {
        //String[] serviceNameArray = {"Anonymity proxy", "Usage of Psuedonyms", "Raid2-data backup", "Mirrored VMs in different physical servers", "Message authentication code (MAC)", "Identity aware fraud detection", "Encryption at transit with keys concealed from cloud sp", "Permanent Deletion of identity related information"};
        String[] serviceNameArray = {"VoIP", "Cloud Storage"};
        if (Asset.VoiceCommunication.getDescription().equals(asset)) {
            serviceNameArray = new String[]{"VoIP"};
        }
        if (Asset.File.getDescription().equals(asset)) {
            serviceNameArray = new String[]{"Cloud Storage"};
        }
        return serviceNameArray;
    }

    public static String[] getServiceArray(IdentityResource.ResourceType resourceType, String asset) {
        //String[] serviceNameFullArray = {"Anonymity proxy", "Usage of Psuedonyms", "Raid2-data backup", "Mirrored VMs in different physical servers", "Message authentication code (MAC)", "Identity aware fraud detection", "Encryption at transit with keys concealed from cloud sp", "Permanent Deletion of identity related information"};
        String[] serviceNameArray = getServiceArray(asset); //{"VoIP Services", "Cloud Storage Services"};

        /*
         int p = 0;
         if (resourceType == IdentityResource.ResourceType.Anonymity) {
         p = 0;
         } else if (resourceType == IdentityResource.ResourceType.Availability) {
         p += 2;
         } else if (resourceType == IdentityResource.ResourceType.Integrity) {
         p += 4;
         } else if (resourceType == IdentityResource.ResourceType.Confidentiality) {
         p += 6;
         }
         serviceNameArray = new String[]{serviceNameFullArray[p], serviceNameFullArray[p + 1]};
         */
        return serviceNameArray;
    }

    public static String getServiceId(String serviceDescription) {
        String serviceId = serviceDescription.replace(" ", "");
        return serviceId;
    }

    public static void setOutput4Data(PrintStream ps) {
        Utilities.psData = ps;
    }

    public static void setOutput4Performance(PrintStream ps) {
        Utilities.psPerformance = ps;
    }

    public static void println4Data(int sequence, String feature, String initial, String adapted, String am, double preferredPrice, double sellingPrice, double cloudProfit, double federatedCommission) {
        psData.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",%f,%f,%f,%f%n", sequence, feature, initial, adapted, am, preferredPrice, sellingPrice, cloudProfit, federatedCommission);
    }

    public static void println4Performance(int sequence, String am, long memBefore, long memAfter, long timeBefore, long timeAfter) {
        psPerformance.printf("\"%d\",\"%s\",%d,%d%n", sequence, am, (memAfter - memBefore), (timeAfter - timeBefore));
    }

    public static void printHeader4Data() {
        if (psData != null) {
            psData.println("\"Sequence\",\"Auction Model\",\"Feature\",\"Former Security Level\",\"Adapted Security Level\",\"Preferred Price\",\"Selling Price\",\"Could SP Profit\",\"Comission\"");
        }
    }

    public static void printHeader4Performance() {
        if (psPerformance != null) {
            psPerformance.println("\"Sequence\",\"Auction Model\",\"Memory (bytes)\",\"Time (milliseconds)\"");
        }
    }

    public static int getBundle(String s, int d) {
        int ir = d;
        try {
            String r = ResourceBundle.getBundle("init").getString(s);
            if (r != null && r.length() > 0) {
                ir = Integer.parseInt(r);
            }
        } catch (MissingResourceException mrex) {
            System.out.println(mrex);
        }
        return ir;
    }

    public static int getRandomFromRange(String range, int d) {
        int ir = d;
        try {
            String r = ResourceBundle.getBundle("init").getString(range);
            if (r != null && r.length() > 0 && r.indexOf("..") > 0) {
                String[] r2 = r.split("\\.\\.");
                if (r2 != null && r2.length == 2) {
                    int ir1 = Integer.parseInt(r2[0]);
                    int ir2 = Integer.parseInt(r2[1]);
                    ir = Utilities.generateRandomInteger(ir1, ir2);
                }
            }
        } catch (MissingResourceException mrex) {
            System.out.println(mrex);
        }
        return ir;
    }

    public static String[] getStringArray(String range) {
        String[] ir = new String[0];
        try {
            String r = ResourceBundle.getBundle("init").getString(range);
            if (r != null && r.length() > 0) {
                ir = r.split(",");
            }
            for (int i = 0; i < ir.length; i++) {
                ir[i] = ir[i].trim();
            }
        } catch (MissingResourceException mrex) {
            System.out.println(mrex);
        }
        return ir;
    }
}
