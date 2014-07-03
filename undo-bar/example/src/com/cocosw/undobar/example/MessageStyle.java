package com.cocosw.undobar.example;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.cocosw.undobar.UndoBarController;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class MessageStyle extends Activity {

    private EditText mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
            if (id != 0 && getResources().getBoolean(id)) { // Translucent available
                Window w = getWindow();
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                //   w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            findViewById(R.id.login_form).setPadding(0, 100, 0, 0);
        }
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_message_style);

        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText("Geek");
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setVisibility(View.GONE);
        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        attemptLogin();
                    }
                }
        );
    }

    private void attemptLogin() {
        UndoBarController.show(this, "Hello " + mEmailView.getText());
    }

}
