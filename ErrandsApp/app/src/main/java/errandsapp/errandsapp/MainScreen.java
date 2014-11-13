package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


public class MainScreen extends Activity implements LocationListener {

//    private Destination[] destinations;
    private ArrayList<Destination> destinations;
    private Button searchButton;
    private Button addCurrentLocationButton;
    private Button buildRouteButton;
    private Button deleteDestButton;
    private Button favoriteDestButton;
    private Button destStartButton;
    private Button destEndButton;

    private Destination startLocation;
    private Destination endLocation;


    private TableLayout table;
    private LocationManager locationManager;
    private Location currentLocation;
    LayoutInflater inflater;

    private ArrayList<String> listOfDestNames;
    private double[] listOfDestLong;
    private double[] listOfDestLat;

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
        buildTestData();
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
                ArrayList<Destination> tempDest;

                tempDest = sortDestinations();
                Intent intent = new Intent(getApplicationContext(), BuildRoute.class);
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=";
                Integer destSize = tempDest.size();

                listOfDestLong = new double[destSize];
                listOfDestLat = new double[destSize];
                listOfDestNames = new ArrayList<String>();

                for(int i=0; i<destSize; i++){
                    Log.e(TAG, "inside For Loop to grab names");
                    listOfDestNames.add(i,tempDest.get(i).name);
                    Log.e(TAG, "Past add to Names ArrayList");
                    listOfDestLong[i] = tempDest.get(i).longitude;
                    listOfDestLat[i] = tempDest.get(i).latitude;

                }


                intent.putExtra("dName", listOfDestNames);
                intent.putExtra("dLong", listOfDestLong);
                intent.putExtra("dLat", listOfDestLat);

                startActivityForResult(intent, 1);
            }

        });


    }

    //This first removes all views within the table if there are any, then builds
    //it from scratch based on the contents of the Array List Destinations
    public boolean buildTable() {
        int count = table.getChildCount();
//        table.removeAllViews();
        for (int i = count - 1; i >= 0; i--) {
            View child = table.getChildAt(i);

            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
            if (child instanceof TableRow) table.removeView(child);
        }
        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the table_row_attributes.xml file
            // not sure what inflates does, but I think I am doing this right....
            TableRow row = (TableRow) inflater.inflate(R.layout.table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.desti)).setText(destinations.get(i).name);
            ((TextView)row.findViewById(R.id.address)).setText(destinations.get(i).address);
            row.findViewById(R.id.start_button).setTag(i);
            row.findViewById(R.id.end_Button).setTag(i);

            row.setTag(i);
            row.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View arg0) {
//                    arg0.setBackgroundColor(Color.rgb(226, 11, 11));
                    TableRow longClickView = (TableRow) inflater.inflate(R.layout.long_click_layout, null);
//                    ((TextView)longClickView.findViewById(R.id.column_1)).setText("TEST");
                    int height =  arg0.getHeight();

                    int tag = (Integer)arg0.getTag();
                    ViewGroup tempTable = (ViewGroup)arg0.getParent();
                    int index = tempTable.indexOfChild(arg0);
                    ((Button)longClickView.findViewById(R.id.delete)).setTag(tag);
                    longClickView.setMinimumHeight(height);
                    tempTable.removeView(arg0);
                    tempTable.addView(longClickView, index);

                    return true;
                }
            });
            table.addView(row);
        }
        if(startLocation != null){

            colorStarts(destinations.indexOf(startLocation));
        }

        if(endLocation != null){
            colorEnds(destinations.indexOf(endLocation));
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
        if(destinations.size() > 0){
            buildTable();
        }
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
        if (id == R.id.action_gps) {
            clickGPS();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteClick(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            ViewGroup tempRow = (ViewGroup)v.getParent();
            ViewGroup tempTable = (ViewGroup)tempRow.getParent();
            tempTable.removeView(tempRow);

            if(startLocation == destinations.get(cellNumber)){
                startLocation = null;
            }
            if(endLocation == destinations.get(cellNumber)){
                endLocation = null;
            }
            destinations.remove(cellNumber);
        }
        buildTable();
    }

    public void cancelClick(View v){
        buildTable();
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

    public void buildTestData() {
        Destination tempDist1= new Destination("The Ohio State University - Dreese Laboratories", -83.015941, 40.002357);
        Destination tempDist2= new Destination("Raising Cane's", -83.007699, 39.999338);
        Destination tempDist3= new Destination("Chipotle Mexican Grill",-83.007168, 39.997513);
        Destination tempDist4= new Destination("Lazenby Hall",-83.015635, 39.998839);
        Destination tempDist5= new Destination("Caffe Apropos",-83.017099, 39.983966);


        destinations.add(tempDist1);
        destinations.add(tempDist2);
        destinations.add(tempDist4);
        destinations.add(tempDist3);
        destinations.add(tempDist5);

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

    public void clickGPS(){
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /**
     * Sorts destinations and appropriately puts the first destination at the front of the array and the final destination at the end of the array.
     * Destination array must have size > 2
     */
    public ArrayList<Destination> sortDestinations() {
        ArrayList<Destination> tempDest;
        tempDest = new ArrayList<Destination>(destinations);
        for(int i = 0; i < tempDest.size(); i++) {
            Destination test = tempDest.get(i);
            if (startLocation != null && test.name.equals(startLocation.name)) {
                tempDest.remove(i);
                break;
            }
        }

        for(int i=0; i<tempDest.size(); i++) {
            Destination test = tempDest.get(i);
            if(endLocation !=null && test.name.equals(endLocation.name)){
                tempDest.remove(i);
                break;
            }
        }
        if(startLocation != null){
            tempDest.add(0,startLocation);
        }
        if(endLocation != null){
            tempDest.add(endLocation);
        }

        return tempDest;
    }

    public void startClicked(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            Log.d(TAG, "cell: " + v.getTag() + " Clicked!!!!");
            colorStarts(cellNumber);
            startLocation = destinations.get(cellNumber);


        }
    }

    public void endClicked(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            Log.d(TAG, "cell: " + v.getTag() + " Clicked!!!!");
            colorEnds(cellNumber);
            endLocation = destinations.get(cellNumber);


        }
    }

    public void colorStarts(int i){
        int count = table.getChildCount();
        for (int j = count - 1; j >= 0; j--) {
            View child = table.getChildAt(j);

            if (child instanceof TableRow){
                Button startTemp = (Button)child.findViewById(R.id.start_button);
                if ((Integer)startTemp.getTag() == i){
                    startTemp.setBackgroundColor(0xFF00CC00);

                }
                else{
                    startTemp.setBackgroundColor(0xFF99EB99);
                }

            };
        }
    }

    public void colorEnds(int i){
        int count = table.getChildCount();
        for (int j = count - 1; j >= 0; j--) {
            View child = table.getChildAt(j);

            if (child instanceof TableRow){
                Button endTemp = (Button)child.findViewById(R.id.end_Button);
                if ((Integer)endTemp.getTag() == i){
                    endTemp.setBackgroundColor(0xFFFF0000);

                }
                else{
                    endTemp.setBackgroundColor(0xFFFF9999);
                }

            };
        }

    }

}

