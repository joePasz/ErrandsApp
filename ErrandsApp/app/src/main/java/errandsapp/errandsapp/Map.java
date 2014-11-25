package errandsapp.errandsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        createMapView();

        //Initialize global variables
        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
        }
        orderedDestinations = new ArrayList<Destination>();

        //Retrieve ordered destinations from intent
        Intent intent = getIntent();
        double[] destLongs = intent.getDoubleArrayExtra("dLong");
        double[] destLats = intent.getDoubleArrayExtra("dLat");
        ArrayList<String> destNames = intent.getStringArrayListExtra("dName");
        for (int i = 0; i < destNames.size(); i++) {
            Destination tempDest = new Destination(destNames.get(i), destLongs[i], destLats[i]);
            orderedDestinations.add(tempDest);
        }

        //Retrieve encodedPolyString from intent and decode it
        String polylineEncodedString = intent.getStringExtra("polyString");
        ArrayList<LatLng> routeLatLongs = decodePoly(polylineEncodedString);

        //Build polyline using the array of latlngs built from the decoded poly string
        PolylineOptions rectOptions = new PolylineOptions();
        for (int i = 0; i < routeLatLongs.size(); i++) {
            rectOptions.add(routeLatLongs.get(i));
        }
        Polyline polyline = googleMap.addPolyline(rectOptions);

        //Determine if the beginning and end are the same destinations
        boolean beginAndEndIsEqual = false;
        if (orderedDestinations.get(0).equals(orderedDestinations.get(orderedDestinations.size() - 1))) {
            beginAndEndIsEqual = true;
        }

        //Adds all the markers to the map for each destination
        for (int i = 0; i < orderedDestinations.size(); i++) {
            Destination tempDest = orderedDestinations.get(i);
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(tempDest.latitude, tempDest.longitude))
                    .title((i + 1) + ". " + tempDest.name);
            if (i == 0) {
                //mark the beginning green
                if (beginAndEndIsEqual) {
                    marker.title((i + 1) + ". " + tempDest.name + " (Begin and Ending)");
                }
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (i == orderedDestinations.size() - 1) {
                //mark the ending red, but only if it is not the same as the beginning
                if (!beginAndEndIsEqual) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            } else {
                //all other points are blue
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            googleMap.addMarker(marker);
        }

        //determine the bounds of the map based on the polyline points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng coordinate : routeLatLongs) {
            builder.include(coordinate);
        }
        LatLngBounds bounds = builder.build();

        //animate camera to the determined bounds
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 450, 450, padding);
        googleMap.animateCamera(cu);
    }


    /*
    This creates the mapfragment
     */
    private void createMapView() {
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if (null == googleMap) {
                    // Toast.makeText(getApplicationContext(),
                    //       "Error creating map",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }
    }

    //Method found from the gentleman at
    //http://stackoverflow.com/questions/15924834/decoding-polyline-with-new-google-maps-api
    //Thank you good sir. You are doing gods work.
    //Takes a google encoded polyline and returns an ArrayList of LatLngs
    private ArrayList<LatLng> decodePoly(String encoded) {

        //Log.i("Location", "String received: " + encoded);
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

//        for (int i = 0; i < poly.size(); i++) {
//            Log.i("Location", "Point sent: Latitude: " + poly.get(i).latitude + " Longitude: " + poly.get(i).longitude);
//        }
        return poly;
    }


}
