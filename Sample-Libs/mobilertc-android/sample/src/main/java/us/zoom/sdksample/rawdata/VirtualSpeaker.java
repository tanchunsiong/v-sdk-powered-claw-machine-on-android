package us.zoom.sdksample.rawdata;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKVirtualAudioSpeaker;
import us.zoom.sdksample.AudioRawDataUtil;

public class VirtualSpeaker implements ZoomVideoSDKVirtualAudioSpeaker {

    final static String TAG = "VirtualSpeaker";

    private final int AUDIO_SIMPLE = 32000;

    private AudioRawDataUtil audioRawDataUtil;

    private AudioTrack _audioTrack = null;
    AudioManager _audioManager;

    private HandlerThread handlerThread;

    private Handler handler;

    private boolean enableSaveFile=false;

    public VirtualSpeaker(Context context) {
        audioRawDataUtil = new AudioRawDataUtil(context.getApplicationContext());
        _audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);

        handlerThread=new HandlerThread("SaveAudioThread");
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());

        // get the minimum buffer size that can be used
        int minPlayBufSize =
                AudioTrack.getMinBufferSize(AUDIO_SIMPLE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

        int playBufSize = minPlayBufSize;
        if (playBufSize < 6000) {
            playBufSize *= 2;
        }

        try {
            _audioTrack = new AudioTrack(
                    AudioManager.STREAM_VOICE_CALL,
                    AUDIO_SIMPLE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    playBufSize, AudioTrack.MODE_STREAM
            );
            _audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            _audioTrack.play();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onVirtualSpeakerMixedAudioReceived(ZoomVideoSDKAudioRawData data) {
        Log.d(TAG, "onVirtualSpeakerMixedAudioReceived:" + data.getSampleRate()+" :"+data);
        playAudio(data);
        data.getBuffer().position(0);
        saveAudio(data,"mix_speaker");
    }

    @Override
    public void onVirtualSpeakerOneWayAudioReceived(ZoomVideoSDKAudioRawData data, ZoomVideoSDKUser user) {
        Log.d(TAG, "onVirtualSpeakerOneWayAudioReceived:" + data);
        saveAudio(data,"oneway_speaker_" + user.getUserName());
    }

    @Override
    public void onVirtualSpeakerShareAudioReceived(ZoomVideoSDKAudioRawData data) {
        Log.d(TAG, "onVirtualSpeakerShareAudioReceived:" + data);
        saveAudio(data,"share_speaker");
    }

    void saveAudio(final ZoomVideoSDKAudioRawData data, final String filename) {
        if (!enableSaveFile) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                audioRawDataUtil.saveAudioRawData(data, filename);
            }
        });
    }

    void playAudio(ZoomVideoSDKAudioRawData data) {
        data.getBuffer().position(0);
        int remaining = data.getBuffer().remaining();
        byte[] buffer = new byte[remaining];
        data.getBuffer().get(buffer);
        _audioTrack.write(buffer, 0, remaining);
    }
}
