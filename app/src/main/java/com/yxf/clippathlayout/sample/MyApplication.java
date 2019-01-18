package com.yxf.clippathlayout.sample;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class MyApplication extends Application {

    private static Context sAppContext;
    private static Toast sToast;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = getApplicationContext();
    }

    public static void displayToast(String message) {
        if (sToast == null) {
            sToast = Toast.makeText(sAppContext, message, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(message);
        }
        sToast.show();
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
