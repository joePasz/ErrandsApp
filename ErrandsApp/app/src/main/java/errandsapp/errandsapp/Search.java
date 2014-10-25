package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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


public class Search extends Activity {



    private TextView info;
    private EditText input;
    private Button goSearch;
    private ArrayList<Destination> destinations;
    private TableLayout table;
    LayoutInflater inflater;
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
        setContentView(R.layout.activity_search);
        destinations = new ArrayList<Destination>();
        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        Intent intent = getIntent();

        //build main table
        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
        buildTable();

        info = (TextView) findViewById(R.id.searchBarResult);
        input = (EditText)findViewById(R.id.searchBar);
        goSearch = (Button) findViewById(R.id.searchButton);
        goSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                String inputText = input.getText().toString();
//                info.setText(inputText);

                //Intent resultIntent = new Intent();
                //resultIntent.putExtra("dName", inputText);
                //setResult(Activity.RESULT_OK, resultIntent);
                new Thread(new Runnable(){

                    public void run(){
                        String inputString = input.getText().toString();
                        inputString = inputString.replace(' ', '+');
                        String URLString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + inputString + "&key=AIzaSyDgoZ4AG4pxViHeKbAHEChnDrknUNmQIYY";
//                        String testURLString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=resturaunts+in+columbus&key=AIzaSyDgoZ4AG4pxViHeKbAHEChnDrknUNmQIYY";
                        String searchResultString = getUrlContents(URLString);
                        try {
                            JSONObject searchResultJSON = new JSONObject(searchResultString);
                            JSONArray resultsJSONArray = searchResultJSON.getJSONArray("results");
                            destinations.clear();
                            for(int i = 0; i < resultsJSONArray.length(); i++) {
                                JSONObject result = (JSONObject)resultsJSONArray.get(i);
                                JSONObject geometry = (JSONObject) result.get("geometry");
                                JSONObject location = (JSONObject) geometry.get("location");
                                Destination tempDest = new Destination(result.getString("name"),(Double) location.get("lat"),(Double) location.get("lng"));
                                destinations.add(tempDest);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Message msg = Message.obtain();
                        msg.what = 0;
                        handler.sendMessage(msg);

                    }

                    protected void onPostExecute() {
                        buildTable();
                    }
                }).start();



                //finish();
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

    public void clickHandlerCell(View v){
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            Log.e(TAG, "cell: " + v.getTag() + " Clicked!!!!");
            Destination clickedDest = destinations.get(cellNumber);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("dName", clickedDest.name);
            resultIntent.putExtra("dLong", clickedDest.longitude);
            resultIntent.putExtra("dLat", clickedDest.latitude);
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
