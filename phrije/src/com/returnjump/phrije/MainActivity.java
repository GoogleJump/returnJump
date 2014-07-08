package com.returnjump.phrije;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity implements CalendarDatePickerDialog.OnDateSetListener, EditNameFragment.OnEditNameButtonClickedListener, ShakeDetector.Listener, UndoBarController.AdvancedUndoListener {

    private ArrayAdapter<FridgeItem> adapter;
    private ArrayList<FridgeItem> fridgeItems = new ArrayList<FridgeItem>();
    private FridgeDbHelper fridgeDbHelper;
    private SwipeDismissListViewTouchListener touchListener;
    protected ListView fridgeListView;
    EditNameFragment editNameFragment;
    private String EDIT_FRAG_TAG = "edit_frag_tag";
    private String CAL_PICKER_TAG = "cal_frag_tag";
    private SensorManager sensorManager;
    private ShakeDetector sd;
    private Activity activity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        fridgeDbHelper = new FridgeDbHelper(this);
        fridgeListView = (ListView) findViewById(R.id.foodItemListView);

        if (DatabaseContract.getCurrentVersion(getApplicationContext()) <= DatabaseContract.DATABASE_VERSION) {
            new InitializeDatabaseTask().execute();

            // Set email from device account
            setDeviceEmail();
        }

        this.initializeSwipeDismissListener();
        fridgeListView.setOnTouchListener(touchListener);
        fridgeListView.setOnScrollListener(touchListener.makeScrollListener());
        this.initializeLongClickListener();

        populateListView();

        LinearLayout addButtonBar = (LinearLayout) findViewById(R.id.addNewItemButtonBar);
        ImageButton addButton = (ImageButton) findViewById(R.id.addNewItemButton);
        addButtonBar.setOnClickListener(addNewItemOnClickListener);
        addButton.setOnClickListener(addNewItemOnClickListener);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.start(sensorManager);

        // Initialize the alarm if it hasn't been initialized before and the user wants a notif
        if (!SettingsActivity.getAlarmSet(this) && SettingsActivity.isUserNotificationEnabled(this)) {
            SettingsActivity.initializeAlarm(this);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        populateListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        sd.start(sensorManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UndoBarController.clear(this);
        sd.stop();
    }

    private void setDeviceEmail() {
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
        String email = accounts.length > 0 ? accounts[0].name : null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String emailPref = sharedPref.getString(SettingsActivity.PREF_EMAIL_ADDRESS, "");

        if (emailPref.equals("")) {
            sharedPref.edit().putString(SettingsActivity.PREF_EMAIL_ADDRESS, email).commit();
        }
    }

    private class InitializeDatabaseTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Preparing our database...", Toast.LENGTH_SHORT).show();
        }

        private String initializeFoodData(FoodTableHelper foodTableHelper, ExpiryTableHelper expiryTableHelper, JSONArray data) {
            String message = "";

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

                message = "Database has been prepared successfully.";

            } catch (JSONException e) {
                message = e.getMessage();
            }

            return message;
        }

        private String initializeDatabase() {

            String message = "";

            FoodTableHelper foodTableHelper = new FoodTableHelper(getApplicationContext());
            ExpiryTableHelper expiryTableHelper = new ExpiryTableHelper(getApplicationContext());

            // Clear database
            foodTableHelper.onUpgrade(foodTableHelper.getWritableDatabase(), -1, -1);
            expiryTableHelper.onUpgrade(expiryTableHelper.getWritableDatabase(), -1, -1);

            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("parsed.json");

                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                in.close();

                String data = new String(buffer, "UTF-8");
                JSONObject json = new JSONObject(data);

                message = initializeFoodData(foodTableHelper, expiryTableHelper, json.getJSONArray("data"));

                // Increment the current version so that when the DATABASE_VERSION is updated,
                // this data will be updated as well
                DatabaseContract.setCurrentVersion(DatabaseContract.DATABASE_VERSION + 1, getApplicationContext());

            } catch (IOException e) {
                message = e.getMessage();
            } catch (JSONException e) {
                message = e.getMessage();
            }

            return message;
        }

        @Override
        protected String doInBackground(Void... voids) {

            // Will only create table if it doesn't exist (table not found error)
            fridgeDbHelper.onCreate(fridgeDbHelper.getWritableDatabase());

            return initializeDatabase();
        }

        protected void onPostExecute(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(this, ShoppingCartActivity.class);
            startActivity(intent);

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void copyDatabaseToList() {
        boolean isAtLeastOneExpired = false;

        Cursor c = fridgeDbHelper.read(null);
        c.moveToFirst();
        fridgeItems.clear();

        while (!c.isAfterLast()) {
            long id = c.getLong(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable._ID)
            );
            String foodName = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM)
            );
            String expiryDate = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE)
            );
            boolean dismissed = c.getInt(c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED)) != 0;

            if (!dismissed) {
                fridgeItems.add(new FridgeItem(id, foodName, expiryDate));

                // Check if the item has expired
                if (expiryDate.compareTo(FridgeDbHelper.calendarToString(Calendar.getInstance(), DatabaseContract.FORMAT_DATE)) <= 0) {
                    isAtLeastOneExpired = true;
                }
            }

            c.moveToNext();
        }

        if (isAtLeastOneExpired) {
            Toast.makeText(this, "Shake to dismiss expired food.", Toast.LENGTH_SHORT).show();
        }
    }

    // We should sort our list by descending expiry date
    private void populateListView() {
        copyDatabaseToList();
        adapter = new MyFridgeAdapter(this, R.layout.list_fooditems, fridgeItems);

        TextView emptyFridge = (TextView) findViewById(R.id.empty_fridge);

        fridgeListView.setAdapter(adapter);

        //toggles the "your fridge is empty :(" eventually we should
        // have a cool graphic of an empty fridge here or something
        if (fridgeItems.size() == 0) {
            fridgeListView.setVisibility(View.GONE);
            emptyFridge.setVisibility(View.VISIBLE);
        } else {
            emptyFridge.setVisibility(View.GONE);
            fridgeListView.setVisibility(View.VISIBLE);
        }
    }


   //helper method to consolidate setting views

    private void updateListView() {
        TextView emptyFridge = (TextView) findViewById(R.id.empty_fridge);

        adapter.notifyDataSetChanged();

        //toggles the "your fridge is empty :(" eventually we should
        // have a cool graphic of an empty fridge here or something
        if (fridgeItems.size() == 0) {
            fridgeListView.setVisibility(View.GONE);
            emptyFridge.setVisibility(View.VISIBLE);
        } else {
            emptyFridge.setVisibility(View.GONE);
            fridgeListView.setVisibility(View.VISIBLE);
        }
    }

    //we could find where to insert this in log(n) time
    private int insertToSortedList(FridgeItem item) {
        int n = fridgeItems.size();
        int i = 0;

        while ((i < n) && ((item.getDaysGood() > fridgeItems.get(i).getDaysGood()) ||
                          ((item.getDaysGood() == fridgeItems.get(i).getDaysGood()) &&
                          (item.getName().compareTo(fridgeItems.get(i).getName())) > 0))) {
            i++;
        }

        fridgeItems.add(i, item);

        return i;
    }

    public void initializeLongClickListener() {

        fridgeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                editItemSequence(view, false);
                return true;
            }
        });
    }

    private OnClickListener addNewItemOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            editItemSequence(view, true);
            return;
        }
    };

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

                            String name = adapter.getItem(position).getName().toString();
                            Bundle b = new Bundle();
                            b.putString("foodName", name);
                            b.putLong("rowId", rowId);
                            new UndoBarController.UndoBar(activity)
                                                 .message("Removed " + name)
                                                 .listener((UndoBarController.UndoListener) activity)
                                                 .token(b)
                                                 .show();

                            adapter.remove(adapter.getItem(position));
                            updateListView();

                        } else {
                            Toast.makeText(getApplicationContext(), "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    public void editItemSequence(View view, Boolean isNewItem) {
        String itemName = "";
        String itemDate = "";
        long rowId = (long)0;
        if(!isNewItem){
            rowId = (Long) view.getTag(R.id.food_item_id);
            FridgeItem fridgeItem = fridgeDbHelper.getRowById(rowId, true);
            itemName = fridgeItem.getName();
            itemDate = fridgeItem.getExpiryDate();
        }
        editNameFragment = new EditNameFragment();
        Bundle args = new Bundle();
        args.putString("name", itemName);
        args.putString("date", itemDate);
        args.putLong("rowId", rowId);
        args.putBoolean("isNewItem", isNewItem);
        editNameFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(editNameFragment, EDIT_FRAG_TAG);
        fragmentTransaction.addToBackStack(EDIT_FRAG_TAG);
        fragmentTransaction.commit();
        //onEditNameButtonClicked called next
    }

    @Override
    public void onEditNameButtonClicked(Boolean isNewItem) {
        Calendar c;
        if(isNewItem){
            c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
        }else {
            c = FridgeDbHelper.stringToCalendar(editNameFragment.getArguments().getString("date"), DatabaseContract.FORMAT_DATE);
        }
        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                .newInstance(MainActivity.this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        calendarDatePickerDialog.setYearRange(c.get(Calendar.YEAR), calendarDatePickerDialog.getMaxYear());
        calendarDatePickerDialog.show(getSupportFragmentManager(), CAL_PICKER_TAG);
        //onDateSet called next
    }
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(getSupportFragmentManager().findFragmentByTag(EDIT_FRAG_TAG));
        ft.commit();

        Calendar expiryDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        String foodName = editNameFragment.getArguments().getString("name");

        if(editNameFragment.getArguments().getBoolean("isNewItem")){
            long id = fridgeDbHelper.put(foodName, expiryDate, foodName, DatabaseContract.BOOL_FALSE, null, null);
            FridgeItem newFridgeItem = new FridgeItem(id, foodName, FridgeDbHelper.calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
            int index = insertToSortedList(newFridgeItem);
            updateListView();
            fridgeListView.smoothScrollToPosition(index);
        }else {
            // Check if the item is expired or not and update the boolean accordingly
            fridgeDbHelper.update(editNameFragment.getArguments().getLong("rowId"), foodName , expiryDate, null, null, null, null, DatabaseContract.BOOL_TRUE,
                    null, null, null, null);
            Toast.makeText(getApplicationContext(), "Item edited.", Toast.LENGTH_LONG).show();
            populateListView();
        }
        editNameFragment = null;
    }

    @Override
    public void hearShake() {

        String[] column = {DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED};
        String[] operator =  {"<=", "="};
        String[] wherevalue = {FridgeDbHelper.calendarToString(Calendar.getInstance(), DatabaseContract.FORMAT_DATE), DatabaseContract.BOOL_FALSE_STR};
        String[] conjunction = {DatabaseContract.AND};

        List<FridgeItem> expiredFridgeItems = fridgeDbHelper.getAll(column, operator, wherevalue, conjunction, true);

        for (int i = 0; i < expiredFridgeItems.size(); i++) {
            FridgeItem fridgeItem = expiredFridgeItems.get(i);

            fridgeDbHelper.update(fridgeItem.getRowId(), null, null, null, DatabaseContract.BOOL_TRUE, DatabaseContract.BOOL_TRUE, null, null, null, null, null, null);
        }

        if (expiredFridgeItems.size() > 0) {
            // there should be a faster way to update the listview immediately after editing an item?
            populateListView();

            Toast.makeText(this, "Expired food has been dismissed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when user presses button in Undobar
     * @param token
     */
    @Override
    public void onUndo(Parcelable token) {
        if (token != null) {
            final String foodName = ((Bundle) token).getString("foodName");
            final long rowId = ((Bundle) token).getLong("rowId");

            fridgeDbHelper.update(rowId, null, null, null, DatabaseContract.BOOL_FALSE, null, null, null, null, DatabaseContract.BOOL_FALSE, null, null);

            // there should be a faster way to update the listview immediately after editing an item?
            populateListView();

            // Need to add an ellipsis to long names
            Toast.makeText(this, "Added back " + foodName, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the Undobar fades out after duration without button clicked.
     * @param token
     */
    @Override
    public void onHide(Parcelable token) {

    }

    @Override
    public void onClear() {

    }
}
