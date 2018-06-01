package de.meek.inavmissionplanner;

import android.content.Context;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;

public class App {

    private String macAddress_ = "20:15:07:20:66:45";
    public int serialPortBaudRate_ = 115200;
    public Handler handler_ = null;
    private IComm comm_ = new BluetoothComm();
    private int sendRate_ = 20;
    private MspHandler msp_ = new MspHandler();
    LinkedList<byte[]> requests_ = new LinkedList<>();
    LinkedList<byte[]> cyclicRequests_ = new LinkedList<>();
    private static App instance_;
    private int cyclicRequestIndex_ = 0;

    public static App getInstance()
    {
        if (instance_ == null) {
            synchronized(App.class) {
                if (instance_ == null)
                    instance_ = new App();
                }
            }
        return instance_;
    }

    private void initCyclicRequests() {
        cyclicRequests_.add(msp_.serialize_MSP_STATUS_Request());
        cyclicRequests_.add(msp_.serialize_MSP_SONAR_ALTITUDE_Request());
        cyclicRequests_.add(msp_.serialize_MSP_ALTITUDE_Request());
        cyclicRequests_.add(msp_.serialize_MSP_RAW_GPS_Request());
        cyclicRequests_.add(msp_.serialize_MSP_RC_Request());
    }

    private App()
    {
        initCyclicRequests();
    }

    public void setHandler(Handler handler) {
        comm_.setHandler(handler);
        handler_ = handler;
    }

    public MspHandler getMsp() {
        return msp_;
    }

    public IComm getComm() {
        return comm_;
    }

    public void connect() {
        threadSender_ = new Thread(runnableSender_);
        threadSender_.start();
    }

    public void disconnect() {
        comm_.close();
    }

    public void setMAC(String mac) {
        macAddress_ = mac;
    }

    public void request(byte[] data) {
        synchronized (requests_) {
            requests_.addLast(data);
        }
    }

    Runnable m_runnableCyclicRequest = new Runnable() {
        @Override
        public void run() {

        while(comm_.isConnected()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cyclicRequestIndex_ %= cyclicRequests_.size();
            byte[] cmd = cyclicRequests_.get(cyclicRequestIndex_);
            cyclicRequestIndex_++;
            request(cmd);
        }
        }
    };

    Runnable runnableSender_ = new Runnable() {
        @Override
        public void run() {

        ((BluetoothComm)comm_).connect(macAddress_, serialPortBaudRate_);

        if (comm_.isConnected()) {
            threadReceiver_ = new Thread(runnableReceiver_);
            threadReceiver_.start();
            threadCyclicRequest_ = new Thread(m_runnableCyclicRequest);
            threadCyclicRequest_.start();
        }

        while(comm_.isConnected()) {

            byte[] data = null;
            synchronized (requests_) {
                if (!requests_.isEmpty()) {
                    data = requests_.removeFirst();
                    comm_.write(data);
                }
            }

            try {
                Thread.sleep(sendRate_);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        }
    };

    Runnable runnableReceiver_ = new Runnable() {
        @Override
        public void run() {

        while(comm_.isConnected()) {

            while (comm_.dataAvailable()) {
                byte b = comm_.read();
                msp_.parse(b);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        }
    };

    Thread threadCyclicRequest_ = null;
    Thread threadSender_ = null;
    Thread threadReceiver_ = null;

    void requestMissionFromCopter() {
        request(msp_.serialize_MSP_WP_2((byte)0));
    }

    public void sendMissionToCopter(MspWaypointList list) {

        byte n = 1;
        for (Waypoint wp: list.waypoints_) {

            byte flag = wp.flags_;
            if (n == list.waypoints_.size()) {
                flag |= Waypoint.FLAG_LAST;
            }
            byte[] msg = getMsp().serialize_MSP_SET_WP(n++, wp.action_, wp.lat_, wp.lon_, wp.alt_, wp.p1_, wp.p2_, wp.p3_, flag);
            request(msg);
        }

        requestMissionFromCopter();
    }

    public void sendGoToPosition(LatLng pos) {

        byte[] msg = getMsp().serialize_MSP_SET_WP((byte)255, Waypoint.ACTION_WAYPOINT,
                Convert.getIntLatFromLatLng(pos), Convert.getIntLonFromLatLng(pos),
                (short)200,
                (short)0,(short)0,(short)0,(byte)0);
        request(msg);
    }
}
