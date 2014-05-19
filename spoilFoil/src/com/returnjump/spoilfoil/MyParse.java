package com.returnjump.spoilfoil;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MyParse {
    final public static int PE_ObjectNotFound = 101;

    private static interface MyCallbackInterface {

        public void success(Object result);
        public void error();
        public void fallback(String message);

    }

    /*
        Return values:
            NO_NETWORK = -1
            TYPE_MOBILE = 0
            TYPE_WIFI = 1

         See http://developer.android.com/reference/android/net/ConnectivityManager.html for others
     */
    public static int networkConnectionType(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return networkInfo.getType();
        } else {
            Toast.makeText(context, "Connect to the internet and try again.", Toast.LENGTH_SHORT).show();

            return -1;
        }
    }

    public static void initialize(Context context, String appId, String clientKey) {
        Parse.initialize(context, appId, clientKey);
    }

    public static ParseInstallation getInstallation() {
        return ParseInstallation.getCurrentInstallation();
    }

    public static String getInstallationId() {
        return getInstallation().getInstallationId();
    }

    public static String getUserParseClass() {
        return "User_" + getInstallation().getObjectId();
    }

    public static void saveInstallationEventually(Context context) {
        ParseInstallation.getCurrentInstallation().saveEventually();
    }

    public static void savePreferenceToCloud(final Context context) {

        if (networkConnectionType(context) != -1) {
            isUserClassSet(new MyCallbackInterface() {

                @Override
                public void success(Object result) {
                    ParseObject user = (ParseObject) result;
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String emailAddress = sharedPref.getString(SettingsActivity.PREF_EMAIL_ADDRESS, "");
                    boolean notifyPush = sharedPref.getBoolean(SettingsActivity.PREF_CHECKBOX_PUSH, true);
                    boolean notifyEmail = sharedPref.getBoolean(SettingsActivity.PREF_CHECKBOX_EMAIL, false);

                    user.put("email", emailAddress);
                    user.put("notifyPush", notifyPush);
                    user.put("notifyEmail", notifyEmail);

                    user.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                fallback("Preferences saved to cloud.");
                            } else {
                                fallback(e.getMessage());
                            }
                        }
                    });
                }

                @Override
                public void error() {

                    ParseObject userClass = new ParseObject(getUserParseClass());

                    userClass.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseObject user = new ParseObject("Users");

                                user.put("installationObjectId", getInstallation().getObjectId());
                                user.put("installationId", getInstallationId());

                                success(user);
                            } else {
                                fallback(e.getMessage());
                            }
                        }
                    });

                }

                @Override
                public void fallback(String message) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    /* Use later for syncing multi-device accounts

    private static List<FridgeItem> getCloudFridge(List<ParseObject> parseCloudFridge) {
        List<FridgeItem> cloudFridge = new ArrayList<FridgeItem>();
        int n = parseCloudFridge.size();

        for (int i = 0; i < n; i++) {
            ParseObject parseCloudFridgeItem = parseCloudFridge.get(i);

            long rowId = parseCloudFridgeItem.getLong("rowId");
            String hash = parseCloudFridgeItem.getString("hash");
            String foodItem = parseCloudFridgeItem.getString("foodItem");
            String rawFoodItem = parseCloudFridgeItem.getString("rawFoodItem");
            String expiryDate = parseCloudFridgeItem.getString("expiryDate");
            String createdDate = parseCloudFridgeItem.getString("createdDate");
            String updatedDate = parseCloudFridgeItem.getString("updatedDate");
            String updatedBy = parseCloudFridgeItem.getString("updatedBy");
            int fromImage = parseCloudFridgeItem.getInt("fromImage");
            byte[] image = parseCloudFridgeItem.getBytes("image");
            byte[] imageBinarized = parseCloudFridgeItem.getBytes("imageBinarized");
            int dismissed = parseCloudFridgeItem.getInt("dismissed");
            int expired = parseCloudFridgeItem.getInt("expired");
            int editedCart = parseCloudFridgeItem.getInt("editedCart");
            int editedFridge = parseCloudFridgeItem.getInt("editedFridge");
            int deletedCart = parseCloudFridgeItem.getInt("deletedCart");
            int deletedFridge = parseCloudFridgeItem.getInt("deletedFridge");
            int notifiedPush = parseCloudFridgeItem.getInt("notifiedPush");
            int notifiedEmail = parseCloudFridgeItem.getInt("notifiedEmail");

            cloudFridge.add(new FridgeItem(rowId, hash, foodItem, rawFoodItem, expiryDate, createdDate, updatedDate, updatedBy,
                    fromImage, image, imageBinarized, expired, editedCart, editedFridge, deletedCart,
                    deletedFridge, notifiedPush, notifiedEmail));
        }

        return cloudFridge;
    }

    private static List<FridgeItem> syncFridges(List<FridgeItem> local, List<FridgeItem> cloud) {
        List<FridgeItem> syncedFridge = new ArrayList<FridgeItem>();
        int m = local.size();
        int i = 0;
        int n = cloud.size();
        int j = 0;

        return null;
    }
    */

    private static List<FridgeItem> getLocalFridge(Context context) {
        List<FridgeItem> localFridge = new ArrayList<FridgeItem>();
        Cursor c = new FridgeDbHelper(context).read(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE + " ASC");
        c.moveToFirst();

        while (!c.isAfterLast()) {
            long rowId = c.getLong(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable._ID)
            );
            String hash = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_HASH)
            );
            String foodItem = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM)
            );
            String rawFoodItem = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM)
            );
            String expiryDate = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE)
            );
            String createdDate = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_CREATED_DATE)
            );
            String updatedDate = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE)
            );
            String updatedBy = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY)
            );
            int fromImage = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE)
            );
            byte[] image = c.getBlob(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE)
            );
            byte[] imageBinarized = c.getBlob(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED)
            );
            int dismissed = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED)
            );
            int expired = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED)
            );
            int editedCart = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART)
            );
            int editedFridge = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE)
            );
            int deletedCart = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART)
            );
            int deletedFridge = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE)
            );
            int notifiedPush = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH)
            );
            int notifiedEmail = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL)
            );

            localFridge.add(new FridgeItem(rowId, hash, foodItem, rawFoodItem, expiryDate, createdDate, updatedDate, updatedBy,
                    fromImage, image, imageBinarized, dismissed, expired, editedCart, editedFridge, deletedCart,
                    deletedFridge, notifiedPush, notifiedEmail));

            c.moveToNext();
        }

        return localFridge;
    }

    private static HashMap<String, ParseObject> getParseCloudFridgeHash(List<ParseObject> parseCloudFridge) {
        HashMap<String, ParseObject> parseCloudFridgeHash = new HashMap<String, ParseObject>();
        int n = parseCloudFridge.size();

        for (int i = 0; i < n; i++) {
            ParseObject parseCloudItem = parseCloudFridge.get(i);
            String hash = parseCloudItem.getString("hash");

            parseCloudFridgeHash.put(hash, parseCloudItem);
        }

        return parseCloudFridgeHash;
    }

    private static void updateCloud(final Context context, List<FridgeItem> localFridge, HashMap<String, ParseObject> parseCloudFridgeHash) {
        int n = localFridge.size();

        SaveCallback saveCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(context, "Syncing complete.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        for (int i = 0; i < n; i++) {
            FridgeItem localFridgeItem = localFridge.get(i);
            ParseObject parseCloudItem = parseCloudFridgeHash.get(localFridgeItem.getHash());
            boolean updated = false;

            if (parseCloudItem == null) {

                ParseObject newParseCloudItem = new ParseObject(getUserParseClass());

                // Non-changable data
                newParseCloudItem.put("rowId", localFridgeItem.getRowId());
                newParseCloudItem.put("hash", localFridgeItem.getHash());
                newParseCloudItem.put("rawFoodItem", localFridgeItem.getRawFoodItem());
                newParseCloudItem.put("createdDate", localFridgeItem.getCreatedDate());
                newParseCloudItem.put("fromImage", localFridgeItem.isFromImage());

                byte[] image = localFridgeItem.getImage();
                byte[] imageBinarized = localFridgeItem.getImageBinarized();
                if (image != null) {
                    newParseCloudItem.put("image", new ParseFile("image.jpg", image));
                }
                if (imageBinarized != null) {
                    newParseCloudItem.put("imageBinarized", new ParseFile("imageBinarized.jpg", imageBinarized));
                }

                newParseCloudItem.put("foodItem", localFridgeItem.getFoodItem());
                newParseCloudItem.put("expiryDate", localFridgeItem.getExpiryDate());
                newParseCloudItem.put("updatedDate", localFridgeItem.getUpdatedDate());
                newParseCloudItem.put("updatedBy", localFridgeItem.getUpdatedBy());
                newParseCloudItem.put("dismissed", localFridgeItem.isDismissed());
                newParseCloudItem.put("expired", localFridgeItem.isExpired());
                newParseCloudItem.put("editedCart", localFridgeItem.isEditedCart());
                newParseCloudItem.put("editedFridge", localFridgeItem.isEditedFridge());
                newParseCloudItem.put("deletedCart", localFridgeItem.isDeletedCart());
                newParseCloudItem.put("deletedFridge", localFridgeItem.isDeletedFridge());
                newParseCloudItem.put("notifiedPush", localFridgeItem.isNotifiedPush());
                newParseCloudItem.put("notifiedEmail", localFridgeItem.isNotifiedEmail());

                newParseCloudItem.saveEventually();

            } else if (!localFridgeItem.getUpdatedDate().equals(parseCloudItem.getString("updatedDate"))) {

                parseCloudItem.put("foodItem", localFridgeItem.getFoodItem());
                parseCloudItem.put("expiryDate", localFridgeItem.getExpiryDate());
                parseCloudItem.put("updatedDate", localFridgeItem.getUpdatedDate());
                parseCloudItem.put("updatedBy", localFridgeItem.getUpdatedBy());
                parseCloudItem.put("dismissed", localFridgeItem.isDismissed());
                parseCloudItem.put("expired", localFridgeItem.isExpired());
                parseCloudItem.put("editedCart", localFridgeItem.isEditedCart());
                parseCloudItem.put("editedFridge", localFridgeItem.isEditedFridge());
                parseCloudItem.put("deletedCart", localFridgeItem.isDeletedCart());
                parseCloudItem.put("deletedFridge", localFridgeItem.isDeletedFridge());
                parseCloudItem.put("notifiedPush", localFridgeItem.isNotifiedPush());
                parseCloudItem.put("notifiedEmail", localFridgeItem.isNotifiedEmail());

                parseCloudItem.saveEventually();

            }

        }

        // Still need a callback on the last item to display completion
    }

    public static void saveFridgeToCloud(final Context context) {

        if (networkConnectionType(context) != -1) {
            isUserClassSet(new MyCallbackInterface() {
                @Override
                public void success(Object result) {

                    fallback("Syncing...");

                    ParseQuery<ParseObject> query = ParseQuery.getQuery(getUserParseClass());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> fridgeList, ParseException e) {
                            if (e == null) {
                                //syncFridges(getLocalFridge(context), getCloudFridge(fridgeList));
                                updateCloud(context, getLocalFridge(context), getParseCloudFridgeHash(fridgeList));
                            } else {
                                fallback(e.getMessage());
                            }
                        }
                    });
                }

                @Override
                public void error() {

                    ParseObject userClass = new ParseObject(getUserParseClass());

                    userClass.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseObject user = new ParseObject("Users");

                                user.put("installationObjectId", getInstallation().getObjectId());
                                user.put("installationId", getInstallationId());

                                user.saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            success(null);
                                        } else {
                                            fallback(e.getMessage());
                                        }
                                    }
                                });

                            } else {
                                fallback(e.getMessage());
                            }
                        }
                    });

                }

                @Override
                public void fallback(String message) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private static void isUserClassSet(final MyCallbackInterface myCallback) {
        final String installationId = getInstallationId();

        ParseQuery query = ParseQuery.getQuery("Users");
        query.whereEqualTo("installationId", installationId);
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject user, ParseException e) {
                if (e == null) { // User class has been set
                    myCallback.success(user);
                } else if (e.getCode() == PE_ObjectNotFound) { // User class has not been set
                    myCallback.error();
                } else {
                    myCallback.fallback(e.getMessage());
                }
            }
        });

    }
}
