package com.example.app_ck;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDiaDiem;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoQuanTriTaiKhoan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.admin.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

/** Kiểm tra hành vi qua UI và đối chiếu dữ liệu thật; mọi bản ghi đều thuộc fixture riêng. */
@RunWith(AndroidJUnit4.class)
public class QuanTriGiaoDienFlowTest {
    private Context context;
    private long admin, oldId;
    private String username, oldName, placeName;
    private int place = -1;
    private static final String POINT = "Điện Kiến Trung – khu trưng bày di sản";
    private static final double LAT = 16.469123456789, LON = 107.578987654321;
    private static final double X = 0.314159265358979, Y = 0.618033988749895;

    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        UiKiemThu.bat(context);
        oldId = PhienDangNhap.id(context); oldName = PhienDangNhap.ten(context);
        username = "admin_ui_" + Long.toHexString(System.nanoTime());
        placeName = "Di sản kiểm thử " + username;
        try (AppDatabase db = new AppDatabase(context)) {
            assertTrue(db.dangKy(username, "password123"));
            admin = db.dangNhap(username, "password123");
            db.getWritableDatabase().execSQL("UPDATE tai_khoan SET vai_tro='ADMIN' WHERE id=?", new Object[]{admin});
            PhienDangNhap.luu(context, admin, username);
            ContentValues v = new ContentValues();
            v.put("ten", placeName); v.put("mo_ta", "Bài giới thiệu chính");
            v.put("vi_do", LAT); v.put("kinh_do", LON); v.put("ban_kinh_auto", 150f);
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context, db.getWritableDatabase(), true, -1, 0, v, "", ""));
            for (DiaDiem d : db.danhSachDiaDiem()) if (placeName.equals(d.ten)) place = d.id;
            assertTrue(place >= 0);
            v = new ContentValues();
            v.put("ten", POINT); v.put("tang", 0); v.put("thu_tu", 1); v.put("so_hien_thi", 7);
            v.put("noi_dung_thuyet_minh", "Bài thuyết minh của điểm");
            v.put("x_ty_le", X); v.put("y_ty_le", Y);
            v.put("vi_do", LAT); v.put("kinh_do", LON); v.put("ban_kinh", 35f);
            v.put("cho_phep_auto", 1); v.put("nguon_gps", "Tọa độ đã xác minh");
            assertTrue(KhoDuLieuThamQuan.luuDiem(context, db.getWritableDatabase(), place, 1, v));
            try (Cursor c = db.getReadableDatabase().rawQuery(
                    "SELECT ten_anh FROM so_do_tham_quan WHERE ten_anh<>'' LIMIT 1", null)) {
                if (c.moveToFirst()) assertTrue(KhoDuLieuThamQuan.luuSoDo(context, db.getWritableDatabase(), place, 0, "Khu di sản", c.getString(0)));
            }
        }
    }

    @After public void cleanup() {
        try (AppDatabase db = new AppDatabase(context)) {
            PhienDangNhap.luu(context, admin, username);
            if (place >= 0) {
                for (DiemThamQuan p : KhoDuLieuThamQuan.danhSach(db.getReadableDatabase(), place))
                    KhoDuLieuThamQuan.xoaDiem(context, db.getWritableDatabase(), place, p.id);
                KhoDuLieuThamQuan.xoaDiaDiem(context, db.getWritableDatabase(), place);
                db.getWritableDatabase().delete("nguon_thong_tin", "ma LIKE ?", new String[]{"admin_diem_" + place + "_%"});
            }
            db.getWritableDatabase().delete("tai_khoan", "id=?", new String[]{"" + admin});
        } finally {
            if (oldId > 0) PhienDangNhap.luu(context, oldId, oldName); else PhienDangNhap.dangXuat(context);
            UiKiemThu.tat(context);
        }
    }

    private <T extends Activity> void selectPlace(ActivityScenario<T> scenario, int spinnerId) {
        scenario.onActivity(a -> {
            Spinner spinner = a.findViewById(spinnerId);
            for (int i = 0; i < spinner.getCount(); i++) if (placeName.equals(spinner.getItemAtPosition(i))) spinner.setSelection(i);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    private <T extends Activity> void selectPoint(ActivityScenario<T> scenario, int spinnerId, int position) {
        scenario.onActivity(a -> ((Spinner) a.findViewById(spinnerId)).setSelection(position));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    private void openPoint() {
        onView(withContentDescription("Chỉnh sửa điểm " + POINT)).perform(scrollTo(), click());
    }
    private DiemThamQuan point() {
        try (AppDatabase db = new AppDatabase(context)) { return KhoDuLieuThamQuan.lay(db.getReadableDatabase(), place, 1); }
    }
    private void assertLocationKept(DiemThamQuan p) {
        assertEquals(LAT, p.viDo, 0); assertEquals(LON, p.kinhDo, 0);
        assertEquals(X, p.x, 0); assertEquals(Y, p.y, 0); assertEquals(35f, p.banKinh, 0);
    }

    @Test public void pointDraftSurvivesRecreateAndCancelThenSaveKeepsGps() {
        try (ActivityScenario<QuanLyDiemThamQuanActivity> scenario = ActivityScenario.launch(QuanLyDiemThamQuanActivity.class)) {
            selectPlace(scenario, R.id.dtChonDiaDiem);
            onView(withId(R.id.dtTimKiem)).perform(scrollTo(), replaceText("dien kien trung"), closeSoftKeyboard());
            onView(withId(R.id.dtSoLuong)).check(matches(withText("1/1 điểm phù hợp")));
            openPoint();
            onView(withId(R.id.dtTen)).perform(scrollTo(), replaceText("Điểm đã sửa"), closeSoftKeyboard());
            scenario.recreate();
            onView(withId(R.id.dtTen)).check(matches(withText("Điểm đã sửa")));
            onView(withId(R.id.dtHuy)).perform(click());
            onView(withText("Tiếp tục sửa")).perform(click());
            onView(withId(R.id.dtTen)).check(matches(withText("Điểm đã sửa")));
            onView(withId(R.id.dtLuu)).perform(click());
            assertEquals("Điểm đã sửa", point().ten); assertLocationKept(point());
            onView(withText("Thay đổi chưa được lưu")).check(doesNotExist());
            onView(withId(R.id.dtXoaTim)).perform(scrollTo(), click());
            onView(withContentDescription("Chỉnh sửa điểm Điểm đã sửa")).perform(scrollTo(), click());
            onView(withId(R.id.dtHuy)).perform(click());
            onView(withText("Thay đổi chưa được lưu")).check(doesNotExist());
        }
    }

    @Test public void createValidatesFieldsAcceptsNoMarkerAndDeletesAfterConfirmation() {
        try (ActivityScenario<QuanLyDiemThamQuanActivity> scenario = ActivityScenario.launch(QuanLyDiemThamQuanActivity.class)) {
            selectPlace(scenario, R.id.dtChonDiaDiem);
            onView(withId(R.id.dtThemMoi)).perform(scrollTo(), click());
            onView(withId(R.id.dtLuu)).perform(click());
            onView(withId(R.id.dtTen)).check(matches(hasErrorText("Vui lòng nhập tên điểm tham quan")));
            onView(withId(R.id.dtTen)).perform(scrollTo(), replaceText("Điểm chưa có marker"), closeSoftKeyboard());
            onView(withId(R.id.dtThuTu)).perform(scrollTo(), replaceText(""), closeSoftKeyboard());
            onView(withId(R.id.dtLuu)).perform(click());
            onView(withId(R.id.dtThuTu)).check(matches(hasErrorText("Thứ tự phải là số nguyên")));
            onView(withId(R.id.dtThuTu)).perform(scrollTo(), replaceText("2"), closeSoftKeyboard());
            onView(withId(R.id.dtLuu)).perform(click());
            onView(withContentDescription("Chỉnh sửa điểm Điểm chưa có marker")).perform(scrollTo(), click());
            scenario.onActivity(a -> assertEquals("", ((EditText) a.findViewById(R.id.dtX)).getText().toString()));
            onView(withId(R.id.dtXoa)).perform(scrollTo(), click());
            onView(withText("Hủy")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            onView(withId(R.id.dtXoa)).perform(scrollTo(), click());
            onView(withText("Xóa")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            try (AppDatabase db = new AppDatabase(context)) { assertEquals(1, KhoDuLieuThamQuan.danhSach(db.getReadableDatabase(), place).size()); }
        }
    }

    @Test public void markerPickerZoomAndCancelLeaveStoredRatiosUntouched() {
        try (ActivityScenario<QuanLyDiemThamQuanActivity> scenario = ActivityScenario.launch(QuanLyDiemThamQuanActivity.class)) {
            selectPlace(scenario, R.id.dtChonDiaDiem); openPoint();
            onView(withId(R.id.dtChonMarker)).perform(scrollTo(), click());
            onView(withText("+")).perform(click()); onView(withText("−")).perform(click());
            onView(withText("Đặt lại")).perform(click());
            onView(withText("Hủy")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            onView(withId(R.id.dtLuu)).perform(click());
            assertLocationKept(point());
            openPoint();
            onView(withId(R.id.dtChonMarker)).perform(scrollTo(), click());
            onView(withText("+")).perform(click());
            onView(isAssignableFrom(SoDoChonMarkerView.class)).perform(scrollTo(),
                    new androidx.test.espresso.action.GeneralClickAction(
                            androidx.test.espresso.action.Tap.SINGLE,
                            androidx.test.espresso.action.GeneralLocation.CENTER,
                            androidx.test.espresso.action.Press.FINGER));
            double[] chosen = new double[2];
            onView(isAssignableFrom(SoDoChonMarkerView.class)).check((view, error) -> {
                if (error != null) throw error;
                SoDoChonMarkerView map = (SoDoChonMarkerView) view;
                chosen[0] = map.getXRatio(); chosen[1] = map.getYRatio();
                // Touch injection rounds screen pixels; the center must remain within one dp.
                assertEquals(0.5, chosen[0], 0.002);
                assertEquals(0.5, chosen[1], 0.002);
            });
            onView(withText("Xác nhận")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            onView(withId(R.id.dtLuu)).perform(click());
            assertEquals(chosen[0], point().x, 0.000001);
            assertEquals(chosen[1], point().y, 0.000001);
            assertEquals(LAT, point().viDo, 0);
        }
    }

    @Test public void pointImageAndSourceStaySeparateFromThePlaceMapAndSource() {
        String map;
        try (AppDatabase db = new AppDatabase(context)) { map = KhoDuLieuThamQuan.anhSoDo(db.getReadableDatabase(), place, 0); }
        try (ActivityScenario<QuanLyDiemThamQuanActivity> scenario = ActivityScenario.launch(QuanLyDiemThamQuanActivity.class)) {
            selectPlace(scenario, R.id.dtChonDiaDiem); openPoint();
            onView(withId(R.id.dtMoAnhNangCao)).perform(scrollTo(), click());
            onView(withId(R.id.dtTenAnh)).perform(scrollTo(), replaceText("anh_dai_noi_hue"), closeSoftKeyboard());
            onView(withId(R.id.dtXemAnhDrawable)).perform(scrollTo(), click());
            onView(withId(R.id.dtNguonTenDiem)).perform(scrollTo(), replaceText("Tài liệu riêng"), closeSoftKeyboard());
            onView(withId(R.id.dtNguonUrlDiem)).perform(scrollTo(), replaceText("https://example.com/admin-fixture"), closeSoftKeyboard());
            onView(withId(R.id.dtLuu)).perform(click());
            assertEquals("anh_dai_noi_hue", point().tenAnh);
            try (AppDatabase db = new AppDatabase(context)) {
                assertEquals(map, KhoDuLieuThamQuan.anhSoDo(db.getReadableDatabase(), place, 0));
                assertEquals(1, com.example.app_ck.data.repository.KhoNguonThongTin.danhSach(db.getReadableDatabase(), place, 1).size());
                assertTrue(com.example.app_ck.data.repository.KhoNguonThongTin.danhSach(db.getReadableDatabase(), place, 0).isEmpty());
            }
            openPoint();
            onView(withId(R.id.dtBoAnhDiem)).perform(scrollTo(), click());
            onView(withId(R.id.dtLuu)).perform(click());
            assertEquals(com.example.app_ck.ui.common.AnhDiaDiem.KHONG_ANH, point().tenAnh);
            assertLocationKept(point());
        }
    }

    @Test public void narrationCancelSwitchRecreateAndSaveTargetTheCorrectRecord() {
        try (ActivityScenario<QuanLyThuyetMinhActivity> scenario = ActivityScenario.launch(QuanLyThuyetMinhActivity.class)) {
            selectPlace(scenario, R.id.tmDiaDiem); selectPoint(scenario, R.id.tmDiem, 1);
            onView(withId(R.id.tmNoiDung)).perform(scrollTo(), replaceText("Nội dung đang soạn"), closeSoftKeyboard());
            onView(withId(R.id.tmTrangThaiLuu)).check(matches(withText("Chưa lưu")));
            selectPoint(scenario, R.id.tmDiem, 0);
            onView(withText("Tiếp tục sửa")).perform(click());
            scenario.recreate();
            onView(withId(R.id.tmNoiDung)).check(matches(withText("Nội dung đang soạn")));
            onView(withId(R.id.tmLuu)).perform(click());
            assertEquals("Nội dung đang soạn", point().noiDung);
            selectPoint(scenario, R.id.tmDiem, 0);
            onView(withId(R.id.tmNoiDung)).check(matches(withText("Bài giới thiệu chính")));
            onView(withId(R.id.tmNoiDung)).perform(scrollTo(), replaceText("Giới thiệu đã sửa"), closeSoftKeyboard());
            onView(withId(R.id.tmLuu)).perform(click());
            try (AppDatabase db = new AppDatabase(context)) { assertEquals("Giới thiệu đã sửa", KhoDiaDiem.lay(db.getReadableDatabase(), place).moTa); }
            assertEquals("Nội dung đang soạn", point().noiDung);
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
            assertLocationKept(point());
        }
    }

    @Test public void gpsOffOnlyChangesPermissionAndCancelKeepsDraftSelection() {
        try (ActivityScenario<QuanLyGpsAutoActivity> scenario = ActivityScenario.launch(QuanLyGpsAutoActivity.class)) {
            selectPlace(scenario, R.id.gpsDiaDiem); selectPoint(scenario, R.id.gpsDiem, 1);
            onView(withId(R.id.gpsChoPhep)).perform(scrollTo(), click());
            selectPoint(scenario, R.id.gpsDiem, 0);
            onView(withText("Tiếp tục sửa")).perform(click());
            scenario.recreate();
            onView(withId(R.id.gpsChoPhep)).check(matches(isNotChecked()));
            onView(withId(R.id.gpsLuu)).perform(click());
            onView(withText("Hủy")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertTrue(point().choPhepAuto);
            onView(withId(R.id.gpsLuu)).perform(click());
            onView(withText("Lưu trạng thái TẮT")).perform(click());
            assertFalse(point().choPhepAuto); assertLocationKept(point());
            onView(withId(R.id.gpsTrangThaiLuu)).check(matches(withText("Đã lưu cấu hình")));
        }
    }

    @Test public void gpsMainAndSubValidateSourceAndKeepIndependentRadius() {
        try (ActivityScenario<QuanLyGpsAutoActivity> scenario = ActivityScenario.launch(QuanLyGpsAutoActivity.class)) {
            selectPlace(scenario, R.id.gpsDiaDiem);
            onView(withId(R.id.gpsViDo)).perform(scrollTo(), replaceText("91"), closeSoftKeyboard());
            onView(withId(R.id.gpsLuu)).perform(click());
            scenario.onActivity(a -> assertNotNull(((EditText) a.findViewById(R.id.gpsViDo)).getError()));
            onView(withId(R.id.gpsViDo)).perform(scrollTo(), replaceText(String.valueOf(LAT)), closeSoftKeyboard());
            onView(withId(R.id.gpsBanKinh)).perform(scrollTo(), replaceText("200"), closeSoftKeyboard());
            onView(withId(R.id.gpsLuu)).perform(click());
            onView(withText("Lưu")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            selectPoint(scenario, R.id.gpsDiem, 1);
            onView(withId(R.id.gpsNguon)).perform(scrollTo(), replaceText(""), closeSoftKeyboard());
            onView(withId(R.id.gpsLuu)).perform(click());
            scenario.onActivity(a -> assertNotNull(((EditText) a.findViewById(R.id.gpsNguon)).getError()));
            onView(withId(R.id.gpsNguon)).perform(scrollTo(), replaceText("Nguồn đã kiểm chứng"), closeSoftKeyboard());
            onView(withId(R.id.gpsBanKinh)).perform(scrollTo(), replaceText("45"), closeSoftKeyboard());
            onView(withId(R.id.gpsLuu)).perform(click());
            onView(withText("Lưu")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertEquals(45f, point().banKinh, 0); assertEquals(LAT, point().viDo, 0);
            assertEquals(X, point().x, 0); assertTrue(point().choPhepAuto);
            try (AppDatabase db = new AppDatabase(context)) { assertEquals(200f, KhoDiaDiem.lay(db.getReadableDatabase(), place).banKinhAuto, 0); }
        }
    }

    @Test public void accountsFindAccentsLockUnlockAndPreventSelfLock() {
        long user = -1;
        String name = "visitor_" + Long.toHexString(System.nanoTime());
        try {
            try (AppDatabase db = new AppDatabase(context)) {
                assertTrue(db.dangKy(name, "password123")); user = db.dangNhap(name, "password123");
                ContentValues v = new ContentValues(); v.put("ho_ten", "Đặng Thị Ánh");
                db.getWritableDatabase().update("tai_khoan", v, "id=?", new String[]{"" + user});
            }
            try (ActivityScenario<QuanLyTaiKhoanActivity> scenario = ActivityScenario.launch(QuanLyTaiKhoanActivity.class)) {
                onView(withId(R.id.qltkTimKiem)).perform(scrollTo(), replaceText("dang thi anh"), closeSoftKeyboard());
                onView(withContentDescription("Xem chi tiết tài khoản " + name)).perform(scrollTo(), click());
                onView(withText("Khóa tài khoản")).perform(click());
                onView(withText("Khóa")).perform(click());
                try (AppDatabase db = new AppDatabase(context)) { assertFalse(KhoQuanTriTaiKhoan.chiTiet(context, db.getReadableDatabase(), user).hoatDong()); }
                onView(withContentDescription("Xem chi tiết tài khoản " + name)).perform(scrollTo(), click());
                onView(withText("Mở khóa")).perform(click());
                onView(withText("Mở khóa")).perform(click());
                try (AppDatabase db = new AppDatabase(context)) { assertTrue(KhoQuanTriTaiKhoan.chiTiet(context, db.getReadableDatabase(), user).hoatDong()); }
                onView(withId(R.id.qltkTimKiem)).perform(scrollTo(), replaceText(username), closeSoftKeyboard());
                onView(withContentDescription("Xem chi tiết tài khoản " + username)).perform(scrollTo(), click());
                onView(withText("Khóa tài khoản")).check(doesNotExist());
            }
        } finally {
            try (AppDatabase db = new AppDatabase(context)) { db.getWritableDatabase().delete("tai_khoan", "id=?", new String[]{"" + user}); }
        }
    }

    @Test public void keyboardLeavesSaveReachableOnAllThreeEditors() {
        try (ActivityScenario<QuanLyDiemThamQuanActivity> scenario = ActivityScenario.launch(QuanLyDiemThamQuanActivity.class)) {
            selectPlace(scenario, R.id.dtChonDiaDiem); openPoint();
            onView(withId(R.id.dtTen)).perform(scrollTo(), click());
            onView(withId(R.id.dtLuu)).check(matches(isDisplayed()));
            onView(withId(R.id.dtTen)).perform(closeSoftKeyboard());
        }
        try (ActivityScenario<QuanLyThuyetMinhActivity> scenario = ActivityScenario.launch(QuanLyThuyetMinhActivity.class)) {
            selectPlace(scenario, R.id.tmDiaDiem);
            onView(withId(R.id.tmNoiDung)).perform(scrollTo(), click());
            onView(withId(R.id.tmLuu)).check(matches(isDisplayed()));
            onView(withId(R.id.tmNoiDung)).perform(closeSoftKeyboard());
        }
        try (ActivityScenario<QuanLyGpsAutoActivity> scenario = ActivityScenario.launch(QuanLyGpsAutoActivity.class)) {
            selectPlace(scenario, R.id.gpsDiaDiem);
            onView(withId(R.id.gpsViDo)).perform(scrollTo(), click());
            onView(withId(R.id.gpsLuu)).check(matches(isDisplayed()));
            onView(withId(R.id.gpsViDo)).perform(closeSoftKeyboard());
        }
    }

    @Test public void previewUsesCurrentDraftAndStopsWithTheActivity() {
        try (ActivityScenario<QuanLyThuyetMinhActivity> scenario = ActivityScenario.launch(QuanLyThuyetMinhActivity.class)) {
            selectPlace(scenario, R.id.tmDiaDiem); selectPoint(scenario, R.id.tmDiem, 1);
            onView(withId(R.id.tmNoiDung)).perform(scrollTo(), replaceText("Nội dung nghe thử chưa lưu."), closeSoftKeyboard());
            long deadline = android.os.SystemClock.elapsedRealtime() + 10000;
            boolean[] ready = {false};
            do {
                scenario.onActivity(a -> ready[0] = a.findViewById(R.id.tmNgheThu).isEnabled());
                if (ready[0]) break;
                android.os.SystemClock.sleep(100);
            } while (android.os.SystemClock.elapsedRealtime() < deadline);
            scenario.onActivity(a -> {
                assertNotNull(UiKiemThu.truong(a, "tts"));
                if (ready[0]) {
                    a.findViewById(R.id.tmNgheThu).performClick();
                    assertNotNull(UiKiemThu.truong(a, "previewSession"));
                    a.findViewById(R.id.tmDung).performClick();
                    assertNull(UiKiemThu.truong(a, "previewSession"));
                    assertFalse(a.findViewById(R.id.tmDung).isEnabled());
                    a.findViewById(R.id.tmNgheThu).performClick();
                } else {
                    // Thiết bị thiếu giọng Việt phải thể hiện lý do và vẫn cho lưu văn bản.
                    String status = ((TextView) a.findViewById(R.id.tmTrangThaiTts)).getText().toString();
                    assertTrue(status.contains("giọng đọc"));
                }
            });
            assertEquals("Bài thuyết minh của điểm", point().noiDung);
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
            scenario.onActivity(a -> assertNull(UiKiemThu.truong(a, "previewSession")));
        }
    }
}
