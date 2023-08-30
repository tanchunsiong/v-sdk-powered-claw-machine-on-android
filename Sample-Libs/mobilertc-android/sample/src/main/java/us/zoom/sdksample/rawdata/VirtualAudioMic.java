package us.zoom.sdksample.rawdata;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import us.zoom.sdk.ZoomVideoSDKAudioRawDataSender;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKVirtualAudioMic;

public class VirtualAudioMic implements ZoomVideoSDKVirtualAudioMic {
    final static String TAG = "VirtualAudioMic";

    private final int AUDIO_SIMPLE = 48 * 1000;

    private final String audioPath = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/demo_audio.wav";

    private RandomAccessFile randomAccessFile = null;

    private int offset = 0;

    private Handler virtualHandler;

    private boolean isStop = false;

    private ZoomVideoSDKAudioRawDataSender sdkAudioRawDataSender;

    public VirtualAudioMic() {
        HandlerThread handlerThread = new HandlerThread("VirtualMicThread");
        handlerThread.start();
        virtualHandler = new Handler(handlerThread.getLooper());
    }


    @Override
    public void onMicInitialize(ZoomVideoSDKAudioRawDataSender sender) {
        Log.d(TAG, "onMicInitialize:" + sender);
        sdkAudioRawDataSender = sender;
    }

    @Override
    public void onMicStartSend() {
        Log.d(TAG, "onMicStartSend");
        try {
            randomAccessFile = new RandomAccessFile(audioPath, "r");
            offset = 0;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        isStop = false;
        virtualHandler.removeCallbacks(audioTask);
        virtualHandler.postDelayed(audioTask, 10);
    }

    @Override
    public void onMicStopSend() {
        Log.d(TAG, "onMicStopSend");
        isStop = true;
        virtualHandler.removeCallbacks(audioTask);
    }

    @Override
    public void onMicUninitialized() {
        Log.d(TAG, "onMicUninitialized");
        virtualHandler.removeCallbacks(audioTask);
        sdkAudioRawDataSender = null;
        isStop = true;
    }

    Runnable audioTask = new Runnable() {
        @Override
        public void run() {
            if (!isStop && null != sdkAudioRawDataSender) {
                AudioFrame audioFrame = readAudioFrame();
                int error=0;
                long duration=0;
                if (null != audioFrame) {
                    if (null != sdkAudioRawDataSender) {
                        long time = System.currentTimeMillis();
                         error = sdkAudioRawDataSender.send(audioFrame.buffer, audioFrame.length, audioFrame.sample);
                         duration = System.currentTimeMillis() - time;
                        Log.d(TAG, "send:" + error + " duration:" + duration);
                        if (error == ZoomVideoSDKErrors.RawDataError_SEND_TOO_FREQUENTLY) {
                            int delay=10*1024*1024/(audioFrame.length);
                            virtualHandler.postDelayed(audioTask, delay);
                        }
                    }
                }
                if (error != ZoomVideoSDKErrors.RawDataError_SEND_TOO_FREQUENTLY) {
                    virtualHandler.postDelayed(audioTask,10-duration);
                }
            }
            else {
                closeFile();
            }
        }
    };

    void closeFile() {
        if (null != randomAccessFile) {
            try {
                randomAccessFile.close();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                offset = 0;
                randomAccessFile = null;
            }
        }
    }

    class AudioFrame {
        ByteBuffer buffer;
        int sample;
        int length;

        public AudioFrame(ByteBuffer buffer, int sample, int length) {
            this.buffer = buffer;
            this.sample = sample;
            this.length = length;
        }
    }


    private AudioFrame readAudioFrame() {
        if (null == randomAccessFile) {
            return null;
        }
        try {
            final int bufferLength = 960;
            if (offset + bufferLength < randomAccessFile.length()) {
                byte[] buf = new byte[bufferLength];
                int readSize= randomAccessFile.read(buf, 0, bufferLength);
                offset += bufferLength;
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferLength);
                byteBuffer.put(buf);
                AudioFrame audioFrame = new AudioFrame(byteBuffer, AUDIO_SIMPLE, bufferLength);
                return audioFrame;
            } else {
                randomAccessFile.seek(0);
                offset = 0;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

}
