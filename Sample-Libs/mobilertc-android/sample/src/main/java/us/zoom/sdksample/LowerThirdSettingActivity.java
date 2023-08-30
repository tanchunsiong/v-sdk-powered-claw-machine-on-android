package us.zoom.sdksample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdksample.cmd.CmdLowerThirdRequest;
import us.zoom.sdksample.cmd.LowerThirdColorType;
import us.zoom.sdksample.view.CycleColorImageView;

public class LowerThirdSettingActivity extends AppCompatActivity {
    private Display display;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lower_third_setting);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        int displayRotation = display.getRotation();
        ZoomVideoSDK.getInstance().getVideoHelper().rotateMyVideo(displayRotation);
        ZoomVideoSDK.getInstance().getVideoHelper().startVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        ZoomVideoSDK.getInstance().getVideoHelper().stopVideo();
    }

    private void initView() {
        display = ((WindowManager) getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof LowerThirdSettingFragment) {
            ((LowerThirdSettingFragment)fragment).setFinishListener(new LowerThirdSettingFragment.OnFinishListener() {
                @Override
                public void onFinishClicked(boolean save) {
                    finish();
                }
            });
        }
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, LowerThirdSettingActivity.class);
        activity.startActivity(intent);
    }
}