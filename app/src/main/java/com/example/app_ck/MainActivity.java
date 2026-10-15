package com.example.app_ck;

import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText oTimKiem;
    private ScrollView vungCuonTrangChu;

    private View diaDiemDinhDocLap;
    private View diaDiemBaoTang;
    private View diaDiemBuuDien;

    private TextView thongBaoViTri;
    private LinearLayout danhSachGanToi;

    private final ActivityResultLauncher<String[]> xinQuyenViTri =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    ketQua -> {
                        boolean duocPhep =
                                Boolean.TRUE.equals(
                                        ketQua.get(Manifest.permission.ACCESS_FINE_LOCATION)
                                )
                                        || Boolean.TRUE.equals(
                                        ketQua.get(Manifest.permission.ACCESS_COARSE_LOCATION)
                                );

                        if (duocPhep) {
                            layViTriHienTai();
                        } else {
                            thongBaoViTri.setText(
                                    "Bạn chưa cấp quyền truy cập vị trí."
                            );
                        }
                    }
            );
    @Override
    protected void onCreate(Bundle trangThaiDaLuu) {
        super.onCreate(trangThaiDaLuu);

        setContentView(R.layout.activity_main);

        anhXaGiaoDien();
        xuLyTimKiem();
        xuLyChucNang();
        xuLyDiaDiem();
        xuLyDieuHuong();
        xuLyDanhMuc();
        xuLyDiaDiemGoiY();
        xuLyDiaDiemGanToi();
    }

    // ANH XA CAC THANH PHAN GIAO DIEN

    private void anhXaGiaoDien() {

        oTimKiem = findViewById(R.id.oTimKiem);

        vungCuonTrangChu =
                findViewById(R.id.vungCuonTrangChu);

        diaDiemDinhDocLap =
                findViewById(R.id.diaDiemDinhDocLap);

        diaDiemBaoTang =
                findViewById(R.id.diaDiemBaoTang);

        diaDiemBuuDien =
                findViewById(R.id.diaDiemBuuDien);
    }


    // GAN SU KIEN CHO CAC NUT
    private void ganSuKien(int maThanhPhan, Runnable hanhDong) {

        View thanhPhan = findViewById(maThanhPhan);

        if (thanhPhan != null) {
            thanhPhan.setOnClickListener(v -> hanhDong.run());
        }
    }

    // CHUAN HOA CHUOI TIM KIEM
    private String chuanHoa(String noiDung) {

        String chuoi = Normalizer.normalize(
                noiDung.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        chuoi = chuoi.replaceAll("\\p{M}+", "");

        return chuoi.replace("đ", "d")
                .trim()
                .replaceAll("\\s+", " ");
    }

    // XU LY TIM KIEM DIA DIEM
    private void xuLyTimKiem() {
        oTimKiem.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                thucHienTimKiem();
                return true;
            }
            return false;
        });
        ganSuKien(R.id.nutTimKiem, this::thucHienTimKiem);
    }

    private static final String[] TEN = {"Dinh Độc Lập", "Bảo tàng Chứng tích Chiến tranh", "Bưu điện Trung tâm Sài Gòn"};
    private static final String[] MO_TA = {
            "Di tích lịch sử với Phòng Khánh tiết, Phòng họp Hội đồng Nội các và khu hầm.",
            "Bảo tàng giới thiệu các tư liệu, hình ảnh và hiện vật về chiến tranh và những tác động của chiến tranh.",
            "Công trình kiến trúc nổi bật tại trung tâm thành phố, với không gian giao dịch và nội thất lịch sử."
    };
    private static final double[][] TOA_DO = {
            {10.7770, 106.6953},
            {10.7795, 106.6921},
            {10.7799, 106.6999}
    };

    private void thucHienTimKiem() {
        String tuKhoa = chuanHoa(oTimKiem.getText().toString());
        if (tuKhoa.isEmpty()) {
            oTimKiem.setError("Vui lòng nhập tên địa điểm");
            return;
        }
        ArrayList<Integer> ketQua = new ArrayList<>();
        for (int i = 0; i < TEN.length; i++) {
            if (chuanHoa(TEN[i] + " TP. Hồ Chí Minh Sai Gon").contains(tuKhoa)) ketQua.add(i);
        }
        InputMethodManager banPhim = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (banPhim != null) banPhim.hideSoftInputFromWindow(oTimKiem.getWindowToken(), 0);
        oTimKiem.clearFocus();
        if (ketQua.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Kết quả tìm kiếm")
                    .setMessage("Không tìm thấy địa điểm phù hợp")
                    .setPositiveButton("Đóng", null).show();
            return;
        }
        String[] danhSach = new String[ketQua.size()];
        for (int i = 0; i < ketQua.size(); i++) danhSach[i] = TEN[ketQua.get(i)];
        new AlertDialog.Builder(this).setTitle("Kết quả tìm kiếm")
                .setItems(danhSach, (dialog, viTri) -> moChiTiet(ketQua.get(viTri)))
                .setNegativeButton("Đóng", null).show();
    }

    private void moChiTiet(int viTri) {
        Intent intent = new Intent(this, ChiTietDiaDiemActivity.class);
        intent.putExtra("viTriDiaDiem", viTri);
        startActivity(intent);
    }

    // XU LY CAC CHUC NANG CHINH
    private void xuLyChucNang() {

        ganSuKien(R.id.nutTaiKhoan, () ->
                hienThiMenuTaiKhoan());

        ganSuKien(R.id.nutBanDo, () ->
                hienThongBao(
                        "Chức năng bản đồ đang được xây dựng"
                ));

        ganSuKien(R.id.nutThuyetMinh, () ->
                hienThongBao(
                        "Chức năng thuyết minh đang được xây dựng"
                ));

        ganSuKien(R.id.nutLoTrinh, () ->
                hienThongBao(
                        "Vui lòng chọn địa điểm để xem lộ trình"
                ));

        ganSuKien(R.id.nutYeuThich, () ->
                hienThongBao(
                        "Danh sách địa điểm yêu thích"
                ));

        ganSuKien(R.id.nutXemTatCa, () ->
                hienThiDanhSachDiaDiem());

        ganSuKien(R.id.nutTraiNghiem, () ->
                hienThongBao(
                        "Khám phá thuyết minh bằng giọng nói"
                ));
    }

    // XU LY CHON DIA DIEM NOI BAT
    private void xuLyDiaDiem() {
        ganSuKien(R.id.diaDiemDinhDocLap, () -> moChiTiet(0));
        ganSuKien(R.id.diaDiemBaoTang, () -> moChiTiet(1));
        ganSuKien(R.id.diaDiemBuuDien, () -> moChiTiet(2));
    }

    // XU LY THANH DIEU HUONG

    private void xuLyDieuHuong() {
        findViewById(R.id.navKhamPha).setOnClickListener(v ->
                hienThongBao("Khám phá địa điểm du lịch"));

        findViewById(R.id.navYeuThich).setOnClickListener(v ->
                hienThongBao("Danh sách yêu thích"));

        findViewById(R.id.navTrangChu).setOnClickListener(v -> {
            if (oTimKiem != null) {
                oTimKiem.setText("");
            }
            hienThongBao("Bạn đang ở Trang chủ");
        });

        findViewById(R.id.navCaNhan).setOnClickListener(v ->
                hienThongBao("Thông tin cá nhân"));

        findViewById(R.id.navGoiY).setOnClickListener(v ->
                hienThongBao("Gợi ý địa điểm du lịch"));
    }
    // HIEN THI DANH SACH DIA DIEM
    private void hienThiDanhSachDiaDiem() {
        new AlertDialog.Builder(this).setTitle("Khám phá điểm du lịch")
                .setItems(TEN, (dialog, viTri) -> moChiTiet(viTri))
                .setNegativeButton("Đóng", null).show();
    }

    // HIEN THI CHI TIET DIA DIEM
    private void hienThiChiTiet(
            String tenDiaDiem,
            String khuVuc,
            String moTa
    ) {

        String noiDung =
                "Tên địa điểm: " + tenDiaDiem
                        + "\n\nKhu vực: " + khuVuc
                        + "\n\nGiới thiệu: " + moTa;

        new AlertDialog.Builder(this)
                .setTitle("Thông tin điểm du lịch")
                .setMessage(noiDung)

                .setPositiveButton(
                        "Khám phá",
                        (hopThoai, nut) -> {

                            hienThongBao(
                                    "Đã chọn " + tenDiaDiem
                            );
                        }
                )

                .setNeutralButton(
                        "Thuyết minh",
                        (hopThoai, nut) -> {

                            hienThongBao(
                                    "Thuyết minh: " + tenDiaDiem
                            );
                        }
                )

                .setNegativeButton("Đóng", null)
                .show();
    }

    // MENU TAI KHOAN
    private void hienThiMenuTaiKhoan() {

        String[] danhSach = {
                "Thông tin cá nhân",
                "Đăng nhập",
                "Đăng ký",
                "Lịch sử tham quan",
                "Cài đặt"
        };

        new AlertDialog.Builder(this)
                .setTitle("Tài khoản và tiện ích")
                .setItems(danhSach, (hopThoai, viTri) -> {

                    hienThongBao(danhSach[viTri]);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // HIEN THI THONG BAO
    private void hienThongBao(String noiDung) {

        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    // XU LY DANH MUC DU LICH
    private void xuLyDanhMuc() {

        int[] danhSachDanhMuc = {
                R.id.danhMucTatCa,
                R.id.danhMucVanHoa,
                R.id.danhMucThienNhien,
                R.id.danhMucTamLinh,
                R.id.danhMucSinhThai
        };

        for (int maDanhMuc : danhSachDanhMuc) {

            TextView mucDuocChon = findViewById(maDanhMuc);

            if (mucDuocChon == null) {
                continue;
            }

            mucDuocChon.setOnClickListener(v -> {

                // BO CHON TAT CA DANH MUC
                for (int maKhac : danhSachDanhMuc) {

                    TextView mucKhac = findViewById(maKhac);

                    if (mucKhac != null) {
                        mucKhac.setBackgroundResource(
                                R.drawable.nen_danh_muc_thuong
                        );

                        mucKhac.setTextColor(
                                Color.parseColor("#24382D")
                        );

                        mucKhac.setTypeface(null,
                                android.graphics.Typeface.NORMAL);
                    }
                }

                // DANH DAU DANH MUC DANG CHON
                mucDuocChon.setBackgroundResource(
                        R.drawable.nen_danh_muc_chon
                );

                mucDuocChon.setTextColor(Color.WHITE);

                mucDuocChon.setTypeface(null,
                        android.graphics.Typeface.BOLD);

                // THONG BAO DANH MUC
                String tenDanhMuc =
                        mucDuocChon.getText().toString();

                locDiaDiemTheoDanhMuc(tenDanhMuc);
            });
        }
    }

    private void locDiaDiemTheoDanhMuc(String danhMuc) {
        // Cả ba điểm đều có giá trị văn hóa - lịch sử.
        boolean tatCa = danhMuc.equals("Tất cả") || danhMuc.equals("Văn hóa – lịch sử") || danhMuc.equals("Lịch sử");
        diaDiemDinhDocLap.setVisibility(tatCa ? View.VISIBLE : View.GONE);
        diaDiemBaoTang.setVisibility(tatCa || danhMuc.equals("Bảo tàng") ? View.VISIBLE : View.GONE);
        diaDiemBuuDien.setVisibility(tatCa || danhMuc.equals("Kiến trúc") ? View.VISIBLE : View.GONE);
    }

    // XU LY DIA DIEM GOI Y
    private void xuLyDiaDiemGoiY() {
        ganSuKien(R.id.goiYDinhDocLap, () -> moChiTiet(0));
        ganSuKien(R.id.goiYBaoTang, () -> moChiTiet(1));
        ganSuKien(R.id.nutXemGoiY, this::hienThiDanhSachGoiY);
    }

    private void hienThiDanhSachGoiY() {
        new AlertDialog.Builder(this).setTitle("Địa điểm gợi ý")
                .setItems(TEN, (dialog, viTri) -> moChiTiet(viTri))
                .setNegativeButton("Đóng", null).show();
    }

    private void xuLyDiaDiemGanToi() {

        thongBaoViTri = findViewById(R.id.thongBaoViTri);
        danhSachGanToi = findViewById(R.id.danhSachGanToi);

        ganSuKien(R.id.nutTimGanToi, () -> {

            boolean coQuyenChinhXac =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            boolean coQuyenGanDung =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            if (coQuyenChinhXac || coQuyenGanDung) {
                layViTriHienTai();
            } else {
                xinQuyenViTri.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    private void layViTriHienTai() {

        LocationManager quanLyViTri =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (quanLyViTri == null) {
            thongBaoViTri.setText("Không thể truy cập dịch vụ vị trí.");
            return;
        }

        boolean gpsBat =
                quanLyViTri.isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean mangBat =
                quanLyViTri.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsBat && !mangBat) {
            thongBaoViTri.setText(
                    "Vui lòng bật dịch vụ vị trí trên thiết bị."
            );
            return;
        }

        thongBaoViTri.setText("Đang xác định vị trí...");
        danhSachGanToi.removeAllViews();

        String nhaCungCap = gpsBat
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            quanLyViTri.getCurrentLocation(
                    nhaCungCap,
                    null,
                    getMainExecutor(),
                    viTri -> {
                        if (viTri != null) {
                            hienThiDiaDiemGanToi(viTri);
                        } else {
                            thongBaoViTri.setText(
                                    "Chưa lấy được vị trí. Vui lòng thử lại."
                            );
                        }
                    }
            );

        } else {

            Location viTri =
                    quanLyViTri.getLastKnownLocation(nhaCungCap);

            if (viTri != null) {
                hienThiDiaDiemGanToi(viTri);
            } else {
                thongBaoViTri.setText(
                        "Chưa có dữ liệu vị trí. Vui lòng thử lại."
                );
            }
        }
    }

    private void hienThiDiaDiemGanToi(Location viTriNguoiDung) {

        thongBaoViTri.setText(
                "Đã xác định vị trí. Các địa điểm được sắp xếp từ gần đến xa."
        );

        String[] tenDiaDiem = TEN;
        double[][] toaDo = TOA_DO;

        ArrayList<Integer> thuTu = new ArrayList<>();

        for (int i = 0; i < tenDiaDiem.length; i++) {
            thuTu.add(i);
        }

        Collections.sort(thuTu, (a, b) -> {

            float[] khoangCachA = new float[1];
            float[] khoangCachB = new float[1];

            Location.distanceBetween(
                    viTriNguoiDung.getLatitude(),
                    viTriNguoiDung.getLongitude(),
                    toaDo[a][0],
                    toaDo[a][1],
                    khoangCachA
            );

            Location.distanceBetween(
                    viTriNguoiDung.getLatitude(),
                    viTriNguoiDung.getLongitude(),
                    toaDo[b][0],
                    toaDo[b][1],
                    khoangCachB
            );

            return Float.compare(
                    khoangCachA[0],
                    khoangCachB[0]
            );
        });

        danhSachGanToi.removeAllViews();

        for (int viTri : thuTu) {

            float[] ketQua = new float[1];

            Location.distanceBetween(
                    viTriNguoiDung.getLatitude(),
                    viTriNguoiDung.getLongitude(),
                    toaDo[viTri][0],
                    toaDo[viTri][1],
                    ketQua
            );

            double km = ketQua[0] / 1000.0;

            TextView dongDiaDiem = new TextView(this);

            dongDiaDiem.setText(
                    tenDiaDiem[viTri]
                            + "  •  "
                            + String.format(Locale.getDefault(), "%.1f km", km)
            );

            dongDiaDiem.setTextColor(Color.parseColor("#24382D"));
            dongDiaDiem.setTextSize(13);
            dongDiaDiem.setPadding(12, 16, 12, 16);

            final int chiSoDiaDiem = viTri;
            dongDiaDiem.setOnClickListener(v -> moChiTiet(chiSoDiaDiem));

            danhSachGanToi.addView(dongDiaDiem);
        }
    }
}
