package com.returnjump.spoilfoil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

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
          foodItemName.setText(currentFood.getFoodItemName());
          
          TextView expirationNumber = (TextView) itemView.findViewById(R.id.expiration_number);
          expirationNumber.setText(Integer.toString(currentFood.getExpirationNumber()));
          
          TextView expirationUnit = (TextView) itemView.findViewById(R.id.expiration_unit);
          expirationUnit.setText(currentFood.getExpirationUnit());
          
          // Set the tags for hidden data
          itemView.setTag(R.id.food_item_id, currentFood.getId());

          /*
          final int pos = position;
          EditText foodItemEdit = (EditText) itemView.findViewById(R.id.newItemEditTextSwipe);
          foodItemEdit.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Toast.makeText(context, Integer.toString(pos), Toast.LENGTH_LONG).show();
              }
          });

          TextView daysGoodEdit = (TextView) itemView.findViewById(R.id.daysGoodTextViewSwipe);
          daysGoodEdit.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Toast.makeText(context, Integer.toString(pos), Toast.LENGTH_LONG).show();
              }
          });

          ImageButton updateItem = (ImageButton) itemView.findViewById(R.id.submitNewItemButtonSwipe);
          updateItem.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Toast.makeText(context, Integer.toString(pos), Toast.LENGTH_LONG).show();
              }
          });
          */

          return itemView;
     }               
}