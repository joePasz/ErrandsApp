package errandsapp.errandsapp;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

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

    public void testSearch() {
        solo.assertCurrentActivity("Wrong Activity", MainScreen.class);
        solo.clickOnImageButton(0);
        solo.assertCurrentActivity("Wrong Activity", Search.class);
    }

    /**
     * Testing to see if the headers are both present on the Main Screen
     */
    public void testTextPresent(){
        solo.searchText(solo.getString(R.string.destinations_header));
        solo.searchText(solo.getString(R.string.start_end_header));
    }


}
