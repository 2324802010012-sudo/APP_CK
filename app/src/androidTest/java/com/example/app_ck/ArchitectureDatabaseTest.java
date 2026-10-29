package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoNoiDungThamQuan;
import com.example.app_ck.data.repository.KhoToaDoThamQuan;
import com.example.app_ck.data.seed.DuLieuNoiDungThamQuan;
import com.example.app_ck.session.PhienDangNhap;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ArchitectureDatabaseTest {
    private Context context;
    @Before public void prepare() {
        Context target=InstrumentationRegistry.getInstrumentation().getTargetContext();
        context=new ContextWrapper(target) {
            @Override public File getDatabasePath(String name) {
                File dir=new File(getCacheDir(),"architecture-database-tests");
                if(!dir.isDirectory() && !dir.mkdirs()) throw new IllegalStateException("Cannot create test database directory");
                return new File(dir,name);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory) {
                return openOrCreateDatabase(name,mode,factory,null);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory,DatabaseErrorHandler handler) {
                return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(),factory,SQLiteDatabase.CREATE_IF_NECESSARY,handler);
            }
            @Override public boolean deleteDatabase(String name) { return SQLiteDatabase.deleteDatabase(getDatabasePath(name)); }
            @Override public SharedPreferences getSharedPreferences(String name,int mode) {
                return target.getSharedPreferences("architecture-test-"+name,mode);
            }
        };
        context.deleteDatabase(AppDatabase.DB); PhienDangNhap.dangXuat(context);
    }
    @After public void clean() { PhienDangNhap.dangXuat(context); context.deleteDatabase(AppDatabase.DB); }

    @Test public void freshInstallSeedsAllToursAndNoAdminAccount() {
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            assertEquals(15,db.getVersion()); assertEquals(5,count(db,"SELECT COUNT(*) FROM dia_diem"));
            int total=0;
            for(BaiThuyetMinh seed:DuLieuNoiDungThamQuan.DIA_DIEM) {
                assertEquals(seed.noiDung(),KhoNoiDungThamQuan.lay(db,seed.id,0).noiDung());
                assertEquals(seed.cacDiem.size(),KhoDuLieuThamQuan.danhSach(db,seed.id).size());
                total+=seed.cacDiem.size();
            }
            assertEquals(total,count(db,"SELECT COUNT(*) FROM diem_tham_quan_chung"));
            assertEquals(0,count(db,"SELECT COUNT(*) FROM tai_khoan WHERE vai_tro='ADMIN'"));
            try(Cursor c=db.rawQuery("PRAGMA foreign_key_check(diem_tham_quan_chung)",null)) { assertFalse(c.moveToFirst()); }
        }
    }

    @Test public void upgrade14KeepsCredentialsHistoryCustomIdsMapsAndMarkerCoordinates() {
        long user; String credentials;
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            assertTrue(helper.dangKy("old_user","password123","recovery-code"));
            user=helper.dangNhap("old_user","password123");
            credentials=text(db,"SELECT muoi||':'||mat_khau_bam||':'||ma_khoi_phuc_bam FROM tai_khoan WHERE id="+user);
            helper.datYeuThich(user,2,true);
            assertTrue(KhoLichSu.ghi(db,user,2,1,KhoLichSu.MANUAL,100,"old-listen"));
            db.execSQL("DROP TABLE diem_tham_quan_chung"); db.execSQL("DROP TABLE so_do_tham_quan");
            db.execSQL("INSERT INTO khu_vuc VALUES(91,0,'Tầng tùy chỉnh',9,'custom_floor_image')");
            db.execSQL("INSERT INTO diem_tham_quan(id,khu_vuc_id,ten,mo_ta,toa_do_x,toa_do_y,ten_anh) VALUES(91,91,'Điểm riêng Dinh','Mô tả riêng',0.123,0.789,'custom_photo')");
            db.execSQL("INSERT INTO thuyet_minh(diem_tham_quan_id,noi_dung) VALUES(91,'Bài nghe riêng Dinh')");
            db.execSQL("CREATE TABLE dai_noi_hue_diem(id INTEGER PRIMARY KEY,ten TEXT,mo_ta TEXT)");
            db.execSQL("INSERT INTO dai_noi_hue_diem VALUES(91,'Điểm riêng Huế','Bài nghe riêng Huế')");
            db.execSQL("CREATE TABLE dai_noi_hue_toa_do(id INTEGER PRIMARY KEY,x_ty_le REAL,y_ty_le REAL)");
            db.execSQL("INSERT INTO dai_noi_hue_toa_do VALUES(91,0.234,0.678)");
            db.execSQL("UPDATE dia_diem SET ten='Huế đã sửa',mo_ta='Tổng quan đã sửa',vi_do=16.4,kinh_do=107.5 WHERE id=2");
            db.setVersion(14);
        }
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            assertEquals(credentials,text(db,"SELECT muoi||':'||mat_khau_bam||':'||ma_khoi_phuc_bam FROM tai_khoan WHERE id="+user));
            assertEquals(user,helper.dangNhap("old_user","password123"));
            assertTrue(helper.daYeuThich(user,2)); assertEquals(1,KhoLichSu.doc(db,user).size());
            DiemThamQuan dinh=KhoDuLieuThamQuan.lay(db,0,91),hue=KhoDuLieuThamQuan.lay(db,2,91);
            assertNotNull(dinh); assertNotNull(hue);
            assertEquals("Bài nghe riêng Dinh",dinh.noiDung); assertEquals("Bài nghe riêng Huế",hue.noiDung);
            assertEquals(9,dinh.tang); assertEquals("custom_floor_image",KhoDuLieuThamQuan.anhSoDo(db,0,9));
            assertEquals(0.123,dinh.x,0.000001); assertEquals(0.789,dinh.y,0.000001);
            assertEquals(0.234,hue.x,0.000001); assertEquals(0.678,hue.y,0.000001);
            assertEquals("Tổng quan đã sửa",KhoNoiDungThamQuan.lay(db,2,0).noiDung());
            assertEquals(1,count(db,"SELECT COUNT(*) FROM diem_tham_quan WHERE id=91"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM dia_diem WHERE id=2 AND vi_do=16.4 AND kinh_do=107.5"));
            assertTrue(KhoLichSu.ghi(db,user,0,91,KhoLichSu.MANUAL,101,"same-id-dinh"));
            assertTrue(KhoLichSu.ghi(db,user,2,91,KhoLichSu.MANUAL,102,"same-id-hue"));
            assertEquals("Điểm riêng Huế",KhoLichSu.doc(db,user).get(0).tenDiem);
            assertEquals("Điểm riêng Dinh",KhoLichSu.doc(db,user).get(1).tenDiem);
        }
    }

    @Test public void preexistingCommonEditsWinOverLegacyDuringMigration() {
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            db.execSQL("UPDATE diem_tham_quan_chung SET ten='Tên chung đã sửa',noi_dung_thuyet_minh='Bài chung đã sửa',x_ty_le=0.321,y_ty_le=0.654 WHERE dia_diem_id=0 AND id=1");
            db.execSQL("UPDATE so_do_tham_quan SET ten_anh='common_edited_floor' WHERE dia_diem_id=0 AND tang=1");
            db.execSQL("UPDATE diem_tham_quan SET ten='Tên cũ',toa_do_x=0.1,toa_do_y=0.2 WHERE id=1");
            db.setVersion(14);
        }
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getReadableDatabase(); DiemThamQuan d=KhoDuLieuThamQuan.lay(db,0,1);
            assertEquals("Tên chung đã sửa",d.ten); assertEquals("Bài chung đã sửa",d.noiDung);
            assertEquals(0.321,d.x,0.000001); assertEquals("common_edited_floor",KhoDuLieuThamQuan.anhSoDo(db,0,1));
        }
    }

    @Test public void adminDynamicPointCanBeReadNarratedRenamedAndRecordedAfterReopen() {
        long user;
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase(); user=adminFixture(helper,db);
            ContentValues place=new ContentValues(); place.put("ten","Địa điểm mới"); place.put("vi_do",10.0); place.put("kinh_do",106.0);
            assertTrue(KhoDuLieuThamQuan.luuDiaDiem(context,db,42,place));
            ContentValues point=new ContentValues(); point.put("ten","Điểm 88"); point.put("noi_dung_thuyet_minh","Nội dung sẽ được phát"); point.put("tang",7);
            ContentValues invalid=new ContentValues(point); invalid.put("x_ty_le",0.5);
            assertFalse(KhoDuLieuThamQuan.luuDiem(context,db,42,99,invalid));
            assertFalse(KhoDuLieuThamQuan.cacTang(db,42).containsKey(7));
            assertTrue(KhoDuLieuThamQuan.luuDiem(context,db,42,88,point));
            assertTrue(KhoDuLieuThamQuan.luuCauHinhAuto(context,db,42,88,true,10.1,106.1,35,"Khảo sát GPS"));
            assertEquals(88,KhoToaDoThamQuan.doc(db,42).get(0).id);
            assertFalse(KhoDuLieuThamQuan.lay(db,42,88).coMarker());
            assertTrue(KhoLichSu.ghi(db,user,42,88,KhoLichSu.MANUAL,200,"dynamic-point"));
            assertTrue(KhoLichSu.ghi(db,user,42,88,KhoLichSu.AUTO_SUB,201,"dynamic-auto"));
            point.clear(); point.put("ten","Tên sau đổi");
            assertTrue(KhoDuLieuThamQuan.luuDiem(context,db,42,88,point));
            assertFalse(KhoDuLieuThamQuan.xoaDiem(context,db,42,88));
            assertFalse(KhoDuLieuThamQuan.xoaDiaDiem(context,db,42));
        }
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getReadableDatabase();
            assertEquals("Tên sau đổi",KhoNoiDungThamQuan.lay(db,42,88).ten);
            assertEquals("Nội dung sẽ được phát",KhoNoiDungThamQuan.lay(db,42,88).noiDung());
            assertEquals(1,KhoNoiDungThamQuan.lay(db,42,0).cacDiem.size());
            assertEquals("",KhoDuLieuThamQuan.anhSoDo(db,42,7));
            assertEquals("Tên sau đổi",KhoLichSu.doc(db,user).get(0).tenDiem);
            assertEquals(88,KhoLichSu.doc(db,user).get(0).diem);
            assertEquals(2,KhoLichSu.doc(db,user).size());
        }
    }

    @Test public void failedUpgradeRollsBackVersionAndImportedRowsThenCanRetry() {
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            db.execSQL("INSERT INTO diem_tham_quan(id,khu_vuc_id,ten,mo_ta) VALUES(91,1,'Điểm chờ nhập','Bản gốc vẫn còn')");
            db.execSQL("CREATE TRIGGER fail_import BEFORE INSERT ON diem_tham_quan_chung WHEN NEW.id=91 BEGIN SELECT RAISE(ABORT,'migration test failure'); END");
            db.setVersion(14);
        }
        try(AppDatabase helper=new AppDatabase(context)) {
            try { helper.getWritableDatabase(); fail("Migration phải rollback khi nhập thiếu dữ liệu"); }
            catch(IllegalStateException expected) { assertTrue(expected.getMessage().contains("91")); }
        }
        try(SQLiteDatabase db=context.openOrCreateDatabase(AppDatabase.DB,Context.MODE_PRIVATE,null)) {
            assertEquals(14,db.getVersion());
            assertEquals(0,count(db,"SELECT COUNT(*) FROM diem_tham_quan_chung WHERE id=91"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM diem_tham_quan WHERE id=91 AND mo_ta='Bản gốc vẫn còn'"));
            db.execSQL("DROP TRIGGER fail_import");
        }
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getReadableDatabase();
            assertEquals(15,db.getVersion()); assertEquals("Điểm chờ nhập",KhoDuLieuThamQuan.lay(db,0,91).ten);
        }
    }

    @Test public void userCannotInvokeAdministrativeWritesOrViewOtherAccountDetails() {
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            assertTrue(helper.dangKy("normal_user","password123"));
            long id=helper.dangNhap("normal_user","password123"); PhienDangNhap.luu(context,id,"normal_user");
            assertEquals(HoSoTaiKhoan.USER,helper.layHoSo(id).vaiTro); assertTrue(helper.layHoSo(id).ngayTao>0);
            assertTrue(helper.dangKy("other_user","password123"));
            long other=helper.dangNhap("other_user","password123");
            assertNull(helper.layHoSo(other));
            assertFalse(helper.capNhatHoSo(other,"Unauthorized","wrong@example.com"));
            ContentValues v=new ContentValues(); v.put("ten","Unauthorized");
            denied(()->KhoDuLieuThamQuan.luuDiem(context,db,0,1,v));
            denied(()->KhoDuLieuThamQuan.luuDiaDiem(context,db,0,v));
            denied(()->KhoDuLieuThamQuan.luuSoDo(context,db,0,1,"Unauthorized",""));
            denied(()->KhoDuLieuThamQuan.luuThuyetMinh(context,db,0,1,"Unauthorized"));
            denied(()->KhoDuLieuThamQuan.luuCauHinhAuto(context,db,0,1,true,10,106,30,"test"));
            denied(()->KhoDuLieuThamQuan.xoaDiem(context,db,0,1));
            denied(()->KhoDuLieuThamQuan.xoaDiaDiem(context,db,0));
            denied(()->KhoDuLieuThamQuan.xoaSoDo(context,db,0,1));
            denied(()->helper.danhSachTaiKhoan("")); denied(()->helper.chiTietTaiKhoan(id));
            denied(()->helper.datKhoaTaiKhoan(id,true));
            assertNotEquals("Unauthorized",KhoDuLieuThamQuan.lay(db,0,1).ten);
        }
    }

    @Test public void lockInvalidatesExistingSessionAndUnlockRestoresPasswordLogin() {
        try(AppDatabase helper=new AppDatabase(context)) {
            SQLiteDatabase db=helper.getWritableDatabase(); long admin=adminFixture(helper,db);
            assertTrue(helper.dangKy("lock_target","password123","recovery-code"));
            long target=helper.dangNhap("lock_target","password123");
            assertEquals(1,helper.danhSachTaiKhoan("lock_target").size());
            assertEquals(target,helper.chiTietTaiKhoan(target).id);
            assertTrue(helper.datKhoaTaiKhoan(target,true)); assertEquals(-1,helper.dangNhap("lock_target","password123"));
            // Mô phỏng phiên đã lưu trước khi admin khóa ở một lần mở ứng dụng khác.
            context.getSharedPreferences("phien_dang_nhap",Context.MODE_PRIVATE).edit().putLong("id",target).commit();
            assertEquals(-1,PhienDangNhap.id(context));
            assertFalse(KhoLichSu.ghi(db,target,0,1,KhoLichSu.MANUAL,300,"locked"));
            PhienDangNhap.luu(context,admin,"admin_fixture");
            assertTrue(helper.datKhoaTaiKhoan(target,false));
            assertEquals(target,helper.dangNhap("lock_target","password123"));
            assertNotNull(helper.doiMatKhauBangMa("lock_target","recovery-code","changed-password"));
        }
    }

    private long adminFixture(AppDatabase helper,SQLiteDatabase db) {
        assertTrue(helper.dangKy("admin_fixture","password123")); long id=helper.dangNhap("admin_fixture","password123");
        // Chỉ fixture kiểm thử cấp quyền qua SQL. Ứng dụng không cung cấp mật khẩu/admin mặc định.
        db.execSQL("UPDATE tai_khoan SET vai_tro='ADMIN' WHERE id=?",new Object[]{id});
        PhienDangNhap.luu(context,id,"admin_fixture"); return id;
    }
    private static void denied(Runnable action) {
        try { action.run(); fail("USER không được thực hiện thao tác quản trị"); }
        catch(SecurityException expected) { assertNotNull(expected.getMessage()); }
    }
    private static int count(SQLiteDatabase db,String sql) { try(Cursor c=db.rawQuery(sql,null)) { c.moveToFirst(); return c.getInt(0); } }
    private static String text(SQLiteDatabase db,String sql) { try(Cursor c=db.rawQuery(sql,null)) { c.moveToFirst(); return c.getString(0); } }
}
