package us.zoom.sdksample.share;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class AppUtil {
    public static int dip2px(Context context, float value) {
        if(context == null)
            return (int)value;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    public static int getDisplayWidth(Context context) {
        WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if(windowMgr == null)
            return 0;

        Display display = windowMgr.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getDisplayHeight(Context context) {
        WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if(windowMgr == null)
            return 0;

        Display display = windowMgr.getDefaultDisplay();
        return display.getHeight();
    }
}
