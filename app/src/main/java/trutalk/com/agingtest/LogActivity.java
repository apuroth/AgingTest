package trutalk.com.agingtest;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileInputStream;

public class LogActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mTextView = (TextView) findViewById(R.id.log);
        ReadFileThread thread = new ReadFileThread();
        thread.start();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    String data = (String) msg.obj;
                    mTextView.setText(data);
                    break;
            }
        }
    };

    public class ReadFileThread extends Thread {
        @Override
        public void run() {
            String data = "";
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

            Message msg = new Message();
            msg.what = 2;
            msg.obj = data;
            mHandler.sendMessage(msg);
        }
    }
}
