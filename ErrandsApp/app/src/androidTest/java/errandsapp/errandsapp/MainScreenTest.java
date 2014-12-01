package errandsapp.errandsapp;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.ImageButton;

import junit.framework.Assert;
import com.robotium.solo.Solo;

import errandsapp.errandsapp.BuildRoute;
import errandsapp.errandsapp.MainScreen;

/**
 * Created by schuster110 on 11/30/14.
 */
public class MainScreenTest extends ActivityInstrumentationTestCase2<MainScreen> {

    private Solo solo;

    public MainScreenTest(){

        super(MainScreen.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

   public void testActivityActive() {
       solo.assertCurrentActivity("Wrong Activity", MainScreen.class);
   }

    /** Testing to see if each button the screen is present
     *
     */
    public void testButtonsPresent() {
        solo.searchButton(solo.getString(R.string.build_route_button), true);
        solo.searchButton(solo.getString(R.string.search_button),true);
        solo.searchButton(solo.getString(R.string.GPSLoc),true);
    }

}
