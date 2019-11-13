package com.example.crowmap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.data.kml.KmlLayer;

import java.io.File;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
            requestPermission();
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);

        ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE))
                .requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000,
                        3,
                        locationListenerGPS);
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            LatLng coordinate = new LatLng(location.getLatitude(),
                    location.getLongitude());
            Point mappoint = mMap.getProjection().toScreenLocation(coordinate);
            mappoint.set(mappoint.x, mappoint.y - 300); // change these values as you need , just hard coded a value if you want you can give it based on a ratio like using DisplayMetrics  as well
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mMap.getProjection().fromScreenLocation(mappoint)));
            if (location.hasBearing() && !mMap.isMyLocationEnabled()) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(coordinate)             // Sets the center of the map to current location
                        .bearing(location.getBearing())// Sets the orientation of the camera to east
                        .zoom(mMap.getCameraPosition().zoom)
                        .tilt(0)// Sets the tilt of the camera to 0 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Write External Storage permission allows us to read  files." +
                    "Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        File sdcard = new File("/mnt/sdcard/");
        File file = new File(sdcard, "Crow.mbtiles");

        if (!file.exists())
            Toast.makeText(this, "File not Found" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        LatLng crow = new LatLng(49.217314, -93.863248);
        mMap.addMarker(new MarkerOptions().position(crow).title("Marker in Crow"));

        TileProvider tileProvider = new ExpandedMBTilesTileProvider(file, 256, 256);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
        try {
            new KmlLayer(mMap, R.raw.points, getApplicationContext()).addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crow, (float) 12.0));
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);
        mMap.setOnMapLongClickListener(onMyMapLongClickListener);
    }

    private GoogleMap.OnMapLongClickListener onMyMapLongClickListener =
            new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {
                    Toast.makeText(getApplicationContext(), "MyLocationClicked " + mMap.isMyLocationEnabled(), Toast.LENGTH_LONG).show();
                    LatLng coordinate = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15));
                    if (mMap.isMyLocationEnabled())
                        mMap.setMyLocationEnabled(false);
                    else
                        mMap.setMyLocationEnabled(true);
                }
            };
}
