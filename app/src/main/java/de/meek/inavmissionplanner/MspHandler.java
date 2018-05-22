package de.meek.inavmissionplanner;

import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import edu.wlu.cs.msppg.MSP_MODE_RANGES_Handler;
import edu.wlu.cs.msppg.MSP_RAW_GPS_Handler;
import edu.wlu.cs.msppg.MSP_RC_Handler;
import edu.wlu.cs.msppg.MSP_SET_MODE_RANGE_Handler;
import edu.wlu.cs.msppg.MSP_SONAR_ALTITUDE_Handler;
import edu.wlu.cs.msppg.MSP_STATUS_Handler;
import edu.wlu.cs.msppg.Parser;

public class MspHandler extends Parser implements
        MSP_STATUS_Handler,
        MSP_SONAR_ALTITUDE_Handler,
        MSP_RAW_GPS_Handler,
        MSP_RC_Handler,
        MSP_SET_MODE_RANGE_Handler,
        MSP_MODE_RANGES_Handler
{

    public boolean accPresent = false;
    public boolean baroPresent = false;
    public boolean magPresent = false;
    public boolean gpsPresent = false;
    public boolean sonarPresent = false;
    public short cycleTime = 0;
    public int sonarAltitude = 0;
    public byte gpsNumSats = 0;
    public boolean gpsFix2d = false;
    public boolean gpsFix3d = false;
    public int gpsLat = 0;
    public int gpsLon = 0;
    public short rcRoll = 0;
    public short rcPitch = 0;
    public short rcYaw = 0;
    public short rcThrottle = 0;
    public short rcAux1 = 0;
    public short rcAux2 = 0;
    public short rcAux3 = 0;
    public short rcAux4 = 0;

    public ArrayList<BoxMode> boxModeList = new ArrayList<>();

    public MspHandler() {
        set_MSP_STATUS_Handler(this);
        set_MSP_SONAR_ALTITUDE_Handler(this);
        set_MSP_RAW_GPS_Handler(this);
        set_MSP_RC_Handler(this);
        set_MSP_MODE_RANGES_Handler(this);
    }

    @Override
    public void handle_MSP_STATUS(short cycletime, short i2cErrorCount, short sensorStatus, int boxModeFlags, byte profile)
    {
        this.cycleTime = cycletime;
        accPresent = (sensorStatus & 1) > 0;
        baroPresent = (sensorStatus & 2) > 0;
        magPresent = (sensorStatus & 4) > 0;
        gpsPresent = (sensorStatus & 8) > 0;
        sonarPresent = (sensorStatus & 16) > 0;
    }

    @Override
    public void handle_MSP_RAW_GPS(byte fixType, byte numSat, int lat, int lon, short alt, short groundSpeed, short groundCourse, short hdop) {
        this.gpsNumSats = numSat;
        this.gpsFix2d = fixType == 1;
        this.gpsFix3d = fixType == 2;
        this.gpsLat = lat;
        this.gpsLon = lon;
    }

    @Override
    public void handle_MSP_SONAR_ALTITUDE(int altitude) {
        this.sonarAltitude = altitude;
    }

    @Override
    public void handle_MSP_RC(short roll, short pitch, short yaw, short throttle, short aux1, short aux2, short aux3) {
        this.rcRoll = roll;
        this.rcPitch = pitch;
        this.rcYaw = yaw;
        this.rcThrottle = throttle;
        this.rcAux1 = aux1;
        this.rcAux2 = aux2;
        this.rcAux3 = aux3;
    }

    @Override
    public void handle_MSP_SET_MODE_RANGE(byte n, byte box, byte aux, byte start, byte end) {

    }

    @Override
    public void handle_MSP_MODE_RANGES(ByteBuffer data) {

        synchronized (boxModeList) {
            boxModeList.clear();
            int count = data.position();
            count = count / 4;
            for (int i=0; i<count; i++) {
                byte box = data.get(0 + (i*4));
                byte aux = data.get(1 + (i*4));
                byte min = data.get(2 + (i*4));
                byte max = data.get(3 + (i*4));
                boxModeList.add(new BoxMode(i, box, aux, min, max));
            }
        }
        if (h!=null) {
            Message msg = new Message();
            msg.obj = 1;
            h.sendMessage(msg);

        }
    }

    Handler h;
    public void registerModeRangeCB(Handler h)
    {
        this.h = h;
    }
}
