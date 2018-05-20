package de.meek.inavmissionplanner;

import edu.wlu.cs.msppg.MSP_RAW_GPS_Handler;
import edu.wlu.cs.msppg.MSP_SONAR_ALTITUDE_Handler;
import edu.wlu.cs.msppg.MSP_STATUS_Handler;
import edu.wlu.cs.msppg.Parser;

public class MspHandler extends Parser implements MSP_STATUS_Handler, MSP_SONAR_ALTITUDE_Handler, MSP_RAW_GPS_Handler {

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

    public MspHandler() {
        set_MSP_STATUS_Handler(this);
        set_MSP_SONAR_ALTITUDE_Handler(this);
        set_MSP_RAW_GPS_Handler(this);
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
}
