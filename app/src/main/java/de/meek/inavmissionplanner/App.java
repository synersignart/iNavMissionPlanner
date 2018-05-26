package de.meek.inavmissionplanner;

import android.content.Context;
import android.os.Handler;

import java.util.LinkedList;

public class App {

    private String m_macAddress = null;
    public int m_serialPortBaudRate = 115200;
    public Handler m_handler = null;
    public static App m_app = null;
    private IComm m_comm = null;
    private int m_refreshRate = 500;
    private int m_sendRate = 20;
    private MspHandler m_mspHandler = new MspHandler();

    public App(Context context, Handler handler)
    {
        this.m_app = this;
        this.m_handler = handler;
        m_comm = new BluetoothComm(context, handler);
    }

    public MspHandler getMsp() {
        return m_mspHandler;
    }

    public IComm getComm() {
        return m_comm;
    }

    public void connect() {
        m_threadSender = new Thread(m_runnableSender);
        m_threadSender.start();
    }

    public void disconnect() {
        m_comm.close();
    }

    public void setMAC(String mac) {
        m_macAddress = mac;
    }

    LinkedList<byte[]> m_requests = new LinkedList<>();

    public void request(byte[] data) {
        synchronized (m_requests) {
            m_requests.addLast(data);
        }
    }

    Runnable m_runnableCyclicRequest = new Runnable() {
        @Override
        public void run() {

            while(m_comm.isConnected()) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                request(m_mspHandler.serialize_MSP_STATUS_Request());
                request(m_mspHandler.serialize_MSP_SONAR_ALTITUDE_Request());
                request(m_mspHandler.serialize_MSP_RAW_GPS_Request());
                request(m_mspHandler.serialize_MSP_RC_Request());
            }
        }
    };

    Runnable m_runnableSender = new Runnable() {
        @Override
        public void run() {

            ((BluetoothComm)m_comm).connect(m_macAddress, m_serialPortBaudRate);

            if (m_comm.isConnected()) {
                m_threadReceiver = new Thread(m_runnableReceiver);
                m_threadReceiver.start();
                m_threadCyclicRequest = new Thread(m_runnableCyclicRequest);
                m_threadCyclicRequest.start();
            }

            while(m_comm.isConnected()) {

                byte[] data = null;
                synchronized (m_requests) {
                    if (!m_requests.isEmpty()) {
                        data = m_requests.removeFirst();
                        m_comm.write(data);
                    }
                }


                try {
                    Thread.sleep(m_sendRate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Runnable m_runnableReceiver = new Runnable() {
        @Override
        public void run() {

            while(m_comm.isConnected()) {

                while (m_comm.dataAvailable()) {
                    byte b = m_comm.read();
                    m_mspHandler.parse(b);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Thread m_threadCyclicRequest = null;
    Thread m_threadSender = null;
    Thread m_threadReceiver = null;
}
