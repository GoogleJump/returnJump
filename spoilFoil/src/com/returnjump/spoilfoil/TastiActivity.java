package com.returnjump.spoilfoil;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TastiActivity extends Activity {

    private static Context context;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static String IMAGE_PATH;
    private static final String LANG = "eng";

    private ArrayAdapter<FridgeItem> adapter;
    private FridgeDbHelper dbHelper;
    private List<FridgeItem> shoppingCart = new ArrayList<FridgeItem>();
    private List<FridgeItem> deletedCart = new ArrayList<FridgeItem>();
    private List<byte[]> shoppingCartImages = new ArrayList<byte[]>();
    private List<byte[]> deletedCartImages = new ArrayList<byte[]>();

    ListView cartListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasti);
        // Show the Up button in the action bar.
        setupActionBar();

        context = this;

        copyTessDataToStorage();

        cartListView = (ListView) findViewById(R.id.shoppingCart);
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(cartListView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        // reverseSortedPositions always has a length = 1
                        int position = reverseSortedPositions[0];
                        FridgeItem item = adapter.getItem(position);

                        // Take the item+image and put it in the deleted cart, then remove it
                        deletedCart.add(item);
                        deletedCartImages.add(shoppingCartImages.get(position));

                        adapter.remove(item);
                        shoppingCartImages.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
        cartListView.setOnTouchListener(touchListener);
        cartListView.setOnScrollListener(touchListener.makeScrollListener());

        // Setup database and list
        dbHelper = new FridgeDbHelper(this);
        adapter = new MyFridgeAdapter(this, R.layout.list_fooditems, shoppingCart);
        cartListView.setAdapter(adapter);

        findViewById(R.id.checkoutButton).setOnClickListener(addToFridge);

        // Open existing camera app, calls onActivityResult() when intent is finished
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,  fileUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tasti, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);

            return true;
        } else if (itemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        } else if (itemId == R.id.action_camera) {
            Intent intent = new Intent(this, TastiActivity.class);
            startActivity(intent);

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    // LANG.traineddata file with the app (in assets folder)
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    // This area needs work and optimization
    // Should use getFreeSpace() to verify the data will fit in storage
    private static void copyTessDataToStorage() {
        File tessStorageDir = new File(Environment.getExternalStorageDirectory() + "/SpoilFoil/tessdata");

        // Create the storage directory if writable and it does not exist
        if (isExternalStorageWritable() && !tessStorageDir.exists()){
            if (!tessStorageDir.mkdirs()){
                Toast.makeText(context, "Failed to create directory:\n" + tessStorageDir.getPath(), Toast.LENGTH_LONG).show();
            }
        }

        if (!(new File(tessStorageDir + "/" + LANG + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = context.getAssets();
                InputStream in = assetManager.open("tessdata/" + LANG + ".traineddata");
                OutputStream out = new FileOutputStream(tessStorageDir + "/" + LANG + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Toast.makeText(context, "Copied " + LANG + " traineddata", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(context, "Was unable to copy " + LANG + " traineddata ", Toast.LENGTH_LONG).show();
            }
        }

    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SpoilFoil");

        // Create the storage directory if it does not exist
        if (isExternalStorageWritable() && !mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Toast.makeText(context, "Failed to create directory.", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            IMAGE_PATH = mediaStorageDir.getPath() + "/IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(IMAGE_PATH);
            //} else if(type == MEDIA_TYPE_VIDEO) {
            //    mediaFile = new File(mediaStorageDir.getPath() + "/VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    // Correct orientation of image
    private static Bitmap fixImageOrientation(Bitmap image, final String PATH) {
        try {
            ExifInterface exif = new ExifInterface(PATH);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            //Toast.makeText(context, "ROTATION: " + rotate, Toast.LENGTH_LONG).show();

            if (rotate != 0) {
                int w = image.getWidth();
                int h = image.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                Log.e("SpoilFoil", "WIDTH: " + Integer.toString(w));
                Log.e("SpoilFoil", "HEIGHT: " + Integer.toString(h));

                // Rotating image
                return Bitmap.createBitmap(image, 0, 0, w, h, mtx, false);
            }

        } catch (IOException e) {
            // Couldn't correct orientation
        }

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Process image in an AsyncTask

                new BinarizeImageTask().execute(IMAGE_PATH);

                // Delete the file after processed using file.delete()
                // or we can save them to the cloud for later use
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                // Image capture failed, advise user
                Toast.makeText(this, "Image capture failed.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private OnClickListener addToFridge = new OnClickListener() {

        @Override
        public void onClick(View view) {
            int n = shoppingCart.size();
            int m = deletedCart.size();

            // Add the shopping cart to the database
            for (int i = 0; i < n; ++i) {
                FridgeItem item = shoppingCart.get(i);

                long id = dbHelper.put(item.getName(), FridgeDbHelper.stringToCalendar(item.getExpiryDate(), DatabaseContract.FORMAT_DATE), item.getRawName(), shoppingCartImages.get(i), shoppingCartImages.get(i));
                dbHelper.update(id, null, null, DatabaseContract.BOOL_TRUE, null, null, null, null, null, null, null, null);
            }

            // Add the deleted cart to the database, setting deleted_cart to True
            for (int j = 0; j < m; ++j) {
                FridgeItem item = deletedCart.get(j);

                long id = dbHelper.put(item.getName(), FridgeDbHelper.stringToCalendar(item.getExpiryDate(), DatabaseContract.FORMAT_DATE), item.getRawName(), deletedCartImages.get(j), deletedCartImages.get(j));
                dbHelper.update(id, null, null, DatabaseContract.BOOL_TRUE, DatabaseContract.BOOL_TRUE, null, null, null, DatabaseContract.BOOL_TRUE, null, null, null);
            }

            finish();
        }

    };

    private class BinarizeImageTask extends AsyncTask<String, Void, Bitmap> {

        protected void onPreExecute () {
            Toast.makeText(getApplicationContext(), "Binarizing image.", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... PATH) {
            // Scale image to reduce memory consumption
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reduces the image resolution to half

            Bitmap bitmap = BitmapFactory.decodeFile(PATH[0], options);

            bitmap = fixImageOrientation(bitmap, PATH[0]);

            // Leptonica binarization
            Pix pix = ReadFile.readBitmap(bitmap);
            pix = Binarize.otsuAdaptiveThreshold(pix, 32, 32, 2, 2, 0.9F);
            //pix = Binarize.otsuAdaptiveThreshold(pix);
            bitmap = WriteFile.writeBitmap(pix);

            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            // Display the binarized image in an alert dialog
            ImageView binaryImageView = new ImageView(getApplicationContext());
            binaryImageView.setImageBitmap(bitmap);

            new AlertDialog.Builder(TastiActivity.this)
                    .setTitle("Binarized Image:")
                    .setView(binaryImageView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();

            new OcrImageTask(true).execute(bitmap);
        }
    }

    private class OcrImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_ocr);
        private Button checkoutButton = (Button) findViewById(R.id.checkoutButton);
        private boolean isFirstCall;

        // Override constructor to pass additional param
        private OcrImageTask(boolean isFirstCall) {
            this.isFirstCall = isFirstCall;
        }

        protected void onPreExecute () {
            if (isFirstCall) {
                Toast.makeText(getApplicationContext(), "OCR'ing image.", Toast.LENGTH_SHORT).show();
            }
        }

        private Bitmap[] splitBitmap(Bitmap bitmap) {
            int threshold = 32;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int pixel;
            boolean started = false;
            boolean isBlankRow = true;

            // Default if height below threshold or no blank row found
            Bitmap first = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            Bitmap rest = null;

            if (height > threshold) {

                for (int y = 0; y < height; ++y) {
                    isBlankRow = true;

                    for (int x = 0; x < width; ++x) {
                        pixel = bitmap.getPixel(x, y);

                        if (pixel == Color.BLACK) {
                            started = true;
                            isBlankRow = false;
                            break;
                        }
                    }

                    if (started && isBlankRow) {
                        first = Bitmap.createBitmap(bitmap, 0, 0, width, y+1);
                        rest = Bitmap.createBitmap(bitmap, 0, y+1, width, height - y-1);

                        break;
                    }
                }

            }

            Bitmap[] splittedBitmap = {first, rest};

            return splittedBitmap;
        }

        private byte[] bitmapToByteArray(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            return stream.toByteArray();
        }

        protected Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap[] splittedBitmap = splitBitmap(bitmaps[0]);
            Bitmap bitmap = splittedBitmap[0];
            Bitmap restBitmap = splittedBitmap[1];

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            TessBaseAPI baseApi = new TessBaseAPI();

            File dataStorageDir = new File(Environment.getExternalStorageDirectory(), "SpoilFoil");

            // Create the storage directory if writable and it does not exist
            if (isExternalStorageWritable() && !dataStorageDir.exists()){
                if (!dataStorageDir.mkdirs()){
                    //Toast.makeText(context, "Failed to create directory.", Toast.LENGTH_LONG).show();
                }
            }

            String DATA_PATH = dataStorageDir.getPath();
            baseApi.init(DATA_PATH, LANG);
            baseApi.setImage(bitmap);
            String recognizedText = baseApi.getUTF8Text().trim();
            baseApi.end();

            // This is where the algorithm should take recognizedText and extract the right word(s)
            // and expiry date out of it

            if (!recognizedText.equals("")) {
                // Add item to  list
                Calendar c = GregorianCalendar.getInstance();
                c.add(Calendar.DATE, 7); // DEFAULTED TO 1 WEEK!
                FridgeItem newFridgeItem = new FridgeItem(-1, recognizedText, FridgeDbHelper.calendarToString(c, DatabaseContract.FORMAT_DATE), recognizedText);
                shoppingCart.add(newFridgeItem);
                shoppingCartImages.add(bitmapToByteArray(bitmap));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            return restBitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                new OcrImageTask(false).execute(bitmap);
            } else {
                progressBar.setVisibility(View.GONE);
                checkoutButton.setVisibility(View.VISIBLE);
            }
        }
    }

}
