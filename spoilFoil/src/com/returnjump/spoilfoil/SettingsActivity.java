package com.returnjump.spoilfoil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            bindPreferenceToSummary();
            findPreference("email_address").setOnPreferenceChangeListener(prefChangeListener);
        }

        private void bindPreferenceToSummary() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            setPrefSummary(sharedPref, "email_address", "");
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

        public static Preference.OnPreferenceChangeListener prefChangeListener = new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                Context context = preference.getContext();
                String key = preference.getKey();

                if (key.equals("email_address")) {
                    String email = value.toString().trim();

                    // Validate email
                    if (isValidEmail(email) || email.equals("")) {
                        preference.setSummary(email);

                        return true;
                    } else {
                        Toast.makeText(context, "Enter a valid email address.", Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else {
                    return true;
                }

            }

        };

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            if (key.equals("email_address")) {
                pref.setSummary(sharedPreferences.getString(key, ""));
            }
        }
    }

}