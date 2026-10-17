
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

        switch (id) {
            case 1:
                return "Phòng Nội các, thuộc Tầng 1 "
                        + "của Dinh Độc Lập. "
                        + "Đây là nơi diễn ra các cuộc họp "
                        + "của Hội đồng Nội các "
                        + "Việt Nam Cộng hòa. "
                        + "Không gian này là một phần "
                        + "của hệ thống các phòng làm việc "
                        + "và hội họp được bảo tồn "
                        + "tại Dinh Độc Lập.";

            case 4:
                return "Phòng Khánh tiết, vị trí số 4 "
                        + "trên sơ đồ Tầng 1. "
                        + "Đây là một trong những không gian "
                        + "tiêu biểu của Dinh Độc Lập, "
                        + "gắn với hoạt động đón tiếp "
                        + "và nghi lễ. "
                        + "Du khách có thể quan sát "
                        + "không gian kiến trúc và "
                        + "cách bố trí nội thất của phòng.";

            default:
                return diem.ten + ". "
                        + "Vị trí số " + diem.id
                        + ", thuộc "
                        + DuLieuDinhDocLap.layTenTang(
                        diem.tang)
                        + " của Dinh Độc Lập. "
                        + "Thông tin lịch sử chi tiết "
                        + "đang được cập nhật "
                        + "và đối chiếu với tư liệu "
                        + "chính thức.";
        }
    }
}
