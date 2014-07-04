package com.returnjump.phrige;

public class FoodExpiry {

    private String type;
    private int freezer;
    private int pantry;
    private int refrigerator;

    public FoodExpiry(String type, int freezer, int pantry, int refrigerator) {
        this.type = type;
        this.freezer = freezer;
        this.pantry = pantry;
        this.refrigerator = refrigerator;
    }

    public String getType() {
        return this.type;
    }

    public int getFreezerDays() {
        return this.freezer;
    }

    public int getPantryDays() {
        return this.pantry;
    }

    public int getRefrigeratorDays() {
        return this.refrigerator;
    }

}
