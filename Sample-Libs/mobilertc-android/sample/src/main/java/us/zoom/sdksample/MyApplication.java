package us.zoom.sdksample;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    static MyApplication application;
    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);


    }

    public static Context getInstance(){
        return application;
    }
}
