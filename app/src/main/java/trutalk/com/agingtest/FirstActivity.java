package trutalk.com.agingtest;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class FirstActivity extends AppCompatActivity {

    CheckBox mSpeaker;
    CheckBox mReceiver;
    CheckBox mVibrate;
    CheckBox mMic;
    CheckBox mBackCamera;
    CheckBox mFrontCamera;
    EditText mItemTime;
    EditText mAllTime;
    Button mStartButton;

    int itemTime = 15;
    int allTime = 240;

    int mStep = 0;
    boolean mSpeakerChecked = false;
    boolean mReceiverChecked = false;
    boolean mVibrateChecked = false;
    boolean mMicChecked = false;
    boolean mBackCameraChecked = false;
    boolean mFrontCameraChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acquireWakeLock();
        setContentView(R.layout.activity_first);
        mSpeaker = (CheckBox) findViewById(R.id.speaker_box);
        mReceiver = (CheckBox) findViewById(R.id.receiver_box);
        mVibrate = (CheckBox) findViewById(R.id.vibrate_box);
        mMic = (CheckBox) findViewById(R.id.mic_box);
        mBackCamera = (CheckBox) findViewById(R.id.back_camera_box);
        mFrontCamera = (CheckBox) findViewById(R.id.front_camera_box);
        mItemTime = (EditText) findViewById(R.id.item_time);
        mAllTime = (EditText) findViewById(R.id.all_time);
        mStartButton = (Button) findViewById(R.id.start);
        mStartButton.setOnClickListener(mButtonListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
    }

    View.OnClickListener mButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mSpeakerChecked = mSpeaker.isChecked();
            mReceiverChecked = mReceiver.isChecked();
            mVibrateChecked = mVibrate.isChecked();
            mMicChecked = mMic.isChecked();
            mBackCameraChecked = mBackCamera.isChecked();
            mFrontCameraChecked = mFrontCamera.isChecked();
            itemTime = Integer.parseInt(mItemTime.getText().toString());
            if (!(mSpeakerChecked || mReceiverChecked || mVibrateChecked || mMicChecked || mBackCameraChecked || mFrontCameraChecked)) {
                Toast.makeText(FirstActivity.this, R.string.test_warning, Toast.LENGTH_SHORT).show();
            } else {
                StartTest(0);
            }
        }
    };

    void StartTest(int step) {
        if (step > 5) {
            return;
        }
        Intent intent = new Intent();

        switch (step) {
            case 0:
                if (!mSpeakerChecked) {
                    StartTest(step + 1);
                    return;
                }
                intent.setClass(FirstActivity.this, VideoTest.class);
                break;
            case 1:
                if (!mReceiverChecked) {
                    StartTest(step + 1);
                    return;
                }
                intent.setClass(FirstActivity.this, VideoTest.class);
                break;
            case 2:
                if (!mVibrateChecked) {
                    StartTest(step + 1);
                    return;
                }
                intent.setClass(FirstActivity.this, OtherTest.class);
                break;
            case 3:
                if (!mMicChecked) {
                    StartTest(step + 1);
                    return;
                }
                intent.setClass(FirstActivity.this, OtherTest.class);
                break;
            case 4:
                if (!mBackCameraChecked) {
                    StartTest(step + 1);
                    return;
                }
                intent.setClass(FirstActivity.this, CameraTest.class);
                break;
            case 5:
                if (!mFrontCameraChecked) {
                    StartTest(step + 1);
                    return;
                }
                intent.setClass(FirstActivity.this, CameraTest.class);
                break;
        }
        intent.putExtra("step", step);
        intent.putExtra("time", itemTime);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            long timeSecond = data.getLongExtra("time", 0);
            int step = data.getIntExtra("step", 0);
            switch (step) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                default:
                    break;
            }
            if (step < 5) {
                StartTest(step + 1);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
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
                wakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
