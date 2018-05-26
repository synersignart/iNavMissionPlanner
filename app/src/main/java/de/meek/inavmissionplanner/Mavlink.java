package de.meek.inavmissionplanner;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class Mavlink {

    class MissionPlan
    {
        String fileType;
        String groundStation;
        Mission mission;
    }

    public void createEmptyMissionPlan()
    {
        plan_ = new MissionPlan();
        plan_.mission = new Mission();
        plan_.mission.items = new ArrayList<>();
    }

    public static class Mission
    {
        int cruiseSpeed;
        int firmwareType;
        int hoverSpeed;
        ArrayList<MissionItem> items;
    }

    public static class MissionItem
    {
        final public static int MAV_CMD_NAV_WAYPOINT = 16;
        final public static int MAV_CMD_NAV_TAKEOFF = 22;
        final public static int MAV_CMD_DO_CHANGE_SPEED = 178;

        boolean autoContinue;
        int command;
        int doJumpId;
        int frame;
        ArrayList<Float> params;

        public void initParams() {
            int n = 7;
            params = new ArrayList<>(n);
            for(int i=0; i<n; i++) {
                params.add(0f);
            }
        }

        public float getLat() {
            return params.get(4);
        }
        public float getLon() {
            return params.get(5);
        }
        public float getAltitude() {
            return params.get(6);
        }
        public float getHoldTime() {
            return params.get(0);
        }
        public float getSpeed() {
            return params.get(1);
        }
        public LatLng getLatLng() {
            return new LatLng(getLat(), getLon());
        }

        public void setLatLng(LatLng ll) {
            params.set(4, (float)ll.latitude);
            params.set(5, (float)ll.longitude);
        }

        public void setAltitude(float altitude) {
            params.set(6, altitude);
        }

        public boolean hasLatLng() {
            switch (command) {
                case Mavlink.MissionItem.MAV_CMD_NAV_WAYPOINT:
                case Mavlink.MissionItem.MAV_CMD_NAV_TAKEOFF:
                    return true;
                default:
                    return false;
            }
        }
        public String detail() {
            String str =String.valueOf(getLat()) + "/" + String.valueOf(getLon()) + "\n";
            str += "" + command + "\n";
            str += String.format("Alt(%f), Flag(%f)", getAltitude(), getSpeed());
            return str;
        }
    }

    class MissionItemParams
    {
        float param1;
        float param2;
        float param3;
        float param4;
        float x;
        float y;
        float z;
    }

    MissionPlan plan_ = null;

    public MissionPlan getMissionPlan() {
        return plan_;
    }

    public boolean parseMission(String str) {

        Gson gson = new Gson();
        plan_ = gson.fromJson(str, MissionPlan.class);
//        updateWaypoints(plan);
        return true;
    }

    public String getStringFromInputStream(InputStream stream) throws IOException
    {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    public boolean loadMission(InputStream inputStream) {
        String jsonStr = null;
        try {
            jsonStr = getStringFromInputStream(inputStream);
            return parseMission(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
