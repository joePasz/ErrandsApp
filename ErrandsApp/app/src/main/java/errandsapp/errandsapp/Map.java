package errandsapp.errandsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by JenniferTurner on 11/11/14.
 */
public class Map extends FragmentActivity {

    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private ArrayList<LatLng> stepCoordinates;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_map, container, false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_map);
       createMapView();

       if (googleMap != null) {
           googleMap.setMyLocationEnabled(true);
       }

        Intent intent = getIntent();
        stepCoordinates = new ArrayList<LatLng>();
        double[] stepLongs = intent.getDoubleArrayExtra("sLong");
        double[] stepLats = intent.getDoubleArrayExtra("sLat");
        for(int i = 0; i < stepLongs.length; i++){
            LatLng tempLocation = new LatLng(stepLats[i], stepLongs[i]);
            stepCoordinates.add(tempLocation);
        }

        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(37.35, -122.0));
        coordinates.add(new LatLng(37.45, -122.0));
        coordinates.add(new LatLng(37.45, -122.2));
        coordinates.add(new LatLng(37.35, -122.2));

       // Instantiates a new Polyline object and adds points to define a rectangle
//       PolylineOptions rectOptions = new PolylineOptions()
//               .add(new LatLng(37.35, -122.0))
//               .add(new LatLng(37.45, -122.0))  // North of the previous point, but at the same longitude
//               .add(new LatLng(37.45, -122.2))  // Same latitude, and 30km to the west
//               .add(new LatLng(37.35, -122.2))  // Same longitude, and 16km to the south
//               .add(new LatLng(37.35, -122.0)); // Closes the polyline.
// Get back the mutable Polyline
        PolylineOptions rectOptions = new PolylineOptions();
        for(int i = 0; i < stepCoordinates.size(); i++) {
            rectOptions.add(stepCoordinates.get(i));
        }

       Polyline polyline = googleMap.addPolyline(rectOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng coordinate : stepCoordinates) {
            builder.include(coordinate);
        }
        LatLngBounds bounds = builder.build();

        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 450,450,padding);

        googleMap.animateCamera(cu);

    }


    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                   // Toast.makeText(getApplicationContext(),
                     //       "Error creating map",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }









    }
