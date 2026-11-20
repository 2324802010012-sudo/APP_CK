package com.example.app_ck.ui.tour;

/** Chỉ chứa khác biệt hiển thị; tên, điểm, tầng và GPS luôn lấy từ SQLite. */
final class CauHinhTour {
    final boolean soDoHue;
    final boolean dieuKhienConDao;
    final boolean hienThanhTang;
    final String lyDoNgheThuCong;
    final String lyDoRoiManHinh;

    private CauHinhTour(boolean soDoHue, boolean dieuKhienConDao, boolean hienThanhTang) {
        this.soDoHue = soDoHue;
        this.dieuKhienConDao = dieuKhienConDao;
        this.hienThanhTang = hienThanhTang;
        lyDoNgheThuCong = dieuKhienConDao ? "Đã chuyển sang nghe thủ công."
                : "Đã chuyển sang tham quan thủ công.";
        lyDoRoiManHinh = dieuKhienConDao ? "Đã rời màn hình; AUTO và âm thanh đã dừng." : "";
    }

    static CauHinhTour choDiaDiem(int id) {
        return new CauHinhTour(id == 2, id == 3, id != 2 && id != 3 && id != 4);
    }

    static int diemMacDinh(int id) {
        // Giữ điểm mở mặc định của đường vào Bảo tàng cũ.
        return id == 1 ? 10 : -1;
    }
}
