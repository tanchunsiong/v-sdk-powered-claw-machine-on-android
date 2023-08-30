package us.zoom.sdksample;


import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.zoom.sdk.ZoomVideoSDKAudioHelper;
import us.zoom.sdk.ZoomVideoSDKChatHelper;
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType;
import us.zoom.sdk.ZoomVideoSDKDelegate;
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper;
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper;
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus;
import us.zoom.sdk.ZoomVideoSDKPasswordHandler;
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler;
import us.zoom.sdk.ZoomVideoSDKRawDataPipe;
import us.zoom.sdk.ZoomVideoSDKRecordingStatus;
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason;
import us.zoom.sdk.ZoomVideoSDKPhoneStatus;
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoCanvas;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus;
import us.zoom.sdk.ZoomVideoSDKShareStatus;

public class AudioRawDataUtil {

    static final String TAG = "AudioRawDataUtil";

    private Map<String, FileChannel> map = new HashMap<>();

    private Context mContext;


    public AudioRawDataUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    private FileChannel createFileChannel(String name) {
        String path=mContext.getExternalCacheDir().getAbsolutePath()+ "/audiorawdata/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = path + name + ".pcm";
        File file = new File(fileName);
        try {
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            FileChannel fileChannel = fileOutputStream.getChannel();

            return fileChannel;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private ZoomVideoSDKDelegate dataDelegate = new SimpleVideoSDKDelegate() {

        @Override
        public void onMixedAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {
            Log.d(TAG,"onMixedAudioRawDataReceived:"+rawData);
            saveAudioRawData(rawData, ZoomVideoSDK.getInstance().getSession().getMySelf().getUserName());

        }

        public void onOneWayAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData, ZoomVideoSDKUser user) {
            Log.d(TAG,"onOneWayAudioRawDataReceived:"+rawData);
            saveAudioRawData(rawData, user.getUserName());
        }


        @Override
        public void onShareAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {
            Log.d(TAG,"onShareAudioRawDataReceived:"+rawData);
            saveAudioRawData(rawData,"share");
        }

    };

    public void saveAudioRawData(ZoomVideoSDKAudioRawData rawData, String fileName) {
        try {

            FileChannel fileChannel = map.get(fileName);
            if (null == fileChannel) {
                fileChannel = createFileChannel(fileName);
                map.put(fileName, fileChannel);
            }
            if (null != fileChannel) {
                fileChannel.write(rawData.getBuffer(), rawData.getBufferLen());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void subscribeAudio() {

        ZoomVideoSDK.getInstance().getAudioHelper().subscribe();
        ZoomVideoSDK.getInstance().addListener(dataDelegate);
    }

    public void unSubscribe() {
        ZoomVideoSDK.getInstance().removeListener(dataDelegate);

        for (FileChannel fileChannel : map.values()) {
            if (null != fileChannel) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ZoomVideoSDK.getInstance().getAudioHelper().unSubscribe();
    }
}
