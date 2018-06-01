package de.meek.inavmissionplanner;

import com.google.android.gms.maps.model.LatLng;

public class Convert {


    public static int getIntLatFromLatLng(LatLng ll) {
        return (int) (ll.latitude * 1e7);
    }
    public static int getIntLonFromLatLng(LatLng ll) {
        return (int) (ll.longitude * 1e7);
    }


    public static LatLng getLatLng(int lat, int lon) {
        return new LatLng(lat / 1e7, lon / 1e7);
    }
}
