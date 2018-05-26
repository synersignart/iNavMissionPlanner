package de.meek.inavmissionplanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

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

import java.io.InputStream;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap map_;
    Marker copterMarker_;
    LatLng copterPos_ = new LatLng(49.482248, 11.092563);
    Mavlink.MissionItem selectedWaypoint_ = null;
    Mavlink mavlin_ = new Mavlink();
    WaypointPlanner waypointPlanner_ = new WaypointPlanner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                // Do whatever you want to do here...
                selectedWaypoint_ = null;
            }
        });

        waypointPlanner_.setMap(map_);
        mavlin_.createEmptyMissionPlan();
        waypointPlanner_.setMissionPlan(mavlin_.getMissionPlan());
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadMission() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select mission"), 123);
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
        if(requestCode==123 && resultCode==RESULT_OK) {
            Uri uri = data.getData(); //The uri with the location of the file
            String jsonStr = null;
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (mavlin_.loadMission(inputStream)) {
                    waypointPlanner_.setMissionPlan(mavlin_.getMissionPlan());
                    LatLngBounds llb = waypointPlanner_.getWaypointBounds(mavlin_.getMissionPlan());
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
        int alt = 2; //Integer.valueOf(editAlt.getText().toString());
        //waypointList.add(new Waypoint(inavApp.theApp.waypointList.size(), center, alt, 5, 5, 5));
        addWaypoint(center, 2);
    }


}
