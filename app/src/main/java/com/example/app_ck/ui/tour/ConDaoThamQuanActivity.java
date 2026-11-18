
package com.example.app_ck.ui.tour;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.ViewGroup;
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
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.location.HuongDanConDao;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.BoHienThiAnh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class ConDaoThamQuanActivity extends BaseActivity
        implements HuongDanTour.ManHinh {

    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private final ArrayList<String> danhSachNoiDung = new ArrayList<>();
    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> danhSachTen = new ArrayList<>();
    private final ArrayList<String> danhSachMoTa = new ArrayList<>();

    private final LinkedHashMap<Integer, String> cacTang =
            new LinkedHashMap<>();

    private int viTriHienTai = 0;
    private int tangDaVe = Integer.MIN_VALUE;

    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc;
    private TextView nutTruoc, nutTiep;

    private ProgressBar tienTrinh;
    private LinearLayout danhSachKhuVuc;

    private GiaoDienTour giaoDienTour;

    private TextToSpeech tts;
    private boolean sanSangDoc = false;

    private HuongDanConDao huongDan;
    private CauHinhAutoDiSan cauHinhAuto;

    private String luotDoc;
    private boolean canNapLai;

    private final ActivityResultLauncher<String[]> quyenConDao =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                        if (huongDan != null) {

                            huongDan.nhanQuyen(Boolean.TRUE.equals(
                                    result.get(android.Manifest.permission
                                            .ACCESS_FINE_LOCATION)
                            ));
                        }
                    }
            );

    void xinQuyenConDao() {

        quyenConDao.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    public void xinQuyenTour() {

        xinQuyenConDao();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tham_quan);

        giaoDienTour = new GiaoDienTour(this, savedInstanceState);

        anhXaGiaoDien();

        if (!taiDuLieuSQLite()) {

            Toast.makeText(this,
                    "Không tải được dữ liệu Nhà tù Côn Đảo.",
                    Toast.LENGTH_LONG).show();

            finish();

            return;
        }

        int idDiem = savedInstanceState == null
                ? getIntent().getIntExtra("idMarker", 1)
                : savedInstanceState.getInt("idDiemDangChon", 1);

        viTriHienTai = Math.max(0, danhSachId.indexOf(idDiem));

        khoiTaoThuyetMinh();
        caiDatSuKien();
        hienThiDiem();

        huongDan = new HuongDanConDao(
                this,
                findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto
        );

        apDungKhoangTrongHeThong();
    }

    private void anhXaGiaoDien() {

        ((TextView) findViewById(R.id.tqTenDiaDiem))
                .setText("Hệ thống Nhà tù Côn Đảo");

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);

        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);

        tienTrinh = findViewById(R.id.tqTienTrinh);
        danhSachKhuVuc = findViewById(R.id.tqKhungSoDo);
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!canNapLai) return;

        canNapLai = false;

        if (isFinishing() || danhSachId.isEmpty()) return;

        int id = danhSachId.get(viTriHienTai);

        if (huongDan != null) huongDan.dung("");

        if (!taiDuLieuSQLite()) {

            finish();

            return;
        }

        viTriHienTai = Math.max(0, danhSachId.indexOf(id));

        hienThiDiem();

        ViewGroup khung = findViewById(R.id.tqKhungHuongDan);

        khung.removeAllViews();

        huongDan = new HuongDanConDao(this, khung, cauHinhAuto);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {

        if (!danhSachId.isEmpty()) {

            state.putInt(
                    "idDiemDangChon", danhSachId.get(viTriHienTai)
            );
        }

        if (giaoDienTour != null) giaoDienTour.luuTrangThai(state);

        super.onSaveInstanceState(state);
    }

    private boolean taiDuLieuSQLite() {

        try (AppDatabase helper = new AppDatabase(this)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            cacDiem.clear();
            cacTang.clear();

            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(db, 3));

            cacTang.putAll(KhoDuLieuThamQuan.cacTang(db, 3));

            danhSachId.clear();
            danhSachTen.clear();
            danhSachMoTa.clear();
            danhSachNoiDung.clear();

            for (DiemThamQuan d : cacDiem) {

                danhSachId.add(d.id);
                danhSachTen.add(d.ten);
                danhSachMoTa.add(d.moTa);

                String bai = d.noiDung == null
                        || d.noiDung.trim().isEmpty()
                        ? d.moTa : d.noiDung;

                danhSachNoiDung.add(bai == null ? "" : bai);
            }

            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(db, 3);

            ((TextView) findViewById(R.id.tqTenDiaDiem))
                    .setText(cauHinhAuto.ten);

            AnhSoDoTour.ganAnhDaiDien(this, cauHinhAuto.tenAnh);

            tangDaVe = Integer.MIN_VALUE;

            return !danhSachId.isEmpty();

        } catch (Exception e) {

            android.util.Log.e(
                    "ConDaoThamQuan", "Lỗi SQLite", e);

            return false;
        }
    }

    private void caiDatSuKien() {

        findViewById(R.id.tqNutQuayLai)
                .setOnClickListener(v -> finish());

        nutTruoc.setOnClickListener(
                v -> chuyenDiem(viTriHienTai - 1));

        nutTiep.setOnClickListener(
                v -> chuyenDiem(viTriHienTai + 1));
    }

    // HIEN THI DIEM

    private void hienThiDiem() {

        if (danhSachId.isEmpty()) return;

        int soDiem = danhSachId.size();

        tenKhuVuc.setText(danhSachTen.get(viTriHienTai));

        String bai = danhSachNoiDung.get(viTriHienTai);

        moTaKhuVuc.setText(bai);

        BoHienThiAnh.hienAnhDiem(
                findViewById(R.id.tqAnhDiemThanhPhan),
                cacDiem.get(viTriHienTai).tenAnh
        );

        ganNguonThongTin(3, danhSachId.get(viTriHienTai));

        TextView nutPhat = findViewById(R.id.tqNutPhat);

        boolean coBai = !bai.trim().isEmpty();

        nutPhat.setEnabled(coBai);
        nutPhat.setAlpha(coBai ? 1f : 0.5f);
        nutPhat.setText(coBai
                ? "Nghe thuyết minh" : "Chưa có thuyết minh");

        demKhuVuc.setText(
                "Điểm " + (viTriHienTai + 1) + "/" + soDiem
        );

        tienTrinh.setProgress(
                (viTriHienTai + 1) * 100 / soDiem
        );

        nutTruoc.setEnabled(viTriHienTai > 0);

        nutTiep.setEnabled(viTriHienTai < soDiem - 1);

        nutTruoc.setAlpha(nutTruoc.isEnabled() ? 1f : 0.35f);

        nutTiep.setAlpha(nutTiep.isEnabled() ? 1f : 0.35f);

        taoDanhSachDiem();
    }

    private void taoDanhSachDiem() {

        if (danhSachId.isEmpty()) return;

        int tang = cacDiem.get(viTriHienTai).tang;

        int id = danhSachId.get(viTriHienTai);

        if (tangDaVe != tang) {

            danhSachKhuVuc.removeAllViews();

            AnhSoDoTour.themSoDo(
                    this,
                    danhSachKhuVuc,
                    3,
                    tang,
                    id,
                    idDiem -> {

                        int index = danhSachId.indexOf(idDiem);

                        if (index >= 0) chuyenDiem(index);
                    }
            );

            tangDaVe = tang;

        } else {

            AnhSoDoTour.capNhatMarker(danhSachKhuVuc, id);
        }

        giaoDienTour.capNhat(
                cacDiem,
                id,
                idDiem -> cauHinhAuto != null
                        && cauHinhAuto.choPhepAuto(idDiem),
                cacTang::get,
                idDiem -> {

                    int index = danhSachId.indexOf(idDiem);

                    if (index >= 0) chuyenDiem(index);
                }
        );
    }


    private void chuyenDiem(int chiSo) {

        if (chiSo < 0
                || chiSo >= danhSachId.size()
                || chiSo == viTriHienTai) {

            return;
        }

        boolean ngheNhanh =
                giaoDienTour != null
                        && giaoDienTour.dangYeuCauNghe(
                        danhSachId.get(chiSo)
                );

        boolean autoDangPhat =
                huongDan != null
                        && huongDan.dangPhatTuDong();

        xacNhanChuyenDiem(
                autoDangPhat,
                danhSachTen.get(chiSo),
                () -> {

                    if (huongDan != null) {

                        huongDan.dung(
                                "Đã chuyển sang tham quan thủ công."
                        );
                    }

                    dungThuyetMinh();

                    viTriHienTai = chiSo;

                    hienThiDiem();

                    if (ngheNhanh
                            && findViewById(R.id.tqNutPhat)
                            .isEnabled()) {

                        moKhungThuyetMinh(true);
                    }
                }
        );
    }

    // TTS VA AUTO

    private void khoiTaoThuyetMinh() {

        tts = new TextToSpeech(this, trangThai -> {

            if (trangThai != TextToSpeech.SUCCESS || tts == null) {

                return;
            }

            int ketQua = tts.setLanguage(new Locale("vi", "VN"));

            sanSangDoc = ketQua != TextToSpeech.LANG_MISSING_DATA
                    && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;

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
        });
    }

    private boolean docDiemHienTai() {

        if (tts == null || !sanSangDoc) return false;

        String bai = danhSachNoiDung.get(viTriHienTai);

        if (bai.trim().isEmpty()) return false;

        luotDoc = "con_dao_"
                + danhSachId.get(viTriHienTai)
                + "_" + System.nanoTime();

        chuanBiLichSu(
                luotDoc,
                3,
                danhSachId.get(viTriHienTai),
                huongDan == null
                        ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu()
        );

        boolean thanhCong = tts.speak(
                danhSachTen.get(viTriHienTai) + ". " + bai,
                TextToSpeech.QUEUE_FLUSH,
                null,
                luotDoc
        ) == TextToSpeech.SUCCESS;

        if (!thanhCong) {

            luotDoc = null;

            huyLuotLichSu();
        }

        return thanhCong;
    }

    @Override
    public void hienThiDiemDemo(int id) {

        int chiSo = danhSachId.indexOf(id);

        if (chiSo < 0) return;

        dungThuyetMinh();

        viTriHienTai = chiSo;

        hienThiDiem();
    }

    @Override
    public boolean phatDiemTuDong(int id) {

        if (tts == null || !sanSangDoc
                || !getLifecycle().getCurrentState()
                .isAtLeast(Lifecycle.State.RESUMED)) {

            return false;
        }

        int chiSo = danhSachId.indexOf(id);

        if (chiSo < 0) return false;

        dungThuyetMinh();

        viTriHienTai = chiSo;

        hienThiDiem();

        return docDiemHienTai();
    }

    private void ketThucDoc(String id, boolean thanhCong) {

        runOnUiThread(() -> {

            if (id == null || !id.equals(luotDoc) || isDestroyed()) {

                return;
            }

            luotDoc = null;

            if (huongDan != null) huongDan.docXong(thanhCong);
        });
    }

    @Override
    public void dungDocTour() {

        dungThuyetMinh();
    }

    private void dungThuyetMinh() {

        huyLuotLichSu();
        dongKhungThuyetMinh();

        luotDoc = null;

        if (tts != null) tts.stop();
    }

    @Override
    protected void dungAmThanhThamQuan() {

        if (huongDan != null) {

            huongDan.dung("Đã chuyển sang nghe thủ công.");
        }

        dungThuyetMinh();
    }

    @Override
    protected void onStop() {

        canNapLai = true;

        if (huongDan != null) {

            huongDan.dung(
                    "Đã rời màn hình; AUTO và âm thanh đã dừng."
            );
        }

        dungThuyetMinh();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {

            tts.stop();
            tts.shutdown();

            tts = null;
        }

        super.onDestroy();
    }
}
