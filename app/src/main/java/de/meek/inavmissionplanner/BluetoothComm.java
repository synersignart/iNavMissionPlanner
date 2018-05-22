package de.meek.inavmissionplanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothComm implements IComm {

    private Handler m_handler;
    private Context m_context;
    private BluetoothAdapter m_btAdapter = null;
    private BluetoothSocket m_btSocket = null;
    private boolean m_connected = false;
    private int m_rx = 0;
    private int m_tx = 0;

    private OutputStream m_outStream = null;
    private InputStream m_inStream = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothComm(Context context, Handler handler) {
        this.m_context = context;
        this.m_handler = handler;
        enable();
    }

    @Override
    public int rx() {
        return m_rx;
    }

    @Override
    public int tx() {
        return m_tx;
    }

    public boolean isConnected() {
        return m_connected;
    }

    public void showToast(final String toast) {
        Message m = new Message();
        m.what = 0;
        m.obj = toast;
        m_handler.sendMessage(m);
    }


    public void enable() {
        //showToast(m_context.getString(R.string.bt_starting));

        m_btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (m_btAdapter == null) {
            showToast(m_context.getString(R.string.bt_not_available));
            return;
        }

        if (!m_btAdapter.isEnabled()) {
            showToast(m_context.getString(R.string.bt_starting));
            m_btAdapter.enable();
            return;
        }
    }

    public boolean connect(String address, int speed) {
        showToast(m_context.getString(R.string.connecting));

        if (m_btAdapter.isEnabled()) {
            try {
                getRemoteDevice(address);
                m_btSocket.connect();
                m_connected = true;
                showToast(m_context.getString(R.string.connected));
            } catch (IOException e) {
                try {
                    m_btSocket.close();
                    m_connected = false;
                    showToast(m_context.getString(R.string.bt_unable_to_connect));
                } catch (IOException e2) {
                    showToast(m_context.getString(R.string.bt_connection_failure));
                }
            }

            try {
                m_outStream = m_btSocket.getOutputStream();
                m_inStream = m_btSocket.getInputStream();
            } catch (IOException e) {
                showToast("Stream creation failed");
            }
        }
        return m_connected;
    }

    public boolean dataAvailable() {
        boolean ret = false;

        try {
            if (m_connected)
                ret = m_inStream.available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public byte read() {
        byte ret = 0;
        try {
            ret = (byte) m_inStream.read();
            m_rx++;
        } catch (IOException e) {
            showToast("Read error");
        }
        return ret;
    }

    public void write(byte[] arr) {
        try {
                if (m_connected) {
                    m_outStream.write(arr);
                    m_outStream.flush();
                    m_tx += arr.length;
                }
        } catch (IOException e) {
            close();
            showToast("Write error");
        }
    }

    public void disable() {
        try {
            m_btAdapter.disable();
        } catch (Exception e) {
            showToast("Failed to disable BT");
        }
    }

    private void getRemoteDevice(String address) {
        BluetoothDevice device = m_btAdapter.getRemoteDevice(address);
        try {
            m_btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            showToast(m_context.getString(R.string.bt_unable_to_connect));
        }

        if (m_btAdapter.isDiscovering()) {
            m_btAdapter.cancelDiscovery();
        }
    }

    public void close() {
        if (m_outStream != null) {
            try {
                m_outStream.flush();
            } catch (IOException e) {
                showToast("Unable to close socket");
            }
        }

        try {
            if (m_btSocket != null) {
                m_btSocket.close();
            }
            Toast.makeText(m_context, m_context.getString(R.string.disconnected), Toast.LENGTH_LONG).show();

        } catch (Exception e2) {
            showToast("Unable to close socket");
        }
        m_connected = false;
    }
}
