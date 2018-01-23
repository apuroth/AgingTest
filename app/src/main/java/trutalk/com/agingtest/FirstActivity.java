package trutalk.com.agingtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
//import android.os.IPersistLogCallback;
//import android.os.IPersistLogService;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
//import android.os.ServiceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FirstActivity extends AppCompatActivity {
    private String TAG = "AgingTest";
    CheckBox mSpeaker;
    CheckBox mReceiver;
    CheckBox mVibrate;
    CheckBox mMic;
    CheckBox mBackCamera;
    CheckBox mFrontCamera;
    EditText mItemTime;
    EditText mAllTime;
    Button mStartButton;
    TextView mResultView;

    int[] resId = {R.string.speaker, R.string.receiver, R.string.mic, R.string.vibrate, R.string.back_camera, R.string.front_camera,};
    int itemTime = 15;
    int allTime = 240;

    int mStep = 0;
    boolean mSpeakerChecked = false;
    boolean mReceiverChecked = false;
    boolean mVibrateChecked = false;
    boolean mMicChecked = false;
    boolean mBackCameraChecked = false;
    boolean mFrontCameraChecked = false;
    private String[] mResultValue = new String[7];

    private MsgReceiver msgReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acquireWakeLock();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_first);
        mSpeaker = (CheckBox) findViewById(R.id.speaker_box);
        mSpeaker.setChecked(getResources().getBoolean(R.bool.speaker_test_enable));
        mReceiver = (CheckBox) findViewById(R.id.receiver_box);
        mReceiver.setChecked(getResources().getBoolean(R.bool.receiver_test_enable));
        mVibrate = (CheckBox) findViewById(R.id.vibrate_box);
        mVibrate.setChecked(getResources().getBoolean(R.bool.vibrator_test_enable));
        mMic = (CheckBox) findViewById(R.id.mic_box);
        mMic.setChecked(getResources().getBoolean(R.bool.mic_test_enable));
        mBackCamera = (CheckBox) findViewById(R.id.back_camera_box);
        mBackCamera.setChecked(getResources().getBoolean(R.bool.back_camera_test_enable));
        mFrontCamera = (CheckBox) findViewById(R.id.front_camera_box);
        mFrontCamera.setChecked(getResources().getBoolean(R.bool.front_camera_test_enable));
        mItemTime = (EditText) findViewById(R.id.item_time);
        mItemTime.setText(getResources().getText(R.string.single_item_test_time));
        mAllTime = (EditText) findViewById(R.id.all_time);
        mStartButton = (Button) findViewById(R.id.start);
        mStartButton.setOnClickListener(mButtonListener);
        msgReceiver = new MsgReceiver();
        mResultView = (TextView) findViewById(R.id.result);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.trutalk.agingtest.RECEIVER");
        registerReceiver(msgReceiver, intentFilter);
        Intent service = new Intent(FirstActivity.this, BootService.class);
        FirstActivity.this.startService(service);
        if (getResources().getBoolean(R.bool.start_auto)) {
            mStartButton.callOnClick();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.read:
                Intent intent = new Intent();
                intent.setClass(FirstActivity.this, LogActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mBatteryInfos = "";
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
            String date = sDateFormat.format(new java.util.Date());
            String battery_health = intent.getStringExtra("battery_health");
            String battery_status = intent.getStringExtra("battery_status");
            String battery_temperature = intent.getStringExtra("battery_temperature");
            String battery_level = intent.getStringExtra("battery_level");
            mBatteryInfos = mBatteryInfos + date + ", ";
            if (battery_health != null) {
                mBatteryInfos = mBatteryInfos + battery_health + ", ";
            }
            if (battery_status != null) {
                mBatteryInfos = mBatteryInfos + battery_status + ", ";
            }
            if (battery_temperature != null) {
                mBatteryInfos = mBatteryInfos + "温度：" + battery_temperature + ", ";
            }
            if (battery_temperature != null) {
                mBatteryInfos = mBatteryInfos + "电量：" + battery_level + "%";
            }

            Log.i(TAG, mBatteryInfos);
            mResultValue[6] = mBatteryInfos;
            updateStateText();
            saveTestLog(mBatteryInfos);
        }

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
            if (getResources().getBoolean(R.bool.cycle_test_enable)) {
                StartTest(0);
            } else {
                return;
            }
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
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
            String date = sDateFormat.format(new java.util.Date());
            String timeStr = data.getStringExtra("time");
            int step = data.getIntExtra("step", 0);
            mResultValue[step] = date + getText(resId[step]).toString() + ":" + timeStr;

            if (step < 5) {
                StartTest(step + 1);
            } else {

            }
            updateStateText();
            saveTestLog(mResultValue[step]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStateText() {
        String value = "";
        for (String result : mResultValue) {
            if (result != null) {
                value += result + "\n";
            }
        }
        mResultView.setText(value);
    }

    public class SaveFileThread extends Thread {
        String mResult = "";

        SaveFileThread(String result) {
            mResult = result + "\n";
        }

        @Override
        public void run() {
            try {
                FileOutputStream fos = new FileOutputStream("/persist/misc/" + "AgingTest", true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                bw.write(mResult);
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTestLog(String result) {
        Intent intent = new Intent();
        intent.setAction("ACTION_AGING_TEST_RESULT");
        intent.putExtra("fileName", "AgingTest");
        intent.putExtra("content", result);
//        this.sendBroadcast(intent);
        new SaveFileThread(result).run();
//        try {
//            IPersistLogService logService = IPersistLogService.Stub.asInterface(ServiceManager.getService("PersistLogService"));
//            Log.d(TAG, "getservice");
//            if (logService != null) {
//                Log.d(TAG, "PersistLogService write");
//                logService.write("AgingTest", result);
//            }
//        } catch(RemoteException ex){
//            Log.d(TAG, "RemoteException ex=" + ex);
//        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    String data = (String) msg.obj;
                    mResultView.setText("State: " + data);
                    break;
            }
        }
    };

    private String getTestLog() {
        String data = "";
//        Intent intent = new Intent();
//        intent.setAction("ACTION_AGING_TEST_RESULT");
//        intent.putExtra("fileName", "AgingTest");
//        intent.putExtra("content", result);
//        this.sendBroadcast(intent);
//        try {
//            FileOutputStream fos = new FileOutputStream("/persist/misc/" + "AgingTest", true);
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
//            bw.write(result);
//            bw.flush();
//            bw.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            FileInputStream fin = new FileInputStream("/persist/misc/" + "AgingTest");
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            data = new String(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
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
