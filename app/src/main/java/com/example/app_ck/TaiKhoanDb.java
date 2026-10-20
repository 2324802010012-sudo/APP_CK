package com.example.app_ck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class TaiKhoanDb extends SQLiteOpenHelper {
    private static final String DB = "du_lich_tai_khoan.db";
    private static final int VERSION = 6;
    public TaiKhoanDb(Context context) { super(context, DB, null, VERSION); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tai_khoan (id INTEGER PRIMARY KEY AUTOINCREMENT, ten_dang_nhap TEXT NOT NULL UNIQUE, muoi TEXT NOT NULL, mat_khau_bam TEXT NOT NULL, ma_khoi_phuc_bam TEXT)");
        db.execSQL("CREATE TABLE yeu_thich (tai_khoan_id INTEGER NOT NULL, dia_diem_id INTEGER NOT NULL, PRIMARY KEY(tai_khoan_id,dia_diem_id), FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id))");
        taoBangDuLich(db);
        themDuLieuDiaDiem(db);
        themDuLieuSoDoDinhDocLap(db);
        themThuyetMinhDinhDocLap(db);
        themMoTaVaDanhMuc(db);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        // NANG CAP TU PHIEN BAN 1 LEN 2
        if (oldVersion < 2) {

            db.execSQL(
                    "ALTER TABLE tai_khoan " +
                            "ADD COLUMN ma_khoi_phuc_bam TEXT"
            );
        }

        // NANG CAP TU PHIEN BAN 2 LEN 3
        if (oldVersion < 3) {

            taoBangDuLich(db);

            themDuLieuDiaDiem(db);
        }

// NANG CAP TU PHIEN BAN 3 LEN 4
        if (oldVersion < 4) {

            // BAO DAM CAC BANG DU LICH DA TON TAI
            taoBangDuLich(db);

            // THEM 6 KHU VUC VA 25 MARKER
            themDuLieuSoDoDinhDocLap(db);
        }

// NANG CAP SQLITE LEN PHIEN BAN 5
        if (oldVersion < 5) {

            // BAO DAM BANG THUYET MINH DA TON TAI
            taoBangDuLich(db);

            // LUU NOI DUNG THUYET MINH
            themThuyetMinhDinhDocLap(db);
        }

// NANG CAP TU PHIEN BAN 5 LEN 6
        if (oldVersion < 6) {

            // BO SUNG DANH MUC CHO DATABASE CU
            db.execSQL(
                    "ALTER TABLE dia_diem " +
                            "ADD COLUMN danh_muc TEXT"
            );

            // BO SUNG MO TA VA DANH MUC
            themMoTaVaDanhMuc(db);
        }

    }

    // TAO CAC BANG DU LIEU DU LICH
    private void taoBangDuLich(SQLiteDatabase db) {

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

    // THEM 3 DIA DIEM BAN DAU
    private void themDuLieuDiaDiem(
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

        themDiaDiem(
                db,
                2,
                "Bưu điện Trung tâm Sài Gòn",
                "2 Công xã Paris, TP. Hồ Chí Minh",
                10.7799,
                106.6999,
                "anh_buu_dien_sai_gon"
        );
    }

    // HAM HO TRO THEM DIA DIEM
    private void themDiaDiem(
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
        cv.put("vi_do", viDo);
        cv.put("kinh_do", kinhDo);
        cv.put("ten_anh", tenAnh);

        db.insertWithOnConflict(
                "dia_diem",
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    private String chuanHoa(String ten) { return ten.trim().toLowerCase(Locale.ROOT); }
    private String bam(String input, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(input.toCharArray(), salt, 120000, 256);
        try { return Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded(), Base64.NO_WRAP); }
        finally { spec.clearPassword(); }
    }
    private byte[] muoiMoi() { byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt); return salt; }
    private String bamMa(String code, byte[] salt) throws Exception { return bam(code.trim().toUpperCase(Locale.ROOT), salt); }
    private boolean bangNhau(String a, String b) {
        byte[] x = Base64.decode(a, Base64.NO_WRAP), y = Base64.decode(b, Base64.NO_WRAP);
        if (x.length != y.length) return false;
        int diff = 0; for (int i=0; i<x.length; i++) diff |= x[i] ^ y[i];
        return diff == 0;
    }
    public static String taoMaKhoiPhuc() {
        byte[] b = new byte[12]; new SecureRandom().nextBytes(b);
        return Base64.encodeToString(b, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }
    // Dung phuong thuc 3 tham so khi dang ky moi.
    public boolean dangKy(String ten, String matKhau, String maKhoiPhuc) {
        try {
            byte[] salt = muoiMoi();
            ContentValues cv = new ContentValues();
            cv.put("ten_dang_nhap", chuanHoa(ten));
            cv.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhau, salt));
            cv.put("ma_khoi_phuc_bam", bamMa(maKhoiPhuc, salt));
            return getWritableDatabase().insert("tai_khoan", null, cv) != -1;
        } catch (Exception ex) { return false; }
    }
    // Giu tuong thich code cu; tai khoan tao bang ham nay KHONG co ma khoi phuc.
    public boolean dangKy(String ten, String matKhau) {
        try {
            byte[] salt = muoiMoi(); ContentValues cv = new ContentValues();
            cv.put("ten_dang_nhap", chuanHoa(ten));
            cv.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhau, salt));
            return getWritableDatabase().insert("tai_khoan", null, cv) != -1;
        } catch (Exception ex) { return false; }
    }
    public long dangNhap(String ten, String matKhau) {
        try (Cursor c = getReadableDatabase().query("tai_khoan", new String[]{"id","muoi","mat_khau_bam"}, "ten_dang_nhap=?", new String[]{chuanHoa(ten)}, null,null,null)) {
            if (!c.moveToFirst()) return -1;
            byte[] salt = Base64.decode(c.getString(1), Base64.NO_WRAP);
            return bangNhau(bam(matKhau,salt), c.getString(2)) ? c.getLong(0) : -1;
        } catch (Exception ex) { return -1; }
    }
    public String doiMatKhauBangMa(String ten, String maKhoiPhuc, String matKhauMoi) {
        if (matKhauMoi.length() < 8 || maKhoiPhuc.trim().isEmpty()) return null;
        try (Cursor c = getReadableDatabase().query("tai_khoan", new String[]{"id","muoi","ma_khoi_phuc_bam"}, "ten_dang_nhap=?", new String[]{chuanHoa(ten)},null,null,null)) {
            if (!c.moveToFirst() || c.isNull(2)) return null;
            byte[] saltCu = Base64.decode(c.getString(1), Base64.NO_WRAP);
            if (!bangNhau(bamMa(maKhoiPhuc,saltCu), c.getString(2))) return null;
            byte[] saltMoi = muoiMoi();
            ContentValues cv = new ContentValues();
            cv.put("muoi", Base64.encodeToString(saltMoi, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhauMoi,saltMoi));
            String maMoi = taoMaKhoiPhuc();
            cv.put("ma_khoi_phuc_bam", bamMa(maMoi,saltMoi));
            return getWritableDatabase().update("tai_khoan",cv,"id=?",new String[]{String.valueOf(c.getLong(0))}) == 1 ? maMoi : null;
        } catch (Exception ex) { return null; }
    }
    public boolean capNhatMaKhoiPhuc(long id, String matKhauHienTai, String maMoi) {
        try (Cursor c = getReadableDatabase().query("tai_khoan",new String[]{"muoi","mat_khau_bam"},"id=?",new String[]{String.valueOf(id)},null,null,null)) {
            if (!c.moveToFirst()) return false;
            byte[] salt = Base64.decode(c.getString(0),Base64.NO_WRAP);
            if (!bangNhau(bam(matKhauHienTai,salt),c.getString(1))) return false;
            ContentValues cv = new ContentValues(); cv.put("ma_khoi_phuc_bam",bamMa(maMoi,salt));
            return getWritableDatabase().update("tai_khoan",cv,"id=?",new String[]{String.valueOf(id)}) == 1;
        } catch (Exception ex) { return false; }
    }
    public boolean daYeuThich(long id, int diaDiem) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM yeu_thich WHERE tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)})) { return c.moveToFirst(); }
    }
    public void datYeuThich(long id, int diaDiem, boolean thich) {
        if (thich) { ContentValues cv = new ContentValues(); cv.put("tai_khoan_id",id); cv.put("dia_diem_id",diaDiem); getWritableDatabase().insertWithOnConflict("yeu_thich",null,cv,SQLiteDatabase.CONFLICT_IGNORE); }
        else getWritableDatabase().delete("yeu_thich","tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)});
    }
    public ArrayList<Integer> danhSachYeuThich(long id) {
        ArrayList<Integer> ds = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("yeu_thich",new String[]{"dia_diem_id"},"tai_khoan_id=?",new String[]{String.valueOf(id)},null,null,"dia_diem_id ASC")) { while(c.moveToNext()) ds.add(c.getInt(0)); }
        return ds;
    }

    // LAY DANH SACH DIA DIEM TU SQLITE
    public ArrayList<DiaDiem> danhSachDiaDiem() {

        ArrayList<DiaDiem> danhSach =
                new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                "dia_diem",
                new String[]{
                        "id",
                        "ten",
                        "dia_chi",
                        "mo_ta",
                        "vi_do",
                        "kinh_do",
                        "ten_anh",
                        "danh_muc"
                },
                null,
                null,
                null,
                null,
                "id ASC"
        )) {

            while (cursor.moveToNext()) {

                DiaDiem diaDiem = new DiaDiem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5),
                        cursor.getString(6)
                );
                diaDiem.danhMuc = cursor.getString(7);
                danhSach.add(diaDiem);
            }
        }

        return danhSach;
    }

    // LUU 6 KHU VUC VA 25 MARKER DINH DOC LAP
    private void themDuLieuSoDoDinhDocLap(
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

    // DOC 25 MARKER DINH DOC LAP TU SQLITE
    public ArrayList<DuLieuDinhDocLap.DiemThamQuan>
    layDiemDinhDocLap() {

        ArrayList<DuLieuDinhDocLap.DiemThamQuan>
                danhSach = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                "diem_tham_quan",
                new String[]{
                        "id",
                        "ten",
                        "khu_vuc_id",
                        "toa_do_x",
                        "toa_do_y",
                        "mo_ta",
                        "ten_anh"
                },
                null,
                null,
                null,
                null,
                "id ASC"
        )) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(0);
                String ten = cursor.getString(1);
                int tang = cursor.getInt(2);

                float x = cursor.getFloat(3);
                float y = cursor.getFloat(4);

                String moTa = cursor.getString(5);
                String hinhAnh = cursor.getString(6);

                DuLieuDinhDocLap.DiemThamQuan diem =
                        new DuLieuDinhDocLap.DiemThamQuan(
                                id,
                                ten,
                                tang,
                                x,
                                y
                        );

                diem.moTa =
                        moTa == null ? "" : moTa;

                diem.hinhAnh =
                        hinhAnh == null ? "" : hinhAnh;

                danhSach.add(diem);
            }
        }

        return danhSach;
    }

    // LUU NOI DUNG THUYET MINH VAO SQLITE
    private void themThuyetMinhDinhDocLap(
            SQLiteDatabase db
    ) {

        for (int id = 1; id <= 25; id++) {

            // LAY NOI DUNG GOC HIEN DANG SU DUNG
            String noiDung =
                    NoiDungThuyetMinhDinhDocLap
                            .layNoiDung(id);

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
    // DOC NOI DUNG THUYET MINH THEO ID MARKER
    public String layThuyetMinhTheoId(
            int diemThamQuanId
    ) {


        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                "thuyet_minh",
                new String[]{
                        "noi_dung"
                },
                "diem_tham_quan_id=?",
                new String[]{
                        String.valueOf(diemThamQuanId)
                },
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }

        return null;
    }

    // BO SUNG MO TA VA DANH MUC CHO BA DIA DIEM
    private void themMoTaVaDanhMuc(
            SQLiteDatabase db
    ) {

        capNhatThongTinDiaDiem(
                db,
                0,
                "Văn hóa – lịch sử",
                "Dinh Độc Lập là một di tích lịch sử nổi bật, " +
                        "gắn liền với những sự kiện quan trọng trong " +
                        "lịch sử Việt Nam. Công trình có nhiều không " +
                        "gian tham quan như Phòng Khánh tiết, Phòng họp " +
                        "Hội đồng Nội các, các phòng làm việc và khu hầm. " +
                        "Du khách có thể tìm hiểu về kiến trúc và những " +
                        "câu chuyện lịch sử tại đây."
        );

        capNhatThongTinDiaDiem(
                db,
                1,
                "Bảo tàng – lịch sử",
                "Bảo tàng Chứng tích Chiến tranh trưng bày " +
                        "các tư liệu, hình ảnh và hiện vật về chiến tranh " +
                        "cùng những tác động đối với con người. Các không " +
                        "gian trưng bày được tổ chức theo nhiều chủ đề, " +
                        "giúp khách tham quan tìm hiểu lịch sử."
        );

        capNhatThongTinDiaDiem(
                db,
                2,
                "Kiến trúc – lịch sử",
                "Bưu điện Trung tâm Sài Gòn là một công trình " +
                        "kiến trúc tiêu biểu tại trung tâm Thành phố Hồ Chí Minh. " +
                        "Công trình nổi bật với mặt tiền, sảnh chính " +
                        "và những chi tiết trang trí nội thất đặc trưng."
        );
    }

    // CHI CAP NHAT TRUONG CHUA CO DU LIEU
    private void capNhatThongTinDiaDiem(
            SQLiteDatabase db,
            int id,
            String danhMuc,
            String moTa
    ) {

        db.execSQL(
                "UPDATE dia_diem " +
                        "SET danh_muc = CASE " +
                        "WHEN danh_muc IS NULL OR TRIM(danh_muc) = '' " +
                        "THEN ? ELSE danh_muc END, " +
                        "mo_ta = CASE " +
                        "WHEN mo_ta IS NULL OR TRIM(mo_ta) = '' " +
                        "THEN ? ELSE mo_ta END " +
                        "WHERE id = ?",
                new Object[]{
                        danhMuc,
                        moTa,
                        id
                }
        );
    }

}
