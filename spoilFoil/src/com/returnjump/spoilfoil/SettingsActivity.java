package com.returnjump.spoilfoil;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends PreferenceActivity {
    final public static String PREF_TIME = "preference_time";
    final public static String PREF_CHECKBOX_PUSH = "checkbox_push";
    final public static String PREF_CHECKBOX_EMAIL = "checkbox_email";
    final public static String PREF_EMAIL_ADDRESS = "email_address";
    final public static String PREF_CHECKBOX_AUTO = "checkbox_auto";
    final public static String PREF_SYNC = "preference_sync";

    final public static String PREF_TIME_DEFAULT = "08:00";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
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
            MyParse.savePreferenceToCloud(getActivity());
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            initNotifyEmailValue();
            bindPreferenceToSummary();

            // Set preference listeners
            findPreference(PREF_EMAIL_ADDRESS).setOnPreferenceChangeListener(emailPrefChangeListener);
            findPreference(PREF_SYNC).setOnPreferenceClickListener(syncPrefClickListener);
            findPreference(PREF_TIME).setOnPreferenceClickListener(timePrefClickListener);
        }

        private void initNotifyEmailValue() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String emailAddress = sharedPreferences.getString(PREF_EMAIL_ADDRESS, "").trim();
            Preference notifyEmail = findPreference(PREF_CHECKBOX_EMAIL);

            if (emailAddress.equals("")) {
                sharedPreferences.edit().putBoolean(PREF_CHECKBOX_EMAIL, false).commit();
            }
        }

        private void bindPreferenceToSummary() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            findPreference(PREF_EMAIL_ADDRESS).setSummary(sharedPref.getString(PREF_EMAIL_ADDRESS, ""));
            findPreference(PREF_TIME).setSummary(prefTimePrettyPrint(sharedPref.getString(PREF_TIME, PREF_TIME_DEFAULT)));
        }

        private static boolean isValidEmail(String email) {
            final String EMAIL_REGEX =
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

            Pattern pattern = Pattern.compile(EMAIL_REGEX);
            Matcher matcher = pattern.matcher(email);

            return matcher.matches();
        }

        public Preference.OnPreferenceChangeListener emailPrefChangeListener = new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                Context context = preference.getContext();
                String key = preference.getKey();

                if (key.equals(PREF_EMAIL_ADDRESS)) {
                    String email = value.toString().trim();

                    // Validate email
                    if (isValidEmail(email) || email.equals("")) {
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

        public Preference.OnPreferenceClickListener syncPrefClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Context context = preference.getContext();

                MyParse.saveFridgeToCloud(context);

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

        public static String hourMinToPrefTime(int hour, int minute) {
            return String.format("%02d:%02d", hour, minute);
        }

        private static int getHourFromPrefTime(String time) {
            return Integer.parseInt(time.substring(0, 2));
        }

        private static int getMinFromPrefTime(String time) {
            return Integer.parseInt(time.substring(3, 5));
        }

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
            }
        }

    }

}