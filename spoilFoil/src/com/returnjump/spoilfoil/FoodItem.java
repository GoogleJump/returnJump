package com.returnjump.spoilfoil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This class represents one food item
 * @author Kelsey Hrubes
 *
 */

public class FoodItem implements FoodItemInterface { 

	private String foodName;
	private Date expireDateGood;
	private int numberOfItems;

	public FoodItem(String foodName, int daysGoodFromToday, int numberOfItems) {
		this.foodName = foodName;
		this.expireDateGood = setDaysGood(daysGoodFromToday);
        this.numberOfItems = numberOfItems;
	}

	public int getDaysGood() {
		Date today = new Date();
		long diffInMillies = this.getExpireDate().getTime() - today.getTime();
		long converted = TimeUnit.DAYS.toDays(diffInMillies);
		int days = (int) ((((converted / 24) / 60) / 60) / 1000);
		today = null;
		return days;
	}

	public Date setDaysGood(int daysGoodFromToday) {
		Date newExpireDate = new Date();

		long daysGoodFrom = TimeUnit.MILLISECONDS.convert(daysGoodFromToday, TimeUnit.DAYS);

		newExpireDate.setTime(newExpireDate.getTime() + daysGoodFrom);

		this.expireDateGood = newExpireDate;
		
		return newExpireDate;
	}

	public String getFoodItemName() {
		return this.foodName;
	}

	public Date getExpireDate() {
		return this.expireDateGood;
	}
	
	//called by the adapter
	public String toString(){
		String s;
		
		if(this.numberOfItems==0 ){
		    s = this.getFoodItemName()+"    "+ this.getDaysGood();
		}else{
		    s = this.getFoodItemName()+" ("+this.numberOfItems+")     "+ this.getDaysGood();
		}
	  
		return s;
	}


	@Override
	public int getNumberOfThisFoodItem() {
		return numberOfItems;
	}

	@Override
	public void setNumberOfThisFoodItem(int numberOfThisItem) {
		this.numberOfItems = numberOfThisItem;
	}
	
	public int getExpirationNumber() {
	    int daysGood = getDaysGood();
	    
	    if (daysGood >= 28) {
	        daysGood = daysGood / 28;
	    } else if (daysGood >= 7) {
	        daysGood = daysGood / 7;
	    }
	    
	    return daysGood;
	}
	
	public String getExpirationUnit() {
	    int daysGood = getDaysGood();
	    String unit;
	    
	    if (daysGood >= 28) {
            daysGood = daysGood / 28;
            
            if (daysGood == 1) {
                unit = "month";
            } else {
                unit = "months";
            }
        } else if (daysGood >= 7) {
            daysGood = daysGood / 7;
            
            if (daysGood == 1) {
                unit = "week";
            } else {
                unit = "weeks";
            }
        } else {
            if (daysGood == 1) {
                unit = "day";
            } else {
                unit = "days";
            }
        }
	    
	    return unit;
	}

}
