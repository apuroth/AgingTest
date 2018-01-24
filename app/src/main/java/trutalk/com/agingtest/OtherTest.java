package trutalk.com.agingtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class OtherTest extends Activity {

    private String TAG = "AgingTest";
    private Vibrator mVibrator;
    private TextView mStartTimeView;
    private TextView mTestStepView;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private AudioManager mAudioManager;
    private int preMode = 0;
    private int testTime = 0;
    private int step = 0;
    File soundFile;
    MediaRecorder mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_test);
        Intent intent = getIntent();
        testTime = intent.getIntExtra("time", 15);
        step = intent.getIntExtra("step", 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTestStepView = (TextView) this.findViewById(R.id.testItem);
        mStartTimeView = (TextView) findViewById(R.id.start_time);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (step == 2) {
            mTestStepView.setText(R.string.vibrate);
            new VibrateThread().start();
        } else if (step == 3) {
            mTestStepView.setText(R.string.mic);
            mRecorder = new MediaRecorder();
            record();
            /*final AudioPlayerHandler audioPlayerHandler = new AudioPlayerHandler();
            audioPlayerHandler.prepare();
            AudioRecoderHandler audioRecoderHandler = new AudioRecoderHandler(this);
            audioRecoderHandler.startRecord(new AudioRecoderHandler.AudioRecordingCallback() {
                @Override
                public void onStopRecord(String savedPath) {
                }

                @Override
                public void onRecording(byte[] data, int startIndex, int length) {
                    audioPlayerHandler.prepare();// 播放前需要prepare。可以重复prepare
                    audioPlayerHandler.onPlaying(data, 0, data.length);
                }
            });*/
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
            preMode = mAudioManager.getMode();
            //audioManager.setMicrophoneMute(false);
            //mAudioManager.setParameters("MelodyTestRCV=1");
            setTitle(getString(R.string.lounder_test));
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(true);
            start();
        }

        new OneSecondThread().start();
        new VideoTestTimeThread().start();
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
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.normal);
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
            mMediaPlayer.setVolume(1f, 1f);
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


    private void start() {
        Log.e(TAG, "Start play....");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        //mStartTimeView.setText(getText(R.string.starttime) + str);
        //mThread.start();
        playSound();
    }

    private void stop() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        //mStopTimeView.setText(getText(R.string.stoptime) + str);
        stopPlay();
    }


    private void record() {
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mRecorder.setOutputFile(Environment.getExternalStorageDirectory() + File.separator + "sound.3pg");
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onDestroy() {
        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }

        if (mRecorder != null) {
            try {
                mRecorder.setOnErrorListener(null);
                mRecorder.setOnInfoListener(null);
                mRecorder.setPreviewDisplay(null);
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        //mAudioManager.setParameters("MelodyTestRCV=0");
        mAudioManager.setMode(preMode);
        super.onDestroy();
    }

    private static final int ONE_SECOND_TIME_OUT = 1;
    private static final int VIDEO_TEST_TIME_OUT = 2;

    public class VibrateThread extends Thread {
        @Override
        public void run() {
            mVibrator = (Vibrator) OtherTest.this.getSystemService(Context.VIBRATOR_SERVICE);
            while (true) {
                try {
                    if (mVibrator != null) {
                        mVibrator.vibrate(5000);
                    }
                    Thread.sleep(4500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

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


    public class VideoTestTimeThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000 * 60 * testTime);
                Message msg = new Message();
                msg.what = VIDEO_TEST_TIME_OUT;
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long recLen = 0;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            switch (msg.what) {
                case ONE_SECOND_TIME_OUT:
                    recLen++;
                    CharSequence sysTimeStr = formatter.format(recLen * 1000);
                    mStartTimeView.setText(sysTimeStr);
                    mStartTimeView.setTextSize(OtherTest.this.getResources().getDimensionPixelSize(R.dimen.time_text_size));
                    break;
                case VIDEO_TEST_TIME_OUT:
                    if (mVibrator != null) {
                        mVibrator.cancel();
                        mVibrator = null;
                    }
                    if (mRecorder != null) {
                        try {
                            mRecorder.setOnErrorListener(null);
                            mRecorder.setOnInfoListener(null);
                            mRecorder.setPreviewDisplay(null);
                            mRecorder.stop();
                            mRecorder.release();
                            mRecorder = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (mMediaPlayer != null) {
                        stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    //mAudioManager.setParameters("MelodyTestRCV=0");
                    mAudioManager.setMode(preMode);
                    Intent intent = new Intent();
                    CharSequence timeStr = formatter.format(1000 * 60 * testTime);
                    intent.putExtra("time", timeStr);
                    intent.putExtra("step", step);
                    OtherTest.this.setResult(RESULT_OK, intent);
                    OtherTest.this.finish();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

}
