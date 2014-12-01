package errandsapp.errandsapp;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by schuster110 on 12/1/14.
 */
public class FavoritesTest extends ActivityInstrumentationTestCase2<Favorites> {

    private Solo solo;
    public FavoritesTest(){
        super(Favorites.class);
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
        solo.assertCurrentActivity("Wrong Activity", Favorites.class);
    }
}
