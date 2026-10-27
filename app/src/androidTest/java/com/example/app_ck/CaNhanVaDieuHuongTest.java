package com.example.app_ck;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashSet;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CaNhanVaDieuHuongTest {
    private Context context;
    private String ten, tenCu;
    private long id, idCu;
    private static final String MAT_KHAU = "MatKhau123!";
    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        UiKiemThu.bat(context);
        idCu = PhienDangNhap.id(context); tenCu = PhienDangNhap.ten(context);
        ten = "profile_" + Long.toHexString(System.nanoTime());
        try (TaiKhoanDb db = new TaiKhoanDb(context)) {
            assertTrue(db.dangKy(ten, MAT_KHAU, "RECOVERY_ORIGINAL"));
            id = db.dangNhap(ten, MAT_KHAU);
            db.datYeuThich(id, 1, true);
        }
        PhienDangNhap.luu(context, id, ten);
    }
    @After public void cleanup() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            HashSet<Activity> all = new HashSet<>();
            for (Stage stage : Stage.values()) if (stage != Stage.DESTROYED)
                all.addAll(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage));
            for (Activity a : all) a.finish();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        UiKiemThu.tat(context);
        try (TaiKhoanDb db = new TaiKhoanDb(context)) {
            db.getWritableDatabase().delete("yeu_thich", "tai_khoan_id=?", new String[]{String.valueOf(id)});
            db.getWritableDatabase().delete("tai_khoan", "id=?", new String[]{String.valueOf(id)});
        }
        if (idCu > 0) PhienDangNhap.luu(context, idCu, tenCu); else PhienDangNhap.dangXuat(context);
    }
    private void nhap(int view, String text) { onView(withId(view)).perform(scrollTo(), replaceText(text), closeSoftKeyboard());
        UiKiemThu.huyTuDongDien(); }
    private void chup(String name) throws Exception {
        android.graphics.Bitmap anh = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(new java.io.File(context.getExternalFilesDir(null), name))) {
            anh.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
        }
        anh.recycle();
    }

    @Test public void namTabDungChungMenuVaMoManHinhThat() {
        try (ActivityScenario<YeuThichActivity> scenario = ActivityScenario.launch(YeuThichActivity.class)) {
            onView(withId(R.id.navYeuThich)).check(matches(isSelected()));
            onView(withId(R.id.navCaNhan)).perform(click());
            onView(withId(R.id.cnTen)).check(matches(withText(ten)));
            onView(withId(R.id.navCaNhan)).check(matches(isSelected()));
            onView(withId(R.id.navKhamPha)).perform(click());
            onView(withId(R.id.oTimKiem)).check(matches(isDisplayed()));
            nhap(R.id.oTimKiem, "chung tich");
            onView(withId(R.id.nutTimKiem)).perform(click());
            onView(withText("Bảo tàng Chứng tích Chiến tranh")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            onView(withId(R.id.nutTimYeuThich)).check(matches(withText("♥")));
            androidx.test.espresso.Espresso.pressBack();
            onView(withId(R.id.navGoiY)).perform(click());
            onView(withId(R.id.navGoiY)).check(matches(isSelected()));
            onView(withId(R.id.kpSoLuong)).check(matches(withText("4 địa điểm gợi ý")));
            onView(withId(R.id.navTrangChu)).perform(click());
            onView(withId(R.id.navTrangChu)).inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(
                    hasDescendant(withId(R.id.vungCuonTrangChu)))).check(matches(isSelected()));
            onView(withId(R.id.oTimKiem)).perform(scrollTo(), replaceText("Hue"), closeSoftKeyboard());
            onView(withId(R.id.vungCuonTrangChu)).perform(swipeUp(), swipeUp());
            onView(withId(R.id.navKhamPha)).perform(click());
            onView(withId(R.id.oTimKiem)).check(matches(isDisplayed()));
            onView(withId(R.id.oTimKiem)).check(matches(withText("Hue")));
        }
    }

    @Test public void suaHoSoKiemTraEmailVaLuuQuaLanMoLai() throws Exception {
        try (ActivityScenario<CaNhanActivity> scenario = ActivityScenario.launch(CaNhanActivity.class)) {
            onView(withId(R.id.cnSoYeuThich)).check(matches(withText("1\nĐịa điểm đã lưu")));
            onView(withId(R.id.cnSuaHoSo)).perform(scrollTo(), click());
            nhap(R.id.hsHoTen, "Nguyễn Minh Anh"); nhap(R.id.hsEmail, "email-sai");
            onView(withId(R.id.hoSoLuu)).perform(scrollTo(), click());
            onView(withId(R.id.hsEmail)).check(matches(hasErrorText("Email không hợp lệ")));
            nhap(R.id.hsEmail, "minhanh@example.com");
            onView(withId(R.id.hoSoLuu)).perform(scrollTo(), click());
            onView(withId(R.id.cnTen)).check(matches(withText("Nguyễn Minh Anh")));
            scenario.recreate();
            onView(withId(R.id.cnTen)).check(matches(withText("Nguyễn Minh Anh")));
            chup("profile-preview.png");
            try (TaiKhoanDb db = new TaiKhoanDb(context)) {
                assertEquals("minhanh@example.com", db.layHoSo(id).email);
                assertEquals(ten, db.layHoSo(id).tenDangNhap);
                assertTrue(db.daYeuThich(id, 1));
            }
        }
    }

    @Test public void doiMatKhauCanMatKhauCuVaCapMaKhoiPhucMoi() {
        try (ActivityScenario<CaNhanActivity> scenario = ActivityScenario.launch(CaNhanActivity.class)) {
            onView(withId(R.id.cnDoiMatKhau)).perform(scrollTo(), click());
            nhap(R.id.hsMatKhauCu, "SaiMatKhau");
            nhap(R.id.hsMatKhauMoi, "MatKhauMoi456!"); nhap(R.id.hsXacNhan, "MatKhauMoi456!");
            onView(withId(R.id.hoSoLuu)).perform(scrollTo(), click());
            onView(withId(R.id.hsMatKhauCu)).check(matches(hasErrorText("Mật khẩu hiện tại không đúng hoặc không thể cập nhật")));
            nhap(R.id.hsMatKhauCu, MAT_KHAU);
            onView(withId(R.id.hoSoLuu)).perform(scrollTo(), click());
            onView(withText("Đổi mật khẩu thành công")).check(matches(isDisplayed()));
            final String[] ma = new String[1];
            onView(withId(android.R.id.message)).check((view, error) -> {
                if (error != null) throw error;
                ma[0] = ((TextView) view).getText().toString().split("\n\n")[1];
            });
            onView(withText("Tôi đã lưu mã")).perform(click());
            onView(withId(R.id.cnTen)).check(matches(withText(ten)));
            assertEquals(id, PhienDangNhap.id(context));
            try (TaiKhoanDb db = new TaiKhoanDb(context)) {
                assertEquals(-1, db.dangNhap(ten, MAT_KHAU));
                assertEquals(id, db.dangNhap(ten, "MatKhauMoi456!"));
                assertNull(db.doiMatKhauBangMa(ten, "RECOVERY_ORIGINAL", MAT_KHAU));
                assertNotNull(db.doiMatKhauBangMa(ten, ma[0], MAT_KHAU));
            }
        }
    }

    @Test public void khachDangNhapTuCaNhanVaDangXuatGiuYeuThich() {
        PhienDangNhap.dangXuat(context);
        try (ActivityScenario<CaNhanActivity> scenario = ActivityScenario.launch(CaNhanActivity.class)) {
            onView(withId(R.id.cnKhach)).check(matches(isDisplayed()));
            onView(withId(R.id.cnDangNhap)).perform(scrollTo(), click());
            nhap(R.id.edtTenDangNhap, ten); nhap(R.id.edtMatKhau, MAT_KHAU);
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            onView(withId(R.id.cnTen)).check(matches(withText(ten)));
            scenario.onActivity(a -> a.findViewById(R.id.cnDangXuat).performClick());
            onView(withId(android.R.id.button2)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertTrue(PhienDangNhap.daDangNhap(context));
            scenario.onActivity(a -> a.findViewById(R.id.cnDangXuat).performClick());
            onView(withId(android.R.id.button1)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            onView(withId(R.id.cnKhach)).check(matches(isDisplayed()));
            assertFalse(PhienDangNhap.daDangNhap(context));
            try (TaiKhoanDb db = new TaiKhoanDb(context)) { assertTrue(db.daYeuThich(id, 1)); }
        }
    }
}
