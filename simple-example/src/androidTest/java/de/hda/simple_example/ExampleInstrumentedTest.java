
package de.hda.simple_example;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.mvp.CustomActivityTestRule;
import com.mvp.EventBus;
import com.mvp.uiautomator.UiAutomatorTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;

import de.hda.simple_example.di.TestDaggerComponentActivity;
import de.hda.simple_example.di.TestDaggerComponentApplication;
import de.hda.simple_example.presenter.Settings;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.di.AndroidTestSimpleApplication;
import de.hda.simple_example.service.CustomService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest extends UiAutomatorTestCase<AndroidTestSimpleApplication> {

    @Mock
    Settings settings;
    @Mock
    CustomService customService;

    @Rule
    public CustomActivityTestRule<MainActivity> activityRule = new CustomActivityTestRule<>(MainActivity.class);

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        doReturn("nachbar").when(settings).readLastQuery();
        dependencies().withSettings(sharedPreferences -> settings)
                      .withCustomService(eventBus -> customService)
                      .apply();

    }

    @Test
    public void useAppContext() throws Exception {
        activityRule.launchActivity(null);
        onView(withId(R.id.search_src_text)).check(ViewAssertions.matches(ViewMatchers.withText("nachbar")));
    }

    @Test
    public void customServiceShouldBeInvokedAfterOnCreate() {
        activityRule.launchActivity(null);
        verify(customService, times(1)).register();
    }
}
