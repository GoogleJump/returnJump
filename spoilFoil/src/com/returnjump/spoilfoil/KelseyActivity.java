package com.returnjump.spoilfoil;

import java.util.ArrayList;
import java.util.Scanner;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class KelseyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kelsey);
		// Show the Up button in the action bar.
		setupActionBar();
		
		ArrayList<foodItem> foodItems = new ArrayList<foodItem>();  //manually added for testing purposes
		foodItem milk = new foodItem("Milk", 10);
		foodItem bread = new foodItem("Bread", 30);
		foodItem eggs = new foodItem("Eggs", 12);
		foodItems.add(milk);
		foodItems.add(bread);
		foodItems.add(eggs);
		
	  final ArrayAdapter adapter = new ArrayAdapter (this, R.layout.food_item_textview, foodItems);
	  
	  ListView listView = (ListView) findViewById(R.id.foodItemListView);
	  listView.setAdapter(adapter);
		
	  EditText textField = (EditText) findViewById(R.id.newItemEditText);
	 String entry  = textField.getText().toString();
	 Scanner scan = new Scanner(entry);
	 try{
	  if(entry.indexOf(" ")==-1){
		  throw new IllegalArgumentException("Please enter an item and number of days it is good");
	  }
	  String foodName = scan.next();
	  int daysGood = Integer.parseInt(scan.next());
	  foodItem newFoodItem = new foodItem(foodName, daysGood);
	  foodItems.add(newFoodItem);
	 }
	  catch (NumberFormatException n){
		  
	  }catch (IllegalArgumentException i){
		  
	  }	  
	  }
	  
	  
	 

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kelsey, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
