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
public enum Asset {

    VoiceCommunication(1, "Voice Communication"),
    File(2, "File");
    /*UniversityID(1, "University ID"),
     CompanyID(2, "Company ID"),
     IdentityInformation(3, "Identity Information"),
     Communication(4, "Communication"),
     DiskSpace(5, "Disk Space");*/

    private final int id;
    private final String description;

    Asset(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public static Asset createByNumber(int id) {
        Asset instance = null;
        for (Asset p : Asset.values()) {
            if (p.getId() == id) {
                instance = p;
                break;
            }
        }
        return instance;
    }

    public static Asset createByDescription(String d) {
        Asset instance = null;
        for (Asset p : Asset.values()) {
            if (p.getDescription().equals(d)) {
                instance = p;
                break;
            }
        }
        return instance;
    }

    public static String[] getStringArray() {
        String[] instance = new String[Asset.values().length];
        int i = 0;
        for (Asset p : Asset.values()) {
            instance[i++] = p.name();
        }
        return instance;
    }

    public static String[] getDescriptionStringArray() {
        String[] instance = new String[Asset.values().length];
        int i = 0;
        for (Asset p : Asset.values()) {
            instance[i++] = p.getDescription();
        }
        return instance;
    }

    public static Asset createRandom() {
        int number;

        number = Utilities.generateRandomInteger(1, 2);
        return createByNumber(number);
    }

}
