package com.example.app_ck.data.seed;

import com.example.app_ck.data.model.DiemThamQuan;

import java.util.ArrayList;
import java.util.List;

/** Dữ liệu từ sơ đồ Dinh Độc Lập do người dùng cung cấp. */
public final class DuLieuDinhDocLap {
    private DuLieuDinhDocLap() {}
    public static final int TANG_1=1, TANG_2=2, TANG_3=3,
            TANG_4=4, TANG_HAM=5, TANG_TRET=6;

    public static class DiemThamQuan {
        public final int id;
        public final String ten;
        public final int tang;
        public final float x, y;
        public String moTa = "";
        public String thuyetMinh = "";
        public String hinhAnh = "";
        public DiemThamQuan(int id, String ten, int tang, float x, float y) {
            this.id=id; this.ten=ten; this.tang=tang; this.x=x; this.y=y;
        }
        public boolean coToaDo() { return x >= 0 && x <= 1 && y >= 0 && y <= 1; }
    }

    public static List<DiemThamQuan> layTatCaDiem() {
        List<DiemThamQuan> ds = new ArrayList<>();
        ds.add(new DiemThamQuan(1, "Phòng Nội các", 1, 395f/532f, 525f/655f));
        ds.add(new DiemThamQuan(2, "Phòng Đại yến", 1, 237f/532f, 525f/655f));
        ds.add(new DiemThamQuan(3, "Cầu thang trung tâm", 1, 315f/532f, 499f/655f));
        ds.add(new DiemThamQuan(4, "Phòng Khánh tiết", 1, 315f/532f, 363f/655f));
        ds.add(new DiemThamQuan(5, "Phòng Hội đồng an ninh Quốc gia", 2, 195f/533f, 520f/655f));
        ds.add(new DiemThamQuan(6, "Phòng làm việc của Tổng thống", 2, 200f/533f, 564f/655f));
        ds.add(new DiemThamQuan(7, "Phòng khách của Tổng thống", 2, 266f/533f, 521f/655f));
        ds.add(new DiemThamQuan(8, "Phòng khách của Phó Tổng thống", 2, 469f/533f, 520f/655f));
        ds.add(new DiemThamQuan(9, "Phòng trình quốc thư", 2, 379f/533f, 375f/655f));
        ds.add(new DiemThamQuan(10, "Khu gia đình Tổng thống", 2, 379f/533f, 285f/655f));
        ds.add(new DiemThamQuan(11, "Thư viện", 3, 135f/533f, 558f/655f));
        ds.add(new DiemThamQuan(12, "Phòng khách phu nhân Tổng thống", 3, 213f/533f, 520f/655f));
        ds.add(new DiemThamQuan(13, "Phòng chiếu phim", 3, 293f/533f, 520f/655f));
        ds.add(new DiemThamQuan(14, "Phòng giải trí", 3, 413f/533f, 520f/655f));
        ds.add(new DiemThamQuan(15, "Tứ phương vô sự lầu", 4, 330f/532f, 429f/578f));
        ds.add(new DiemThamQuan(16, "Trực thăng UH1", 4, 329f/532f, 193f/578f));
        ds.add(new DiemThamQuan(17, "Tham mưu tác chiến", 5, 445f/533f, 378f/578f));
        ds.add(new DiemThamQuan(18, "Thông tin liên lạc", 5, 374f/533f, 411f/578f));
        ds.add(new DiemThamQuan(19, "Phòng ngủ Tổng thống", 5, 288f/533f, 412f/578f));
        ds.add(new DiemThamQuan(20, "Phòng trực chiến của Tổng thống", 5, 268f/533f, 413f/578f));
        ds.add(new DiemThamQuan(21, "Bếp", 6, 112f/533f, 414f/578f));
        ds.add(new DiemThamQuan(22, "Xe Jeep", 6, 156f/533f, 423f/578f));
        ds.add(new DiemThamQuan(23, "Xe Mercedes", 6, 350f/533f, 393f/578f));
        ds.add(new DiemThamQuan(24, "Phim tư liệu", 6, 238f/533f, 328f/578f));
        ds.add(new DiemThamQuan(25, "Phòng tập bắn", 6, 283f/533f, 286f/578f));
        return ds;
    }
    public static List<DiemThamQuan> layDiemTheoTang(int tang) {
        List<DiemThamQuan> ds = new ArrayList<>();
        for (DiemThamQuan d : layTatCaDiem()) if (d.tang == tang) ds.add(d);
        return ds;
    }
    public static DiemThamQuan timTheoId(int id) {
        for (DiemThamQuan d : layTatCaDiem()) if (d.id == id) return d;
        return null;
    }
    public static int demSoDiem() { return 25; }
    public static String layTenTang(int tang) {
        switch(tang) {
            case 1: return "Tầng 1";
            case 2: return "Tầng 2";
            case 3: return "Tầng 3";
            case 4: return "Tầng 4";
            case 5: return "Tầng hầm";
            case 6: return "Tầng trệt";
            default: return "Không xác định";
        }
    }
    public static String layAnhSoDo(int tang) {
        switch(tang) {
            case 1: return "so_do_tang_1";
            case 2: return "so_do_tang_2";
            case 3: return "so_do_tang_3";
            case 4: return "so_do_tang_4";
            case 5: return "so_do_tang_ham";
            case 6: return "so_do_tang_tret";
            default: return "";
        }
    }
}
