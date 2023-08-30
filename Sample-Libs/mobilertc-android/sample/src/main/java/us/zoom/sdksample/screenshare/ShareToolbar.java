package us.zoom.sdksample.screenshare;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdksample.R;

public class ShareToolbar {
    private final static String TAG = "ShareToolbar";

    public interface Listener {
        void onClickStopShare();
    }

    protected static final boolean annoter_test = false;

    private final WindowManager mWindowManager;

    private final Context mContext;

    private View contentView;
    private View stopShareLayout;
    private View annotationView;
    private View annotationLayout;

    private Listener mListener;
    private Display mDisplay;

    float mLastRawX = -1f;
    float mLastRawY = -1f;

    boolean annotateStart = false;
    public ShareToolbar(Listener listener, Context context) {
        mListener = listener;
        mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        annotateStart = false;
    }

    private void init() {
        contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_share_toolbar, null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        stopShareLayout = contentView.findViewById(R.id.stop_share_layout);
        annotationLayout = contentView.findViewById(R.id.annotation_layout);

        if (stopShareLayout != null) {
            stopShareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onClickStopShare();
                    }
                    destroy();
                }
            });
        }

        if (annoter_test) {
            if (annotationLayout != null) {
                annotationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int error = ZoomVideoSDKErrors.Errors_Wrong_Usage;
                        ZoomVideoSDKAnnotationHelper annotationHelper = getAnnotationHelper();
                        if (annotationHelper != null) {
                            if (!annotateStart) {
                                error = annotationHelper.startAnnotation();
                            } else {
                                error = annotationHelper.stopAnnotation();
                            }
                        }
                        if (error == ZoomVideoSDKErrors.Errors_Success) {
                            annotateStart = !annotateStart;
                        } else {
                            Log.e(TAG, "start/stop annotation error: " + error);
                        }
                    }
                });
                annotationLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void destroy() {
        if (null != mWindowManager) {
            if (annoter_test) {
                if (null != annotationView) {
                    mWindowManager.removeView(annotationView);
                    annotationView = null;
                }
            }
            if (null != contentView) {
                mWindowManager.removeView(contentView);
                contentView = null;
            }
        }
    }

    private ZoomVideoSDKAnnotationHelper annotationHelper = null;
    private ZoomVideoSDKAnnotationHelper getAnnotationHelper() {
        if (annotationHelper == null) {
            annotationHelper = ZoomVideoSDK.getInstance().getShareHelper().createAnnotationHelper(null);
        }
        return annotationHelper;
    }

    public void showToolbar() {

        if (annoter_test) {
            if (null == annotationView) {
                ZoomVideoSDKAnnotationHelper annotationHelper = getAnnotationHelper();
                if (annotationHelper != null) {
                    annotationView = annotationHelper.getAnnotationView();
                }
            }
            if (annotationView != null) {
                WindowManager.LayoutParams inLayoutParams = new WindowManager.LayoutParams();
                inLayoutParams.type = getWindowLayoutParamsType();
                inLayoutParams.format = PixelFormat.RGBA_8888;
                inLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                inLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                mWindowManager.addView(annotationView, inLayoutParams);
            }
        }

        if (null == contentView) {
            init();
        }
        contentView.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = getWindowLayoutParamsType();
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        int height = contentView.getHeight();
        if (height == 0) {
            height = 150;
        }
        layoutParams.x = 100;
        layoutParams.y = mDisplay.getHeight() - 100 - height;
        mWindowManager.addView(contentView, layoutParams);

    }

    private int getWindowLayoutParamsType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (Settings.canDrawOverlays(mContext))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                return WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                return WindowManager.LayoutParams.TYPE_TOAST;
            }
        }
    }
}
