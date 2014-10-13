package errandsapp.errandsapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class Search extends Activity {



    private TextView info;
    private EditText input;
    private Button goSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();

        info = (TextView) findViewById(R.id.searchBarResult);
        input = (EditText)findViewById(R.id.searchBar);
        goSearch = (Button) findViewById(R.id.searchButton);
        goSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String inputText = input.getText().toString();
                info.setText(inputText);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("dName", inputText);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

        });
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
}
