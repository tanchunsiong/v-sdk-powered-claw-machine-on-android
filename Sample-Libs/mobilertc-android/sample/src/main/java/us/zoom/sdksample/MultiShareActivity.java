package us.zoom.sdksample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper;
import us.zoom.sdk.ZoomVideoSDKAudioHelper;
import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKCRCCallStatus;
import us.zoom.sdk.ZoomVideoSDKChatHelper;
import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType;
import us.zoom.sdk.ZoomVideoSDKChatPrivilegeType;
import us.zoom.sdk.ZoomVideoSDKDelegate;
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper;
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus;
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper;
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus;
import us.zoom.sdk.ZoomVideoSDKNetworkStatus;
import us.zoom.sdk.ZoomVideoSDKPasswordHandler;
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason;
import us.zoom.sdk.ZoomVideoSDKPhoneStatus;
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler;
import us.zoom.sdk.ZoomVideoSDKRawDataPipe;
import us.zoom.sdk.ZoomVideoSDKRecordingConsentHandler;
import us.zoom.sdk.ZoomVideoSDKRecordingStatus;
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoCanvas;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoSubscribeFailReason;
import us.zoom.sdk.ZoomVideoSDKVideoView;
import us.zoom.sdksample.share.AnnotateToolbar;

public class MultiShareActivity extends AppCompatActivity implements ZoomVideoSDKDelegate {

    private ZoomVideoSDKUser shareUser1;

    private FrameLayout videoContain1;
    private FrameLayout videoContain2;
    private View annotationView;
    private ZoomVideoSDKVideoView videoSDKVideoView1;
    private ZoomVideoSDKVideoView videoSDKVideoView2;
    private AnnotateToolbar annotateToolbar1;
    private AnnotateToolbar annotateToolbar2;
    private ZoomVideoSDKAnnotationHelper annotationHelper;
    private ZoomVideoSDKShareHelper shareHelper;

    private TextView selectTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_share);
        initView();
        ZoomVideoSDK.getInstance().addListener(this);
        shareHelper = ZoomVideoSDK.getInstance().getShareHelper();
    }

    private void initView() {
        videoContain1 = findViewById(R.id.shareView1);
        videoContain2 = findViewById(R.id.shareView2);
        videoSDKVideoView1 = new ZoomVideoSDKVideoView(this, false);
        videoContain1.addView(videoSDKVideoView1, 0);
        videoSDKVideoView2 = new ZoomVideoSDKVideoView(this, false);
        videoContain2.addView(videoSDKVideoView2, 0);
        annotateToolbar1 = findViewById(R.id.annotateToolbar1);
        annotateToolbar2 = findViewById(R.id.annotateToolbar2);
        selectTv = findViewById(R.id.selectTv);

        selectTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomVideoSDKVideoView originalView = null;
                ZoomVideoSDKVideoView targetView = null;
                AnnotateToolbar targetAnnotationToolbar = null;
                if (selectTv.getText().equals("1")) {
                    selectTv.setText("2");
                    originalView = videoSDKVideoView1;
                    targetView = videoSDKVideoView2;
                    targetAnnotationToolbar = annotateToolbar2;
                } else {
                    selectTv.setText("1");
                    originalView = videoSDKVideoView2;
                    targetView = videoSDKVideoView1;
                    targetAnnotationToolbar = annotateToolbar1;
                }
                if (annotationHelper != null) {
                    shareHelper.destroyAnnotationHelper(annotationHelper);
                }
                annotationHelper = shareHelper.createAnnotationHelper(targetView);
                if (annotationView != null) {
                    originalView.removeView(annotationView);
                }
                if (annotationHelper != null) {
                    annotationView = annotationHelper.getAnnotationView();
                }
                if (annotationView != null) {
                    targetView.addView(annotationView);
                    targetAnnotationToolbar.updateAnnotationController(annotationHelper);
                }
            }
        });
    }

    @Override
    public void onSessionJoin() {

    }

    @Override
    public void onSessionLeave() {

    }

    @Override
    public void onError(int errorCode) {

    }

    @Override
    public void onUserJoin(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {

    }

    @Override
    public void onUserLeave(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {

    }

    @Override
    public void onUserVideoStatusChanged(ZoomVideoSDKVideoHelper videoHelper, List<ZoomVideoSDKUser> userList) {

    }

    @Override
    public void onUserAudioStatusChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> userList) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (annotationHelper != null) {
            shareHelper.destroyAnnotationHelper(annotationHelper);
        }
        ZoomVideoSDK.getInstance().removeListener(this);
    }

    @Override
    public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {
        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            shareUser1 = userInfo;
            shareUser1.getShareCanvas().subscribe(videoSDKVideoView1, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
            if (annotationHelper != null) {
                shareHelper.destroyAnnotationHelper(annotationHelper);
            }
            annotateToolbar1.setRenderView(videoSDKVideoView1);
            annotateToolbar1.setVisibility(View.VISIBLE);
            shareUser1.getShareCanvas().subscribe(videoSDKVideoView2, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
            annotateToolbar2.setRenderView(videoSDKVideoView2);
            annotateToolbar2.setVisibility(View.VISIBLE);
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            shareUser1.getShareCanvas().unSubscribe(videoSDKVideoView1);
            annotateToolbar1.setVisibility(View.GONE);
            shareUser1.getShareCanvas().unSubscribe(videoSDKVideoView2);
            annotateToolbar2.setVisibility(View.GONE);
            shareUser1 = null;
        }
    }

    @Override
    public void onLiveStreamStatusChanged(ZoomVideoSDKLiveStreamHelper liveStreamHelper, ZoomVideoSDKLiveStreamStatus status) {

    }

    @Override
    public void onChatNewMessageNotify(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatMessage messageItem) {

    }

    @Override
    public void onChatDeleteMessageNotify(ZoomVideoSDKChatHelper chatHelper, String msgID, ZoomVideoSDKChatMessageDeleteType deleteBy) {

    }

    @Override
    public void onChatPrivilegeChanged(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatPrivilegeType currentPrivilege) {

    }

    @Override
    public void onUserHostChanged(ZoomVideoSDKUserHelper userHelper, ZoomVideoSDKUser userInfo) {

    }

    @Override
    public void onUserManagerChanged(ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserNameChanged(ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserActiveAudioChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> list) {

    }

    @Override
    public void onSessionNeedPassword(ZoomVideoSDKPasswordHandler handler) {

    }

    @Override
    public void onSessionPasswordWrong(ZoomVideoSDKPasswordHandler handler) {

    }

    @Override
    public void onMixedAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onOneWayAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData, ZoomVideoSDKUser user) {

    }

    @Override
    public void onShareAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onCommandReceived(ZoomVideoSDKUser sender, String strCmd) {

    }

    @Override
    public void onCommandChannelConnectResult(boolean isSuccess) {

    }

    @Override
    public void onCloudRecordingStatus(ZoomVideoSDKRecordingStatus status, ZoomVideoSDKRecordingConsentHandler handler) {

    }

    @Override
    public void onHostAskUnmute() {

    }

    @Override
    public void onInviteByPhoneStatus(ZoomVideoSDKPhoneStatus status, ZoomVideoSDKPhoneFailedReason reason) {

    }

    @Override
    public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKRawDataPipe videoPipe) {

    }

    @Override
    public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKVideoCanvas canvas) {

    }

    @Override
    public void onLiveTranscriptionStatus(ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionStatus status) {

    }

    @Override
    public void onLiveTranscriptionMsgReceived(String ltMsg, ZoomVideoSDKUser pUser, ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionOperationType type) {

    }

    @Override
    public void onOriginalLanguageMsgReceived(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo messageInfo) {

    }

    @Override
    public void onLiveTranscriptionMsgInfoReceived(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo messageInfo) {

    }

    @Override
    public void onLiveTranscriptionMsgError(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage spokenLanguage, ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage transcriptLanguage) {

    }

    @Override
    public void onProxySettingNotification(ZoomVideoSDKProxySettingHandler handler) {

    }

    @Override
    public void onSSLCertVerifiedFailNotification(ZoomVideoSDKSSLCertificateInfo info) {

    }

    @Override
    public void onCameraControlRequestResult(ZoomVideoSDKUser user, boolean isApproved) {

    }

    @Override
    public void onUserVideoNetworkStatusChanged(ZoomVideoSDKNetworkStatus status, ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserRecordingConsent(ZoomVideoSDKUser user) {

    }

    @Override
    public void onCallCRCDeviceStatusChanged(ZoomVideoSDKCRCCallStatus status) {

    }

    @Override
    public void onVideoCanvasSubscribeFail(ZoomVideoSDKVideoSubscribeFailReason fail_reason, ZoomVideoSDKUser pUser, ZoomVideoSDKVideoView view) {

    }

    @Override
    public void onShareCanvasSubscribeFail(ZoomVideoSDKVideoSubscribeFailReason fail_reason, ZoomVideoSDKUser pUser, ZoomVideoSDKVideoView view) {

    }

    public static void startActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, MultiShareActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onAnnotationHelperCleanUp(ZoomVideoSDKAnnotationHelper helper) {
        if (helper == annotationHelper) {
            annotationHelper = null;
        }
    }

    @Override
    public void onAnnotationPrivilegeChange(boolean enable, ZoomVideoSDKUser user) {
        // TODO: 5/11/2023
    }
}