package de.meek.inavmissionplanner;

import java.util.Map;
import java.util.TreeMap;

public class Const {
    public static final int ACTIVITY_SCAN_BLUETOOTH_DEVICES = 33;
    public static final int ACTIVITY_SETTINGS = 34;
    public static final int ACTIVITY_MAP = 35;
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    public static final int REQUEST_CODE_LOAD_MISSION = 123;
    public static final int REQUEST_CODE_SAVE_MISSION = 124;

    public static final int refreshRateUI = 100;


    public static class Box {
        int boxId;
        String name;
        int permanentId;

        private Box( int boxId, String name, int permanentId) {
            this.boxId = boxId;
            this.name = name;
            this.permanentId = permanentId;
        }
    }

    static Box[] boxes = {
            new Box(0, "ARM", 0  ),
            new Box(1, "ANGLE", 1 ),
            new Box(2, "HORIZON", 2 ),
            new Box(3, "NAV ALTHOLD", 3 ),   // old BARO
            new Box(4, "HEADING HOLD", 5 ),
            new Box(5, "HEADFREE", 6 ),
            new Box(6, "HEADADJ", 7 ),
            new Box(7, "CAMSTAB", 8 ),
            new Box(8, "NAV RTH", 10 ),         // old GPS HOME
            new Box(9, "NAV POSHOLD", 11 ),     // old GPS HOLD
            new Box(10, "MANUAL", 12 ),
            new Box(11, "BEEPER", 13 ),
            new Box(12, "LEDLOW", 15 ),
            new Box(13, "LIGHTS", 16 ),
            new Box(15, "OSD SW", 19 ),
            new Box(16, "TELEMETRY", 20 ),
            new Box(28, "AUTO TUNE", 21 ),
            new Box(17, "BLACKBOX", 26 ),
            new Box(18, "FAILSAFE", 27 ),
            new Box(19, "NAV WP", 28 ),
            new Box(20, "AIR MODE", 29 ),
            new Box(21, "HOME RESET", 30 ),
            new Box(22, "GCS NAV", 31 ),
            //new Box(BOXHEADINGLOCK, "HEADING LOCK", 32 ),
            new Box(24, "SURFACE", 33 ),
            new Box(25, "FLAPERON", 34 ),
            new Box(26, "TURN ASSIST", 35 ),
            new Box(14, "NAV LAUNCH", 36 ),
            new Box(27, "SERVO AUTOTRIM", 37 ),
            new Box(23, "KILLSWITCH", 38 ),
            new Box(29, "CAMERA CONTROL 1", 39 ),
            new Box(30, "CAMERA CONTROL 2", 40 ),
            new Box(31, "CAMERA CONTROL 3", 41 )
    };

    static String BoxModeId2String(int id) {
        for( Box b : boxes) {
            if (b.permanentId == id) {
                return b.name;
            }
        }
        return "?";
    }

}
