package us.zoom.sdksample.rawdata;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

import us.zoom.sdk.ExternalSourceDataFormat;
import us.zoom.sdk.ZoomVideoSDKVideoCapability;
import us.zoom.sdk.ZoomVideoSDKVideoSender;
import us.zoom.sdk.ZoomVideoSDKVideoSource;

public class VirtualVideoSource implements ZoomVideoSDKVideoSource {

    private static final String TAG = "VirtualVideoSource";

    private HandlerThread virtualSourceThread;

    private Handler virtualHandler;

    private ZoomVideoSDKVideoSender sender;

    private int fps = 25;

    private VideoFrame frame;

    private VideoFrame frameTwo;

    private String mVideoPath_FULL = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/zoom_640x480_full.yuv";

    private String mVideoPath_640_FULL = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/zoom_640x480_full.yuv";
    private String mVideoPath_720_FULL = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/zoom_1280x720_full.yuv";
    private String mVideoPath_1080_FULL = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/football_1920x1080_i420_full.yuv";

    private String mVideoPath_LIMIE = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/football_1920x1080_i420.yuv";

    private int size=1080;

    private ExternalSourceDataFormat format = ExternalSourceDataFormat.ExternalSourceDataFormat_I420_Limited;

    private boolean isStop = false;

    public VirtualVideoSource() {
        virtualSourceThread = new HandlerThread("VirtualSource");
        virtualSourceThread.start();
        virtualHandler = new Handler(virtualSourceThread.getLooper());
//        File file = new File(mVideoPath);
//        if (!file.exists() || file.length() <= 100) {
//            copyFile();
//        }
    }

//    private void copyFile() {
//        InputStream inputStream = null;
//        FileOutputStream fileOutputStream = null;
//        try {
//            inputStream = SDKApplication.getInstance().getResources().openRawResource(R.raw.video_640x480);
//            fileOutputStream = new FileOutputStream(mVideoPath);
//            byte[] buffer = new byte[1024];
//            while (inputStream.read(buffer) != -1) {
//                fileOutputStream.write(buffer);
//            }
//
//        } catch (Exception e) {
//
//        } finally {
//            try {
//                if (null != fileOutputStream) {
//                    fileOutputStream.close();
//                }
//                if (null != inputStream) {
//                    inputStream.close();
//                }
//            } catch (Exception e) {
//
//            }
//        }
//    }

    @Override
    public void onInitialize(ZoomVideoSDKVideoSender sender, List<ZoomVideoSDKVideoCapability> support_cap_list, ZoomVideoSDKVideoCapability suggest_cap) {
        this.sender = sender;
        fps=suggest_cap.getFrame();
        virtualHandler.post(new Runnable() {
            @Override
            public void run() {
//                Context context = MyApplication.getInstance();
//                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frame_one);
//                byte[] bytes = YUVConvert.convertBitmapToYuv(bitmap);
//                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
//                byteBuffer.put(bytes);
//                frame = new VideoFrame(byteBuffer, bitmap.getWidth(), bitmap.getHeight());
//
//
//                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frame_two);
//                bytes = YUVConvert.convertBitmapToYuv(bitmap);
//                byteBuffer = ByteBuffer.allocateDirect(bytes.length);
//                byteBuffer.put(bytes);
//                frameTwo = new VideoFrame(byteBuffer, bitmap.getWidth(), bitmap.getHeight());
            }
        });
        Log.d(TAG, "onInitialize");
    }


    private VideoFrame readYUVFromStore(String path, int frameIndex, int width, int height) {
        RandomAccessFile randomAccessFile = null;
        if (!new File(path).exists()) {
            return null;
        }
        try {
            if (frameIndex < 0) {
                frameIndex = 0;
            }
            randomAccessFile = new RandomAccessFile(path, "r");
            int size = width * height * 3 / 2;
            long position = ((long) size) * frameIndex;
            if (randomAccessFile.getChannel().size() - size < position) {
                mFrameIndex = 0;
                position = 0;
            }
            randomAccessFile.getChannel().position(position);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
            randomAccessFile.getChannel().read(byteBuffer);
            VideoFrame yuvFrame = new VideoFrame(byteBuffer, width, height);
            return yuvFrame;

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (null != randomAccessFile) {
                    randomAccessFile.getChannel().close();
                    randomAccessFile.close();
                }
            } catch (Exception e) {

            }
        }
        return null;
    }

    @Override
    public void onPropertyChange(List<ZoomVideoSDKVideoCapability> support_cap_list, ZoomVideoSDKVideoCapability suggest_cap) {
        Log.d(TAG, "onPropertyChange");
        fps = suggest_cap.getFrame();
    }

    @Override
    public void onStartSend() {
        isStop = false;
        virtualHandler.removeCallbacks(frameTask);
        virtualHandler.postDelayed(frameTask, 1000 / fps);
        Log.d(TAG, "onStartSend:" + this);
    }

    @Override
    public void onStopSend() {
        isStop = true;
        Log.d(TAG, "onStopSend:" + this);
        virtualHandler.removeCallbacks(frameTask);
    }

    @Override
    public void onUninitialized() {
        Log.d(TAG, "onUninitialized");
        isStop = true;
        virtualHandler.post(new Runnable() {
            @Override
            public void run() {
                sender = null;
            }
        });
        virtualHandler.removeCallbacks(frameTask);
    }


    class VideoFrame {
        ByteBuffer byteBuffer;
        int width;
        int height;

        public VideoFrame(ByteBuffer byteBuffer, int width, int height) {
            this.byteBuffer = byteBuffer;
            this.width = width;
            this.height = height;
        }
    }


    int mFrameIndex = 0;
    Runnable frameTask = new Runnable() {
        @Override
        public void run() {
            if (!isStop && null != sender) {
                int width=640;
                int height=480;
                mVideoPath_FULL = mVideoPath_640_FULL;
                if (size == 720) {
                    mVideoPath_FULL = mVideoPath_720_FULL;
                    width = 1280;
                    height = 720;
                } else if (size == 1080) {
                    mVideoPath_FULL = mVideoPath_1080_FULL;
                    width = 1920;
                    height = 1080;
                }
                String videoPath = "";
                switch (format) {
                    case ExternalSourceDataFormat_I420_Limited:
                        videoPath = mVideoPath_LIMIE;
                        break;
                    case ExternalSourceDataFormat_I420_Full:
                        videoPath = mVideoPath_FULL;
                        break;
                }
                VideoFrame yuvFrame = readYUVFromStore(videoPath, mFrameIndex, width, height);
                VideoFrame paddingFrame;
//                Log.d(TAG,"mFrameIndex："+mFrameIndex+" :"+VirtualVideoSource.this);
                if (null != yuvFrame) {
                    mFrameIndex++;
                    paddingFrame = yuvFrame;
                } else {
                    if ((System.currentTimeMillis() / 1000) % 2 == 0) {
                        paddingFrame = frame;
                    } else {
                        paddingFrame = frameTwo;
                    }
                }
                if (null != paddingFrame) {
                    if (null != sender) {
                        sender.sendVideoFrame(paddingFrame.byteBuffer, paddingFrame.width, paddingFrame.height, paddingFrame.byteBuffer.capacity(), 0, format);
                    }
                }

                virtualHandler.postDelayed(frameTask, 1000 / fps);
            }
        }
    };


}
