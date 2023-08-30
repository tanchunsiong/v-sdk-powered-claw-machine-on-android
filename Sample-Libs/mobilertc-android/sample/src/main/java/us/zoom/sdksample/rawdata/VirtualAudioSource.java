package us.zoom.sdksample.rawdata;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import us.zoom.sdk.ZoomVideoSDKAudioChannel;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKShareAudioSender;
import us.zoom.sdk.ZoomVideoSDKShareAudioSource;

public class VirtualAudioSource implements ZoomVideoSDKShareAudioSource {

    final static String TAG = "VirtualAudioSource";

    private final int AUDIO_SIMPLE = 16 * 1000;

    private final String audio_mono_path = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/audio_rawdata_8k_16_mono.pcm";
    private final String audio_stereo_path = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/audio_rawdata_8k_16_stereo.pcm";

    private final String audio_mono_path_16k = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/audio_rawdata_16k_mono.wav";
    private final String audio_stereo_path_16k = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/audio_rawdata_16k_stereo.wav";

    private RandomAccessFile randomAccessFile = null;

    private int offset = 0;

    private Handler virtualHandler;

    private boolean isStop = false;

    private ZoomVideoSDKShareAudioSender sdkAudioRawDataSender;

    private ZoomVideoSDKAudioChannel channel = ZoomVideoSDKAudioChannel.ZoomVideoSDKAudioChannel_Mono;

    public VirtualAudioSource() {
        HandlerThread handlerThread = new HandlerThread("VirtualMicThread");
        handlerThread.start();
        virtualHandler = new Handler(handlerThread.getLooper());
    }


    @Override
    public void onStartSendAudio(ZoomVideoSDKShareAudioSender shareAudioSender) {
        Log.d(TAG, "onStartSendAudio:" + shareAudioSender);
        sdkAudioRawDataSender = shareAudioSender;

        try {
            String audioPath = audio_mono_path;
            if (AUDIO_SIMPLE == 16 * 1000) {
                audioPath = audio_mono_path_16k;
            }
            if (channel == ZoomVideoSDKAudioChannel.ZoomVideoSDKAudioChannel_Stereo) {
                audioPath = audio_stereo_path;
                if (AUDIO_SIMPLE == 16 * 1000) {
                    audioPath = audio_stereo_path_16k;
                }
            }
            randomAccessFile = new RandomAccessFile(audioPath, "r");
            offset = 0;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        virtualHandler.removeCallbacks(audioTask);
        virtualHandler.postDelayed(audioTask, 10);
    }

    @Override
    public void onStopSendAudio() {
        Log.d(TAG, "onStopSendAudio");
        isStop = true;
        virtualHandler.removeCallbacks(audioTask);
    }

    Runnable audioTask = new Runnable() {
        @Override
        public void run() {
            if (!isStop && null != sdkAudioRawDataSender) {
                AudioFrame audioFrame = readAudioFrame();
                int error = ZoomVideoSDKErrors.Errors_Success;
                long duration = 0;
                if (null != audioFrame) {
                    if (null != sdkAudioRawDataSender) {
                        long time = System.currentTimeMillis();
                        error = sdkAudioRawDataSender.sendShareAudio(audioFrame.buffer, audioFrame.length, audioFrame.sample, channel);
                        duration = System.currentTimeMillis() - time;
                        Log.d(TAG, "send:" + error + " duration:" + duration);
                        if (error == ZoomVideoSDKErrors.RawDataError_SEND_TOO_FREQUENTLY) {
                            int delay = 10;
                            offset -= audioFrame.length;
                            virtualHandler.postDelayed(audioTask, delay);
                        }
                    }
                }
                if (error != ZoomVideoSDKErrors.RawDataError_SEND_TOO_FREQUENTLY) {
                    virtualHandler.postDelayed(audioTask, 10 - duration);
                }
            } else {
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
            final int bufferLength = AUDIO_SIMPLE * 2 * 5 / 100;
            if (offset + bufferLength < randomAccessFile.length()) {
                byte[] buf = new byte[bufferLength];
                int readSize = randomAccessFile.read(buf, 0, bufferLength);
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
