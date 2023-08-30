package us.zoom.sdksample.rawdata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import us.zoom.sdk.ExternalSourceDataFormat;
import us.zoom.sdk.ZoomVideoSDKShareSender;
import us.zoom.sdk.ZoomVideoSDKShareSource;
import us.zoom.sdksample.R;
import us.zoom.sdksample.YUVConvert;

public class VirtualShareSource implements ZoomVideoSDKShareSource {

    private static final String TAG = "VirtualShareSource";

    private HandlerThread mVirtualSourceThread;
    private Handler mVirtualHandler;

    private final Object mSenderLockObj = new Object();
    private ZoomVideoSDKShareSender mSender;
    private int mFps = 25;

    private VideoFrame mFrameOne;
    private VideoFrame mFrameTwo;

    private String mVideoFile_Full = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/football_1920x1080_i420_full.yuv";
    private String mVideoFile_LIMITED = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/football_1920x1080_i420.yuv";
    private int mWidth = 1920;
    private int mHeight = 1080;
    private int mFrameIndex = 0;

    private final Context mContext;

    private ExternalSourceDataFormat format = ExternalSourceDataFormat.ExternalSourceDataFormat_I420_Full;

    public VirtualShareSource(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mVirtualSourceThread = new HandlerThread("VirtualSource");
        mVirtualSourceThread.start();
        mVirtualHandler = new Handler(mVirtualSourceThread.getLooper());
        mVirtualHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.frame_one);
                // yuv
                byte[] bytes = YUVConvert.convertBitmapToYuv(bitmap);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
                byteBuffer.put(bytes);
                mFrameOne = new VideoFrame(byteBuffer, bitmap.getWidth(), bitmap.getHeight());

                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.frame_two);
                // yuv
                bytes = YUVConvert.convertBitmapToYuv(bitmap);
                byteBuffer = ByteBuffer.allocateDirect(bytes.length);
                byteBuffer.put(bytes);
                mFrameTwo = new VideoFrame(byteBuffer, bitmap.getWidth(), bitmap.getHeight());
            }
        });
    }

    private VideoFrame readFrame(String filename, int frameIndex, int width, int height) {
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                return null;
//            }
//        }
//        String videoPath = mContext.getExternalFilesDir(null) + File.separator + filename;
        String videoPath = filename;
        RandomAccessFile randomAccessFile = null;
        if (!new File(videoPath).exists()) {
            return null;
        }
        try {
            if (frameIndex < 0) {
                frameIndex = 0;
            }
            randomAccessFile = new RandomAccessFile(videoPath, "r");
            int frameSize = (width * height) + (width * height) / 4 + (width * height) / 4;
            long position = ((long) frameSize) * frameIndex;

            if (randomAccessFile.getChannel().size() - frameSize < position) {
                mFrameIndex = 0;
                position = 0;
            }
            randomAccessFile.getChannel().position(position);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(frameSize);
            randomAccessFile.getChannel().read(byteBuffer);
            VideoFrame videoFrame = new VideoFrame(byteBuffer, width, height);
            return videoFrame;

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.getChannel().close();
                    randomAccessFile.close();
                }
            } catch (Exception e) {

            }
        }
        return null;
    }

    @Override
    public void onShareSendStarted(ZoomVideoSDKShareSender sender) {
        synchronized (mSenderLockObj) {
            this.mSender = sender;
        }
        mVirtualHandler.removeCallbacks(frameTask);
        mVirtualHandler.postDelayed(frameTask, 1000 / mFps);
        Log.d(TAG, "onStartSend");
    }

    @Override
    public void onShareSendStopped() {
        synchronized (mSenderLockObj) {
            this.mSender = null;
        }
        mVirtualHandler.removeCallbacks(frameTask);
        Log.d(TAG, "onStopSend");
    }

    private static class VideoFrame {
        ByteBuffer byteBuffer;
        int width;
        int height;

        public VideoFrame(ByteBuffer byteBuffer, int width, int height) {
            this.byteBuffer = byteBuffer;
            this.width = width;
            this.height = height;
        }
    }

    Runnable frameTask = new Runnable() {
        @Override
        public void run() {
            synchronized (mSenderLockObj) {
                if (mSender == null) {
                    return;
                }
            }

            VideoFrame videoFrame;
            String videoFile = "";
            switch (format) {
                case ExternalSourceDataFormat_I420_Full:
                    videoFile = mVideoFile_Full;
                    break;
                case ExternalSourceDataFormat_I420_Limited:
                    videoFile = mVideoFile_LIMITED;
                    break;
            }
            videoFrame = readFrame(videoFile, mFrameIndex, mWidth, mHeight);
            mFrameIndex++;

            if (videoFrame == null) {
                if ((System.currentTimeMillis() / 1000) % 2 == 0) {
                    videoFrame = mFrameOne;
                } else {
                    videoFrame = mFrameTwo;
                }
            }

            if (videoFrame == null) {
                return;
            }
            synchronized (mSenderLockObj) {
                if (mSender != null) {
                    mSender.sendShareFrame(videoFrame.byteBuffer, videoFrame.width, videoFrame.height, videoFrame.byteBuffer.capacity(), format);
                }
            }
            mVirtualHandler.postDelayed(frameTask, 1000 / mFps);
        }
    };

}
