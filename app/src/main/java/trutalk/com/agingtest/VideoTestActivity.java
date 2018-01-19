package trutalk.com.agingtest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VideoTestActivity extends Activity implements OnClickListener {

    private String TAG = "AgingTest";
    private ImageButton btnplay, btnstop, btnpause;
    private SurfaceView surfaceView;
    private SurfaceView mCameraView;
    private MediaPlayer mediaPlayer;
    private AudioManager mAudioManager;
    private TextView mStartTimeView;
    private Camera camera = null;
    private int position;
    TextView textView1;
    private int preMode = 0;
    private int VIDEO_TEST_TIME = 120; //minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        acquireWakeLock();
        btnplay = (ImageButton) this.findViewById(R.id.btnplay);
        btnstop = (ImageButton) this.findViewById(R.id.btnstop);
        btnpause = (ImageButton) this.findViewById(R.id.btnpause);
        textView1 = (TextView) this.findViewById(R.id.textView1);
        mStartTimeView = (TextView) findViewById(R.id.start_time);
        btnstop.setOnClickListener(this);
        btnplay.setOnClickListener(this);
        btnpause.setOnClickListener(this);
        //new SwitchThread().start();
        new OneSecondThread().start();
        new VideoTestTimeThread().start();
        mediaPlayer = new MediaPlayer();
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        mCameraView = (SurfaceView) findViewById(R.id.cameraView);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preMode = mAudioManager.getMode();
        /*mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);*/
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                textView1.setText("播放完毕!...");
            }
        });
        mediaPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                textView1.setText("播放出错!...");
                return false;
            }
        });
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                textView1.setText("准备就绪!...");
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                textView1.setText("进度拖放完毕!...");
            }
        });


        //设置SurfaceView自己不管理的缓冲区
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //surfaceView.setRotation(90);
        surfaceView.getHolder().addCallback(new Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //if (position>0)
                {
                    try {
                        //开始播放
                        play();
                        //并直接从指定位置开始播放
                        mediaPlayer.seekTo(position);
                        position = 0;
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
            }
        });

        //设置SurfaceView自己不管理的缓冲区
        mCameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCameraView.getHolder().addCallback(new Callback() {
            private boolean preview;
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    Log.d(TAG, "Open Camera");
                    camera = Camera.open(0);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(240, 320);
                    //parameters.setPreviewFrameRate(5); //每秒5帧
                    //parameters.setPictureFormat(PixelFormat.JPEG);//设置照片的输出格式
                    //parameters.set("jpeg-quality", 85);//照片质量
                    //parameters.setPictureSize(mCameraView.getWidth(), mCameraView.getHeight());
                    camera.setParameters(parameters);
                    camera.setPreviewDisplay(mCameraView.getHolder());
                    camera.startPreview();
                    preview = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                if (camera != null) {
                    if (preview) {
                        camera.stopPreview();
                        preview = false;
                    }
                    camera.release();
                    camera = null; // 记得释放
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged....");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnplay:
                play();
                break;
            case R.id.btnpause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                break;
            case R.id.btnstop:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                break;
            default:
                break;
        }
    }

    private void play() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置需要播放的视频
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.testvideo);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            //mediaPlayer.setDataSource(filename);
            //把视频画面输出到SurfaceView
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            //播放
            //mediaPlayer.setVolume(0.1f,0.1f);
            mediaPlayer.start();

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy.............");
        //stopPlay();
        if (mediaPlayer.isPlaying()) {
            //如果正在播放我们就先保存这个播放位置
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.stop();
        }
        mAudioManager.setMode(preMode);
        releaseWakeLock();
        //mAudioManager.setParameters("MelodyTestRCV=0");
        //mAudioManager.setMode(preMode);
        super.onDestroy();
    }

    private static final int ONE_SECOND_TIME_OUT = 1;
    private static final int TYPE_SWITCH_TIME_OUT = 3;
    private static final int VIDEO_TEST_TIME_OUT = 2;

    public class OneSecondThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = ONE_SECOND_TIME_OUT;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    public class SwitchThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(15000);
                    Message msg = new Message();
                    msg.what = TYPE_SWITCH_TIME_OUT;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    public class VideoTestTimeThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000 * 60 * VIDEO_TEST_TIME);
                Message msg = new Message();
                msg.what = VIDEO_TEST_TIME_OUT;
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long recLen = 0;
    private boolean on = false;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ONE_SECOND_TIME_OUT:
                    recLen++;
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    CharSequence sysTimeStr = formatter.format(recLen * 1000);
                    mStartTimeView.setText(sysTimeStr);
                    mStartTimeView.setTextSize(60.0f);
                    break;
                case TYPE_SWITCH_TIME_OUT:
                    if (on) {
                        changeToSpeaker();
                        on = false;
                    } else {
                        changeToReceiver();
                        on = true;
                    }
                    break;
                case VIDEO_TEST_TIME_OUT:
                    if (mediaPlayer.isPlaying()) {
                        //如果正在播放我们就先保存这个播放位置
                        position = mediaPlayer.getCurrentPosition();
                        mediaPlayer.stop();
                    }
                    releaseWakeLock();
                    Intent intent = new Intent();
                    intent.setClass(VideoTestActivity.this, TestActivity.class);
                    intent.setFlags(1);
                    intent.putExtra("special_sound", false);
                    startActivity(intent);
                    VideoTestActivity.this.finish();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public void changeToSpeaker() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset() {
        mAudioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver() {
        mAudioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    private PowerManager.WakeLock wakeLock = null;

    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                Log.i(TAG, "call acquireWakeLock");
                wakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            Log.i(TAG, "call releaseWakeLock");
            wakeLock.release();
            wakeLock = null;
        }
    }
}
