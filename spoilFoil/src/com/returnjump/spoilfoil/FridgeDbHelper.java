package com.returnjump.spoilfoil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FridgeDbHelper extends SQLiteOpenHelper {
    
    public FridgeDbHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.FridgeTable.SQL_CREATE_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.FridgeTable.SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long size() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        return DatabaseUtils.queryNumEntries(db, DatabaseContract.FridgeTable.TABLE_NAME);
    }
    
    public static String calendarToString(Calendar cal, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        
        return dateFormat.format(cal.getTime());
    }
    
    public long put(String foodItem, Calendar expiryDate, String rawFoodItem, Bitmap image) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM, rawFoodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_ADDED_DATE, calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_LAST_UPDATE_DATE, calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME));
        //values.put(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE, image);
        values.putNull(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE, DatabaseContract.BOOL_TRUE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED, DatabaseContract.BOOL_FALSE);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.FridgeTable.TABLE_NAME,
                null,
                values);
        
        return newRowId;
    }
    
    public static Calendar stringToCalendar(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = GregorianCalendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            cal = null;
        }
        
        return cal;
    }
    
    public Cursor read() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.FridgeTable._ID,
                DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_ADDED_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_LAST_UPDATE_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE,
                DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED,
                };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE + " ASC";

        Cursor c = db.query(
                DatabaseContract.FridgeTable.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
                );
     
        return c;
    }
    
    // Instead of deleting, this implementation should just set the visible column to false using update()
    public void delete(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Define 'where' part of query.
        String selection = DatabaseContract.FridgeTable._ID + " LIKE ?";
        
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) };
        
        // Issue SQL statement.
        db.delete(DatabaseContract.FridgeTable.TABLE_NAME, selection, selectionArgs);
    }
    
    public void update(long rowId, String foodItem, Calendar expiryDate, Integer visible, Integer notified) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        ContentValues values = new ContentValues();

        // New value for one column (null means don't change the value)
        if (foodItem != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        }
        if (expiryDate != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        }
        if (visible != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE, visible);
        }
        if (notified != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED, notified);
        }
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_LAST_UPDATE_DATE, calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME));

        // Which row to update, based on the ID
        String selection = DatabaseContract.FridgeTable._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) };

        int count = db.update(
            DatabaseContract.FridgeTable.TABLE_NAME,
            values,
            selection,
            selectionArgs);
    }
}