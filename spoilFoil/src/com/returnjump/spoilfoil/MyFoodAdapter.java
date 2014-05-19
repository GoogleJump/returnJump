package com.returnjump.spoilfoil;

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyFoodAdapter extends ArrayAdapter<FoodItem> {
      private Context context;
      private int layout;
      private List<FoodItem> foods;
      
      public MyFoodAdapter(Context context, int layout, List<FoodItem> foods) {
          super(context, layout, foods);
          this.context = context;
          this.layout = layout;
          this.foods = foods;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
          // Make sure we have a view to work with (may have been given null)
          View itemView = convertView;
          if (itemView == null) {
              itemView = LayoutInflater.from(context).inflate(layout, parent, false);
          }
          
          FoodItem currentFood = foods.get(position);
          
          TextView foodItemName = (TextView) itemView.findViewById(R.id.food_item_name);
          foodItemName.setText(currentFood.getFoodName());

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

              TextView expirationUnit = (TextView) itemView.findViewById(R.id.expiration_unit);
              expirationUnit.setText(currentFood.getExpirationUnit());
          } else {
              expirationMessage.setLayoutParams(paramShow);
              expirationTime.setLayoutParams(paramHide);
          }
          
          // Set the tags for hidden data
          itemView.setTag(R.id.food_item_id, currentFood.getId());

          return itemView;
     }               
}