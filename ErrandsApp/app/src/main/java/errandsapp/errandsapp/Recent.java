package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


public class Recent extends Activity {
    private TableLayout table;
    LayoutInflater inflater;
    private ArrayList<Destination> destinations;
    private final String TAG = ((Object) this).getClass().getSimpleName();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent);

        //intialize global variables and select all recent destinations from the db and store them in destinations
        dbHelper = new DatabaseHelper(getApplicationContext());
        destinations = dbHelper.rlSelectAll();
        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();

        //build table to reflect the recent locations
        buildTable();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Fix the up button
        if(id == android.R.id.home) {
            Recent.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Method in charge of building the table based on the contents of the destinations arraylist
     */
    public boolean buildTable() {
        //first clears the table of previous entries
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.size(); i++){
            // Inflates the search_results_table_row_attributes.xml file
            TableRow row = (TableRow) inflater.inflate(R.layout.search_results_table_row_attributes, null);

            //Finds oritentation and alters the row width if in landscape
            if(getResources().getConfiguration().orientation == 2) {
                LinearLayout ll = ((LinearLayout)row.findViewById(R.id.layout_contents));
                ll.getLayoutParams().width = 1700;
                ll.requestLayout();
            }

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
}
