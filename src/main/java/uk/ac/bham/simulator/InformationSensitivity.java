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
public enum InformationSensitivity { 

    TopSecret(1),
    Secret(2),
    Classified(3),
    Restricted(4),
    Official(5),
    NonClassified(6),
    Clearance(7),
    CompartmentedInformation(8);

    private final int id;

    InformationSensitivity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static InformationSensitivity createByNumber(int id) {
        InformationSensitivity instance = null;
        for (InformationSensitivity p : InformationSensitivity.values()) {
            if (p.getId() == id) {
                instance = p;
                break;
            }
        }
        return instance;
    }

    public static InformationSensitivity createRandom(Position rank) {
        int number;
        
        if(rank==Position.Administrator) {
            number = Utilities.generateRandomInteger(1, 8);
        } else if (rank == Position.Researcher) {
            number = Utilities.generateRandomInteger(3, 8);
        } else {
            number = Utilities.generateRandomInteger(6, 8);
        }

        return createByNumber(number);
    }

}
