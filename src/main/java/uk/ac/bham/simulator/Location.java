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
public enum Location {

    SecuredLocation(1),
    UnknownLocation(2),
    HostileLocation(3);

    private final int id;

    Location(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Location createByNumber(int id) {
        Location instance = null;
        for (Location p : Location.values()) {
            if (p.getId() == id) {
                instance = p;
                break;
            }
        }
        return instance;
    }
    
    public static Location createRandom() {
        return createByNumber(Utilities.generateRandomInteger(1, 3));
    }
}
