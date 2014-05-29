package com.returnjump.spoilfoil;

public class FridgeItem {

    private long rowId;
    private String hash;
    private String foodItem;
    private String rawFoodItem;
    private String expiryDate;
    private String createdDate;
    private String updatedDate;
    private String updatedBy;
    private boolean fromImage;
    private byte[] image;
    private byte[] imageBinarized;
    private boolean dismissed;
    private boolean expired;
    private boolean editedCart;
    private boolean editedFridge;
    private boolean deletedCart;
    private boolean deletedFridge;
    private boolean notifiedPush;
    private boolean notifiedEmail;

    // General constructor (Parse)
    public FridgeItem(long rowId, String hash, String foodItem, String rawFoodItem, String expiryDate,
                      String createdDate, String updatedDate, String updatedBy, boolean fromImage,
                      byte[] image, byte[] imageBinarized, boolean dismissed, boolean expired, boolean editedCart, boolean editedFridge,
                      boolean deletedCart, boolean deletedFridge, boolean notifiedPush, boolean notifiedEmail) {

        this.rowId = rowId;
        this.hash = hash;
        this.foodItem = foodItem;
        this.rawFoodItem = rawFoodItem;
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
    public FridgeItem(long rowId, String hash, String foodItem, String rawFoodItem, String expiryDate,
                      String createdDate, String updatedDate, String updatedBy, int fromImage,
                      byte[] image, byte[] imageBinarized, int dismissed, int expired, int editedCart, int editedFridge,
                      int deletedCart, int deletedFridge, int notifiedPush, int notifiedEmail) {

        this(rowId, hash, foodItem, rawFoodItem, expiryDate, createdDate, updatedDate, updatedBy,
             intToBoolean(fromImage), image, imageBinarized, intToBoolean(expired), intToBoolean(dismissed),
             intToBoolean(editedCart), intToBoolean(editedFridge), intToBoolean(deletedCart),
             intToBoolean(deletedFridge), intToBoolean(notifiedPush), intToBoolean(notifiedEmail));

    }



    private static boolean intToBoolean(int n) {
        return n != 0;
    }



    public long getRowId() {
        return this.rowId;
    }

    public String getHash() {
        return this.hash;
    }

    public String getFoodItem() {
        return this.foodItem;
    }
    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public String getRawFoodItem() {
        return this.rawFoodItem;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
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

    public byte[] getImage() {
        return this.image;
    }

    public byte[] getImageBinarized() {
        return this.imageBinarized;
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
