package com.returnjump.frij;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MyFridgeAdapter extends ArrayAdapter<FridgeItem> {
      private Context context;
      private int layout;
      private List<FridgeItem> fridgeItems;
      
      public MyFridgeAdapter(Context context, int layout, List<FridgeItem> fridgeItems) {
          super(context, layout, fridgeItems);
          this.context = context;
          this.layout = layout;
          this.fridgeItems = fridgeItems;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
          // Make sure we have a view to work with (may have been given null)
          View itemView = convertView;
          if (itemView == null) {
              itemView = LayoutInflater.from(context).inflate(layout, parent, false);
          }
          
          FridgeItem currentFood = fridgeItems.get(position);
          
          TextView foodItemName = (TextView) itemView.findViewById(R.id.food_item_name);
          foodItemName.setText(currentFood.getName());
          //foodItemName.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.otf"));

          // Set up which linear layout will show since only one of the two should be
          // visible at any given time
          int expirationNum = currentFood.getExpirationNumber();
          LinearLayout expirationMessage = (LinearLayout) itemView.findViewById(R.id.expiration_message);
          LinearLayout expirationTime = (LinearLayout) itemView.findViewById(R.id.expiration_time);

          LinearLayout.LayoutParams paramShow = new LinearLayout.LayoutParams(
                  0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
          LinearLayout.LayoutParams paramHide = new LinearLayout.LayoutParams(
                  0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);

          if (expirationNum > 0) {
              expirationMessage.setLayoutParams(paramHide);
              expirationTime.setLayoutParams(paramShow);

              TextView expirationNumber = (TextView) itemView.findViewById(R.id.expiration_number);
              expirationNumber.setText(Integer.toString(expirationNum));
              //expirationNumber.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.otf"));

              TextView expirationUnit = (TextView) itemView.findViewById(R.id.expiration_unit);
              expirationUnit.setText(currentFood.getExpirationUnit());
              //expirationUnit.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.otf"));
          } else {
              expirationMessage.setLayoutParams(paramShow);
              expirationTime.setLayoutParams(paramHide);
          }
          
          // Set the tags for hidden data
          itemView.setTag(R.id.food_item_id, currentFood.getRowId());

          return itemView;
     }               
}