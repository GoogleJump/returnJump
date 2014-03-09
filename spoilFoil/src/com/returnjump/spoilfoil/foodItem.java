package com.returnjump.spoilfoil;

public class foodItem {  //used to represent a food item in our list of fooditems
	
	String foodName;
	int daysGood;
	
	public foodItem(String food, int daysGood){
		this.foodName = food;
		this.daysGood = daysGood;
	}
	
	@Override
	public String toString(){
		String s = this.foodName + "              " + this.daysGood+ " days good";
		return s;
	}

	
}
