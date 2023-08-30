package us.zoom.sdksample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoView;

public class FourScreenVideoActivity extends AppCompatActivity{

    FrameLayout videoContain1;
    FrameLayout videoContain2;
    FrameLayout videoContain3;
    FrameLayout videoContain4;

    boolean isVideo1Subscribe = false;
    boolean isVideo2Subscribe = false;
    boolean isVideo3Subscribe = false;
    boolean isVideo4Subscribe = false;

    ZoomVideoSDKVideoView view1;
    ZoomVideoSDKVideoView view2;
    ZoomVideoSDKVideoView view3;
    ZoomVideoSDKVideoView view4;

    ZoomVideoSDKSession session = ZoomVideoSDK.getInstance().getSession();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_screen);
        initView();
    }

    private void initView() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        videoContain1 = findViewById(R.id.big_video_contain1);
        view1 = new ZoomVideoSDKVideoView(this, false);
        videoContain1.addView(view1, 0, params);
        videoContain1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomVideoSDKUser user = session.getMySelf();
                if (user != null) {
                    if (!isVideo1Subscribe) {
                        user.getVideoCanvas().subscribe(view1, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
                    } else {
                        user.getVideoCanvas().unSubscribe(view1);
                    }
                    isVideo1Subscribe = !isVideo1Subscribe;
                }
            }
        });

        videoContain2 = findViewById(R.id.big_video_contain2);
        view2 = new ZoomVideoSDKVideoView(this, true);
        videoContain2.addView(view2, 0, params);
        videoContain2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ZoomVideoSDKUser> userList = session.getRemoteUsers();
                ZoomVideoSDKUser user = null;
                if (userList != null && userList.size() >= 1) {
                    user = userList.get(0);
                }
                if (user != null) {
                    if (!isVideo2Subscribe) {
                        user.getVideoCanvas().subscribe(view2, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
                    } else {
                        user.getVideoCanvas().unSubscribe(view2);
                    }
                    isVideo2Subscribe = !isVideo2Subscribe;
                }
            }
        });

        videoContain3 = findViewById(R.id.big_video_contain3);
        view3 = new ZoomVideoSDKVideoView(this, false);
        videoContain3.addView(view3, 0, params);
        videoContain3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ZoomVideoSDKUser> userList = session.getRemoteUsers();
                ZoomVideoSDKUser user = null;
                if (userList != null && userList.size() >= 2) {
                    user = userList.get(1);
                }
                if (user != null) {
                    if (!isVideo3Subscribe) {
                        user.getVideoCanvas().subscribe(view3, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
                    } else {
                        user.getVideoCanvas().unSubscribe(view3);
                    }
                    isVideo3Subscribe = !isVideo3Subscribe;
                }
            }
        });

        videoContain4 = findViewById(R.id.big_video_contain4);
        view4 = new ZoomVideoSDKVideoView(this, true);
        videoContain4.addView(view4, 0, params);
        videoContain4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ZoomVideoSDKUser> userList = session.getRemoteUsers();
                ZoomVideoSDKUser user = null;
                if (userList != null && userList.size() >= 3) {
                    user = userList.get(2);
                }
                if (user != null) {
                    if (!isVideo4Subscribe) {
                        user.getVideoCanvas().subscribe(view4, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
                    } else {
                        user.getVideoCanvas().unSubscribe(view4);
                    }
                    isVideo4Subscribe = !isVideo4Subscribe;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isVideo1Subscribe) {
            ZoomVideoSDKUser user = session.getMySelf();
            if (user != null) {
                user.getVideoCanvas().unSubscribe(view1);
            }
        }
        List<ZoomVideoSDKUser> userList = session.getRemoteUsers();
        ZoomVideoSDKUser user = null;
        if (isVideo2Subscribe) {
            if (userList != null && userList.size() >= 1) {
                user = userList.get(0);
                user.getVideoCanvas().unSubscribe(view2);
            }
        }
        if (isVideo3Subscribe) {
            if (userList != null && userList.size() >= 2) {
                user = userList.get(1);
                user.getVideoCanvas().unSubscribe(view3);
            }
        }
        if (isVideo4Subscribe) {
            if (userList != null && userList.size() >= 3) {
                user = userList.get(2);
                user.getVideoCanvas().unSubscribe(view4);
            }
        }
    }

    public static void startActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, FourScreenVideoActivity.class);
        activity.startActivity(intent);
    }
}
