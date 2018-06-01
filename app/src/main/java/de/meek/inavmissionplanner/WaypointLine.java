package de.meek.inavmissionplanner;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaypointLine {

    Polyline polyline_ = null;
    ArrayList<LatLng> waypointList_ = new ArrayList<LatLng>();

    int color_ = Color.DKGRAY;
    List<PatternItem> pattern_ = null;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dash.
    public static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);


    WaypointLine(int color, List<PatternItem> pattern) {
        color_ = color;
        pattern_ = pattern;
    }

    public void clear() {
        waypointList_.clear();
    }

    public void add(LatLng ll) {
        waypointList_.add(ll);
    }

    public void update(GoogleMap map)
    {
        if(null == polyline_)
        {
            PolylineOptions rectOptions = new PolylineOptions();
            rectOptions.color(color_);
            polyline_ = map.addPolyline(rectOptions);
            if (pattern_ != null) {
                polyline_.setPattern(pattern_);
            }
        }
        polyline_.setPoints(waypointList_);
    }

}
