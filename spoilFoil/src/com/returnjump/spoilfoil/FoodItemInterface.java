package com.returnjump.spoilfoil;

import java.util.Calendar;

public interface FoodItemInterface {
	
	public int getDaysGood();
	
	public void setExpiryDate(Calendar newExpiryDate);
	
	public String getFoodItemName();
	
	public Calendar getExpiryDate();
	
	public int getNumberOfThisFoodItem();
	
	public void setNumberOfThisFoodItem(int numberOfThisItem);

}
