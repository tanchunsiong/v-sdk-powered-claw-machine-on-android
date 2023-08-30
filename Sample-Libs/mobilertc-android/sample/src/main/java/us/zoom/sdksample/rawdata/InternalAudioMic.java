package us.zoom.sdksample.rawdata;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.nio.ByteBuffer;

import us.zoom.sdk.ZoomVideoSDKAudioRawDataSender;
import us.zoom.sdk.ZoomVideoSDKVirtualAudioMic;

public class InternalAudioMic implements ZoomVideoSDKVirtualAudioMic {

    private static final String TAG = "InternalAudioMic";

    private ZoomVideoSDKAudioRawDataSender sdkAudioRawDataSender;

    private Handler recordHandler;

    private static final int SAMPLE_RATE = 44100;//44.1K

    private ByteBuffer _recBuffer;
    private byte[] _tempBufRec;

    private int mRecBufSize;

    private AudioRecord _audioRecord;

    private volatile boolean isStop = false;

    private AudioManager _audioManager;

    public InternalAudioMic(Context context) {
        HandlerThread handlerThread = new HandlerThread("AudioThread");
        handlerThread.start();
        recordHandler = new Handler(handlerThread.getLooper());

        _audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onMicInitialize(ZoomVideoSDKAudioRawDataSender sender) {
        sdkAudioRawDataSender = sender;
        initRecord();
    }

    @Override
    public void onMicStartSend() {
        isStop = false;
        startRecord();
        recordHandler.post(audioTask);
    }

    @Override
    public void onMicStopSend() {
        recordHandler.removeCallbacks(audioTask);
        isStop = true;
        stopRecord();
    }

    @Override
    public void onMicUninitialized() {
        recordHandler.removeCallbacks(audioTask);
        isStop = true;
        sdkAudioRawDataSender = null;
        releaseRecord();
    }

    private boolean _doRecInit = true;
    Runnable audioTask = new Runnable() {
        @Override
        public void run() {
            if (_doRecInit) {
                try {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Throwable e) {
                    Log.d(TAG, "Set rec thread priority failed: " + e.getMessage());
                }
                _doRecInit = false;
            }

            while (!isStop && null != _audioRecord) {
                int lengthInBytes = 2 * (SAMPLE_RATE / 100);
                _recBuffer.rewind();
                int readBytes = _audioRecord.read(_tempBufRec, 0, lengthInBytes);
                _recBuffer.put(_tempBufRec);

                int duration = 0;
                if (readBytes != lengthInBytes) {
                    Log.d(TAG, "Could not read all data from sc (read = " + readBytes + ", length = " + lengthInBytes + ")");
                } else {
                    if (null != sdkAudioRawDataSender) {
                        duration = sdkAudioRawDataSender.send(_recBuffer, lengthInBytes, SAMPLE_RATE);
                        Log.d(TAG, "send duration:" + duration + " length:" + lengthInBytes);
                    }
                }
            }
        }
    };

    void initRecord() {

        stopRecord();

        _recBuffer = ByteBuffer.allocateDirect(2 * SAMPLE_RATE/100); // Max 10 ms
        _tempBufRec = new byte[2 * SAMPLE_RATE/100];

        int minRecBufSize =
                AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        // AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

        mRecBufSize = minRecBufSize * 2;
        _audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                mRecBufSize);
    }

    void startRecord() {
        if (null != _audioRecord) {
            _audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            _audioRecord.startRecording();
        }
    }

    void stopRecord() {
        if (null != _audioRecord) {
            _audioRecord.stop();
        }
    }

    void releaseRecord() {
        if (null != _audioRecord) {
            _audioRecord.release();
            _audioRecord = null;
        }
    }
}
