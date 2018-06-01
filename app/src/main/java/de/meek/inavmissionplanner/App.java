package de.meek.inavmissionplanner;

import android.content.Context;
import android.os.Handler;

import java.util.LinkedList;

public class App {

    private String macAddress_ = "20:15:07:20:66:45";
    public int serialPortBaudRate_ = 115200;
    public Handler handler_ = null;
    private IComm comm_ = new BluetoothComm();
    private int sendRate_ = 20;
    private MspHandler msp_ = new MspHandler();
    LinkedList<byte[]> requests_ = new LinkedList<>();
    private static App instance_;

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

    private App()
    {
    }

    public void setHandler(Handler handler) {
        comm_.setHandler(handler);
        handler_ = handler;
    }

  /*
    public App(Context context, Handler handler)
    {
        this.m_app = this;
        this.m_handler = handler;
        m_comm = new BluetoothComm(context, handler);
    }
*/
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                request(msp_.serialize_MSP_STATUS_Request());
                request(msp_.serialize_MSP_SONAR_ALTITUDE_Request());
                request(msp_.serialize_MSP_ALTITUDE_Request());
                request(msp_.serialize_MSP_RAW_GPS_Request());
                request(msp_.serialize_MSP_RC_Request());
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
/*        request(msp_.serialize_MSP_WP_2((byte)1));
        request(msp_.serialize_MSP_WP_2((byte)2));
        request(msp_.serialize_MSP_WP_2((byte)3));*/
    }
}
