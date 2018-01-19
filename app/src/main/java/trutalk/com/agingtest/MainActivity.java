package trutalk.com.agingtest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    private TextView mTextView;
    private boolean mSpecialSound = false;
    private MsgReceiver msgReceiver;
    private TextView mBatteryInfo;

    private String mBatteryInfos = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.state);
        mBatteryInfo = (TextView) findViewById(R.id.textBattery);
        mTextView.setText(R.string.normal_sound);

        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.trutalk.agingtest.RECEIVER");
        registerReceiver(msgReceiver, intentFilter);

        Intent service = new Intent(MainActivity.this, BootService.class);
        MainActivity.this.startService(service);
        //mTextView.setTextColor(0x76EE00);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.test_list)));
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, TestActivity.class);
                intent.setFlags(i);
                intent.putExtra("special_sound", mSpecialSound);
                startActivity(intent);
            }
        });
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, VideoTestActivity.class);
        //startActivity(intent);
        //this.finish();
    }

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
            String date = sDateFormat.format(new java.util.Date());
            String battery_health = intent.getStringExtra("battery_health");
            String battery_status = intent.getStringExtra("battery_status");
            String battery_temperature = intent.getStringExtra("battery_temperature");
            String battery_level = intent.getStringExtra("battery_level");
            mBatteryInfos = mBatteryInfos + date +", ";
            if (battery_health != null) {
                mBatteryInfos = mBatteryInfos + battery_health +", ";
            }
            if (battery_status != null) {
                mBatteryInfos = mBatteryInfos + battery_status +", ";
            }
            if (battery_temperature != null) {
                mBatteryInfos = mBatteryInfos + "温度：" + battery_temperature +", ";
            }
            if (battery_temperature != null) {
                mBatteryInfos = mBatteryInfos + "电量：" + battery_level;
            }
            mBatteryInfos = mBatteryInfos + '\n';
            mBatteryInfo.setText(mBatteryInfos);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0,1,1,R.string.normal_sound);
        menu.add(0,2,2,R.string.special_sound);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 1:
                mSpecialSound = false;
                mTextView.setText(R.string.normal_sound);
                //mTextView.setTextColor(0x76EE00);
                break;
            case 2:
                mSpecialSound = true;
                mTextView.setText(R.string.special_sound);
                //mTextView.setTextColor(0xFF1111);
                break;
        }
        return true;
    }
}
