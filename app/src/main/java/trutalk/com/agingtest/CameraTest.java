package trutalk.com.agingtest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraTest extends Activity {

    private final String TAG = "CameraTest";
    private int testTime = 0;
    private int step = 0;
    private int mCameraIndex = 1;
    private TextView mStartTimeView;

    class Preview extends ViewGroup implements PreviewCallback, Callback {

        Preview(Context context) {
            super(context);
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        public void setCameraDisplayOrientation(Activity activity,
                                                int cameraId, android.hardware.Camera camera) {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated");
            try {
                if (mCamera != null) {
                    setCameraDisplayOrientation(CameraTest.this, mCameraIndex, mCamera);
                    mCamera.setPreviewDisplay(holder);
                    Log.i(TAG, "setPreviewDisplay");
                }
            } catch (IOException exception) {
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.stopPreview();
                Log.i(TAG, "stopPreview");
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            mCamera.startPreview();
            Log.i(TAG, "startPreview");
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }

    }

    Preview mPreview;
    Camera mCamera;
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        Intent intent = getIntent();
        testTime = intent.getIntExtra("time", 15);
        step = intent.getIntExtra("step", 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStartTimeView = (TextView) findViewById(R.id.start_time);
        //imageView = (ImageView) findViewById(R.id.iv);
        mSurfaceView = (SurfaceView) findViewById(R.id.cameraView);
        mPreview = new Preview(this);
        Log.i(TAG, "onCreate");
        if (step == 4) {
            mCameraIndex = 0;
        } else if (step == 5) {
            mCameraIndex = 1;
        }
        new OneSecondThread().start();
        new TestTimeThread().start();
    }


    public class OneSecondThread extends Thread {
        @Override
        public void run() {
            do {
                if (bClose) {
                    break;
                }
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


    public class TestTimeThread extends Thread {
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

    private static final int ONE_SECOND_TIME_OUT = 1;
    private static final int VIDEO_TEST_TIME_OUT = 2;
    private long recLen = 0;
    private boolean bClose = false;
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
                    mStartTimeView.setTextSize(CameraTest.this.getResources().getDimensionPixelSize(R.dimen.time_text_size));
                    if (recLen > 5 && recLen % 5 == 0 && !bClose) {
                        try {
                            mCamera.takePicture(null, null, null, new PictureCallback() {

                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                                            data.length);
                                    //imageView.setImageBitmap(bitmap);
                                    mCamera.startPreview();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case VIDEO_TEST_TIME_OUT:
                    bClose = true;
                    if (mCamera != null) {
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                    }
                    Intent intent = new Intent();
                    CharSequence timeStr = formatter.format(1000 * 60 * testTime);
                    intent.putExtra("time", timeStr);
                    intent.putExtra("step", step);
                    CameraTest.this.setResult(RESULT_OK, intent);
                    CameraTest.this.finish();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        try {
            mCamera = Camera.open(mCameraIndex);
            Camera.Parameters parameters = mCamera.getParameters(); // Camera parameters to obtain
            parameters.setFlashMode("on");
            mCamera.setParameters(parameters); // Setting camera parameters
            Log.i(TAG, "Camera.open");
        } catch (Exception e) {
            e.printStackTrace();
            bClose = true;
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            Intent intent = new Intent();
            CharSequence timeStr = formatter.format(recLen * 1000);
            intent.putExtra("time", timeStr);
            intent.putExtra("step", step);
            CameraTest.this.setResult(RESULT_OK, intent);
            CameraTest.this.finish();
        }
    }

    protected void onDestroy() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

}
