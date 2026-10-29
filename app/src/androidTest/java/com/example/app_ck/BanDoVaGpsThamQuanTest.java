package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.location.BoNhanDienDiaDiem;
import com.example.app_ck.location.ThamQuanTuDong;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.home.MainActivity;
import com.example.app_ck.ui.home.WelcomeActivity;
import com.example.app_ck.ui.map.BanDoActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.tour.BaoTangThamQuanActivity;
import com.example.app_ck.ui.tour.ConDaoThamQuanActivity;
import com.example.app_ck.ui.tour.DaiNoiHueThamQuanActivity;
import com.example.app_ck.ui.tour.ThamQuanActivity;
import com.example.app_ck.ui.tour.VanMieuThamQuanActivity;

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
    @Test public void banDoDocMarkerVaNutChonTheoIdVaToaDoSQLite() {
        try (ActivityScenario<BanDoActivity> scenario = ActivityScenario.launch(BanDoActivity.class)) {
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoTuongTac);
                ArrayList<Marker> markers = new ArrayList<>();
                for (org.osmdroid.views.overlay.Overlay o : map.getOverlays()) if (o instanceof Marker) markers.add((Marker) o);
                try (AppDatabase db = new AppDatabase(a)) {
                    int coGps = 0;
                    for (DiaDiem d : db.danhSachDiaDiem()) {
                        if (Double.isFinite(d.viDo) && Double.isFinite(d.kinhDo)) {
                            coGps++;
                            Marker marker = markers.stream().filter(m -> String.valueOf(d.id).equals(m.getId()))
                                    .findFirst().orElseThrow(AssertionError::new);
                            assertEquals(d.ten, marker.getTitle());
                            assertEquals(d.viDo, marker.getPosition().getLatitude(), .0000001);
                            assertEquals(d.kinhDo, marker.getPosition().getLongitude(), .0000001);
                        }
                        android.view.View nut = timNutTheoId(a.findViewById(android.R.id.content), d.id);
                        assertNotNull("Thiếu nút chọn địa điểm " + d.id, nut);
                        nut.performClick();
                        assertEquals(d.ten, ((TextView) a.findViewById(R.id.tenDiaDiemBanDo)).getText().toString());
                    }
                    assertEquals(coGps, markers.size());
                }
            });
        }
    }

    private android.view.View timNutTheoId(android.view.View view, int id) {
        if (Integer.valueOf(id).equals(view.getTag())) return view;
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                android.view.View found = timNutTheoId(group.getChildAt(i), id);
                if (found != null) return found;
            }
        }
        return null;
    }

    private ActivityScenario<MainActivity> moTrangChu() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(a -> a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
        return scenario;
    }

    @Test public void gpsSaiSoLonVanHienDangTheoDoiVaKhongMoLoiMoi() {
        try (ActivityScenario<MainActivity> scenario = moTrangChu()) {
            scenario.onActivity(a -> {
                try {
                    java.lang.reflect.Field field = BaseActivity.class.getDeclaredField("thamQuanTuDong");
                    field.setAccessible(true);
                    ThamQuanTuDong bo = (ThamQuanTuDong) field.get(a);
                    // Chỉ bơm callback; không thay đổi GPS/quyền trên thiết bị.
                    java.lang.reflect.Method batDau = ThamQuanTuDong.class.getDeclaredMethod("batDau", int.class);
                    batDau.setAccessible(true);
                    assertEquals(true, batDau.invoke(bo, ThamQuanTuDong.GPS));
                    TextView viTri = a.findViewById(R.id.autoViTri);
                    TextView trangThai = a.findViewById(R.id.autoTrangThai);
                    assertTrue(viTri.getText().toString().contains("Đã bật theo dõi"));
                    DiaDiem diaDiem = null;
                    try (AppDatabase db = new AppDatabase(a)) {
                        for (DiaDiem d : db.danhSachDiaDiem()) if (d.id == 0) diaDiem = d;
                    }
                    assertNotNull(diaDiem);
                    android.location.Location l = new android.location.Location("gps");
                    l.setLatitude(diaDiem.viDo);
                    l.setLongitude(diaDiem.kinhDo);
                    l.setAccuracy(100);
                    l.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos());
                    bo.onLocationChanged(l);
                    assertTrue(viTri.getText().toString().contains("Đang theo dõi"));
                    assertTrue(viTri.getText().toString().contains("Sai số ±100 m"));
                    TextView khoangCach = a.findViewById(R.id.autoKhoangCach);
                    assertTrue(khoangCach.getText().toString().contains(diaDiem.ten + ": 0 m"));
                    assertTrue(khoangCach.getText().toString().contains("đường chim bay"));
                    try (AppDatabase db = new AppDatabase(a)) {
                        for (DiaDiem d : db.danhSachDiaDiem())
                            assertTrue(khoangCach.getText().toString().contains(d.ten + ": "));
                    }
                    assertTrue(trangThai.getText().toString().contains("cần ≤25 m"));
                    java.lang.reflect.Field nhanDien = ThamQuanTuDong.class.getDeclaredField("nhanDien");
                    nhanDien.setAccessible(true);
                    assertFalse(((BoNhanDienDiaDiem) nhanDien.get(bo)).dangChoTraLoi());
                    java.lang.reflect.Field phien = ThamQuanTuDong.class.getDeclaredField("phien");
                    phien.setAccessible(true);
                    java.lang.reflect.Method nhip = ThamQuanTuDong.class.getDeclaredMethod("nhip", int.class);
                    nhip.setAccessible(true);
                    nhip.invoke(bo, phien.getInt(bo));
                    assertTrue(trangThai.getText().toString().contains("cần ≤25 m"));

                    l.setAccuracy(3);
                    l.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos() - 20000000000L);
                    String viTriMoi = viTri.getText().toString();
                    bo.onLocationChanged(l);
                    assertEquals("Mẫu cũ không ghi đè mẫu mới", viTriMoi, viTri.getText().toString());
                    // Khi chỉ có mẫu cũ, không trình bày khoảng cách đó là hiện tại.
                    bo.dung("Bắt đầu lại kiểm thử");
                    batDau.invoke(bo, ThamQuanTuDong.GPS);
                    bo.onLocationChanged(l);
                    assertTrue(viTri.getText().toString().contains("đo cách đây"));
                    assertTrue(trangThai.getText().toString().contains("Vị trí cũ"));
                    assertFalse(khoangCach.getText().toString().contains(diaDiem.ten + ": 0 m"));
                    assertFalse(((BoNhanDienDiaDiem) nhanDien.get(bo)).dangChoTraLoi());
                    if (android.os.Build.VERSION.SDK_INT >= 31) {
                        l.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos());
                        l.setMock(true);
                        bo.onLocationChanged(l);
                        assertTrue(trangThai.getText().toString().contains("GPS giả lập"));
                        assertFalse(khoangCach.getText().toString().contains(diaDiem.ten + ": 0 m"));
                    }
                    bo.dung("Kết thúc kiểm thử");
                    assertEquals("Đã tắt theo dõi vị trí", viTri.getText().toString());
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            });
        }
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
            onView(withId(R.id.ngheTenDiem)).inRoot(UiKiemThu.hoiThoai())
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(withText("Đại Nội Huế")));
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                assertEquals(2, chiTiet.getIntent().getIntExtra("viTriDiaDiem", -1));
                assertFalse("Không giữ cờ để tự phát lần sau", chiTiet.getIntent().hasExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI));
                assertNull(chiTiet.findViewById(R.id.autoGps));
                assertNull(chiTiet.findViewById(R.id.autoDemo));
                assertNotNull(chiTiet.findViewById(R.id.nutNgheThuyetMinh));
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
        for (int i = 0; i < classes.length; i++) {
            @SuppressWarnings("unchecked") Class<BaseActivity> lop = (Class<BaseActivity>) classes[i];
            final int id = i;
            try (ActivityScenario<BaseActivity> scenario = ActivityScenario.launch(lop)) {
                scenario.onActivity(a -> {
                    int soDiem;
                    try (AppDatabase helper = new AppDatabase(a)) {
                        soDiem = KhoDuLieuThamQuan.danhSach(helper.getReadableDatabase(), id).size();
                    }
                    assertTrue("Fixture có ít nhất hai điểm để kiểm tra chuyển điểm", soDiem >= 2);
                    String dem = ((TextView) a.findViewById(R.id.tqDemKhuVuc)).getText().toString();
                    assertTrue(dem, dem.endsWith("/" + soDiem));
                    assertNull(a.findViewById(R.id.autoDemo));
                    assertNull(a.findViewById(R.id.autoGps));
                    assertNotNull(a.findViewById(R.id.tqNutPhat));
                    String truoc = ((TextView) a.findViewById(R.id.tqTenKhuVuc)).getText().toString();
                    a.findViewById(R.id.tqNutTiep).performClick();
                    assertNotEquals(truoc, ((TextView) a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    String sau = ((TextView) a.findViewById(R.id.tqDemKhuVuc)).getText().toString();
                    assertNotEquals("Chuyển điểm phải cập nhật bộ đếm", dem, sau);
                    assertTrue(sau, sau.endsWith("/" + soDiem));
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
