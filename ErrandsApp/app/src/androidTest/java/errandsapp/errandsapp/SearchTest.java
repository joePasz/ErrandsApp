package errandsapp.errandsapp;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import junit.framework.Assert;

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

    public void testSearch() throws Exception {
        // open the menu
        solo.clickOnEditText(0);
        solo.clearEditText(0);
        Assert.assertTrue(solo.searchText(""));
        solo.enterText(0, "Wendy's");
        Assert.assertTrue(solo.searchText("Wendy's"));
    }
}
