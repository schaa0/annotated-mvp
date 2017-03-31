
package de.hda.simple_example;

import android.content.SharedPreferences;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.mvp.CustomActivityTestRule;
import com.mvp.uiautomator.UiAutomatorTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import de.hda.simple_example.presenter.Settings;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.di.AndroidTestSimpleApplication;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest extends UiAutomatorTestCase<AndroidTestSimpleApplication> {

    @Mock
    SharedPreferences sharedPreferences;

    @Rule
    public CustomActivityTestRule<MainActivity> activityRule = new CustomActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {

        doReturn("nachbar").when(sharedPreferences).getString(Settings.LAST_QUERY_KEY, "");
        dependencies().with(sharedPreferences).apply();

        activityRule.launchActivity(null);

        onView(withId(R.id.search_src_text)).check(ViewAssertions.matches(ViewMatchers.withText("nachbar")));

    }
}
