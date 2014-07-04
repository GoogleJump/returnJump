package com.returnjump.phrige;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;

public class MyParse {
    final public static int PE_ObjectNotFound = 101;

    public static interface MyCallbackInterface {

        public void success(Object result);
        public void error(); // Too many duplicates, make a default method for this
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
        }, context);

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

    private static void saveNewFridgeItemEventually(FridgeItem fridgeItem) {
        ParseObject parseObject = new ParseObject(getUserParseClass());

        // This data should never be modified
        parseObject.put("rowId", fridgeItem.getRowId());
        parseObject.put("hash", fridgeItem.getHash());
        parseObject.put("rawFoodItem", fridgeItem.getRawName());
        parseObject.put("createdDate", fridgeItem.getCreatedDate());
        parseObject.put("fromImage", fridgeItem.isFromImage());

        byte[] image = fridgeItem.getImage();
        byte[] imageBinarized = fridgeItem.getImageBinarized();
        if (image != null) {
            ParseFile pfImage = new ParseFile("image.jpg", image);
            pfImage.saveInBackground(); // Need a save eventually
            parseObject.put("image", pfImage);
        }
        if (imageBinarized != null) {
            ParseFile pfImageBinarized = new ParseFile("imageBinarized.jpg", imageBinarized);
            pfImageBinarized.saveInBackground();
            parseObject.put("imageBinarized", pfImageBinarized);
        }

        saveUpdatedFridgeItemEventually(fridgeItem, parseObject);
        
    }

    private static void saveUpdatedFridgeItemEventually(FridgeItem fridgeItem, ParseObject parseObject) {

        // Modifiable data
        parseObject.put("foodItem", fridgeItem.getName());
        parseObject.put("expiryDate", fridgeItem.getExpiryDate());
        parseObject.put("updatedDate", fridgeItem.getUpdatedDate());
        parseObject.put("updatedBy", fridgeItem.getUpdatedBy());
        parseObject.put("dismissed", fridgeItem.isDismissed());
        parseObject.put("expired", fridgeItem.isExpired());
        parseObject.put("editedCart", fridgeItem.isEditedCart());
        parseObject.put("editedFridge", fridgeItem.isEditedFridge());
        parseObject.put("deletedCart", fridgeItem.isDeletedCart());
        parseObject.put("deletedFridge", fridgeItem.isDeletedFridge());
        parseObject.put("notifiedPush", fridgeItem.isNotifiedPush());
        parseObject.put("notifiedEmail", fridgeItem.isNotifiedEmail());

        parseObject.saveEventually();

    }

    public static void saveFridgeItemEventually(final FridgeItem fridgeItem, final boolean isNew, final Context context) {
        MyParse.isUserClassSet(new MyParse.MyCallbackInterface() {
            @Override
            public void success(Object result) {

                if (isNew) {
                    saveNewFridgeItemEventually(fridgeItem);
                } else {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(MyParse.getUserParseClass());
                    query.whereEqualTo("hash", fridgeItem.getHash());
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                saveUpdatedFridgeItemEventually(fridgeItem, parseObject);
                            } else {
                                fallback(e.getMessage());
                            }
                        }
                    });
                }

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
        }, context);
    }

    // TODO: Still need a callback on the last item to display completion
    private static void backupToCloud(final Context context, HashMap<String, ParseObject> parseCloudFridgeHash) {

        List<FridgeItem> localFridge = new ArrayList<FridgeItem>();
        Cursor c = new FridgeDbHelper(context).read(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE + " ASC");
        c.moveToFirst();

        while (!c.isAfterLast()) {
            FridgeItem localFridgeItem = FridgeDbHelper.cursorToFridgeItem(c, false);

            ParseObject parseCloudItem = parseCloudFridgeHash.get(localFridgeItem.getHash());
            boolean updated = false;

            if (parseCloudItem == null) { // This is a new item

                saveNewFridgeItemEventually(localFridgeItem);

            } else if (!localFridgeItem.getUpdatedDate().equals(parseCloudItem.getString("updatedDate"))) { // This item is just being updated

                saveUpdatedFridgeItemEventually(localFridgeItem, parseCloudItem);

            }

            c.moveToNext();
        }

    }

    public static void saveFridgeToCloud(final Context context) {

        isUserClassSet(new MyCallbackInterface() {
            @Override
            public void success(Object result) {

                fallback("Syncing...");

                ParseQuery<ParseObject> query = ParseQuery.getQuery(getUserParseClass());
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> fridgeList, ParseException e) {
                        if (e == null) {
                            //syncFridges(getLocalFridge(context), getCloudFridge(fridgeList));
                            backupToCloud(context, getParseCloudFridgeHash(fridgeList));
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
        }, context);

    }

    public static void isUserClassSet(final MyCallbackInterface myCallback, Context context) {
        final String installationId = getInstallationId();

        if (networkConnectionType(context) != -1) {
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
}