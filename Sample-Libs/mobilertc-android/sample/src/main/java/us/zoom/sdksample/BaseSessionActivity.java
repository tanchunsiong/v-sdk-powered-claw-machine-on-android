package us.zoom.sdksample;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import us.zoom.sdk.ZoomVideoSDKAnnotationHelper;
import us.zoom.sdk.ZoomVideoSDKAudioHelper;
import us.zoom.sdk.ZoomVideoSDKCRCCallStatus;
import us.zoom.sdk.ZoomVideoSDKCameraDevice;
import us.zoom.sdk.ZoomVideoSDKChatHelper;
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
import us.zoom.sdk.ZoomVideoSDKPreProcessRawData;
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler;
import us.zoom.sdk.ZoomVideoSDKRawDataPipe;
import us.zoom.sdk.ZoomVideoSDKRecordingConsentHandler;
import us.zoom.sdk.ZoomVideoSDKRecordingStatus;
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason;
import us.zoom.sdk.ZoomVideoSDKPhoneStatus;
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKSessionASVStatisticInfo;
import us.zoom.sdk.ZoomVideoSDKSessionAudioStatisticInfo;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoCanvas;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAudioOption;
import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdk.ZoomVideoSDKSessionContext;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKVideoOption;
import us.zoom.sdk.ZoomVideoSDKVideoSourcePreProcessor;
import us.zoom.sdk.ZoomVideoSDKVideoSubscribeFailReason;
import us.zoom.sdk.ZoomVideoSDKVideoView;
import us.zoom.sdksample.rawdata.VirtualAudioMic;
import us.zoom.sdksample.rawdata.VirtualSpeaker;
import us.zoom.sdksample.rawdata.VirtualVideoSource;
import us.zoom.sdksample.util.NetworkUtil;

import static us.zoom.sdksample.BaseMeetingActivity.RENDER_TYPE_OPENGLES;
import static us.zoom.sdksample.BaseMeetingActivity.RENDER_TYPE_ZOOMRENDERER;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;


public class BaseSessionActivity extends AppCompatActivity implements View.OnClickListener, ZoomVideoSDKDelegate {

    protected String[] defaultNameList = {"Grand Canyon", "Yosemite", "Yellowstone", "Disneyland", "Golden Gate Bridge", "Monument Valley", "Death Valley", "Brooklyn Bridge",
            "Hoover Dam", "Lake Tahoe"};


    protected final static int REQUEST_VIDEO_AUDIO_CODE = 1010;

    protected final static int REQUEST_AUDIO_TEST_CODE = 1011;

    protected static final String TAG = "BaseSessionActivity";

    protected TextView leftView;

    protected TextView titleTextView;

    protected Button btnJoin;
    protected View btnCopy;

    protected EditText sessionEditText;

    protected TextView nameEdit;

    protected EditText passwordEdit;
    protected EditText sessionIdleTimeoutMinsEdit;
    protected TextView mTvRenderer;

    protected int renderType = RENDER_TYPE_ZOOMRENDERER;

    private static final boolean show_audio_test = false;



    //GPIODemo
    private Handler handlerForGPIO;
    private boolean isCarDemo = true; //if set to false, this is for claw machine.

    //GPIODemo
    UsbSerialPort port;

    //GPIODemo
    private void initialisePins() throws IOException {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        }

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        port.open(connection);
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getIntent().getExtras()) {
            renderType = getIntent().getIntExtra("render_type", RENDER_TYPE_ZOOMRENDERER);
        }
        setContentView(R.layout.activity_session);



        init();

        //GPIODemo
        initInputTextbox();

        //GPIODemo
        try {
            initialisePins();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    //GPIODemo
    private void initInputTextbox() {

        TextView sessionNameTextView = findViewById(R.id.session_edit);
        TextView sessionNameTextView2 = findViewById(R.id.password_edit);


        if (isCarDemo) {
            sessionNameTextView.setText("herochun6871");
        } else {
            sessionNameTextView.setText("herochun5104");
        }
        sessionNameTextView2.setText("12345678");
    }

    protected String getDefaultSessionName() {
        Random rand = new Random();
        int index = rand.nextInt(defaultNameList.length);
        return defaultNameList[index];
    }

    protected void init() {
        findViewById(R.id.btn_audio_test).setVisibility(show_audio_test ? View.VISIBLE : View.GONE);

        leftView = findViewById(R.id.tvBack);

        titleTextView = findViewById(R.id.title);
        mTvRenderer = findViewById(R.id.tvRenderer);

        if (null != leftView) {
            leftView.setOnClickListener(this);
        }

        sessionEditText = findViewById(R.id.session_edit);

        String defaultSessionName = getDefaultSessionName().toLowerCase();
        sessionEditText.setText(defaultSessionName);

        sessionEditText.setSelection(0, sessionEditText.getText().length());
        sessionEditText.setTransformationMethod(new ReplacementTransformationMethod() {
            @NonNull
            private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            @NonNull
            private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

            @NonNull
            @Override
            protected char[] getOriginal() {
                return upper;
            }

            @NonNull
            @Override
            protected char[] getReplacement() {
                return lower;
            }
        });

        nameEdit = findViewById(R.id.userName_edit);
        btnCopy = findViewById(R.id.btn_copy);
        if (null != nameEdit) {
            nameEdit.setText(Build.MODEL + "-" + Build.VERSION.SDK_INT);
        }
        passwordEdit = findViewById(R.id.password_edit);
        sessionIdleTimeoutMinsEdit = findViewById(R.id.sessionIdleTimeoutMins_edit);
        btnJoin = findViewById(R.id.btn_join);

        updateJoinButton();
        sessionEditText.addTextChangedListener(textWatcher);
        nameEdit.addTextChangedListener(textWatcher);
        sessionIdleTimeoutMinsEdit.addTextChangedListener(textWatcher);

        sessionEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if (result == EditorInfo.IME_ACTION_DONE) {
                    joinOrCreateSession();
                }
                return false;
            }
        });


        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onPasswordChanged();
            }
        });
        updateSelectedRenderer();
        if (null != ZoomVideoSDK.getInstance()) {
            ZoomVideoSDK.getInstance().addListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionEditText.setSelection(0, sessionEditText.getText().length());
        sessionEditText.requestFocus();
        hasInJoinorCreate = false;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updateJoinButton();
        }
    };

    protected void onPasswordChanged() {
    }

    private void updateJoinButton() {
        if (TextUtils.isEmpty(sessionEditText.getText().toString())) {
            btnCopy.setVisibility(View.GONE);
            btnJoin.setEnabled(false);
            return;
        }
        btnCopy.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(nameEdit.getText().toString())) {
            btnJoin.setEnabled(false);
        } else {
            btnJoin.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != ZoomVideoSDK.getInstance()) {
            ZoomVideoSDK.getInstance().removeListener(this);
        }

    }

    protected void setHeadTile(int id) {
        if (null != titleTextView && id > 0) {
            titleTextView.setText(id);
        }
    }

    protected boolean requestPermission(int code) {

        String[] permissions = new String[]{android.Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{android.Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.POST_NOTIFICATIONS};
        } else if (Build.VERSION.SDK_INT >= 31) {
            permissions = new String[]{android.Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT};
        }
        if (code == REQUEST_AUDIO_TEST_CODE) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO};
            if (Build.VERSION.SDK_INT >= 31) {
                permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT};
            }
        }


        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, code);
                    return false;
                }
            }
            return true;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_VIDEO_AUDIO_CODE) {
            if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    && (Build.VERSION.SDK_INT >= 31 && checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED))) {
                onPermissionGranted();
            }
        }
    }

    protected void onPermissionGranted() {
        joinOrCreateSession();
    }

    private boolean hasInJoinorCreate = false;

    protected void joinOrCreateSession() {
        if (hasInJoinorCreate)
            return;
        if (!requestPermission(REQUEST_VIDEO_AUDIO_CODE))
            return;
        if (!NetworkUtil.hasDataNetwork(this)) {
            Toast.makeText(this, "Connection Failed. Please check your network connection and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (null == ZoomVideoSDK.getInstance()) {
            Toast.makeText(this, "Please initialize SDK", Toast.LENGTH_LONG).show();
            return;
        }

        String sessionName = sessionEditText.getText().toString().toLowerCase().trim();

        if (TextUtils.isEmpty(sessionName)) {
            Toast.makeText(this, "Session name is empty", Toast.LENGTH_LONG).show();
            return;
        }

        ZoomVideoSDKSessionContext sessionContext = new ZoomVideoSDKSessionContext();

        ZoomVideoSDKAudioOption audioOption = new ZoomVideoSDKAudioOption();
        audioOption.connect = true;
        audioOption.mute = false;
        audioOption.isMyVoiceInMix = true;
        sessionContext.audioOption = audioOption;

        ZoomVideoSDKVideoOption videoOption = new ZoomVideoSDKVideoOption();
        videoOption.localVideoOn = true;
        sessionContext.videoOption = videoOption;

        String name = nameEdit.getText().toString();
        if (TextUtils.isEmpty(name)) {
            name = Build.MODEL;
        }

        String password = passwordEdit.getText().toString();

        //Required
        sessionContext.sessionName = sessionName;
        sessionContext.userName = name;

        //GET START
       String token =Constants.TOKEN(sessionName);

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Token is empty", Toast.LENGTH_LONG).show();
            return;
        }
        //GET END
        sessionContext.token = token;

        //Optional
        sessionContext.sessionPassword = password;

        //Optional
        int sessionIdleTimeOutMins = 40;
        try {
            sessionIdleTimeOutMins = Integer.parseInt(sessionIdleTimeoutMinsEdit.getText().toString());
        } catch (Exception e) {

        }
        sessionContext.sessionIdleTimeoutMins = sessionIdleTimeOutMins;
//        sessionContext.preProcessor=new ZoomVideoSDKVideoSourcePreProcessor() {
//            @Override
//            public void onPreProcessRawData(ZoomVideoSDKPreProcessRawData rawData) {
//                Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.zm_watermark_sdk);
//                WaterMarkData data=new WaterMarkData(bitmap.getWidth(),bitmap.getHeight(),YUVConvert.convertBitmapToYuv(bitmap));
//                YUVConvert.addWaterMark(rawData,data,20,20,true);
//            }
//        };

//        sessionContext.externalVideoSource = new VirtualVideoSource();

//        sessionContext.virtualAudioSpeaker=new VirtualSpeaker(this);
//        sessionContext.virtualAudioMic = new VirtualAudioMic();
//        sessionContext.virtualAudioMic = new InternalAudioMic(this);

        ZoomVideoSDKSession session = ZoomVideoSDK.getInstance().joinSession(sessionContext);
        if (null == session) {
            return;
        }
        hasInJoinorCreate = true;
        Intent intent = new Intent(this, MeetingActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("password", password);
        intent.putExtra("sessionName", sessionName);
        intent.putExtra("render_type", renderType);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v == leftView) {
            onBackPressed();
        }
    }


    @Override
    public void onSessionJoin() {
//        logSession();
    }

    void logSession() {
        if (!ZoomVideoSDK.getInstance().isInSession()) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logSession();
            }
        }, 1000);
        ZoomVideoSDKSessionASVStatisticInfo statisticInfo = ZoomVideoSDK.getInstance().getSession().getSessionVideoStatisticInfo();
        ZoomVideoSDKSessionASVStatisticInfo shareStatisticInfo = ZoomVideoSDK.getInstance().getSession().getSessionShareStatisticInfo();
        ZoomVideoSDKSessionAudioStatisticInfo audioStatisticInfo = ZoomVideoSDK.getInstance().getSession().getSessionAudioStatisticInfo();

        Log.d(TAG, statisticInfo + ":" + shareStatisticInfo + ":" + audioStatisticInfo);
    }

    @Override
    public void onSessionLeave() {

    }

    @Override
    public void onError(int errorcode) {
        Toast.makeText(this, "Session error:" + errorcode, Toast.LENGTH_LONG).show();
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

    }

    @Override
    public void onLiveStreamStatusChanged(ZoomVideoSDKLiveStreamHelper liveStreamHelper, ZoomVideoSDKLiveStreamStatus status) {

    }

    @Override
    public void onChatNewMessageNotify(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatMessage messageItem) {
        String receivedtext = messageItem.getContent().toLowerCase();
        if (isCarDemo){
                switch (receivedtext) {
                    case "forward":
                    case "f":
                        System.out.println("Move Forward");
                        moveCarForward();
                        break;
                    case "backward":
                    case "b":
                        System.out.println("Move Backward");
                        moveCarBackward();
                        break;
                    case "left":
                    case "l":
                        System.out.println("Turn Left");
                        moveCarLeft();
                        break;
                    case "right":
                    case "r":
                        System.out.println("Turn Right");
                        moveCarRight();
                        break;
                    case "camera":
                        System.out.println("Camera");
                        switchCamera();
                        break;
                    case "lights":
                    case "light":
                        System.out.println("Lights");
                        toggleCarLights();
                        break;
                    case "rotate":
                        System.out.println("rotate");
                        rotateCamera();
                    default:
                        System.out.println("Unknown Direction");
                }
        }else{
            switch (receivedtext) {
                case "u":
                    System.out.println("Move Claw Up");
                    //moveClawUp();
                    break;
                case "d":
                    System.out.println("Move Claw Down");
                    //moveClawDown();
                    break;
                case "l":
                    System.out.println("Move Claw Left");
                    //moveClawLeft();
                    break;
                case "r":
                    System.out.println("Move Claw Right");
                    //moveClawRight();
                    break;
                case "start":
                case "s":
                    System.out.println("Start Claw Game");
                    //startClawGame();
                    break;
                case "catch":
                case "c":
                    System.out.println("Catch Claw");
                    //catchClaw();
                    break;
                case "camera":
                    System.out.println("Camera");
                    switchCamera();
                case "rotate":
                    System.out.println("rotate");
                    rotateCamera();
                default:
                    System.out.println("Unknown Direction");
            }
        }

    }
    private void switchCamera(){
        if (null != ZoomVideoSDK.getInstance()) {
         List<ZoomVideoSDKCameraDevice> cameraDevices=  ZoomVideoSDK.getInstance().getVideoHelper().getCameraList();


            try
            {
                    ZoomVideoSDK.getInstance().getVideoHelper().switchCamera();
             }
             catch (Exception ex){

             }


        }
    }

    private void rotateCamera(){
        Random rand = new Random();
        int randInt= rand.nextInt(4);
        boolean ret = ZoomVideoSDK.getInstance().getVideoHelper().rotateMyVideo(randInt);
    }

    //GPIODemo
    private void moveCarForward() {

        try{
            port.write("forward\n".getBytes(), 1000);

        }
        catch (Exception ex){
            ex.toString();
        }
    }
    //GPIODemo
    private void moveCarBackward() {

        try{
            port.write("backward\n".getBytes(), 1000);

        }
        catch (Exception ex){
            ex.toString();
        }
    }
    //GPIODemo
    private void moveCarLeft() {

        try{
            port.write("left\n".getBytes(), 1000);

        }
        catch (Exception ex){
            ex.toString();
        }

    }
    //GPIODemo
    private void moveCarRight() {
        try{
            port.write("right\n".getBytes(), 1000);

        }
        catch (Exception ex){
            ex.toString();
        }
    }
    //GPIODemo
    private void toggleCarLights() {
        try{
            port.write("light\n".getBytes(), 1000);

        }
        catch (Exception ex){
            ex.toString();
        }
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
    public void onSessionNeedPassword(ZoomVideoSDKPasswordHandler handler) {

    }


    @Override
    public void onSessionPasswordWrong(ZoomVideoSDKPasswordHandler handler) {
        Toast.makeText(this, R.string.wrong_pass_tips, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onUserActiveAudioChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> list) {

    }

    @Override
    public void onMixedAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onOneWayAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData, ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserManagerChanged(ZoomVideoSDKUser user) {
        Log.d(TAG, "onUserManagerChanged:" + user);
    }

    @Override
    public void onUserNameChanged(ZoomVideoSDKUser user) {
        Log.d(TAG, "onUserNameChanged:" + user);
    }

    public void onClickCopy(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (null == cm)
            return;
        String input = sessionEditText.getText().toString();
        if (TextUtils.isEmpty(input))
            return;
        ClipData mClipData = ClipData.newPlainText("Label", input);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, R.string.copy_tips, Toast.LENGTH_LONG).show();
    }

    private void audioTest() {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.layout_audio_test);
        builder.findViewById(R.id.btn_test_speaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = ZoomVideoSDK.getInstance().getTestAudioDeviceHelper().startSpeakerTest();
                Toast.makeText(BaseSessionActivity.this, "ret:" + ret, Toast.LENGTH_LONG).show();
            }
        });

        final TextView textView = builder.findViewById(R.id.text_progress);
        final TextView stopRecord = builder.findViewById(R.id.btn_recording);
        builder.findViewById(R.id.btn_test_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomVideoSDK.getInstance().getTestAudioDeviceHelper().stopMicTest();
                int ret = ZoomVideoSDK.getInstance().getTestAudioDeviceHelper().startMicTest();
                Toast.makeText(BaseSessionActivity.this, "startMicTestRecording ret:" + ret, Toast.LENGTH_LONG).show();

                if (ret == ZoomVideoSDKErrors.Errors_Permission_RECORD_AUDIO ||
                        ret == ZoomVideoSDKErrors.Errors_Permission_READ_PHONE_STATE ||
                        ret == ZoomVideoSDKErrors.Errors_Permission_BLUETOOTH_CONNECT) {
                    requestPermission(REQUEST_AUDIO_TEST_CODE);
                    return;
                }
                delay = 6;
                stopRecord.setVisibility(View.GONE);
                recording_stop = false;
                delayPlay(textView);
            }
        });

        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setVisibility(View.GONE);
                stopRecord.setVisibility(View.GONE);
                recording_stop = true;
                delay = 0;

            }
        });

        builder.findViewById(R.id.btn_test_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = ZoomVideoSDK.getInstance().getTestAudioDeviceHelper().playMicTest();
                Toast.makeText(BaseSessionActivity.this, "ret:" + ret, Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    Handler handler = new Handler();
    int delay = 10;
    boolean recording_stop = false;

    void delayPlay(TextView textView) {
        if (recording_stop) {
            return;
        }
        if (delay <= 0) {
            textView.setVisibility(View.GONE);
            ((ViewGroup) textView.getParent()).findViewById(R.id.btn_recording)
                    .setVisibility(View.GONE);
            return;
        }
        textView.setVisibility(View.VISIBLE);
        textView.setText("" + delay);
        handlerForGPIO.postDelayed(new Runnable() {
            @Override
            public void run() {
                delay--;
                delayPlay(textView);
            }
        }, 1000);
    }

    public void onClickJoinSession(View view) {
        joinOrCreateSession();
    }

    public void onClickAudioTest(View view) {
        audioTest();
    }


    public void onClickRenderer(View view) {
        if (isRendererOptionsShowing) return;
        isRendererOptionsShowing = true;
        showRendererOptions();
    }


    private void updateSelectedRenderer() {
        mTvRenderer.setText(renderType == RENDER_TYPE_ZOOMRENDERER ? R.string.renderer_option_zoom : R.string.renderer_option_opengl);
    }

    private boolean isRendererOptionsShowing = false;

    private void showRendererOptions() {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_renderer);

        final View llZoom = builder.findViewById(R.id.llZoom);
        final TextView tvZoom = builder.findViewById(R.id.tvZoom);
        final ImageView ivZoom = builder.findViewById(R.id.ivZoom);
        final TextView tvZoomInfo = builder.findViewById(R.id.tvZoomInfo);

        final View llOpengl = builder.findViewById(R.id.llOpengl);
        final TextView tvOpengl = builder.findViewById(R.id.tvOpengl);
        final ImageView ivOpengl = builder.findViewById(R.id.ivOpengl);
        final TextView tvOpenglInfo = builder.findViewById(R.id.tvOpenglInfo);

        tvZoom.setEnabled(renderType == RENDER_TYPE_ZOOMRENDERER);
        tvZoomInfo.setEnabled(renderType == RENDER_TYPE_ZOOMRENDERER);
        tvOpengl.setEnabled(renderType == RENDER_TYPE_OPENGLES);
        tvOpenglInfo.setEnabled(renderType == RENDER_TYPE_OPENGLES);
        ivZoom.setVisibility(renderType == RENDER_TYPE_ZOOMRENDERER ? View.VISIBLE : View.INVISIBLE);
        ivOpengl.setVisibility(renderType == RENDER_TYPE_OPENGLES ? View.VISIBLE : View.INVISIBLE);

        llZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderType = RENDER_TYPE_ZOOMRENDERER;
                builder.dismiss();
                updateSelectedRenderer();
            }
        });
        llOpengl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderType = RENDER_TYPE_OPENGLES;
                builder.dismiss();
                updateSelectedRenderer();
            }
        });


        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isRendererOptionsShowing = false;
            }
        });
        builder.show();
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

    }
}