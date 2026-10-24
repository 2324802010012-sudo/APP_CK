package com.example.app_ck;

import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BanDoVaGpsThamQuanTest {
    @Test public void banDoCoDuNamMarkerDungToaDoVaNutChonDungId() {
        try (ActivityScenario<BanDoActivity> scenario = ActivityScenario.launch(BanDoActivity.class)) {
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoTuongTac);
                ArrayList<Marker> markers = new ArrayList<>();
                for (org.osmdroid.views.overlay.Overlay o : map.getOverlays()) if (o instanceof Marker) markers.add((Marker) o);
                assertEquals(5, markers.size());
                int[] nuts = {R.id.nutChonDinhDocLap, R.id.nutChonBaoTang, R.id.nutChonDaiNoiHue,
                        R.id.nutChonConDao, R.id.nutChonVanMieu};
                try (TaiKhoanDb db = new TaiKhoanDb(a)) {
                    for (DiaDiem d : db.danhSachDiaDiem()) {
                        Marker marker = markers.stream().filter(m -> d.ten.equals(m.getTitle())).findFirst().orElseThrow(AssertionError::new);
                        assertEquals(KhoToaDoThamQuan.TOA_DO[d.id][0], marker.getPosition().getLatitude(), .0000001);
                        assertEquals(KhoToaDoThamQuan.TOA_DO[d.id][1], marker.getPosition().getLongitude(), .0000001);
                        a.findViewById(nuts[d.id]).performClick();
                        assertEquals(d.ten, ((TextView) a.findViewById(R.id.tenDiaDiemBanDo)).getText().toString());
                    }
                }
            });
        }
    }

    private ActivityScenario<MainActivity> moTrangChu() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(a -> a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
        return scenario;
    }

    @Test public void demoCaNamDiaDiemTuTrangChuHoiTruocKhiPhat() {
        try (ActivityScenario<MainActivity> scenario = moTrangChu()) {
            for (int id = 0; id < 5; id++) {
                final int chon = id;
                scenario.onActivity(a -> ((android.widget.Spinner) a.findViewById(R.id.autoChonDiaDiemDemo)).setSelection(chon));
                batDemo(scenario);
                // Tuyến tự chạy được kiểm tra ở ca xác nhận; ở đây dùng nút chuyển nhanh để thử đủ 5 điểm.
                scenario.onActivity(a -> {
                    for (int step=0; step<3; step++) a.findViewById(R.id.autoDemoDenGan).performClick();
                });
                doiLoiMoi(scenario);
                scenario.onActivity(a -> assertFalse("Chưa xác nhận thì chưa phát", a.mayDocGioiThieu().isSpeaking()));
                onView(withText("Bỏ qua")).inRoot(UiKiemThu.hoiThoai()).perform(click());
                scenario.onActivity(a -> assertTrue(((TextView) a.findViewById(R.id.autoTrangThai))
                        .getText().toString().contains("Không phát âm thanh")));
            }
        }
    }

    @Test public void batDauTuDongMoDungChiTietVaPhatSauXacNhan() {
        try (ActivityScenario<MainActivity> scenario = moTrangChu()) {
            scenario.onActivity(a -> ((android.widget.Spinner) a.findViewById(R.id.autoChonDiaDiemDemo)).setSelection(2));
            batDemo(scenario);
            doiLoiMoi(scenario);
            onView(withText("Bắt đầu")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            ChiTietDiaDiemActivity chiTiet = doiChiTiet();
            final java.util.concurrent.atomic.AtomicReference<String> status = new java.util.concurrent.atomic.AtomicReference<>();
            long deadline = android.os.SystemClock.elapsedRealtime() + 12000;
            do {
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                        status.set(((TextView) chiTiet.findViewById(R.id.trangThaiThuyetMinh)).getText().toString()));
                if (!status.get().contains("Đang chuẩn bị")) break;
                android.os.SystemClock.sleep(100);
            } while (android.os.SystemClock.elapsedRealtime() < deadline);
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                assertEquals(2, chiTiet.getIntent().getIntExtra("viTriDiaDiem", -1));
                assertFalse("Không giữ cờ để tự phát lần sau", chiTiet.getIntent().hasExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI));
                assertNull(chiTiet.findViewById(R.id.autoGps));
                assertNull(chiTiet.findViewById(R.id.autoDemo));
                assertNotNull(chiTiet.findViewById(R.id.nutNgheThuyetMinh));
                assertTrue(status.get(), status.get().contains("Đang phát giới thiệu Đại Nội Huế")
                        || status.get().contains("Đã đọc xong giới thiệu Đại Nội Huế")
                        || status.get().contains("Chưa có giọng đọc tiếng Việt"));
                chiTiet.finish();
            });
            scenario.onActivity(a -> assertEquals("Chưa bật hướng dẫn",
                    ((TextView) a.findViewById(R.id.autoCheDo)).getText().toString()));
        }
    }

    @Test public void moChiTietThuongKhongTuDocVaKhoiHuongDanNamDuoiMenu() {
        android.content.Context context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();
        try (ActivityScenario<ChiTietDiaDiemActivity> scenario = ActivityScenario.launch(
                new android.content.Intent(context, ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem", 0))) {
            scenario.onActivity(a -> assertEquals(android.view.View.GONE,
                    a.findViewById(R.id.trangThaiThuyetMinh).getVisibility()));
        }
        try (ActivityScenario<MainActivity> scenario = moTrangChu()) {
            scenario.onActivity(a -> {
                android.view.View menu = a.findViewById(R.id.thanhChucNang);
                android.view.View huongDan = a.findViewById(R.id.khungThuyetMinhTuDong);
                android.view.ViewGroup parent = (android.view.ViewGroup) menu.getParent();
                assertSame(parent, huongDan.getParent());
                assertEquals(parent.indexOfChild(menu) + 1, parent.indexOfChild(huongDan));
            });
        }
    }

    private ChiTietDiaDiemActivity doiChiTiet() {
        java.util.concurrent.atomic.AtomicReference<ChiTietDiaDiemActivity> result = new java.util.concurrent.atomic.AtomicReference<>();
        long deadline = android.os.SystemClock.elapsedRealtime() + 12000;
        do {
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (android.app.Activity a : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED))
                    if (a instanceof ChiTietDiaDiemActivity) result.set((ChiTietDiaDiemActivity) a);
            });
            if (result.get() != null) return result.get();
            android.os.SystemClock.sleep(100);
        } while (android.os.SystemClock.elapsedRealtime() < deadline);
        fail("Xác nhận phải tự mở trang chi tiết");
        return null;
    }

    @Test public void caNamSoDoChiCoMarkerThuCongKhongCoGpsDemo() {
        Class<?>[] classes = {ThamQuanActivity.class, BaoTangThamQuanActivity.class,
                DaiNoiHueThamQuanActivity.class, ConDaoThamQuanActivity.class, VanMieuThamQuanActivity.class};
        int[] soDiem = {25, 13, 10, 19, 16};
        for (int i = 0; i < classes.length; i++) {
            @SuppressWarnings("unchecked") Class<BaseActivity> lop = (Class<BaseActivity>) classes[i];
            final int id = i;
            try (ActivityScenario<BaseActivity> scenario = ActivityScenario.launch(lop)) {
                scenario.onActivity(a -> {
                    String dem = ((TextView) a.findViewById(R.id.tqDemKhuVuc)).getText().toString();
                    assertTrue(dem, dem.endsWith("/" + soDiem[id]));
                    assertNull(a.findViewById(R.id.autoDemo));
                    assertNull(a.findViewById(R.id.autoGps));
                    assertNotNull(a.findViewById(R.id.tqNutPhat));
                    String truoc = ((TextView) a.findViewById(R.id.tqTenKhuVuc)).getText().toString();
                    a.findViewById(R.id.tqNutTiep).performClick();
                    assertNotEquals(truoc, ((TextView) a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    String sau = ((TextView) a.findViewById(R.id.tqDemKhuVuc)).getText().toString();
                    assertNotEquals("Chuyển điểm phải cập nhật bộ đếm", dem, sau);
                    assertTrue(sau, sau.endsWith("/" + soDiem[id]));
                });
            }
        }
    }

    @Test public void roiTrangChuKhiDangHoiKhongTuPhatLai() {
        try (ActivityScenario<MainActivity> scenario = moTrangChu()) {
            batDemo(scenario);
            doiLoiMoi(scenario);
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            scenario.onActivity(a -> {
                assertEquals("Chưa bật hướng dẫn", ((TextView) a.findViewById(R.id.autoCheDo)).getText().toString());
                assertFalse(a.mayDocGioiThieu().isSpeaking());
            });
        }
    }

    @Test public void welcomeBatDauMoTrangChuVaKhoiDongYeuCauViTri() {
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            onView(withId(R.id.btnBatDau)).perform(click());
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                MainActivity main = null;
                for (androidx.test.runner.lifecycle.Stage stage : new androidx.test.runner.lifecycle.Stage[]{
                        androidx.test.runner.lifecycle.Stage.RESUMED, androidx.test.runner.lifecycle.Stage.STARTED,
                        androidx.test.runner.lifecycle.Stage.PAUSED})
                    for (android.app.Activity a : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))
                        if (a instanceof MainActivity) main = (MainActivity) a;
                assertNotNull(main);
                assertNotNull(main.findViewById(R.id.autoGps));
                assertFalse(main.getIntent().hasExtra(MainActivity.BAT_DAU_HUONG_DAN));
                String text = ((TextView) main.findViewById(R.id.autoTrangThai)).getText().toString();
                assertFalse(text.contains("Tự nhận diện điểm đến gần bạn"));
                main.finish();
            });
        }
    }

    private void batDemo(ActivityScenario<MainActivity> scenario) {
        final android.view.View[] root = new android.view.View[1];
        scenario.onActivity(a -> root[0] = a.getWindow().getDecorView());
        onView(withId(R.id.autoDemo))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(org.hamcrest.Matchers.is(root[0])))
                .perform(scrollTo());
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        android.os.SystemClock.sleep(300);
        onView(withId(R.id.autoDemo))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(org.hamcrest.Matchers.is(root[0])))
                .perform(click());
        scenario.onActivity(a -> assertEquals("DEMO · Vị trí mô phỏng",
                ((TextView) a.findViewById(R.id.autoCheDo)).getText().toString()));
    }

    private void doiLoiMoi(ActivityScenario<MainActivity> scenario) {
        long deadline = android.os.SystemClock.elapsedRealtime() + 18000;
        java.util.concurrent.atomic.AtomicBoolean xong = new java.util.concurrent.atomic.AtomicBoolean();
        java.util.concurrent.atomic.AtomicReference<String> text = new java.util.concurrent.atomic.AtomicReference<>();
        while (!xong.get() && android.os.SystemClock.elapsedRealtime() < deadline) {
            scenario.onActivity(a -> {
                text.set(((TextView) a.findViewById(R.id.autoTrangThai)).getText().toString());
                xong.set(text.get().contains("Đang chờ bạn xác nhận"));
            });
            if (!xong.get()) android.os.SystemClock.sleep(100);
        }
        assertTrue("Phải hiện lời mời ở 90 m; trạng thái: " + text.get(), xong.get());
    }
}
