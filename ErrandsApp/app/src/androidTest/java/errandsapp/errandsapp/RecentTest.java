package errandsapp.errandsapp;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by schuster110 on 12/1/14.
 */
public class RecentTest extends ActivityInstrumentationTestCase2<Recent> {

    private Solo solo;

    public RecentTest(){
        super(Recent.class);
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

    public void testActivityActive(){
        solo.assertCurrentActivity("Wrong Activity" , Recent.class);
    }
}
