package com.returnjump.spoilfoil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class KelseyActivity extends Activity {

	private ArrayAdapter<FoodItem> adapter;
	private ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();

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

		// added for
		// testing
		// purposes

		FoodItem milk = new FoodItem("Milk", 10, 0);
		FoodItem bread = new FoodItem("Bread", 12, 2);
		FoodItem eggs = new FoodItem("Eggs", 7, 12);
		FoodItem apple = new FoodItem("Apple", 14, 5);
		FoodItem cookies = new FoodItem("Cookies", 30, 24);
		FoodItem orangeJuice = new FoodItem("Orange Juice", 21, 1);
		FoodItem cereal = new FoodItem("Honey Nut Cheerios", 180, 1);
		
		foodItems.add(milk);
		foodItems.add(bread);
		foodItems.add(eggs);
		foodItems.add(apple);
		foodItems.add(cookies);
		foodItems.add(orangeJuice);
		foodItems.add(cereal);

		populateListView(foodItems);

		findViewById(R.id.submitNewItemButton).setOnClickListener(addNewItemToListView);
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
	    int itemId = item.getItemId();
	    
	    if (itemId == android.R.id.home) {
	        NavUtils.navigateUpFromSameTask(this);
            return true;
	    } else if (itemId == R.id.action_settings) {
	        return true;
	    } else if (itemId == R.id.action_camera) {
	        Intent intent = new Intent(this, TastiActivity.class);
	        startActivity(intent);
	        
	        return true;
	    } else {
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private OnClickListener addNewItemToListView = new OnClickListener() {

        @Override
        public void onClick(View v) {
            EditText newItemField = (EditText) findViewById(R.id.newItemEditText);
            EditText daysGoodField = (EditText) findViewById(R.id.daysGoodEditText);
            
            String foodName = newItemField.getText().toString();
            String daysGood = daysGoodField.getText().toString();
            
            if (!foodName.equals("") && !daysGood.equals("")) { 
                FoodItem newFoodItem = new FoodItem(foodName, Integer.parseInt(daysGood), 0);
                foodItems.add(newFoodItem);
                adapter.notifyDataSetChanged();
                newItemField.setText(null);
                daysGoodField.setText(null);
            }
        }
        
    };

    private void populateListView(ArrayList<FoodItem> list) {
        adapter = new MyFoodAdapter(this, R.layout.list_fooditems, list);

        ListView listView = (ListView) findViewById(R.id.foodItemListView);
        listView.setAdapter(adapter);
    }
	
	private static class MyFoodAdapter extends ArrayAdapter<FoodItem> {
	      private Context context;
	      private int layout;
	      private List<FoodItem> foods;
	      
	      public MyFoodAdapter(Context context, int layout, List<FoodItem> foods) {
	          super(context, layout, foods);
	          this.context = context;
	          this.layout = layout;
	          this.foods = foods;
	      }

	      @Override
	      public View getView(int position, View convertView, ViewGroup parent) {
	          // Make sure we have a view to work with (may have been given null)
	          View itemView = convertView;
	          if (itemView == null) {
	              itemView = LayoutInflater.from(context).inflate(layout, parent, false);
	          }
	          
	          FoodItem currentFood = foods.get(position);
	          
	          TextView foodItemName = (TextView) itemView.findViewById(R.id.food_item_name);
	          foodItemName.setText(currentFood.getFoodItemName());
	          
	          TextView expirationNumber = (TextView) itemView.findViewById(R.id.expiration_number);
	          expirationNumber.setText(Integer.toString(currentFood.getExpirationNumber()));
	          
	          TextView expirationUnit = (TextView) itemView.findViewById(R.id.expiration_unit);
	          expirationUnit.setText(currentFood.getExpirationUnit());

	          return itemView;
	      }               
	  }

}
