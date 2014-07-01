package com.returnjump.spoilfoil;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

public final class DatabaseContract {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "database.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    public static final int BOOL_TRUE = 1;
    public static final int BOOL_FALSE = 0;
    public static final String BOOL_TRUE_STR = "1";
    public static final String BOOL_FALSE_STR = "0";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String COMMA_SEP = ",";
    public static final String QUESTION_MARK = "?";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME = "yyyy-MM-dd-H-m-s-S";
    
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty *private* constructor.
    private DatabaseContract() {}

    public static int getCurrentVersion(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPref.getInt(SettingsActivity.DB_VERSION, 1);
    }

    public static void setCurrentVersion(int version, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(SettingsActivity.DB_VERSION, version).commit();
    }

    public static abstract class FridgeTable implements BaseColumns {
        public static final String TABLE_NAME = "fridge";
        public static final String COLUMN_NAME_HASH = "hash";
        public static final String COLUMN_NAME_FOOD_ITEM = "food_item";
        public static final String COLUMN_NAME_RAW_FOOD_ITEM = "raw_food_item";
        public static final String COLUMN_NAME_EXPIRY_DATE = "expiry_date";
        public static final String COLUMN_NAME_CREATED_DATE = "created_date";
        public static final String COLUMN_NAME_UPDATED_DATE = "updated_date";
        public static final String COLUMN_NAME_UPDATED_BY = "update_by";
        public static final String COLUMN_NAME_FROM_IMAGE = "from_image";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_IMAGE_BINARIZED = "image_binarized";
        public static final String COLUMN_NAME_DISMISSED = "dismissed";
        public static final String COLUMN_NAME_EXPIRED = "expired";
        public static final String COLUMN_NAME_EDITED_CART = "edited_cart";
        public static final String COLUMN_NAME_EDITED_FRIDGE = "edited_fridge";
        public static final String COLUMN_NAME_DELETED_CART = "deleted_cart";
        public static final String COLUMN_NAME_DELETED_FRIDGE = "deleted_fridge";
        public static final String COLUMN_NAME_NOTIFIED_PUSH = "notified_push";
        public static final String COLUMN_NAME_NOTIFIED_EMAIL = "notified_email";
        
        public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            FridgeTable._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
            FridgeTable.COLUMN_NAME_HASH + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_FOOD_ITEM + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_EXPIRY_DATE + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_CREATED_DATE + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_UPDATED_DATE + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_UPDATED_BY + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_FROM_IMAGE + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_IMAGE + BLOB_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_IMAGE_BINARIZED + BLOB_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_DISMISSED + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_EXPIRED + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_EDITED_CART + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_EDITED_FRIDGE + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_DELETED_CART + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_DELETED_FRIDGE + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_NOTIFIED_PUSH + INTEGER_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL + INTEGER_TYPE +
            " )";

        public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + FridgeTable.TABLE_NAME;
    }

    public static abstract class FoodTable implements BaseColumns {
        public static final String TABLE_NAME = "food";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_FULL_NAME = "full_name";
        public static final String COLUMN_NAME_FIRST_LETTER = "first_letter";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        FoodTable._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        FoodTable.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                        FoodTable.COLUMN_NAME_FULL_NAME + TEXT_TYPE + COMMA_SEP +
                        FoodTable.COLUMN_NAME_FIRST_LETTER + TEXT_TYPE +
                        " )";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + FoodTable.TABLE_NAME;
    }

    public static abstract class ExpiryTable implements BaseColumns {
        public static final String TABLE_NAME = "expiry";
        public static final String COLUMN_NAME_FOOD_ID = "food_id";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_FREEZER = "freezer";
        public static final String COLUMN_NAME_PANTRY = "pantry";
        public static final String COLUMN_NAME_REFRIGERATOR = "refrigerator";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        ExpiryTable._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        ExpiryTable.COLUMN_NAME_FOOD_ID + INTEGER_TYPE + COMMA_SEP +
                        ExpiryTable.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                        ExpiryTable.COLUMN_NAME_FREEZER + INTEGER_TYPE + COMMA_SEP +
                        ExpiryTable.COLUMN_NAME_PANTRY + INTEGER_TYPE + COMMA_SEP +
                        ExpiryTable.COLUMN_NAME_REFRIGERATOR + INTEGER_TYPE + COMMA_SEP +
                        "FOREIGN KEY (" + ExpiryTable.COLUMN_NAME_FOOD_ID + ") REFERENCES " + FoodTable.TABLE_NAME + "(" + FoodTable._ID + ")" +
                        " )";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + ExpiryTable.TABLE_NAME;
    }

}