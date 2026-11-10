package com.example.app_ck;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteConstraintException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.local.NangCapDatabase;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoNguonThongTin;
import com.example.app_ck.session.PhienDangNhap;
import java.io.File;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** DB và phiên riêng; không xóa dữ liệu ứng dụng để kiểm thử migration. */
@RunWith(AndroidJUnit4.class)
public class SQLiteToanVenTest {
    private Context context;
    @Before public void setup() {
        Context target=InstrumentationRegistry.getInstrumentation().getTargetContext();
        context=new ContextWrapper(target) {
            @Override public File getDatabasePath(String name) {
                File dir=new File(getCacheDir(),"sqlite-v16-tests");
                if(!dir.isDirectory() && !dir.mkdirs()) throw new IllegalStateException("Test directory");
                return new File(dir,name);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory f) {
                return openOrCreateDatabase(name,mode,f,null);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory f,DatabaseErrorHandler h) {
                return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(),f,SQLiteDatabase.CREATE_IF_NECESSARY,h);
            }
            @Override public boolean deleteDatabase(String name) {return SQLiteDatabase.deleteDatabase(getDatabasePath(name));}
            @Override public SharedPreferences getSharedPreferences(String name,int mode) {
                return target.getSharedPreferences("sqlite-v16-test-"+name,mode);
            }
        };
        context.deleteDatabase(AppDatabase.DB);PhienDangNhap.dangXuat(context);
    }
    @After public void cleanup() {PhienDangNhap.dangXuat(context);context.deleteDatabase(AppDatabase.DB);}

    private long account(AppDatabase h,String name,boolean admin) {
        assertTrue(h.dangKy(name,"password123","recovery"));
        long id=h.dangNhap(name,"password123");
        if(admin) h.getWritableDatabase().execSQL("UPDATE tai_khoan SET vai_tro='ADMIN' WHERE id=?",new Object[]{id});
        PhienDangNhap.luu(context,id,name);return id;
    }
    private static long count(SQLiteDatabase db,String sql) {
        try(Cursor c=db.rawQuery(sql,null)){assertTrue(c.moveToFirst());return c.getLong(0);}
    }
    private static void rejected(Runnable r) {
        try{r.run();fail("Constraint must reject invalid reference");}catch(SQLiteConstraintException expected) { }
    }
    private ContentValues place(String ten) {
        ContentValues v=new ContentValues();v.put("ten",ten);v.put("vi_do",10d);v.put("kinh_do",106d);return v;
    }

    @Test public void freshAndReopenEnforceAllReferences() {
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();long id=account(h,"owner",false);
            assertEquals(1,count(db,"PRAGMA foreign_keys"));
            rejected(()->db.execSQL("INSERT INTO yeu_thich VALUES(?,999)",new Object[]{id}));
            rejected(()->db.execSQL("INSERT INTO yeu_thich VALUES(999,0)"));
            rejected(()->db.execSQL("INSERT INTO ghi_chu_nguon VALUES(0,999,'x','today')"));
            rejected(()->db.execSQL("INSERT INTO bai_nguon VALUES(0,999,'missing',1)"));
            assertTrue(KhoLichSu.ghi(context,db,id,0,1,KhoLichSu.MANUAL,100,"listen"));
            rejected(()->db.execSQL("DELETE FROM diem_tham_quan_chung WHERE dia_diem_id=0 AND id=1"));
            NangCapDatabase.kiemTraKhoaNgoai(db);
        }
        try(AppDatabase h=new AppDatabase(context)) {assertEquals(1,count(h.getWritableDatabase(),"PRAGMA foreign_keys"));}
    }

    @Test public void sourcesUpdateWithoutReplaceAndFailureRollsBackWholeEdit() {
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();account(h,"admin",true);
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,true,42,0,place("First"),"Source","https://example.com/a"));
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,true,42,0,place("Second"),"Edited","https://example.com/b"));
            assertEquals("Edited",KhoNguonThongTin.danhSach(db,42,0).get(0).ten);
            db.execSQL("CREATE TRIGGER fail_source BEFORE UPDATE ON nguon_thong_tin BEGIN SELECT RAISE(ABORT,'test'); END");
            assertFalse(KhoDuLieuThamQuan.luuKemNguon(context,db,true,42,0,place("Must rollback"),"Failure","https://example.com/c"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM dia_diem WHERE id=42 AND ten='Second'"));
            assertEquals("Edited",KhoNguonThongTin.danhSach(db,42,0).get(0).ten);
            db.execSQL("DROP TRIGGER fail_source");
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,true,42,0,place("Third"),"",""));
            assertTrue(KhoNguonThongTin.danhSach(db,42,0).isEmpty());
            assertTrue(KhoDuLieuThamQuan.xoaDiaDiem(context,db,42));
            NangCapDatabase.kiemTraKhoaNgoai(db);
        }
    }

    @Test public void personalDataCannotCrossSessionsOrLockedAccount() {
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();long a=account(h,"a",false);
            assertTrue(h.datYeuThich(a,0,true));
            assertTrue(KhoLichSu.ghi(context,db,a,0,1,KhoLichSu.MANUAL,100,"a"));
            long b=account(h,"b",false);
            assertFalse(h.datYeuThich(a,0,false));assertFalse(h.daYeuThich(a,0));
            assertTrue(h.danhSachYeuThich(a).isEmpty());assertTrue(KhoLichSu.doc(context,db,a).isEmpty());
            assertFalse(KhoLichSu.ghi(context,db,a,0,1,KhoLichSu.MANUAL,101,"forged"));
            try{KhoLichSu.xoa(context,db,a,null);fail();}catch(SecurityException expected) { }
            assertTrue(h.datYeuThich(b,1,true));
            db.execSQL("UPDATE tai_khoan SET trang_thai='LOCKED' WHERE id=?",new Object[]{b});
            assertFalse(h.datYeuThich(b,2,true));
            PhienDangNhap.luu(context,a,"a");assertTrue(h.daYeuThich(a,0));
            assertEquals(1,KhoLichSu.doc(context,db,a).size());
        }
    }

    @Test public void upgrade15PreservesEditsAndDeletionAndArchivesOrphans() {
        long user;
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();user=account(h,"old",false);
            assertTrue(h.datYeuThich(user,0,true));
            assertTrue(KhoLichSu.ghi(context,db,user,0,1,KhoLichSu.MANUAL,100,"valid"));
            db.execSQL("UPDATE dia_diem SET ten='Custom name' WHERE id=0");
            db.execSQL("DELETE FROM diem_tham_quan_chung WHERE dia_diem_id=4 AND id=16");
            db.setForeignKeyConstraintsEnabled(false);
            db.execSQL("INSERT INTO yeu_thich VALUES(?,999)",new Object[]{user});
            db.execSQL("INSERT INTO lich_su_nghe(tai_khoan_id,dia_diem_id,diem_id,ten_dia_diem,ten_diem,che_do,thoi_gian,ma_luot) "
                    + "VALUES(?,0,999,'Old','Missing','MANUAL',101,'orphan')",new Object[]{user});
            db.setVersion(15);
        }
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();
            assertEquals(AppDatabase.VERSION,db.getVersion());assertEquals(1,count(db,"PRAGMA foreign_keys"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM dia_diem WHERE id=0 AND ten='Custom name'"));
            assertNull(KhoDuLieuThamQuan.lay(db,4,16));
            assertEquals(user,h.dangNhap("old","password123"));
            assertTrue(h.daYeuThich(user,0));assertEquals(1,KhoLichSu.doc(context,db,user).size());
            assertEquals(2,count(db,"SELECT COUNT(*) FROM du_lieu_cach_ly"));
            NangCapDatabase.kiemTraKhoaNgoai(db);
        }
    }

    @Test public void cannotDeleteReferencedPointThenCanDeleteAfterHistoryCleared() {
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();long admin=account(h,"admin",true);
            assertTrue(KhoDuLieuThamQuan.luuDiaDiem(context,db,50,place("New")));
            ContentValues p=new ContentValues();p.put("ten","Point");p.put("tang",7);
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,false,50,12,p,"Source","https://example.com"));
            assertTrue(KhoLichSu.ghi(context,db,admin,50,12,KhoLichSu.MANUAL,1,"point"));
            assertFalse(KhoDuLieuThamQuan.xoaDiem(context,db,50,12));
            KhoLichSu.xoa(context,db,admin,null);
            assertTrue(KhoDuLieuThamQuan.xoaDiem(context,db,50,12));
            assertTrue(KhoNguonThongTin.danhSach(db,50,12).isEmpty());
            assertTrue(KhoDuLieuThamQuan.xoaDiaDiem(context,db,50));
            NangCapDatabase.kiemTraKhoaNgoai(db);
        }
    }

    @Test public void adminSetupRequiresPasswordAndCannotBeRepeatedEvenIfAdminLocked() {
        try(AppDatabase h=new AppDatabase(context)) {
            long a=account(h,"first",false);
            assertFalse(h.daCoQuanTri());assertFalse(h.khoiTaoQuanTri("first","wrong"));
            assertTrue(h.khoiTaoQuanTri("first","password123"));
            assertTrue(h.daCoQuanTri());assertTrue(PhienDangNhap.laAdmin(context));
            account(h,"second",false);
            assertFalse(h.khoiTaoQuanTri("second","password123"));
            h.getWritableDatabase().execSQL("UPDATE tai_khoan SET trang_thai='LOCKED' WHERE id=?",new Object[]{a});
            assertTrue(h.daCoQuanTri());assertFalse(h.khoiTaoQuanTri("second","password123"));
        }
    }

    @Test public void generatedIdsStayAboveSeedAndAreNotReusedAfterDeletion() {
        try(AppDatabase h=new AppDatabase(context)) {
            SQLiteDatabase db=h.getWritableDatabase();account(h,"admin",true);
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,true,-1,0,place("Generated"),"",""));
            long id=count(db,"SELECT id FROM dia_diem WHERE ten='Generated'");assertTrue(id>=5);
            ContentValues p=new ContentValues();p.put("ten","Generated point");
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,false,(int)id,-1,p,"",""));
            int point=(int)count(db,"SELECT id FROM diem_tham_quan_chung WHERE dia_diem_id="+id);
            assertTrue(KhoDuLieuThamQuan.xoaDiem(context,db,(int)id,point));
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,false,(int)id,-1,p,"",""));
            assertTrue(count(db,"SELECT id FROM diem_tham_quan_chung WHERE dia_diem_id="+id)>point);
            assertTrue(KhoDuLieuThamQuan.xoaDiem(context,db,(int)id,point+1));
            assertTrue(KhoDuLieuThamQuan.xoaDiaDiem(context,db,(int)id));
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,db,true,-1,0,place("Next"),"",""));
            assertTrue(count(db,"SELECT id FROM dia_diem WHERE ten='Next'")>id);
        }
    }
}
