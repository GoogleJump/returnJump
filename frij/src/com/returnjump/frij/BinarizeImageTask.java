package com.returnjump.frij;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;

public class BinarizeImageTask extends AsyncTask<String, Void, Bitmap> {

    private Context context;
    private Activity activity;

    public BinarizeImageTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    protected void onPreExecute () {
        Toast.makeText(context, "Binarizing image.", Toast.LENGTH_SHORT).show();
    }

    protected Bitmap doInBackground(String... PATH) {
        // Scale image to reduce memory consumption
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // Reduces the image resolution to half

        Bitmap bitmap = BitmapFactory.decodeFile(PATH[0], options);

        bitmap = ShoppingCartActivity.fixImageOrientation(bitmap, PATH[0]);

        // Leptonica binarization
        Pix pix = ReadFile.readBitmap(bitmap);
        pix = Binarize.otsuAdaptiveThreshold(pix, 32, 32, 2, 2, 0.9F);
        //pix = Binarize.otsuAdaptiveThreshold(pix);
        bitmap = WriteFile.writeBitmap(pix);
        //remove dust here
        return bitmap;
    }

    protected void onPostExecute(Bitmap bitmap) {
        // Display the binarized image in an alert dialog
        ImageView binaryImageView = new ImageView(context);
        binaryImageView.setImageBitmap(bitmap);

        new AlertDialog.Builder(activity)
                .setTitle("Binarized Image:")
                .setView(binaryImageView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

        new OcrImageTask(true, context, activity).execute(bitmap);
    }

}
