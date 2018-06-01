package de.meek.inavmissionplanner;

import com.google.android.gms.maps.model.LatLng;

public class Waypoint {

    public static final byte ACTION_WAYPOINT = 0x01;
    public static final byte ACTION_RTH = 0x04;
    public static final byte FLAG_LAST = (byte)0xA5;

    byte action_ = 0;
    int lat_= 0;
    int lon_= 0;
    int alt_= 0;
    short p1_= 0;
    short p2_= 0;
    short p3_= 0;
    byte flags_= 0;
    byte nr_ = 0;

    public Waypoint() {}

    public Waypoint(byte nr, byte action, int lat, int lon, int alt, short p1, short p2, short p3, byte flags) {
        nr_ = nr;
        action_ = action;
        lat_ = lat;
        lon_ = lon;
        alt_ = alt;
        p1_ = p1;
        p2_ = p2;
        p3_ = p3;
        flags_ = flags_;
    }

    public boolean isLast() {
        return (flags_ & FLAG_LAST) != 0;
    }

    public LatLng getLatLng() {
        return Convert.getLatLng(lat_, lon_);
    }
}
