package trutalk.com.agingtest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends Activity implements View.OnClickListener {

    private TextView mStartTimeView;
    //private TextView mStopTimeView;
    private Button mStartBtn;
    private Button mStopBtn;
    private boolean mRunning = false;
    private int mFlag = 0;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private AudioManager mAudioManager;
    private String TAG = "AgingTest";
    private final String NORMAL_SOUND = "sounds/normal.mp3";
    private final String SPECIAL_SOUND = "sounds/special.mp3";
    private String mCurrentSound = NORMAL_SOUND;
    private int mSoundId = 0;
    private int preMode = 0;
    private boolean mSpecialSound = false;

    private int TEST_TIME = 120; //minutes

    private int mType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate.............");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //getWindow().setType(2029);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mFlag = getIntent().getFlags();
        mSpecialSound = getIntent().getBooleanExtra("special_sound", false);
        Resources res = getResources();
        if (mSpecialSound) {
            Drawable drawable = res.getDrawable(R.drawable.bkcolor_r);
            getWindow().setBackgroundDrawable(drawable);
            mSoundId = R.raw.special;
        } else {
            Drawable drawable = res.getDrawable(R.drawable.bkcolor_g);
            getWindow().setBackgroundDrawable(drawable);
            mSoundId = R.raw.normal;
        }
        mStartTimeView = (TextView) findViewById(R.id.start_time);
        //mStopTimeView = (TextView) findViewById(R.id.stop_time);
        mStartBtn = (Button) findViewById(R.id.start);
        mStopBtn = (Button) findViewById(R.id.stop);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);

        //mMediaPlayer = MediaPlayer.create(this, R.raw.normal);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preMode = mAudioManager.getMode();
        //audioManager.setMicrophoneMute(false);
        //mAudioManager.setParameters("MelodyTestRCV=1");
        if (mFlag == 0) {
            setTitle(getString(R.string.lounder_test));
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(true);
        } else if (mFlag == 1) {
            setTitle(getString(R.string.mic_test));
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mAudioManager.setSpeakerphoneOn(false);
        }
        acquireWakeLock();
        start();
        new TimeThread().start();
        new TestTimeThread().start();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged....");
    }

    private static final int msgKey1 = 1;
    private static final int time_out =2;
    private static final int start_play = 3;
    public class TimeThread extends Thread {
        @Override
        public void run () {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(true);
        }
    }

    public class TestTimeThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000 * 60 * TEST_TIME);
                Message msg = new Message();
                msg.what = time_out;
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private long recLen = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    long sysTime = System.currentTimeMillis();
                    recLen++;
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    CharSequence sysTimeStr = formatter.format(recLen*1000);
                    mStartTimeView.setText(sysTimeStr);
                    mStartTimeView.setTextSize(60.0f);
                    break;
                case time_out:
                    releaseWakeLock();
                    //Intent intent = new Intent();
                    //intent.setClass(TestActivity.this, BatteryInfoActivity.class);
                    //startActivity(intent);
                    TestActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy.............");
        stopPlay();
        releaseWakeLock();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        //mAudioManager.setParameters("MelodyTestRCV=0");
        mAudioManager.setMode(preMode);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.e(TAG, "onPause.............");
    }

    private void playSound() {
        try {
            //mMediaPlayer.reset();
            /*if ((new File("/sdcard/hansuo/hansuo.mp3").exists())) {
                mMediaPlayer.setDataSource("/sdcard/hansuo/hansuo.mp3");
            } else {*/
            //AssetManager assets = getAssets();
            //mMediaPlayer.setAudioStreamType(mFlag == 0 ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL);
            //mMediaPlayer.setLooping(true);
            AssetFileDescriptor afd = getResources().openRawResourceFd(mSoundId);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            //mMediaPlayer.setDataSource(assets.openFd(mCurrentSound).getFileDescriptor());
            //assets.
            //}
            //mMediaPlayer.prepare();
            //mMediaPlayer.start();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMediaPlayer.prepare();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
            mMediaPlayer.setVolume(1f,1f);
            /*mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                    mp.start();
                }
            });*/
        } catch (Exception e) {
            Log.e(TAG, "Sound error");
        }
    }

    private void stopPlay() {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            Log.e(TAG, "Sound error");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.start:
                if (!mRunning) {
                    start();
                }
                break;
            case R.id.stop:
                if (mRunning) {
                    stop();
                }
                break;
        }
    }


    private void start() {
        Log.e(TAG, "Start play....");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        //mStartTimeView.setText(getText(R.string.starttime) + str);
        //mThread.start();
        playSound();
        mRunning = true;
    }

    private void stop() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        //mStopTimeView.setText(getText(R.string.stoptime) + str);
        stopPlay();
        mRunning = false;
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
