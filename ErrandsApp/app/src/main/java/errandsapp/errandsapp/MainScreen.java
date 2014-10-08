package errandsapp.errandsapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class MainScreen extends Activity {

    private Destination[] destinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        //Just an ugly, quick, and dirty way to build an array of these destination object
        Destination dest = new Destination("Starbucks",32.04947,-40.2381);
        destinations = new Destination[5];
        for(int i = 0; i < 5; i++){
            destinations[i] = dest;
        }

        //build main table
        TableLayout table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
        //builds a header row, ugly, but proof of concept
        TableRow header = (TableRow) inflater.inflate(R.layout.table_row_attributes, null);
        ((TextView)header.findViewById(R.id.column_1)).setText("Destination Name");
        ((TextView)header.findViewById(R.id.column_2)).setText("Long:Lat");
        table.addView(header);
        //Dynamically adds rows based on the size of the destinations array
        for(int i = 0; i < destinations.length; i++){
            // Inflates the table_row_attributes.xml file
            // not sure what inflates does, but I think I am doing this right....
            TableRow row = (TableRow) inflater.inflate(R.layout.table_row_attributes, null);
            //adds contents of the destination to the row
            ((TextView)row.findViewById(R.id.column_1)).setText(destinations[i].name);
            ((TextView)row.findViewById(R.id.column_2)).setText(destinations[i].longitude + ":" + destinations[i].latitude);
            table.addView(row);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
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
}
