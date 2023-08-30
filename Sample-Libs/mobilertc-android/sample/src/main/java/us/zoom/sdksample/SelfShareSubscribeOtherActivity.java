package us.zoom.sdksample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import us.zoom.sdk.ZoomVideoSDKErrors;
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
import us.zoom.sdk.ZoomVideoSDKSession;
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

public class SelfShareSubscribeOtherActivity extends AppCompatActivity implements ZoomVideoSDKDelegate {

    private static final String TAG = "SelfShareSubscribeOtherActivity";

    public final static int REQUEST_SELECT_ORIGINAL_PIC = 3003;
    private ZoomVideoSDKShareHelper shareHelper;
    ZoomVideoSDKAnnotationHelper annotationHelper;
    private ImageView shareImageView;
    private FrameLayout shareViewGroup;
    private FrameLayout videoContain;
    private TextView textView;

    private View annotateView;
    private AnnotateToolbar annotateToolbar;
    private AnnotateToolbar annotateToolbarForSubscribe;
    private ZoomVideoSDKUser shareUser1;
    private ZoomVideoSDKVideoView videoSDKVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_share_subscribe_other);
        initData();
        initView();
        ZoomVideoSDK.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZoomVideoSDK.getInstance().removeListener(this);
    }

    private void initData() {
        shareHelper = ZoomVideoSDK.getInstance().getShareHelper();
    }

    private void initView() {
        shareImageView = findViewById(R.id.share_image);
        shareViewGroup = findViewById(R.id.share_view_group);
        videoContain = findViewById(R.id.subscribeView);
        videoSDKVideoView = new ZoomVideoSDKVideoView(this, false);
        videoContain.addView(videoSDKVideoView, 0);
        annotateToolbar = findViewById(R.id.annotateToolbar1);
        annotateToolbarForSubscribe = findViewById(R.id.annotateToolbar2);
        textView = findViewById(R.id.selectTv);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getText().equals("start share")) {
                    selectFromGallery();
                } else if (textView.getText().equals("destroy helper")) {
                    if (annotationHelper != null) {
                        shareHelper.destroyAnnotationHelper(annotationHelper);
                    }
                    textView.setText("create helper");
                } else if (textView.getText().equals("create helper")) {
                    if (annotationHelper != null) {
                        shareHelper.destroyAnnotationHelper(annotationHelper);
                    }
                    annotationHelper = shareHelper.createAnnotationHelper(null);
                    if (annotateView != null) {
                        shareViewGroup.removeView(annotateView);
                    }
                    if (annotationHelper != null) {
                        annotateView = annotationHelper.getAnnotationView();
                        if (annotateView != null) {
                            shareViewGroup.addView(annotateView);
                        }
                    }
                    textView.setText("destroy helper");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_ORIGINAL_PIC:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = data.getData();
                        if (null != selectedImage) {
                            shareImageView.setImageURI(selectedImage);
                            shareViewGroup.setVisibility(View.VISIBLE);
                            int ret = ZoomVideoSDK.getInstance().getShareHelper().startShareView(shareImageView);
                            Log.d(TAG, "start share " + ret);
                            if (ret == ZoomVideoSDKErrors.Errors_Success) {
                                textView.setText("destroy helper");
                                if (annotationHelper != null) {
                                    shareHelper.destroyAnnotationHelper(annotationHelper);
                                }
                                if (annotateView != null) {
                                    ((ViewGroup) shareViewGroup).removeView(annotateView);
                                    annotateView = null;
                                }

                                annotationHelper = shareHelper.createAnnotationHelper(null);
                                if (annotationHelper != null) {
                                    annotateView = annotationHelper.getAnnotationView();
                                    if (annotateView != null) {
                                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                                        ((ViewGroup) shareViewGroup).addView(annotateView, params);
                                        annotateToolbar.setRenderView(null);
                                        annotateToolbar.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } else {
                            shareImageView.setImageBitmap(null);
                            shareViewGroup.setVisibility(View.GONE);
                            if (annotateView != null) {
                                ((ViewGroup) shareViewGroup).removeView(annotateView);
                                annotateView = null;
                                annotateToolbar.setVisibility(View.GONE);
                            }
                            boolean isLocked = ZoomVideoSDK.getInstance().getShareHelper().isShareLocked();
                            Toast.makeText(this, "Share Fail isLocked=" + isLocked, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
    }

    public void onClickStopShare(View view) {
        ZoomVideoSDK.getInstance().getShareHelper().stopShare();
    }

    private void selectFromGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_SELECT_ORIGINAL_PIC);
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
    public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {
        ZoomVideoSDKSession session = ZoomVideoSDK.getInstance().getSession();
        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            if (!session.getMySelf().equals(userInfo)) {
                shareUser1 = userInfo;
                shareUser1.getShareCanvas().subscribe(videoSDKVideoView, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
                annotateToolbarForSubscribe.setRenderView(videoSDKVideoView);
                annotateToolbarForSubscribe.setVisibility(View.VISIBLE);
            }
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            if (!session.getMySelf().equals(userInfo)) {
                annotateToolbarForSubscribe.setVisibility(View.GONE);
                //  shareUser1.getShareCanvas().unSubscribe(videoSDKVideoView);
                textView.setText("start share");

                shareImageView.setImageBitmap(null);
                shareViewGroup.setVisibility(View.VISIBLE);
                shareUser1.getShareCanvas().unSubscribe(videoSDKVideoView);
                shareUser1 = null;
            }
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

    @Override
    public void onAnnotationHelperCleanUp(ZoomVideoSDKAnnotationHelper helper) {

    }

    @Override
    public void onAnnotationPrivilegeChange(boolean enable, ZoomVideoSDKUser user) {
        // TODO: 5/11/2023  
    }

    public static void startActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, SelfShareSubscribeOtherActivity.class);
        activity.startActivity(intent);
    }
}
