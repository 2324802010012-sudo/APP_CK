package com.example.app_ck.data.seed;

import com.example.app_ck.data.model.DiemThamQuan;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.example.app_ck.data.repository.KhoToaDoThamQuan;

/** Seed chỉ được dùng trong giao dịch tạo/nâng cấp database, không chạy khi mở màn hình. */
public final class KhoiTaoDuLieu {
    private KhoiTaoDuLieu() {}
    public static void taoBangDuLich(SQLiteDatabase db) {

        // BANG DIA DIEM
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS dia_diem (" +
                        "id INTEGER PRIMARY KEY, " +
                        "ten TEXT NOT NULL, " +
                        "dia_chi TEXT, " +
                        "mo_ta TEXT, " +
                        "danh_muc TEXT, " +
                        "vi_do REAL NOT NULL, " +
                        "kinh_do REAL NOT NULL, " +
                        "ten_anh TEXT" +
                        ")"
        );

        // BANG KHU VUC THAM QUAN
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS khu_vuc (" +
                        "id INTEGER PRIMARY KEY, " +
                        "dia_diem_id INTEGER NOT NULL, " +
                        "ten TEXT NOT NULL, " +
                        "tang INTEGER, " +
                        "ten_anh_so_do TEXT, " +
                        "FOREIGN KEY(dia_diem_id) " +
                        "REFERENCES dia_diem(id)" +
                        ")"
        );

        // BANG MARKER THAM QUAN
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS diem_tham_quan (" +
                        "id INTEGER PRIMARY KEY, " +
                        "khu_vuc_id INTEGER NOT NULL, " +
                        "ten TEXT NOT NULL, " +
                        "mo_ta TEXT, " +
                        "toa_do_x REAL, " +
                        "toa_do_y REAL, " +
                        "ten_anh TEXT, " +
                        "FOREIGN KEY(khu_vuc_id) " +
                        "REFERENCES khu_vuc(id)" +
                        ")"
        );

        // BANG NOI DUNG THUYET MINH
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS thuyet_minh (" +
                        "id INTEGER PRIMARY KEY, " +
                        "diem_tham_quan_id INTEGER NOT NULL UNIQUE, " +
                        "noi_dung TEXT NOT NULL, " +
                        "FOREIGN KEY(diem_tham_quan_id) " +
                        "REFERENCES diem_tham_quan(id)" +
                        ")"
        );
    }

    public static void themDuLieuDiaDiem(
            SQLiteDatabase db
    ) {

        themDiaDiem(
                db,
                0,
                "Dinh Độc Lập",
                "135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh",
                10.7770,
                106.6953,
                "anh_dinh_doc_lap"
        );

        themDiaDiem(
                db,
                1,
                "Bảo tàng Chứng tích Chiến tranh",
                "28 Võ Văn Tần, TP. Hồ Chí Minh",
                10.7795,
                106.6921,
                "anh_bao_tang_chung_tich"
        );


        // ID 2: Đại Nội Huế.
        themDiaDiem(
                db, 2, "Đại Nội Huế",
                "Đường 23 Tháng 8, Huế",
                16.4698, 107.5799,
                "anh_dai_noi_hue"
        );

        // ID 3: Hệ thống Nhà tù Côn Đảo.
        themDiaDiem(
                db,
                3,
                "Hệ thống Nhà tù Côn Đảo",
                "Côn Đảo, TP. Hồ Chí Minh",
                8.68907,
                106.61484,
                "anh_nha_tu_con_dao"
        );

        // ID 4: Văn Miếu – Quốc Tử Giám.
        themDiaDiem(
                db,
                4,
                "Văn Miếu – Quốc Tử Giám",
                "58 Quốc Tử Giám, Hà Nội",
                21.02930,
                105.83575,
                "anh_van_mieu"
        );

    }

    public static void themDiaDiem(
            SQLiteDatabase db,
            int id,
            String ten,
            String diaChi,
            double viDo,
            double kinhDo,
            String tenAnh
    ) {

        ContentValues cv = new ContentValues();

        cv.put("id", id);
        cv.put("ten", ten);
        cv.put("dia_chi", diaChi);
        cv.put("vi_do", KhoToaDoThamQuan.TOA_DO[id][0]);
        cv.put("kinh_do", KhoToaDoThamQuan.TOA_DO[id][1]);
        cv.put("ten_anh", tenAnh);
        cv.put("mo_ta", DuLieuNoiDungThamQuan.lay(id, 0).noiDung());
        String[] danhMuc = {"Văn hóa – lịch sử", "Bảo tàng – lịch sử", "Di sản – kiến trúc", "Di tích – lịch sử", "Văn hóa – kiến trúc"};
        cv.put("danh_muc", danhMuc[id]);

        db.insertWithOnConflict(
                "dia_diem",
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public static void themDuLieuSoDoDinhDocLap(
            SQLiteDatabase db
    ) {

        // 1. THEM 6 KHU VUC
        for (int tang = 1; tang <= 6; tang++) {

            ContentValues khuVuc = new ContentValues();

            khuVuc.put("id", tang);

            // DIA DIEM ID = 0 LA DINH DOC LAP
            khuVuc.put("dia_diem_id", 0);

            khuVuc.put(
                    "ten",
                    DuLieuDinhDocLap.layTenTang(tang)
            );

            khuVuc.put("tang", tang);

            khuVuc.put(
                    "ten_anh_so_do",
                    DuLieuDinhDocLap.layAnhSoDo(tang)
            );

            db.insertWithOnConflict(
                    "khu_vuc",
                    null,
                    khuVuc,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
        }

        // 2. THEM 25 DIEM THAM QUAN
        for (
                DuLieuDinhDocLap.DiemThamQuan diem
                : DuLieuDinhDocLap.layTatCaDiem()
        ) {

            ContentValues giaTri = new ContentValues();

            // GIU NGUYEN ID MARKER 1 - 25
            giaTri.put("id", diem.id);

            // ID KHU VUC TRUNG VOI MA TANG
            giaTri.put("khu_vuc_id", diem.tang);

            giaTri.put("ten", diem.ten);

            giaTri.put("mo_ta", diem.moTa);

            // TOA DO TUONG DOI TREN ANH SO DO
            giaTri.put("toa_do_x", (double) diem.x);

            giaTri.put("toa_do_y", (double) diem.y);

            giaTri.put("ten_anh", diem.hinhAnh);

            db.insertWithOnConflict(
                    "diem_tham_quan",
                    null,
                    giaTri,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
        }
    }

    public static void themThuyetMinhDinhDocLap(
            SQLiteDatabase db
    ) {

        for (int id = 1; id <= 25; id++) {

            // LAY NOI DUNG GOC HIEN DANG SU DUNG
            String noiDung =
                    DuLieuNoiDungThamQuan.lay(0, id).noiDung();

            ContentValues giaTri =
                    new ContentValues();

            giaTri.put(
                    "diem_tham_quan_id",
                    id
            );

            giaTri.put(
                    "noi_dung",
                    noiDung
            );

            db.insertWithOnConflict(
                    "thuyet_minh",
                    null,
                    giaTri,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
        }
    }


}
