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
    private Activity activity;

    private Context context;


    public MyProductsForRecyclerView(String name, String countProd, String value, String imagePath, long notificationTime, long notificationDate, Context context,Activity activity){

        this.name=name;
        this.countProd = countProd;
        this.value = value;
        this.imagePath = imagePath;
        this.notificationTime = notificationTime;
        this.notificationDate = notificationDate;
        this.context = context;
        this.activity = activity;


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
}
