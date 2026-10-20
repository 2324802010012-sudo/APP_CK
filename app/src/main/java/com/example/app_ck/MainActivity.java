package com.example.app_ck;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
import android.widget.ImageView;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private TaiKhoanDb dbKiemTra;
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

        // GIU SQLITE MO DE DATABASE INSPECTOR KIEM TRA
        dbKiemTra = new TaiKhoanDb(this);

        dbKiemTra.getWritableDatabase();

        android.util.Log.d(
                "KIEM_TRA_SQLITE",
                "Da mo ket noi SQLite cho Database Inspector"
        );


        // ANH XA GIAO DIEN
        anhXaGiaoDien();

        // DOC DIA DIEM TU SQLITE
        if (!taiDiaDiemTuSQLite()) {

            Toast.makeText(
                    this,
                    "Không đọc được dữ liệu địa điểm từ SQLite.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }
// CAP NHAT ANH VA TEN CUA 5 THE TU SQLITE
        capNhatTheDiaDiemTuSQLite();
        // KHOI TAO CAC CHUC NANG TRANG CHU
        xuLyTimKiem();
        xuLyChucNang();
        xuLyDiaDiem();
        xuLyDieuHuong();
        xuLyDanhMuc();
        xuLyDiaDiemGoiY();
        xuLyDiaDiemGanToi();

        // AN THANH DIEU HUONG SAMSUNG
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.hide(
                WindowInsetsCompat.Type.navigationBars()
        );

        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
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


    // TEN DIA DIEM DOC TU SQLITE
    private final String[] TEN =
            new String[3];

    // TOA DO DIA DIEM DOC TU SQLITE
    private final double[][] TOA_DO =
            new double[3][2];

    // LUU DOI TUONG DIA DIEM DOC TU SQLITE
    private final DiaDiem[] duLieuDiaDiem =
            new DiaDiem[3];
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


        ganSuKien(
                R.id.nutBanDo,
                () -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            BanDoActivity.class
                    );

                    startActivity(intent);
                }
        );


        ganSuKien(R.id.nutThuyetMinh, () ->
                hienThongBao(
                        "Chức năng thuyết minh đang được xây dựng"
                ));

        ganSuKien(R.id.nutLoTrinh, () ->
                hienThongBao(
                        "Vui lòng chọn địa điểm để xem lộ trình"
                ));


        ganSuKien(
                R.id.nutYeuThich,
                () -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            YeuThichActivity.class
                    );

                    startActivity(intent);
                }
        );


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


        findViewById(R.id.navYeuThich)
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            MainActivity.this,
                            YeuThichActivity.class
                    );

                    startActivity(intent);
                });


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

    // LOC 3 THE DIA DIEM THEO DANH MUC DUOC LUU TRONG SQLITE
    private void locDiaDiemTheoDanhMuc(String danhMuc) {

        View[] cacThe = {
                diaDiemDinhDocLap,
                diaDiemBaoTang,
                diaDiemBuuDien
        };

        boolean[] hienThi = new boolean[cacThe.length];
        String boLoc = chuanHoa(danhMuc);
        int soKetQua = 0;

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            // DOC LAI SQLITE MOI KHI NGUOI DUNG CHON DANH MUC
            ArrayList<DiaDiem> danhSach = db.danhSachDiaDiem();

            for (DiaDiem diaDiem : danhSach) {
                int id = diaDiem.id;
                if (id < 0 || id >= cacThe.length) {
                    continue;
                }

                String nhom = chuanHoa(
                        diaDiem.danhMuc == null
                                ? ""
                                : diaDiem.danhMuc
                );

                boolean phuHop;
                if (boLoc.equals("tat ca")) {
                    phuHop = true;
                } else if (boLoc.equals("van hoa – lich su")
                        || boLoc.equals("van hoa - lich su")) {
                    phuHop = nhom.contains("van hoa");
                } else if (boLoc.equals("bao tang")) {
                    phuHop = nhom.contains("bao tang");
                } else if (boLoc.equals("kien truc")) {
                    phuHop = nhom.contains("kien truc");
                } else if (boLoc.equals("lich su")) {
                    phuHop = nhom.contains("lich su");
                } else {
                    phuHop = nhom.equals(boLoc);
                }

                hienThi[id] = phuHop;
            }

        } catch (Exception e) {
            android.util.Log.e(
                    "KIEM_TRA_SQLITE",
                    "Loi loc danh muc tu SQLite",
                    e
            );
            Toast.makeText(
                    this,
                    "Không đọc được danh mục từ SQLite.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        for (int i = 0; i < cacThe.length; i++) {
            cacThe[i].setVisibility(
                    hienThi[i] ? View.VISIBLE : View.GONE
            );
            if (hienThi[i]) {
                soKetQua++;
            }
        }

        android.util.Log.d(
                "KIEM_TRA_SQLITE",
                "Loc danh muc SQLite: " + danhMuc
                        + ", so dia diem: " + soKetQua
        );

        if (soKetQua == 0) {
            Toast.makeText(
                    this,
                    "Chưa có địa điểm thuộc danh mục này.",
                    Toast.LENGTH_SHORT
            ).show();
        }
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

    // DOC THONG TIN DIA DIEM TU SQLITE
    private boolean taiDiaDiemTuSQLite() {

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            ArrayList<DiaDiem> danhSach =
                    db.danhSachDiaDiem();

            boolean[] daCoDuLieu =
                    new boolean[3];

            for (DiaDiem diaDiem : danhSach) {

                int id = diaDiem.id;

                // GIU NGUYEN 3 ID DIA DIEM HIEN TAI
                if (id < 0 || id >= 3) {
                    continue;
                }

                if (diaDiem.ten == null ||
                        diaDiem.ten.trim().isEmpty()) {
                    continue;
                }

                TEN[id] = diaDiem.ten;

                TOA_DO[id][0] = diaDiem.viDo;
                TOA_DO[id][1] = diaDiem.kinhDo;
// LUU CA DIA CHI, MO TA VA TEN ANH
                duLieuDiaDiem[id] = diaDiem;
                daCoDuLieu[id] = true;
            }

            boolean thanhCong =
                    daCoDuLieu[0]
                            && daCoDuLieu[1]
                            && daCoDuLieu[2];

            android.util.Log.d(
                    "KIEM_TRA_SQLITE",
                    "Trang chu doc dia diem: "
                            + (thanhCong
                            ? "THANH CONG"
                            : "THIEU DU LIEU")
            );

            return thanhCong;

        } catch (Exception e) {

            android.util.Log.e(
                    "KIEM_TRA_SQLITE",
                    "Loi doc dia diem trang chu",
                    e
            );

            return false;
        }
    }

    // GAN TEN VA ANH SQLITE CHO MOT THE DIA DIEM
    private void ganDuLieuChoThe(
            int id,
            int idAnh,
            int idTen
    ) {

        DiaDiem diaDiem = duLieuDiaDiem[id];

        if (diaDiem == null) {
            return;
        }

        TextView ten = findViewById(idTen);

        ImageView anh = findViewById(idAnh);

        // CAP NHAT TEN TU SQLITE
        if (ten != null) {
            ten.setText(diaDiem.ten);
        }

        // CAP NHAT ANH TU TEN RESOURCE LUU TRONG SQLITE
        if (anh != null &&
                diaDiem.tenAnh != null &&
                !diaDiem.tenAnh.trim().isEmpty()) {

            int maAnh = getResources().getIdentifier(
                    diaDiem.tenAnh,
                    "drawable",
                    getPackageName()
            );

            if (maAnh != 0) {
                anh.setImageResource(maAnh);
                anh.setContentDescription(
                        "Ảnh " + diaDiem.ten
                );
            }
        }
    }

    // DONG BO 5 THE TREN TRANG CHU
    private void capNhatTheDiaDiemTuSQLite() {

        // 3 THE DIA DIEM NOI BAT
        ganDuLieuChoThe(
                0,
                R.id.anhTheDinhDocLap,
                R.id.tenTheDinhDocLap
        );

        ganDuLieuChoThe(
                1,
                R.id.anhTheBaoTang,
                R.id.tenTheBaoTang
        );

        ganDuLieuChoThe(
                2,
                R.id.anhTheBuuDien,
                R.id.tenTheBuuDien
        );

        // 2 THE GOI Y
        ganDuLieuChoThe(
                0,
                R.id.anhGoiYDinhDocLap,
                R.id.tenGoiYDinhDocLap
        );

        ganDuLieuChoThe(
                1,
                R.id.anhGoiYBaoTang,
                R.id.tenGoiYBaoTang
        );

        android.util.Log.d(
                "KIEM_TRA_SQLITE",
                "Da dong bo 5 the dia diem tu SQLite"
        );
    }

    @Override
    protected void onDestroy() {

        if (dbKiemTra != null) {
            dbKiemTra.close();
            dbKiemTra = null;
        }

        super.onDestroy();
    }

}