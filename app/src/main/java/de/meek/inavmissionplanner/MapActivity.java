package de.meek.inavmissionplanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.samsung.sprc.fileselector.FileOperation;
import com.samsung.sprc.fileselector.FileSelector;
import com.samsung.sprc.fileselector.OnHandleFileListener;

import java.io.InputStream;
import java.io.OutputStream;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap map_;
    Marker copterMarker_;
    LatLng copterPos_ = new LatLng(49.482248, 11.092563);
    Mavlink.MissionItem selectedWaypoint_ = null;
    Mavlink mavlin_ = new Mavlink();
    WaypointPlanner waypointPlanner_ = new WaypointPlanner();
    Mavlink.MissionPlan missonPlan_ = null;
    String chosenDir_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showControlsEditMission(false);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        _handlerUpdateUI.postDelayed(_runnableUpdateUI, Const.refreshRateUI);
        handlerUpdateCopterPos_.postDelayed(runnableUpdateCopterPos_, Const.refreshRateUI);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map_ = googleMap;
        map_.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        else {
            map_.setMyLocationEnabled(true);
        }
        map_.getUiSettings().setZoomControlsEnabled(true);
        map_.getUiSettings().setScrollGesturesEnabled(true);
        map_.setOnMarkerDragListener(this);
        map_.setOnMarkerClickListener(this);

        addCopterMarker();

        map_.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                selectedWaypoint_ = null;
            }
        });

        WaypointMarker wpMarker = new WaypointMarker(getLayoutInflater());
        map_.setInfoWindowAdapter(wpMarker);

        waypointPlanner_.setMap(map_);
        missonPlan_ = mavlin_.createEmptyMissionPlan();
        waypointPlanner_.setMissionPlan(missonPlan_);
    }

    private void addCopterMarker() {
        MarkerOptions o = new MarkerOptions()
                .position(copterPos_)
                .zIndex(10)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.drone));
        copterMarker_ = map_.addMarker(o);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
       selectedWaypoint_ = (Mavlink.MissionItem) marker.getTag();
        //if(wp!=null)
        {
//            waypointList.selectedIndex = wp.Number - 1;
//            waypointListAdapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Mavlink.MissionItem wp = (Mavlink.MissionItem) marker.getTag();
        if(null != wp) {
            wp.setLatLng(marker.getPosition());
        }

        updateWaypoints();
        //waypointListAdapter.notifyDataSetChanged();
    }

    private void updateWaypoints() {
        waypointPlanner_.update();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_load_mission:
                loadMission();
                return true;
            case R.id.action_save_mission:
                saveMission();
                return true;
            case R.id.action_clear_mission:
                clearMission();
                return true;
            case R.id.action_connect:
                getApp().connect();
                return true;
            case R.id.action_map_hybrid:
                setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.action_map_normal:
                setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.action_map_terrain:
                setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.action_map_satellite:
                setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.action_status:
                showActivityStatus();
                return true;
            case R.id.action_mode_settings:
                showActivitySettings();
                return true;
            case R.id.action_edit_mission:
                toggleControlsEditMission();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleControlsEditMission() {
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layoutEditMission);
        showControlsEditMission(layout.getVisibility() != View.VISIBLE);
    }

    private void showControlsEditMission(boolean visible)
    {
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layoutEditMission);
        layout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void showActivitySettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent,Const.ACTIVITY_SETTINGS);
    }

    private void setMapType(int i)
    {
        map_.setMapType(i);
    }

    OnHandleFileListener loadFileListener_ = new OnHandleFileListener() {
        @Override
        public void handleFile(final String filePath) {
//            Toast.makeText(MapActivity.this, "Load: " + filePath, Toast.LENGTH_SHORT).show();
            Uri uri = Uri.parse("file://" + filePath);
            String jsonStr = null;
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                missonPlan_ = mavlin_.loadMission(inputStream);
                waypointPlanner_.setMissionPlan(missonPlan_);
                LatLngBounds llb = waypointPlanner_.getWaypointBounds(missonPlan_);
                if (llb != null) {
                    setMapToRegion(llb);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void loadMission() {
        if (!checkPermissionForReadExtertalStorage()) {
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new FileSelector(MapActivity.this, FileOperation.LOAD, loadFileListener_, null).show();
    }

    OnHandleFileListener saveFileListener_ = new OnHandleFileListener() {
        @Override
        public void handleFile(final String filePath) {
//            Toast.makeText(MapActivity.this, "Save: " + filePath, Toast.LENGTH_SHORT).show();
            try {
                Uri uri = Uri.parse("file://" + filePath);
                String str = mavlin_.convertMissionToJSON(missonPlan_);
                OutputStream stream = getContentResolver().openOutputStream(uri);
                stream.write(str.getBytes());
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private void saveMission() {
        new FileSelector(MapActivity.this, FileOperation.SAVE, saveFileListener_, null).show();
    }

    private void clearMission() {
        selectedWaypoint_ = null;
        missonPlan_ = mavlin_.createEmptyMissionPlan();
        waypointPlanner_.setMissionPlan(missonPlan_);
        updateWaypoints();
    }

    void setMapToRegion(LatLngBounds llb) {
        if(llb != null)
        {
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(llb, 10);
            map_.moveCamera(cu);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == Const.REQUEST_CODE_LOAD_MISSION) && (resultCode == RESULT_OK)) {
            Uri uri = data.getData(); //The uri with the location of the file
            String jsonStr = null;
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                missonPlan_ = mavlin_.loadMission(inputStream);
                waypointPlanner_.setMissionPlan(missonPlan_);
                LatLngBounds llb = waypointPlanner_.getWaypointBounds(missonPlan_);
                if (llb != null) {
                    setMapToRegion(llb);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addWaypoint(LatLng ll, float altitude) {

        if (selectedWaypoint_ != null) {
            selectedWaypoint_.setLatLng(ll);
        } else {
            waypointPlanner_.addWaypoint(ll, altitude);
        }
        updateWaypoints();
    }

    public void onBtnDeleteWP(View v) {
        waypointPlanner_.deleteWaypoint(selectedWaypoint_);
        updateWaypoints();
    }

    public void onBtnMoveUpWP(View v) {
        waypointPlanner_.moveUpWaypoint(selectedWaypoint_);
        updateWaypoints();
    }

    public void onBtnMoveDownWP(View v) {
        waypointPlanner_.moveDownWaypoint(selectedWaypoint_);
        updateWaypoints();
    }

    public void onBtnAddCenterWP(View v) {
        LatLng center = map_.getCameraPosition().target;
        //int alt = 2; //Integer.valueOf(editAlt.getText().toString());
        //waypointList.add(new Waypoint(inavApp.theApp.waypointList.size(), center, alt, 5, 5, 5));
        addWaypoint(center, 2);
    }

    public void onBtnAddDroneWP(View v) {
        addWaypoint(copterPos_, 2);
    }

    public void onBtnEditWP(View v) {
        showPopup(selectedWaypoint_);
    }


    private void showPopup(final Mavlink.MissionItem wp) {

        if (wp == null)
            return;

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        View popupView = layoutInflater.inflate(R.layout.popup_waypoint, null);

        final PopupWindow popup = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

        popup.setTouchable(true);
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);


        final EditText alt = (EditText)popupView.findViewById(R.id.editAltitude);
        alt.setText( "" + wp.getAltitude() );

        ((Button) popupView.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        ((Button) popupView.findViewById(R.id.btnOK)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    float altitude = Float.parseFloat(alt.getText().toString());
                    wp.setAltitude(altitude);
                    popup.dismiss();
                } catch (Exception e) {
                }
            }
        });
    }

    private void showActivityStatus() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent,Const.ACTIVITY_MAIN);
    }

    public void onBtnStatus(View v) {
        showActivityStatus();
    }
    
    public IComm getComm() {
        return getApp().getComm();
    }
    
    public App getApp() {
        return App.getInstance();
    }

    public void onBtnConnect(View v) {
        if (getComm().isConnected())
            getApp().disconnect();
        else 
            getApp().connect();
    }
    
    Handler _handlerUpdateUI = new Handler();
    Runnable _runnableUpdateUI = new Runnable() {
        @Override
        public void run() {

            String txt = "";
            if (getComm().isConnected())
                txt += "  connected, ";
            else
                txt += "  disconnected, ";
            if (getData().gpsFix2d)
                txt += "2D fix: ";
            else if (getData().gpsFix3d)
                txt += "3D fix: ";
            else
                txt += "no fix: ";
            txt +=  + getData().gpsNumSats + " sats";

            ((TextView)findViewById(R.id.tvStatus)).setText(txt);
            _handlerUpdateUI.postDelayed(this, Const.refreshRateUI);
        }
    };

    MspHandler getData()
    {
        return getApp().getMsp();
    }

    Handler handlerUpdateCopterPos_ = new Handler();
    Runnable runnableUpdateCopterPos_ = new Runnable() {
        @Override
        public void run() {

            copterPos_ = new LatLng(getData().gpsLat / Math.pow(10, 7), getData().gpsLon / Math.pow(10, 7));
            copterMarker_.setPosition(copterPos_);
            handlerUpdateCopterPos_.postDelayed(this, Const.refreshRateUI);
        }
    };

}
