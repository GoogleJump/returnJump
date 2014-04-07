package com.returnjump.spoilfoil;

import android.annotation.TargetApi;
import android.location.GpsStatus.NmeaListener;
import android.os.Build;

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

	public FoodItem(String foodName, Date expireDateGood, int numberOfItems) {
		this.foodName = foodName;
		this.expireDateGood = expireDateGood;
        this.numberOfItems = numberOfItems;
	}


	@Override
	public int getDaysGood() {
		Date today = new Date();
		long diffInMillies = this.getExpireDate().getTime() - today.getTime();
		long converted = TimeUnit.DAYS.toDays(diffInMillies);
		int days = (int) ((((converted / 24) / 60) / 60) / 1000);
		today = null;
		return days;
	}

	
	@Override
	public void setDaysGood(int daysGoodFromToday) {
		Date newExpireDate = new Date();

		long daysGoodFrom = TimeUnit.MILLISECONDS.convert(daysGoodFromToday,
				TimeUnit.DAYS);

		newExpireDate.setTime(newExpireDate.getTime() + daysGoodFrom);

		this.expireDateGood = newExpireDate;

	}

	@Override
	public String getFoodItemName() {
		return this.foodName;
	}

	@Override
	public Date getExpireDate() {
		return this.expireDateGood;
	}

	@Override  //called by the adapter
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

}
