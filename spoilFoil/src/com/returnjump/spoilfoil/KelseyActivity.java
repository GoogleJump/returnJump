package com.returnjump.spoilfoil;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class KelseyActivity extends Activity {

	ArrayAdapter<FoodItem> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kelsey);
		// Show the Up button in the action bar.
		setupActionBar();

		/*
		 * Change KelseyActivity to whatever activity will be handling push notifications
		 */
		PushService.setDefaultPushCallback(this, KelseyActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();

		final ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>(); // manually
		// added for
		// testing
		// purposes

		Date milkDate = new Date();
		Date breadDate = new Date();
		Date eggDate = new Date();
		FoodItem milk = new FoodItem("Milk", milkDate, 0);
		FoodItem bread = new FoodItem("Bread", breadDate, 2);
		FoodItem eggs = new FoodItem("Eggs", eggDate, 12);
		milk.setDaysGood(10);
		bread.setDaysGood(12);
		eggs.setDaysGood(7);
		foodItems.add(milk);
		foodItems.add(bread);
		foodItems.add(eggs);

		this.populateListView(foodItems);

		final EditText newItemField = (EditText) findViewById(R.id.newItemEditText);
		final EditText daysGoodField = (EditText) findViewById(R.id.daysGoodEditText);
		final Button submitNewItemButton = (Button) findViewById(R.id.submitNewItemButton);
		Context v = this;

		submitNewItemButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String foodName = newItemField.getText().toString();
				int daysGood = Integer.parseInt(daysGoodField.getText()
						.toString());
				
				if(TextUtils.isEmpty(daysGoodField.getText().toString()) //if either of the edittexts are empty, how should I do this?
						|| TextUtils.isEmpty(foodName)) {

					
				}
				else {	
					Date itemDate = new Date();
					FoodItem newFoodItem = new FoodItem(foodName, itemDate, 0);
					newFoodItem.setDaysGood(daysGood);
					foodItems.add(newFoodItem);
					adapter.notifyDataSetChanged();
					newItemField.setText(null);
					daysGoodField.setText(null);
				}
			}

		});

	}

	public void populateListView(ArrayList<FoodItem> list) {
		adapter = new ArrayAdapter<FoodItem>(this,
				R.layout.food_items_list_textview, list);

		ListView listView = (ListView) findViewById(R.id.foodItemListView);
		listView.setAdapter(adapter);
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
