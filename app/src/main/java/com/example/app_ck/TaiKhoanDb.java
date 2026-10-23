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
    private static final int VERSION = 12;
    public TaiKhoanDb(Context context) { super(context, DB, null, VERSION); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tai_khoan (id INTEGER PRIMARY KEY AUTOINCREMENT, ten_dang_nhap TEXT NOT NULL UNIQUE, muoi TEXT NOT NULL, mat_khau_bam TEXT NOT NULL, ma_khoi_phuc_bam TEXT)");
        db.execSQL("CREATE TABLE yeu_thich (tai_khoan_id INTEGER NOT NULL, dia_diem_id INTEGER NOT NULL, PRIMARY KEY(tai_khoan_id,dia_diem_id), FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id))");
        taoBangDuLich(db);
        themDuLieuDiaDiem(db);
        themDuLieuSoDoDinhDocLap(db);
        themThuyetMinhDinhDocLap(db);
        themMoTaVaDanhMuc(db);
        KhoToaDoThamQuan.nangCap(db);
        nangCapHoSo(db);
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
            boolean coDanhMuc = false;
            try (Cursor cursor = db.rawQuery("PRAGMA table_info(dia_diem)", null)) {
                int cotTen = cursor.getColumnIndexOrThrow("name");
                while (cursor.moveToNext()) {
                    if ("danh_muc".equals(cursor.getString(cotTen))) {
                        coDanhMuc = true;
                        break;
                    }
                }
            }
            if (!coDanhMuc) {
                db.execSQL("ALTER TABLE dia_diem ADD COLUMN danh_muc TEXT");
            }

            // BO SUNG MO TA VA DANH MUC
            themMoTaVaDanhMuc(db);
        }

        if (oldVersion < 7) {
            xoaDiaDiemKhongConSuDung(db);
        }

        // Phiên bản 8: bổ sung các địa điểm mới, không xóa dữ liệu cũ.
        if (oldVersion < 8) {
            taoBangDuLich(db);
            themDuLieuDiaDiem(db);
            themMoTaVaDanhMuc(db);
        }

        if (oldVersion < 9) {
            xoaDiaDiemKhongConSuDung(db);
            db.execSQL("DROP TABLE IF EXISTS lang_van_hoa_toa_do");
            db.execSQL("DROP TABLE IF EXISTS lang_van_hoa_diem");
        }

        // Phiên bản 10: tái sử dụng ID 2 cho Đại Nội Huế.
        // Giữ nguyên mọi tài khoản, yêu thích và dữ liệu của địa điểm khác.
        if (oldVersion < 10) {
            taoBangDuLich(db);
            themDuLieuDiaDiem(db);
            themMoTaVaDanhMuc(db);
        }
        if (oldVersion < 11) {
            KhoToaDoThamQuan.nangCap(db);
        }
        if (oldVersion < 12) nangCapHoSo(db);
    }


    private void nangCapHoSo(SQLiteDatabase db) {
        java.util.Set<String> cot = new java.util.HashSet<>();
        try (Cursor c = db.rawQuery("PRAGMA table_info(tai_khoan)", null)) {
            while (c.moveToNext()) cot.add(c.getString(c.getColumnIndexOrThrow("name")));
        }
        if (!cot.contains("ho_ten")) db.execSQL("ALTER TABLE tai_khoan ADD COLUMN ho_ten TEXT NOT NULL DEFAULT ''");
        if (!cot.contains("email")) db.execSQL("ALTER TABLE tai_khoan ADD COLUMN email TEXT NOT NULL DEFAULT ''");
    }

    public HoSoTaiKhoan layHoSo(long id) {
        try (Cursor c = getReadableDatabase().query("tai_khoan", new String[]{"ten_dang_nhap","ho_ten","email"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? new HoSoTaiKhoan(id, c.getString(0), c.getString(1), c.getString(2)) : null;
        }
    }

    public boolean capNhatHoSo(long id, String hoTen, String email) {
        String h = hoTen.trim(), e = email.trim();
        if (id <= 0 || h.length() > 80 || e.length() > 254
                || (!e.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches())) return false;
        ContentValues v = new ContentValues();
        v.put("ho_ten", h); v.put("email", e);
        return getWritableDatabase().update("tai_khoan", v, "id=?", new String[]{String.valueOf(id)}) == 1;
    }

    /** Xác thực mật khẩu hiện tại, đổi salt và cấp mã khôi phục mới cho tài khoản đang đăng nhập. */
    public String doiMatKhauDaDangNhap(long id, String matKhauCu, String matKhauMoi) {
        if (id <= 0 || matKhauMoi.length() < 8 || matKhauMoi.equals(matKhauCu)) return null;
        try (Cursor c = getReadableDatabase().query("tai_khoan", new String[]{"muoi","mat_khau_bam"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            String hashCu = c.getString(1);
            if (!bangNhau(bam(matKhauCu, Base64.decode(c.getString(0), Base64.NO_WRAP)), hashCu)) return null;
            byte[] salt = muoiMoi();
            String maMoi = taoMaKhoiPhuc();
            ContentValues v = new ContentValues();
            v.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            v.put("mat_khau_bam", bam(matKhauMoi, salt));
            v.put("ma_khoi_phuc_bam", bamMa(maMoi, salt));
            return getWritableDatabase().update("tai_khoan", v, "id=? AND mat_khau_bam=?",
                    new String[]{String.valueOf(id), hashCu}) == 1 ? maMoi : null;
        } catch (Exception e) { return null; }
    }

    // Dọn địa điểm ID 2 và dữ liệu liên kết trong các bản cài đặt cũ.
    // Xóa bảng con trước, giữ nguyên hai địa điểm ID 0 và 1 cùng tài khoản.
    private void xoaDiaDiemKhongConSuDung(SQLiteDatabase db) {
        String[] idDaXoa = {"2"};
        String cacKhuVuc = "SELECT id FROM khu_vuc WHERE dia_diem_id=?";
        String cacDiem = "SELECT id FROM diem_tham_quan WHERE khu_vuc_id IN ("
                + cacKhuVuc + ")";
        db.delete("thuyet_minh", "diem_tham_quan_id IN (" + cacDiem + ")", idDaXoa);
        db.delete("diem_tham_quan", "khu_vuc_id IN (" + cacKhuVuc + ")", idDaXoa);
        db.delete("khu_vuc", "dia_diem_id=?", idDaXoa);
        db.delete("yeu_thich", "dia_diem_id=?", idDaXoa);
        db.delete("dia_diem", "id=?", idDaXoa);
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

    // THEM 4 DIA DIEM HIEN TAI
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

    // BO SUNG MO TA VA DANH MUC CHO HAI DIA DIEM
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
                "Di sản – kiến trúc",
                "Đại Nội Huế là trung tâm Hoàng cung triều Nguyễn, " +
                        "có Ngọ Môn, Điện Thái Hòa, Điện Kiến Trung, " +
                        "các cung điện và khu vườn di sản."
        );

        capNhatThongTinDiaDiem(
                db,
                3,
                "Di tích – lịch sử",
                "Quần thể di tích Nhà tù Côn Đảo ghi dấu lịch sử " +
                        "đấu tranh của những người từng bị giam giữ tại đây."
        );

        capNhatThongTinDiaDiem(
                db,
                4,
                "Văn hóa – kiến trúc",
                "Văn Miếu – Quốc Tử Giám là di tích lịch sử văn hóa " +
                        "nổi tiếng tại Hà Nội, gắn với truyền thống hiếu học."
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
