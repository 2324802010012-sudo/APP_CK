package com.example.app_ck;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.test.espresso.Root;
import androidx.test.espresso.ViewInteraction;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** Chờ đúng cửa sổ có focus và không mời lưu thông tin tài khoản thử vào dịch vụ autofill. */
public final class UiKiemThu {
    private static final Matcher<Root> CO_FOCUS = new TypeSafeMatcher<Root>() {
        @Override public void describeTo(Description d) { d.appendText("cửa sổ hiển thị và đang nhận focus"); }
        @Override protected boolean matchesSafely(Root r) { return r.getDecorView().isShown() && r.getDecorView().hasWindowFocus(); }
    };
    public static ViewInteraction onView(Matcher<View> matcher) {
        return androidx.test.espresso.Espresso.onView(matcher).inRoot(CO_FOCUS);
    }
    public static Matcher<Root> hoiThoai() {
        return org.hamcrest.Matchers.allOf(CO_FOCUS, androidx.test.espresso.matcher.RootMatchers.isDialog());
    }
    private static final Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {
        @Override public void onActivityCreated(Activity a, Bundle b) {
            if (android.os.Build.VERSION.SDK_INT >= 26)
                a.getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        @Override public void onActivityStarted(Activity a) {}
        @Override public void onActivityResumed(Activity a) {}
        @Override public void onActivityPaused(Activity a) { huy(a); }
        @Override public void onActivityStopped(Activity a) {}
        @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
        @Override public void onActivityDestroyed(Activity a) {}
    };
    public static void bat(Context context) { ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(callbacks); }
    public static void tat(Context context) { ((Application) context.getApplicationContext()).unregisterActivityLifecycleCallbacks(callbacks); }
    private static void huy(Activity a) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            android.view.autofill.AutofillManager manager = a.getSystemService(android.view.autofill.AutofillManager.class);
            if (manager != null) manager.cancel();
        }
    }
    public static void huyTuDongDien() {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            for (Activity a : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)) huy(a);
        });
    }
}
