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

        dbHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<Destination> tempDests = dbHelper.rlSelectAll();
        destinations = tempDests;

        inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        table = (TableLayout)findViewById(R.id.table);
        table.bringToFront();
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
        if(id == android.R.id.home) {
            Recent.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    protected	void	onSaveInstanceState	(Bundle	outState){
        super.onSaveInstanceState(outState);

    }
    protected	void	onRestoreInstanceState	(Bundle	savedInstanceState)	{
        super.onRestoreInstanceState(savedInstanceState);

    }



}
