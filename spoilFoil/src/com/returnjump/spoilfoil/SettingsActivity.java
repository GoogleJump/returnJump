package com.returnjump.spoilfoil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends PreferenceActivity {
    final public static String PREF_CHECKBOX_PUSH = "checkbox_push";
    final public static String PREF_CHECKBOX_EMAIL = "checkbox_email";
    final public static String PREF_EMAIL_ADDRESS = "email_address";
    final public static String PREF_SYNC = "preference_sync";


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

            notifyEmailInitValue();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            notifyEmailInitValue();
            bindPreferenceToSummary();

            // Set preference listeners
            findPreference(PREF_EMAIL_ADDRESS).setOnPreferenceChangeListener(emailPrefChangeListener);
            findPreference(PREF_SYNC).setOnPreferenceClickListener(syncPrefClickListener);
        }

        private void notifyEmailInitValue() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String emailAddress = sharedPreferences.getString(PREF_EMAIL_ADDRESS, "").trim();
            Preference notifyEmail = findPreference(PREF_CHECKBOX_EMAIL);

            if (emailAddress.equals("")) {
                sharedPreferences.edit().putBoolean(PREF_CHECKBOX_EMAIL, false).commit();
            }
        }

        private void bindPreferenceToSummary() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            setPrefSummary(sharedPref, PREF_EMAIL_ADDRESS, "");
        }

        private void setPrefSummary(SharedPreferences sharedPref, String key, String deflt) {
            findPreference(key).setSummary(sharedPref.getString(key, deflt));
        }

        private static boolean isValidEmail(String email) {
            final String EMAIL_REGEX =
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

            Pattern pattern = Pattern.compile(EMAIL_REGEX);
            Matcher matcher = pattern.matcher(email);

            return matcher.matches();
        }

        public static Preference.OnPreferenceChangeListener emailPrefChangeListener = new Preference.OnPreferenceChangeListener() {

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

        public static Preference.OnPreferenceClickListener syncPrefClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Context context = preference.getContext();

                Toast.makeText(context, "Syncing...", Toast.LENGTH_LONG).show();

                MyParse.savePreferenceEventually(context);

                return true;
            }
        };

    }

}