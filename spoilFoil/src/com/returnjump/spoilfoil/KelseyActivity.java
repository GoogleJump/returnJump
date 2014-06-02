package com.returnjump.spoilfoil;

import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;

public class KelseyActivity extends FragmentActivity implements CalendarDatePickerDialog.OnDateSetListener {
//public class KelseyActivity extends Activity {

    private ArrayAdapter<FoodItem> adapter;
    private ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();
    private FridgeDbHelper dbHelper;
    private static final String EXPIRY_DATE_PICKER_TAG = "expiry_date_picker";

    ListView fridgeListView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kelsey);

        dbHelper = new FridgeDbHelper(this);
        // dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Use this to delete database

        fridgeListView = (ListView) findViewById(R.id.foodItemListView);
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(fridgeListView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        // reverseSortedPositions always has a length = 1
                        int position = reverseSortedPositions[0];

                        //View child = MyApplication.getViewByPosition(position, fridgeListView);
                        View child = adapter.getView(position, null, fridgeListView);

                        // Set visible to false in the database for the item that was swiped
                        if (child != null) {
                            long rowId = (Long) child.getTag(R.id.food_item_id);
                            dbHelper.update(rowId, null, null, null, DatabaseContract.BOOL_TRUE, null, null, null, null, DatabaseContract.BOOL_TRUE, null, null);

                            adapter.remove(adapter.getItem(position));
                            updateListView(foodItems);
                        } else {
                            Toast.makeText(getApplicationContext(), "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        fridgeListView.setOnTouchListener(touchListener);
        fridgeListView.setOnScrollListener(touchListener.makeScrollListener());

        fridgeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {


                long rowId = (Long) view.getTag(R.id.food_item_id);


//                FridgeItem fridgeItem = dbHelper.get(rowId);
//
//                String name = fridgeItem.getFoodItem();
//                String date = fridgeItem.getExpiryDate(); // Then extract the day,month,year from this


                FoodItem testItem = foodItems.get(0);
                String name = testItem.getFoodName();
                int date = testItem.getDaysGood();

                Toast.makeText(getApplicationContext(), name + "  " + date, Toast.LENGTH_LONG).show();

                Log.d("      $$$$$$$ onLongPress", name + "  " + date + "     yeahhh ");

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if(fragmentManager.findFragmentByTag("EDIT_ACTIVITY_TAG")==null){
                    EditNameFragment editNameFragmentActivity = new EditNameFragment();
                    fragmentTransaction.add(editNameFragmentActivity, "EDIT_ACTIVITY_TAG");
                }

                fragmentTransaction.commit();





                return true;
            }
        });


        populateListView(foodItems);
		
		findViewById(R.id.submitNewItemButton).setOnClickListener(addNewItemToListView);
		findViewById(R.id.daysGoodTextView).setOnClickListener(openCalendarDialogClick);
		findViewById(R.id.daysGoodTextView).setOnFocusChangeListener(openCalendarDialogFocus);

	}

	@Override
	protected void onRestart() {
	    super.onRestart();
	    
	    populateListView(foodItems);
	}
	
	private Calendar getCalendar(int daysFromToday) {
	    Calendar c = GregorianCalendar.getInstance();
	    c.add(Calendar.DATE, daysFromToday);
	    
	    return c;
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
	    
	    if (itemId == R.id.action_settings) {
	        Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
	    } else if (itemId == R.id.action_arturo) {
	        Intent intent = new Intent(this, ArturoActivity.class);
            startActivity(intent);
            
            return true;
	    } else if (itemId == R.id.action_camera) {
	        Intent intent = new Intent(this, TastiActivity.class);
	        startActivity(intent);
	        
	        return true;
	    } else {
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void copyDatabaseToList() {
	    Cursor c = dbHelper.read(null);
	    c.moveToFirst();
	    foodItems.clear();
	    
	    while (!c.isAfterLast()) {
	        long id = c.getLong(
	                c.getColumnIndexOrThrow(DatabaseContract.FridgeTable._ID)
	                );
	        String foodName = c.getString(
	                c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM)
	                );
	        Calendar expiryDate = FridgeDbHelper.stringToCalendar(c.getString(
	                c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE)), DatabaseContract.FORMAT_DATE
	                );
	        boolean dismissed = c.getInt(c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED)) != 0;
	        
	        if (!dismissed) {
	            foodItems.add(new FoodItem(id, foodName, expiryDate, 0));
	        }
	        
	        c.moveToNext();
	    }
	}

	// We should sort our list by descending expiry date
	private void populateListView(ArrayList<FoodItem> list) {
	    copyDatabaseToList();
        adapter = new MyFoodAdapter(this, R.layout.list_fooditems, list);

        TextView emptyFridge = (TextView) findViewById(R.id.empty_fridge);

        fridgeListView.setAdapter(adapter);

        //toggles the "your fridge is empty :(" eventually we should
        // have a cool graphic of an empty fridge here or something
        if (list.size() == 0) {
            fridgeListView.setVisibility(View.GONE);
            emptyFridge.setVisibility(View.VISIBLE);
        } else {
            emptyFridge.setVisibility(View.GONE);
            fridgeListView.setVisibility(View.VISIBLE);
        }
    }

	private void updateListView(ArrayList<FoodItem> list) {
	    TextView emptyFridge = (TextView) findViewById(R.id.empty_fridge);

	    adapter.notifyDataSetChanged();

        //toggles the "your fridge is empty :(" eventually we should
        // have a cool graphic of an empty fridge here or something
	    if (list.size() == 0) {
            fridgeListView.setVisibility(View.GONE);
            emptyFridge.setVisibility(View.VISIBLE);
        } else {
            emptyFridge.setVisibility(View.GONE);
            fridgeListView.setVisibility(View.VISIBLE);
        }
    }

    private int insertToSortedList(FoodItem item) {
        int n = foodItems.size();
        int i = 0;

        while ((i < n) && ((item.getDaysGood() > foodItems.get(i).getDaysGood()) ||
                          ((item.getDaysGood() == foodItems.get(i).getDaysGood()) &&
                          (item.getFoodName().compareTo(foodItems.get(i).getFoodName())) > 0)))  {
            i++;
        }

        foodItems.add(i, item);

        return i;
    }

    private class ColorFlashTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... index) {
            //final View item = MyApplication.getViewByPosition(index[0], fridgeListView);
            final View item = adapter.getView(index[0], null, fridgeListView);

            // Fade in
            for (int x = 0; x < 64; x++) {
                final int alpha = x;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        item.setBackgroundColor(Color.argb(alpha, 134, 179, 0));
                    }
                });

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }

            // Fade out
            for (int y = 63; y >= 0; y--) {
                final int alpha = y;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        item.setBackgroundColor(Color.argb(alpha, 134, 179, 0));
                    }
                });

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }

            return null;
        }

    }
	
	private OnClickListener addNewItemToListView = new OnClickListener() {

        @Override
        public void onClick(View v) {
            EditText newItemField = (EditText) findViewById(R.id.newItemEditText);
            TextView daysGoodField = (TextView) findViewById(R.id.daysGoodTextView);

            String foodName = newItemField.getText().toString();
            String daysGood = daysGoodField.getText().toString();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (!foodName.equals("") && !daysGood.equals("")) {
                int year = (Integer) daysGoodField.getTag(R.id.year_id);
                int month = (Integer) daysGoodField.getTag(R.id.month_id);
                int day = (Integer) daysGoodField.getTag(R.id.day_id);

                Calendar expiryDate = new GregorianCalendar(year, month, day);

                // Hide the keyboard if showing
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                long id = dbHelper.put(foodName, expiryDate, foodName, null, null);
                FoodItem newFoodItem = new FoodItem(id, foodName, expiryDate, 0);
                int index = insertToSortedList(newFoodItem); //foodItems.add(newFoodItem);
                updateListView(foodItems);

                // UI clean up
                newItemField.setText("");
                daysGoodField.setText("");
                newItemField.clearFocus();
                daysGoodField.clearFocus();
                daysGoodField.setTag(R.id.year_id, 0);
                daysGoodField.setTag(R.id.month_id, 0);
                daysGoodField.setTag(R.id.day_id, 0);
                fridgeListView.smoothScrollToPosition(index); //fridgeListView.setSelection(index);
                //new ColorFlashTask().execute(index);

            } else if (foodName.equals("")) {
                // Set focus and show keyboard
                if (newItemField.requestFocus()) {
                    imm.showSoftInput(newItemField, InputMethodManager.SHOW_IMPLICIT);
                }
            } else if (daysGood.equals("")) {
                daysGoodField.requestFocus();
            }
        }

    };

    private OnClickListener openCalendarDialogClick = new OnClickListener() {

        @Override
        public void onClick(View v) {

            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            FragmentManager fm = getSupportFragmentManager();

            CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                    .newInstance(KelseyActivity.this, year, month, day);
            calendarDatePickerDialog.show(fm, "FRAG_TAG_DATE_PICKER");

            calendarDatePickerDialog.setYearRange(year,calendarDatePickerDialog.getMaxYear());

            findViewById(R.id.daysGoodTextView).clearFocus();


        }

    };

    private OnFocusChangeListener openCalendarDialogFocus = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {


// Use the current date as the default date in the picker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                FragmentManager fm = getSupportFragmentManager();

                CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                        .newInstance(KelseyActivity.this, year, month, day);

                calendarDatePickerDialog.setYearRange(year,calendarDatePickerDialog.getMaxYear());

                calendarDatePickerDialog.show(fm, "FRAG_TAG_DATE_PICKER");


                findViewById(R.id.daysGoodTextView).clearFocus();
            }
        }
    };

    //Sets the daysGood field
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {

        // Do something with the date chosen by the user
        TextView daysGood = (TextView) findViewById(R.id.daysGoodTextView);

        // Display selected date in the TextView
        Calendar expiryDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        daysGood.setText(dateFormat.format(expiryDate.getTime()));

//            // Set hidden data in view for calculations when Add button is pressed
        daysGood.setTag(R.id.year_id, year);
        daysGood.setTag(R.id.month_id, monthOfYear);
        daysGood.setTag(R.id.day_id, dayOfMonth);

    }

    @Override
    public void onResume() {
        // Reattaching to the fragment
        super.onResume();
        CalendarDatePickerDialog calendarDatePickerDialog = (CalendarDatePickerDialog) getSupportFragmentManager()
                .findFragmentByTag(EXPIRY_DATE_PICKER_TAG);
        if (calendarDatePickerDialog != null) {
            calendarDatePickerDialog.setOnDateSetListener(this);
        }
    }

    /* Old DatePicker (Leave it for now)

    private OnClickListener openCalendarDialogClick = new OnClickListener() {

        @Override
        public void onClick(View v) {            
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(), "datePicker");

            findViewById(R.id.daysGoodTextView).clearFocus();
        }
        
    };
    
    private OnFocusChangeListener openCalendarDialogFocus = new OnFocusChangeListener() {
        
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(), "datePicker");

            findViewById(R.id.daysGoodTextView).clearFocus();
        }
        }
    };
    
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.setTitle("Enter expiry date"); // (Only shows on tablets)
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() / 86400000L * 86400000L);
            // Probably should set a maximum date too
            // Calendar shown on tablets needs to be set to current date
            
            return datePickerDialog;
        }
        
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            TextView daysGood = (TextView) getActivity().findViewById(R.id.daysGoodTextView);
            
            // Display selected date in the TextView
            Calendar expiryDate = new GregorianCalendar(year, month, day);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            daysGood.setText(dateFormat.format(expiryDate.getTime()));
            
            // Set hidden data in view for calculations when Add button is pressed
            daysGood.setTag(R.id.year_id, year);
            daysGood.setTag(R.id.month_id, month);
            daysGood.setTag(R.id.day_id, day);
        }
        
    }
    */

}
