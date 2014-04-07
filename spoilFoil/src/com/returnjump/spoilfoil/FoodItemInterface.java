package com.returnjump.spoilfoil;

import java.util.Date;

public interface FoodItemInterface {
	
	public int getDaysGood();
	
	public void setDaysGood(int daysGoodFromToday);
	
	public String getFoodItemName();
	
	public Date getExpireDate();
	
	public int getNumberOfThisFoodItem();
	
	public void setNumberOfThisFoodItem(int numberOfThisItem);

}
