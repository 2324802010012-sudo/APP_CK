package com.example.app_ck.ui.home;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
public class WelcomeActivity extends BaseActivity {
    private Button nutBatDau;
    private TextView nutDangNhap;
    private MapView banDoChaoMung;
    @Override
    protected void onCreate(Bundle trangThaiDaLuu) {
        super.onCreate(trangThaiDaLuu);
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("cau_hinh_ban_do", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("APP_CK/1.0 (Android; com.example.app_ck)");
        setContentView(R.layout.activity_welcome);
        apDungKhoangTrongHeThong();
        khoiTaoBanDo(trangThaiDaLuu);
        nutBatDau = findViewById(R.id.btnBatDau);
        nutDangNhap = findViewById(R.id.btnDangNhap);
        nutBatDau.setOnClickListener(v -> {
            Intent chuyenTrang = new Intent(WelcomeActivity.this, MainActivity.class)
                    .putExtra(MainActivity.BAT_DAU_HUONG_DAN, true);
            startActivity(chuyenTrang);
            finish();
        });
        nutDangNhap.setOnClickListener(v -> {
            if (PhienDangNhap.daDangNhap(this)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, DangNhapActivity.class)
                        .putExtra(DangNhapActivity.VE_TRANG_CHU, true));
            }
        });
    }

    private void khoiTaoBanDo(Bundle trangThaiDaLuu) {
        banDoChaoMung = findViewById(R.id.banDoChaoMung);
        // Cùng nguồn bản đồ và cache với màn hình Bản đồ từ trang chủ.
        banDoChaoMung.setTileSource(new XYTileSource("OSMOfficial", 0, 19, 256, ".png",
                new String[]{"https://tile.openstreetmap.org/"}));
        banDoChaoMung.setMultiTouchControls(true);
        banDoChaoMung.setBuiltInZoomControls(false);
        banDoChaoMung.setMinZoomLevel(4.0);
        banDoChaoMung.setMaxZoomLevel(19.0);
        banDoChaoMung.getController().setZoom(5.5);
        banDoChaoMung.getController().setCenter(new GeoPoint(15.5, 107.5));
        ArrayList<GeoPoint> toaDo = new ArrayList<>();
        try (AppDatabase db = new AppDatabase(this)) {
            for (DiaDiem d : db.danhSachDiaDiem()) {
                if (!Double.isFinite(d.viDo) || !Double.isFinite(d.kinhDo)
                        || Math.abs(d.viDo) > 90 || Math.abs(d.kinhDo) > 180) continue;
                GeoPoint viTri = new GeoPoint(d.viDo, d.kinhDo);
                toaDo.add(viTri);
                Marker marker = new Marker(banDoChaoMung);
                marker.setId(String.valueOf(d.id));
                marker.setPosition(viTri);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(d.ten);
                marker.setSnippet(d.diaChi);
                banDoChaoMung.getOverlays().add(marker);
            }
        } catch (Exception e) {
            android.util.Log.e("WelcomeActivity", "Không đọc được địa điểm cho bản đồ", e);
            android.widget.Toast.makeText(this, "Chưa tải được các điểm đến. Bạn vẫn có thể khám phá bản đồ.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
        findViewById(R.id.chaoPhongToBanDo).setOnClickListener(v -> banDoChaoMung.getController().zoomIn());
        findViewById(R.id.chaoThuNhoBanDo).setOnClickListener(v -> banDoChaoMung.getController().zoomOut());
        if (trangThaiDaLuu != null && trangThaiDaLuu.containsKey("chao_zoom")) {
            banDoChaoMung.getController().setZoom(trangThaiDaLuu.getDouble("chao_zoom"));
            banDoChaoMung.getController().setCenter(new GeoPoint(
                    trangThaiDaLuu.getDouble("chao_vi_do"), trangThaiDaLuu.getDouble("chao_kinh_do")));
        } else if (!toaDo.isEmpty()) {
            banDoChaoMung.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    int le = Math.round(70 * getResources().getDisplayMetrics().density);
                    banDoChaoMung.zoomToBoundingBox(BoundingBox.fromGeoPoints(toaDo), false, le);
                }
            });
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        if (banDoChaoMung != null) {
            state.putDouble("chao_zoom", banDoChaoMung.getZoomLevelDouble());
            state.putDouble("chao_vi_do", banDoChaoMung.getMapCenter().getLatitude());
            state.putDouble("chao_kinh_do", banDoChaoMung.getMapCenter().getLongitude());
        }
        super.onSaveInstanceState(state);
    }

    @Override protected void onResume() {
        super.onResume();
        if (banDoChaoMung != null) banDoChaoMung.onResume();
    }

    @Override protected void onPause() {
        if (banDoChaoMung != null) banDoChaoMung.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (banDoChaoMung != null) banDoChaoMung.onDetach();
        super.onDestroy();
    }
}
