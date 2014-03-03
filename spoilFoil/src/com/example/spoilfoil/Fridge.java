package com.example.spoilfoil;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Fridge extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//first push
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fridge, menu);
        return true;
    }
    
}
