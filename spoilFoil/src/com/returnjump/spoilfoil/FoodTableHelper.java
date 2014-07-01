package com.returnjump.spoilfoil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// Database to store the names of the parsed food data
public class FoodTableHelper extends SQLiteOpenHelper {

    private Context context;

    public FoodTableHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);

        this.context = context;
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.FoodTable.SQL_CREATE_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.FoodTable.SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long size() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        return DatabaseUtils.queryNumEntries(db, DatabaseContract.FoodTable.TABLE_NAME);
    }

    public long put(String name, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.FoodTable.COLUMN_NAME_NAME, name);
        values.put(DatabaseContract.FoodTable.COLUMN_NAME_FULL_NAME, fullName);
        values.put(DatabaseContract.FoodTable.COLUMN_NAME_FIRST_LETTER, name.substring(0, 1));

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.FoodTable.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    public long getRowIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                DatabaseContract.FoodTable._ID
        };

        String whereColumn = DatabaseContract.FoodTable.COLUMN_NAME_NAME + "=" + DatabaseContract.QUESTION_MARK;
        String[] whereValue = { name };

        Cursor c = db.query(
                DatabaseContract.FoodTable.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                whereColumn,                              // The columns for the WHERE clause
                whereValue,                               // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );
        c.moveToFirst();

        return c.getLong(c.getColumnIndexOrThrow(DatabaseContract.FoodTable._ID));
    }

    // Returns a list of food names that begin with letter
    public List<String> getAllByLetter(String letter) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                DatabaseContract.FoodTable.COLUMN_NAME_NAME
        };

        String whereColumn = DatabaseContract.FoodTable.COLUMN_NAME_FIRST_LETTER + "=" + DatabaseContract.QUESTION_MARK;
        String[] whereValue = { letter.toLowerCase() };

        Cursor c = db.query(
                DatabaseContract.FoodTable.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                whereColumn,                              // The columns for the WHERE clause
                whereValue,                               // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );
        c.moveToFirst();

        List<String> foodName = new ArrayList<String>();

        while (!c.isAfterLast()) {
            foodName.add(c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FoodTable.COLUMN_NAME_NAME)
            ));
            c.moveToNext();
        }

        return foodName;
    }

}