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
import android.speech.tts.TextToSpeech;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.graphics.drawable.GradientDrawable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    public static final String BAT_DAU_HUONG_DAN = "com.example.app_ck.BAT_DAU_HUONG_DAN";
    public static final String CUON_LEN_TIM_KIEM = "com.example.app_ck.CUON_LEN_TIM_KIEM";
    TextToSpeech mayDocGioiThieu() { return ttsTrangChu; }

    private TextToSpeech ttsTrangChu;
    private boolean sanSangThuyetMinh = false;
    private static final int[] ID_DIA_DIEM = {0, 1, 2, 3, 4};

    private int viTriTheoId(int id) {
        for (int i = 0; i < ID_DIA_DIEM.length; i++) {
            if (ID_DIA_DIEM[i] == id) return i;
        }
        return -1;
    }

    private TaiKhoanDb dbKiemTra;
    private EditText oTimKiem;
    private ScrollView vungCuonTrangChu;

    private View diaDiemDinhDocLap;
    private View diaDiemBaoTang;
    private View diaDiemDaiNoi;
    private View diaDiemConDao;
    private View diaDiemVanMieu;

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
        taoTheDaiNoiHue();

        khoiTaoThuyetMinh();

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

    @Override protected void onPostCreate(Bundle state) {
        super.onPostCreate(state);
        nhanYeuCauTimKiem();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        nhanYeuCauTimKiem();
    }

    private void nhanYeuCauTimKiem() {
        if (getIntent().getBooleanExtra(CUON_LEN_TIM_KIEM, false)) {
            getIntent().removeExtra(CUON_LEN_TIM_KIEM);
            cuonLenTimKiem();
        }
    }

    void cuonLenTimKiem() {
        if (vungCuonTrangChu != null) vungCuonTrangChu.post(() -> vungCuonTrangChu.smoothScrollTo(0, 0));
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

        diaDiemConDao = findViewById(R.id.diaDiemConDao);
        diaDiemVanMieu = findViewById(R.id.diaDiemVanMieu);
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
            new String[ID_DIA_DIEM.length];

    // TOA DO DIA DIEM DOC TU SQLITE
    private final double[][] TOA_DO =
            new double[ID_DIA_DIEM.length][2];

    // LUU DOI TUONG DIA DIEM DOC TU SQLITE
    private final DiaDiem[] duLieuDiaDiem =
            new DiaDiem[ID_DIA_DIEM.length];
    private void thucHienTimKiem() {
        String tuKhoa = chuanHoa(oTimKiem.getText().toString());
        if (tuKhoa.isEmpty()) {
            oTimKiem.setError("Vui lòng nhập tên địa điểm");
            return;
        }
        ArrayList<Integer> ketQua = new ArrayList<>();
        for (int i = 0; i < TEN.length; i++) {
            if (chuanHoa(TEN[i] + " " + (duLieuDiaDiem[i] != null ? duLieuDiaDiem[i].diaChi : "")).contains(tuKhoa)) ketQua.add(i);
        }
        InputMethodManager banPhim = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (banPhim != null) banPhim.hideSoftInputFromWindow(oTimKiem.getWindowToken(), 0);
        oTimKiem.clearFocus();
        if (ketQua.isEmpty()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Kết quả tìm kiếm")
                    .setMessage("Không tìm thấy địa điểm phù hợp")
                    .setPositiveButton("Đóng", null).show();
            return;
        }
        String[] danhSach = new String[ketQua.size()];
        for (int i = 0; i < ketQua.size(); i++) danhSach[i] = TEN[ketQua.get(i)];
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Kết quả tìm kiếm")
                .setItems(danhSach, (dialog, viTri) -> moChiTiet(ID_DIA_DIEM[ketQua.get(viTri)]))
                .setNegativeButton("Đóng", null).show();
    }

    private void moChiTiet(int viTri) {
        if (viTriTheoId(viTri) < 0) return;
        Intent intent = new Intent(this, ChiTietDiaDiemActivity.class);
        intent.putExtra("viTriDiaDiem", viTri);
        startActivity(intent);
    }

    // XU LY CAC CHUC NANG CHINH
    private void xuLyChucNang() {

        ganSuKien(R.id.nutTaiKhoan, () ->
                hienThiMenuTaiKhoan());
        ganSuKien(R.id.nutLichSu, () -> startActivity(new Intent(this, LichSuActivity.class)));


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
        ganSuKien(
                R.id.nutThuyetMinh,
                this::hienThiDanhSachThuyetMinh
        );
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

        ganSuKien(R.id.nutTraiNghiem, this::hienThiDanhSachThuyetMinh);
    }

    // XU LY CHON DIA DIEM NOI BAT
    private void xuLyDiaDiem() {
        ganSuKien(R.id.diaDiemDinhDocLap, () -> moChiTiet(0));
        ganSuKien(R.id.diaDiemBaoTang, () -> moChiTiet(1));
        if (diaDiemDaiNoi != null) {
            diaDiemDaiNoi.setOnClickListener(v -> moChiTiet(2));
        }
        ganSuKien(R.id.diaDiemConDao, () -> moChiTiet(3));
        ganSuKien(R.id.diaDiemVanMieu, () -> moChiTiet(4));
    }

    // XU LY THANH DIEU HUONG

    private void xuLyDieuHuong() {
        ThanhDieuHuongDuoi.gan(this, ThanhDieuHuongDuoi.TRANG_CHU);
    }
    // HIEN THI DANH SACH DIA DIEM
    private void hienThiDanhSachDiaDiem() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Khám phá điểm du lịch")
                .setItems(TEN, (dialog, viTri) -> moChiTiet(ID_DIA_DIEM[viTri]))
                .setNegativeButton("Đóng", null).show();
    }

    // MENU TAI KHOAN
    private void hienThiMenuTaiKhoan() {
        ThanhDieuHuongDuoi.mo(this, ThanhDieuHuongDuoi.CA_NHAN);
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

    // LOC 4 THE DIA DIEM THEO DANH MUC DUOC LUU TRONG SQLITE
    private void locDiaDiemTheoDanhMuc(String danhMuc) {

        View[] cacThe = {
                diaDiemDinhDocLap,
                diaDiemBaoTang,
                diaDiemDaiNoi,
                diaDiemConDao,
                diaDiemVanMieu
        };

        boolean[] hienThi = new boolean[cacThe.length];
        String boLoc = chuanHoa(danhMuc);
        int soKetQua = 0;

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            // DOC LAI SQLITE MOI KHI NGUOI DUNG CHON DANH MUC
            ArrayList<DiaDiem> danhSach = db.danhSachDiaDiem();

            for (DiaDiem diaDiem : danhSach) {
                int id = viTriTheoId(diaDiem.id);
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
            if (cacThe[i] != null) {
                cacThe[i].setVisibility(
                        hienThi[i] ? View.VISIBLE : View.GONE
                );
            }
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
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Địa điểm gợi ý")
                .setItems(TEN, (dialog, viTri) -> moChiTiet(ID_DIA_DIEM[viTri]))
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
            dongDiaDiem.setOnClickListener(v -> moChiTiet(ID_DIA_DIEM[chiSoDiaDiem]));

            danhSachGanToi.addView(dongDiaDiem);
        }
    }

    // DOC THONG TIN DIA DIEM TU SQLITE
    private boolean taiDiaDiemTuSQLite() {

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            ArrayList<DiaDiem> danhSach =
                    db.danhSachDiaDiem();

            boolean[] daCoDuLieu =
                    new boolean[ID_DIA_DIEM.length];

            for (DiaDiem diaDiem : danhSach) {

                int id = viTriTheoId(diaDiem.id);

                // CHI NHAN CAC ID DIA DIEM DA KHAI BAO
                if (id < 0 || id >= TEN.length) {
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

            boolean thanhCong = true;
            for (boolean co : daCoDuLieu) {
                if (!co) { thanhCong = false; break; }
            }

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

        int viTri = viTriTheoId(id);
        if (viTri < 0) return;
        DiaDiem diaDiem = duLieuDiaDiem[viTri];

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

    // Tạo thẻ Đại Nội Huế ngay trong nhóm thẻ nổi bật hiện có.
    // Không yêu cầu thêm ID vào activity_main.xml.
    private void taoTheDaiNoiHue() {
        int viTri = viTriTheoId(2);
        if (viTri < 0 || duLieuDiaDiem[viTri] == null || diaDiemVanMieu == null) {
            return;
        }

        if (!(diaDiemVanMieu.getParent() instanceof ViewGroup)) return;
        ViewGroup cha = (ViewGroup) diaDiemVanMieu.getParent();
        if (cha == null) {
            return;
        }

        DiaDiem diaDiem = duLieuDiaDiem[viTri];
        LinearLayout the = new LinearLayout(this);
        the.setOrientation(LinearLayout.VERTICAL);
        the.setPadding(dpThe(7), dpThe(7), dpThe(7), dpThe(7));
        the.setClickable(true);
        the.setFocusable(true);
        GradientDrawable nen = new GradientDrawable();
        nen.setColor(Color.WHITE);
        nen.setCornerRadius(dpThe(14));
        nen.setStroke(dpThe(1), Color.parseColor("#E0E8DF"));
        the.setBackground(nen);

        ImageView anh = new ImageView(this);
        anh.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int maAnh = getResources().getIdentifier(
                "anh_dai_noi_hue", "drawable", getPackageName());
        if (maAnh != 0) {
            anh.setImageResource(maAnh);
        } else {
            anh.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        the.addView(anh, new LinearLayout.LayoutParams(-1,
                getResources().getDimensionPixelSize(R.dimen.featured_destination_image_height)));

        TextView ten = new TextView(this);
        ten.setText(diaDiem.ten);
        ten.setTextSize(14);
        ten.setLines(2);
        ten.setEllipsize(android.text.TextUtils.TruncateAt.END);
        ten.setTypeface(null, android.graphics.Typeface.BOLD);
        ten.setTextColor(Color.parseColor("#24382D"));
        LinearLayout.LayoutParams thongSoTen = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thongSoTen.setMarginStart(dpThe(5));
        thongSoTen.topMargin = dpThe(10);
        the.addView(ten, thongSoTen);

        TextView moTa = new TextView(this);
        moTa.setText("Di sản kiến trúc và lịch sử triều Nguyễn");
        moTa.setTextSize(11);
        moTa.setSingleLine(true);
        moTa.setEllipsize(android.text.TextUtils.TruncateAt.END);
        moTa.setTextColor(Color.parseColor("#66766B"));
        LinearLayout.LayoutParams thongSoMoTa = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thongSoMoTa.setMarginStart(dpThe(5));
        thongSoMoTa.topMargin = dpThe(5);
        thongSoMoTa.bottomMargin = dpThe(9);
        the.addView(moTa, thongSoMoTa);
        the.setOnClickListener(v -> moChiTiet(2));

        // Thêm vào cùng container với các thẻ cũ.
        // Không ép LayoutParams kiểu LinearLayout vào GridLayout.
        int viTriChen = Math.min(2, cha.getChildCount());
        if (cha instanceof LinearLayout) {
            LinearLayout.LayoutParams thongSoThe = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.featured_destination_card_width),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            thongSoThe.setMarginEnd(dpThe(12));
            cha.addView(the, viTriChen, thongSoThe);
        } else {
            cha.addView(the, viTriChen);
        }
        diaDiemDaiNoi = the;
    }

    private int dpThe(float soDp) {
        return Math.round(soDp * getResources().getDisplayMetrics().density);
    }

    // DONG BO 5 THE TREN TRANG CHU
    private void capNhatTheDiaDiemTuSQLite() {

        // 4 THE DIA DIEM NOI BAT
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

        ganDuLieuChoThe(3, R.id.anhTheConDao, R.id.tenTheConDao);
        ganDuLieuChoThe(4, R.id.anhTheVanMieu, R.id.tenTheVanMieu);

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
    protected void onStop() {
        dungThuyetMinhTrangChu();
        super.onStop();
    }
    @Override
    protected void onDestroy() {

        if (dbKiemTra != null) {
            dbKiemTra.close();
            dbKiemTra = null;
        }

        // DUNG VA GIAI PHONG THUYET MINH
        if (ttsTrangChu != null) {
            ttsTrangChu.stop();
            ttsTrangChu.shutdown();
            ttsTrangChu = null;
        }

        super.onDestroy();
    }

    private void khoiTaoThuyetMinh() {

        ttsTrangChu = new TextToSpeech(
                this,
                trangThai -> {

                    if (trangThai == TextToSpeech.SUCCESS
                            && ttsTrangChu != null) {

                        int ketQua = ttsTrangChu.setLanguage(
                                new Locale("vi", "VN")
                        );

                        sanSangThuyetMinh =
                                ketQua != TextToSpeech.LANG_MISSING_DATA
                                        && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;

                        if (!sanSangThuyetMinh) {
                            android.util.Log.w(
                                    "KIEM_TRA_SQLITE",
                                    "Thiet bi chua ho tro giong doc tieng Viet"
                            );
                        }
                    }
                }
        );
        ttsTrangChu.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override public void onStart(String id) { batDauLichSu(id); }
            @Override public void onDone(String id) {}
            @Override public void onError(String id) {}
        });
    }

    // HIEN THI DANH SACH DIA DIEM CO THUYET MINH
    private void hienThiDanhSachThuyetMinh() {
        dungHuongDanTheoViTri();

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Thuyết minh địa điểm")
                .setItems(
                        TEN,
                        (dialog, viTri) -> phatThuyetMinh(viTri)
                )
                .setNeutralButton(
                        "Dừng đọc",
                        (dialog, which) -> dungThuyetMinhTrangChu()
                )
                .setNegativeButton("Đóng", null)
                .show();
    }

    // PHAT NOI DUNG MO TA DA LUU TRONG SQLITE
    private void phatThuyetMinh(int viTri) {
        dungHuongDanTheoViTri();

        if (viTri < 0 || viTri >= duLieuDiaDiem.length) {
            hienThongBao("Địa điểm không hợp lệ.");
            return;
        }

        DiaDiem diaDiem = duLieuDiaDiem[viTri];

        if (diaDiem == null ||
                diaDiem.moTa == null ||
                diaDiem.moTa.trim().isEmpty()) {

            hienThongBao(
                    "Địa điểm chưa có nội dung thuyết minh trong SQLite."
            );
            return;
        }

        if (!sanSangThuyetMinh || ttsTrangChu == null) {

            hienThongBao(
                    "Giọng đọc tiếng Việt chưa sẵn sàng."
            );
            return;
        }

        String maLuot = "trang_chu_" + viTri + "_" + System.nanoTime();
        chuanBiLichSu(maLuot, diaDiem.id, 0, KhoLichSu.THU_CONG);
        int ketQua = ttsTrangChu.speak(
                diaDiem.moTa,
                TextToSpeech.QUEUE_FLUSH,
                null,
                maLuot
        );

        if (ketQua == TextToSpeech.ERROR) {
            huyLuotLichSu();
            hienThongBao("Không thể phát thuyết minh.");
            return;
        }

        hienThongBao(
                "Đang thuyết minh: " + diaDiem.ten
        );

        android.util.Log.d(
                "KIEM_TRA_SQLITE",
                "Thuyet minh trang chu doc SQLite, id=" + viTri
        );
    }

    // DUNG GIONG DOC
    private void dungThuyetMinhTrangChu() {
        huyLuotLichSu();

        if (ttsTrangChu != null) {
            ttsTrangChu.stop();
        }
    }

}
