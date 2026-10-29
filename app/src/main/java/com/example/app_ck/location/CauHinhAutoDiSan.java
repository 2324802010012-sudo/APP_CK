package com.example.app_ck.location;

import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** WGS84 tham chiếu OSM, độc lập hoàn toàn với tọa độ marker trên ảnh. */
public final class CauHinhAutoDiSan {
    public static final class Diem {
        public final int id;
        public final String ten, nguon;
        public final double lat, lon;
        public final float banKinh;
        public final boolean choPhepAuto;

        public Diem(int id, String ten, boolean choPhepAuto, double lat, double lon, float banKinh, String nguon) {
            this.id = id; this.ten = ten; this.lat = lat; this.lon = lon;
            this.banKinh = banKinh; this.nguon = nguon;
            // Có tọa độ chưa đủ để bật AUTO: điểm phải được cho phép riêng.
            this.choPhepAuto = choPhepAuto && Double.isFinite(lat) && Double.isFinite(lon)
                    && Math.abs(lat) <= 90 && Math.abs(lon) <= 180
                    && Float.isFinite(banKinh) && banKinh > 0;
        }
    }

    public final String ten, tenAnh;
    public final List<Diem> cacDiem;

    private CauHinhAutoDiSan(String ten, String tenAnh, List<Diem> ds) {
        this.ten = ten;
        this.tenAnh = tenAnh;
        cacDiem = Collections.unmodifiableList(new ArrayList<>(ds));
    }

    /** Cấu hình vận hành đọc từ SQLite; các hàm hue/vanMieu chỉ dùng để seed. */
    public static CauHinhAutoDiSan tuSQLite(SQLiteDatabase db, int diaDiem) {
        String ten = "Địa điểm tham quan", anh = "";
        try (Cursor c = db.rawQuery("SELECT ten,ten_anh FROM dia_diem WHERE id=?",
                new String[]{String.valueOf(diaDiem)})) {
            if (c.moveToFirst()) { ten = c.getString(0); anh = c.getString(1); }
        }
        List<Diem> ds = new ArrayList<>();
        for (DiemThamQuan d : KhoDuLieuThamQuan.danhSach(db, diaDiem)) {
            ds.add(new Diem(d.id, d.ten, d.choPhepAuto, d.viDo, d.kinhDo, d.banKinh, d.nguonGps));
        }
        return new CauHinhAutoDiSan(ten, anh == null ? "" : anh, ds);
    }

    private CauHinhAutoDiSan(String ten, List<String> tenDiem, int[] ids, double[][] gps,
                            float radius, long[] ways) {
        this.ten = ten;
        this.tenAnh = "";
        List<Diem> ds = new ArrayList<>();
        for (int i = 0; i < tenDiem.size(); i++) {
            int index = -1;
            for (int j = 0; j < ids.length; j++) if (ids[j] == i + 1) index = j;
            ds.add(index < 0 ? new Diem(i + 1, tenDiem.get(i), false, Double.NaN, Double.NaN, radius, "")
                    : new Diem(i + 1, tenDiem.get(i), true, gps[index][0], gps[index][1], radius,
                    "https://www.openstreetmap.org/way/" + ways[index]));
        }
        cacDiem = Collections.unmodifiableList(ds);
    }

    public boolean choPhepAuto(int id) {
        for (Diem d : cacDiem) if (d.id == id) return d.choPhepAuto;
        return false;
    }

    public String nhanCheDo(int id) {
        return choPhepAuto(id) ? "Có hỗ trợ AUTO · Luôn nghe thủ công được" : "Nghe thủ công";
    }

    public List<BoChonDiemTuDong.Moc> cacMocTuDong() {
        List<BoChonDiemTuDong.Moc> ds = new ArrayList<>();
        for (Diem d : cacDiem) if (d.choPhepAuto)
            ds.add(new BoChonDiemTuDong.Moc(d.id, -1, d.lat, d.lon, d.banKinh));
        return ds;
    }

    public String tenCacDiemTuDong() {
        StringBuilder ten = new StringBuilder();
        for (Diem d : cacDiem) if (d.choPhepAuto) {
            if (ten.length() > 0) ten.append(", ");
            ten.append(d.ten);
        }
        return ten.toString();
    }

    public static CauHinhAutoDiSan hue(List<String> tenDiem) {
        // Tâm bbox công trình từ OSM 24/10/2026; giới thiệu tổng thể khi tiếp cận,
        // không nhận diện phòng/tầng. Duyệt Thị Đường, Thái Bình Lâu, Hậu hồ thủ công.
        return new CauHinhAutoDiSan("Đại Nội Huế", tenDiem, new int[]{1,2,3,4,7,8,9},
                new double[][]{{16.46768245,107.5791834}, {16.46874345,107.5783867},
                        {16.4694768,107.5777963}, {16.4710952,107.5765216},
                        {16.47177995,107.5789196}, {16.46889155,107.5754008},
                        {16.4697182,107.57467835}}, 35,
                new long[]{172657562,531031566,285990343,285990348,617991240,172763420,172763427});
    }

    public static CauHinhAutoDiSan vanMieu(List<String> tenDiem) {
        // Chỉ ba mốc trên trục ngoài trời, cách nhau >100m. Vùng 15m đòi GPS tốt;
        // các nhà bia, giếng, điện, lầu và các cổng sát nhau nghe bằng marker.
        return new CauHinhAutoDiSan("Văn Miếu – Quốc Tử Giám", tenDiem, new int[]{4,6,11},
                new double[][]{{21.02764735,105.83551655}, {21.02858265,105.83588945},
                        {21.02951465,105.8362564}}, 15,
                new long[]{702185143,702185155,702241331});
    }
}
