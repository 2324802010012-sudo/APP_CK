
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
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.BoHienThiAnh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class VanMieuThamQuanActivity extends BaseActivity
        implements HuongDanTour.ManHinh {

    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();

    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> noiDungDiem = new ArrayList<>();
    private final ArrayList<String> tenDiem = new ArrayList<>();
    private final ArrayList<String> moTaDiem = new ArrayList<>();

    private final LinkedHashMap<Integer, String> cacTang =
            new LinkedHashMap<>();

    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc;
    private TextView nutTruoc, nutTiep;

    private LinearLayout danhSachKhuVuc;
    private ProgressBar tienTrinh;

    private GiaoDienTour giaoDienTour;

    private HuongDanTour huongDan;
    private CauHinhAutoDiSan cauHinhAuto;

    private String luotDoc;
    private boolean canNapLai;

    private TextToSpeech tts;
    private boolean sanSangDoc = false;

    private int viTri = 0;
    private int tangDaVe = Integer.MIN_VALUE;

    private final ActivityResultLauncher<String[]> quyenTour =
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

    @Override
    public void xinQuyenTour() {

        quyenTour.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        setContentView(R.layout.activity_tham_quan);

        giaoDienTour = new GiaoDienTour(this, state);

        ((TextView) findViewById(R.id.tqTenDiaDiem))
                .setText("Văn Miếu – Quốc Tử Giám");

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);

        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);

        danhSachKhuVuc = findViewById(R.id.tqKhungSoDo);
        tienTrinh = findViewById(R.id.tqTienTrinh);

        findViewById(R.id.tqNutQuayLai)
                .setOnClickListener(v -> finish());

        nutTruoc.setOnClickListener(v -> chuyenDiem(viTri - 1));

        nutTiep.setOnClickListener(v -> chuyenDiem(viTri + 1));

        if (!taiDiemTuSQLite()) {

            Toast.makeText(this,
                    "Không đọc được dữ liệu Văn Miếu từ SQLite.",
                    Toast.LENGTH_LONG).show();

            finish();

            return;
        }

        int idDiem = state == null
                ? getIntent().getIntExtra("idMarker", 1)
                : state.getInt("idDiemDangChon", 1);

        viTri = Math.max(0, danhSachId.indexOf(idDiem));

        khoiTaoTTS();

        hienThi();

        huongDan = new HuongDanTour(
                this,
                findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto
        );

        caiDatTheoDoiDoc();

        apDungKhoangTrongHeThong();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!canNapLai) return;

        canNapLai = false;

        if (isFinishing() || danhSachId.isEmpty()) return;

        int id = danhSachId.get(viTri);

        if (huongDan != null) huongDan.dung("");

        if (!taiDiemTuSQLite()) {

            finish();

            return;
        }

        viTri = Math.max(0, danhSachId.indexOf(id));

        hienThi();

        ViewGroup khung = findViewById(R.id.tqKhungHuongDan);

        khung.removeAllViews();

        huongDan = new HuongDanTour(this, khung, cauHinhAuto);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {

        if (!danhSachId.isEmpty()) {

            state.putInt(
                    "idDiemDangChon", danhSachId.get(viTri)
            );
        }

        if (giaoDienTour != null) giaoDienTour.luuTrangThai(state);

        super.onSaveInstanceState(state);
    }

    // DOC DU LIEU SQLITE

    private boolean taiDiemTuSQLite() {

        try (AppDatabase troGiup = new AppDatabase(this)) {

            SQLiteDatabase db = troGiup.getReadableDatabase();

            cacDiem.clear();
            cacTang.clear();

            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(db, 4));

            cacTang.putAll(KhoDuLieuThamQuan.cacTang(db, 4));

            danhSachId.clear();
            tenDiem.clear();
            moTaDiem.clear();
            noiDungDiem.clear();

            for (DiemThamQuan d : cacDiem) {

                danhSachId.add(d.id);

                tenDiem.add(d.ten);

                moTaDiem.add(d.moTa);

                String bai = d.noiDung == null
                        || d.noiDung.trim().isEmpty()
                        ? d.moTa : d.noiDung;

                noiDungDiem.add(bai == null ? "" : bai);
            }

            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(db, 4);

            ((TextView) findViewById(R.id.tqTenDiaDiem))
                    .setText(cauHinhAuto.ten);

            AnhSoDoTour.ganAnhDaiDien(
                    this, cauHinhAuto.tenAnh
            );

            tangDaVe = Integer.MIN_VALUE;

            return !danhSachId.isEmpty();

        } catch (Exception e) {

            android.util.Log.e(
                    "VAN_MIEU_SQLITE", "Lỗi dữ liệu", e
            );

            return false;
        }
    }

    // HIEN THI DIEM DANG CHON

    private void hienThi() {

        if (tenDiem.isEmpty()) return;

        tenKhuVuc.setText(tenDiem.get(viTri));

        String bai = noiDungDiem.get(viTri);

        moTaKhuVuc.setText(bai);

        BoHienThiAnh.hienAnhDiem(
                findViewById(R.id.tqAnhDiemThanhPhan),
                cacDiem.get(viTri).tenAnh
        );

        ganNguonThongTin(4, danhSachId.get(viTri));

        TextView nutPhat = findViewById(R.id.tqNutPhat);

        boolean coBai = !bai.trim().isEmpty();

        nutPhat.setEnabled(coBai);
        nutPhat.setAlpha(coBai ? 1f : 0.5f);

        nutPhat.setText(coBai
                ? "Nghe thuyết minh" : "Chưa có thuyết minh");

        demKhuVuc.setText(
                "Điểm " + (viTri + 1) + "/" + tenDiem.size()
        );

        tienTrinh.setProgress(
                (viTri + 1) * 100 / tenDiem.size()
        );

        nutTruoc.setEnabled(viTri > 0);

        nutTiep.setEnabled(viTri < tenDiem.size() - 1);

        nutTruoc.setAlpha(nutTruoc.isEnabled() ? 1f : 0.35f);

        nutTiep.setAlpha(nutTiep.isEnabled() ? 1f : 0.35f);

        capNhatSoDo();

        capNhatDanhSach();
    }

    // CAP NHAT SO DO

    private void capNhatSoDo() {

        int tang = cacDiem.get(viTri).tang;

        int id = danhSachId.get(viTri);

        if (tangDaVe != tang) {

            danhSachKhuVuc.removeAllViews();

            AnhSoDoTour.themSoDo(
                    this,
                    danhSachKhuVuc,
                    4,
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
    }

    private void capNhatDanhSach() {

        giaoDienTour.capNhat(
                cacDiem,
                danhSachId.get(viTri),
                id -> cauHinhAuto != null
                        && cauHinhAuto.choPhepAuto(id),
                cacTang::get,
                id -> {

                    int index = danhSachId.indexOf(id);

                    if (index >= 0) chuyenDiem(index);
                }
        );
    }


    private void chuyenDiem(int moi) {

        if (moi < 0
                || moi >= tenDiem.size()
                || moi == viTri) {

            return;
        }

        boolean ngheNhanh =
                giaoDienTour != null
                        && giaoDienTour.dangYeuCauNghe(
                        danhSachId.get(moi)
                );

        boolean autoDangPhat =
                huongDan != null
                        && huongDan.dangPhatTuDong();

        xacNhanChuyenDiem(
                autoDangPhat,
                tenDiem.get(moi),
                () -> {

                    dungAutoThuCong();

                    dungThuyetMinh();

                    viTri = moi;

                    hienThi();

                    if (ngheNhanh
                            && findViewById(R.id.tqNutPhat)
                            .isEnabled()) {

                        moKhungThuyetMinh(true);
                    }
                }
        );
    }

    // TTS

    private void khoiTaoTTS() {

        tts = new TextToSpeech(this, trangThai -> {

            if (trangThai == TextToSpeech.SUCCESS && tts != null) {

                int ketQua = tts.setLanguage(
                        new Locale("vi", "VN")
                );

                sanSangDoc = ketQua != TextToSpeech.LANG_MISSING_DATA
                        && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
    }

    @Override
    public void hienThiDiemDemo(int id) {

        int index = danhSachId.indexOf(id);

        if (index < 0) return;

        dungThuyetMinh();

        viTri = index;

        hienThi();
    }

    @Override
    public boolean phatDiemTuDong(int id) {

        if (!sanSangDoc
                || tts == null
                || !danhSachId.contains(id)
                || !getLifecycle().getCurrentState()
                .isAtLeast(Lifecycle.State.RESUMED)) {

            return false;
        }

        hienThiDiemDemo(id);

        return docDiemHienTai();
    }

    private boolean docDiemHienTai() {

        if (tts == null || !sanSangDoc) return false;

        String bai = noiDungDiem.get(viTri);

        if (bai.trim().isEmpty()) return false;

        luotDoc = "van_mieu_"
                + danhSachId.get(viTri)
                + "_" + System.nanoTime();

        chuanBiLichSu(
                luotDoc,
                4,
                danhSachId.get(viTri),
                huongDan == null
                        ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu()
        );

        boolean ok = tts.speak(
                tenDiem.get(viTri) + ". " + bai,
                TextToSpeech.QUEUE_FLUSH,
                null,
                luotDoc
        ) == TextToSpeech.SUCCESS;

        if (!ok) {

            luotDoc = null;

            huyLuotLichSu();
        }

        return ok;
    }

    private void dungAutoThuCong() {

        if (huongDan != null) {

            huongDan.dung("Đã chuyển sang tham quan thủ công.");
        }
    }

    private void dungThuyetMinh() {

        huyLuotLichSu();
        dongKhungThuyetMinh();

        luotDoc = null;

        if (tts != null) tts.stop();
    }

    @Override
    public void dungDocTour() {

        dungThuyetMinh();
    }

    private void caiDatTheoDoiDoc() {

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
                    public void onError(String id, int maLoi) {

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
    protected void dungAmThanhThamQuan() {

        dungAutoThuCong();

        dungThuyetMinh();
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

        if (tts != null) {

            tts.stop();
            tts.shutdown();

            tts = null;
        }

        super.onDestroy();
    }
}
