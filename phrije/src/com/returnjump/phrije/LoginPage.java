package com.returnjump.phrije;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;


/**
 * Created by arturomenacruz on 2014-07-18.
 */
public class LoginPage extends Activity {
    AccountManager mAccountManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, 1);
    }
    ArrayList<String> gUsernameList = new ArrayList<String>();
    AccountManager accountManager = AccountManager.get(this);
    Account[] accounts = accountManager.getAccountsByType("com.google");

    gUsernameList.clear();
//loop
    for (Account account : accounts) {
        gUsernameList.add(account.name);
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Choose you gmail-account");

    ListView lv = new ListView(this);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, gUsernameList);
    lv.setAdapter(adapter);
    lv.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView <?> parent, View view,int position,long id) {
            Log.d("You-select-gmail-account", gUsernameList.get(position)) );
        }
    });
    builder.setView(lv);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
        }
    });
    final Dialog dialog = builder.create();
    dialog.show();


}
