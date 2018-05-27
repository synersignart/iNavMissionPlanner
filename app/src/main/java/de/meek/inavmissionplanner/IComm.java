package de.meek.inavmissionplanner;

import android.os.Handler;

public interface IComm {
    public boolean isConnected();
    public void write(byte[] data);
    public byte read();
    public boolean dataAvailable();
    public int rx();
    public int tx();
    public void close();
    public void setHandler(Handler handler);
}
