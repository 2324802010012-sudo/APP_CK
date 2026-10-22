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
    public void freshInstallContainsOnlyTwoActivePlaces() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            assertEquals(7, db.getVersion());
            assertEquals(2, count(db, "SELECT COUNT(*) FROM dia_diem"));
            assertEquals(2, count(db, "SELECT COUNT(*) FROM dia_diem WHERE id IN (0,1)"));
            assertEquals(25, count(db, "SELECT COUNT(*) FROM diem_tham_quan"));
        }
    }

    @Test
    public void upgradeFromSixRemovesRetiredPlaceAndOnlyItsRelatedData() {
        try (TaiKhoanDb helper = new TaiKhoanDb(testContext)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam) VALUES(1,'tester','salt','hash')");
            db.execSQL("INSERT INTO dia_diem(id,ten,vi_do,kinh_do) VALUES(2,'Retired',0,0)");
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
            assertEquals(7, db.getVersion());
            assertEquals(2, count(db, "SELECT COUNT(*) FROM dia_diem"));
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
            assertEquals(7, db.getVersion());
            assertEquals(2, count(db, "SELECT COUNT(*) FROM dia_diem WHERE danh_muc IS NOT NULL"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM tai_khoan WHERE id=1"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM yeu_thich WHERE tai_khoan_id=1 AND dia_diem_id=0"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM yeu_thich"));
        }
    }

    private int count(SQLiteDatabase db, String sql) {
        try (Cursor cursor = db.rawQuery(sql, null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }
}
