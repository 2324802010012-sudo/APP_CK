package com.example.app_ck.data.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Bài thuyết minh độc lập với dữ liệu khởi tạo. */
public final class BaiThuyetMinh {
    public final int id;
    public final String ten, gioiThieu, diemNhan, thamQuan, ghiChu;
    public final List<String> nguon;
    public final List<BaiThuyetMinh> cacDiem;
    private String vanBanSqlite;

    public BaiThuyetMinh(int id, String ten, String gioiThieu, String diemNhan, String thamQuan,
            String ghiChu, String[] nguon, BaiThuyetMinh... cacDiem) {
        this.id = id;
        this.ten = ten;
        this.gioiThieu = gioiThieu;
        this.diemNhan = diemNhan;
        this.thamQuan = thamQuan;
        this.ghiChu = ghiChu;
        this.nguon = Collections.unmodifiableList(Arrays.asList(nguon.clone()));
        this.cacDiem = Collections.unmodifiableList(Arrays.asList(cacDiem.clone()));
    }

    public String noiDung() {
        if (vanBanSqlite != null) return vanBanSqlite;
        return "Giới thiệu\n" + gioiThieu
                + "\n\nĐiểm đáng chú ý\n" + diemNhan
                + "\n\nGợi ý tham quan\n" + thamQuan
                + (ghiChu.isEmpty() ? "" : "\n\nLưu ý về thông tin\n" + ghiChu);
    }

    public static BaiThuyetMinh tuSqlite(int id, String ten, String vanBan, String[] nguon, BaiThuyetMinh... cacDiem) {
        BaiThuyetMinh bai = new BaiThuyetMinh(id, ten, vanBan, "", "", "", nguon, cacDiem);
        bai.vanBanSqlite = vanBan == null ? "" : vanBan;
        return bai;
    }
}

