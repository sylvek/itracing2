package tools.fastlane.localetester;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import android.support.test.rule.ActivityTestRule;
import net.sylvek.itracing2.devices.DevicesActivity;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

/**
 * Created by sylvek on 13/03/2017.
 */
public class JUnit4StyleTests {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<DevicesActivity> activityRule = new ActivityTestRule<>(DevicesActivity.class);

    @BeforeClass
    public static void beforeAll()
    {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Test
    public void testTakeScreenShot()
    {
        Screengrab.screenshot("hello");
    }
}
