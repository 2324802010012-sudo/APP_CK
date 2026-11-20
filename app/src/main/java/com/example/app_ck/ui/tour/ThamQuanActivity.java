package com.example.app_ck.ui.tour;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Lifecycle;
import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.model.DuLieuTour;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.BoHienThiAnh;
import java.util.Locale;

/** Điều phối chung của mọi tour; dữ liệu, dựng sơ đồ và giao diện có chủ sở hữu riêng. */
public class ThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {
    private AppDatabase dbHelper;
    private DuLieuTour duLieu;
    private CauHinhTour cauHinh;
    private int idDiaDiem, tangDangXem;
    private int idDangChon = -1;
    private boolean canNapLai;
    private GiaoDienTour giaoDienTour;
    private KhungSoDoTour khungSoDo;
    private HuongDanTour huongDan;
    private CauHinhAutoDiSan cauHinhAuto;
    private TextToSpeech tts;
    private boolean sanSangDoc;
    private String luotDoc;
    private final ActivityResultLauncher<String[]> quyenTour = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (huongDan != null) huongDan.nhanQuyen(Boolean.TRUE.equals(
                        result.get(android.Manifest.permission.ACCESS_FINE_LOCATION)));
            });

    @Override public void xinQuyenTour() {
        quyenTour.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    /** Các lớp tương thích Intent cũ chỉ cung cấp mã địa điểm mặc định. */
    protected int diaDiemMacDinh() { return 0; }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        idDiaDiem = getIntent().getIntExtra(DieuHuongThamQuan.DIA_DIEM, diaDiemMacDinh());
        cauHinh = CauHinhTour.choDiaDiem(idDiaDiem);
        setContentView(R.layout.activity_tham_quan);
        giaoDienTour = new GiaoDienTour(this, state);
        khungSoDo = new KhungSoDoTour(this, cauHinh,
                id -> chonDiem(id), this::chonTang);
        dbHelper = new AppDatabase(this);
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
        findViewById(R.id.tqNutTruoc).setOnClickListener(v -> chuyenTheoThuTu(-1));
        findViewById(R.id.tqNutTiep).setOnClickListener(v -> chuyenTheoThuTu(1));
        idDangChon = state == null
                ? getIntent().getIntExtra(DieuHuongThamQuan.DIEM, CauHinhTour.diemMacDinh(idDiaDiem))
                : state.getInt("idDiemDangChon", -1);
        if (!taiDuLieu()) return;
        DiemThamQuan diem = duLieu.timDiem(idDangChon);
        if (diem == null && !duLieu.cacDiem.isEmpty()) {
            diem = duLieu.cacDiem.get(0);
            idDangChon = diem.id;
        }
        tangDangXem = state == null ? (diem == null ? 0 : diem.tang)
                : state.getInt("tangDangXem", diem == null ? 0 : diem.tang);
        kiemTraTang();
        hienThiKhuVuc();
        khoiTaoTts();
        taoBangHuongDan();
        apDungKhoangTrongHeThong();
    }

    private boolean taiDuLieu() {
        try {
            duLieu = KhoDuLieuThamQuan.docTour(dbHelper.getReadableDatabase(), idDiaDiem);
            if (duLieu.diaDiem == null) throw new IllegalStateException("Địa điểm không còn tồn tại");
            cauHinhAuto = CauHinhAutoDiSan.tuDuLieu(duLieu.diaDiem, duLieu.cacDiem);
            ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(duLieu.diaDiem.ten);
            if (cauHinh.hienThanhTang) {
                BoHienThiAnh.hienAnhDiem(findViewById(R.id.tqAnhDiaDiem), duLieu.diaDiem.tenAnh);
            } else {
                AnhSoDoTour.ganAnhDaiDien(this, duLieu.diaDiem.tenAnh);
            }
            return true;
        } catch (Exception ex) {
            android.util.Log.e("TOUR_SQLITE", "Không đọc được dữ liệu", ex);
            Toast.makeText(this, "Không thể đọc dữ liệu tham quan.", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
    }

    private void kiemTraTang() {
        if (!duLieu.cacTang.containsKey(tangDangXem) && !duLieu.cacTang.isEmpty()) {
            DiemThamQuan diem = duLieu.timDiem(idDangChon);
            tangDangXem = diem == null ? duLieu.cacTang.keySet().iterator().next() : diem.tang;
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (!canNapLai || duLieu == null || isFinishing()) return;
        canNapLai = false;
        if (!taiDuLieu()) return;
        kiemTraTang();
        hienThiKhuVuc();
        taoBangHuongDan();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("idDiemDangChon", idDangChon);
        state.putInt("tangDangXem", tangDangXem);
        if (giaoDienTour != null) giaoDienTour.luuTrangThai(state);
        super.onSaveInstanceState(state);
    }

    private void hienThiKhuVuc() {
        DiemThamQuan diem = duLieu.timDiem(idDangChon);
        if (diem == null || diem.tang != tangDangXem) diem = duLieu.diemDauTang(tangDangXem);
        idDangChon = diem == null ? -1 : diem.id;
        int viTri = duLieu.viTriDiem(idDangChon);
        ((TextView) findViewById(R.id.tqTenKhuVuc)).setText(
                diem == null ? "Chưa có điểm tham quan" : diem.ten);
        BoHienThiAnh.hienAnhDiem(findViewById(R.id.tqAnhDiemThanhPhan), diem == null ? "" : diem.tenAnh);
        String bai = diem == null ? "" : diem.baiThuyetMinh();
        ((TextView) findViewById(R.id.tqMoTaKhuVuc)).setText(bai);
        ganNguonThongTin(idDiaDiem, diem == null ? 0 : diem.id);
        boolean coBai = !bai.trim().isEmpty();
        TextView nutPhat = findViewById(R.id.tqNutPhat);
        nutPhat.setEnabled(coBai);
        nutPhat.setAlpha(coBai ? 1f : .5f);
        nutPhat.setText(coBai ? "Nghe thuyết minh" : "Chưa có thuyết minh");
        ((TextView) findViewById(R.id.tqDemKhuVuc)).setText(viTri < 0 ? "0/" + duLieu.cacDiem.size()
                : "Điểm " + (viTri + 1) + "/" + duLieu.cacDiem.size());
        ((ProgressBar) findViewById(R.id.tqTienTrinh)).setProgress(
                viTri < 0 ? 0 : (viTri + 1) * 100 / duLieu.cacDiem.size());
        capNhatNut(R.id.tqNutTruoc, viTri > 0);
        capNhatNut(R.id.tqNutTiep, viTri >= 0 && viTri < duLieu.cacDiem.size() - 1);
        khungSoDo.capNhat(duLieu, tangDangXem, idDangChon);
        giaoDienTour.capNhat(duLieu.cacDiem, idDangChon, cauHinhAuto::choPhepAuto,
                duLieu::tenTang, id -> chonDiem(id));
    }

    private void capNhatNut(int id, boolean bat) {
        findViewById(id).setEnabled(bat);
        findViewById(id).setAlpha(bat ? 1f : .35f);
    }

    private void chuyenTheoThuTu(int buoc) {
        int viTri = duLieu.viTriDiem(idDangChon);
        int moi = viTri + buoc;
        if (viTri >= 0 && moi >= 0 && moi < duLieu.cacDiem.size()) chonDiem(duLieu.cacDiem.get(moi).id);
    }

    private void chonTang(int tang) {
        if (tangDangXem == tang) return;
        DiemThamQuan diem = duLieu.diemDauTang(tang);
        if (diem != null) { chonDiem(diem.id); return; }
        xacNhanChuyenDiem(huongDan != null && huongDan.dangPhatTuDong(), duLieu.tenTang(tang), () -> {
            dungAutoVaNgheThuCong();
            tangDangXem = tang;
            idDangChon = -1;
            hienThiKhuVuc();
        });
    }

    private void chonDiem(int id) {
        DiemThamQuan diem = duLieu.timDiem(id);
        if (diem == null) return;
        // Chụp yêu cầu nghe trước khi callback xác nhận bất đồng bộ trả về.
        boolean ngheNhanh = giaoDienTour.dangYeuCauNghe(id);
        boolean autoDangPhat = huongDan != null && huongDan.dangPhatTuDong();
        if (idDangChon == id && !ngheNhanh) return;
        xacNhanChuyenDiem(autoDangPhat, diem.ten, () -> {
            dungAutoVaNgheThuCong();
            idDangChon = diem.id;
            tangDangXem = diem.tang;
            hienThiKhuVuc();
            if (ngheNhanh && findViewById(R.id.tqNutPhat).isEnabled()) moKhungThuyetMinh(true);
        });
    }

    private void taoBangHuongDan() {
        if (huongDan != null) huongDan.dung("");
        LinearLayout khung = findViewById(R.id.tqKhungHuongDan);
        khung.removeAllViews();
        huongDan = new HuongDanTour(this, khung, cauHinhAuto, cauHinh.dieuKhienConDao);
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
        DiemThamQuan diem = duLieu.timDiem(id);
        if (diem == null) return;
        dungThuyetMinh();
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
        DiemThamQuan diem = duLieu.timDiem(id);
        if (diem == null) return false;
        diem = KhoDuLieuThamQuan.lay(
                dbHelper.getReadableDatabase(), idDiaDiem, id
        );
        if (diem == null) return false;
        String noiDung = diem.baiThuyetMinh();
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
        dungAutoVaNgheThuCong("Đã chuyển sang tham quan thủ công.");
    }

    private void dungAutoVaNgheThuCong(String lyDo) {
        if (huongDan != null) {
            huongDan.dung(lyDo);
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
        dungAutoVaNgheThuCong(cauHinh.lyDoNgheThuCong);
    }

    @Override protected void onStop() {
        canNapLai = true;
        if (huongDan != null) huongDan.dung(cauHinh.lyDoRoiManHinh);
        dungThuyetMinh();
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (huongDan != null) huongDan.dung("");
        dungThuyetMinh();
        if (tts != null) { tts.shutdown(); tts = null; }
        if (khungSoDo != null) khungSoDo.giaiPhong();
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
}
