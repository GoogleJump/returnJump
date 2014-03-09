package com.returnjump.spoilfoil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.provider.MediaStore;

public class TastiActivity extends Activity {
	
	private static Context context;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static String IMAGE_PATH;
	private static final String LANG = "eng";
	private Uri fileUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasti);
		// Show the Up button in the action bar.
		setupActionBar();
		
		context = this;
		
		// To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File tessStorageDir = new File(Environment.getExternalStorageDirectory() + "/SpoilFoil/tessdata");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (!tessStorageDir.exists()){
	        if (!tessStorageDir.mkdirs()){
	            Toast.makeText(context, "Failed to create directory:\n" + tessStorageDir.getPath(), Toast.LENGTH_LONG).show();
	        }
	    }

		// LANG.traineddata file with the app (in assets folder)
		// You can get them at:
		// http://code.google.com/p/tesseract-ocr/downloads/list
		// This area needs work and optimization
		if (!(new File(tessStorageDir + "/" + LANG + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + LANG + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(tessStorageDir + "/" + LANG + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
				
				Toast.makeText(context, "Copied " + LANG + " traineddata", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				Toast.makeText(context, "Was unable to copy " + LANG + " traineddata ", Toast.LENGTH_LONG).show();
			}
		}
		
		// Open existing camera app, calls onActivityResult() when intent is finished
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
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
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SpoilFoil");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (!mediaStorageDir.exists()){
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
	        	new OcrImageTask().execute(IMAGE_PATH);
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        	Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_LONG).show();
	        } else {
	            // Image capture failed, advise user
	        	Toast.makeText(this, "Image capture failed.", Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
	private class OcrImageTask extends AsyncTask<String, Void, String> {
	    private ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_ocr);
	    
	    protected void onPreExecute () {
	        progressBar.setVisibility(View.VISIBLE);
	    }
	    
	    protected String doInBackground(final String... PATH) {
		    // Image captured and saved to fileUri specified in the Intent
		    //Toast.makeText(context, "Image saved to:\n" + PATH[0], Toast.LENGTH_LONG).show();
            
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap imageBitmap = BitmapFactory.decodeFile(PATH[0]);
            
            // Scale image to reduce memory usage
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth() / 2, imageBitmap.getHeight() / 2, false);
            imageBitmap = fixImageOrientation(imageBitmap, PATH[0]);
            
            // Convert to ARGB_8888, required by tess
            imageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            
            TessBaseAPI baseApi = new TessBaseAPI();
			
			// To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this.

		    File dataStorageDir = new File(Environment.getExternalStorageDirectory(), "SpoilFoil");
		    // This location works best if you want the created images to be shared
		    // between applications and persist after your app has been uninstalled.

		    // Create the storage directory if it does not exist
		    if (!dataStorageDir.exists()){
		        if (!dataStorageDir.mkdirs()){
		            //Toast.makeText(context, "Failed to create directory.", Toast.LENGTH_LONG).show();
		        }
		    }
		    
		    String DATA_PATH = dataStorageDir.getPath();
			baseApi.init(DATA_PATH, LANG);
			baseApi.setImage(imageBitmap);
			String recognizedText = baseApi.getUTF8Text();
			baseApi.end();
			
			// Display image in ImageView
			final Bitmap imageBitmapFinal = imageBitmap;
			//imageBitmap.recycle();
			
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = (ImageView) findViewById(R.id.image_thumbnail);
                    imageView.setImageBitmap(imageBitmapFinal);
               }
            });
			
            // Optimize text
			return recognizedText.replaceAll("[^a-zA-Z0-9\n]+", " ").trim();
	     }

	    protected void onPostExecute(final String recognizedText) {
	        // Display text in TextView
			runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			         TextView textView = (TextView) findViewById(R.id.recognized_text);
			         textView.setText(recognizedText);
			    }
			});
			
			progressBar.setVisibility(View.GONE);
	     }
	 }

}
