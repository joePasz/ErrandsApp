package errandsapp.errandsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class Search extends Activity implements LocationListener{
    //global variables
    private EditText input;
    private ArrayList<Destination> destinations;
    private TableLayout table;
    private LocationManager locationManager;
    private Location currentLocation;
    LayoutInflater inflater;
    ProgressDialog indicator;

    //This handler is passed messages at the end of the html request thread
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                //This is called when the request is successful
                case 0:
                    indicator.dismiss();
                    buildTable();
                    break;
                //this case is called during a failed request
                case 1:
                    if(indicator.isShowing()) {
                        indicator.dismiss();
                        //Builds alert dialog to inform the user it failed
                        AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
                        builder.setMessage("Search Failed! (Possible Network Issues)")
                                .setCancelable(false)
                                .setNegativeButton("Cancel", null);
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    break;
            }
        }
    };
    private final String TAG = ((Object) this).getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ In onCreate() ++");
        setContentView(R.layout.activity_search);

        //initilize global variables
        destinations = new ArrayList<Destination>();
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        establishLocation();

        //build table and screen
        input = (EditText)findViewById(R.id.searchBar);
        table = (TableLayout)findViewById(R.id.table);
        initializeSearchButton();
        table.bringToFront();
        buildTable();

    }

    /*
    This method sets up the onclicklistener for the search button
    This onClick takes the text in the input box and starts and google places request
    Also uses the users current location if available
     */
    public void initializeSearchButton(){
        ((ImageButton)findViewById(R.id.searchButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLocation == null) {
                    //builds an alert dialog to inform the user there is no location found
                    AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
                    builder.setMessage("Location not found. Go to GPS Settings?")
                            .setCancelable(false)
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                //cancel runs the search anyway
                                public void onClick(DialogInterface dialog, int id) {
                                    runGoogleSearchQuery();
                                }
                            })
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                //OK opens the GPS settings
                                public void onClick(DialogInterface dialog, int id) {
                                    clickGPS();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    //location found, run the query
                    runGoogleSearchQuery();
                }
            }

        });
    }

    /*
    This method handles the google Place request
     */
    public void runGoogleSearchQuery() {
        //First, clears the table if needed
        destinations.clear();
        buildTable();

        //Then builds indicator to inform the user it is searching
        indicator = new ProgressDialog(this);
        indicator.setMessage("Searching \"" + input.getText().toString() + "\"...");
        indicator.setCancelable(false);
        indicator.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        indicator.show();

        //Finally builds thread that runs the request
        Thread googleSearch = new Thread(new Runnable(){

            public void run(){
                //takes the user input text
                String inputString = input.getText().toString();
                //formats the string into an http request
                inputString = inputString.replace(' ', '+');
                String URLString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + inputString;
                if(currentLocation != null) {
                    //adds location if available
                    URLString = URLString + "&location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&radius=5000";
                }
                URLString = URLString + "&key=AIzaSyDgoZ4AG4pxViHeKbAHEChnDrknUNmQIYY";

                //Runs the request and takes the response as a string
                String searchResultString = getUrlContents(URLString);

                try {
                    //turns the response into a JSON object
                    JSONObject searchResultJSON = new JSONObject(searchResultString);
                    //parses the object to find each returned location and adds them to the destinations arraylist
                    JSONArray resultsJSONArray = searchResultJSON.getJSONArray("results");
                    for(int i = 0; i < resultsJSONArray.length(); i++) {
                        JSONObject result = (JSONObject)resultsJSONArray.get(i);
                        JSONObject geometry = (JSONObject) result.get("geometry");
                        JSONObject location = (JSONObject) geometry.get("location");
                        Destination tempDest = new Destination(result.getString("name"),(Double) location.get("lng"),(Double) location.get("lat"));
                        tempDest.address = result.getString("formatted_address");
                        destinations.add(tempDest);
                    }
                    //informs the message handler that is has been sucessful
                    Message msg = Message.obtain();
                    msg.what = 0;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    //error found, usually network related problem
                    e.printStackTrace();
                    //informs the message handler that is failed
                    Message msg = Message.obtain();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }

            }

//                    protected void onPostExecute() {
//                        buildTable();
//                    }
        });
        googleSearch.start();
    }


    /*
    Method that is called whenever the table needs to be refreshed
     */
    public boolean buildTable() {
        //first clears the table of previous entries
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
            if (child instanceof TableRow) table.removeView(child);
        }
        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the search_results_table_row_attributes.xml file
            TableRow row = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.desti)).setText(destinations.get(i).name);
            ((TextView)row.findViewById(R.id.address)).setText(destinations.get(i).address);
            row.setTag(i);
            table.addView(row);
        }
        return true;
    }

    /*
    Method that is called when a cell is clicked
    It finds which cell was selected by using the tag established in buildtable
    this tag links the row with the destinations index
    It then builds an intent to return to the mainscreen with the contents of the selected destination
     */
    public void clickHandlerCell(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            Destination clickedDest = destinations.get(cellNumber);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("dName", clickedDest.name);
            resultIntent.putExtra("dLong", clickedDest.longitude);
            resultIntent.putExtra("dLat", clickedDest.latitude);
            resultIntent.putExtra("dAddress", clickedDest.address);
            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Fixes the Up Button
        if(id == android.R.id.home) {
            Search.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
    Wonderful method borrowed and modified from StackOverflow which takes a url, establish a connection
    then reads the response and returns said response as a string
     */
    private String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            //reads the response
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()), 8);
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line + "\n");
            }
            br.close();
        }catch (Exception e) {
            //error occured, usually network related
            e.printStackTrace();
        }
        return content.toString();
    }

    /*
    method that builds the intent to open the GPS settings
     */
    public void clickGPS(){
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /*
    Establishes the current location if GPS is turned on
     */
    public void establishLocation(){
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

    //Save the state of the current destinations table
    protected	void	onSaveInstanceState	(Bundle	outState){
        Log.e(TAG, "++ SAVING!!! ++");

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

    //Following are location methods!!
    @Override
    public void onLocationChanged(Location location) {
        //Should stop updating location once it has been found once. Saves battery
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
