package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


public class Favorites extends Activity {

    private TableLayout table;
    LayoutInflater inflater;
    private ArrayList<Destination> destinations;
    private final String TAG = ((Object) this).getClass().getSimpleName();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //intialize global variables and select all favorite destinations from the db and store them in destinations
        dbHelper = new DatabaseHelper(getApplicationContext());
        destinations = dbHelper.favSelectAll();
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
        getMenuInflater().inflate(R.menu.favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Fix up button
        if(id == android.R.id.home) {
            Favorites.this.finish();
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
            // Inflates the favorite_table_row_attributes.xml file
            TableRow row = (TableRow) inflater.inflate(R.layout.favorite_table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.desti)).setText(destinations.get(i).name);
            ((TextView)row.findViewById(R.id.address)).setText(destinations.get(i).address);
            //color and tag the delete button with its corresponding destinations index
            ImageButton deleteButton = (ImageButton)row.findViewById(R.id.delete_Button);
            deleteButton.setTag(i);
            deleteButton.setColorFilter(Color.argb(255, 255, 0, 0)); // White Tint
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

    /*
    This method is called whenever the delete icon is clicked
    It finds which cell was clicked, then deletes it from the table as well as the arraylist
     */
    public void deleteClicked(View v) {
        int cellNumber = (Integer)v.getTag();
        if (cellNumber != -1) {
            ViewGroup tempRow = (ViewGroup)v.getParent();
            ViewGroup tempTable = (ViewGroup)tempRow.getParent();
            tempTable.removeView(tempRow);

            destinations.remove(cellNumber);

        }
        buildTable();
    }

    /*
    On pause was overridden to take the current favorite destinations arraylist and override the
    database with it's contents

    On pause was chosen because saveInstance was giving me consistency issues
     */
    protected void onPause() {
        super.onPause();
        dbHelper.favDeleteAll();
        for(int i = 0; i < destinations.size(); i++) {
            Destination fDest = destinations.get(i);
            dbHelper.favInsert(fDest.name, fDest.longitude, fDest.latitude, fDest.address);
        }
        Log.e(TAG, "++ In onPause() ++");
    }


}
