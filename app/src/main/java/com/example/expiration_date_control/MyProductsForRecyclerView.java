package com.example.expiration_date_control;

import android.app.Activity;
import android.content.Context;

public class MyProductsForRecyclerView {

    private String name;
    private String countProd;
    private String value;
    private String imagePath;
    private long notificationTime;
    long notificationDate;
    long validUntilDate;
    private long productionDate;
    private int number;
    private String category;
    private Activity activity;

    private Context context;


    public MyProductsForRecyclerView(String name, String countProd, String value, String imagePath, long notificationTime, long notificationDate,long validUntilDate, long productionDate, String category,int number, Context context,Activity activity){

        this.name=name;
        this.countProd = countProd;
        this.value = value;
        this.imagePath = imagePath;
        this.notificationTime = notificationTime;
        this.notificationDate = notificationDate;
        this.validUntilDate = validUntilDate;
        this.productionDate = productionDate;
        this.category = category;
        this.context = context;
        this.activity = activity;
        this.number = number;



    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(long notificationDate) {
        this.notificationDate = notificationDate;
    }

    public String getCountProd() {
        return countProd;
    }

    public void setCountProd(String countProd) {
        this.countProd = countProd;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(long notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public long getValidUntilDate() {
        return validUntilDate;
    }

    public void setValidUntilDate(long validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public long getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(long productionDate) {
        this.productionDate = productionDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
