package errandsapp.errandsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


public class BuildRoute extends Activity {

    private ArrayList<Destination> destinations; //represents the destinations received
    private ArrayList<Destination> orderedDestinations; //represents the optimized order, respecting the beginning and ending
    private TableLayout table;
    LayoutInflater inflater;
    //polylineEncodedString is an encoded string the coorelates to how to plot the route on a google map
    private String polylineEncodedString;
    ProgressDialog indicator;
    private String directionsRequestUrl;

    //This handler is passed messages at the end of the html request thread
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    //This is called when the request is successful
                    indicator.dismiss();
                    buildTable();
                    break;
                case 1:
                    //This is called when the request failed
                    indicator.dismiss();
                    //builds alert to warn user
                    AlertDialog.Builder builder = new AlertDialog.Builder(BuildRoute.this);
                    builder.setMessage("Building Route Failed! (Possible Network Issues)")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //kills the buildRoute once accepted
                                    BuildRoute.this.finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    break;
            }
        }
    };
    private final String TAG = ((Object) this).getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "++ In onCreate() ++");
        setContentView(R.layout.activity_build_route);

        //initialize global variables
        destinations = new ArrayList<Destination>();
        orderedDestinations = new ArrayList<Destination>();
        polylineEncodedString = "";
        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
        //creates and starts the ProgressDialog to inform the user it is building the optimized route
        indicator = new ProgressDialog(this);
        indicator.setMessage("Building Optimized Route");
        indicator.setCancelable(false);
        indicator.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                BuildRoute.this.finish();
            }
        });
        indicator.show();

        //Rebuilds the destinations array from the intent, and then uses this to build the requestURL
        buildDestinationsFromIntent();
        directionsRequestUrl = buildGoogleDirectionsRequest();

        //initilize the build map button
        initializeBuildMapButton();

        //Finally runs the directions request which uses the url build earlier
        runGoogleDirectionsQuery();
    }

    /*
    This method takes the intent and rebuilds the destinations array from the contents of the intent
     */
    public void buildDestinationsFromIntent() {
        Intent intent = getIntent();
        ArrayList<String> listOfDestNames = intent.getStringArrayListExtra("dName");
        double[] listOfDestLong = intent.getDoubleArrayExtra("dLong");
        double[] listOfDestLat = intent.getDoubleArrayExtra("dLat");

        for(int i=0; i < listOfDestNames.size(); i++){
            Destination des = new Destination(listOfDestNames.get(i), listOfDestLong[i], listOfDestLat[i]);
            destinations.add(i, des);
        }
    }

    /*
    This method takes the contents of the destinations arraylist and returns the google directions http
    request.
     */
    public String buildGoogleDirectionsRequest(){
        String originName = destinations.get(0).latitude + "," + destinations.get(0).longitude;
        String deName = destinations.get(destinations.size()-1).latitude + "," + destinations.get(destinations.size()-1).longitude;
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=";

        //Adds each destination as a waypoint in the request
        for(int i=1; i<destinations.size()-1; i++){
            if(i==1){
                urlString = urlString + originName + "&destination=" + deName + "&waypoints=optimize:true|";
            }
            String locString;
            locString = destinations.get(i).latitude + "," + destinations.get(i).longitude;

            if(i == (destinations.size()-2)){
                urlString = urlString + locString;
            }
            else{
                urlString = urlString + locString + "|";
            }
        }
        urlString = urlString + "&key=AIzaSyDgoZ4AG4pxViHeKbAHEChnDrknUNmQIYY";
        return urlString;
    }

    /*
    This takes the BuildMapButton and adds the onclick event
    this even takes the orderedDestinations and the encondedPolySting and sends them
    in the Map Intent
     */
    public void initializeBuildMapButton(){
        Button buildMapButton = (Button) findViewById(R.id.buildMapButton);
        buildMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getApplicationContext(), Map.class);

                ArrayList<String> destNames = new ArrayList<String>();
                double[] destLongs = new double[orderedDestinations.size()];
                double[] destLats = new double[orderedDestinations.size()];

                for(int i=0; i< orderedDestinations.size(); i++){
                    Destination tempDest = orderedDestinations.get(i);
                    destNames.add(tempDest.name);
                    destLongs[i] = tempDest.longitude;
                    destLats[i] = tempDest.latitude;
                }
                mapIntent.putExtra("dLong", destLongs);
                mapIntent.putExtra("dLat", destLats);
                mapIntent.putExtra("dName", destNames);
                mapIntent.putExtra("polyString", polylineEncodedString);
                startActivity(mapIntent);
            }

        });
    }

    /*
    This takes the google directions request string and builds a thread which sends this request
    and parses the response to build the new orderedDestinations arrayList
     */
    public void runGoogleDirectionsQuery(){
        if(directionsRequestUrl != null) {
            new Thread(new Runnable(){
                public void run(){
                    //Establishes URLConnections, sends the request, and receives the response as a string
                    String searchResultString = getUrlContents(directionsRequestUrl);
                    try {
                        //Turns the response to a JSON object
                        JSONObject directionsResultJSON = new JSONObject(searchResultString);

                        //Parses the JSON object
                        JSONArray routesArray = directionsResultJSON.getJSONArray("routes");
                        JSONObject route = routesArray.getJSONObject(0);

                        //Extract the encodedPolylineString
                        JSONObject overviewPolylines = route.getJSONObject("overview_polyline");
                        polylineEncodedString = overviewPolylines.getString("points");

                        //Extract the optimized order of the waypoints
                        if(route.has("waypoint_order")) {
                            //uses this new order to build the orderedDestinations ArrayList
                            JSONArray waypoint_order = route.getJSONArray("waypoint_order");
                            int[] order = new int[waypoint_order.length()];
                            for(int i = 0; i < waypoint_order.length(); i++){
                                order[i] = (Integer)waypoint_order.get(i);
                            }
                            orderedDestinations = calculateOrderedDestinations(order);
                        } else {
                            orderedDestinations = destinations;
                        }

                        //Inform the message handler that the request was a success
                        Message msg = Message.obtain();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        //error, probably network related
                        e.printStackTrace();
                        //inform the message handler that the request was a failure
                        Message msg = Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                }
            }).start();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.build_route, menu);
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
        //Fixes the Up Button
        if(id == android.R.id.home) {
            BuildRoute.this.finish();
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
    Method that is called whenever the table needs to be refreshed
    This table represents the results of the sorted directions query
     */
    public boolean buildTable() {
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the search_results_table_row_attributes.xml file
            TableRow row = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.desti)).setText(orderedDestinations.get(i).name);
            ((TextView)row.findViewById(R.id.address)).setText(orderedDestinations.get(i).address);
            table.addView(row);
        }
        return true;
    }


    /*
    This takes an int[] and returns a sorted version of the destinations array using this information
    the waypoint order will be in the form of [2,0,1]
      This means the first waypoint is third in the new order, the second is first, and the final is second
     */
    public ArrayList<Destination> calculateOrderedDestinations(int[] waypointOrder) {
        ArrayList<Destination> orderedDestinations = new ArrayList<Destination>();
        orderedDestinations.add(destinations.get(0));
        for(int i = 0; i < waypointOrder.length; i++){
            orderedDestinations.add(destinations.get(waypointOrder[i]+1));
        }
        orderedDestinations.add(destinations.get(destinations.size()-1));
        return orderedDestinations;
    }

}
