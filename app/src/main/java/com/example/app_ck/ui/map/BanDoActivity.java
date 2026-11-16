
package com.example.app_ck.ui.map;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.ThanhDieuHuongDuoi;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class BanDoActivity extends BaseActivity {

    // =================================================
    // 1. HANG SO
    // =================================================

    private static final int MA_QUYEN_GPS = 101;

    private static final float BAN_KINH_GAN_MET = 30000f;

    private static final long THOI_GIAN_CHO_GPS = 18000L;

    private static final String KHOA_DIA_DIEM =
            "dia_diem_duoc_chon";

    // =================================================
    // 2. GIAO DIEN
    // =================================================

    private MapView banDo;

    private View theDiaDiem;

    private ImageView anhDiaDiem;

    private TextView tenDiaDiem;
    private TextView diaChiDiaDiem;
    private TextView nutViTriCuaToi;
    private TextView soDiaDiem;

    private EditText oTimKiem;

    private Button nutXemChiTiet;
    private Button nutChiDuong;

    // =================================================
    // 3. DU LIEU
    // =================================================

    private final ArrayList<DiaDiem> cacDiaDiem =
            new ArrayList<>();

    private final ArrayList<Marker> cacMarker =
            new ArrayList<>();

    private int idDangChon = -1;

    private boolean daQuaLanResumeDau = false;

    // =================================================
    // 4. GPS
    // =================================================

    private MyLocationNewOverlay lopViTri;

    private boolean daBatGPS = false;

    private boolean dangXinQuyen = false;

    private boolean dangChoGPS = false;

    private boolean muonMoGanToi = false;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable hetHanGPS = () -> {

        if (!dangChoGPS) {
            return;
        }

        dangChoGPS = false;
        muonMoGanToi = false;

        thongBao(
                "Chưa xác định được vị trí. "
                        + "Hãy kiểm tra GPS và thử lại."
        );
    };

    private BottomSheetDialog bangDanhSachDangMo;

    // =================================================
    // 5. ON CREATE
    // =================================================

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        // Cau hinh OpenStreetMap
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences(
                        "cau_hinh_ban_do",
                        MODE_PRIVATE
                )
        );

        Configuration.getInstance().setUserAgentValue(
                "APP_CK/1.0 (com.example.app_ck)"
        );

        setContentView(R.layout.activity_ban_do);

        apDungKhoangTrongHeThong();

        // Thanh dieu huong chung
        ThanhDieuHuongDuoi.gan(
                this,
                ThanhDieuHuongDuoi.BAN_DO
        );

        anhXaGiaoDien();

        khoiTaoBanDo();

        caiDatSuKien();

        caiDatViTriNutBanDo();

        if (state != null) {

            idDangChon = state.getInt(
                    KHOA_DIA_DIEM,
                    -1
            );
        }

        if (!taiDuLieuVaMarker()) {

            thongBao(
                    "Không thể đọc dữ liệu địa điểm."
            );
        }

        if (idDangChon >= 0) {

            chonDiaDiem(
                    idDangChon,
                    true
            );

        } else {

            hienThiTatCaBanDo();
        }
    }

    // =================================================
    // 6. ANH XA
    // =================================================

    private void anhXaGiaoDien() {

        banDo = findViewById(
                R.id.banDoTuongTac
        );

        theDiaDiem = findViewById(
                R.id.khungThongTinBanDo
        );

        anhDiaDiem = findViewById(
                R.id.anhDiaDiemBanDo
        );

        tenDiaDiem = findViewById(
                R.id.tenDiaDiemBanDo
        );

        diaChiDiaDiem = findViewById(
                R.id.diaChiBanDo
        );

        nutViTriCuaToi = findViewById(
                R.id.nutViTriCuaToi
        );

        soDiaDiem = findViewById(
                R.id.banDoSoDiem
        );

        oTimKiem = findViewById(
                R.id.oTimKiemBanDo
        );

        nutXemChiTiet = findViewById(
                R.id.nutXemChiTietBanDo
        );

        nutChiDuong = findViewById(
                R.id.nutChiDuongBanDo
        );

        // Chua chon dia diem thi an the
        theDiaDiem.setVisibility(View.GONE);

        nutXemChiTiet.setEnabled(false);
        nutChiDuong.setEnabled(false);
    }

    private int dp(float giaTri) {

        return Math.round(
                giaTri
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =================================================
    // 7. BAN DO OPENSTREETMAP
    // =================================================

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

        // Su dung hai nut zoom rieng cua ung dung
        banDo.setBuiltInZoomControls(false);

        banDo.setMinZoomLevel(4.0);
        banDo.setMaxZoomLevel(19.0);

        banDo.getController().setZoom(5.5);

        banDo.getController().setCenter(
                new GeoPoint(
                        15.5,
                        107.5
                )
        );
    }

    // =================================================
    // 8. SU KIEN
    // =================================================

    private void caiDatSuKien() {

        // Xem chi tiet dia diem
        nutXemChiTiet.setOnClickListener(
                v -> moChiTietDiaDiem()
        );

        // Chi duong
        nutChiDuong.setOnClickListener(
                v -> moChiDuong()
        );

        // Dong the dia diem
        findViewById(R.id.nutDongTheBanDo)
                .setOnClickListener(
                        v -> boChonDiaDiem()
                );

        // Phong to
        findViewById(R.id.nutPhongToBanDo)
                .setOnClickListener(
                        v -> banDo.getController().zoomIn()
                );

        // Thu nho
        findViewById(R.id.nutThuNhoBanDo)
                .setOnClickListener(
                        v -> banDo.getController().zoomOut()
                );

        // Vi tri hien tai
        nutViTriCuaToi.setOnClickListener(
                v -> batDinhVi(false)
        );

        // Tat ca marker
        findViewById(R.id.nutTatCaBanDo)
                .setOnClickListener(
                        v -> hienThiTatCaBanDo()
                );

        // Danh sach dia diem
        findViewById(R.id.nutDanhSachBanDo)
                .setOnClickListener(
                        v -> moDanhSachBanDo()
                );

        // Dia diem gan toi
        findViewById(R.id.nutGanToiBanDo)
                .setOnClickListener(
                        v -> batDinhVi(true)
                );

        // Tim kiem
        oTimKiem.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            != EditorInfo.IME_ACTION_SEARCH) {

                        return false;
                    }

                    anBanPhim();

                    timKiemDiaDiem(
                            oTimKiem.getText()
                                    .toString()
                    );

                    return true;
                }
        );
    }

    private void anBanPhim() {

        InputMethodManager banPhim =
                (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);

        if (banPhim != null) {

            banPhim.hideSoftInputFromWindow(
                    oTimKiem.getWindowToken(),
                    0
            );
        }

        oTimKiem.clearFocus();
    }

    // =================================================
    // 9. DOC SQLITE VA TAO MARKER
    // =================================================

    private boolean taiDuLieuVaMarker() {

        try (AppDatabase db =
                     new AppDatabase(this)) {

            ArrayList<DiaDiem> duLieuMoi =
                    db.danhSachDiaDiem();

            if (duLieuMoi == null) {
                return false;
            }

            // Chi cap nhat khi doc thanh cong
            cacDiaDiem.clear();
            cacDiaDiem.addAll(duLieuMoi);

            soDiaDiem.setText(
                    cacDiaDiem.size()
                            + " địa điểm"
            );

            // Xoa marker cu, khong xoa GPS overlay
            banDo.getOverlays().removeAll(
                    cacMarker
            );

            cacMarker.clear();

            for (DiaDiem diaDiem : cacDiaDiem) {

                if (!coToaDo(diaDiem)) {
                    continue;
                }

                Marker marker = new Marker(banDo);

                marker.setId(
                        String.valueOf(diaDiem.id)
                );

                marker.setPosition(
                        new GeoPoint(
                                diaDiem.viDo,
                                diaDiem.kinhDo
                        )
                );

                marker.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                );

                marker.setTitle(
                        diaDiem.ten
                );

                marker.setSnippet(
                        diaDiem.diaChi == null
                                ? ""
                                : diaDiem.diaChi
                );

                final int id = diaDiem.id;

                marker.setOnMarkerClickListener(
                        (markerDuocChon, mapView) -> {

                            chonDiaDiem(
                                    id,
                                    true
                            );

                            return true;
                        }
                );

                cacMarker.add(marker);

                banDo.getOverlays().add(marker);
            }

            banDo.invalidate();

            return true;

        } catch (Exception e) {

            android.util.Log.e(
                    "BanDoActivity",
                    "Loi doc du lieu SQLite",
                    e
            );

            return false;
        }
    }

    private boolean coToaDo(DiaDiem diaDiem) {

        if (diaDiem == null) {
            return false;
        }

        return Double.isFinite(diaDiem.viDo)
                && Double.isFinite(diaDiem.kinhDo)
                && Math.abs(diaDiem.viDo) <= 90
                && Math.abs(diaDiem.kinhDo) <= 180
                && !(diaDiem.viDo == 0
                && diaDiem.kinhDo == 0);
    }

    private DiaDiem timDiaDiemTheoId(int id) {

        for (DiaDiem d : cacDiaDiem) {

            if (d.id == id) {
                return d;
            }
        }

        return null;
    }

    // =================================================
    // 10. CHON DIA DIEM
    // =================================================

    private void chonDiaDiem(
            int id,
            boolean diChuyen
    ) {

        DiaDiem diaDiem = timDiaDiemTheoId(id);

        if (diaDiem == null) {

            boChonDiaDiem();

            return;
        }

        idDangChon = diaDiem.id;

        AnhDiaDiem.hien(
                anhDiaDiem,
                diaDiem.tenAnh
        );

        tenDiaDiem.setText(
                diaDiem.ten
        );

        nutXemChiTiet.setEnabled(true);
        nutChiDuong.setEnabled(true);

        theDiaDiem.setVisibility(
                View.VISIBLE
        );

        capNhatKhoangCach();

        // Dua nut GPS len tren the thong tin
        theDiaDiem.post(
                this::capNhatViTriNutBanDo
        );

        if (diChuyen) {

            if (coToaDo(diaDiem)) {

                banDo.getController()
                        .setZoom(15.0);

                banDo.getController()
                        .animateTo(
                                new GeoPoint(
                                        diaDiem.viDo,
                                        diaDiem.kinhDo
                                )
                        );

            } else {

                thongBao(
                        "Địa điểm chưa có tọa độ "
                                + "để hiển thị marker."
                );
            }
        }
    }

    private void boChonDiaDiem() {

        idDangChon = -1;

        theDiaDiem.setVisibility(
                View.GONE
        );

        nutXemChiTiet.setEnabled(false);
        nutChiDuong.setEnabled(false);

        theDiaDiem.post(
                this::capNhatViTriNutBanDo
        );
    }

    private void hienThiDiaDiemDangChon() {

        if (idDangChon < 0) {

            boChonDiaDiem();

        } else {

            chonDiaDiem(
                    idDangChon,
                    false
            );
        }
    }

    // =================================================
    // 11. DIEU CHINH NUT NOI
    // =================================================

    private void caiDatViTriNutBanDo() {

        theDiaDiem.addOnLayoutChangeListener(
                (v, left, top, right, bottom,
                 oldLeft, oldTop, oldRight, oldBottom) -> {

                    capNhatViTriNutBanDo();
                }
        );

        theDiaDiem.post(
                this::capNhatViTriNutBanDo
        );
    }

    private void capNhatViTriNutBanDo() {

        if (theDiaDiem == null) {
            return;
        }

        int khoangDuoi = dp(16);

        if (theDiaDiem.getVisibility()
                == View.VISIBLE) {

            int chieuCaoThe =
                    theDiaDiem.getHeight();

            if (chieuCaoThe == 0) {
                return;
            }

            ViewGroup.LayoutParams lp =
                    theDiaDiem.getLayoutParams();

            int marginDuoi = 0;

            if (lp instanceof
                    ViewGroup.MarginLayoutParams) {

                marginDuoi =
                        ((ViewGroup.MarginLayoutParams) lp)
                                .bottomMargin;
            }

            khoangDuoi =
                    chieuCaoThe
                            + marginDuoi
                            + dp(12);
        }

        int[] cacId = {
                R.id.nutViTriCuaToi,
                R.id.bdNguonBanDo
        };

        for (int id : cacId) {

            View view = findViewById(id);

            if (view == null) {
                continue;
            }

            ViewGroup.LayoutParams goc =
                    view.getLayoutParams();

            if (!(goc instanceof
                    FrameLayout.LayoutParams)) {

                continue;
            }

            FrameLayout.LayoutParams thamSo =
                    (FrameLayout.LayoutParams) goc;

            if (thamSo.bottomMargin != khoangDuoi) {

                thamSo.bottomMargin = khoangDuoi;

                view.setLayoutParams(thamSo);
            }
        }
    }

    // =================================================
    // 12. XEM TAT CA MARKER
    // =================================================

    private void hienThiTatCaBanDo() {

        boChonDiaDiem();

        ArrayList<GeoPoint> cacToaDo =
                new ArrayList<>();

        for (DiaDiem d : cacDiaDiem) {

            if (coToaDo(d)) {

                cacToaDo.add(
                        new GeoPoint(
                                d.viDo,
                                d.kinhDo
                        )
                );
            }
        }

        if (cacToaDo.isEmpty()) {

            banDo.getController()
                    .setZoom(5.5);

            banDo.getController()
                    .animateTo(
                            new GeoPoint(
                                    15.5,
                                    107.5
                            )
                    );

            return;
        }

        if (cacToaDo.size() == 1) {

            banDo.getController()
                    .setZoom(12.0);

            banDo.getController()
                    .animateTo(
                            cacToaDo.get(0)
                    );

            return;
        }

        BoundingBox gioiHan =
                BoundingBox.fromGeoPoints(cacToaDo);

        banDo.post(() -> {

            if (isFinishing()
                    || isDestroyed()
                    || banDo == null) {
                return;
            }

            banDo.zoomToBoundingBox(
                    gioiHan,
                    true,
                    dp(65)
            );
        });
    }

    // =================================================
    // 13. BOTTOM SHEET DANH SACH
    // =================================================

    private void moBangDanhSach(
            String tieuDe,
            String moTa,
            ArrayList<DiaDiem> duLieu,
            HashMap<Integer, Float> khoangCach
    ) {

        if (duLieu == null || duLieu.isEmpty()) {

            thongBao(
                    "Chưa có địa điểm để hiển thị."
            );

            return;
        }

        // Dong bang cu neu dang mo
        if (bangDanhSachDangMo != null) {

            bangDanhSachDangMo.dismiss();
        }

        BottomSheetDialog bang =
                new BottomSheetDialog(this);

        View giaoDien =
                getLayoutInflater().inflate(
                        R.layout.view_bd_danh_sach,
                        null
                );

        bang.setContentView(giaoDien);

        bangDanhSachDangMo = bang;

        TextView tieuDeBang =
                giaoDien.findViewById(
                        R.id.bdSheetTieuDe
                );

        TextView moTaBang =
                giaoDien.findViewById(
                        R.id.bdSheetMoTa
                );

        LinearLayout danhSach =
                giaoDien.findViewById(
                        R.id.bdSheetDanhSach
                );

        ScrollView vungCuon =
                giaoDien.findViewById(
                        R.id.bdSheetCuon
                );

        tieuDeBang.setText(tieuDe);
        moTaBang.setText(moTa);

        danhSach.removeAllViews();

        for (DiaDiem diaDiem : duLieu) {

            View the =
                    getLayoutInflater().inflate(
                            R.layout.item_bd_dia_diem,
                            danhSach,
                            false
                    );

            ImageView anh =
                    the.findViewById(
                            R.id.bdItemAnh
                    );

            TextView ten =
                    the.findViewById(
                            R.id.bdItemTen
                    );

            TextView diaChi =
                    the.findViewById(
                            R.id.bdItemDiaChi
                    );

            TextView khoangCachView =
                    the.findViewById(
                            R.id.bdItemKhoangCach
                    );

            AnhDiaDiem.hien(
                    anh,
                    diaDiem.tenAnh
            );

            ten.setText(
                    diaDiem.ten
            );

            String noiDungDiaChi =
                    diaDiem.diaChi == null
                            ? ""
                            : diaDiem.diaChi.trim();

            if (noiDungDiaChi.isEmpty()) {

                noiDungDiaChi =
                        "Chưa có địa chỉ";
            }

            if (!coToaDo(diaDiem)) {

                noiDungDiaChi +=
                        " · Chưa có tọa độ";
            }

            diaChi.setText(
                    noiDungDiaChi
            );

            // Hien khoang cach neu co GPS
            if (khoangCach != null
                    && khoangCach.containsKey(
                    diaDiem.id
            )) {

                float met =
                        khoangCach.get(
                                diaDiem.id
                        );

                khoangCachView.setText(
                        dinhDangKhoangCach(met)
                                + " · đường chim bay"
                );

                khoangCachView.setVisibility(
                        View.VISIBLE
                );

            } else {

                khoangCachView.setVisibility(
                        View.GONE
                );
            }

            the.setContentDescription(
                    "Xem địa điểm "
                            + diaDiem.ten
            );

            the.setOnClickListener(v -> {

                bang.dismiss();

                // Chon marker va hien the thong tin
                chonDiaDiem(
                        diaDiem.id,
                        true
                );
            });

            danhSach.addView(the);
        }

        // Gioi han chieu cao bang danh sach
        int gioiHan = Math.min(
                dp(460),
                Math.round(
                        getResources()
                                .getDisplayMetrics()
                                .heightPixels * 0.58f
                )
        );

        int chieuCaoDuKien =
                duLieu.size() * dp(106);

        int chieuCao = Math.min(
                gioiHan,
                Math.max(
                        dp(106),
                        chieuCaoDuKien
                )
        );

        ViewGroup.LayoutParams thamSo =
                vungCuon.getLayoutParams();

        thamSo.height = chieuCao;

        vungCuon.setLayoutParams(thamSo);

        // Giu nen Bottom Sheet bo tron
        bang.setOnShowListener(dialog -> {

            FrameLayout khung =
                    bang.findViewById(
                            com.google.android.material.R.id
                                    .design_bottom_sheet
                    );

            if (khung != null) {

                khung.setBackgroundColor(
                        Color.TRANSPARENT
                );
            }
        });

        bang.setOnDismissListener(dialog -> {

            if (bangDanhSachDangMo == bang) {

                bangDanhSachDangMo = null;
            }
        });

        bang.show();
    }

    // =================================================
    // 14. DANH SACH TAT CA DIA DIEM
    // =================================================

    private void moDanhSachBanDo() {

        moBangDanhSach(
                "Tất cả địa điểm",
                cacDiaDiem.size()
                        + " địa điểm để khám phá",
                new ArrayList<>(cacDiaDiem),
                null
        );
    }

    // =================================================
    // 15. TIM KIEM
    // =================================================

    private void timKiemDiaDiem(
            String tuKhoa
    ) {

        String noiDung =
                chuanHoa(tuKhoa);

        if (noiDung.isEmpty()) {

            thongBao(
                    "Vui lòng nhập tên hoặc địa chỉ."
            );

            return;
        }

        ArrayList<DiaDiem> ketQua =
                new ArrayList<>();

        for (DiaDiem d : cacDiaDiem) {

            if (chuanHoa(d.ten).contains(noiDung)
                    || chuanHoa(d.diaChi)
                    .contains(noiDung)) {

                ketQua.add(d);
            }
        }

        if (ketQua.isEmpty()) {

            thongBao(
                    "Không tìm thấy địa điểm phù hợp."
            );

            return;
        }

        if (ketQua.size() == 1) {

            chonDiaDiem(
                    ketQua.get(0).id,
                    true
            );

            return;
        }

        // Nhieu ket qua: dung Bottom Sheet
        moBangDanhSach(
                "Kết quả tìm kiếm",
                ketQua.size()
                        + " địa điểm phù hợp",
                ketQua,
                null
        );
    }

    private String chuanHoa(String noiDung) {

        if (noiDung == null) {
            return "";
        }

        String chuoi = Normalizer.normalize(
                noiDung.toLowerCase(
                        Locale.ROOT
                ),
                Normalizer.Form.NFD
        );

        return chuoi
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .trim()
                .replaceAll("\\s+", " ");
    }

    // =================================================
    // 16. QUYEN GPS
    // =================================================

    private boolean daCoQuyenViTri() {

        boolean quyenChinhXac =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean quyenGanDung =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        return quyenChinhXac || quyenGanDung;
    }

    private void batDinhVi(
            boolean ganToi
    ) {

        muonMoGanToi = ganToi;

        // Chua co quyen thi xin quyen
        if (!daCoQuyenViTri()) {

            dangXinQuyen = true;

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission
                                    .ACCESS_FINE_LOCATION,
                            Manifest.permission
                                    .ACCESS_COARSE_LOCATION
                    },
                    MA_QUYEN_GPS
            );

            return;
        }

        if (lopViTri == null) {

            lopViTri = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this),
                    banDo
            );

            banDo.getOverlays().add(
                    lopViTri
            );
        }

        boolean batThanhCong =
                lopViTri.enableMyLocation();

        if (!batThanhCong) {

            muonMoGanToi = false;

            thongBao(
                    "Không thể bật GPS. "
                            + "Hãy kiểm tra định vị thiết bị."
            );

            return;
        }

        daBatGPS = true;

        GeoPoint viTriHienTai =
                lopViTri.getMyLocation();

        if (viTriHienTai != null) {

            huyChoGPS();

            if (ganToi) {

                hienThiGanToi(
                        viTriHienTai
                );

            } else {

                diChuyenDenViTri(
                        viTriHienTai
                );
            }

            muonMoGanToi = false;

            return;
        }

        // Neu da cho fix GPS thi khong tao callback moi
        if (dangChoGPS) {
            return;
        }

        dangChoGPS = true;

        thongBao(
                "Đang xác định vị trí của bạn..."
        );

        handler.removeCallbacks(hetHanGPS);

        handler.postDelayed(
                hetHanGPS,
                THOI_GIAN_CHO_GPS
        );

        lopViTri.runOnFirstFix(
                () -> runOnUiThread(() -> {

                    if (!dangChoGPS
                            || isFinishing()
                            || isDestroyed()
                            || lopViTri == null) {

                        return;
                    }

                    dangChoGPS = false;

                    handler.removeCallbacks(
                            hetHanGPS
                    );

                    GeoPoint viTriMoi =
                            lopViTri.getMyLocation();

                    if (viTriMoi == null) {

                        muonMoGanToi = false;

                        thongBao(
                                "Chưa lấy được vị trí. "
                                        + "Hãy thử lại."
                        );

                        return;
                    }

                    if (muonMoGanToi) {

                        hienThiGanToi(
                                viTriMoi
                        );

                    } else {

                        diChuyenDenViTri(
                                viTriMoi
                        );
                    }

                    muonMoGanToi = false;
                })
        );
    }

    private void huyChoGPS() {

        handler.removeCallbacks(
                hetHanGPS
        );

        dangChoGPS = false;
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

        if (requestCode != MA_QUYEN_GPS) {
            return;
        }

        dangXinQuyen = false;

        if (daCoQuyenViTri()) {

            // Tu tiep tuc chuc nang dang cho
            batDinhVi(
                    muonMoGanToi
            );

        } else {

            muonMoGanToi = false;

            thongBao(
                    "Bạn chưa cấp quyền truy cập vị trí."
            );
        }
    }

    // =================================================
    // 17. DI CHUYEN DEN VI TRI THIET BI
    // =================================================

    private void diChuyenDenViTri(
            GeoPoint viTri
    ) {

        banDo.getController()
                .setZoom(15.5);

        banDo.getController()
                .animateTo(viTri);

        capNhatKhoangCach();
    }

    // =================================================
    // 18. KHOANG CACH
    // =================================================

    private void capNhatKhoangCach() {

        DiaDiem diaDiem =
                timDiaDiemTheoId(
                        idDangChon
                );

        if (diaDiem == null) {
            return;
        }

        String diaChi =
                diaDiem.diaChi == null
                        ? ""
                        : diaDiem.diaChi;

        GeoPoint viTriNguoiDung =
                lopViTri == null || !daBatGPS
                        ? null
                        : lopViTri.getMyLocation();

        if (viTriNguoiDung == null
                || !coToaDo(diaDiem)) {

            diaChiDiaDiem.setText(
                    diaChi
            );

            return;
        }

        float[] ketQua = new float[1];

        Location.distanceBetween(
                viTriNguoiDung.getLatitude(),
                viTriNguoiDung.getLongitude(),
                diaDiem.viDo,
                diaDiem.kinhDo,
                ketQua
        );

        diaChiDiaDiem.setText(
                diaChi
                        + "\nCách bạn "
                        + dinhDangKhoangCach(
                        ketQua[0]
                )
                        + " (đường chim bay)"
        );
    }

    private String dinhDangKhoangCach(
            float met
    ) {

        if (met < 1000) {

            return String.format(
                    Locale.forLanguageTag("vi-VN"),
                    "%.0f m",
                    met
            );
        }

        return String.format(
                Locale.forLanguageTag("vi-VN"),
                "%.1f km",
                met / 1000f
        );
    }

    // =================================================
    // 19. GAN TOI
    // =================================================

    private void hienThiGanToi(
            GeoPoint viTri
    ) {

        ArrayList<DiaDiem> diaDiemHopLe =
                new ArrayList<>();

        HashMap<Integer, Float> khoangCach =
                new HashMap<>();

        // Tinh khoang cach tu vi tri thiet bi
        for (DiaDiem d : cacDiaDiem) {

            if (!coToaDo(d)) {
                continue;
            }

            float[] ketQua = new float[1];

            Location.distanceBetween(
                    viTri.getLatitude(),
                    viTri.getLongitude(),
                    d.viDo,
                    d.kinhDo,
                    ketQua
            );

            diaDiemHopLe.add(d);

            khoangCach.put(
                    d.id,
                    ketQua[0]
            );
        }

        if (diaDiemHopLe.isEmpty()) {

            thongBao(
                    "Chưa có địa điểm có tọa độ hợp lệ."
            );

            return;
        }

        // Sap xep tang dan theo khoang cach
        diaDiemHopLe.sort(
                Comparator.comparingDouble(
                        d -> khoangCach.get(d.id)
                )
        );

        ArrayList<DiaDiem> ganToi =
                new ArrayList<>();

        for (DiaDiem d : diaDiemHopLe) {

            if (khoangCach.get(d.id)
                    <= BAN_KINH_GAN_MET) {

                ganToi.add(d);
            }
        }

        boolean coDiaDiemGan =
                !ganToi.isEmpty();

        if (!coDiaDiemGan) {

            // Khong co diem trong 30 km
            // Hien thi toi da 5 dia diem gan nhat
            ganToi.addAll(
                    diaDiemHopLe.subList(
                            0,
                            Math.min(
                                    5,
                                    diaDiemHopLe.size()
                            )
                    )
            );
        }

        String moTa;

        if (coDiaDiemGan) {

            moTa = ganToi.size()
                    + " địa điểm trong phạm vi 30 km"
                    + " · khoảng cách đường chim bay";

        } else {

            moTa = "Chưa có địa điểm trong 30 km. "
                    + "Hiển thị các điểm gần nhất.";
        }

        moBangDanhSach(
                "Địa điểm gần bạn",
                moTa,
                ganToi,
                khoangCach
        );
    }

    // =================================================
    // 20. MO CHI TIET DIA DIEM
    // =================================================

    private void moChiTietDiaDiem() {

        DiaDiem diaDiem =
                timDiaDiemTheoId(
                        idDangChon
                );

        if (diaDiem == null) {

            thongBao(
                    "Vui lòng chọn địa điểm."
            );

            return;
        }

        Intent intent = new Intent(
                this,
                ChiTietDiaDiemActivity.class
        );

        intent.putExtra(
                "viTriDiaDiem",
                diaDiem.id
        );

        startActivity(intent);
    }

    // =================================================
    // 21. CHI DUONG KHONG CAN API KEY
    // =================================================

    private void moChiDuong() {

        DiaDiem diaDiem =
                timDiaDiemTheoId(
                        idDangChon
                );

        if (diaDiem == null) {

            thongBao(
                    "Vui lòng chọn địa điểm cần đến."
            );

            return;
        }

        String dichDen;

        if (coToaDo(diaDiem)) {

            dichDen =
                    diaDiem.viDo
                            + ","
                            + diaDiem.kinhDo;

        } else {

            dichDen =
                    (diaDiem.ten == null
                            ? ""
                            : diaDiem.ten)
                            + ", "
                            + (diaDiem.diaChi == null
                            ? ""
                            : diaDiem.diaChi);
        }

        // Google Maps URL:
        // Khong goi Directions API co tinh phi
        Uri url = Uri.parse(
                        "https://www.google.com/maps/dir/"
                ).buildUpon()
                .appendQueryParameter(
                        "api",
                        "1"
                )
                .appendQueryParameter(
                        "destination",
                        dichDen
                )
                .appendQueryParameter(
                        "travelmode",
                        "driving"
                )
                .build();

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                url
        );

        try {

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            thongBao(
                    "Không tìm thấy ứng dụng "
                            + "để mở chỉ đường."
            );
        }
    }

    // =================================================
    // 22. THONG BAO
    // =================================================

    private void thongBao(
            String noiDung
    ) {

        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    // =================================================
    // 23. LUU TRANG THAI
    // =================================================

    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle out
    ) {

        super.onSaveInstanceState(out);

        out.putInt(
                KHOA_DIA_DIEM,
                idDangChon
        );
    }

    // =================================================
    // 24. VONG DOI ACTIVITY
    // =================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (banDo == null) {
            return;
        }

        banDo.onResume();

        // Lan dau du lieu da tai trong onCreate
        if (daQuaLanResumeDau) {

            // Cap nhat dia diem neu Admin da thay doi
            if (taiDuLieuVaMarker()) {

                hienThiDiaDiemDangChon();
            }
        }

        daQuaLanResumeDau = true;

        // Bat lai GPS overlay neu da tung bat
        if (daBatGPS
                && lopViTri != null
                && daCoQuyenViTri()) {

            lopViTri.enableMyLocation();
        }
    }

    @Override
    protected void onPause() {

        // Dung cho GPS khi roi man hinh
        // Neu dang xin quyen, giu yeu cau
        // de tiep tuc sau khi nguoi dung cho phep
        if (!dangXinQuyen) {

            huyChoGPS();

            muonMoGanToi = false;
        }

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

        handler.removeCallbacksAndMessages(null);

        if (bangDanhSachDangMo != null) {

            bangDanhSachDangMo.dismiss();

            bangDanhSachDangMo = null;
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
}
