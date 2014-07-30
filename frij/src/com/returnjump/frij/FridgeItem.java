package com.returnjump.frij;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FridgeItem {

    private long rowId;
    private String hash = null;
    private String name;
    private String rawName = null;
    private String expiryDate;
    private String createdDate = null;
    private String updatedDate = null;
    private String updatedBy = "DEVICE";
    private boolean fromImage= false;
    private String image = null;
    private String imageBinarized = null;
    private boolean dismissed = false;
    private boolean expired = false;
    private boolean editedCart = false;
    private boolean editedFridge = false;
    private boolean deletedCart = false;
    private boolean deletedFridge = false;
    private boolean notifiedPush = false;
    private boolean notifiedEmail = false;

    // General constructor (Parse)
    public FridgeItem(long rowId, String hash, String name, String rawName, String expiryDate,
                      String createdDate, String updatedDate, String updatedBy, boolean fromImage,
                      String image, String imageBinarized, boolean dismissed, boolean expired, boolean editedCart, boolean editedFridge,
                      boolean deletedCart, boolean deletedFridge, boolean notifiedPush, boolean notifiedEmail) {

        this.rowId = rowId;
        this.hash = hash;
        this.name = name;
        this.rawName = rawName;
        this.expiryDate = expiryDate;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.updatedBy = updatedBy;
        this.fromImage = fromImage;
        this.image = image;
        this.imageBinarized = imageBinarized;
        this.dismissed = dismissed;
        this.expired = expired;
        this.editedCart = editedCart;
        this.editedFridge = editedFridge;
        this.deletedCart = deletedCart;
        this.deletedFridge = deletedFridge;
        this.notifiedPush = notifiedPush;
        this.notifiedEmail = notifiedEmail;

    }

    // SQLite constructor
    public FridgeItem(long rowId, String hash, String name, String rawName, String expiryDate,
                      String createdDate, String updatedDate, String updatedBy, int fromImage,
                      String image, String imageBinarized, int dismissed, int expired, int editedCart, int editedFridge,
                      int deletedCart, int deletedFridge, int notifiedPush, int notifiedEmail) {

        this(rowId, hash, name, rawName, expiryDate, createdDate, updatedDate, updatedBy,
             intToBoolean(fromImage), image, imageBinarized, intToBoolean(expired), intToBoolean(dismissed),
             intToBoolean(editedCart), intToBoolean(editedFridge), intToBoolean(deletedCart),
             intToBoolean(deletedFridge), intToBoolean(notifiedPush), intToBoolean(notifiedEmail));

    }

    // ShoppingCart constructor
    public FridgeItem(long rowId, String name, String rawName, String expiryDate, String image, String imageBinarized) {

        this.rowId = rowId;
        this.name = name;
        this.rawName = rawName;
        this.expiryDate = expiryDate;
        this.fromImage = true; // This has to be from an image since it's from the ShoppingCart
        this.image = image;
        this.imageBinarized = imageBinarized;

    }

    // Minimal constructor
    public FridgeItem(long rowId, String name, String expiryDate) {

        this.rowId = rowId;
        this.name = name;
        this.expiryDate = expiryDate;

    }



    private static boolean intToBoolean(int n) {
        return n != 0;
    }

    public static String getMD5Hash(String message) {
        String hash = "";

        try {
            // Create MD5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(message.getBytes("UTF-8"));

            // Create hex string
            StringBuffer hexString = new StringBuffer();
            int n = messageDigest.length;
            for (int i = 0; i < n; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }

            hash = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }

        return hash;
    }



    public long getRowId() {
        return this.rowId;
    }

    public String getHash() {
        return this.hash;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getRawName() {
        return this.rawName;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    public int getDaysGood() {
        Calendar today = GregorianCalendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar expiry = FridgeDbHelper.stringToCalendar(getExpiryDate(), DatabaseContract.FORMAT_DATE);
        expiry.set(Calendar.HOUR_OF_DAY, 0);
        expiry.set(Calendar.MINUTE, 0);
        expiry.set(Calendar.SECOND, 0);
        expiry.set(Calendar.MILLISECOND, 0);

        int diffInDays = (int) ((expiry.getTimeInMillis() - today.getTimeInMillis()) / 86400000L);

        return diffInDays;
    }
    public int getExpirationNumber() {
        int timeGood = getDaysGood();

        if (timeGood >= 365) {
            timeGood = timeGood / 365;
        } else if (timeGood >= 28) {
            timeGood = timeGood / 28;
        } else if (timeGood >= 7) {
            timeGood = timeGood / 7;
        }

        return timeGood;
    }
    public String getExpirationUnit() {
        int timeGood = getDaysGood();
        String unit;

        if (timeGood >= 365) {
            timeGood = timeGood / 365;

            if (timeGood == 1) {
                unit = "year";
            } else {
                unit = "years";
            }
        } else if (timeGood >= 28) {
            timeGood = timeGood / 28;

            if (timeGood == 1) {
                unit = "month";
            } else {
                unit = "months";
            }
        } else if (timeGood >= 7) {
            timeGood = timeGood / 7;

            if (timeGood == 1) {
                unit = "week";
            } else {
                unit = "weeks";
            }
        } else {
            if (timeGood == 1) {
                unit = "day";
            } else {
                unit = "days";
            }
        }

        return unit;
    }

    public String getCreatedDate() {
        return this.createdDate;
    }

    public String getUpdatedDate() {
        return this.updatedDate;
    }
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean isFromImage() {
        return this.fromImage;
    }

    private byte[] getByteArray(String path) {
        if(path.equals("")) { // There was an error when the image was saving (no image saved)
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // Reduces the image resolution by 4

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        return stream.toByteArray();
    }

    public String getImagePath() {
        return this.image;
    }

    public byte[] getImageByteArray() {
        return getByteArray(getImagePath());
    }

    public String getImageBinarizedPath() {
        return this.imageBinarized;
    }

    public byte[] getImageBinarizedByteArray() {
        return getByteArray(getImageBinarizedPath());
    }

    public boolean isDismissed() {
        return this.dismissed;
    }
    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }

    public boolean isExpired() {
        return this.expired;
    }
    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isEditedCart() {
        return this.editedCart;
    }
    public void setEditedCart(boolean editedCart) {
        this.editedCart = editedCart;
    }

    public boolean isEditedFridge() {
        return this.editedFridge;
    }
    public void setEditedFridge(boolean editedFridge) {
        this.editedFridge = editedFridge;
    }

    public boolean isDeletedCart() {
        return this.deletedCart;
    }
    public void setDeletedCart(boolean deletedCart) {
        this.deletedCart = deletedCart;
    }

    public boolean isDeletedFridge() {
        return this.deletedFridge;
    }
    public void setDeletedFridge(boolean deletedFridge) {
        this.deletedFridge = deletedFridge;
    }

    public boolean isNotifiedPush() {
        return this.notifiedPush;
    }
    public void setNotifiedPush(boolean notifiedPush) {
        this.notifiedPush = notifiedPush;
    }

    public boolean isNotifiedEmail() {
        return this.notifiedEmail;
    }
    public void setNotifiedEmail(boolean notifiedEmail) {
        this.notifiedEmail = notifiedEmail;
    }

}
