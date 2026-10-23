package com.example.app_ck;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public class BanDoActivity extends BaseActivity {

    // =====================================================
    // 1. KHAI BÁO DỮ LIỆU
    // =====================================================

    private static final int YEU_CAU_VI_TRI = 101;
    private static final int[] ID_DIA_DIEM = {0, 1, 2, 3, 4};

    private int viTriTheoId(int id) {
        for (int i = 0; i < ID_DIA_DIEM.length; i++) {
            if (ID_DIA_DIEM[i] == id) return i;
        }
        return -1;
    }

    private static final int SO_DIA_DIEM = ID_DIA_DIEM.length;

    // ID phải trùng với SQLite.
    private final String[] TEN_DIA_DIEM =
            new String[SO_DIA_DIEM];

    private final String[] DIA_CHI =
            new String[SO_DIA_DIEM];

    private final double[][] TOA_DO =
            new double[SO_DIA_DIEM][2];

    private final String[] TEN_ANH =
            new String[SO_DIA_DIEM];

    private final boolean[] CO_TOA_DO =
            new boolean[SO_DIA_DIEM];

    // Ảnh dự phòng cho địa điểm chưa có ảnh.
    private final int[] ANH_DIA_DIEM = {
            R.drawable.anh_dinh_doc_lap,
            R.drawable.anh_bao_tang_chung_tich,
            R.drawable.anh_dai_noi_hue,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_gallery
    };

    // =====================================================
    // 2. KHAI BÁO GIAO DIỆN
    // =====================================================

    private MapView banDo;
    private TaiKhoanDb dbKiemTra;

    private ImageView anhDiaDiemBanDo;

    private TextView tenDiaDiem;
    private TextView diaChiDiaDiem;
    private TextView nutViTriCuaToi;

    private Button nutXemChiTiet;
    private Button nutChiDuong;

    private MyLocationNewOverlay lopViTri;

    private boolean daBatDinhVi = false;

    // Chưa chọn Marker.
    private int viTriDaChon = -1;

    // =====================================================
    // 3. KHỞI TẠO ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình osmdroid.
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
        apDungKhoangTrongHeThong();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Giữ kết nối SQLite cho Database Inspector.
        dbKiemTra = new TaiKhoanDb(this);
        dbKiemTra.getReadableDatabase();

        anhXaGiaoDien();

        caiDatSuKien();

        // Đọc đủ 5 địa điểm từ SQLite.
        if (!taiDiaDiemTuSQLite()) {

            thongBao(
                    "SQLite chưa có đủ 5 địa điểm du lịch."
            );

            finish();
            return;
        }

        khoiTaoBanDo();

        themCacMarker();

        caiDatNutChonNhanh();

        caiDatTimKiem();
    }

    // =====================================================
    // 4. ÁNH XẠ GIAO DIỆN
    // =====================================================

    private void anhXaGiaoDien() {

        banDo = findViewById(
                R.id.banDoTuongTac
        );

        anhDiaDiemBanDo = findViewById(
                R.id.anhDiaDiemBanDo
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

        // Ban đầu chưa chọn địa điểm.
        nutXemChiTiet.setEnabled(false);
        nutChiDuong.setEnabled(false);

        // Đặt nút GPS phía trên thẻ thông tin.
        View khungThongTin =
                findViewById(R.id.khungThongTinBanDo);

        if (khungThongTin == null) {
            return;
        }

        khungThongTin.addOnLayoutChangeListener(
                (v, left, top, right, bottom,
                 oldLeft, oldTop, oldRight, oldBottom) -> {

                    ViewGroup.LayoutParams thamSoGoc =
                            nutViTriCuaToi.getLayoutParams();

                    if (!(thamSoGoc
                            instanceof FrameLayout.LayoutParams)) {
                        return;
                    }

                    if (!(v.getLayoutParams()
                            instanceof ViewGroup.MarginLayoutParams)) {
                        return;
                    }

                    FrameLayout.LayoutParams thamSo =
                            (FrameLayout.LayoutParams) thamSoGoc;

                    ViewGroup.MarginLayoutParams thamSoKhung =
                            (ViewGroup.MarginLayoutParams)
                                    v.getLayoutParams();

                    int khoangCachCan =
                            v.getHeight()
                                    + thamSoKhung.bottomMargin
                                    + dp(12);

                    if (thamSo.bottomMargin != khoangCachCan) {

                        thamSo.bottomMargin = khoangCachCan;

                        nutViTriCuaToi.setLayoutParams(
                                thamSo
                        );
                    }
                }
        );
    }

    private int dp(int giaTri) {

        return (int) (
                giaTri
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    // =====================================================
    // 5. XỬ LÝ CÁC NÚT
    // =====================================================

    private void caiDatSuKien() {

        findViewById(R.id.nutQuayLaiBanDo)
                .setOnClickListener(
                        v -> finish()
                );

        nutXemChiTiet.setOnClickListener(
                v -> moChiTietDiaDiem()
        );

        nutChiDuong.setOnClickListener(
                v -> moChiDuong()
        );

        nutViTriCuaToi.setOnClickListener(
                v -> kiemTraQuyenViTri()
        );

        findViewById(R.id.nutPhongToBanDo)
                .setOnClickListener(v ->
                        banDo.getController().zoomIn()
                );

        findViewById(R.id.nutThuNhoBanDo)
                .setOnClickListener(v ->
                        banDo.getController().zoomOut()
                );
    }

    // =====================================================
    // 6. ĐỌC 4 ĐỊA ĐIỂM TỪ SQLITE
    // =====================================================

    private boolean taiDiaDiemTuSQLite() {

        try {

            ArrayList<DiaDiem> danhSach =
                    dbKiemTra.danhSachDiaDiem();

            boolean[] daCoDiaDiem =
                    new boolean[SO_DIA_DIEM];

            for (DiaDiem diaDiem : danhSach) {

                int id = viTriTheoId(diaDiem.id);

                if (id < 0 || id >= SO_DIA_DIEM) {
                    continue;
                }

                TEN_DIA_DIEM[id] = diaDiem.ten;

                DIA_CHI[id] = diaDiem.diaChi;

                TEN_ANH[id] = diaDiem.tenAnh;

                if (Double.isFinite(diaDiem.viDo)
                        && Double.isFinite(diaDiem.kinhDo)
                        && diaDiem.viDo >= -90
                        && diaDiem.viDo <= 90
                        && diaDiem.kinhDo >= -180
                        && diaDiem.kinhDo <= 180
                        && !(diaDiem.viDo == 0
                        && diaDiem.kinhDo == 0)) {

                    TOA_DO[id][0] = diaDiem.viDo;
                    TOA_DO[id][1] = diaDiem.kinhDo;

                    CO_TOA_DO[id] = true;
                }

                // Tìm ảnh có tên tương ứng trong drawable.
                if (diaDiem.tenAnh != null
                        && !diaDiem.tenAnh.trim().isEmpty()) {

                    int maAnh = getResources().getIdentifier(
                            diaDiem.tenAnh,
                            "drawable",
                            getPackageName()
                    );

                    if (maAnh != 0) {
                        ANH_DIA_DIEM[id] = maAnh;
                    }
                }

                daCoDiaDiem[id] = true;
            }

            for (int i = 0; i < SO_DIA_DIEM; i++) {

                if (!daCoDiaDiem[i]) {
                    return false;
                }

                if (TEN_DIA_DIEM[i] == null
                        || TEN_DIA_DIEM[i].trim().isEmpty()) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {

            android.util.Log.e(
                    "BanDoActivity",
                    "Không đọc được dữ liệu SQLite",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // 7. KHỞI TẠO OPENSTREETMAP
    // =====================================================

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

        // Cho phép xem cả miền Bắc và miền Nam.
        banDo.setMinZoomLevel(4.0);
        banDo.setMaxZoomLevel(19.0);

        // Ban đầu hiển thị bản đồ Việt Nam.
        GeoPoint trungTam = new GeoPoint(
                15.5,
                107.5
        );

        banDo.getController().setZoom(5.5);

        banDo.getController().setCenter(
                trungTam
        );

        banDo.invalidate();
    }

    // =====================================================
    // 8. HIỂN THỊ MARKER
    // =====================================================

    private void themCacMarker() {

        for (int i = 0; i < SO_DIA_DIEM; i++) {

            if (!CO_TOA_DO[i]) {
                continue;
            }

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
                    DIA_CHI[i] == null
                            ? ""
                            : DIA_CHI[i]
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

    // =====================================================
    // 9. CHỌN ĐỊA ĐIỂM
    // =====================================================

    private void chonDiaDiem(int chiSo) {

        if (chiSo < 0 || chiSo >= SO_DIA_DIEM) {
            return;
        }

        viTriDaChon = chiSo;

        anhDiaDiemBanDo.setImageResource(
                ANH_DIA_DIEM[chiSo]
        );

        tenDiaDiem.setText(
                TEN_DIA_DIEM[chiSo]
        );

        capNhatKhoangCach();

        nutXemChiTiet.setEnabled(true);

        nutChiDuong.setEnabled(true);
    }

    private void chonVaDiChuyen(int chiSo) {

        if (chiSo < 0 || chiSo >= SO_DIA_DIEM) {
            return;
        }

        chonDiaDiem(chiSo);

        if (!CO_TOA_DO[chiSo]) {

            thongBao(
                    "Địa điểm chưa có tọa độ GPS hợp lệ."
            );

            return;
        }

        GeoPoint diemDen = new GeoPoint(
                TOA_DO[chiSo][0],
                TOA_DO[chiSo][1]
        );

        banDo.getController().setZoom(17.0);

        banDo.getController().animateTo(
                diemDen
        );
    }

    // =====================================================
    // 10. NÚT CHỌN NHANH
    // =====================================================

    private void caiDatNutChonNhanh() {

        // Hai nút cũ đã có trong XML.
        ganNutChonNhanh(
                R.id.nutChonDinhDocLap,
                0
        );

        ganNutChonNhanh(
                R.id.nutChonBaoTang,
                1
        );

        // Tìm nút mới theo tên để tránh lỗi
        // khi XML chưa khai báo các ID này.

        ganNutChonNhanhTheoTen(
                "nutChonDaiNoiHue",
                2
        );

        ganNutChonNhanhTheoTen(
                "nutChonConDao",
                3
        );

        ganNutChonNhanhTheoTen(
                "nutChonVanMieu",
                4
        );
    }

    private void ganNutChonNhanh(
            int idNut,
            int chiSoDiaDiem
    ) {

        View nut = findViewById(idNut);

        if (nut != null) {

            nut.setOnClickListener(
                    v -> chonVaDiChuyen(chiSoDiaDiem)
            );
        }
    }

    private void ganNutChonNhanhTheoTen(
            String tenId,
            int chiSoDiaDiem
    ) {

        int idNut = getResources().getIdentifier(
                tenId,
                "id",
                getPackageName()
        );

        if (idNut == 0) {
            return;
        }

        ganNutChonNhanh(
                idNut,
                chiSoDiaDiem
        );
    }

    // =====================================================
    // 11. TÌM KIẾM ĐỊA ĐIỂM
    // =====================================================

    private void caiDatTimKiem() {

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

    private String chuanHoa(String noiDung) {

        if (noiDung == null) {
            return "";
        }

        String chuoi = Normalizer.normalize(
                noiDung.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        chuoi = chuoi.replaceAll("\\p{M}+", "");

        return chuoi.replace('đ', 'd')
                .trim()
                .replaceAll("\\s+", " ");
    }

    private void timKiemDiaDiem(String tuKhoa) {

        String noiDung = chuanHoa(tuKhoa);

        if (noiDung.isEmpty()) {

            thongBao(
                    "Vui lòng nhập tên địa điểm."
            );

            return;
        }

        for (int i = 0; i < SO_DIA_DIEM; i++) {

            String ten = chuanHoa(
                    TEN_DIA_DIEM[i]
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

    // =====================================================
    // 12. KIỂM TRA QUYỀN GPS
    // =====================================================

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

    // =====================================================
    // 13. BẬT ĐỊNH VỊ
    // =====================================================

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

        boolean dangLayViTri =
                lopViTri.enableMyLocation();

        if (!dangLayViTri) {

            thongBao(
                    "Không thể khởi động định vị. " +
                            "Hãy kiểm tra GPS."
            );

            return;
        }

        daBatDinhVi = true;

        GeoPoint viTriHienTai =
                lopViTri.getMyLocation();

        if (viTriHienTai != null) {

            diChuyenDenViTri(
                    viTriHienTai
            );

            return;
        }

        thongBao(
                "Đang xác định vị trí của bạn..."
        );

        lopViTri.runOnFirstFix(() ->

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

                        diChuyenDenViTri(
                                viTriMoi
                        );
                    }
                })
        );
    }

    // =====================================================
    // 14. HIỂN THỊ VỊ TRÍ VÀ KHOẢNG CÁCH
    // =====================================================

    private void diChuyenDenViTri(
            GeoPoint viTri
    ) {

        banDo.getController().setZoom(17.0);

        banDo.getController().animateTo(
                viTri
        );

        capNhatKhoangCach();
    }

    private void capNhatKhoangCach() {

        if (viTriDaChon < 0) {
            return;
        }

        String diaChi =
                DIA_CHI[viTriDaChon] == null
                        ? ""
                        : DIA_CHI[viTriDaChon];

        String noiDung = "⌖  " + diaChi;

        if (!CO_TOA_DO[viTriDaChon]) {

            diaChiDiaDiem.setText(noiDung);
            return;
        }

        if (lopViTri == null || !daBatDinhVi) {

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

    // =====================================================
    // 15. MỞ CHI TIẾT ĐỊA ĐIỂM
    // =====================================================

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
                ID_DIA_DIEM[viTriDaChon]
        );

        startActivity(intent);
    }

    // =====================================================
    // 16. CHỈ ĐƯỜNG GOOGLE MAPS
    // =====================================================

    private void moChiDuong() {

        if (viTriDaChon < 0) {

            thongBao(
                    "Vui lòng chọn địa điểm cần đến."
            );

            return;
        }

        String dichDen;

        if (CO_TOA_DO[viTriDaChon]) {

            dichDen =
                    TOA_DO[viTriDaChon][0]
                            + ","
                            + TOA_DO[viTriDaChon][1];

        } else {

            // Tìm theo tên nếu chưa có tọa độ.
            dichDen = Uri.encode(
                    TEN_DIA_DIEM[viTriDaChon]
                            + ", "
                            + DIA_CHI[viTriDaChon]
            );
        }

        Uri duongDan = Uri.parse(
                "https://www.google.com/maps/dir/"
                        + "?api=1&destination="
                        + dichDen
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

    // =====================================================
    // 17. THÔNG BÁO
    // =====================================================

    private void thongBao(String noiDung) {

        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    // =====================================================
    // 18. VÒNG ĐỜI BẢN ĐỒ
    // =====================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (banDo != null) {
            banDo.onResume();
        }

        if (daBatDinhVi
                && lopViTri != null
                && daCoQuyenViTri()) {

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

        if (lopViTri != null) {

            lopViTri.disableMyLocation();
            lopViTri = null;
        }

        if (banDo != null) {

            banDo.onDetach();
            banDo = null;
        }

        if (dbKiemTra != null) {

            dbKiemTra.close();
            dbKiemTra = null;
        }

        super.onDestroy();
    }
}
