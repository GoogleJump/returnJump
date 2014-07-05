package com.returnjump.phrije;

import android.app.Application;
import android.view.View;
import android.widget.ListView;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Parse App ID and Client Key can be found in phrije/res/values/secret.xml
	    MyParse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        MyParse.saveInstallationEventually(this);

	}

    /*
        Add any global methods below
     */

    public static View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
