package com.yxf.clippathlayout.sample;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.squareup.leakcanary.LeakCanary;

public class MyApplication extends Application {

    private static Context sAppContext;
    private static Toast sToast;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
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
