package us.zoom.sdksample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdksample.util.SharePreferenceUtil;

public class SettingActivity extends AppCompatActivity {
    protected final static int REQUEST_VIDEO_CODE = 1000;

    private TextView lowerStateTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        TextView leftView = findViewById(R.id.tvBack);
        leftView.setText(getString(R.string.actionbar_done));
        leftView.setTextColor(getResources().getColor(R.color.done_text));
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.title)).setText(R.string.setting_title);
        ((TextView) findViewById(R.id.text_version)).setText(getString(R.string.launch_setting_version, ZoomVideoSDK.getInstance().getSDKVersion()));
        lowerStateTv = findViewById(R.id.lowerStateTv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean lowerStateOn = SharePreferenceUtil.readBoolean(this, LowerThirdSettingFragment.LOWER_THIRD_KEY, false);
        lowerStateTv.setText(getString(lowerStateOn ? R.string.on : R.string.off));
    }

    public void onClickClearLog(View view) {
        File file = new File("/sdcard/Android/data/" + getPackageName() + "/logs");
        if (file.exists()) {
            for (File item : file.listFiles()) {
                try {
                    item.delete();
                } catch (Exception e) {
                }
            }
            boolean result = file.delete();
            if (result) {
                Toast.makeText(this, "Clear success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Clear Fail", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No log files found", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSendLog(View view) {
        Intent email = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        File file = new File("/sdcard/Android/data/" + getPackageName() + "/logs");
        email.setType("application/octet-stream");
        String emailTitle = getString(R.string.app_name) + "_logs";
        email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailTitle);
        email.putExtra(android.content.Intent.EXTRA_TEXT, "attach only");
        ArrayList<Parcelable> uris = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null){
            Toast.makeText(this, "No log files found", Toast.LENGTH_SHORT).show();
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff < 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }

            public boolean equals(Object obj) {
                return true;
            }
        });

        boolean containUtil = true;
        if (files.length > 10) {
            containUtil = false;
        } else {
            long size = 0;
            for (File item : files) {
                size += item.getFreeSpace();
                if (size > 10 * 1024 * 1024) {
                    containUtil = false;
                    break;
                }
            }
        }
        for (File item : files) {
            if (item.getName().endsWith(".log")) {
                if (!containUtil && item.getName().startsWith("util")) {
                    continue;
                }
                Uri contentUri = FileProvider.getUriForFile(this, "us.zoom.VideoSDKPlaygroud.fileProvider", item);
                uris.add(contentUri);
            }
        }
        email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(email, "Send Email"));
    }

    public void onClickLowerThird(View view) {
        if (requestPermission()) {
            LowerThirdSettingActivity.startActivity(this);
        }
    }

    protected boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_VIDEO_CODE);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_VIDEO_CODE) {
            if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                LowerThirdSettingActivity.startActivity(this);
            }
        }
    }
}
