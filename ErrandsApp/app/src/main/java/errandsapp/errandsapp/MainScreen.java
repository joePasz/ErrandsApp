package errandsapp.errandsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


public class MainScreen extends Activity implements LocationListener {

    //Current Destinations, all recent destinations, and all favorite destinatinos
    private ArrayList<Destination> destinations;
    private ArrayList<Destination> recentDestinations;
    private ArrayList<Destination> favoriteDestinations;

    //Used to keep track of which location has been selected to be start/end
    private Destination startLocation;
    private Destination endLocation;

    //the database helped
    private DatabaseHelper dbHelper;

    //Additional helpful global variables
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

        //initilize global variables
        recentDestinations = new ArrayList<Destination>();
        favoriteDestinations = new ArrayList<Destination>();
        destinations = new ArrayList<Destination>();
        dbHelper = new DatabaseHelper(getApplicationContext());
        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        //builds test data
        buildTestData();

        //Sets location abilities up
        establishLocation();


        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
        buildTable();

        //establishes onClickListeners for each button
        initializeButtons();
    }

    /*
    This is the large method in charge of updating the table and buttons to reflect the current
      state of the backend.
    So this first initializes the table, then it is called whenever destinations are added/removed
     */
    public boolean buildTable() {
        //This first enables or disables the build route if there is enough destinations in list
        Button tempBuildRouteButton = (Button) findViewById(R.id.buildRouteButton);
        if(destinations.size()>2) {
            tempBuildRouteButton.setEnabled(true);
        } else {
            tempBuildRouteButton.setEnabled(false);
        }

        //This removes all the current rows of the table
        int count = table.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
            if (child instanceof TableRow) table.removeView(child);
        }

        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the table_row_attributes.xml file
            TableRow row = (TableRow) inflater.inflate(R.layout.table_row_attributes, null);

            //Finds oritentation and alters the row width if in landscape
            if(getResources().getConfiguration().orientation == 2) {
                LinearLayout ll = ((LinearLayout)row.findViewById(R.id.text_layout));
                ll.getLayoutParams().width = 1350;
                ll.requestLayout();
            }
            //Sets the tags for each button(start and end) and their default colors
            ((TextView)row.findViewById(R.id.desti)).setText(destinations.get(i).name);
            ((TextView)row.findViewById(R.id.address)).setText(destinations.get(i).address);
            ImageButton startButton = (ImageButton)row.findViewById(R.id.start_button);
            startButton.setTag(i);
            startButton.setColorFilter(Color.argb(255, 150,200,150));
            ImageButton endButton = (ImageButton)row.findViewById(R.id.end_Button);
            endButton.setTag(i);
            endButton.setColorFilter(Color.argb(255, 200,150,150));
            row.setTag(i);

            //sets the longClickListener for each row
            row.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View arg0) {
                    TableRow longClickView = (TableRow) inflater.inflate(R.layout.long_click_layout, null);
                    int height =  arg0.getHeight();
                    int tag = (Integer)arg0.getTag();
                    ViewGroup tempTable = (ViewGroup)arg0.getParent();
                    int index = tempTable.indexOfChild(arg0);

                    //Finds oritentation and alters the row width if in landscape
                    if(getResources().getConfiguration().orientation == 2) {
                        LinearLayout ll = ((LinearLayout)longClickView.findViewById(R.id.long_click));
                        ll.getLayoutParams().width = 1700;
                        ll.requestLayout();
                    }

                    ((ImageButton)longClickView.findViewById(R.id.delete)).setTag(tag);
                    ImageButton deleteButton = (ImageButton)longClickView.findViewById(R.id.delete);
                    deleteButton.setColorFilter(Color.argb(255, 255,0,0)); // White Tint
                    ImageButton favButton = ((ImageButton)longClickView.findViewById(R.id.favorite));
                    favButton.setTag(tag);
                    int favLoc = locationOfFavorite(destinations.get(tag));
                    if(favLoc == -1) {
                        favButton.setColorFilter(Color.argb(0, 255,255,255));
                    } else {
                        favButton.setColorFilter(Color.argb(255, 255,255,0));
                    }
                    longClickView.setMinimumHeight(height);
                    tempTable.removeView(arg0);
                    tempTable.addView(longClickView, index);

                    return true;
                }
            });
            table.addView(row);
        }

        //If there is a start or end location selected, correctly paint the buttons
        if(startLocation != null){
            colorStarts(destinations.indexOf(startLocation));
        }
        if(endLocation != null){
            colorEnds(destinations.indexOf(endLocation));
        }
        return true;
    }

    /*
    This methods grabs all the buttons on the main screen (Build Route, Search, Add Current Location)
    and sets up their onclicklisteners
     */
    public void initializeButtons(){
        //establishes the Search Intent if search is selected
        ImageButton searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Search.class);
                //starts the search activity with an id of 0
                startActivityForResult(intent, 0);
            }

        });

        //Adds the current location to the list if this button is selected
        ImageButton addCurrentLocationButton = (ImageButton) findViewById(R.id.addCurrentLocationButton);
        addCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checks that there is a current location found
                if(currentLocation != null) {
                    Destination dest = new Destination("Current Location",currentLocation.getLongitude(),currentLocation.getLatitude());
                    if(!checkRepeatedDestination(dest)) {
                        destinations.add(dest);
                        //rebuild table
                        buildTable();
                    }
                } else {
                    //Creates a dialog if there is no location
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
                    builder.setMessage("Can not find location. Go to GPS Settings")
                            .setCancelable(false)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    clickGPS();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        });

        //Button for creating the BuildRoute Intent when selected
        Button buildRouteButton = (Button) findViewById(R.id.buildRouteButton);
        buildRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Seperates the destinations into a 3 arrays representing their Names, Longs, and Lats
                ArrayList<Destination> tempDest;

                tempDest = sortDestinations();
                Intent intent = new Intent(getApplicationContext(), BuildRoute.class);
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=";
                Integer destSize = tempDest.size();

                double[] listOfDestLong = new double[destSize];
                double[] listOfDestLat = new double[destSize];
                ArrayList<String> listOfDestNames = new ArrayList<String>();
                ArrayList<String> listOfDestAddr = new ArrayList<String>();

                for(int i=0; i<destSize; i++){
                    listOfDestNames.add(i,tempDest.get(i).name);
                    listOfDestLong[i] = tempDest.get(i).longitude;
                    listOfDestLat[i] = tempDest.get(i).latitude;
                    listOfDestAddr.add(tempDest.get(i).address);
                }

                //Attaches these arrays to the Intent to be reassembled in the other activity
                intent.putExtra("dName", listOfDestNames);
                intent.putExtra("dLong", listOfDestLong);
                intent.putExtra("dLat", listOfDestLat);
                intent.putExtra("dAddr", listOfDestAddr);

                startActivityForResult(intent, 1);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    protected void onResume() {
        super.onResume();
        //Rebuilds the recent and favorite destinations, then rebuilds the table
        recentDestinations = dbHelper.rlSelectAll();
        favoriteDestinations = dbHelper.favSelectAll();
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
        } else if (id == R.id.action_recent) {
            clickRecent();
            return true;
        } else if (id == R.id.action_favorites) {
            clickFavorites();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
      This is called when ever the trash icon is clicked on a highlighted destination
      It is in charge of removing the destination and updating the table
     */
    public void deleteClick(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            ViewGroup tempRow = (ViewGroup)v.getParent();
            ViewGroup tempTable = (ViewGroup)tempRow.getParent();
            tempTable.removeView(tempRow);

            //Makes sure to clear this destinations from start/end if needed
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

    /*
      This is called when ever the favorite icon is clicked on a highlighted destination
      It is in charge of adding or removing it from the favorites database and coloring
      the icon accordingly
     */
    public void favoriteClick(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            Destination tempFavDest = destinations.get(cellNumber);
            int favLoc = locationOfFavorite(tempFavDest);
            ImageButton imageView = (ImageButton)v;
            if(favLoc == -1){
                addFavoriteDestination(tempFavDest);
                imageView.setColorFilter(Color.argb(255, 255,255,0)); // White Tint
            } else {
                removeFavoriteDestination(tempFavDest, favLoc);
                imageView.setColorFilter(Color.argb(0, 255,255,255)); // White Tint
            }
        }
    }

    //Lazy way to rebuild the table if cancel is clicked on a highlighted destination.
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
                    //This extracts the destination details from the intent and adds it to the destination
                    String newText = data.getStringExtra("dName");
                    double newLong = data.getDoubleExtra("dLong",0.0);
                    double newLat = data.getDoubleExtra("dLat",0.0);
                    Destination dest = new Destination(newText,newLong,newLat);
                    dest.address = data.getStringExtra("dAddress");
                    //ensures it is not repeated first, and then also adds it to the recent database
                    if(!checkRepeatedDestination(dest)){
                        destinations.add(dest);
                    }
                    addRecentDestination(dest);

                    //rebuild table
                    buildTable();
                }
                break;
            }

        }
    }

    /*
    Method for easily adding test data to the table
    Only used while in testing
     */
    public void buildTestData() {
        Destination tempDist1= new Destination("The Ohio State University - Dreese Laboratories", -83.015941, 40.002357);
        tempDist1.address = "test address";
        Destination tempDist2= new Destination("Raising Cane's", -83.007699, 39.999338);
        tempDist2.address = "test address";
        Destination tempDist3= new Destination("Chipotle Mexican Grill",-83.007168, 39.997513);
        tempDist3.address = "test address";
        Destination tempDist4= new Destination("Lazenby Hall",-83.015635, 39.998839);
        tempDist4.address = "test address";
        Destination tempDist5= new Destination("Caffe Apropos",-83.017099, 39.983966);
        tempDist5.address = "test address";


        destinations.add(tempDist1);
        destinations.add(tempDist2);
        destinations.add(tempDist4);
        destinations.add(tempDist3);
        destinations.add(tempDist5);

    }

    /*
    This establishes location and stores the current location if it is found
    GPS must be turned on to find location
     */
    public void establishLocation() {
        //establishes locations
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isWifiEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(locationManager != null && isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(currentLocation != null) {
            }
        } else if(locationManager != null && isWifiEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(currentLocation != null) {
            }
        }
    }

    /*
    This is called when the GPS option is selected in the menu, opens the GPS settings page
     */
    public void clickGPS(){
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /*
    This is called when the Recent Locations is selected in the menu
    It builds the intent for the Recent Activity
     */
    public void clickRecent(){
        Intent intent = new Intent(getApplicationContext(), Recent.class);
        //starts the search activity with an id of 0
        startActivityForResult(intent, 0);
    }

    /*
    This is called when the Favorite Locations is selected in the menu
    It builds the intent for the Favorite Activity
     */
    public void clickFavorites(){
        Intent intent = new Intent(getApplicationContext(), Favorites.class);
        //starts the search activity with an id of 0
        startActivityForResult(intent,0);
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

    /*
    This is called whenever a destination needs to be added to the recent destinations database
    This updates the database to reflect the local ArrayList of recent destinations
     */
    public void addRecentDestination(Destination destination) {
        recentDestinations.add(0, destination);
        if(recentDestinations.size() > 10) {
            recentDestinations.remove(recentDestinations.size()-1);
        }
        dbHelper.rlDeleteAll();
        for(int i = 0; i < recentDestinations.size(); i++) {
            Destination rDest = recentDestinations.get(i);
            dbHelper.rlInsert(rDest.name, rDest.longitude, rDest.latitude, rDest.address);
        }
    }

    /*
    This is called whenever a destination needs to be added to the favorite destinations database
    This updates the database to reflect the local ArrayList of favorite destinations
     */
    public void addFavoriteDestination(Destination destination) {
        favoriteDestinations.add(0, destination);
        dbHelper.favDeleteAll();
        for(int i = 0; i < favoriteDestinations.size(); i++) {
            Destination fDest = favoriteDestinations.get(i);
            dbHelper.favInsert(fDest.name, fDest.longitude, fDest.latitude, fDest.address);
        }
    }

    /*
    This is called whenever a destination needs to be removed from the favorite destinations database
    This updates the database to reflect the local ArrayList of favorite destinations
     */
    public void removeFavoriteDestination(Destination destination, int location) {
        favoriteDestinations.remove(location);
        dbHelper.favDeleteAll();
        for(int i = 0; i < favoriteDestinations.size(); i++) {
            Destination fDest = favoriteDestinations.get(i);
            dbHelper.favInsert(fDest.name, fDest.longitude, fDest.latitude, fDest.address);
        }
    }

    /*
    returns the index of a destination from the favorites arraylist
    or returns -1 if it is not located in the arraylist
     */
    public int locationOfFavorite(Destination favDest) {
        for(int i = 0; i < favoriteDestinations.size(); i++) {
            if(favDest.equals(favoriteDestinations.get(i))) {
                return i;
            }
        }
        return -1;
    }

    //Saves the local recent and favorites destinations to the database
    //also save the state of the current destinations table
    protected	void	onSaveInstanceState	(Bundle	outState){
        Log.e(TAG, "++ SAVING!!! ++");
        dbHelper.rlDeleteAll();
        for(int i = 0; i < recentDestinations.size(); i++) {
            Destination rDest = recentDestinations.get(i);
            dbHelper.rlInsert(rDest.name, rDest.longitude, rDest.latitude, rDest.address);
        }
        dbHelper.favDeleteAll();
        for(int i = 0; i < favoriteDestinations.size(); i++) {
            Destination fDest = favoriteDestinations.get(i);
            dbHelper.favInsert(fDest.name, fDest.longitude, fDest.latitude, fDest.address);
        }

        //grab all the contents of each destination
        double[] listOfDestLong = new double[destinations.size()];
        double[] listOfDestLat = new double[destinations.size()];
        ArrayList<String> listOfDestNames = new ArrayList<String>();
        ArrayList<String> listOfDestAddr = new ArrayList<String>();
        for(int i=0; i<destinations.size(); i++){
            listOfDestNames.add(i,destinations.get(i).name);
            listOfDestLong[i] = destinations.get(i).longitude;
            listOfDestLat[i] = destinations.get(i).latitude;
            listOfDestAddr.add(destinations.get(i).address);
        }

        //Store the contents of each destination in the saved bundled
        outState.putStringArrayList("dName", listOfDestNames);
        outState.putDoubleArray("dLong", listOfDestLong);
        outState.putDoubleArray("dLat", listOfDestLat);
        outState.putStringArrayList("dAddr", listOfDestAddr);
        super.onSaveInstanceState(outState);
    }

    //restore the state of the app by recreating the destinations arraylist
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> tempDestNames = savedInstanceState.getStringArrayList("dName");
        double[] tempDestLongs = savedInstanceState.getDoubleArray("dLong");
        double[] tempDestLats = savedInstanceState.getDoubleArray("dLat");
        ArrayList<String> tempDestAddr = savedInstanceState.getStringArrayList("dAddr");

        destinations.clear();
        for(int i=0; i<tempDestNames.size(); i++){
            Destination tempDest = new Destination(tempDestNames.get(i),tempDestLongs[i],tempDestLats[i]);
            tempDest.address = tempDestAddr.get(i);
            destinations.add(tempDest);
        }
        buildTable();
    }



    /*
    This is called when a start icon is clicked
    updates the start location global variable and recolors all the icons to reflect this
     */
    public void startClicked(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            colorStarts(cellNumber);
            startLocation = destinations.get(cellNumber);
        }
    }

    /*
    This is called when a end icon is clicked
    updates the end location global variable and recolors all the icons to reflect this
     */
    public void endClicked(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            colorEnds(cellNumber);
            endLocation = destinations.get(cellNumber);
        }
    }

    /*
    Colors all the start icons to reflect their selected/unselected state
     */
    public void colorStarts(int i){
        int count = table.getChildCount();
        for (int j = count - 1; j >= 0; j--) {
            View child = table.getChildAt(j);
            if (child instanceof TableRow){
                ImageButton startTemp = (ImageButton)child.findViewById(R.id.start_button);
                if ((Integer)startTemp.getTag() == i){
                    //this is the selected start
                    startTemp.setColorFilter(Color.argb(255, 0,200,0)); //bright green
                }
                else{
                    //this  is an unselected start
                    startTemp.setColorFilter(Color.argb(255, 150,200,150)); //faded green
                }
            };
        }
    }

    /*
    Colors all the end icons to reflect their selected/unselected state
     */
    public void colorEnds(int i){
        int count = table.getChildCount();
        for (int j = count - 1; j >= 0; j--) {
            View child = table.getChildAt(j);
            if (child instanceof TableRow){
                ImageButton endTemp = (ImageButton)child.findViewById(R.id.end_Button);
                if ((Integer)endTemp.getTag() == i){
                    //this is the selected end
                    endTemp.setColorFilter(Color.argb(255, 200,0,0)); //bright red
                }
                else{
                    //this  is an unselected start
                    endTemp.setColorFilter(Color.argb(255, 200,150,150)); //faded red
                }
            };
        }

    }

    //just checks if the destinations arraylist contains the parameter dest
    // the .contains method was not working despite correctly overriding .equals, so this is the ugly solution
    public boolean checkRepeatedDestination(Destination tempDest) {
        for(int i = 0; i < destinations.size(); i++) {
            if (tempDest.equals(destinations.get(i))){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main_screen);
    }

    //******These Methods are required for location manager******
    @Override
    public void onLocationChanged(Location location) {
        //Removes updates once a locaiton is found, beeter on battery life
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
    //*************************************************************
}

