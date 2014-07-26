package com.returnjump.frij;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignInActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Check to see if the user clicked "don't show this [sign in] again"
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean dontShowSignIn = sharedPref.getBoolean(SettingsActivity.PREF_DONT_SHOW_SIGN_IN, SettingsActivity.PREF_DONT_SHOW_SIGN_IN_DEFAULT);

        if (dontShowSignIn) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        TextView textViewSkip = (TextView) findViewById(R.id.skip);
        TextView textViewDontShowThisAgain = (TextView) findViewById(R.id.dont_show_this_again);

        textViewSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textViewDontShowThisAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the preference so that it won't show next time
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPref.edit().putBoolean(SettingsActivity.PREF_DONT_SHOW_SIGN_IN, true).commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}