package com.returnjump.spoilfoil;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class KelseyActivity extends FragmentActivity implements CalendarDatePickerDialog.OnDateSetListener, EditNameFragment.OnEditNameButtonClickedListener, ShakeDetector.Listener {

    private ArrayAdapter<FoodItem> adapter;
    private ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();
    private FridgeDbHelper fridgeDbHelper;
    private SwipeDismissListViewTouchListener touchListener;
    protected ListView fridgeListView;
    private boolean editingItem = false;
    EditNameFragment editNameFragment;
    private String EDIT_FRAG_TAG = "edit_frag_tag";
    private String CAL_PICKER_TAG = "cal_frag_tag";
    private SensorManager sensorManager;
    private ShakeDetector sd;
    //private Sensor mAccelerometer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelsey);

        initializeDatabase();

        fridgeDbHelper = new FridgeDbHelper(this);
        // dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Use this to delete database
        fridgeListView = (ListView) findViewById(R.id.foodItemListView);

        this.initializeSwipeDismissListener();
        fridgeListView.setOnTouchListener(touchListener);
        fridgeListView.setOnScrollListener(touchListener.makeScrollListener());
        this.initializeLongClickListener();

        populateListView(foodItems);

        findViewById(R.id.submitNewItemButton).setOnClickListener(addNewItemToListView);
        findViewById(R.id.daysGoodTextView).setOnClickListener(openCalendarDialogClick);
        findViewById(R.id.daysGoodTextView).setOnFocusChangeListener(openCalendarDialogFocus);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.start(sensorManager);

        // Alarm Set up
        //Calendar cal = Calendar.getInstance();

        //Creates intent that will be called when alarm goes off
        //Intent intent = new Intent(getBaseContext(), NotificationAlarm.class);
        //PendingIntent pIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);

        //AlarmManager alarm = (AlarmManager) getSystemService(getBaseContext().ALARM_SERVICE);
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), (60 * 1000* 60 * 24), pIntent);


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        populateListView(foodItems);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sd.start(sensorManager);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sd.stop();
    }

    private void initializeFoodData(FoodTableHelper foodTableHelper, ExpiryTableHelper expiryTableHelper, JSONArray data) {
        try {

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                JSONArray expiry = item.getJSONArray("expiry");

                long rowId = foodTableHelper.put(item.getString("name"), item.getString("full_name"));

                for (int j = 0; j < expiry.length(); j++) {
                    JSONObject exp = expiry.getJSONObject(j);

                    expiryTableHelper.put(rowId, exp.getString("type"), exp.getInt("freezer"), exp.getInt("pantry"), exp.getInt("refrigerator"));
                }

            }

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // This should be asyncronous
    private void initializeDatabase() {
        FoodTableHelper foodTableHelper = new FoodTableHelper(this);
        ExpiryTableHelper expiryTableHelper = new ExpiryTableHelper(this);

        if (DatabaseContract.getCurrentVersion(this) <= DatabaseContract.DATABASE_VERSION) {

            Toast.makeText(this, "Initializing database.", Toast.LENGTH_LONG).show();

            // Clear database
            foodTableHelper.onUpgrade(foodTableHelper.getWritableDatabase(), -1, -1);
            expiryTableHelper.onUpgrade(expiryTableHelper.getWritableDatabase(), -1, -1);

            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("stilltasty_parsed.json");

                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                in.close();

                String data = new String(buffer, "UTF-8");
                JSONObject json = new JSONObject(data);

                initializeFoodData(foodTableHelper, expiryTableHelper, json.getJSONArray("data"));

                // Increment the current version so that when the DATABASE_VERSION is updated,
                // this data will be updated as well
                DatabaseContract.setCurrentVersion(DatabaseContract.DATABASE_VERSION + 1, this);

            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
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
        } else if (itemId == R.id.action_camera) {
            Intent intent = new Intent(this, TastiActivity.class);
            startActivity(intent);

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void copyDatabaseToList() {
        Cursor c = fridgeDbHelper.read(null);
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


   //helper method to consolidate setting views

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

    //we could find where to insert this in log(n) time
    private int insertToSortedList(FoodItem item) {
        int n = foodItems.size();
        int i = 0;

        while ((i < n) && ((item.getDaysGood() > foodItems.get(i).getDaysGood()) ||
                ((item.getDaysGood() == foodItems.get(i).getDaysGood()) &&
                        (item.getFoodName().compareTo(foodItems.get(i).getFoodName())) > 0))) {
            i++;
        }

        foodItems.add(i, item);

        return i;
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

                long id = fridgeDbHelper.put(foodName, expiryDate, foodName, null, null);
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
                fridgeListView.smoothScrollToPosition(index);

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
            calendarDatePickerDialog.setYearRange(year, calendarDatePickerDialog.getMaxYear());
            calendarDatePickerDialog.show(fm, "FRAG_TAG_DATE_PICKER");
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
                calendarDatePickerDialog.setYearRange(year, calendarDatePickerDialog.getMaxYear());
                calendarDatePickerDialog.show(fm, calendarDatePickerDialog.toString());
                findViewById(R.id.daysGoodTextView).clearFocus();
            }
        }
    };

    //Sets the daysGood field
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        if (!editingItem) {
            // Do something with the date chosen by the user
            TextView daysGood = (TextView) findViewById(R.id.daysGoodTextView);
            // Display selected date in the TextView
            Calendar expiryDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            daysGood.setText(dateFormat.format(expiryDate.getTime()));
            // Set hidden data in view for calculations when Add button is pressed
            daysGood.setTag(R.id.year_id, year);
            daysGood.setTag(R.id.month_id, monthOfYear);
            daysGood.setTag(R.id.day_id, dayOfMonth);

        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(getSupportFragmentManager().findFragmentByTag(EDIT_FRAG_TAG));
            ft.commit();
            editingItem = false;
            Calendar expiryDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            fridgeDbHelper.update(editNameFragment.getArguments().getLong("rowId"),editNameFragment.getArguments().getString("name"), expiryDate, null, null, null, null, DatabaseContract.BOOL_TRUE,
                    null, null, null, null);
            Toast.makeText(getApplicationContext(), "Item edited!", Toast.LENGTH_LONG).show();

            // there should be a faster way to update the listview immediately after editing an item?
            populateListView(foodItems);

        }
    }

    public void editItemSequence(View view) {
        long rowId = (Long) view.getTag(R.id.food_item_id);
        FridgeItem fridgeItem = fridgeDbHelper.getRowById(rowId, true);
        String itemName = fridgeItem.getFoodItem();
        String itemDate = fridgeItem.getExpiryDate(); // Then extract the day,month,year from this
        editingItem = true;
        editNameFragment = new EditNameFragment();
        Bundle args = new Bundle();
        args.putString("name", itemName);
        args.putString("date", itemDate);
        args.putLong("rowId", rowId);
        editNameFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(editNameFragment, EDIT_FRAG_TAG);
        fragmentTransaction.addToBackStack(EDIT_FRAG_TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void onEditNameButtonClicked() {
        Calendar c = FridgeDbHelper.stringToCalendar(editNameFragment.getArguments().getString("date"), DatabaseContract.FORMAT_DATE);
        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                .newInstance(KelseyActivity.this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        calendarDatePickerDialog.setYearRange(c.get(Calendar.YEAR), calendarDatePickerDialog.getMaxYear());
        editingItem = true;
        Bundle args = new Bundle();
        calendarDatePickerDialog.show(getSupportFragmentManager(), CAL_PICKER_TAG);

    }

    public void initializeLongClickListener() {

        fridgeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                editItemSequence(view);
                return true;
            }
        });
    }

    public void initializeSwipeDismissListener() {
        touchListener =
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
                            fridgeDbHelper.update(rowId, null, null, null, DatabaseContract.BOOL_TRUE, null, null, null, null, DatabaseContract.BOOL_TRUE, null, null);

                            adapter.remove(adapter.getItem(position));
                            updateListView(foodItems);
                        } else {
                            Toast.makeText(getApplicationContext(), "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    @Override
    public void hearShake() {
        Toast.makeText(this, "Expired food has been dismissed.", Toast.LENGTH_SHORT).show();
    }



}
