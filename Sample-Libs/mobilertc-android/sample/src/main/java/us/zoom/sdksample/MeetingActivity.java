package us.zoom.sdksample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKRawDataPipeDelegate;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoResolution;
import us.zoom.sdk.ZoomVideoSDKVideoView;
import us.zoom.sdksample.cmd.CmdFeedbackPushRequest;
import us.zoom.sdksample.cmd.CmdHandler;
import us.zoom.sdksample.cmd.CmdHelper;
import us.zoom.sdksample.cmd.CmdLowerThirdRequest;
import us.zoom.sdksample.cmd.CmdRequest;
import us.zoom.sdksample.feedback.data.FeedbackDataManager;
import us.zoom.sdksample.feedback.view.FeedbackSubmitDialog;
import us.zoom.sdksample.rawdata.RawDataRenderer;
import us.zoom.sdksample.util.SharePreferenceUtil;


public class MeetingActivity extends BaseMeetingActivity implements RawDataRenderer.RawDataStatusChangedDelegate {

    private static final String TAG = "MeetingActivity";

    ZoomVideoSDKVideoView zoomCanvas;

    View annotationView;

    RawDataRenderer rawDataRenderer;

    private FrameLayout videoContain;

    private AudioRawDataUtil audioRawDataUtil;
    private CmdHandler mFeedbackPushHandler = new CmdHandler() {
        @Override
        public void onCmdReceived(CmdRequest request) {
            if (request instanceof CmdFeedbackPushRequest) {
                FeedbackSubmitDialog.show(MeetingActivity.this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioRawDataUtil = new AudioRawDataUtil(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        CmdHelper.getInstance().addListener(mFeedbackPushHandler);
        FeedbackDataManager.getInstance().startListenerFeedbackData();
    }

    @Override
    public void onSessionJoin() {
        super.onSessionJoin();
//        audioRawDataUtil.subscribeAudio();
        startMeetingService();
    }

    @Override
    public void onCommandChannelConnectResult(boolean isSuccess) {
        super.onCommandChannelConnectResult(isSuccess);
        if (isSuccess) {
            String name = SharePreferenceUtil.readString(this, LowerThirdSettingFragment.NAME_KEY, "");
            if (name != null && !name.isEmpty()) {
                String company = SharePreferenceUtil.readString(this, LowerThirdSettingFragment.COMPANY_KEY, "");
                int rgbIndex = SharePreferenceUtil.readInt(this, LowerThirdSettingFragment.RGB_KEY, 0);

                final CmdLowerThirdRequest request = new CmdLowerThirdRequest();
                request.user = null;
                request.name = name;
                request.companyName = company;
                request.rgb = CmdLowerThirdRequest.getColorTypeFromIndex(rgbIndex);

                CmdHelper.getInstance().sendCommand(request);
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected:");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected:");
        }
    };

    private void startMeetingService() {
        Intent intent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        if (null != serviceConnection) {
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void stopMeetingService() {
        Intent intent = new Intent(this, NotificationService.class);
        stopService(intent);
        try {
            if (null != serviceConnection) {
                unbindService(serviceConnection);
            }
            serviceConnection = null;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onSessionLeave() {
        super.onSessionLeave();
        audioRawDataUtil.unSubscribe();
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
    }

    @Override
    public void finish() {
        super.finish();
        stopMeetingService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CmdHelper.getInstance().removeListener(emojiHandler);
        CmdHelper.getInstance().removeListener(lowerThirdHandler);
//        stopMeetingService();
        handler.removeCallbacks(runnable);
        CmdHelper.getInstance().removeListener(mFeedbackPushHandler);
        FeedbackDataManager.getInstance().stopListenerFeedbackData();
        FeedbackDataManager.getInstance().clear();
    }

    @Override
    protected void initView() {
        super.initView();
        videoContain = findViewById(R.id.big_video_contain);
        videoContain.setOnClickListener(onEmptyContentClick);
        chatListView.setOnClickListener(onEmptyContentClick);
    }

    View.OnClickListener onEmptyContentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!ZoomVideoSDK.getInstance().isInSession()) {
                return;
            }
            boolean isShow = actionBar.getVisibility() == View.VISIBLE;
            toggleView(!isShow);
//            if (BuildConfig.DEBUG) {
//                changeResolution();
//            }
        }
    };

    private int resolution = -1;
    private int aspectMode = -1;

    private void changeResolution() {
        resolution++;
        if (resolution > ZoomVideoSDKVideoResolution.VideoResolution_1080P.getValue()) {
            resolution = 0;
        }
        aspectMode++;
        if (aspectMode >= ZoomVideoSDKVideoAspect.values().length) {
            aspectMode = 0;
        }
        ZoomVideoSDKVideoResolution size = ZoomVideoSDKVideoResolution.fromValue(resolution);
        Log.d(TAG, "changeResolution:" + size + " aspectMode:" + aspectMode);
        if (renderType == RENDER_TYPE_OPENGLES) {
            if (null == currentShareUser && null != mActiveUser) {
                mActiveUser.getVideoPipe().subscribe(size, rawDataRenderer);
            }
        } else if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            mActiveUser.getVideoCanvas().setResolution(zoomCanvas, size);
            mActiveUser.getVideoCanvas().setAspectMode(zoomCanvas, ZoomVideoSDKVideoAspect.values()[aspectMode]);
        }
    }

    @Override
    public void onItemClick() {
        if (!ZoomVideoSDK.getInstance().isInSession()) {
            return;
        }
        boolean isShow = actionBar.getVisibility() == View.VISIBLE;
        toggleView(!isShow);
    }

    protected void toggleView(boolean show) {
        if (!show) {
            if (keyBoardLayout.isKeyBoardShow()) {
                keyBoardLayout.dismissChat(true);
                return;
            }
        }
        actionBar.setVisibility(show ? View.VISIBLE : View.GONE);
        chatListView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initMeeting() {
        ZoomVideoSDK.getInstance().addListener(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            zoomCanvas = new ZoomVideoSDKVideoView(this, !renderWithSurfaceView);
            videoContain.addView(zoomCanvas, 0, params);
        } else {
            rawDataRenderer = new RawDataRenderer(this);
            videoContain.addView(rawDataRenderer, 0, params);
        }

        ZoomVideoSDKUser mySelf = ZoomVideoSDK.getInstance().getSession().getMySelf();
        subscribeVideoByUser(mySelf);
        refreshFps();
        CmdHelper.getInstance().addListener(lowerThirdHandler);
        CmdHelper.getInstance().addListener(emojiHandler);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (null != mActiveUser) {
                if (mActiveUser == currentShareUser) {
                    updateFps(mActiveUser.getShareStatisticInfo());
                } else {
                    updateFps(mActiveUser.getVideoStatisticInfo());
                }
            }

            refreshFps();
        }
    };

    private void refreshFps() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 500);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_meeting;
    }


    @Override
    public void onClickSwitchShare(View view) {
        if (null != currentShareUser) {
            updateVideoAvatar(true);
            subscribeShareByUser(currentShareUser);
            selectAndScrollToUser(mActiveUser);
        }
    }

    protected void unSubscribe() {
        if (null != currentShareUser) {
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                currentShareUser.getVideoCanvas().unSubscribe(zoomCanvas);
                currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
            } else {
                currentShareUser.getVideoPipe().unSubscribe(rawDataRenderer);
            }
        }

        if (null != mActiveUser) {
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
                mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
            } else {
                mActiveUser.getVideoPipe().unSubscribe(rawDataRenderer);
            }
        }

        if (annotate_test) {
            if (annotationView != null) {
                videoContain.removeView(annotationView);
            }
        }
    }

    protected void subscribeVideoByUser(ZoomVideoSDKUser user) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            ZoomVideoSDKVideoAspect aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_LetterBox;
            if (ZoomVideoSDK.getInstance().isInSession()) {
                aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original;
            }
            if (null != currentShareUser) {
                currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
            }
            user.getVideoCanvas().unSubscribe(zoomCanvas);
            int ret = user.getVideoCanvas().subscribe(zoomCanvas, aspect);
            if (ret != ZoomVideoSDKErrors.Errors_Success) {
                Toast.makeText(this, "subscribe error:" + ret, Toast.LENGTH_LONG).show();
            }
        } else {
            if (ZoomVideoSDK.getInstance().isInSession()) {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original);
            } else {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Full_Filled);
            }
            if (null != currentShareUser) {
                currentShareUser.getSharePipe().unSubscribe(rawDataRenderer);
            }
            user.getVideoPipe().unSubscribe(rawDataRenderer);
            int ret = user.getVideoPipe().subscribe(ZoomVideoSDKVideoResolution.VideoResolution_720P, rawDataRenderer);
            rawDataRenderer.setDelegate(this);
            rawDataRenderer.setUser(user);
            if (ret != ZoomVideoSDKErrors.Errors_Success) {
                Toast.makeText(this, "subscribe error:" + ret, Toast.LENGTH_LONG).show();
            }
        }
        mActiveUser = user;
        onUserActive(mActiveUser);

        if (null != user.getVideoStatus()) {
            updateVideoAvatar(user.getVideoStatus().isOn());
        }

        if (null != currentShareUser) {
            btnViewShare.setVisibility(View.VISIBLE);
        } else {
            btnViewShare.setVisibility(View.GONE);
        }

        if (annotate_test) {
            if (annotationView != null) {
                videoContain.removeView(annotationView);
                annotationView = null;
                annotateToolbar.setVisibility(View.GONE);
            }
        }
    }


    protected void subscribeShareByUser(ZoomVideoSDKUser user) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            if (null != mActiveUser) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
                mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
            }

            if (ZoomVideoSDK.getInstance().getShareHelper().isOtherSharing()) {
                user.getShareCanvas().subscribe(zoomCanvas, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);

                if (annotate_test) {
                    if (!isActivityPaused) {
                        ZoomVideoSDKAnnotationHelper annotationHelper = getAnnotationHelper(zoomCanvas);
                        if (annotationHelper != null) {
                            annotationView = annotationHelper.getAnnotationView();
                            if (annotationView != null && annotationView.getParent() == null) {
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                                videoContain.addView(annotationView, params);
                                annotateToolbar.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        } else {
            rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original);
            rawDataRenderer.subscribe(user, ZoomVideoSDKVideoResolution.VideoResolution_1080P, true);
        }

        mActiveUser = user;
        onUserActive(mActiveUser);
        btnViewShare.setVisibility(View.GONE);
    }

    public void onRawDataStatusChanged(ZoomVideoSDKRawDataPipeDelegate.RawDataStatus status, ZoomVideoSDKUser user) {
        if (user == mActiveUser) {
            if (status == ZoomVideoSDKRawDataPipeDelegate.RawDataStatus.RawData_On) {
                refreshRotation();
            }
            updateVideoAvatar(status == ZoomVideoSDKRawDataPipeDelegate.RawDataStatus.RawData_On);
        }
    }

    private void updateVideoAvatar(boolean isOn) {
        if (!ZoomVideoSDK.getInstance().isInSession()) {
            isOn = true;//show preview
        }
        if (isOn) {
            videoOffView.setVisibility(View.GONE);
        } else {
            videoOffView.setVisibility(View.VISIBLE);
            text_fps.setVisibility(View.GONE);
            videoOffView.setImageResource(R.drawable.zm_conf_no_avatar);
        }
    }


    @Override
    public void onUserLeave(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {
        super.onUserLeave(userHelper, userList);
        if (null == mActiveUser || userList.contains(mActiveUser)) {
            subscribeVideoByUser(session.getMySelf());
            selectAndScrollToUser(session.getMySelf());
        }
    }

    @Override
    public void onUserVideoStatusChanged(ZoomVideoSDKVideoHelper videoHelper, List<ZoomVideoSDKUser> userList) {
        super.onUserVideoStatusChanged(videoHelper, userList);

        if (null != mActiveUser && userList.contains(mActiveUser)) {
            updateVideoAvatar(mActiveUser.getVideoStatus().isOn());
//            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
//                if (null==currentShareUser&&mActiveUser.getVideoStatus().isOn()) {
//                    subscribeVideoByUser(mActiveUser);
//                    adapter.notifyDataSetChanged();
//                }
//            }
        }
    }

    @Override
    protected void onStartShareView() {
        super.onStartShareView();
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            if (null != mActiveUser) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
            }
        } else {
            rawDataRenderer.unSubscribe();
        }
        adapter.clear(false);
    }

    @Override
    public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {
        super.onUserShareStatusChanged(shareHelper, userInfo, status);
        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            if (userInfo != session.getMySelf()) {
                subscribeShareByUser(userInfo);
                updateVideoAvatar(true);
                selectAndScrollToUser(userInfo);
                annotateToolbar.setRenderView(zoomCanvas);
            } else {
                if (!ZoomVideoSDK.getInstance().getShareHelper().isScreenSharingOut()) {
                    unSubscribe();
                    adapter.clear(false);
                }
                annotateToolbar.setRenderView(null);
            }
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            if (currentShareUser == userInfo) {
                currentShareUser = null;
            }
            subscribeVideoByUser(userInfo);
            if (adapter.getItemCount() == 0) {
                adapter.addAll();
            }
            selectAndScrollToUser(userInfo);
        }
    }


    @Override
    public void onBackPressed() {
    }

    @Override
    protected ZoomVideoSDKRawDataPipeDelegate getMultiStreamDelegate() {
        return rawDataRenderer;
    }

    @Override
    protected ZoomVideoSDKVideoView getMultiStreamVideoView() {
        return zoomCanvas;
    }
}
