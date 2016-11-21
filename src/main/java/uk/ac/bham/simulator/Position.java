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
public enum Position {
    Administrator(1),
    Researcher(2),
    Lecturer(3);
    
    private final int id;
    
    Position(int id) {
        this.id=id;
    }
    
    public int getId() {
        return this.id;
    }
    
    public static Position createByNumber(int id) {
        Position instance = null;
        for (Position p : Position.values()) {
            if (p.getId() == id) {
                instance = p;
                break;
            }
        }
        return instance;
    }
    
    public static Position createRandom() {
        return createByNumber(Utilities.generateRandomInteger(1, 3));
    }

}
