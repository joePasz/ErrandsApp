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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by JenniferTurner on 11/11/14.
 */
public class Map extends FragmentActivity {
    private ArrayList<Destination> orderedDestinations;
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

        orderedDestinations = new ArrayList<Destination>();

        Intent intent = getIntent();
        stepCoordinates = new ArrayList<LatLng>();
        double[] stepLongs = intent.getDoubleArrayExtra("sLong");
        double[] stepLats = intent.getDoubleArrayExtra("sLat");
        for(int i = 0; i < stepLongs.length; i++){
            LatLng tempLocation = new LatLng(stepLats[i], stepLongs[i]);
            stepCoordinates.add(tempLocation);
        }

        double[] destLongs = intent.getDoubleArrayExtra("dLong");
        double[] destLats = intent.getDoubleArrayExtra("dLat");
        ArrayList<String> destNames = intent.getStringArrayListExtra("dName");
        for(int i = 0; i < destNames.size(); i++){
            Destination tempDest = new Destination(destNames.get(i),destLongs[i], destLats[i]);
            orderedDestinations.add(tempDest);
        }

        PolylineOptions rectOptions = new PolylineOptions();
        for(int i = 0; i < stepCoordinates.size(); i++) {
            rectOptions.add(stepCoordinates.get(i));
        }

       Polyline polyline = googleMap.addPolyline(rectOptions);

        for(int i = 0; i < orderedDestinations.size(); i++) {
            Destination tempDest = orderedDestinations.get(i);
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(tempDest.latitude, tempDest.longitude))
                    .title((i+1) + ". " + tempDest.name);
            if(i == 0) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (i == orderedDestinations.size()-1) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            googleMap.addMarker(marker);
        }

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
