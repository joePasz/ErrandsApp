package errandsapp.errandsapp;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by schuster110 on 12/1/14.
 */
public class SearchTest extends ActivityInstrumentationTestCase2<Search> {

    private Solo solo;

    public SearchTest(){
        super(Search.class);
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

    public void testSearchActive() {
        solo.assertCurrentActivity("Wrong Activity", Search.class);
    }

    public void testButtonPresent(){
        solo.searchButton(solo.getString(R.id.searchButton), true);
    }
}
