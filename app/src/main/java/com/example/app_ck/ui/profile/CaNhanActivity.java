
package com.example.app_ck.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.admin.AdminActivity;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.ThanhDieuHuongDuoi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CaNhanActivity extends BaseActivity {

    private static final String TAG = "CaNhanActivity";

    // =====================================================
    // 1. KET QUA DANG NHAP
    // =====================================================

    private final ActivityResultLauncher<Intent> dangNhap =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> hienThi()
            );

    // =====================================================
    // 2. KHOI TAO
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ca_nhan);

        apDungKhoangTrongHeThong();

        ThanhDieuHuongDuoi.gan(
                this,
                ThanhDieuHuongDuoi.CA_NHAN
        );

        caiDatSuKien();
        caiDatCacMucTaiKhoan();
    }

    // =====================================================
    // 3. SU KIEN CO BAN
    // =====================================================

    private void caiDatSuKien() {

        // Dang nhap
        findViewById(R.id.cnDangNhap)
                .setOnClickListener(
                        v -> moDangNhap(false)
                );

        // Dang ky
        findViewById(R.id.cnDangKy)
                .setOnClickListener(
                        v -> moDangNhap(true)
                );

        // Xem danh sach yeu thich
        findViewById(R.id.cnThongKeYeuThich)
                .setOnClickListener(v -> {

                    if (!PhienDangNhap.daDangNhap(this)) {
                        moDangNhap(false);
                        return;
                    }

                    ThanhDieuHuongDuoi.mo(
                            this,
                            ThanhDieuHuongDuoi.YEU_THICH
                    );
                });

        // Xem lich su nghe
        findViewById(R.id.cnThongKeLichSu)
                .setOnClickListener(v -> moLichSu());

        // Mo trang quan tri
        findViewById(R.id.cnAdmin)
                .setOnClickListener(
                        v -> moTrangAdmin()
                );

        // Dang xuat
        findViewById(R.id.cnDangXuat)
                .setOnClickListener(
                        v -> xacNhanDangXuat()
                );
    }

    // =====================================================
    // 4. CAC MUC CHUC NANG
    // =====================================================

    private void caiDatCacMucTaiKhoan() {

        // Thong tin ca nhan
        ganHang(
                R.id.cnSuaHoSo,
                R.drawable.bieu_tuong_ca_nhan,
                "Thông tin cá nhân",
                "Chỉnh sửa họ tên và email liên hệ",
                () -> moThongTinCaNhan()
        );

        // Lich su nghe
        ganHang(
                R.id.cnLichSu,
                R.drawable.ui_ic_history,
                "Lịch sử nghe",
                "Xem lại các lượt thuyết minh",
                () -> moLichSu()
        );

        // Doi mat khau
        ganHang(
                R.id.cnDoiMatKhau,
                R.drawable.ui_ic_lock,
                "Đổi mật khẩu",
                "Bảo mật tài khoản của bạn",
                () -> moDoiMatKhau()
        );

        // Thong tin ung dung
        ganHang(
                R.id.cnHuongDan,
                R.drawable.ui_ic_route,
                "Thông tin ứng dụng",
                "Giới thiệu và lưu trữ dữ liệu",
                () -> moThongTinUngDung()
        );
    }

    private void ganHang(
            int id,
            int icon,
            String tieuDe,
            String moTa,
            Runnable hanhDong
    ) {

        View hang = findViewById(id);

        if (hang == null) {
            return;
        }

        ImageView anh = hang.findViewById(
                R.id.cnActionIcon
        );

        TextView ten = hang.findViewById(
                R.id.cnActionTitle
        );

        TextView moTaView = hang.findViewById(
                R.id.cnActionDetail
        );

        anh.setImageResource(icon);

        ten.setText(tieuDe);

        moTaView.setText(moTa);

        hang.setContentDescription(
                tieuDe + ". " + moTa
        );

        hang.setOnClickListener(
                v -> hanhDong.run()
        );
    }

    // =====================================================
    // 5. DIEU HUONG TAI KHOAN
    // =====================================================

    private void moDangNhap(boolean dangKy) {

        Intent intent = new Intent(
                this,
                DangNhapActivity.class
        );

        intent.putExtra(
                DangNhapActivity.MO_DANG_KY,
                dangKy
        );

        dangNhap.launch(intent);
    }

    private void moThongTinCaNhan() {

        if (!kiemTraDangNhap()) {
            return;
        }

        startActivity(
                new Intent(
                        this,
                        SuaHoSoActivity.class
                )
        );
    }

    private void moLichSu() {

        if (!kiemTraDangNhap()) {
            return;
        }

        startActivity(
                new Intent(
                        this,
                        LichSuActivity.class
                )
        );
    }

    private void moDoiMatKhau() {

        if (!kiemTraDangNhap()) {
            return;
        }

        startActivity(
                new Intent(
                        this,
                        DoiMatKhauActivity.class
                )
        );
    }

    private boolean kiemTraDangNhap() {

        if (PhienDangNhap.daDangNhap(this)) {
            return true;
        }

        Toast.makeText(
                this,
                "Vui lòng đăng nhập để tiếp tục.",
                Toast.LENGTH_SHORT
        ).show();

        moDangNhap(false);

        return false;
    }

    // =====================================================
    // 6. QUAN TRI VIEN
    // =====================================================

    private void moTrangAdmin() {

        if (!PhienDangNhap.laAdmin(this)) {

            Toast.makeText(
                    this,
                    "Bạn không có quyền quản trị.",
                    Toast.LENGTH_SHORT
            ).show();

            hienThi();
            return;
        }

        startActivity(
                new Intent(
                        this,
                        AdminActivity.class
                )
        );
    }

    // =====================================================
    // 7. XAC NHAN DANG XUAT
    // =====================================================

    private void xacNhanDangXuat() {

        if (!PhienDangNhap.daDangNhap(this)) {

            hienThi();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất?")
                .setMessage(
                        "Bạn sẽ chuyển về chế độ khách. "
                                + "Hồ sơ, địa điểm yêu thích và "
                                + "lịch sử nghe của tài khoản "
                                + "vẫn được lưu trên thiết bị."
                )
                .setNegativeButton(
                        "Ở lại",
                        null
                )
                .setPositiveButton(
                        "Đăng xuất",
                        (dialog, which) -> {

                            PhienDangNhap.dangXuat(this);

                            hienThi();

                            Toast.makeText(
                                    this,
                                    "Đã đăng xuất.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    // =====================================================
    // 8. THONG TIN UNG DUNG
    // =====================================================

    private void moThongTinUngDung() {

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "APP_CK · Khám phá Việt Nam"
                )
                .setMessage(
                        "Ứng dụng hỗ trợ khám phá các địa điểm "
                                + "du lịch và di tích lịch sử tại Việt Nam.\n\n"

                                + "Chức năng chính:\n"
                                + "• Khám phá và tìm kiếm địa điểm.\n"
                                + "• Xem vị trí trên bản đồ.\n"
                                + "• Nghe thuyết minh khi tham quan.\n"
                                + "• Lưu địa điểm yêu thích.\n"
                                + "• Xem lại lịch sử nghe.\n\n"

                                + "Dữ liệu tài khoản và lịch sử "
                                + "được lưu cục bộ trên thiết bị, "
                                + "chưa đồng bộ giữa các điện thoại.\n\n"

                                + "Email liên hệ chưa được xác minh "
                                + "và không dùng để khôi phục mật khẩu. "
                                + "Hãy lưu giữ mã khôi phục tài khoản "
                                + "được cấp khi đăng ký hoặc đổi mật khẩu."
                )
                .setPositiveButton(
                        "Đóng",
                        null
                )
                .show();
    }

    // =====================================================
    // 9. HIEN THI LAI KHI QUAY VE
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        hienThi();
    }

    // =====================================================
    // 10. DOC DU LIEU VA CAP NHAT GIAO DIEN
    // =====================================================

    private void hienThi() {

        if (findViewById(R.id.cnTen) == null) {
            return;
        }

        boolean daDangNhap =
                PhienDangNhap.daDangNhap(this);

        HoSoTaiKhoan hoSo = null;

        int soYeuThich = 0;
        int soLuotNghe = 0;

        // Chi doc du lieu ca nhan khi da dang nhap
        if (daDangNhap) {

            try (AppDatabase db =
                         new AppDatabase(this)) {

                long taiKhoanId =
                        PhienDangNhap.id(this);

                hoSo = db.layHoSo(
                        taiKhoanId
                );

                // Phien khong con hop le
                if (hoSo == null
                        || !hoSo.hoatDong()) {

                    PhienDangNhap.dangXuat(this);

                    daDangNhap = false;

                } else {

                    // Lay danh sach ID dia diem con ton tai
                    Set<Integer> idHopLe =
                            new HashSet<>();

                    for (DiaDiem d :
                            db.danhSachDiaDiem()) {

                        idHopLe.add(d.id);
                    }

                    // Dem so dia diem yeu thich hop le
                    for (int id :
                            db.danhSachYeuThich(hoSo.id)) {

                        if (idHopLe.contains(id)) {
                            soYeuThich++;
                        }
                    }

                    // Doc lich su rieng cua tai khoan.
                    // Khong su dung so luong gia lap.
                    soLuotNghe =
                            KhoLichSu.doc(
                                    this,
                                    db.getReadableDatabase(),
                                    hoSo.id
                            ).size();
                }

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Khong tai duoc ho so ca nhan",
                        e
                );

                Toast.makeText(
                        this,
                        "Không tải được thông tin tài khoản. "
                                + "Vui lòng thử lại.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }
        }

        // ---------------------------------------------
        // 10.1. CHUYEN TRANG THAI GIAO DIEN
        // ---------------------------------------------

        hienThiView(
                R.id.cnKhach,
                !daDangNhap
        );

        hienThiView(
                R.id.cnDaDangNhap,
                daDangNhap
        );

        hienThiView(
                R.id.cnThongKe,
                daDangNhap
        );

        hienThiView(
                R.id.cnDangXuat,
                daDangNhap
        );

        // ---------------------------------------------
        // 10.2. THONG TIN NGUOI DUNG
        // ---------------------------------------------

        String tenHienThi =
                daDangNhap && hoSo != null
                        ? hoSo.tenHienThi()
                        : "Chào bạn!";

        if (tenHienThi == null
                || tenHienThi.trim().isEmpty()) {

            tenHienThi = "Khách tham quan";
        }

        chu(
                R.id.cnTen,
                tenHienThi
        );

        chu(
                R.id.cnTenDangNhap,
                daDangNhap && hoSo != null
                        ? "@" + hoSo.tenDangNhap
                        : "Khám phá Việt Nam cùng APP_CK"
        );

        boolean laAdmin =
                daDangNhap
                        && hoSo != null
                        && HoSoTaiKhoan.ADMIN.equals(
                        hoSo.vaiTro
                )
                        && HoSoTaiKhoan.ACTIVE.equals(
                        hoSo.trangThai
                );

        chu(
                R.id.cnTrangThai,
                !daDangNhap
                        ? "Khách tham quan"
                        : laAdmin
                        ? "Quản trị viên"
                        : "Tài khoản cá nhân"
        );

        chu(
                R.id.cnAvatar,
                taoChuAvatar(
                        tenHienThi,
                        daDangNhap
                )
        );

        // ---------------------------------------------
        // 10.3. THONG KE THUC TE
        // ---------------------------------------------

        chu(
                R.id.cnSoYeuThich,
                String.valueOf(soYeuThich)
        );

        chu(
                R.id.cnSoLichSu,
                String.valueOf(soLuotNghe)
        );

        findViewById(R.id.cnThongKeYeuThich)
                .setContentDescription(
                        soYeuThich
                                + " địa điểm đã lưu. Nhấn để xem."
                );

        findViewById(R.id.cnThongKeLichSu)
                .setContentDescription(
                        soLuotNghe
                                + " lượt đã nghe. Nhấn để xem lịch sử."
                );

        // ---------------------------------------------
        // 10.4. PHAN QUYEN ADMIN
        // ---------------------------------------------

        hienThiView(
                R.id.cnVungAdmin,
                laAdmin
        );
    }

    // =====================================================
    // 11. HAM HO TRO GIAO DIEN
    // =====================================================

    private void chu(
            int id,
            String noiDung
    ) {

        TextView view = findViewById(id);

        if (view != null) {

            view.setText(noiDung);
        }
    }

    private void hienThiView(
            int id,
            boolean hien
    ) {

        View view = findViewById(id);

        if (view != null) {

            view.setVisibility(
                    hien ? View.VISIBLE : View.GONE
            );
        }
    }

    // Lay chu cai dau cua ten va ho
    private String taoChuAvatar(
            String hoTen,
            boolean daDangNhap
    ) {

        if (!daDangNhap) {
            return "K";
        }

        if (hoTen == null
                || hoTen.trim().isEmpty()) {
            return "U";
        }

        String[] cacTu =
                hoTen.trim().split("\\s+");

        String chuDau =
                kyTuDau(cacTu[0]);

        if (cacTu.length > 1) {

            chuDau += kyTuDau(
                    cacTu[cacTu.length - 1]
            );
        }

        return chuDau.toUpperCase(
                Locale.ROOT
        );
    }

    private String kyTuDau(String noiDung) {

        if (noiDung == null
                || noiDung.isEmpty()) {

            return "";
        }

        int maKyTu =
                noiDung.codePointAt(0);

        return new String(
                Character.toChars(maKyTu)
        );
    }
}
