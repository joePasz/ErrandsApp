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
import android.view.View.OnClickListener;
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



    private TextView info;
    private EditText input;
    private ImageButton goSearch;
    private ArrayList<Destination> destinations;
    private TableLayout table;
    private LocationManager locationManager;
    private Location currentLocation;
    LayoutInflater inflater;
    ProgressDialog indicator;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    indicator.dismiss();
                    buildTable();
                    break;
                case 1:
                    if(indicator.isShowing()) {
                        indicator.dismiss();
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
        destinations = new ArrayList<Destination>();
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Intent intent = getIntent();

        //build main table
        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
        buildTable();

        info = (TextView) findViewById(R.id.searchBarResult);
        input = (EditText)findViewById(R.id.searchBar);
        goSearch = (ImageButton) findViewById(R.id.searchButton);
        goSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(currentLocation == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
                    builder.setMessage("Location not found. Go to GPS Settings?")
                            .setCancelable(false)
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    runGoogleSearchQuery();
                                }
                            })
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    clickGPS();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    runGoogleSearchQuery();
                }



                //finish();
            }

        });


    }

    public void runGoogleSearchQuery() {
        destinations.clear();
        buildTable();
        indicator = new ProgressDialog(this);
        indicator.setMessage("Searching...");
        indicator.setCancelable(false);

        indicator.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        indicator.show();

        Thread googleSearch = new Thread(new Runnable(){

            public void run(){
                String inputString = input.getText().toString();
                inputString = inputString.replace(' ', '+');
                String URLString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + inputString;
                if(currentLocation != null) {
                    URLString = URLString + "&location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&radius=5000";
                }
                URLString = URLString + "&key=AIzaSyDgoZ4AG4pxViHeKbAHEChnDrknUNmQIYY";
                String searchResultString = getUrlContents(URLString);
                try {
                    JSONObject searchResultJSON = new JSONObject(searchResultString);
                    JSONArray resultsJSONArray = searchResultJSON.getJSONArray("results");
                    destinations.clear();
                    for(int i = 0; i < resultsJSONArray.length(); i++) {
                        JSONObject result = (JSONObject)resultsJSONArray.get(i);
                        JSONObject geometry = (JSONObject) result.get("geometry");
                        JSONObject location = (JSONObject) geometry.get("location");
                        Destination tempDest = new Destination(result.getString("name"),(Double) location.get("lng"),(Double) location.get("lat"));
                        tempDest.address = result.getString("formatted_address");
                        destinations.add(tempDest);
                    }
                    Message msg = Message.obtain();
                    msg.what = 0;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
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

    //This first removes all views within the table if there are any, then builds
    //it from scratch based on the contents of the Array List Destinations
    public boolean buildTable() {
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        //builds a header row, ugly, but proof of concept
//        TableRow header = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);
//        ((TextView)header.findViewById(R.id.column_1)).setText("Destination Name");
//        header.setTag(-1);
//        table.addView(header);
        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the table_row_attributes.xml file
            // not sure what inflates does, but I think I am doing this right....
            TableRow row = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.desti)).setText(destinations.get(i).name);
            ((TextView)row.findViewById(R.id.address)).setText(destinations.get(i).address);
            row.setTag(i);
            table.addView(row);
        }
        return true;
    }

    public void clickHandlerCell(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            Log.d(TAG, "cell: " + v.getTag() + " Clicked!!!!");
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public void clickGPS(){
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }



    //Following are location methods!!
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

//class DownloadImageTask extends AsyncTask {
//    public int doInBackground(String... urls) {
//        int i = 10;
//        return 1;
//    }
//
//    protected void onPostExecute() {
//            buildTable();
//    }
//
//    @Override
//    protected Object doInBackground(Object[] objects) {
//        return null;
//    }
//}
