
package com.example.app_ck;

public final class NoiDungThuyetMinhDinhDocLap {

    private NoiDungThuyetMinhDinhDocLap() {
    }

    public static String layNoiDung(int id) {
        DuLieuDinhDocLap.DiemThamQuan diem =
                DuLieuDinhDocLap.timTheoId(id);

        if (diem == null) {
            return "Không tìm thấy điểm tham quan.";
        }

        return KhoNoiDungThamQuan.bai(KhoNoiDungThamQuan.lay(0, id));
    }
}
