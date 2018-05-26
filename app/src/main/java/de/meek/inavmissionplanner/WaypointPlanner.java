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

public class WaypointPlanner {

//    ArrayList<Marker> marker_ = new ArrayList<>();
    Polyline polyline_ = null;
    int polyColor_ = Color.DKGRAY;


    void updatePolyline(GoogleMap map)
    {
        if(null == polyline_)
        {
            PolylineOptions rectOptions = new PolylineOptions();
            rectOptions.color(polyColor_);
            polyline_ = map.addPolyline(rectOptions);
        }
        polyline_.setPoints(waypointPolylist_);
    }

    HashMap<Mavlink.MissionItem, Marker> markers_ = new HashMap<>();

    private void setWaypointMarker(Mavlink.MissionItem wp, int i, GoogleMap map) {

        Marker marker = null;
        if (!markers_.containsKey(wp)) {
            marker = map.addMarker(new MarkerOptions()
                            .position(wp.getLatLng())
//                .anchor(0.5f, 0.5f)
                            .draggable(true)

                    //        .snippet(wp.toString())
            );
            marker.setTag(wp);
            markers_.put(wp, marker);
        } else {
            marker = markers_.get(wp);
            marker.setPosition(wp.getLatLng());
        }

        marker.setTitle(String.format("%d", i));
        marker.setSnippet(wp.detail());
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


    private void updateWaypoint(Mavlink.MissionItem wp, int i, GoogleMap map) {

        switch (wp.command) {
            case Mavlink.MissionItem.MAV_CMD_NAV_WAYPOINT:
            case Mavlink.MissionItem.MAV_CMD_NAV_TAKEOFF: {

                waypointPolylist_.add(wp.getLatLng());
                setWaypointMarker(wp, i, map);
            } break;
        }
    }

    ArrayList<LatLng> waypointPolylist_ = new ArrayList<LatLng>();
    Mavlink.MissionPlan plan_ = null;
    GoogleMap map_ = null;

    public void update() {
        if(map_ != null && plan_ != null) {
            int i=0;
            waypointPolylist_.clear();
            for (Mavlink.MissionItem wp : plan_.mission.items) {
                updateWaypoint(wp, i++, map_);
            }
            updatePolyline(map_);
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
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Mavlink.MissionItem wp : plan.mission.items) {
            if (wp.hasLatLng()) {
                builder.include(wp.getLatLng());
            }
        }
        LatLngBounds bounds = builder.build();
        return bounds;
    }
}
