package com.example.app_ck;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.seed.DanhMucAnhMacDinh;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AnhMacDinhTest {

    private Context context;

    @Before
    public void taoContextTest() {

        Context app = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        context = new ContextWrapper(app) {

            @Override
            public File getDatabasePath(String name) {

                File folder = new File(
                        getCacheDir(),
                        "anh-mac-dinh-tests"
                );

                if (!folder.isDirectory()
                        && !folder.mkdirs()) {
                    throw new IllegalStateException(
                            "Cannot create test DB directory"
                    );
                }

                return new File(folder, name);
            }

            @Override
            public SQLiteDatabase openOrCreateDatabase(
                    String name,
                    int mode,
                    SQLiteDatabase.CursorFactory factory
            ) {
                return openOrCreateDatabase(
                        name, mode, factory, null
                );
            }

            @Override
            public SQLiteDatabase openOrCreateDatabase(
                    String name,
                    int mode,
                    SQLiteDatabase.CursorFactory factory,
                    DatabaseErrorHandler handler
            ) {
                return SQLiteDatabase.openDatabase(
                        getDatabasePath(name).getPath(),
                        factory,
                        SQLiteDatabase.CREATE_IF_NECESSARY,
                        handler
                );
            }

            @Override
            public boolean deleteDatabase(String name) {
                return SQLiteDatabase.deleteDatabase(
                        getDatabasePath(name)
                );
            }
        };

        context.deleteDatabase(AppDatabase.DB);
    }

    @After
    public void xoaDatabaseTest() {
        context.deleteDatabase(AppDatabase.DB);
    }

    private String layAnh(
            SQLiteDatabase db,
            int idDiaDiem,
            int idDiem
    ) {

        try (Cursor cursor = db.rawQuery(
                "SELECT ten_anh "
                        + "FROM diem_tham_quan_chung "
                        + "WHERE dia_diem_id=? AND id=?",
                new String[]{
                        String.valueOf(idDiaDiem),
                        String.valueOf(idDiem)
                }
        )) {

            assertTrue(cursor.moveToFirst());

            return cursor.getString(0);
        }
    }

    @Test
    public void kiemTra83AnhDrawable() {

        assertEquals(
                83,
                DanhMucAnhMacDinh.tongSoAnh()
        );

        for (int idDiaDiem = 0; idDiaDiem < 5; idDiaDiem++) {

            for (int idDiem = 1;
                 idDiem <= DanhMucAnhMacDinh.soDiem(idDiaDiem);
                 idDiem++) {

                String tenAnh = DanhMucAnhMacDinh.lay(
                        idDiaDiem,
                        idDiem
                );

                assertFalse(tenAnh.isEmpty());

                int idAnh = context.getResources().getIdentifier(
                        tenAnh,
                        "drawable",
                        context.getPackageName()
                );

                assertTrue(
                        "Missing drawable: " + tenAnh,
                        idAnh != 0
                );
            }
        }
    }

    @Test
    public void kiemTraDuLieuAnhKhoiTao() {

        try (AppDatabase helper = new AppDatabase(context)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            for (int idDiaDiem = 0; idDiaDiem < 5; idDiaDiem++) {

                for (int idDiem = 1;
                     idDiem <= DanhMucAnhMacDinh.soDiem(idDiaDiem);
                     idDiem++) {

                    assertEquals(
                            DanhMucAnhMacDinh.lay(
                                    idDiaDiem,
                                    idDiem
                            ),
                            layAnh(db, idDiaDiem, idDiem)
                    );
                }
            }
        }
    }

    @Test
    public void kiemTraNangCapDatabase17Len18() {

        try (AppDatabase helper = new AppDatabase(context)) {

            SQLiteDatabase db = helper.getWritableDatabase();

            db.execSQL(
                    "UPDATE diem_tham_quan_chung "
                            + "SET ten_anh='' "
                            + "WHERE dia_diem_id=2 AND id=1"
            );

            db.execSQL(
                    "UPDATE diem_tham_quan_chung "
                            + "SET ten_anh='anh_may:admin.jpg' "
                            + "WHERE dia_diem_id=2 AND id=2"
            );

            db.execSQL(
                    "UPDATE diem_tham_quan_chung "
                            + "SET ten_anh='khong_anh' "
                            + "WHERE dia_diem_id=2 AND id=3"
            );

            db.setVersion(17);
        }

        try (AppDatabase helper = new AppDatabase(context)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            assertEquals(18, db.getVersion());

            // Anh thieu duoc bo sung.
            assertEquals(
                    "hue_ngo_mon",
                    layAnh(db, 2, 1)
            );

            // Anh Admin duoc giu nguyen.
            assertEquals(
                    "anh_may:admin.jpg",
                    layAnh(db, 2, 2)
            );

            // Trang thai bo anh khong bi ghi de.
            assertEquals(
                    "khong_anh",
                    layAnh(db, 2, 3)
            );
        }
    }
}