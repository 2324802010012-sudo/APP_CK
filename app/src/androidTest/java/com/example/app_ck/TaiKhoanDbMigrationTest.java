package com.example.app_ck;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TaiKhoanDbMigrationTest {
    private static final String DB_NAME = "du_lich_tai_khoan.db";
    private Context testContext;

    @Before
    public void setUp() {
        // Dùng thư mục cache riêng, không mở CSDL ứng dụng của người dùng.
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testContext = new ContextWrapper(target) {
            @Override
            public File getDatabasePath(String name) {
                File folder = new File(getCacheDir(), "migration-tests");
                if (!folder.isDirectory() && !folder.mkdirs()) {
                    throw new IllegalStateException("Cannot create isolated database directory");
                }
                return new File(folder, name);
            }

            @Override
            public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                    SQLiteDatabase.CursorFactory factory) {
                return openOrCreateDatabase(name, mode, factory, null);
            }

            @Override
            public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                    SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
                return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(), factory,
                        SQLiteDatabase.CREATE_IF_NECESSARY, errorHandler);
            }

            @Override
            public boolean deleteDatabase(String name) {
                return SQLiteDatabase.deleteDatabase(getDatabasePath(name));
            }
        };
        testContext.deleteDatabase(DB_NAME);
    }

    @After
    public void tearDown() {
        testContext.deleteDatabase(DB_NAME);
    }

    @Test
    public void freshInstallContainsFiveActivePlaces() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            assertEquals(14, db.getVersion());
            assertEquals(5, count(db, "SELECT COUNT(*) FROM dia_diem"));
            assertEquals(2, count(db, "SELECT COUNT(*) FROM dia_diem WHERE id IN (0,1)"));
            assertEquals(25, count(db, "SELECT COUNT(*) FROM diem_tham_quan"));
        }
    }

    @Test public void upgradeFromTwelveAddsHistoryAndKeepsExistingData() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam,ho_ten,email) "
                    + "VALUES(99,'history_migration','salt','hash','Tên cũ','cu@example.com')");
            db.execSQL("INSERT INTO yeu_thich VALUES(99,4)");
            db.execSQL("CREATE TABLE toa_do_giu_lai(id INTEGER PRIMARY KEY,x REAL,y REAL)");
            db.execSQL("INSERT INTO toa_do_giu_lai VALUES(1,0.123,0.456)");
            db.execSQL("DROP TABLE lich_su_nghe");
            db.setVersion(12);
        }
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            assertEquals(14,db.getVersion());
            assertEquals(0,count(db,"SELECT COUNT(*) FROM lich_su_nghe"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=99 AND dia_diem_id=4"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM tai_khoan WHERE id=99 AND mat_khau_bam='hash' AND ho_ten='Tên cũ'"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM toa_do_giu_lai WHERE x=0.123 AND y=0.456"));
            assertEquals(25,count(db,"SELECT COUNT(*) FROM diem_tham_quan"));
        }
    }

    @Test
    public void upgradeFromSixRemovesRetiredPlaceAndOnlyItsRelatedData() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam) VALUES(1,'tester','salt','hash')");
            db.execSQL("INSERT OR REPLACE INTO dia_diem(id,ten,vi_do,kinh_do) VALUES(2,'Retired',0,0)");
            db.execSQL("INSERT INTO khu_vuc(id,dia_diem_id,ten,tang) VALUES(70,2,'Retired area',1)");
            db.execSQL("INSERT INTO diem_tham_quan(id,khu_vuc_id,ten) VALUES(200,70,'Retired point')");
            db.execSQL("INSERT INTO thuyet_minh(diem_tham_quan_id,noi_dung) VALUES(200,'Retired narration')");
            db.execSQL("INSERT INTO yeu_thich(tai_khoan_id,dia_diem_id) VALUES(1,0),(1,1),(1,2)");
            db.execSQL("CREATE TABLE bao_tang_marker (id INTEGER PRIMARY KEY, noi_dung TEXT)");
            db.execSQL("INSERT INTO bao_tang_marker VALUES(1,'Keep museum data')");
            db.setVersion(6);
        }

        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            assertEquals(14, db.getVersion());
            assertEquals(5, count(db, "SELECT COUNT(*) FROM dia_diem"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM khu_vuc WHERE id=70"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM diem_tham_quan WHERE id=200"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM thuyet_minh WHERE diem_tham_quan_id=200"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE dia_diem_id=2"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE id=1"));
            assertEquals(2, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=1"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM khu_vuc"));
            assertEquals(25, count(db, "SELECT COUNT(*) FROM diem_tham_quan"));
            assertEquals(25, count(db, "SELECT COUNT(*) FROM thuyet_minh"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM bao_tang_marker"));
        }
    }

    @Test
    public void upgradeFromEightDeletesOnlyRemovedDestination() {
        String[] keptTables = {"bao_tang_marker", "con_dao_diem", "con_dao_toa_do",
                "van_mieu_diem", "van_mieu_toa_do"};
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam) VALUES(1,'keep_user','salt','hash')");
            db.execSQL("INSERT OR REPLACE INTO dia_diem(id,ten,vi_do,kinh_do) VALUES(2,'Removed destination',21,105)");
            db.execSQL("INSERT INTO yeu_thich VALUES(1,0),(1,1),(1,2),(1,3),(1,4)");
            db.execSQL("CREATE TABLE lang_van_hoa_diem(id INTEGER PRIMARY KEY,ten TEXT,mo_ta TEXT)");
            db.execSQL("INSERT INTO lang_van_hoa_diem VALUES(101,'Removed data','Removed narration')");
            db.execSQL("CREATE TABLE lang_van_hoa_toa_do(id INTEGER PRIMARY KEY,x_ty_le REAL,y_ty_le REAL)");
            db.execSQL("INSERT INTO lang_van_hoa_toa_do VALUES(101,0.5,0.5)");
            for (String table : keptTables) {
                db.execSQL("CREATE TABLE " + table + "(id INTEGER PRIMARY KEY, sentinel TEXT)");
                db.execSQL("INSERT INTO " + table + " VALUES(1,'keep unchanged')");
            }
            db.execSQL("UPDATE dia_diem SET ten='Keep custom name',mo_ta='Keep custom description',vi_do=8.7,kinh_do=106.6 WHERE id=3");
            db.setVersion(8);
        }
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            assertEquals(14, db.getVersion());
            assertEquals(5, count(db, "SELECT COUNT(*) FROM dia_diem"));
            assertEquals(4, count(db, "SELECT COUNT(*) FROM dia_diem WHERE id IN (0,1,3,4)"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM dia_diem WHERE id=2"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM sqlite_master WHERE name IN ('lang_van_hoa_diem','lang_van_hoa_toa_do')"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE ten_dang_nhap='keep_user' AND muoi='salt' AND mat_khau_bam='hash'"));
            assertEquals(4, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=1 AND dia_diem_id IN (0,1,3,4)"));
            assertEquals(4, count(db, "SELECT COUNT(*) FROM yeu_thich"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM dia_diem WHERE id=3 AND ten='Keep custom name' AND mo_ta='Keep custom description' AND vi_do=8.68907 AND kinh_do=106.61484"));
            for (String table : keptTables) {
                assertEquals(1, count(db, "SELECT COUNT(*) FROM " + table + " WHERE id=1 AND sentinel='keep unchanged'"));
            }
            assertEquals(25, count(db, "SELECT COUNT(*) FROM diem_tham_quan"));
            assertEquals(25, count(db, "SELECT COUNT(*) FROM thuyet_minh"));
        }
    }

    @Test
    public void upgradeFromOnePreservesAccountAndActiveFavorite() {
        verifyLegacyUpgrade(1);
    }

    @Test
    public void upgradeFromTwoDoesNotAddCategoryTwice() {
        verifyLegacyUpgrade(2);
    }

    private void verifyLegacyUpgrade(int oldVersion) {
        try (SQLiteDatabase db = testContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)) {
            db.execSQL("CREATE TABLE tai_khoan (id INTEGER PRIMARY KEY AUTOINCREMENT, ten_dang_nhap TEXT NOT NULL UNIQUE, muoi TEXT NOT NULL, mat_khau_bam TEXT NOT NULL"
                    + (oldVersion >= 2 ? ", ma_khoi_phuc_bam TEXT" : "") + ")");
            db.execSQL("CREATE TABLE yeu_thich (tai_khoan_id INTEGER NOT NULL, dia_diem_id INTEGER NOT NULL, PRIMARY KEY(tai_khoan_id,dia_diem_id))");
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam) VALUES(1,'legacy','salt','hash')");
            db.execSQL("INSERT INTO yeu_thich VALUES(1,0),(1,2)");
            db.setVersion(oldVersion);
        }
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            assertEquals(14, db.getVersion());
            assertEquals(5, count(db, "SELECT COUNT(*) FROM dia_diem WHERE danh_muc IS NOT NULL"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE id=1"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=1 AND dia_diem_id=0"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM yeu_thich"));
        }
    }

    @Test
    public void upgradeFromTenKeepsHueFavoriteAndUpdatesGpsOnly() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam) VALUES(1,'gps_user','salt','hash')");
            db.execSQL("INSERT INTO yeu_thich VALUES(1,2)");
            db.execSQL("UPDATE dia_diem SET ten='Custom Hue',mo_ta='Keep narration',vi_do=16.4698,kinh_do=107.5799 WHERE id=2");
            db.execSQL("DROP TABLE gps_diem_tham_quan");
            db.setVersion(10);
        }
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            assertEquals(14, db.getVersion());
            assertEquals(1, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=1 AND dia_diem_id=2"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE ten_dang_nhap='gps_user' AND mat_khau_bam='hash'"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM dia_diem WHERE id=2 AND ten='Custom Hue' AND mo_ta='Keep narration' AND vi_do=16.4689726 AND kinh_do=107.5781266"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM gps_diem_tham_quan"));
        }
    }

    @Test
    public void gpsPointIsScopedByDestinationAndRetainsFloorAndRadius() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            KhoToaDoThamQuan.luu(db, 0, 1, 1, 10.77702, 106.6954, 8, 30);
            KhoToaDoThamQuan.luu(db, 1, 1, 0, 10.779475, 106.692132, 6, 40);
            assertEquals(1, KhoToaDoThamQuan.doc(db, 0).size());
            assertEquals(1, KhoToaDoThamQuan.doc(db, 1).size());
            assertEquals(1, KhoToaDoThamQuan.doc(db, 0).get(0).tang);
            assertEquals(30f, KhoToaDoThamQuan.doc(db, 0).get(0).banKinh, 0);
            assertEquals(0, KhoToaDoThamQuan.doc(db, 2).size());
            assertEquals(25, count(db, "SELECT COUNT(*) FROM diem_tham_quan"));
        }
    }

    @Test
    public void upgradeFromElevenAddsProfileWithoutChangingCredentialsFavoritesOrGps() {
        // V11 đã có các bảng du lịch; giữ đầy đủ fixture để onOpen cập nhật nội dung.
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DROP TABLE lich_su_nghe");
            db.execSQL("DROP TABLE phien_ban_noi_dung");
            db.execSQL("DROP TABLE tai_khoan");
            db.execSQL("DROP TABLE yeu_thich");
            db.execSQL("DROP TABLE gps_diem_tham_quan");
        }
        // Các bảng liên quan trong schema v11, trước khi có ho_ten và email.
        try (SQLiteDatabase db = testContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)) {
            db.execSQL("CREATE TABLE tai_khoan(id INTEGER PRIMARY KEY,ten_dang_nhap TEXT,muoi TEXT,mat_khau_bam TEXT,ma_khoi_phuc_bam TEXT)");
            db.execSQL("INSERT INTO tai_khoan VALUES(1,'profile_user','keep_salt','keep_hash','keep_recovery')");
            db.execSQL("CREATE TABLE yeu_thich(tai_khoan_id INTEGER,dia_diem_id INTEGER)");
            db.execSQL("INSERT INTO yeu_thich VALUES(1,2)");
            db.execSQL("CREATE TABLE gps_diem_tham_quan(dia_diem_id INTEGER,marker_id INTEGER,tang INTEGER,vi_do REAL,kinh_do REAL,ban_kinh REAL)");
            db.execSQL("INSERT INTO gps_diem_tham_quan VALUES(2,1,-1,16.4689,107.5781,40)");
            db.setVersion(11);
        }
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            assertEquals(14, db.getVersion());
            assertEquals("profile_user", helper.layHoSo(1).tenHienThi());
            assertEquals("", helper.layHoSo(1).email);
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE muoi='keep_salt' AND mat_khau_bam='keep_hash' AND ma_khoi_phuc_bam='keep_recovery'"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=1 AND dia_diem_id=2"));
            assertEquals(1, KhoToaDoThamQuan.doc(db, 2).size());
            org.junit.Assert.assertTrue(helper.capNhatHoSo(1, "Nguyễn Minh Anh", "minhanh@example.com"));
            assertEquals("Nguyễn Minh Anh", helper.layHoSo(1).tenHienThi());
            assertEquals("minhanh@example.com", helper.layHoSo(1).email);
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE mat_khau_bam='keep_hash'"));
        }
    }

    @Test public void upgradeFromThirteenPreservesEveryHistoryRowAndConvertsModes() {
        try (TaiKhoanDb helper=new TaiKhoanDb(testContext)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam,ho_ten) VALUES(99,'history13','salt','hash','Giữ họ tên')");
            db.execSQL("INSERT INTO yeu_thich VALUES(99,4)");
            KhoToaDoThamQuan.luu(db,4,6,-1,21.02858,105.83589,5,15);
            db.execSQL("DROP TABLE lich_su_nghe");
            db.execSQL("CREATE TABLE lich_su_nghe(id INTEGER PRIMARY KEY AUTOINCREMENT,tai_khoan_id INTEGER NOT NULL,"
                    + "dia_diem_id INTEGER NOT NULL,diem_id INTEGER,ten_dia_diem TEXT NOT NULL,ten_diem TEXT NOT NULL,"
                    + "che_do TEXT NOT NULL CHECK(che_do IN ('THU_CONG','AUTO_GPS','DEMO')),thoi_gian INTEGER NOT NULL,"
                    + "ma_luot TEXT NOT NULL,UNIQUE(tai_khoan_id,ma_luot))");
            db.execSQL("CREATE INDEX lich_su_theo_tai_khoan ON lich_su_nghe(tai_khoan_id,thoi_gian DESC,id DESC)");
            db.execSQL("INSERT INTO lich_su_nghe VALUES"
                    + "(10,99,0,NULL,'Tên đã lưu','Tổng quan','THU_CONG',100,'cu-1'),"
                    + "(11,99,2,NULL,'Huế','Tổng quan','AUTO_GPS',200,'cu-2'),"
                    + "(12,99,4,6,'Văn Miếu','Khuê Văn Các','AUTO_GPS',300,'cu-3'),"
                    + "(13,99,3,1,'Côn Đảo','Phú Hải','DEMO',400,'cu-4')");
            db.setVersion(13);
        }
        try(TaiKhoanDb helper=new TaiKhoanDb(testContext)) {
            SQLiteDatabase db=helper.getWritableDatabase();
            assertEquals(14,db.getVersion());
            java.util.ArrayList<KhoLichSu.Luot> ds=KhoLichSu.doc(db,99);
            assertEquals(4,ds.size());
            assertEquals(KhoLichSu.DEMO,ds.get(0).cheDo);
            assertEquals(KhoLichSu.AUTO_SUB,ds.get(1).cheDo);
            assertEquals(KhoLichSu.AUTO_MAIN,ds.get(2).cheDo);
            assertEquals(KhoLichSu.MANUAL,ds.get(3).cheDo);
            assertEquals(10,ds.get(3).id); assertEquals(100,ds.get(3).thoiGian);
            assertEquals("Tên đã lưu",ds.get(3).tenDiaDiem);
            assertEquals(1,count(db,"SELECT COUNT(*) FROM sqlite_master WHERE type='index' AND name='lich_su_theo_tai_khoan' AND tbl_name='lich_su_nghe'"));
            assertEquals(1,count(db,"SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=99 AND dia_diem_id=4"));
            assertEquals("Giữ họ tên",helper.layHoSo(99).hoTen);
            assertEquals(1,KhoToaDoThamQuan.doc(db,4).size());
            org.junit.Assert.assertFalse(KhoLichSu.ghi(db,99,0,0,KhoLichSu.MANUAL,500,"cu-1"));
            org.junit.Assert.assertTrue(KhoLichSu.ghi(db,99,0,0,KhoLichSu.MANUAL,500,"moi"));
            org.junit.Assert.assertTrue(KhoLichSu.doc(db,99).get(0).id>13);
        }
    }

    private int count(SQLiteDatabase db, String sql) {
        try (Cursor cursor = db.rawQuery(sql, null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }
}
