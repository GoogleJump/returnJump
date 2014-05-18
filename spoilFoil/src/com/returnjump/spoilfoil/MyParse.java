package com.returnjump.spoilfoil;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.List;

public class MyParse {
    final public static int PE_ObjectNotFound = 101;

    private static interface MyCallbackInterface {

        public void success(Object result);
        public void error();

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

    public static void saveInstallationEventually(Context context) {
        ParseInstallation.getCurrentInstallation().saveEventually();
    }

    public static void savePreferenceEventually(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String emailAddress = sharedPref.getString(SettingsActivity.PREF_EMAIL_ADDRESS, "");
        final boolean notifyPush = sharedPref.getBoolean(SettingsActivity.PREF_CHECKBOX_PUSH, true);
        final boolean notifyEmail = sharedPref.getBoolean(SettingsActivity.PREF_CHECKBOX_EMAIL, false);

        isUserClassSet(new MyCallbackInterface() {

            @Override
            public void success(Object result) {
                ParseObject user = (ParseObject) result;

                user.put("email", emailAddress);
                user.put("notifyPush", notifyPush);
                user.put("notifyEmail", notifyEmail);

                user.saveEventually();
            }

            @Override
            public void error() {
                ParseObject user = new ParseObject("Users");

                user.put("installationObjectId", getInstallation().getObjectId());
                user.put("installationId", getInstallationId());
                user.put("email", emailAddress);
                user.put("notifyPush", notifyPush);
                user.put("notifyEmail", notifyEmail);

                user.saveEventually();
            }
        });

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
                }
            }
        });

    }
}
