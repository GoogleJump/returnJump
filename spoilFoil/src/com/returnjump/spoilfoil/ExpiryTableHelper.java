package com.returnjump.spoilfoil;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Database to store the expiration amount of the parsed food data
// with a relation to FoodDbHelper
public class ExpiryTableHelper extends SQLiteOpenHelper {

    private Context context;

    public ExpiryTableHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);

        this.context = context;
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.ExpiryTable.SQL_CREATE_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.ExpiryTable.SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long size() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        return DatabaseUtils.queryNumEntries(db, DatabaseContract.ExpiryTable.TABLE_NAME);
    }

    public long put(long foodId, String type, int freezer, int pantry, int refrigerator) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.ExpiryTable.COLUMN_NAME_FOOD_ID, foodId);
        values.put(DatabaseContract.ExpiryTable.COLUMN_NAME_TYPE, type);
        values.put(DatabaseContract.ExpiryTable.COLUMN_NAME_FREEZER, freezer);
        values.put(DatabaseContract.ExpiryTable.COLUMN_NAME_PANTRY, pantry);
        values.put(DatabaseContract.ExpiryTable.COLUMN_NAME_REFRIGERATOR, refrigerator);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.ExpiryTable.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

}