package de.meek.inavmissionplanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static App m_app = null;

    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(msg.what==0) {
                showToast((String)msg.obj);
            }
        }
    };

    void showToast(final String toast)
    {
        Toast.makeText(MainActivity.this,toast, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_app.connect();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        m_app = new App(getApplicationContext(), handler);
        m_app.setMAC("20:15:07:20:66:45");
        m_handlerUpdateUI.postDelayed(m_runnableUpdateUI, Const.refreshRateUI);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_scan_bluetooth:
                showActivityScanBluetoothDevices();
                return true;
            case R.id.action_settings:
                showActivitySettings();
                return true;
            case R.id.action_map:
                showActivityMap();
                return true;
            case R.id.action_connect_bluetooth:
                m_app.connect();
                return true;
            case R.id.action_disconnect_bluetooth:
                m_app.disconnect();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBtnShowMap(View v) {
        showActivityMap();
    }

    void showActivityScanBluetoothDevices()
    {
        Intent intent = new Intent(this, ScanBluetoothActivity.class);
        startActivityForResult(intent,Const.ACTIVITY_SCAN_BLUETOOTH_DEVICES);
    }

    void showActivitySettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent,Const.ACTIVITY_SETTINGS);
    }

    void showActivityMap()
    {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent,Const.ACTIVITY_MAP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Const.ACTIVITY_SCAN_BLUETOOTH_DEVICES:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(Const.EXTRA_DEVICE_ADDRESS);
                    m_app.setMAC(address);
                }
                break;
            default:
                break;
        }
    }

    private MspHandler getData() {
        return m_app.getMsp();
    }

    private IComm getComm() {
        return m_app.getComm();
    }

    Handler m_handlerUpdateUI = new Handler();
    Runnable m_runnableUpdateUI = new Runnable() {
        @Override
        public void run() {

            ((TextView)findViewById(R.id.tvCycleTime)).setText(""+getData().cycleTime);
            ((CheckBox)findViewById(R.id.cbAccPresent)).setChecked(getData().accPresent);
            ((CheckBox)findViewById(R.id.cbMagPresent)).setChecked(getData().magPresent);
            ((CheckBox)findViewById(R.id.cbSonarPresent)).setChecked(getData().sonarPresent);
            ((CheckBox)findViewById(R.id.cbBaroPresent)).setChecked(getData().baroPresent);
            ((CheckBox)findViewById(R.id.cbAccPresent)).setChecked(getData().accPresent);

            ((CheckBox)findViewById(R.id.cbConnected)).setChecked(getComm().isConnected());
            ((TextView)findViewById(R.id.tvTx)).setText(""+ getComm().tx());
            ((TextView)findViewById(R.id.tvRx)).setText(""+ getComm().rx());

            ((TextView)findViewById(R.id.tvGpsSats)).setText(""+getData().gpsNumSats);
            ((CheckBox)findViewById(R.id.cbGps2dFix)).setChecked(getData().gpsFix2d);
            ((CheckBox)findViewById(R.id.cbGps3dFix)).setChecked(getData().gpsFix3d);
            ((TextView)findViewById(R.id.tvGpsPos)).setText(String.format("%d / %d" , getData().gpsLat, getData().gpsLon));

            ((TextView)findViewById(R.id.tvSonarAlt)).setText(""+getData().sonarAltitude);

            ((TextView)findViewById(R.id.tvRC1)).setText(String.format("Roll=%d, Pitch=%d, Yaw=%d, Throttle=%d" , getData().rcRoll, getData().rcPitch, getData().rcYaw, getData().rcThrottle));
            ((TextView)findViewById(R.id.tvRC2)).setText(String.format("Aux1=%d, Aux2=%d, Aux3=%d, Aux4=%d" , getData().rcAux1, getData().rcAux2, getData().rcAux3, getData().rcAux4));

            m_handlerUpdateUI.postDelayed(this, Const.refreshRateUI);
        }
    };

}
