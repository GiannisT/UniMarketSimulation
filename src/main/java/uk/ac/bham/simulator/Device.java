/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

/**
 *
 * @author Francisco Ramirez
 */
public enum Device {
    UniversityDevice(1),
    SharedPublicDevice(2),
    PersonalDevice(3);
    
    private final int id;
    
    Device(int id) {
        this.id=id;
    }
    
    public int getId() {
        return id;
    }
    
    public static Device createByNumber(int id) {
        Device instance = null;
        for (Device p : Device.values()) {
            if (p.getId() == id) {
                instance = p;
                break;
            }
        }
        return instance;
    }
    
    public static Device createRandom() {
        return createByNumber(Utilities.generateRandomInteger(1, 3));
    }
    
}
