package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.View.OnClickListener;
import android.widget.Button;

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

    private ArrayList<Destination> destinations;
    private TableLayout table;
    LayoutInflater inflater;

    private ArrayList<String> listOfDestNames;
    private double[] listOfDestLong;
    private double[] listOfDestLat;

    private String displayUrl;
    private TextView textView;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    buildTable();
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
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        String originName;
        String deName;

        listOfDestNames = new ArrayList<String>();
        destinations = new ArrayList<Destination>();

        textView = (TextView)findViewById(R.id.Url);

        Intent intent = getIntent();
        listOfDestNames = intent.getStringArrayListExtra("dName");

        int size = listOfDestNames.size();
        int lastIndex = size - 1;


        listOfDestLong = new double[size];
        listOfDestLat = new double[size];

        listOfDestLong = intent.getDoubleArrayExtra("dLong");
        listOfDestLat = intent.getDoubleArrayExtra("dLat");



        for(int i=0; i <size; i++){
            String destName;
            Double destLong;
            Double destLat;
            destName = listOfDestNames.get(i);
            destLong = listOfDestLong[i];
            destLat = listOfDestLat[i];
            Destination des = new Destination(destName, destLong, destLat);

            destinations.add(i, des);

        }

        originName = destinations.get(0).longitude + "," + destinations.get(0).latitude;
        deName = destinations.get(lastIndex).longitude + "," + destinations.get(lastIndex).latitude;


        for(int i=1; i<lastIndex; i++){
            if(i==1){
                urlString = urlString + originName + "&destination=" + deName + "&waypoints=";
            }
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

        displayUrl = urlString;
        textView.setText(displayUrl);

        Log.e(TAG, displayUrl);
        if(displayUrl != null) {
            new Thread(new Runnable(){
                public void run(){
                    String URLString = displayUrl;
                    String searchResultString = getUrlContents(URLString);

                    //This is the parsing code for Search, not Build Route, This will cause it to crash
                    //We need to fix this parser for build route
//                    try {
//                        JSONObject searchResultJSON = new JSONObject(searchResultString);
//                        JSONArray resultsJSONArray = searchResultJSON.getJSONArray("results");
//                        destinations.clear();
//                        for(int i = 0; i < resultsJSONArray.length(); i++) {
//                            JSONObject result = (JSONObject)resultsJSONArray.get(i);
//                            JSONObject geometry = (JSONObject) result.get("geometry");
//                            JSONObject location = (JSONObject) geometry.get("location");
//                            Destination tempDest = new Destination(result.getString("name"),(Double) location.get("lat"),(Double) location.get("lng"));
//                            destinations.add(tempDest);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    Message msg = Message.obtain();
                    msg.what = 0;
                    //handler.sendMessage(msg);

                }

//                protected void onPostExecute() {
//                    buildTable();
//                }
            }).start();
        }

        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();

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

    public boolean buildTable() {
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        //builds a header row, ugly, but proof of concept
        TableRow header = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);
        ((TextView)header.findViewById(R.id.column_1)).setText("Destination Name");
        header.setTag(-1);
        table.addView(header);
        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the table_row_attributes.xml file
            // not sure what inflates does, but I think I am doing this right....
            TableRow row = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.column_1)).setText(destinations.get(i).name);
            row.setTag(i);
            table.addView(row);
        }
        return true;
    }

}
