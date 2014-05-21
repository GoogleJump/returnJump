package com.returnjump.spoilfoil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;
import com.doomonafireball.betterpickers.expirationpicker.ExpirationPickerBuilder;
import com.doomonafireball.betterpickers.expirationpicker.ExpirationPickerDialogFragment;
import com.parse.ParseInstallation;
import com.parse.PushService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class KelseyActivity extends FragmentActivity implements CalendarDatePickerDialog.OnDateSetListener {

    private ArrayAdapter<FoodItem> adapter;
    private ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();
    private FridgeDbHelper dbHelper;
    private static final String EXPIRY_DATE_PICKER_TAG = "expiry_date_picker";

    ListView fridgeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelsey);

		/*
         * Change KelseyActivity to whatever activity will be handling push notifications
		 */
        PushService.setDefaultPushCallback(this, KelseyActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();

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
                        //View child = fridgeListView.getChildAt(position);
                        View child = MyApplication.getViewByPosition(position, fridgeListView);

                        // Set visible to false in the database for the item that was swiped
                        if (child != null) {
                            long rowId = (Long) child.getTag(R.id.food_item_id);
                            dbHelper.update(rowId, null, null, DatabaseContract.BOOL_FALSE, null);
                            adapter.remove(adapter.getItem(position));
                            updateListView(foodItems);
                        } else {
                            Toast.makeText(getApplicationContext(), "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        fridgeListView.setOnTouchListener(touchListener);
        fridgeListView.setOnScrollListener(touchListener.makeScrollListener());

        dbHelper = new FridgeDbHelper(this);
        // dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Use this to delete database
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
            //does nothing when selected
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
        Cursor c = dbHelper.read();
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
            boolean visible = 0 != c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE)
            );

            if (visible) {
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

                long id = dbHelper.put(foodName, expiryDate, foodName, null);
                FoodItem newFoodItem = new FoodItem(id, foodName, expiryDate, 0);
                foodItems.add(newFoodItem);
                updateListView(foodItems);

                // UI clean up
                newItemField.setText("");
                daysGoodField.setText("");
                newItemField.clearFocus();
                daysGoodField.clearFocus();
                daysGoodField.setTag(R.id.year_id, 0);
                daysGoodField.setTag(R.id.month_id, 0);
                daysGoodField.setTag(R.id.day_id, 0);
                fridgeListView.setSelection(adapter.getCount() - 1); // Should change if list is sorted

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


}
