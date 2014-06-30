package com.returnjump.spoilfoil;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class LetterDbHelper extends SQLiteOpenHelper {

    private Context context;

    public LetterDbHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);

        this.context = context;
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.LetterTable.SQL_CREATE_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.LetterTable.SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long size() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        return DatabaseUtils.queryNumEntries(db, DatabaseContract.LetterTable.TABLE_NAME);
    }
    
    public long put(String letter, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.LetterTable.COLUMN_NAME_LETTER, letter);
        values.put(DatabaseContract.LetterTable.COLUMN_NAME_POSITION, position);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.LetterTable.TABLE_NAME,
                null,
                values);
        
        return newRowId;
    }

    public int getPosition(String letter) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                DatabaseContract.LetterTable.COLUMN_NAME_POSITION
        };

        String whereColumn = DatabaseContract.LetterTable.COLUMN_NAME_LETTER + "=" + DatabaseContract.QUESTION_MARK;
        String[] whereValue = { letter.toLowerCase() };

        Cursor c = db.query(
                DatabaseContract.LetterTable.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                whereColumn,                              // The columns for the WHERE clause
                whereValue,                               // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );
        c.moveToFirst();

        return c.getInt(
                c.getColumnIndexOrThrow(DatabaseContract.LetterTable.COLUMN_NAME_POSITION)
        );
    }

}