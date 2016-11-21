/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

/**
 *
 * @author Francisco Ramirez
 * @version 1.2
 */
public enum Feature {
    EncryptionTlsSsl(1, "Encryption Tls Ssl"), Proxies(2, "Proxies"), MediaEncryptionSRTP(3, "Media Encryption SRTP"),
    EncryptionAtRest(4, "Encryption At Rest"), EncryptionAtTransit(5, "Encryption At Transit"),
    DifferentEncryptionKeysPerFile(6, "Different Encryption Keys Per File"),
    PasswordProtectedFiles(7, "Password Protected Files"),SegmentationOfFiles(8, "Segmentation Of Files");
    
    private final int id;
    private final String description;
    
    Feature(int id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getDescription() {
        return this.description;
    }

    public static Feature createByNumber(int id) {
        Feature instance = null;
        for (Feature p : Feature.values()) {
            if (p.getId() == id) {
                instance = p;
                break;
            }
        }
        return instance;
    }

    public static Feature createByDescription(String d) {
        Feature instance = null;
        for (Feature p : Feature.values()) {
            if (p.getDescription().equals(d)) {
                instance = p;
                break;
            }
        }
        return instance;
    }

    public static Feature createRandom(Asset asset) {
        int number;
        
        if(asset==Asset.VoiceCommunication) {
            number = Utilities.generateRandomInteger(1, 3);
        } else {
            number = Utilities.generateRandomInteger(4, 8);
        }

        return createByNumber(number);
    }

}
