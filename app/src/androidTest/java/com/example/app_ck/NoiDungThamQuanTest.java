package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoNguonThongTin;
import com.example.app_ck.data.repository.KhoNoiDungThamQuan;
import com.example.app_ck.data.seed.DuLieuNoiDungThamQuan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashSet;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoiDungThamQuanTest {
    private static final int[] SO_DIEM = {25, 13, 10, 19, 16};
    private static final String[] BANG = {"thuyet_minh", "bao_tang_marker", "dai_noi_hue_diem", "con_dao_diem", "van_mieu_diem"};

    @Test public void duNamDiaDiemTamMuoiBaDiemVaNguonChinhThuc() {
        assertEquals(5, DuLieuNoiDungThamQuan.DIA_DIEM.size());
        for (int id = 0; id < 5; id++) {
            BaiThuyetMinh diaDiem = DuLieuNoiDungThamQuan.lay(id, 0);
            assertEquals(SO_DIEM[id], diaDiem.cacDiem.size());
            for (int diem = 0; diem <= SO_DIEM[id]; diem++) {
                BaiThuyetMinh bai = DuLieuNoiDungThamQuan.lay(id, diem);
                assertFalse(bai.gioiThieu.trim().isEmpty());
                assertFalse(bai.diemNhan.trim().isEmpty());
                assertFalse(bai.thamQuan.trim().isEmpty());
                assertTrue(KhoNoiDungThamQuan.bai(bai).length() < 3500);
                assertFalse(bai.nguon.isEmpty());
                HashSet<String> khongTrung = new HashSet<>();
                for (String maNguon : bai.nguon) {
                    assertTrue(khongTrung.add(maNguon));
                    DuLieuNoiDungThamQuan.Nguon s = DuLieuNoiDungThamQuan.NGUON.get(maNguon);
                    assertNotNull(s);
                    assertTrue(s.url.startsWith("https://"));
                    assertFalse(s.coQuan.isEmpty());
                }
            }
        }
    }

    private SQLiteDatabase taoDuLieu() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        try (AppDatabase helper = new AppDatabase(context)) { helper.onCreate(db); }
        return db;
    }

    @Test public void caiMoiDuNoiDungVaNguonTrongSQLite() {
        try (SQLiteDatabase db = taoDuLieu()) {
            for (int id = 0; id < 5; id++) {
                assertEquals(SO_DIEM[id], KhoDuLieuThamQuan.danhSach(db, id).size());
                for (int point = 0; point <= SO_DIEM[id]; point++) {
                    assertEquals(DuLieuNoiDungThamQuan.lay(id, point).ten,
                            KhoNoiDungThamQuan.lay(db, id, point).ten);
                    assertFalse(KhoNoiDungThamQuan.bai(KhoNoiDungThamQuan.lay(db, id, point)).isEmpty());
                    assertFalse(KhoNguonThongTin.danhSach(db, id, point).isEmpty());
                }
            }
        }
    }

    @Test public void moLaiKhongDongBoGhiDeNoiDungDaSuaVaKhongBatBuocDuSoDiemMau() {
        try (SQLiteDatabase db = taoDuLieu()) {
            db.execSQL("UPDATE diem_tham_quan_chung SET ten='Tên đã sửa',noi_dung_thuyet_minh='Bài đã sửa',x_ty_le=0.123,y_ty_le=0.456 WHERE dia_diem_id=4 AND id=1");
            db.delete(KhoDuLieuThamQuan.BANG_DIEM, "dia_diem_id=4 AND id=16", null);
            android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
            try (AppDatabase helper = new AppDatabase(context)) { helper.onOpen(db); helper.onOpen(db); }
            assertEquals(15, KhoDuLieuThamQuan.danhSach(db, 4).size());
            DiemThamQuan point = KhoDuLieuThamQuan.lay(db, 4, 1);
            assertEquals("Tên đã sửa", point.ten);
            assertEquals("Bài đã sửa", point.noiDung);
            assertEquals(0.123, point.x, .00001);
            assertEquals(0.456, point.y, .00001);
            assertEquals("Tên đã sửa", KhoNoiDungThamQuan.lay(db, 4, 1).ten);
            assertEquals("Bài đã sửa", KhoNoiDungThamQuan.bai(KhoNoiDungThamQuan.lay(db, 4, 1)));
        }
    }
}
