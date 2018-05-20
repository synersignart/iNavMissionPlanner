package de.meek.inavmissionplanner;

import android.content.Context;
import android.os.Handler;

public class MissionPlanner {

    private String m_macAddress = null;
    public int m_serialPortBaudRate = 115200;
    public Handler m_handler = null;
    public static MissionPlanner m_app = null;
    private IComm m_comm = null;
    private int m_refreshRate = 500;
    private MspHandler m_mspHandler = new MspHandler();

    public MissionPlanner(Context context, Handler handler)
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

    public void setMAC(String mac) {
        m_macAddress = mac;
    }

    public void request(byte[] data) {
        m_comm.write(data);
        try {
            Thread.sleep(m_refreshRate);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Runnable m_runnableSender = new Runnable() {
        @Override
        public void run() {

            ((BluetoothComm)m_comm).connect(m_macAddress, m_serialPortBaudRate);

            m_threadReceiver = new Thread(m_runnableReceiver);
            m_threadReceiver.start();

            while(true) {

                request(m_mspHandler.serialize_MSP_STATUS_Request());
                request(m_mspHandler.serialize_MSP_SONAR_ALTITUDE_Request());
                request(m_mspHandler.serialize_MSP_RAW_GPS_Request());
            }
        }
    };

    Runnable m_runnableReceiver = new Runnable() {
        @Override
        public void run() {

            while(true) {

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

    Thread m_threadSender = null;
    Thread m_threadReceiver = null;

}
