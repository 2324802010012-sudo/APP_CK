package com.example.app_ck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.admin.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/** Ảnh chụp thực tế trên emulator; tạo tài khoản fixture riêng rồi dọn sau khi chụp. */
@RunWith(AndroidJUnit4.class)
public class AdminCaptureTest {
    @Test public void captureScreens() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        long oldId = PhienDangNhap.id(context);
        String oldName = PhienDangNhap.ten(context);
        long user = -1;
        UiKiemThu.bat(context);
        try (AppDatabase db = new AppDatabase(context)) {
            String username = "admin_capture_" + Long.toHexString(System.nanoTime());
            assertTrue(db.dangKy(username, "password123"));
            user = db.dangNhap(username, "password123");
            db.getWritableDatabase().execSQL("UPDATE tai_khoan SET vai_tro='ADMIN' WHERE id=?", new Object[]{user});
            PhienDangNhap.luu(context, user, username);
            capture(QuanLyDiemThamQuanActivity.class, "points");
            capture(QuanLyThuyetMinhActivity.class, "narration");
            capture(QuanLyGpsAutoActivity.class, "gps");
            capture(QuanLyTaiKhoanActivity.class, "accounts");
        } finally {
            try (AppDatabase db = new AppDatabase(context)) {
                db.getWritableDatabase().delete("tai_khoan", "id=?", new String[]{"" + user});
            }
            if (oldId > 0) PhienDangNhap.luu(context, oldId, oldName); else PhienDangNhap.dangXuat(context);
            UiKiemThu.tat(context);
        }
    }

    private <T extends Activity> void capture(Class<T> type, String name) throws Exception {
        try (ActivityScenario<T> scenario = ActivityScenario.launch(type)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            // Screenshot only after the Activity is resumed and its main view has focus.
            UiKiemThu.onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(
                            androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            writeScreenshot(name);
            if (type == QuanLyDiemThamQuanActivity.class) {
                scenario.onActivity(a -> a.findViewById(a.getResources().getIdentifier("dtThemMoi", "id", a.getPackageName())).performClick());
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                writeScreenshot("points-form");
            }
            if (type != QuanLyTaiKhoanActivity.class) {
                scenario.onActivity(a -> {
                    android.widget.EditText field = type == QuanLyDiemThamQuanActivity.class
                            ? a.findViewById(a.getResources().getIdentifier("dtTen", "id", a.getPackageName())) : (android.widget.EditText) UiKiemThu.truong(a,
                            type == QuanLyThuyetMinhActivity.class ? "editor" : "edtLat");
                    field.requestFocus();
                    ((android.view.inputmethod.InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(field, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                });
                long deadline = android.os.SystemClock.elapsedRealtime() + 3000;
                boolean[] visible = {false};
                do {
                    scenario.onActivity(a -> {
                        androidx.core.view.WindowInsetsCompat insets = androidx.core.view.ViewCompat.getRootWindowInsets(a.getWindow().getDecorView());
                        visible[0] = insets != null && insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime());
                    });
                    if (!visible[0]) android.os.SystemClock.sleep(100);
                } while (!visible[0] && android.os.SystemClock.elapsedRealtime() < deadline);
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                writeScreenshot(name + "-keyboard");
                androidx.test.espresso.Espresso.closeSoftKeyboard();
            }
        }
    }

    private void writeScreenshot(String name) throws Exception {
        // Window/IME transitions can outlive the main queue's idle notification.
        android.os.SystemClock.sleep(350);
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String dir = InstrumentationRegistry.getArguments().getString("adminCaptureDir", "admin-after");
        File folder = new File(context.getExternalFilesDir(null), dir);
        assertTrue(folder.isDirectory() || folder.mkdirs());
        try (FileOutputStream out = new FileOutputStream(new File(folder, name + ".png"))) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out));
        } finally { bitmap.recycle(); }
    }
}
