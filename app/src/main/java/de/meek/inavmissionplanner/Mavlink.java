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

    public MissionPlan createEmptyMissionPlan()
    {
        MissionPlan plan = new MissionPlan();
        plan.mission = new Mission();
        plan.mission.items = new ArrayList<>();
        return plan;
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

    public MissionPlan createMissonPlanFromJSON(String str) {

        Gson gson = new Gson();
        MissionPlan plan = gson.fromJson(str, MissionPlan.class);
//        updateWaypoints(plan);
        return plan;
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

    public MissionPlan loadMission(InputStream inputStream) {
        String jsonStr = null;
        try {
            jsonStr = getStringFromInputStream(inputStream);
            return createMissonPlanFromJSON(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String convertMissionToJSON(MissionPlan plan) {
        Gson gson = new Gson();
        String str = gson.toJson(plan);
        return str;
    }
}
