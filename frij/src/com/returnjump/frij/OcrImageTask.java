package com.returnjump.frij;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.faizmalkani.floatingactionbutton.Fab;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class OcrImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
    private ProgressBar progressBar;
    private Fab fabCheckout;
    private boolean isFirstCall;
    private Context context;
    private Activity activity;

    private FoodTableHelper foodTableHelper;
    private ExpiryTableHelper expiryTableHelper;

    // Override constructor to pass additional param
    public OcrImageTask(boolean isFirstCall, Context context, Activity activity) {
        this.isFirstCall = isFirstCall;
        this.context = context;
        this.activity = activity;
        this.progressBar = (ProgressBar) this.activity.findViewById(R.id.progress_ocr);
        this.fabCheckout = (Fab) this.activity.findViewById(R.id.fab_checkout);

        foodTableHelper = new FoodTableHelper(this.activity);
        expiryTableHelper = new ExpiryTableHelper(this.activity);
    }

    protected void onPreExecute () {
        if (isFirstCall) {
            Toast.makeText(context, "OCR'ing image.", Toast.LENGTH_SHORT).show();
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

    // Name should be set to the fridgeItem's hash later
    private String saveBitmapToFileSystem(Bitmap bitmap) {
        File dataStorageDir = new File(Environment.getExternalStorageDirectory(), "frij");

        // Create the storage directory if writable and it does not exist
        if (ShoppingCartActivity.isExternalStorageWritable() && !dataStorageDir.exists()){
            if (!dataStorageDir.mkdirs()){
                Toast.makeText(context, "Failed to create directory.", Toast.LENGTH_LONG).show();
                return "";
            }
        }

        String name = FridgeItem.getMD5Hash(bitmap.toString()) + ".jpg";
        File image = new File(dataStorageDir, name);
        try {
            FileOutputStream out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return "";
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return "";
        }

        return dataStorageDir.getPath() + "/" + name;
    }

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

    protected Bitmap doInBackground(Bitmap... bitmaps) {
        Bitmap[] splittedBitmap = splitBitmap(bitmaps[0]);
        Bitmap bitmap = splittedBitmap[0];
        Bitmap restBitmap = splittedBitmap[1];

        String fileName = saveBitmapToFileSystem(bitmap);

        // Convert to ARGB_8888, required by tess
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        TessBaseAPI baseApi = new TessBaseAPI();

        File dataStorageDir = new File(Environment.getExternalStorageDirectory(), "frij");

        // Create the storage directory if writable and it does not exist
        if (ShoppingCartActivity.isExternalStorageWritable() && !dataStorageDir.exists()){
            if (!dataStorageDir.mkdirs()){
                Toast.makeText(context, "Failed to create directory.", Toast.LENGTH_LONG).show();
            }
        }

        String DATA_PATH = dataStorageDir.getPath();
        baseApi.init(DATA_PATH, ShoppingCartActivity.LANG);
        baseApi.setImage(bitmap);
        String[] recognizedTexts = baseApi.getUTF8Text().trim().split("\n");
        baseApi.end();

        for (String recognizedText : recognizedTexts) {
            //removing non-letter characters and stuff after the $
            if(recognizedText.equals(""))
            {
                continue;
            }

            String recognizedTextStripped = cleanText(recognizedText);

            if (!recognizedTextStripped.equals("")) {
                String matchedText = findMatchInDatabase(recognizedTextStripped);
                //if a match less than RecieptTODBHelper.MAX_EDIT_DISTANCE_THRESHOLD was found
                if (matchedText != null) {
                    long rowId = foodTableHelper.getRowIdByName(matchedText);
                    int days = getDaysUntilExpiry(rowId);

                    Log.wtf("ORIGINAL", recognizedText);
                    Log.wtf("CLEANED", recognizedTextStripped);
                    Log.wtf("MATCH", matchedText);

                    // Add item to list
                    Calendar c = GregorianCalendar.getInstance();
                    c.add(Calendar.DATE, days);
                    FridgeItem newFridgeItem = new FridgeItem(-1, matchedText, recognizedText, FridgeDbHelper.calendarToString(c, DatabaseContract.FORMAT_DATE), ShoppingCartActivity.IMAGE_PATH, fileName);

                    //check if the item is in the list, if not, do nothing as we don't need to add you again

                    if(!((ShoppingCartActivity) activity).shoppingCart.contains(newFridgeItem)) {
                        ((ShoppingCartActivity) activity).shoppingCart.add(newFridgeItem);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ShoppingCartActivity) activity).adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        }

        return restBitmap;
    }

    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            new OcrImageTask(false, context, activity).execute(bitmap);
        } else {
            progressBar.setVisibility(View.GONE);
            fabCheckout.setVisibility(View.VISIBLE); // Fix to prevent button from appearing at top-right corner (must be set to invisible in the layout)
            fabCheckout.showFab();
        }
    }


    //Takes in s, a line of text from a receipt, and removes unwanted character
    private static String cleanText(String s) {
        Log.wtf(s, "  cleanText   in");
        int firstLetterPos = getPositionOfFirstLetter(s);
        if(firstLetterPos == -1)
            return "";
        s = s.toLowerCase();
        if(s.indexOf("total") != -1)
            return "";
        if(s.indexOf("tax") != -1)
            return "";
        String stripped = s.substring(firstLetterPos);

        String out = "";
        boolean goodWord = true;
        int lastWhitespace = -1;
        for(int i = 0; i < stripped.length(); i++) {
            if(!isLetter(stripped.charAt(i))) {

                if(Character.isWhitespace(stripped.charAt(i))) {
                    if(goodWord && lastWhitespace < i-2)
                        out += stripped.substring(lastWhitespace+1, i) + " ";
                    lastWhitespace = i;
                    goodWord = true;
                }
                else
                    goodWord = false;
            }
        }
        if(goodWord && lastWhitespace < stripped.length()-2)
            out += stripped.substring(lastWhitespace+1, stripped.length());
        out = out.trim();
        Log.wtf(out, " cleanText   out");
        return out;
    }

    private static boolean isLetter(char c) {
            return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }
}


