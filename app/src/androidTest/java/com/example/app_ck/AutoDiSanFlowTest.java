package com.example.app_ck;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.tour.DaiNoiHueThamQuanActivity;
import com.example.app_ck.ui.tour.VanMieuThamQuanActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/** Regression test: a heritage tour exposes a direct real-GPS AUTO entry point. */
@RunWith(AndroidJUnit4.class)
public class AutoDiSanFlowTest {
    private <T extends BaseActivity> void chiCoGpsThat(Class<T> activityClass) {
        try (ActivityScenario<T> scenario = ActivityScenario.launch(activityClass)) {
            onView(withId(R.id.tourBatAuto)).check(matches(isDisplayed()));
        }
    }

    @Test public void daiNoiHueChiMoHuongDanGpsThat() {
        chiCoGpsThat(DaiNoiHueThamQuanActivity.class);
    }

    @Test public void vanMieuChiMoHuongDanGpsThat() {
        chiCoGpsThat(VanMieuThamQuanActivity.class);
    }
}
