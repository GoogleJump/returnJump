package com.returnjump.spoilfoil;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * Created by Kelsey on 5/31/2014.
 */
public class EditNameFragment extends DialogFragment {
    protected OnEditNameButtonClickedListener editNameButtonClickedListener;
    protected EditText editText;
    protected ImageButton button;

    public EditNameFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_Holo_Light_Dialog);
        if(savedInstanceState==null) {
            savedInstanceState = getArguments();
        }
    }

    //implement onSaveInstanceState(Bundle savedInstanceState)  and onRestoreInstanceState(Bundle savedInstanceState)
    //to restore the previous state    or   onResume?

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.edit_name_fragment, container, false);
        editText = (EditText) view.findViewById(R.id.edit_text_dialog);
        if(!getArguments().getBoolean("isNewItem")){
            editText.setText(getArguments().getString("name"));
        }
        editText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.otf"));
                button = (ImageButton) view.findViewById(R.id.submit_new_item_button_dialog);

        //TODO set onKeyListener for enter

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!editText.getText().equals("")) {
                    getArguments().putString("name", editText.getText().toString());
                    editNameButtonClickedListener.onEditNameButtonClicked(getArguments().getBoolean("isNewItem"));
                }
            }
        });
        editText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return view;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            editNameButtonClickedListener = (OnEditNameButtonClickedListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public interface OnEditNameButtonClickedListener{
        public void onEditNameButtonClicked(Boolean isNewItem);
    }
}





