package com.returnjump.phrije;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.Parcelable;
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

import com.cocosw.undobar.UndoBarController;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.seismic.ShakeDetector;

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

public class ShoppingCartActivity extends Activity implements /*CalendarDatePickerDialog.OnDateSetListener, EditNameFragment.OnEditNameButtonClickedListener,*/ UndoBarController.AdvancedUndoListener {

    private static Context context;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static String IMAGE_PATH;
    private static final String LANG = "eng";

    private Activity activity;
    private SwipeDismissListViewTouchListener swipeDismiss;
    private ArrayAdapter<FridgeItem> adapter;
    private FridgeDbHelper dbHelper;
    private List<FridgeItem> shoppingCart = new ArrayList<FridgeItem>();
    private List<FridgeItem> deletedCart = new ArrayList<FridgeItem>();
    private FoodTableHelper foodTableHelper;
    private ExpiryTableHelper expiryTableHelper;

    ListView cartListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        // Show the Up button in the action bar.
        setupActionBar();

        activity = this;

        // Check if device has a camera, go back if it doesn't
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "This device does not have a camera.", Toast.LENGTH_LONG).show();
            finish();
        }

        copyTessDataToStorage();

        cartListView = (ListView) findViewById(R.id.shoppingCart);
        this.initializeSwipeDismissListener();
        cartListView.setOnTouchListener(swipeDismiss);
        cartListView.setOnScrollListener(swipeDismiss.makeScrollListener());

        // Setup database and list
        dbHelper = new FridgeDbHelper(this);
        adapter = new MyFridgeAdapter(this, R.layout.list_fooditems, shoppingCart);
        cartListView.setAdapter(adapter);

        foodTableHelper = new FoodTableHelper(this);
        expiryTableHelper = new ExpiryTableHelper(this);

        findViewById(R.id.checkoutButton).setOnClickListener(addToFridge);

        // Open existing camera app, calls onActivityResult() when intent is finished
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,  fileUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void initializeSwipeDismissListener() { swipeDismiss =
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

                        Bundle b = new Bundle();
                        b.putInt("shoppingCartPosition", position);
                        b.putInt("deletedCartPosition", deletedCart.size() - 1);
                        b.putString("name", item.getName());
                        b.putString("rawName", item.getRawName());
                        b.putString("expiryDate", item.getExpiryDate());
                        b.putString("image", item.getImagePath());
                        b.putString("imageBinarized", item.getImageBinarizedPath());
                        new UndoBarController.UndoBar(activity)
                                .message("Removed " + item.getName())
                                .listener((UndoBarController.UndoListener) activity)
                                .token(b)
                                .show();

                        adapter.remove(item);
                        adapter.notifyDataSetChanged();

                    }
                });
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
        getMenuInflater().inflate(R.menu.camera, menu);
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
            Intent intent = new Intent(this, ShoppingCartActivity.class);
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
        File tessStorageDir = new File(Environment.getExternalStorageDirectory() + "/phrije/tessdata");

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
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "phrije");

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

                long id = dbHelper.put(item.getName(), FridgeDbHelper.stringToCalendar(item.getExpiryDate(), DatabaseContract.FORMAT_DATE), item.getRawName(), DatabaseContract.BOOL_TRUE, item.getImagePath(), item.getImageBinarizedPath());
                dbHelper.update(id, null, null, null, null, null, null, null, null, null, null);
            }

            // Add the deleted cart to the database, setting deleted_cart to True
            for (int j = 0; j < m; ++j) {
                FridgeItem item = deletedCart.get(j);

                long id = dbHelper.put(item.getName(), FridgeDbHelper.stringToCalendar(item.getExpiryDate(), DatabaseContract.FORMAT_DATE), item.getRawName(), DatabaseContract.BOOL_TRUE, item.getImagePath(), item.getImageBinarizedPath());
                dbHelper.update(id, null, null, DatabaseContract.BOOL_TRUE, null, null, null, DatabaseContract.BOOL_TRUE, null, null, null);
            }

            finish();
        }

    };

    private static int getPositionOfFirstLetter(String text) {
        text = text.toLowerCase();

        for (int i = 0; i < text.length(); i++) {
            int c = (int) text.charAt(i); // ascii value of character

            if (c >= 'a' && c <= 'z') {
                return i;
            }
        }

        return text.length();
    }

    private String findMatchInDatabase(String text) {
        // Later we can get the database to pass the name and row id so we don't need to do a second lookup
        // (also prevents error where matchedText isnt in db when getting rowId
        String matchedText = RecieptToDBHelper.minimumEditDistance(foodTableHelper.getAllByLetter(text.substring(0,1)), text);

        return matchedText;
    }

    // Chooses a random type for now
    private int getDaysUntilExpiry(long rowId) {
        List<FoodExpiry> foodExpiryList = expiryTableHelper.getAllByFoodId(rowId);
        int random = (int) (Math.random() * foodExpiryList.size());
        FoodExpiry foodExpiry = foodExpiryList.get(random);
        int days = -1;

        // Since we aren't asking the user where they'll store the food,
        // we will give preference based on the order below
        // (Food is more likely to be put in the refrigerator than a freezer)
        if (foodExpiry.getRefrigeratorDays() != -1) {
            days = foodExpiry.getRefrigeratorDays();
        } else if (foodExpiry.getPantryDays() != -1) {
            days = foodExpiry.getPantryDays();
        } else {
            days = foodExpiry.getFreezerDays();
        }

        return days;
    }

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

            new AlertDialog.Builder(ShoppingCartActivity.this)
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

        protected Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap[] splittedBitmap = splitBitmap(bitmaps[0]);
            Bitmap bitmap = splittedBitmap[0];
            Bitmap restBitmap = splittedBitmap[1];

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            TessBaseAPI baseApi = new TessBaseAPI();

            File dataStorageDir = new File(Environment.getExternalStorageDirectory(), "phrije");

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

            // Start at the first letter
            int firstLetterPos = getPositionOfFirstLetter(recognizedText);
            String recognizedTextFromFirstLetter = recognizedText.substring(firstLetterPos);

            if (!recognizedTextFromFirstLetter.equals("")) {
                String matchedText = findMatchInDatabase(recognizedTextFromFirstLetter);
                long rowId = foodTableHelper.getRowIdByName(matchedText);
                int days = getDaysUntilExpiry(rowId);

                Log.wtf("ORIGINAL", recognizedText);
                Log.wtf("MATCH", matchedText);

                // Add item to list
                Calendar c = GregorianCalendar.getInstance();
                c.add(Calendar.DATE, days);
                FridgeItem newFridgeItem = new FridgeItem(-1, matchedText, recognizedText, FridgeDbHelper.calendarToString(c, DatabaseContract.FORMAT_DATE), IMAGE_PATH, "PATH_TO_IMAGE_BINARIZED");
                shoppingCart.add(newFridgeItem);

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

    @Override
    public void onUndo(Parcelable token) {
        if (token != null) {
            final int shoppingCartPosition = ((Bundle) token).getInt("shoppingCartPosition");
            final int deletedCartPosition = ((Bundle) token).getInt("deletedCartPosition");
            final String name = ((Bundle) token).getString("name");
            final String rawName = ((Bundle) token).getString("rawName");
            final String expiryDate = ((Bundle) token).getString("expiryDate");
            final String image = ((Bundle) token).getString("image");
            final String imageBinarized = ((Bundle) token).getString("imageBinarized");

            FridgeItem item = new FridgeItem(-1, name, rawName, expiryDate, image, imageBinarized);

            // Remove the item from deleted and put it back in the shopping cart
            deletedCart.remove(deletedCartPosition);

            shoppingCart.add(shoppingCartPosition, item);
            adapter.notifyDataSetChanged();

            // Need to add an ellipsis to long names
            Toast.makeText(this, "Added back " + name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHide(Parcelable token) {

    }

    @Override
    public void onClear() {

    }

}
