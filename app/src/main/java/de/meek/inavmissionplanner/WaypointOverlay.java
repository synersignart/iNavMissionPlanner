package de.meek.inavmissionplanner;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WaypointOverlay {

    WaypointLine lineEdit_ = new WaypointLine(Color.CYAN, null);
    WaypointLine lineCopter_ = new WaypointLine(Color.RED, WaypointLine.PATTERN_POLYGON_ALPHA);
    HashMap<Mavlink.MissionItem, Marker> markers_ = new HashMap<>();
    Mavlink.MissionPlan plan_ = null;
    GoogleMap map_ = null;

    private void setWaypointMarker(Mavlink.MissionItem wp, int i, GoogleMap map) {

        Marker marker = null;
        if (!markers_.containsKey(wp)) {
            marker = map.addMarker(new MarkerOptions()
                            .position(wp.getLatLng())
                            .draggable(true)
            );
            marker.setTag(wp);
            markers_.put(wp, marker);
        } else {
            marker = markers_.get(wp);
            marker.setPosition(wp.getLatLng());
        }

//        marker.setTitle(String.format("%d", i));
//        marker.setSnippet(wp.detail());
        float col = (i == 0) ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_ORANGE;
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(col));
    }

     public void addWaypoint(LatLng ll, float altitude) {

        Mavlink.MissionItem wp  = new Mavlink.MissionItem();
        wp.initParams();
        wp.command = wp.MAV_CMD_NAV_WAYPOINT;
        wp.setLatLng(ll);
        wp.setAltitude(altitude);
        plan_.mission.items.add(wp);
    }

    public void deleteWaypoint(Mavlink.MissionItem wp) {
        if (wp != null) {
            plan_.mission.items.remove(wp);
            Marker marker = null;
            if (markers_.containsKey(wp)) {
                marker = markers_.get(wp);
                marker.remove();
                markers_.remove(wp);
            }
        }
    }

    public void moveUpWaypoint(Mavlink.MissionItem wp) {
        if (wp != null) {
            int i = plan_.mission.items.indexOf(wp);
            if (i > 0) {
                Collections.swap(plan_.mission.items, i, i - 1);
            }
        }
    }

    public void moveDownWaypoint(Mavlink.MissionItem wp) {
        if (wp != null) {
            int i = plan_.mission.items.indexOf(wp);
            if (i < plan_.mission.items.size()-1) {
                Collections.swap(plan_.mission.items, i, i + 1);
            }
        }
    }

    private boolean updateWaypoint(Mavlink.MissionItem wp, int i, GoogleMap map) {

        switch (wp.command) {
            case Mavlink.MissionItem.MAV_CMD_NAV_WAYPOINT:
            case Mavlink.MissionItem.MAV_CMD_NAV_TAKEOFF: {

                lineEdit_.add(wp.getLatLng());
                setWaypointMarker(wp, i, map);
            } return true;
            default:
                return false;
        }
    }

    private void deleteAllMarkers() {

        for(Map.Entry<Mavlink.MissionItem, Marker> entry : markers_.entrySet()) {
            entry.getValue().remove();
        }
        markers_.clear();
    }

    public void update() {
        lineEdit_.clear();
        deleteAllMarkers();
        if((map_ != null) && (plan_ != null)) {
            int i=0;
            for (Mavlink.MissionItem wp : plan_.mission.items) {
                if (updateWaypoint(wp, i, map_)) {
                    i++;
                }
            }
            lineEdit_.update(map_);
        }
    }

    public boolean setMissionPlan(Mavlink.MissionPlan plan) {
        plan_ = plan;
        update();
        return true;
    }

    public void setMap(GoogleMap map) {
        map_ = map;
    }

    public LatLngBounds getWaypointBounds(Mavlink.MissionPlan plan)
    {
        LatLngBounds bounds = null;
        if (plan != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Mavlink.MissionItem wp : plan.mission.items) {
                if (wp.hasLatLng()) {
                    builder.include(wp.getLatLng());
                }
            }
            bounds = builder.build();
        }
        return bounds;
    }

    public void updateReceivedWayPointList(MspWaypointList list) {
        lineCopter_.clear();
        Waypoint rth = null;
        if (list != null) {
            for (Waypoint wp : list.waypoints_) {
                if (wp.nr_ == 0) {
                    rth = wp;
                } else {
                    lineCopter_.add(wp.getLatLng());
                }
            }
            if (rth != null) {
                lineCopter_.add(rth.getLatLng());
            }
        }
        lineCopter_.update(map_);
    }

}
