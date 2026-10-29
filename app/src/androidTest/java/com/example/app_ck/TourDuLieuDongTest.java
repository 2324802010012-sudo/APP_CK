package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.ui.tour.BaoTangThamQuanActivity;
import com.example.app_ck.ui.tour.ThamQuanActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Các ID mới không liên tiếp phải đi qua giao diện, trình nghe và lịch sử. */
@RunWith(AndroidJUnit4.class)
public class TourDuLieuDongTest {
    private Context context;
    private int idDiem, idTang;
    private String tenTaiKhoan;
    private long taiKhoan;
    private final boolean[] daThem = new boolean[2];
    private final boolean[] daThemDiem = new boolean[2];

    @Before public void taoDuLieuRieng() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        int ma = (int) (System.nanoTime() & 0x1fffffff);
        idDiem = 1000000 + ma;
        idTang = 600000000 + ma;
        tenTaiKhoan = "tour_sqlite_" + Long.toHexString(System.nanoTime());
        UiKiemThu.bat(context);
        try (AppDatabase helper = new AppDatabase(context)) {
            assertTrue(helper.dangKy(tenTaiKhoan, "TourSqlite123!", "MA_TOUR"));
            taiKhoan = helper.dangNhap(tenTaiKhoan, "TourSqlite123!");
            assertTrue(taiKhoan > 0);
            SQLiteDatabase db = helper.getWritableDatabase();
            for (int place = 0; place < 2; place++) {
                assertNull(KhoDuLieuThamQuan.lay(db, place, idDiem));
                ContentValues floor = new ContentValues();
                floor.put("dia_diem_id", place);
                floor.put("tang", idTang);
                floor.put("ten", "Khu vực thử " + place);
                floor.put("ten_anh", "");
                db.insertOrThrow(KhoDuLieuThamQuan.BANG_SO_DO, null, floor);
                daThem[place] = true;
                ContentValues point = new ContentValues();
                point.put("dia_diem_id", place);
                point.put("id", idDiem);
                point.put("ten", tenDiem(place));
                point.put("mo_ta", "Mô tả ngắn từ SQLite " + place);
                point.put("noi_dung_thuyet_minh", noiDung(place));
                point.put("tang", idTang);
                point.put("thu_tu", Integer.MAX_VALUE);
                db.insertOrThrow(KhoDuLieuThamQuan.BANG_DIEM, null, point);
                daThemDiem[place] = true;
            }
        }
    }

    @After public void donDuLieuRieng() {
        try (AppDatabase helper = new AppDatabase(context)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            KhoLichSu.xoa(db, taiKhoan, null);
            for (int place = 0; place < 2; place++) if (daThem[place]) {
                String[] khoa = {String.valueOf(place), String.valueOf(idDiem)};
                if (daThemDiem[place]) db.delete(KhoDuLieuThamQuan.BANG_DIEM, "dia_diem_id=? AND id=?", khoa);
                db.delete(KhoDuLieuThamQuan.BANG_SO_DO, "dia_diem_id=? AND tang=?",
                        new String[]{String.valueOf(place), String.valueOf(idTang)});
            }
            db.delete("tai_khoan", "ten_dang_nhap=?", new String[]{tenTaiKhoan});
        }
        UiKiemThu.tat(context);
    }

    private String tenDiem(int place) { return "Điểm SQLite mới " + place; }
    private String noiDung(int place) { return "Bài thuyết minh được sửa trong SQLite " + place; }

    @Test public void diemMoiKhongSoDoHienThiVaMoDungBaiNgheSauKhiTaoLai() {
        kiemTraTour(0, ThamQuanActivity.class);
        kiemTraTour(1, BaoTangThamQuanActivity.class);
    }

    private void kiemTraTour(int place, Class<? extends ThamQuanActivity> lop) {
        Intent intent = new Intent(context, lop).putExtra("viTriDiaDiem", place)
                .putExtra("idMarker", idDiem);
        try (ActivityScenario<ThamQuanActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                assertEquals(tenDiem(place), chu(activity, R.id.tqTenKhuVuc));
                assertEquals(noiDung(place), chu(activity, R.id.tqMoTaKhuVuc));
                View khuVuc = timNhan(activity.findViewById(R.id.tqDanhSachKhuVuc), "Khu vực thử " + place);
                assertNotNull("Tầng mới từ SQLite phải có nút chọn", khuVuc);
                assertFalse(activity.findViewById(R.id.tqNutTiep).isEnabled());
                activity.findViewById(R.id.tqNutTruoc).performClick();
                assertNotEquals(tenDiem(place), chu(activity, R.id.tqTenKhuVuc));
                activity.findViewById(R.id.tqNutTiep).performClick();
                assertEquals(tenDiem(place), chu(activity, R.id.tqTenKhuVuc));
                Dialog dialog = (Dialog) truong(truong(activity, "khungNghe"), "dialog");
                assertTrue(dialog.isShowing());
                assertEquals(noiDung(place), ((TextView) dialog.findViewById(R.id.ngheNoiDung)).getText().toString());
                assertEquals(place, ((Number) truong(activity, "diaDiemNguon")).intValue());
                assertEquals(idDiem, ((Number) truong(activity, "diemNguon")).intValue());
                assertNull("Chọn điểm chưa tự phát âm thanh", truong(truong(activity, "khungNghe"), "player"));
                dialog.findViewById(R.id.ngheDong).performClick();
                View diem = timNhan(activity.findViewById(R.id.tqDanhSachKhuVuc), idDiem + ".  " + tenDiem(place));
                assertNotNull("Điểm không có marker vẫn phải có trong danh sách", diem);
                diem.performClick();
                assertTrue(((Dialog) truong(truong(activity, "khungNghe"), "dialog")).isShowing());
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(tenDiem(place), chu(activity, R.id.tqTenKhuVuc));
                assertNull("Tạo lại màn hình không tự mở bài nghe", truong(activity, "khungNghe"));
            });
            scenario.moveToState(Lifecycle.State.CREATED);
            try (AppDatabase helper = new AppDatabase(context)) {
                ContentValues change = new ContentValues();
                change.put("ten", "Đã đổi tên " + place);
                change.put("noi_dung_thuyet_minh", "Bài nghe mới " + place);
                assertEquals(1, helper.getWritableDatabase().update(KhoDuLieuThamQuan.BANG_DIEM,
                        change, "dia_diem_id=? AND id=?", new String[]{String.valueOf(place), String.valueOf(idDiem)}));
            }
            scenario.moveToState(Lifecycle.State.RESUMED);
            scenario.onActivity(activity -> {
                assertEquals("Đã đổi tên " + place, chu(activity, R.id.tqTenKhuVuc));
                assertEquals("Bài nghe mới " + place, chu(activity, R.id.tqMoTaKhuVuc));
            });
        }
    }

    @Test public void lichSuDiemTrungIdKhacDiaDiemVaDoiTenVanGiuDungLienKet() {
        try (AppDatabase helper = new AppDatabase(context)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            assertTrue(KhoLichSu.ghi(db, taiKhoan, 0, idDiem, KhoLichSu.MANUAL, 101, "noi-0"));
            assertTrue(KhoLichSu.ghi(db, taiKhoan, 1, idDiem, KhoLichSu.MANUAL, 102, "noi-1"));
            ArrayList<KhoLichSu.Luot> history = KhoLichSu.doc(db, taiKhoan);
            assertEquals(2, history.size());
            assertEquals(1, history.get(0).diaDiem);
            assertEquals(tenDiem(1), history.get(0).tenDiem);
            assertEquals(0, history.get(1).diaDiem);
            assertEquals(tenDiem(0), history.get(1).tenDiem);
            ContentValues rename = new ContentValues();
            rename.put("ten", "Điểm đã đổi tên");
            assertEquals(1, db.update(KhoDuLieuThamQuan.BANG_DIEM, rename,
                    "dia_diem_id=? AND id=?", new String[]{"0", String.valueOf(idDiem)}));
        }
        try (AppDatabase helper = new AppDatabase(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            assertEquals("Điểm đã đổi tên", KhoDuLieuThamQuan.lay(db, 0, idDiem).ten);
            assertEquals(tenDiem(1), KhoDuLieuThamQuan.lay(db, 1, idDiem).ten);
            assertTrue(KhoLichSu.ghi(helper.getWritableDatabase(), taiKhoan, 0, idDiem,
                    KhoLichSu.MANUAL, 103, "doi-ten"));
            KhoLichSu.Luot moi = KhoLichSu.doc(db, taiKhoan).get(0);
            assertEquals(0, moi.diaDiem);
            assertEquals(idDiem, moi.diem);
            assertEquals("Điểm đã đổi tên", moi.tenDiem);
        }
    }

    private String chu(ThamQuanActivity activity, int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    private View timNhan(View root, String text) {
        if (root instanceof TextView && text.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = timNhan(group.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Object truong(Object object, String name) {
        for (Class<?> cls = object.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Field field = cls.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException ex) {
                // Tìm ở lớp cha khi Activity dùng chung phần hiển thị.
            } catch (IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
        }
        throw new AssertionError(name);
    }
}
