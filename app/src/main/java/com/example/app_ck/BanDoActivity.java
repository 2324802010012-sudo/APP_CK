package com.example.app_ck;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Locale;

public class BanDoActivity extends AppCompatActivity {

    private static final int YEU_CAU_VI_TRI = 101;

    // DANH SACH DIA DIEM DU LICH

    // DU LIEU DIA DIEM DOC TU SQLITE
    private final String[] TEN_DIA_DIEM =
            new String[3];

    private final String[] DIA_CHI =
            new String[3];

    private final double[][] TOA_DO =
            new double[3][2];

    private final String[] TEN_ANH =
            new String[3];
    private MapView banDo;
    private TaiKhoanDb dbKiemTra;

    private ImageView anhDiaDiemBanDo;

    private static final int[] ANH_DIA_DIEM = {
            R.drawable.anh_dinh_doc_lap,
            R.drawable.anh_bao_tang_chung_tich,
            R.drawable.anh_buu_dien_sai_gon
    };

    private TextView tenDiaDiem;
    private TextView diaChiDiaDiem;
    private TextView nutViTriCuaToi;

    private Button nutXemChiTiet;
    private Button nutChiDuong;

    private MyLocationNewOverlay lopViTri;

    private boolean daBatDinhVi = false;

    // -1 NGHIA LA CHUA CHON DIA DIEM
    private int viTriDaChon = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // KHOI TAO CAU HINH OPENSTREETMAP
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences(
                        "cau_hinh_ban_do",
                        MODE_PRIVATE
                )
        );

        Configuration.getInstance().setUserAgentValue(
                "APP_CK/1.0 (Android; com.example.app_ck)"
        );

        setContentView(R.layout.activity_ban_do);

        dbKiemTra = new TaiKhoanDb(this);

// Mo va giu ket noi SQLite khi man hinh ban do dang mo
        dbKiemTra.getReadableDatabase();


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        anhDiaDiemBanDo = findViewById(
                R.id.anhDiaDiemBanDo
        );
        // ANH XA GIAO DIEN
        banDo = findViewById(
                R.id.banDoTuongTac
        );

        tenDiaDiem = findViewById(
                R.id.tenDiaDiemBanDo
        );

        diaChiDiaDiem = findViewById(
                R.id.diaChiBanDo
        );

        nutXemChiTiet = findViewById(
                R.id.nutXemChiTietBanDo
        );

        nutChiDuong = findViewById(
                R.id.nutChiDuongBanDo
        );

        nutViTriCuaToi = findViewById(
                R.id.nutViTriCuaToi
        );

        // NUT QUAY LAI
        findViewById(R.id.nutQuayLaiBanDo)
                .setOnClickListener(
                        v -> finish()
                );

        // NUT XEM CHI TIET
        nutXemChiTiet.setOnClickListener(
                v -> moChiTietDiaDiem()
        );

        // NUT CHI DUONG
        nutChiDuong.setOnClickListener(
                v -> moChiDuong()
        );

        // NUT VI TRI CUA TOI
        nutViTriCuaToi.setOnClickListener(
                v -> kiemTraQuyenViTri()
        );

        // DOC DU LIEU TU SQLITE
        if (!taiDiaDiemTuSQLite()) {

            thongBao(
                    "Không đọc được dữ liệu địa điểm từ SQLite."
            );

            finish();
            return;
        }

        // KHOI TAO BAN DO
        khoiTaoBanDo();

        // THEM 3 MARKER TU SQLITE
        themCacMarker();

// CHON NHANH DINH DOC LAP
        findViewById(R.id.nutChonDinhDocLap)
                .setOnClickListener(
                        v -> chonVaDiChuyen(0)
                );

// CHON NHANH BAO TANG
        findViewById(R.id.nutChonBaoTang)
                .setOnClickListener(
                        v -> chonVaDiChuyen(1)
                );

// CHON NHANH BUU DIEN
        findViewById(R.id.nutChonBuuDien)
                .setOnClickListener(
                        v -> chonVaDiChuyen(2)
                );

// NUT PHONG TO
        findViewById(R.id.nutPhongToBanDo)
                .setOnClickListener(v -> {
                    banDo.getController().zoomIn();
                });

// NUT THU NHO
        findViewById(R.id.nutThuNhoBanDo)
                .setOnClickListener(v -> {
                    banDo.getController().zoomOut();
                });

// TIM KIEM DIA DIEM
        EditText oTimKiem = findViewById(
                R.id.oTimKiemBanDo
        );

        oTimKiem.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        timKiemDiaDiem(
                                oTimKiem.getText().toString()
                        );

                        return true;
                    }

                    return false;
                }
        );

    }

    // THIET LAP BAN DO OSM
    private void khoiTaoBanDo() {

        XYTileSource nguonBanDo = new XYTileSource(
                "OSMOfficial",
                0,
                19,
                256,
                ".png",
                new String[]{
                        "https://tile.openstreetmap.org/"
                }
        );

        banDo.setTileSource(nguonBanDo);

        banDo.setMultiTouchControls(true);
        banDo.setBuiltInZoomControls(false);

        banDo.setMinZoomLevel(12.0);
        banDo.setMaxZoomLevel(19.0);

        GeoPoint trungTam = new GeoPoint(
                10.7790,
                106.6960
        );

        banDo.getController().setZoom(15.5);

        banDo.getController().setCenter(
                trungTam
        );

        banDo.invalidate();
    }

    // THEM CAC MARKER DIA DIEM
    private void themCacMarker() {

        for (int i = 0; i < TEN_DIA_DIEM.length; i++) {

            final int chiSo = i;

            GeoPoint toaDo = new GeoPoint(
                    TOA_DO[i][0],
                    TOA_DO[i][1]
            );

            Marker marker = new Marker(banDo);

            marker.setPosition(toaDo);

            marker.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
            );

            marker.setTitle(
                    TEN_DIA_DIEM[i]
            );

            marker.setSnippet(
                    DIA_CHI[i]
            );

            marker.setOnMarkerClickListener(
                    (markerDuocChon, mapView) -> {

                        chonDiaDiem(chiSo);

                        mapView.getController().animateTo(
                                markerDuocChon.getPosition()
                        );

                        return true;
                    }
            );

            banDo.getOverlays().add(marker);
        }

        banDo.invalidate();
    }

    // CAP NHAT THONG TIN KHI CHON MARKER

    private void chonDiaDiem(int chiSo) {

        if (chiSo < 0 ||
                chiSo >= TEN_DIA_DIEM.length) {
            return;
        }

        viTriDaChon = chiSo;

        // CAP NHAT ANH DIA DIEM
        anhDiaDiemBanDo.setImageResource(
                ANH_DIA_DIEM[chiSo]
        );

        // CAP NHAT TEN DIA DIEM
        tenDiaDiem.setText(
                TEN_DIA_DIEM[chiSo]
        );

        // GIU NGUYEN TINH KHOANG CACH GPS
        capNhatKhoangCach();

        // BAT NUT CHUC NANG
        nutXemChiTiet.setEnabled(true);
        nutChiDuong.setEnabled(true);
    }

    // KIEM TRA QUYEN TRUY CAP VI TRI
    private boolean daCoQuyenViTri() {

        boolean quyenChinhXac =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean quyenGanDung =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        return quyenChinhXac || quyenGanDung;
    }

    private void kiemTraQuyenViTri() {

        if (daCoQuyenViTri()) {
            batDinhVi();
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                YEU_CAU_VI_TRI
        );
    }

    // KET QUA CAP QUYEN GPS
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode != YEU_CAU_VI_TRI) {
            return;
        }

        if (daCoQuyenViTri()) {
            batDinhVi();
        } else {
            thongBao(
                    "Cần cấp quyền vị trí để sử dụng GPS."
            );
        }
    }

    // BAT DINH VI VA HIEN THI TREN BAN DO
    private void batDinhVi() {

        if (!daCoQuyenViTri()) {
            return;
        }

        if (lopViTri == null) {

            GpsMyLocationProvider nhaCungCap =
                    new GpsMyLocationProvider(this);

            lopViTri = new MyLocationNewOverlay(
                    nhaCungCap,
                    banDo
            );

            banDo.getOverlays().add(lopViTri);
        }

        daBatDinhVi = true;

        // BAT CAP NHAT VI TRI
        boolean dangLayViTri = lopViTri.enableMyLocation();

        if (!dangLayViTri) {
            thongBao(
                    "Không thể khởi động định vị. Hãy kiểm tra GPS."
            );
            return;
        }

        GeoPoint viTriHienTai =
                lopViTri.getMyLocation();

        // NEU DA CO VI TRI THI DI CHUYEN NGAY
        if (viTriHienTai != null) {

            diChuyenDenViTri(
                    viTriHienTai
            );

            return;
        }

        thongBao(
                "Đang xác định vị trí của bạn..."
        );

        // CHO DU LIEU VI TRI DAU TIEN
        lopViTri.runOnFirstFix(() -> {

            runOnUiThread(() -> {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (lopViTri == null) {
                    return;
                }

                GeoPoint viTriMoi =
                        lopViTri.getMyLocation();

                if (viTriMoi != null) {
                    diChuyenDenViTri(viTriMoi);
                }
            });
        });
    }

    // DI CHUYEN BAN DO DEN VI TRI GPS
    private void diChuyenDenViTri(
            GeoPoint viTri
    ) {

        banDo.getController().setZoom(17.0);

        banDo.getController().animateTo(
                viTri
        );

        capNhatKhoangCach();
    }

    // TINH KHOANG CACH TU GPS DEN DIA DIEM
    private void capNhatKhoangCach() {

        if (viTriDaChon < 0) {
            return;
        }

        String noiDung =
                "⌖  " + DIA_CHI[viTriDaChon];

        if (lopViTri == null ||
                !daBatDinhVi) {

            diaChiDiaDiem.setText(noiDung);
            return;
        }

        GeoPoint viTriNguoiDung =
                lopViTri.getMyLocation();

        if (viTriNguoiDung == null) {

            diaChiDiaDiem.setText(noiDung);
            return;
        }

        float[] ketQua = new float[1];

        Location.distanceBetween(
                viTriNguoiDung.getLatitude(),
                viTriNguoiDung.getLongitude(),
                TOA_DO[viTriDaChon][0],
                TOA_DO[viTriDaChon][1],
                ketQua
        );

        float met = ketQua[0];

        String khoangCach;

        if (met < 1000) {

            khoangCach = String.format(
                    Locale.forLanguageTag("vi-VN"),
                    "%.0f m",
                    met
            );

        } else {

            khoangCach = String.format(
                    Locale.forLanguageTag("vi-VN"),
                    "%.2f km",
                    met / 1000.0f
            );
        }

        diaChiDiaDiem.setText(
                noiDung
                        + "\nKhoảng cách đường chim bay: "
                        + khoangCach
        );
    }

    // MO MAN HINH CHI TIET DIA DIEM
    private void moChiTietDiaDiem() {

        if (viTriDaChon < 0) {

            thongBao(
                    "Vui lòng chọn địa điểm trên bản đồ."
            );

            return;
        }

        Intent intent = new Intent(
                this,
                ChiTietDiaDiemActivity.class
        );

        intent.putExtra(
                "viTriDiaDiem",
                viTriDaChon
        );

        startActivity(intent);
    }

    // MO CHI DUONG
    private void moChiDuong() {

        if (viTriDaChon < 0) {

            thongBao(
                    "Vui lòng chọn địa điểm cần đến."
            );

            return;
        }

        String toaDo =
                TOA_DO[viTriDaChon][0]
                        + ","
                        + TOA_DO[viTriDaChon][1];

        Uri duongDan = Uri.parse(
                "https://www.google.com/maps/dir/"
                        + "?api=1&destination="
                        + toaDo
                        + "&travelmode=driving"
        );

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                duongDan
        );

        try {
            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            thongBao(
                    "Không tìm thấy ứng dụng chỉ đường."
            );
        }
    }

    // THONG BAO NGAN
    private void thongBao(String noiDung) {

        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    // QUAN LY VONG DOI MAPVIEW
    @Override
    protected void onResume() {
        super.onResume();

        if (banDo != null) {
            banDo.onResume();
        }

        if (daBatDinhVi &&
                lopViTri != null &&
                daCoQuyenViTri()) {

            lopViTri.enableMyLocation();
        }
    }

    @Override
    protected void onPause() {

        if (lopViTri != null) {
            lopViTri.disableMyLocation();
        }

        if (banDo != null) {
            banDo.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {


        if (dbKiemTra != null) {
            dbKiemTra.close();
            dbKiemTra = null;
        }

        if (lopViTri != null) {
            lopViTri.disableMyLocation();
            lopViTri = null;
        }

        if (banDo != null) {
            banDo.onDetach();
            banDo = null;
        }

        super.onDestroy();
    }

    // CHON DIA DIEM VA DI CHUYEN BAN DO
    private void chonVaDiChuyen(int chiSo) {

        if (chiSo < 0 ||
                chiSo >= TEN_DIA_DIEM.length) {
            return;
        }

        // CAP NHAT THE THONG TIN
        chonDiaDiem(chiSo);

        // LAY TOA DO DIA DIEM
        GeoPoint diemDen = new GeoPoint(
                TOA_DO[chiSo][0],
                TOA_DO[chiSo][1]
        );

        // PHONG TO KHU VUC DU LICH
        banDo.getController().setZoom(17.0);

        // DI CHUYEN DEN DIA DIEM
        banDo.getController().animateTo(
                diemDen
        );
    }

    // TIM KIEM TRONG 3 DIA DIEM HIEN CO
    private void timKiemDiaDiem(String tuKhoa) {

        String noiDung = tuKhoa
                .trim()
                .toLowerCase(
                        Locale.forLanguageTag("vi-VN")
                );

        if (noiDung.isEmpty()) {

            thongBao(
                    "Vui lòng nhập tên địa điểm."
            );

            return;
        }

        for (int i = 0; i < TEN_DIA_DIEM.length; i++) {

            String ten = TEN_DIA_DIEM[i]
                    .toLowerCase(
                            Locale.forLanguageTag("vi-VN")
                    );

            if (ten.contains(noiDung)) {

                chonVaDiChuyen(i);

                return;
            }
        }

        thongBao(
                "Không tìm thấy địa điểm phù hợp."
        );
    }

    // LAY 3 DIA DIEM TU SQLITE
    private boolean taiDiaDiemTuSQLite() {

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            ArrayList<DiaDiem> danhSach =
                    db.danhSachDiaDiem();

            boolean[] daCoDiaDiem =
                    new boolean[3];

            for (DiaDiem diaDiem : danhSach) {

                int id = diaDiem.id;

                if (id < 0 || id >= 3) {
                    continue;
                }

                TEN_DIA_DIEM[id] = diaDiem.ten;

                DIA_CHI[id] = diaDiem.diaChi;

                TOA_DO[id][0] = diaDiem.viDo;
                TOA_DO[id][1] = diaDiem.kinhDo;

                TEN_ANH[id] = diaDiem.tenAnh;

                daCoDiaDiem[id] = true;
            }

            return daCoDiaDiem[0]
                    && daCoDiaDiem[1]
                    && daCoDiaDiem[2];

        } catch (Exception e) {

            android.util.Log.e(
                    "BanDoActivity",
                    "Loi doc du lieu tu SQLite",
                    e
            );

            return false;
        }
    }

}

