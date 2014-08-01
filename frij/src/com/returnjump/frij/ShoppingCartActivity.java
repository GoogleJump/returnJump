package com.returnjump.frij;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.faizmalkani.floatingactionbutton.Fab;

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

public class ShoppingCartActivity extends FragmentActivity implements CalendarDatePickerDialog.OnDateSetListener, EditNameFragment.OnEditNameButtonClickedListener, UndoBarController.AdvancedUndoListener {

    private static Context context;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static String IMAGE_PATH;
    public static final String LANG = "eng";

    private Activity activity;
    private SwipeDismissListViewTouchListener swipeDismiss;
    public ArrayAdapter<FridgeItem> adapter;
    private ListView cartListView;
    private FridgeDbHelper dbHelper;
    public List<FridgeItem> shoppingCart = new ArrayList<FridgeItem>();
    private List<FridgeItem> deletedCart = new ArrayList<FridgeItem>();
    private EditNameFragment editNameFragment;
    private String EDIT_FRAG_TAG = "edit_frag_tag";
    private String CAL_PICKER_TAG = "cal_frag_tag";
    private Fab fabCheckout;
    private int mLastFirstVisibleItem = 0;
    private boolean isUndoBarVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        // Show the Up button in the action bar.
        setupActionBar();

        activity = this;
        context = this;

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
        cartListView.setOnScrollListener(onScrollListener);

        // Setup database and list
        dbHelper = new FridgeDbHelper(this);
        adapter = new MyFridgeAdapter(this, R.layout.list_fooditems, shoppingCart);
        cartListView.setAdapter(adapter);

        // Initialize FAB when we get the image
        fabCheckout = (Fab) findViewById(R.id.fab_checkout);
        fabCheckout.setFabColor(context.getResources().getColor(R.color.theme));
        fabCheckout.setFabDrawable(getResources().getDrawable(R.drawable.ic_action_checkout));
        fabCheckout.setOnClickListener(addToFridge);

        // Open existing camera app, calls onActivityResult() when intent is finished
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,  fileUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        initializeLongClickListener();
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

                        isUndoBarVisible = true;
                        fabCheckout.hideFab();
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

    public void initializeLongClickListener() {
        cartListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                Bundle args = new Bundle();
                args.putInt("position", position);
                editNameFragment = new EditNameFragment();
                editNameFragment.setArguments(args);
                editItemSequence(view, false);
                return true;
            }
        });
    }

    public void editItemSequence(View view, Boolean isNewItem) {
        FridgeItem fridgeItem = adapter.getItem(editNameFragment.getArguments().getInt("position"));
        String itemName = fridgeItem.getName();
        String itemDate = fridgeItem.getExpiryDate();
        Bundle args = editNameFragment.getArguments();
        args.putString("name", itemName);
        args.putString("date", itemDate);
        editNameFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(editNameFragment, EDIT_FRAG_TAG);
        fragmentTransaction.addToBackStack(EDIT_FRAG_TAG);
        fragmentTransaction.commit();
        //onEditNameButtonClicked implemented in MainActivity called next
    }

    /**
     * @param dialog      The view associated with this listener.
     * @param year        The year that was set.
     * @param monthOfYear The month that was set (0-11) for compatibility with {@link java.util.Calendar}.
     * @param dayOfMonth  The day of the month that was set.
     */
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(getSupportFragmentManager().findFragmentByTag(EDIT_FRAG_TAG));
        ft.commit();

        Calendar expiryDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        String foodName = editNameFragment.getArguments().getString("name");

        FridgeItem fridgeItem = shoppingCart.get(editNameFragment.getArguments().getInt("position"));
        fridgeItem.setName(foodName);
        fridgeItem.setExpiryDate(FridgeDbHelper.calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        adapter.notifyDataSetChanged();
        editNameFragment=null;
    }

    @Override
    public void onEditNameButtonClicked(Boolean isNewItem) {
        String date = editNameFragment.getArguments().getString("date");
        Calendar c = FridgeDbHelper.stringToCalendar(date, DatabaseContract.FORMAT_DATE);
        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
            .newInstance(ShoppingCartActivity.this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    calendarDatePickerDialog.setYearRange(Calendar.getInstance().get(Calendar.YEAR), calendarDatePickerDialog.getMaxYear());
    calendarDatePickerDialog.show(getSupportFragmentManager(), CAL_PICKER_TAG);
        //onDateSet called next
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
        getMenuInflater().inflate(R.menu.shopping_cart, menu);
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
        File tessStorageDir = new File(Environment.getExternalStorageDirectory() + "/frij/tessdata");

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
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "frij");

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
    public static Bitmap fixImageOrientation(Bitmap image, final String PATH) {
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
                new BinarizeImageTask(getApplicationContext(), ShoppingCartActivity.this).execute(IMAGE_PATH);

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

    // Async task this!
    private OnClickListener addToFridge = new OnClickListener() {

        @Override
        public void onClick(View view) {
            int n = shoppingCart.size();
            int m = deletedCart.size();

            // Add the shopping cart to the database
            for (int i = 0; i < n; ++i) {
                FridgeItem item = shoppingCart.get(i);

                long id = dbHelper.put(item.getName(), FridgeDbHelper.stringToCalendar(item.getExpiryDate(), DatabaseContract.FORMAT_DATE), item.getRawName(), DatabaseContract.BOOL_TRUE, item.getImagePath(), item.getImageBinarizedPath());
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

    @Override
    public void onUndo(Parcelable token) {
        isUndoBarVisible = false;
        fabCheckout.showFab();

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
        }
    }

    @Override
    public void onHide(Parcelable token) {
        isUndoBarVisible = false;
        fabCheckout.showFab();
    }

    @Override
    public void onClear() {

    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // Prevents fab from appearing if scrolling while undobar is visible
            if (isUndoBarVisible) {
                return;
            }

            if (firstVisibleItem > mLastFirstVisibleItem) { // Down
                fabCheckout.hideFab();
            } else if (firstVisibleItem < mLastFirstVisibleItem) { // Up
                fabCheckout.showFab();
            }

            mLastFirstVisibleItem = firstVisibleItem;
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    };

}
