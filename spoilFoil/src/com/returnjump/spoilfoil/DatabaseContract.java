package com.returnjump.spoilfoil;

import android.provider.BaseColumns;

public final class DatabaseContract {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "database.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    public static final int BOOL_TRUE = 1;
    public static final int BOOL_FALSE = 0;
    private static final String COMMA_SEP = ",";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME = "yyyy-MM-dd-H-m-s-S";
    
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty *private* constructor.
    private DatabaseContract() {}

    // Inner class that defines the table contents
    public static abstract class FridgeTable implements BaseColumns {
        public static final String TABLE_NAME = "fridge";
        public static final String COLUMN_NAME_FOOD_ITEM = "fooditem";
        public static final String COLUMN_NAME_EXPIRY_DATE = "expirydate";
        public static final String COLUMN_NAME_RAW_FOOD_ITEM = "rawfooditem";
        public static final String COLUMN_NAME_ADDED_DATE = "addeddate";
        public static final String COLUMN_NAME_LAST_UPDATE_DATE = "lastupdatedate";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_VISIBLE = "visible";
        public static final String COLUMN_NAME_NOTIFIED = "notified";
        
        public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            FridgeTable._ID + " INTEGER PRIMARY KEY," +
            FridgeTable.COLUMN_NAME_FOOD_ITEM + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_EXPIRY_DATE + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_ADDED_DATE + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_LAST_UPDATE_DATE + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_IMAGE + BLOB_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_VISIBLE + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_NOTIFIED + INTEGER_TYPE +
            " )";

        public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + FridgeTable.TABLE_NAME;
    }
}