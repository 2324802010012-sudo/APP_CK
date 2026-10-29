package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.home.WelcomeActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.profile.YeuThichActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/** Dùng tài khoản thử riêng; giữ lại phiên và dữ liệu tài khoản có sẵn trên thiết bị. */
@RunWith(AndroidJUnit4.class)
public class TaiKhoanVaYeuThichFlowTest {
    private Context context;
    private String ten, tenKhac, tenCu;
    private long idCu;
    private static final String MAT_KHAU = "MatKhau123!";
    private static final String MA = "MA_KHOI_PHUC_TEST";

    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        UiKiemThu.bat(context);
        idCu = PhienDangNhap.id(context);
        tenCu = PhienDangNhap.ten(context);
        PhienDangNhap.dangXuat(context);
        ten = "flow_" + Long.toHexString(System.nanoTime());
        tenKhac = ten + "_b";
    }

    @After public void cleanUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            HashSet<Activity> activities = new HashSet<>();
            for (Stage stage : Stage.values()) {
                if (stage != Stage.DESTROYED) activities.addAll(
                        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage));
            }
            for (Activity a : activities) a.finish();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        UiKiemThu.tat(context);
        try (AppDatabase db = new AppDatabase(context)) {
            db.getWritableDatabase().execSQL("DELETE FROM yeu_thich WHERE tai_khoan_id IN "
                    + "(SELECT id FROM tai_khoan WHERE ten_dang_nhap IN (?,?))", new Object[]{ten, tenKhac});
            db.getWritableDatabase().delete("tai_khoan", "ten_dang_nhap IN (?,?)", new String[]{ten, tenKhac});
        }
        if (idCu > 0) PhienDangNhap.luu(context, idCu, tenCu);
        else PhienDangNhap.dangXuat(context);
    }

    private long taoTaiKhoan(String name) {
        try (AppDatabase db = new AppDatabase(context)) {
            assertTrue(db.dangKy(name, MAT_KHAU, MA));
            return db.dangNhap(name, MAT_KHAU);
        }
    }

    private void nhap(int id, String text) {
        onView(withId(id)).perform(scrollTo(), replaceText(text), closeSoftKeyboard());
        UiKiemThu.huyTuDongDien();
    }

    @Test public void welcomeCoHaiDuongVaoVaHuyDangNhapQuayVeWelcome() {
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            onView(withId(R.id.edtTenDangNhap)).check(matches(isDisplayed()));
            onView(withId(R.id.dnQuayLai)).perform(scrollTo(), click());
            onView(withId(R.id.btnBatDau)).perform(scrollTo(), click());
            onView(withId(R.id.navTrangChu)).check(matches(isDisplayed()));
            assertFalse(PhienDangNhap.daDangNhap(context));
        }
    }

    @Test public void dangKyTuYeuThichRoiDangNhapQuayLaiDanhSachTrong() {
        try (ActivityScenario<YeuThichActivity> scenario = ActivityScenario.launch(YeuThichActivity.class)) {
            onView(withId(R.id.btnChuyenDangKy)).perform(scrollTo(), click());
            nhap(R.id.dkTenDangNhap, ten);
            nhap(R.id.dkMatKhau, MAT_KHAU);
            nhap(R.id.dkNhapLaiMatKhau, "KhongKhop123!");
            onView(withId(R.id.dkNutDangKy)).perform(scrollTo(), click());
            onView(withId(R.id.dkNhapLaiMatKhau)).check(matches(hasErrorText("Mật khẩu nhập lại không khớp")));
            nhap(R.id.dkNhapLaiMatKhau, MAT_KHAU);
            onView(withId(R.id.dkNutDangKy)).perform(scrollTo(), click());
            onView(withText("Đăng ký thành công")).check(matches(isDisplayed()));
            onView(withText("Tôi đã lưu mã")).perform(click());
            onView(withId(R.id.edtTenDangNhap)).check(matches(withText(ten)));
            nhap(R.id.edtMatKhau, "SaiMatKhau");
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            onView(withId(R.id.edtMatKhau)).check(matches(hasErrorText("Tên đăng nhập hoặc mật khẩu không đúng")));
            nhap(R.id.edtMatKhau, MAT_KHAU);
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            onView(withId(R.id.ytTongSo)).check(matches(withText("0 địa điểm đã lưu")));
            onView(withId(R.id.ytTieuDeTrangThai)).check(matches(withText("Chưa có địa điểm yêu thích")));
            assertTrue(PhienDangNhap.daDangNhap(context));
            try (AppDatabase db = new AppDatabase(context)) {
                assertFalse(db.dangKy(ten.toUpperCase(java.util.Locale.ROOT), MAT_KHAU, MA));
            }
        }
    }

    @Test public void quenMatKhauDoiMaVaDangNhapBangMatKhauMoi() {
        taoTaiKhoan(ten);
        try (ActivityScenario<DangNhapActivity> scenario = ActivityScenario.launch(DangNhapActivity.class)) {
            nhap(R.id.edtTenDangNhap, ten);
            onView(withId(R.id.btnQuenMatKhau)).perform(scrollTo(), click());
            onView(withId(R.id.qmkTenDangNhap)).check(matches(withText(ten)));
            nhap(R.id.qmkMaKhoiPhuc, "MA_SAI");
            nhap(R.id.qmkMatKhauMoi, "MatKhauMoi456!");
            nhap(R.id.qmkXacNhan, "MatKhauMoi456!");
            onView(withId(R.id.qmkXacNhanDoi)).perform(scrollTo(), click());
            try (AppDatabase db = new AppDatabase(context)) {
                assertTrue(db.dangNhap(ten, MAT_KHAU) > 0);
            }
            nhap(R.id.qmkMaKhoiPhuc, MA);
            onView(withId(R.id.qmkXacNhanDoi)).perform(scrollTo(), click());
            onView(withText("Đổi mật khẩu thành công")).check(matches(isDisplayed()));
            onView(withText("Tôi đã lưu mã")).perform(click());
            onView(withId(R.id.edtTenDangNhap)).check(matches(withText(ten)));
            try (AppDatabase db = new AppDatabase(context)) {
                assertEquals(-1, db.dangNhap(ten, MAT_KHAU));
                assertNull(db.doiMatKhauBangMa(ten, MA, MAT_KHAU));
            }
            nhap(R.id.edtMatKhau, "MatKhauMoi456!");
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            assertTrue(PhienDangNhap.daDangNhap(context));
        }
    }

    @Test public void yeuThichHienHueBoLuuVaDangXuatKhongLoDanhSach() throws Exception {
        long id = taoTaiKhoan(ten), idKhac = taoTaiKhoan(tenKhac);
        try (AppDatabase db = new AppDatabase(context)) {
            db.datYeuThich(id, 2, true);
            db.datYeuThich(idKhac, 0, true);
        }
        PhienDangNhap.luu(context, id, ten);
        try (ActivityScenario<YeuThichActivity> scenario = ActivityScenario.launch(YeuThichActivity.class)) {
            onView(withId(R.id.ytTongSo)).check(matches(withText("1 địa điểm đã lưu")));
            onView(withId(R.id.ytTenDiaDiem)).check(matches(withText("Đại Nội Huế")));
            android.graphics.Bitmap anh = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(
                    new java.io.File(context.getExternalFilesDir(null), "favorites-preview.png"))) {
                anh.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
            }
            anh.recycle();
            onView(withId(R.id.ytTenDiaDiem)).perform(click());
            onView(withId(R.id.nutTimYeuThich)).check(matches(isDisplayed()));
            androidx.test.espresso.Espresso.pressBack();
            onView(withId(R.id.ytBoLuu)).perform(click());
            onView(withText("Giữ lại")).perform(click());
            onView(withId(R.id.ytTongSo)).check(matches(withText("1 địa điểm đã lưu")));
            onView(withId(R.id.ytBoLuu)).perform(click());
            onView(withText("Bỏ lưu")).perform(click());
            onView(withId(R.id.ytTongSo)).check(matches(withText("0 địa điểm đã lưu")));
            onView(withId(R.id.ytDangXuat)).perform(click());
            onView(withText("Đăng xuất")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            onView(withId(R.id.ytDangNhap)).check(matches(isDisplayed()));
            onView(withId(R.id.ytTenDiaDiem)).check(doesNotExist());
            assertFalse(PhienDangNhap.daDangNhap(context));
            try (AppDatabase db = new AppDatabase(context)) { assertTrue(db.daYeuThich(idKhac, 0)); }
        }
    }

    @Test public void huyDangNhapKhongXemDuocYeuThich() {
        try (ActivityScenario<YeuThichActivity> scenario = ActivityScenario.launch(YeuThichActivity.class)) {
            onView(withId(R.id.dnQuayLai)).perform(scrollTo(), click());
            onView(withId(R.id.ytDangNhap)).check(matches(isDisplayed()));
            onView(withId(R.id.ytTenDiaDiem)).check(doesNotExist());
            assertFalse(PhienDangNhap.daDangNhap(context));
        }
    }

    @Test public void dangNhapTuWelcomeMoTrangChu() {
        taoTaiKhoan(ten);
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            nhap(R.id.edtTenDangNhap, ten);
            nhap(R.id.edtMatKhau, MAT_KHAU);
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            onView(withId(R.id.navTrangChu))
                    .inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(
                            hasDescendant(withId(R.id.navTrangChu))))
                    .check(matches(isDisplayed()));
            assertTrue(PhienDangNhap.daDangNhap(context));
        }
    }

    @Test public void dangNhapTuChiTietTiepTucLuuVaKhongXoaDiemDaLuu() {
        long id = taoTaiKhoan(ten);
        try (AppDatabase db = new AppDatabase(context)) { db.datYeuThich(id, 1, true); }
        Intent intent = new Intent(context, ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem", 1);
        try (ActivityScenario<ChiTietDiaDiemActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.nutTimYeuThich)).perform(click());
            onView(withText("Đăng nhập")).perform(click());
            nhap(R.id.edtTenDangNhap, ten);
            nhap(R.id.edtMatKhau, MAT_KHAU);
            onView(withId(R.id.btnDangNhap)).perform(scrollTo(), click());
            onView(withId(R.id.nutTimYeuThich)).check(matches(withText("♥")));
            try (AppDatabase db = new AppDatabase(context)) { assertTrue(db.daYeuThich(id, 1)); }
        }
    }
}
