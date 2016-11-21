/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 */
public class ServiceProviderManager extends TimerTask {
        
    private static ServiceProviderManager instance=new ServiceProviderManager();
    private final List<ServiceProvider> serviceProviderList=new ArrayList<ServiceProvider>();
    static final List<String> serviceProviderNameList=new ArrayList<String>();
    public static final List<List<Integer>> serviceProviderResourceList=new ArrayList<List<Integer>>();
    private int countAnonymity=0;
    private int countAvailability=0;
    private int countIntegrity=0;
    private int countConfidentiality=0;
    
    private boolean running;
    
    public static final String RUNNING_LOCK="RUNNING LOCK";
    
    private int counter;
    private Timer timer;
    
    private boolean isRandom;
    
    private ServiceProviderManager()
    {
        synchronized (RUNNING_LOCK)
        {
            this.running=false;
        }
    }

    public static void clear()
    {
        ServiceProviderManager.instance=new ServiceProviderManager();
        ServiceProviderManager.instance.countAnonymity=0;
        ServiceProviderManager.instance.countAvailability=0;
        ServiceProviderManager.instance.countConfidentiality=0;
        ServiceProviderManager.instance.countIntegrity=0;
        ServiceProviderManager.instance.serviceProviderList.clear();
        ServiceProviderManager.instance.serviceProviderNameList.clear();
        ServiceProviderManager.instance.serviceProviderResourceList.clear();
    }
    
    public static ServiceProviderManager getInstance()
    {
        return ServiceProviderManager.instance;
    }
    
    public void setRandom()
    {
        this.isRandom=true;
    }
    
    public void setModelled()
    {
        this.isRandom=false;
    }
    
    public boolean isRandom()
    {
        return this.isRandom;
    }
    
    
    
    public void start()
    {
        synchronized (RUNNING_LOCK)
        {
            if(!this.running)
            {
                this.counter=Utilities.getBundle("serviceProvider.max", 4); //how many asks will be created, if changed, the serviceProvider.max attribute in init.properties should change as well
               
                //************************************************************ BEGIN GIANNIS CODE********************************************************************
                if(FederatedCoordinator.input.toLowerCase().equals("yes")){
                  int temp=Math.round((counter*FederatedCoordinator.SPpercentage) /100); //the number of sps that are malicious....by doing this we are able to remove a percentage of the attackers that are malicious from the market
                                
                  if(temp<1 & counter>1){ //use in case that the percentage returns a number smaller that one
                	 temp=1; //at least flag one sp as malicious if applicable (total number of SPs exceeding one)
                	 counter=counter-temp;	
                  }
                }
                
                //************************************************************** END GIANNIS CODE***************************************************************
                long delayNewServiceProvider=Utilities.generateRandomInteger(1, 10)*10;

                this.timer=new Timer("Service Provider Manager", true);
                this.running=true;
                timer.schedule(this, delayNewServiceProvider, delayNewServiceProvider);
                //Thread thread=new Thread(this, "Service Provider Manager");
                //thread.start();
            }
        }
    }
    
    public void stop()
    {
        synchronized (RUNNING_LOCK)
        {
            if(running)
                running=false;
        }
    }
    
    public boolean isRunning()
    {
        boolean ret;
        synchronized (RUNNING_LOCK)
        {
            ret=running;
        }
        return ret;
    }
    
    public List<String> setFeatureFor(List<String> list, String feature) {
        
        for(int i=0;i<list.size(); i++) {
            String _old=list.get(i);
            String _new=feature+":"+_old;
            list.set(i, _new);
        }
        return list;
    }
    
    public String[] getServiceProviderNameBaseList() {
        String[] s1=Utilities.getStringArray("serviceProviderNameList.Anonymity");
        String[] s2=Utilities.getStringArray("serviceProviderNameList.Availability");
        String[] s3=Utilities.getStringArray("serviceProviderNameList.Integrity");
        String[] s4=Utilities.getStringArray("serviceProviderNameList.Confidentiality");
        
        List<String> a=new ArrayList <String>();
        a.addAll(setFeatureFor(Arrays.asList(s1),"Anonymity"));
        a.addAll(setFeatureFor(Arrays.asList(s2),"Availability"));
        a.addAll(setFeatureFor(Arrays.asList(s3),"Integrity"));
        a.addAll(setFeatureFor(Arrays.asList(s4),"Confidentiality"));
        
        return a.toArray(new String[]{});
   }
    
    /**
     * Create new service provider according to a random delay, each new service provider create only one new auction ask
     * 
     */
    
    public synchronized void run()
    {
        //IdentityProvider ip=new IdentityProvider();
        //FederatedCoordinator.getInstance().registerIdentityProvider(ip);
        
        if (isRunning() && counter >0)
        {
            ServiceProvider serviceProvider=new ServiceProvider();
            serviceProviderList.add(serviceProvider);
            String [] sp=getServiceProviderNameBaseList();
            int i=counter%sp.length;
            int k=counter/sp.length;
            String spName=sp[i]+""+k;
            serviceProvider.setName(spName);
            serviceProviderNameList.add(serviceProvider.getName()); //if needed to present different numbers: serviceProviderNameList.add(sp[i]+" "+counter);
            List<Integer> list=new ArrayList<Integer>();
         
          //************************************************************ BEGIN GIANNIS CODE********************************************************************
           int datarate=0;
           int cpu=0;
           int memory=0;
           int disk=0;
            
           if(FederatedCoordinator.input2.toLowerCase().equals("yes")){ //if the user requires to damage a percentage of the overall resources in the market 
            	datarate=Utilities.getRandomFromRange("resourceInitialValueRange.dataRate", 10); //returns a random amount of datarate based on the bounds described in init.properties 
                cpu=Utilities.getRandomFromRange("resourceInitialValueRange.cpu", 10);//returns a random amount of cpu based on the bounds described in init.properties 
                memory=Utilities.getRandomFromRange("resourceInitialValueRange.memory", 10);//returns a random amount of memory based on the bounds described in init.properties 
                disk=Utilities.getRandomFromRange("resourceInitialValueRange.disk", 10);//returns a random amount of disk based on the bounds described in init.properties 
            	
                //flag SP resources as damaged and remove them from the available resources of each SP
                datarate=Math.round(datarate-((datarate*FederatedCoordinator.Resourcepercentage)/100));
                cpu=Math.round(cpu-((cpu*FederatedCoordinator.Resourcepercentage)/100));
                memory=Math.round(memory-((memory*FederatedCoordinator.Resourcepercentage)/100));
                disk=Math.round(disk-((disk*FederatedCoordinator.Resourcepercentage)/100));
                
            }else{ //if the user does not require a percentage of the resources to be damaged
            
                datarate=Utilities.getRandomFromRange("resourceInitialValueRange.dataRate", 10); //returns a random amount of datarate based on the bounds described in init.properties 
                cpu=Utilities.getRandomFromRange("resourceInitialValueRange.cpu", 10);//returns a random amount of cpu based on the bounds described in init.properties 
                memory=Utilities.getRandomFromRange("resourceInitialValueRange.memory", 10);//returns a random amount of memory based on the bounds described in init.properties 
                disk=Utilities.getRandomFromRange("resourceInitialValueRange.disk", 10);//returns a random amount of disk based on the bounds described in init.properties 
           
            }
           
           list.add(datarate); 
           list.add(cpu); 
           list.add(memory); 
           list.add(disk); 
           serviceProviderResourceList.add(list);
          //************************************************************ END GIANNIS CODE********************************************************************
            
            FederatedCoordinator.getInstance().registerServiceProvider(serviceProvider);
            if (FederatedCoordinator.isDebugging()) Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.INFO, "a new {0} was created and added to {1}", new Object[] {serviceProvider, FederatedCoordinator.getInstance()});

            AskCreator creator=new AskCreator(serviceProvider, 2, isRandom);
            creator.start();
        }
        counter--;
        if(counter<=0)
        {
            this.timer.cancel();
            stop();
        }
    }
    
    class AskCreator extends TimerTask
    {
        ServiceProvider serviceProvider;
        Timer timer;
        int counter=0;
        
        boolean isRandom;
        
        public AskCreator(ServiceProvider sp, int c, boolean isRandom)
        {
            this.serviceProvider=sp;
            this.timer=new Timer("AuctionAsk Creator", true);
            this.counter=c;
            this.isRandom=isRandom;
        }
        
        public void start()
        {
            long delayNewAsk=Utilities.generateRandomInteger(1, 10)*10;
            this.timer.schedule(this, delayNewAsk, delayNewAsk);            
        }
        
        public void run()
        {
            if(counter>0)
            {
                AuctionAsk ask;
                if(isRandom)
                {
                    ask=new OpenOutcryAsk(serviceProvider); //serviceProvider.createBid(null);
                }
                else
                {
                    ask=new PostedOfferAsk(serviceProvider);
                }
                ask.configIdentityResources();
                
                FederatedCoordinator.getInstance().publishAuctionAsk(ask);
                if (FederatedCoordinator.isDebugging()) Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.INFO, "a new {0} was published by {1} to {2}", new Object[] {ask, serviceProvider, FederatedCoordinator.getInstance()});
            }
            counter--;
            if(counter<=0)
            {
                timer.cancel();
            }
        }
    }
}
