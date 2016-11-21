package uk.ac.bham.simulator;

/**
 * QUESTIONS FOR THIS CLASS What other attributes to include when registering an
 * SP to the federation except the ServiceProviderID,PublicKey ? how do we
 * handle asks ? how frequently are they going to be submitted , how many and
 * for how long? Should the ServiceProvider Class contain a Main method or it
 * should be only consist by setters and getters for attributes ?
 */


/*import java.util.Scanner;
 import java.rmi.server.UID;*/
import java.security.*;
import java.util.ArrayList;
import uk.ac.bham.simulator.IdentityResource.SecurityLevel;
/*import java.util.logging.Level;
 import java.util.logging.Logger;*/

public class ServiceProvider {

    private String name;
    private String feature;

    StringBuffer publicK, privateK;
    ArrayList<AuctionAsk> auctionAsks;
    private Integer revenue;

    public ServiceProvider() {
        auctionAsks = new ArrayList<AuctionAsk>();
        revenue = 0;
    }

    public AuctionAsk createAuctionAsk() {
        AuctionAsk auctionAsk = new OpenOutcryAsk(this);
        auctionAsk.configIdentityResources();
        auctionAsk.setMaxDecrementPercentage(Utilities.generateRandomInteger(1, 50));
        auctionAsks.add(auctionAsk);
        return auctionAsk;
    }

    public void removeAuctionAsk(AuctionAsk auctionAsk) {
        auctionAsks.remove(auctionAsk);
    }

    public void requestAuthentication() {

    }

    /**
     * Constructs the public and private pair of cryptographic RSA keys for each
     * Service provider
     *
     */
    public void GenerateRSAKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
        publicK = new StringBuffer();

        for (int i = 0; i < publicKey.length; ++i) {
            publicK.append(Integer.toHexString(0x0100 + (publicKey[i] & 0x00FF)).substring(1));
        }
//        System.out.println(publicK);
//        System.out.println();

        byte[] privateKey = keyGen.genKeyPair().getPrivate().getEncoded();
        privateK = new StringBuffer();

        for (int i = 0; i < privateKey.length; ++i) {
            privateK.append(Integer.toHexString(0x0100 + (privateKey[i] & 0x00FF)).substring(1));
        }

        //   System.out.println(privateK);
        //   System.out.println();
        //   System.out.println("----------------------NEW PAIR--------------------------------");
    }


    /*public static void main(String[] args) 
     {
        
     AuctionAsk ask=new AuctionAsk();
     ServiceProvider obj = new ServiceProvider();
        
     System.out.println("Please specify the number of Service providers (Integer Required) required for the simulation");
     Scanner num = new Scanner(System.in);
     int SpNum = (int) num.nextDouble();//this ensures that even if a user gives a decimal number the system will trancate it and use it without crashing 
     String[] SpS = new String[SpNum]; //Array that will hold the constructed SP entities
     String CreatedSP, resource = "";
     boolean register=false;
        
     System.out.println("Please specify how the asks will be generated:"+"\n"  + "1: Statistical Model"+ "\n" +"2: Random Model");
     Scanner model=new Scanner (System.in);
     int AskType=model.nextInt(); //contains the type of asks the user wants to create for the simulation, random ask or statistical asks
        
                 
     for (int i = 0; i < SpS.length; i++) {

     //generates the unique identifier for each SP
     UID ServiceProviderId = new UID();

     //calls the function to generate keys for SPs
     try {
     obj.GenerateRSAKeys();
     } catch (NoSuchAlgorithmException ex) {
     Logger.getLogger(ServiceProvider.class.getName()).log(Level.SEVERE, null, ex);
     }

     //this holds all the attributes that should be send to the federation to register
     CreatedSP = "ServiceProviderID: " + ServiceProviderId.toString() + ",PublicKey: " + obj.publicK + ",PrivateKey: " + obj.privateK;

     System.out.println(CreatedSP);
     //  register=obj.RegisterSPtoFederation(CreatedSP); //registers SP to the federation.. Create a FUNCTION IN FEDERATION TO REGISTER SP, IF SUCESFULL SEND A TRUE VALUE BACK.. REGISTER sp TO A DB 
            
     if(register==true){ // if SP successfully registered to federation add the certain SP to the SP list
     SpS[i] = CreatedSP; //adds created SP to the SP list
     }
         
     if(AskType==1){
     StatisticalAsk statistical=new StatisticalAsk();
     }else if(AskType==2){
     OpenOutcryAsk ran=new OpenOutcryAsk();
     ran.CreateRandomAsk();
     }

            
     }
        
     }*/
    protected IdentityResource.ResourceType getRandomResourceType(IdentityResource.ResourceType but) {
        int number = Utilities.generateRandomInteger(1, 4);

        if (but != null) {
            while (number == but.getId()) {
                number = Utilities.generateRandomInteger(1, 4);
            }
        }
        return IdentityResource.ResourceType.createByNumber(number);
    }

    /**
     *
     * @param identityProvider
     * @param price
     * @param requiredPrice
     * @param bid
     */
    public void notifyAuctionWinner(IdentityProvider identityProvider, Bid bid, Float price, Float requiredPrice) {
        IdentityResource.ResourceType firstResourceType = null;
        ArrayList<IdentityResource.ResourceType> rtList = new ArrayList<>();
        ArrayList<IdentityResource.SecurityLevel> pList = new ArrayList<>();
        // here random if the bid change just after be a winner
        int count = 0;
        while (count < 2) {
            // one or two times
            IdentityResource last_ir=null;
            IdentityResource.ResourceType rt = getRandomResourceType(firstResourceType); // FIRSTRESOURCETYPE

            float priorityValue = 1.0f;
            switch (Utilities.generateRandomInteger(1, 3)) // begin with Low, so change to Midium or High
            {
                case 1:
                    priorityValue = 1.0f;
                    break;
                case 2:
                    priorityValue = 1.3f;
                    break;
                case 3:
                    priorityValue = 1.6f;
                    break;
            }
            IdentityResource.SecurityLevel p = IdentityResource.SecurityLevel.createByNumber(priorityValue);
            boolean modified = false;
            for (IdentityResource ir : bid.getBidData().getIdentityResources()) {
                if (ir.getResourceType().getId() == rt.getId()) {
                    last_ir=ir;
                    if (p.getLevel() > ir.getPriority().getLevel()) {
                        modified = true;
                    }
                    break;
                }
            }
            if (modified) {
                count++;
                firstResourceType = rt;
                rtList.add(rt);
                // TODO modified by machine learning
                if (FederatedCoordinator.strategy.isReady() && last_ir!=null) {
                    String asset_feature=last_ir.getAsset().name()+","+last_ir.getFeature().name();
                    String dataset=bid.getAgent().toDataset()+","+asset_feature+",?";
                    String sSecurityLevel=FederatedCoordinator.strategy.predictOneRow(dataset);
                    p = SecurityLevel.valueOf(sSecurityLevel);
                    pList.add(p);
                } else {
                    // old behavior
                    pList.add(p);
                }

                // TODO check why the value is not updated
                /*
                 if(i==0) firstAddNewPrice=(oldPrice/bid.getIdentityResources().size()*(1-priorityValue));
                 else secondAddNewPrice=(oldPrice/bid.getIdentityResources().size()*(1-priorityValue));
                 float tmpPrice=oldPrice+firstAddNewPrice+secondAddNewPrice;
                 tmpPrice=Math.round(tmpPrice/100.0f)*100.0f;
                 bid.setPreferredPrice(/ * bid.calculateCurrentOffer(requiredPrice)* /tmpPrice);
                 */
            }
        }

        // modified by machine learning
        bid.modifiedBy(rtList, pList);

        bid.getAgent().getIdentityProvider().requestPayment(price, bid);
    }

    public boolean allocateResources(Bid bid) {
        executeJobs(bid.getBidData().getIdentityResources());
        return true;
    }

    private boolean executeJobs(ArrayList<IdentityResource> identityResources) {
        for (IdentityResource identityResource : identityResources) {
            //executing job by job...
            System.out.println("Executing:" + identityResource.getResourceType());
        }

        return true;
    }

    @Override
    public String toString() {
        return "" + this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    /**
     * @return the revenue
     */
    public Integer getRevenue() {
        return revenue;
    }

    /**
     * @param revenue the revenue to set
     */
    public void addRevenue(Integer revenue) {
        this.revenue += revenue;
    }

    public void setName(String name) {
        String n = name;
        String f = "";
        String[] nf = n.split(":");
        if (nf.length == 2) {
            n = nf[1];
            f = nf[0];
        }
        this.name = n;
    }

    public String getName() {
        return this.name;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getFeature() {
        return this.feature;
    }

}
