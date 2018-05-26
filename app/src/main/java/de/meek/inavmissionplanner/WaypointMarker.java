package de.meek.inavmissionplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class WaypointMarker implements GoogleMap.InfoWindowAdapter {
    LayoutInflater inflater = null;

    WaypointMarker(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        Mavlink.MissionItem wp = (Mavlink.MissionItem) marker.getTag();
        View popup = inflater.inflate(R.layout.waypoint_marker, null);
        if (wp != null) {

            ((TextView) popup.findViewById(R.id.title)).setText(marker.getTitle());
            ((TextView) popup.findViewById(R.id.lat)).setText(String.format("Lat: %f", wp.getLat()));
            ((TextView) popup.findViewById(R.id.lon)).setText(String.format("Lon: %f", wp.getLon()));
            ((TextView) popup.findViewById(R.id.altitude)).setText(String.format("Alt: %f", wp.getAltitude()));
            ((TextView) popup.findViewById(R.id.speed)).setText(String.format("Speed: %f", wp.getSpeed()));
        }
        return popup;
    }
}
