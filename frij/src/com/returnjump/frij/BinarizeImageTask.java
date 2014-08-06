package com.returnjump.frij;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;
import java.awt.Point;
import java.util.LinkedList;

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
        dustOff(bitmap);
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

    protected int numBlackNeighbors(Bitmap b, int x, int y) {
        int count = 0;
        if(x+1 < b.getWidth())
            count += b.getPixel(x+1, y) == Color.BLACK ? 1 : 0;
        if(y+1 < b.getHeight())
            count += b.getPixel(x, y+1) == Color.BLACK ? 1 : 0;
        if(x-1 > -1)
            count += b.getPixel(x-1, y) == Color.BLACK ? 1 : 0;
        if(y-1 > -1)
            count += b.getPixel(x, y-1) == Color.BLACK ? 1 : 0;
        return count;
    }
    protected Point getBlackNeighbor(Bitmap b, int x, int y) {
        if(x+1 < b.getWidth() && b.getPixel(x+1, y) == Color.BLACK)
            return new Point(x+1, y);
        if(y+1 < b.getWidth() && b.getPixel(x, y+1) == Color.BLACK)
            return new Point(x, y+1);
        if(x-1 > -1 && b.getPixel(x-1, y) == Color.BLACK)
            return new Point(x-1, y);
        if(y-1 < -1 && b.getPixel(x, y-1) == Color.BLACK)
            return new Point(x, y-1);
        return null;
    }

    protected void dustOff(Bitmap b) {
        LinkedList<Point> blackPixels = new LinkedList<Point>();
        for(int x = 0; x < b.getWidth(); x++) {
            for(int y = 0; y < b.getHeight(); y++) {
                if(b.getPixel(x, y) == Color.Black)
                    blackPixels.add(new Point(x, y));
            }
        }
        while(blackPixels.size() != 0) {
            Point p = blackPixels.removeFirst();
            if(numBlackNeighbors(b, p.x, p.y) < 2) {
                b.setPixel(p.x, p.y, Color.WHITE);
                Point temp = getBlackNeighbor(b, p.x, p.y);
                if(temp != null && !blackPixels.contains(temp))
                    blackPixels.addLast(temp);
            }
        }
    }
}
