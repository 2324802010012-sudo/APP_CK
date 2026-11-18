
package com.example.app_ck.ui.tour;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Lifecycle;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.BoHienThiAnh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class ThamQuanActivity extends BaseActivity
        implements HuongDanTour.ManHinh {

    private final List<DiemThamQuan> cacDiem = new ArrayList<>();

    private final LinkedHashMap<Integer, String> cacTang =
            new LinkedHashMap<>();

    private AppDatabase dbHelper;

    private int idDiaDiem;
    private int idDangChon = -1;
    private int tangDangXem;
    private int tangDaVe = Integer.MIN_VALUE;

    private boolean daTaoGiaoDien;
    private boolean canNapLai;

    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc;
    private TextView nutTruoc, nutTiep;

    private LinearLayout danhSachKhuVuc;
    private ProgressBar tienTrinh;

    private GiaoDienTour giaoDienTour;

    private HuongDanTour huongDan;
    private CauHinhAutoDiSan cauHinhAuto;

    private TextToSpeech tts;
    private boolean sanSangDoc;
    private String luotDoc;

    private final ActivityResultLauncher<String[]> quyenTour =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (huongDan != null) {

                            huongDan.nhanQuyen(Boolean.TRUE.equals(
                                    result.get(
                                            android.Manifest.permission
                                                    .ACCESS_FINE_LOCATION)
                            ));
                        }
                    }
            );

    @Override
    public void xinQuyenTour() {

        quyenTour.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    protected int diaDiemMacDinh() {
        return 0;
    }

    protected int diemMacDinh() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        idDiaDiem = getIntent().getIntExtra(
                "viTriDiaDiem", diaDiemMacDinh()
        );

        if (chuyenSangTourRieng()) return;

        setContentView(R.layout.activity_tham_quan);

        giaoDienTour = new GiaoDienTour(this, savedInstanceState);

        dbHelper = new AppDatabase(this);

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);

        danhSachKhuVuc = findViewById(R.id.tqKhungSoDo);
        tienTrinh = findViewById(R.id.tqTienTrinh);

        findViewById(R.id.tqNutQuayLai)
                .setOnClickListener(v -> finish());

        nutTruoc.setOnClickListener(v -> chuyenTheoThuTu(-1));
        nutTiep.setOnClickListener(v -> chuyenTheoThuTu(1));

        idDangChon = savedInstanceState == null
                ? getIntent().getIntExtra("idMarker", diemMacDinh())
                : savedInstanceState.getInt("idDiemDangChon", -1);

        if (!taiDuLieu()) return;

        DiemThamQuan diem = timDiem(idDangChon);

        if (diem == null && !cacDiem.isEmpty()) {

            diem = cacDiem.get(0);

            idDangChon = diem.id;
        }

        tangDangXem = savedInstanceState != null
                ? savedInstanceState.getInt(
                "tangDangXem", diem == null ? 0 : diem.tang)
                : diem == null ? 0 : diem.tang;

        if (!cacTang.containsKey(tangDangXem)
                && !cacTang.isEmpty()) {

            tangDangXem = cacTang.keySet().iterator().next();
        }

        daTaoGiaoDien = true;

        hienThiKhuVuc();
        khoiTaoTts();
        taoBangHuongDan();
        apDungKhoangTrongHeThong();
    }

    private boolean chuyenSangTourRieng() {

        if (getClass() != ThamQuanActivity.class) return false;

        Intent intent = DieuHuongThamQuan.taoIntent(
                this, idDiaDiem,
                getIntent().getIntExtra("idMarker", -1)
        );

        if (intent.getComponent() == null
                || ThamQuanActivity.class.getName().equals(
                intent.getComponent().getClassName())) {

            return false;
        }

        if (!getIntent().hasExtra("idMarker")) {

            intent.removeExtra("idMarker");
        }

        if (getIntent().getExtras() != null) {

            intent.putExtras(getIntent().getExtras());
        }

        startActivity(intent);

        finish();

        return true;
    }

    // DOC DU LIEU SQLITE

    private boolean taiDuLieu() {

        try {

            cacDiem.clear();

            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(
                    dbHelper.getReadableDatabase(), idDiaDiem));

            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(
                    dbHelper.getReadableDatabase(), idDiaDiem);

            cacTang.clear();

            cacTang.putAll(KhoDuLieuThamQuan.cacTang(
                    dbHelper.getReadableDatabase(), idDiaDiem));

            for (DiemThamQuan diem : cacDiem) {

                if (!cacTang.containsKey(diem.tang)) {

                    cacTang.put(diem.tang, "Khu vực " + diem.tang);
                }
            }

            TextView tenDiaDiem = findViewById(R.id.tqTenDiaDiem);
            ImageView anh = findViewById(R.id.tqAnhDiaDiem);

            tenDiaDiem.setText("Địa điểm tham quan");
            anh.setImageDrawable(null);

            for (DiaDiem diaDiem : dbHelper.danhSachDiaDiem()) {

                if (diaDiem.id != idDiaDiem) continue;

                tenDiaDiem.setText(diaDiem.ten);

                BoHienThiAnh.hienAnhDiem(anh, diaDiem.tenAnh);

                break;
            }

            tangDaVe = Integer.MIN_VALUE;

            return true;

        } catch (Exception ex) {

            android.util.Log.e(
                    "TOUR_SQLITE", "Không đọc được dữ liệu", ex);

            Toast.makeText(this,
                    "Không thể đọc dữ liệu tham quan.",
                    Toast.LENGTH_LONG).show();

            finish();

            return false;
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!daTaoGiaoDien || !canNapLai) return;

        canNapLai = false;

        if (isFinishing() || !taiDuLieu()) return;

        if (!cacTang.containsKey(tangDangXem)
                && !cacTang.isEmpty()) {

            DiemThamQuan diem = timDiem(idDangChon);

            tangDangXem = diem == null
                    ? cacTang.keySet().iterator().next()
                    : diem.tang;
        }

        hienThiKhuVuc();

        if (huongDan != null) huongDan.dung("");

        taoBangHuongDan();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {

        state.putInt("idDiemDangChon", idDangChon);
        state.putInt("tangDangXem", tangDangXem);

        if (giaoDienTour != null) giaoDienTour.luuTrangThai(state);

        super.onSaveInstanceState(state);
    }

    private DiemThamQuan timDiem(int id) {

        for (DiemThamQuan diem : cacDiem) {

            if (diem.id == id) return diem;
        }

        return null;
    }

    private int viTriDiem(int id) {

        for (int i = 0; i < cacDiem.size(); i++) {

            if (cacDiem.get(i).id == id) return i;
        }

        return -1;
    }

    private int soHienThi(DiemThamQuan diem) {
        return diem.soHienThi > 0 ? diem.soHienThi : diem.id;
    }

    private String tenTang(int tang) {

        String ten = cacTang.get(tang);

        return ten == null || ten.trim().isEmpty()
                ? "Khu vực " + tang : ten;
    }

    private String noiDungDiem(DiemThamQuan diem) {

        if (diem == null) return "";

        String bai = diem.noiDung == null
                || diem.noiDung.trim().isEmpty()
                ? diem.moTa : diem.noiDung;

        return bai == null ? "" : bai;
    }

    // HIEN THI DIEM DANG CHON

    private void hienThiKhuVuc() {

        DiemThamQuan diem = timDiem(idDangChon);

        if (diem == null || diem.tang != tangDangXem) {

            idDangChon = -1;
            diem = null;

            for (DiemThamQuan ungVien : cacDiem) {

                if (ungVien.tang == tangDangXem) {

                    diem = ungVien;
                    idDangChon = diem.id;

                    break;
                }
            }
        }

        if (diem != null) {

            diem = KhoDuLieuThamQuan.lay(
                    dbHelper.getReadableDatabase(),
                    idDiaDiem, diem.id
            );
        }

        if (diem == null) idDangChon = -1;

        int viTri = diem == null ? -1 : viTriDiem(diem.id);

        tenKhuVuc.setText(diem == null
                ? "Chưa có điểm tham quan" : diem.ten);

        BoHienThiAnh.hienAnhDiem(
                findViewById(R.id.tqAnhDiemThanhPhan),
                diem == null ? "" : diem.tenAnh
        );

        String bai = noiDungDiem(diem);

        moTaKhuVuc.setText(bai);

        ganNguonThongTin(idDiaDiem, diem == null ? 0 : diem.id);

        boolean coBai = diem != null && !bai.trim().isEmpty();

        TextView nutPhat = findViewById(R.id.tqNutPhat);

        nutPhat.setEnabled(coBai);
        nutPhat.setAlpha(coBai ? 1f : 0.5f);

        nutPhat.setText(coBai
                ? "Nghe thuyết minh" : "Chưa có thuyết minh");

        demKhuVuc.setText(
                viTri < 0
                        ? "0/" + cacDiem.size()
                        : "Điểm " + (viTri + 1) + "/" + cacDiem.size()
        );

        tienTrinh.setProgress(
                viTri < 0 ? 0 : (viTri + 1) * 100 / cacDiem.size()
        );

        nutTruoc.setEnabled(viTri > 0);

        nutTiep.setEnabled(
                viTri >= 0 && viTri < cacDiem.size() - 1);

        nutTruoc.setAlpha(nutTruoc.isEnabled() ? 1f : .35f);
        nutTiep.setAlpha(nutTiep.isEnabled() ? 1f : .35f);

        // Chi tao lai khi doi tang hoac nap lai du lieu

        if (tangDaVe != tangDangXem) {

            danhSachKhuVuc.removeAllViews();

            taoSoDo();

            tangDaVe = tangDangXem;

        } else {

            AnhSoDoTour.capNhatMarker(danhSachKhuVuc, idDangChon);
        }

        taoDanhSach();
    }

    // SO DO VA CHUYEN TANG

    private void taoSoDo() {

        HorizontalScrollView thanhCuon = new HorizontalScrollView(this);

        thanhCuon.setHorizontalScrollBarEnabled(false);

        LinearLayout thanhTang = new LinearLayout(this);

        thanhTang.setOrientation(LinearLayout.HORIZONTAL);

        thanhCuon.addView(thanhTang);

        danhSachKhuVuc.addView(thanhCuon);

        for (int tang : cacTang.keySet()) {

            boolean dangXem = tangDangXem == tang;

            TextView nutTang = taoNhan(tenTang(tang), 13, true);

            nutTang.setGravity(Gravity.CENTER);

            nutTang.setMinHeight(dp(48));

            nutTang.setPadding(dp(16), dp(10), dp(16), dp(10));

            nutTang.setTextColor(
                    dangXem ? Color.WHITE : Color.parseColor("#2C7351")
            );

            nutTang.setBackground(nen(
                    dangXem ? "#2C7351" : "#FFFFFF",
                    dangXem ? "#2C7351" : "#E0E9E0"
            ));

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-2, -2);

            lp.rightMargin = dp(8);
            lp.bottomMargin = dp(6);

            thanhTang.addView(nutTang, lp);

            nutTang.setSelected(dangXem);

            if (dangXem) {

                nutTang.post(() ->
                        thanhCuon.smoothScrollTo(nutTang.getLeft(), 0));
            }

            nutTang.setOnClickListener(v -> {

                if (tangDangXem == tang) return;

                for (DiemThamQuan diem : cacDiem) {

                    if (diem.tang == tang) {

                        chonDiem(diem.id, false);

                        return;
                    }
                }

                boolean autoDangPhat = huongDan != null
                        && huongDan.dangPhatTuDong();

                xacNhanChuyenDiem(
                        autoDangPhat,
                        tenTang(tang),
                        () -> {

                            dungAutoVaNgheThuCong();

                            tangDangXem = tang;
                            idDangChon = -1;

                            hienThiKhuVuc();
                        }
                );
            });
        }

        AnhSoDoTour.themSoDo(
                this,
                danhSachKhuVuc,
                idDiaDiem,
                tangDangXem,
                idDangChon,
                id -> chonDiem(id, false)
        );
    }

    private void taoDanhSach() {

        giaoDienTour.capNhat(
                cacDiem,
                idDangChon,
                id -> cauHinhAuto != null
                        && cauHinhAuto.choPhepAuto(id),
                this::tenTang,
                id -> chonDiem(id, false)
        );
    }

    private void chuyenTheoThuTu(int buoc) {

        int viTri = viTriDiem(idDangChon);
        int moi = viTri + buoc;

        if (viTri >= 0 && moi >= 0 && moi < cacDiem.size()) {

            chonDiem(cacDiem.get(moi).id, false);
        }
    }


    private void chonDiem(int id, boolean moTrinhNghe) {

        DiemThamQuan diem = timDiem(id);

        if (diem == null) return;

        boolean ngheNhanh = giaoDienTour != null
                && giaoDienTour.dangYeuCauNghe(id);

        boolean autoDangPhat =
                huongDan != null
                        && huongDan.dangPhatTuDong();

        if (idDangChon == diem.id
                && (autoDangPhat || !moTrinhNghe)
                && !ngheNhanh) {

            return;
        }

        xacNhanChuyenDiem(
                autoDangPhat,
                diem.ten,
                () -> {

                    dungAutoVaNgheThuCong();

                    idDangChon = diem.id;

                    tangDangXem = diem.tang;

                    hienThiKhuVuc();

                    if (ngheNhanh
                            && findViewById(R.id.tqNutPhat)
                            .isEnabled()) {

                        moKhungThuyetMinh(true);

                    } else if (moTrinhNghe) {

                        moKhungThuyetMinh(false);
                    }
                }
        );
    }

    // AUTO VA TTS

    private void taoBangHuongDan() {

        LinearLayout khung = findViewById(R.id.tqKhungHuongDan);

        if (khung == null || cauHinhAuto == null) return;

        khung.removeAllViews();

        huongDan = new HuongDanTour(this, khung, cauHinhAuto);
    }

    private void khoiTaoTts() {

        tts = new TextToSpeech(this, ketQua -> {

            if (ketQua == TextToSpeech.SUCCESS && tts != null) {

                int ngonNgu = tts.setLanguage(new Locale("vi", "VN"));

                sanSangDoc = ngonNgu != TextToSpeech.LANG_MISSING_DATA
                        && ngonNgu != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        tts.setOnUtteranceProgressListener(
                new UtteranceProgressListener() {

                    @Override
                    public void onStart(String id) {
                        batDauLichSu(id);
                    }

                    @Override
                    public void onDone(String id) {
                        ketThucDoc(id, true);
                    }

                    @Override
                    public void onError(String id) {
                        ketThucDoc(id, false);
                    }

                    @Override
                    public void onError(String id, int errorCode) {
                        ketThucDoc(id, false);
                    }
                }
        );
    }

    private void ketThucDoc(String id, boolean thanhCong) {

        runOnUiThread(() -> {

            if (isDestroyed() || id == null || !id.equals(luotDoc)) {

                return;
            }

            luotDoc = null;

            if (huongDan != null) huongDan.docXong(thanhCong);
        });
    }

    @Override
    public void hienThiDiemDemo(int id) {

        DiemThamQuan diem = timDiem(id);

        if (diem == null) return;

        dongKhungThuyetMinh();

        idDangChon = diem.id;
        tangDangXem = diem.tang;

        hienThiKhuVuc();
    }

    @Override
    public boolean phatDiemTuDong(int id) {

        if (!sanSangDoc || tts == null
                || isFinishing() || isDestroyed()
                || !getLifecycle().getCurrentState()
                .isAtLeast(Lifecycle.State.RESUMED)) {

            return false;
        }

        DiemThamQuan diem = timDiem(id);

        if (diem == null) return false;

        diem = KhoDuLieuThamQuan.lay(
                dbHelper.getReadableDatabase(), idDiaDiem, id
        );

        if (diem == null) return false;

        String noiDung = noiDungDiem(diem);

        if (noiDung.trim().isEmpty()) return false;

        hienThiDiemDemo(id);

        luotDoc = "auto_sub_" + idDiaDiem + "_"
                + id + "_" + System.nanoTime();

        chuanBiLichSu(
                luotDoc,
                idDiaDiem,
                id,
                huongDan == null
                        ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu()
        );

        int ketQua = tts.speak(
                diem.ten + ". " + noiDung,
                TextToSpeech.QUEUE_FLUSH,
                null,
                luotDoc
        );

        if (ketQua != TextToSpeech.SUCCESS) {

            luotDoc = null;

            huyLuotLichSu();

            return false;
        }

        return true;
    }

    @Override
    public void dungDocTour() {

        dungThuyetMinh();
    }

    private void dungAutoVaNgheThuCong() {

        if (huongDan != null) {

            huongDan.dung("Đã chuyển sang tham quan thủ công.");
        }

        dungThuyetMinh();
    }

    private void dungThuyetMinh() {

        luotDoc = null;

        huyLuotLichSu();
        dongKhungThuyetMinh();

        if (tts != null) tts.stop();
    }

    @Override
    protected void dungAmThanhThamQuan() {

        dungAutoVaNgheThuCong();
    }

    private int dp(float giaTri) {

        return Math.round(giaTri * getResources()
                .getDisplayMetrics().density);
    }

    private GradientDrawable nen(String mau, String vien) {

        GradientDrawable d = new GradientDrawable();

        d.setColor(Color.parseColor(mau));
        d.setCornerRadius(dp(14));
        d.setStroke(dp(1), Color.parseColor(vien));

        return d;
    }

    private TextView taoNhan(String s, int size, boolean dam) {

        TextView t = new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor("#182D22"));

        t.setTypeface(Typeface.create(
                dam ? "sans-serif-medium" : "sans-serif",
                Typeface.NORMAL
        ));

        return t;
    }

    @Override
    protected void onStop() {

        canNapLai = true;

        if (huongDan != null) huongDan.dung("");

        dungThuyetMinh();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (huongDan != null) huongDan.dung("");

        dungThuyetMinh();

        if (tts != null) {

            tts.shutdown();

            tts = null;
        }

        if (dbHelper != null) dbHelper.close();

        super.onDestroy();
    }
}
