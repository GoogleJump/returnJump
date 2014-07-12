package com.returnjump.phrije;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Patterns;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends PreferenceActivity {
    final public static String DB_VERSION = "db_version";
    final public static String ALARM_SET = "alarm_set";
    final public static String PREF_TIME = "preference_time";
    final public static String PREF_CHECKBOX_PUSH = "checkbox_push";
    final public static String PREF_CHECKBOX_EMAIL = "checkbox_email";
    final public static String PREF_EMAIL_ADDRESS = "email_address";
    final public static String PREF_CHECKBOX_AUTO = "checkbox_auto";
    final public static String PREF_SYNC = "preference_sync";

    final public static int DB_VERSION_DEFAULT = 1;
    final public static boolean ALARM_SET_DEFAULT = false;
    final public static String PREF_TIME_DEFAULT = "08:00";
    final public static boolean PREF_CHECKBOX_PUSH_DEFAULT = true;
    final public static boolean PREF_CHECKBOX_EMAIL_DEFAULT = false;
    final public static String PREF_EMAIL_ADDRESS_DEFAULT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static boolean isUserNotificationEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean pushPref = sharedPreferences.getBoolean(PREF_CHECKBOX_PUSH, PREF_CHECKBOX_PUSH_DEFAULT);
        boolean emailPref = sharedPreferences.getBoolean(PREF_CHECKBOX_EMAIL, PREF_CHECKBOX_EMAIL_DEFAULT);

        return pushPref || emailPref;
    }

    public static boolean getAlarmSet(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPref.getBoolean(ALARM_SET, ALARM_SET_DEFAULT);
    }

    private static void setAlarmSet(boolean set, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(ALARM_SET, set).commit();
    }

    public static void initializeAlarm(Context context){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Get the hour and minute set by the user's preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefTime = sharedPreferences.getString(PREF_TIME, PREF_TIME_DEFAULT);
        int hour = getHourFromPrefTime(prefTime);
        int minute = getMinFromPrefTime(prefTime);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        //Creates intent that will be called when alarm goes off
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        //BootReceiver.enable(context);

        // Set flag so that alarm gets initialized once in the main activity
        if (!getAlarmSet(context)) {
            setAlarmSet(true, context);
        }

    }

    // Example 15, 30 -> 15:30 | 8, 5 -> 08:05
    public static String hourMinToPrefTime(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }

    // Example: 15:30 -> 15
    public static int getHourFromPrefTime(String time) {
        return Integer.parseInt(time.substring(0, 2));
    }

    // Example: 15:30 -> 30
    public static int getMinFromPrefTime(String time) {
        return Integer.parseInt(time.substring(3, 5));
    }

    // Example: 15:30 -> 3:30 PM
    private static String prefTimePrettyPrint(String time) {
        int hour = getHourFromPrefTime(time);
        int minute = getMinFromPrefTime(time);
        String amPm = "";

        if (hour > 11) {
            amPm = "PM";
        } else {
            amPm = "AM";
        }

        if (hour % 12 == 0) {
            hour = 12;
        } else {
            hour %= 12;
        }

        return String.format("%d:%02d %s", hour, minute, amPm);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            initNotifyEmailValue();
            MyParse.savePreferenceToCloud(getActivity(), false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            initNotifyEmailValue();
            bindPreferenceToSummary();

            // Set preference listeners
            findPreference(PREF_CHECKBOX_PUSH).setOnPreferenceChangeListener(notificationPrefChangeListener);
            findPreference(PREF_CHECKBOX_EMAIL).setOnPreferenceChangeListener(notificationPrefChangeListener);
            findPreference(PREF_EMAIL_ADDRESS).setOnPreferenceChangeListener(emailAddressPrefChangeListener);
            findPreference(PREF_SYNC).setOnPreferenceClickListener(syncPrefClickListener);
            findPreference(PREF_TIME).setOnPreferenceClickListener(timePrefClickListener);
        }

        private void initNotifyEmailValue() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String emailAddress = sharedPreferences.getString(PREF_EMAIL_ADDRESS, PREF_EMAIL_ADDRESS_DEFAULT).trim();
            Preference notifyEmail = findPreference(PREF_CHECKBOX_EMAIL);

            if (emailAddress.equals(PREF_EMAIL_ADDRESS_DEFAULT)) {
                sharedPreferences.edit().putBoolean(PREF_CHECKBOX_EMAIL, false).commit();
            }
        }

        private void bindPreferenceToSummary() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            findPreference(PREF_EMAIL_ADDRESS).setSummary(sharedPref.getString(PREF_EMAIL_ADDRESS, PREF_EMAIL_ADDRESS_DEFAULT));
            findPreference(PREF_TIME).setSummary(prefTimePrettyPrint(sharedPref.getString(PREF_TIME, PREF_TIME_DEFAULT)));
        }

        private static boolean isValidEmail(String email) {
            Pattern pattern = Patterns.EMAIL_ADDRESS;

            return pattern.matcher(email).matches();
        }

        public Preference.OnPreferenceChangeListener emailAddressPrefChangeListener = new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                Context context = preference.getContext();
                String key = preference.getKey();

                if (key.equals(PREF_EMAIL_ADDRESS)) {
                    String email = value.toString().trim();

                    // Validate email
                    if (isValidEmail(email)) {
                        // Set the preference to the trimmed email
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPreferences.edit().putString(PREF_EMAIL_ADDRESS, email).commit();
                        ((EditTextPreference) preference).setText(email);

                        preference.setSummary(email);
                    } else {
                        Toast.makeText(context, "Enter a valid email address.", Toast.LENGTH_LONG).show();
                    }

                    return false;
                } else {
                    return true;
                }

            }

        };

        public Preference.OnPreferenceChangeListener notificationPrefChangeListener = new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                Context context = preference.getContext();
                String key = preference.getKey();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                boolean newPushPref = sharedPreferences.getBoolean(PREF_CHECKBOX_PUSH, PREF_CHECKBOX_PUSH_DEFAULT);
                boolean newEmailPref = sharedPreferences.getBoolean(PREF_CHECKBOX_EMAIL, PREF_CHECKBOX_EMAIL_DEFAULT);

                if (key.equals(PREF_CHECKBOX_PUSH)) {
                    newPushPref = (Boolean) value;
                } else if (key.equals(PREF_CHECKBOX_EMAIL)) {
                    newEmailPref = (Boolean) value;
                }

                if (newPushPref || newEmailPref) {
                    // If at least one notification is on, set the alarm
                    initializeAlarm(context);
                } else { // User does not want notifications, cancel the alarm
                    Intent intent = new Intent(context, NotificationReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    alarmMgr.cancel(alarmIntent);
                    //BootReceiver.disable(context);
                }

                return true;

            }

        };

        public Preference.OnPreferenceClickListener syncPrefClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();

            MyParse.saveFridgeToCloud(context, true);

            return true;
            }
        };

        public Preference.OnPreferenceClickListener timePrefClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();

            DialogFragment newFragment = TimePickerFragment.newInstance(preference);
            newFragment.show(getFragmentManager(), "timePicker");

            return true;
            }
        };

    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private static Preference preference;

        public static TimePickerFragment newInstance(Preference pref) {
            preference = pref;

            return new TimePickerFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the preference time as the default values for the picker
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String prefTime = sharedPreferences.getString(PREF_TIME, PREF_TIME_DEFAULT);

            int hour = getHourFromPrefTime(prefTime);
            int minute = getMinFromPrefTime(prefTime);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            String newPrefTime = hourMinToPrefTime(hourOfDay, minute);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.edit().putString(PREF_TIME, newPrefTime).commit();

            preference.setSummary(prefTimePrettyPrint(newPrefTime));

            // Update notification alarm
            if (isUserNotificationEnabled(getActivity())) {
                initializeAlarm(getActivity());
            }
        }
    }

}