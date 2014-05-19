package com.returnjump.spoilfoil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

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

    private static String getMD5Hash(String message) {
        String hash = "";

        try {
            // Create MD5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(message.getBytes("UTF-8"));

            // Create hex string
            StringBuffer hexString = new StringBuffer();
            int n = messageDigest.length;
            for (int i = 0; i < n; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }

            hash = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }

        return hash;
    }
    
    public long put(String foodItem, Calendar expiryDate, String rawFoodItem, Bitmap image, Bitmap imageBinarized) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        String calNow = calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME);

        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_HASH, getMD5Hash(foodItem + calNow));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM, rawFoodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_CREATED_DATE, calNow);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE, calNow);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY, "DEVICE");
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE, DatabaseContract.BOOL_FALSE);
        //values.put(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE, image);
        values.putNull(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE);
        //values.put(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED, imageBinarized);
        values.putNull(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL, DatabaseContract.BOOL_FALSE);

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
    
    public Cursor read(String sortBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.FridgeTable._ID,
                DatabaseContract.FridgeTable.COLUMN_NAME_HASH,
                DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_CREATED_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY,
                DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED,
                DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED,
                DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART,
                DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART,
                DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH,
                DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL,
                };

        // How you want the results sorted in the resulting Cursor
        String sortOrder;

        if (sortBy != null) {
            sortOrder = sortBy;
        } else {
            sortOrder =
                    DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE + DatabaseContract.COMMA_SEP +
                    DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM + " ASC";
        }

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

    public void update(long rowId, String foodItem, Calendar expiryDate, Integer fromImage, Integer dismissed, Integer expired, Integer editedCart, Integer editedFridge,
                       Integer deletedCart, Integer deletedFridge, Integer notifiedPush, Integer notifiedEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        ContentValues values = new ContentValues();

        // Update value for each column (null means don't change the value)
        if (foodItem != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        }
        if (expiryDate != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        }
        if (fromImage != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE, fromImage);
        }
        if (dismissed != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED, dismissed);
        }
        if (expired != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED, expired);
        }
        if (editedCart != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART, editedCart);
        }
        if (editedFridge != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE, editedFridge);
        }
        if (deletedCart != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART, deletedCart);
        }
        if (deletedFridge != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE, deletedFridge);
        }
        if (notifiedPush != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH, notifiedPush);
        }
        if (notifiedEmail != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL, notifiedEmail);
        }
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE, calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY, "DEVICE");

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