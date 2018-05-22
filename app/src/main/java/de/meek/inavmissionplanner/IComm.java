package de.meek.inavmissionplanner;

public interface IComm {
    public boolean isConnected();
    public void write(byte[] data);
    public byte read();
    public boolean dataAvailable();
    public int rx();
    public int tx();
    public void close();
}
