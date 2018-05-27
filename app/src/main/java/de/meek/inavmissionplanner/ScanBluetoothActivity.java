package de.meek.inavmissionplanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class ScanBluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter btAdapter_;
    private ArrayAdapter<String> pairedDevicesArrayAdapter_;
    private ArrayAdapter<String> newDevicesArrayAdapter_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bluetooth);

        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        pairedDevicesArrayAdapter_ = new ArrayAdapter<String>(this, R.layout.device_name);
        newDevicesArrayAdapter_ = new ArrayAdapter<String>(this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter_);
        pairedListView.setOnItemClickListener(m_deviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(newDevicesArrayAdapter_);
        newDevicesListView.setOnItemClickListener(m_deviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(m_receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(m_receiver, filter);

        btAdapter_ = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = btAdapter_.getBondedDevices();

        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter_.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter_.add(noDevices);
        }
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(getResources().getText(R.string.scanning).toString());
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        if (btAdapter_.isDiscovering()) {
            btAdapter_.cancelDiscovery();
        }
        btAdapter_.startDiscovery();
    }

    private ListView.OnItemClickListener m_deviceClickListener = new ListView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        btAdapter_.cancelDiscovery();
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17);
        Intent intent = new Intent();
        intent.putExtra(Const.EXTRA_DEVICE_ADDRESS, address);
        setResult(Activity.RESULT_OK, intent);
        finish();
        }
    };

    private final BroadcastReceiver m_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesArrayAdapter_.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (newDevicesArrayAdapter_.getCount() == 0) {
                    newDevicesArrayAdapter_.add(getResources().getText(R.string.none_found).toString());
                }
            }
        }
    };
}
