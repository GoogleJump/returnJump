package com.returnjump.spoilfoil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * This class represents one food item
 * @author Kelsey Hrubes
 *
 */

public class FoodItem {

    private long id;
	private String foodName;
	private Calendar expiryDate;
	private int numberOfItems;

	public FoodItem(long id, String foodName, Calendar expiryDate, int numberOfItems) {
		this.id = id;
	    this.foodName = foodName;
		this.expiryDate = expiryDate;
        this.numberOfItems = numberOfItems;
	}
	
	public long getId() {
	    return this.id;
	}
	
	public void setId(long newId) {
	    this.id = newId;
	}
	
	public void setFoodName(String foodName) {
	    this.foodName = foodName;
	}

    public String getFoodName() {
        return this.foodName;
    }

	public int getDaysGood() {
	    long today = GregorianCalendar.getInstance().getTimeInMillis() / 86400000L * 86400000L;
	    long expiryDay = this.expiryDate.getTimeInMillis();
		int diffInDays = (int) ((expiryDay - today) / 86400000L);
		
	    return diffInDays;
	}

	public void setExpiryDate(Calendar newExpiryDate) {
		this.expiryDate = newExpiryDate;
	}

	public Calendar getExpiryDate() {
		return this.expiryDate;
	}

	public int getNumberOfThisFoodItem() {
		return this.numberOfItems;
	}

	public void setNumberOfThisFoodItem(int numberOfThisItem) {
		this.numberOfItems = numberOfThisItem;
	}
	
	public int getExpirationNumber() {
	    int timeGood = getDaysGood();
	    
	    if (timeGood >= 365) {
	        timeGood = timeGood / 365;
	    } else if (timeGood >= 28) {
	        timeGood = timeGood / 28;
	    } else if (timeGood >= 7) {
	        timeGood = timeGood / 7;
	    }
	    
	    return timeGood;
	}
	
	public String getExpirationUnit() {
	    int timeGood = getDaysGood();
	    String unit;
	    
	    if (timeGood >= 365) {
	        timeGood = timeGood / 365;
            
            if (timeGood == 1) {
                unit = "year";
            } else {
                unit = "years";
            }
	    } else if (timeGood >= 28) {
	        timeGood = timeGood / 28;
            
            if (timeGood == 1) {
                unit = "month";
            } else {
                unit = "months";
            }
        } else if (timeGood >= 7) {
            timeGood = timeGood / 7;
            
            if (timeGood == 1) {
                unit = "week";
            } else {
                unit = "weeks";
            }
        } else {
            if (timeGood == 1) {
                unit = "day";
            } else {
                unit = "days";
            }
        }
	    
	    return unit;
	}

}
