
package com.example.app_ck.data.seed;


public final class NoiDungThuyetMinhDinhDocLap {

    private NoiDungThuyetMinhDinhDocLap() {
    }

    public static String layNoiDung(int id) {
        DuLieuDinhDocLap.DiemThamQuan diem =
                DuLieuDinhDocLap.timTheoId(id);

        if (diem == null) {
            return "Không tìm thấy điểm tham quan.";
        }

        return DuLieuNoiDungThamQuan.lay(0, id).noiDung();
    }
}
