package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.TextView;

import java.util.ArrayList;


public class MainScreen extends Activity implements LocationListener {

//    private Destination[] destinations;
    private ArrayList<Destination> destinations;
    private Button searchButton;
    private Button addCurrentLocationButton;
    private Button buildRouteButton;
    private TableLayout table;
    private LocationManager locationManager;
    private Location currentLocation;
    LayoutInflater inflater;

    private final String TAG = ((Object) this).getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ In onCreate() ++");
        setContentView(R.layout.activity_main_screen);

        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        //Just an ugly, quick, and dirty way to build an array of these destination object
//        Destination dest = new Destination("Starbucks",32.04947,-40.2381);
        destinations = new ArrayList<Destination>();
//        for(int i = 0; i < 5; i++){
//            destinations.add(dest);
//        }

        //build main table
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isWifiEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(locationManager != null && isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(currentLocation != null) {
                Log.e(TAG, "Long: " + currentLocation.getLongitude() + " Lat: " + currentLocation.getLatitude());
            }
        } else if(locationManager != null && isWifiEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(currentLocation != null) {
                Log.e(TAG, "Long: " + currentLocation.getLongitude() + " Lat: " + currentLocation.getLatitude());
            }
        }


        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
        buildTable();

        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Search.class);
                //starts the search activity with an id of 0
                startActivityForResult(intent, 0);
            }

        });
        addCurrentLocationButton = (Button) findViewById(R.id.addCurrentLocationButton);
        addCurrentLocationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Destination dest = new Destination("Current Location",currentLocation.getLongitude(),currentLocation.getLatitude());
                destinations.add(dest);
                //rebuild table
                buildTable();
            }

        });

        buildRouteButton = (Button) findViewById(R.id.buildRouteButton);
        buildRouteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BuildRoute.class);
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=";
                Integer lastIndex = destinations.size() - 1;
                String originName;
                String deName;

                originName = destinations.get(0).longitude + "," + destinations.get(0).latitude;
                deName = destinations.get(lastIndex).longitude + "," + destinations.get(lastIndex).latitude;

                urlString = urlString + originName + "&destination=" + deName + "&waypoints=";

                for(int i=0; i<lastIndex; i++){
                    String locString;
                    locString = destinations.get(i).longitude + "," + destinations.get(i).latitude;

                    if(i == (lastIndex-1)){
                    urlString = urlString + locString;
                    }
                    else{
                        urlString = urlString + locString + "|";
                    }
                }

                urlString = urlString + "&key=AIzaSyDgoZ4AG4pxViHeKbAHEChnDrknUNmQIYY";

                intent.putExtra("dString", urlString);

                startActivityForResult(intent, 1);
            }

        });


    }

    //This first removes all views within the table if there are any, then builds
    //it from scratch based on the contents of the Array List Destinations
    public boolean buildTable() {
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);

            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the table_row_attributes.xml file
            // not sure what inflates does, but I think I am doing this right....
            TableRow row = (TableRow) inflater.inflate(R.layout.table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.column_1)).setText(destinations.get(i).name);
            ((TextView)row.findViewById(R.id.column_2)).setText(destinations.get(i).longitude + ":" + destinations.get(i).latitude);
            table.addView(row);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    protected void onResume() {
        super.onResume();
        Log.e(TAG, "++ In onResume() ++");
    }
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "++ In onStart() ++");

    }
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "++ In onRestart() ++");

    }
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "++ In onPause() ++");

    }
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "++ In onStop() ++");

    }
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "++ In onDestroy() ++");

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This method is called once the activity it started ends
    //(When the Search AActivity finishes, this is called)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            //The 0 case was set earlier on as the case for the Search Activity
            case (0) : {
                if (resultCode == Activity.RESULT_OK) {
                    String newText = data.getStringExtra("dName");
                    double newLong = data.getDoubleExtra("dLong",0.0);
                    double newLat = data.getDoubleExtra("dLat",0.0);
                    Destination dest = new Destination(newText,newLong,newLat);
                    dest.address = data.getStringExtra("dAddress");
                    destinations.add(dest);
                    //rebuild table
                    buildTable();
                }
                break;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

