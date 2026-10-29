package com.example.app_ck.ui.tour;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.location.HuongDanConDao;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ConDaoThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {

    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private final ArrayList<String> danhSachNoiDung = new ArrayList<>();
    private CauHinhAutoDiSan cauHinhAuto;
    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> danhSachTen = new ArrayList<>();
    private final ArrayList<String> danhSachMoTa = new ArrayList<>();

    private int viTriHienTai = 0;
    private TextView tenKhuVuc;
    private TextView moTaKhuVuc;
    private TextView demKhuVuc;
    private TextView nutTruoc;
    private TextView nutTiep;
    private ProgressBar tienTrinh;
    private LinearLayout danhSachKhuVuc;
    private TextToSpeech tts;
    private boolean sanSangDoc = false;
    private HuongDanConDao huongDan;
    private String luotDoc;
    private boolean canNapLai;
    private final androidx.activity.result.ActivityResultLauncher<String[]> quyenConDao =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (huongDan != null) huongDan.nhanQuyen(Boolean.TRUE.equals(
                                result.get(android.Manifest.permission.ACCESS_FINE_LOCATION)));
                    });

    void xinQuyenConDao() {
        quyenConDao.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tham_quan);
        anhXaGiaoDien();

        if (!taiDuLieuSQLite()) {
            Toast.makeText(this, "Không tải được dữ liệu Nhà tù Côn Đảo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int idDiem = getIntent().getIntExtra("idMarker", 1);
        for (int i = 0; i < danhSachId.size(); i++) {
            if (danhSachId.get(i) == idDiem) {
                viTriHienTai = i;
                break;
            }
        }

        khoiTaoThuyetMinh();
        caiDatSuKien();
        hienThiDiem();
        huongDan = new HuongDanConDao(this, findViewById(R.id.tqKhungHuongDan), cauHinhAuto);
        apDungKhoangTrongHeThong();
    }

    private void anhXaGiaoDien() {
        ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(
                "Hệ thống Nhà tù Côn Đảo"
        );

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        danhSachKhuVuc = findViewById(R.id.tqDanhSachKhuVuc);
    }

    @Override protected void onResume() {
        super.onResume();
        if (!canNapLai) return;
        canNapLai = false;
        if (isFinishing() || danhSachId.isEmpty()) return;
        int id = danhSachId.get(viTriHienTai);
        if (huongDan != null) huongDan.dung("");
        if (!taiDuLieuSQLite()) { finish(); return; }
        viTriHienTai = Math.max(0, danhSachId.indexOf(id));
        hienThiDiem();
        android.view.ViewGroup khung = findViewById(R.id.tqKhungHuongDan);
        khung.removeAllViews();
        huongDan = new HuongDanConDao(this, khung, cauHinhAuto);
    }

    private boolean taiDuLieuSQLite() {
        try (AppDatabase helper = new AppDatabase(this)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            cacDiem.clear();
            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(db, 3));
            danhSachId.clear();
            danhSachTen.clear();
            danhSachMoTa.clear();
            danhSachNoiDung.clear();
            for (DiemThamQuan d : cacDiem) {
                danhSachId.add(d.id);
                danhSachTen.add(d.ten);
                danhSachMoTa.add(d.moTa);
                danhSachNoiDung.add(d.noiDung);
            }
            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(db, 3);
            ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(cauHinhAuto.ten);
            AnhSoDoTour.ganAnhDaiDien(this, cauHinhAuto.tenAnh);
            return !danhSachId.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("ConDaoThamQuan", "Lỗi SQLite", e);
            return false;
        }
    }

    private void caiDatSuKien() {
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
        nutTruoc.setOnClickListener(v -> chuyenDiem(viTriHienTai - 1));
        nutTiep.setOnClickListener(v -> chuyenDiem(viTriHienTai + 1));

    }

    private void hienThiDiem() {
        int soDiem = danhSachId.size();
        tenKhuVuc.setText(danhSachTen.get(viTriHienTai));
        moTaKhuVuc.setText(danhSachMoTa.get(viTriHienTai));
        ganNguonThongTin(3, danhSachId.get(viTriHienTai));
        demKhuVuc.setText("Điểm " + (viTriHienTai + 1) + "/" + soDiem);
        tienTrinh.setProgress((viTriHienTai + 1) * 100 / soDiem);
        nutTruoc.setEnabled(viTriHienTai > 0);
        nutTiep.setEnabled(viTriHienTai < soDiem - 1);
        nutTruoc.setAlpha(viTriHienTai > 0 ? 1f : 0.35f);
        nutTiep.setAlpha(viTriHienTai < soDiem - 1 ? 1f : 0.35f);
        taoDanhSachDiem();
    }

    private void taoDanhSachDiem() {
        danhSachKhuVuc.removeAllViews();
        AnhSoDoTour.themSoDo(this, danhSachKhuVuc, 3, cacDiem.get(viTriHienTai).tang,
                danhSachId.get(viTriHienTai), id -> {
                    int index = danhSachId.indexOf(id);
                    if (index >= 0) chuyenDiem(index);
                });
        TextView tieuDe = new TextView(this);
        tieuDe.setText("Danh sách điểm tham quan");
        tieuDe.setTextSize(18);
        tieuDe.setTypeface(null, Typeface.BOLD);
        tieuDe.setTextColor(Color.parseColor("#24382D"));
        tieuDe.setPadding(dp(4), dp(12), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);

        for (int i = 0; i < danhSachTen.size(); i++) {
            final int chiSo = i;
            boolean dangChon = i == viTriHienTai;
            TextView the = new TextView(this);
            DiemThamQuan d = cacDiem.get(i);
            String nhanSo = d.soHienThi > 0 ? "Số " + d.soHienThi + " · " : "";
            the.setText(nhanSo + danhSachTen.get(i) + "\n" + cauHinhAuto.nhanCheDo(d.id));
            the.setTextSize(14);
            the.setTypeface(null, dangChon ? Typeface.BOLD : Typeface.NORMAL);
            the.setTextColor(dangChon ? Color.WHITE : Color.parseColor("#24382D"));
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setPadding(dp(14), dp(16), dp(14), dp(16));

            GradientDrawable nen = new GradientDrawable();
            nen.setColor(Color.parseColor(dangChon ? "#4F7C5A" : "#EEF4EA"));
            nen.setCornerRadius(dp(12));
            the.setBackground(nen);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, lp);
            the.setOnClickListener(v -> chuyenDiem(chiSo));
        }
    }

    private void chuyenDiem(int chiSo) {
        if (chiSo < 0 || chiSo >= danhSachId.size()) {
            return;
        }
        boolean autoDangPhat = huongDan != null && huongDan.dangPhatTuDong();
        if (autoDangPhat && chiSo == viTriHienTai) return;
        xacNhanChuyenDiem(autoDangPhat, danhSachTen.get(chiSo), () -> {
            if (huongDan != null) huongDan.dung("Đã chọn điểm thủ công; AUTO tạm dừng. Bấm GPS thật hoặc Demo để bật lại.");
            dungThuyetMinh();
            viTriHienTai = chiSo;
            hienThiDiem();
            moKhungThuyetMinh(false);
        });
    }

    private void khoiTaoThuyetMinh() {
        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai != TextToSpeech.SUCCESS || tts == null) {
                return;
            }
            int ketQua = tts.setLanguage(new Locale("vi", "VN"));
            sanSangDoc = ketQua != TextToSpeech.LANG_MISSING_DATA
                    && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
            tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override public void onStart(String id) { batDauLichSu(id); }
                @Override public void onDone(String id) { ketThucDoc(id, true); }
                @Override public void onError(String id) { ketThucDoc(id, false); }
            });
        });
    }



    private boolean docDiemHienTai() {
        // ID của lượt được tạo bên dưới trước khi TTS nhận yêu cầu.
        if (tts == null || !sanSangDoc) return false;
        luotDoc = "con_dao_" + danhSachId.get(viTriHienTai) + "_" + System.nanoTime();
        chuanBiLichSu(luotDoc, 3, danhSachId.get(viTriHienTai), huongDan == null ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu());
        boolean thanhCong = tts.speak(danhSachTen.get(viTriHienTai) + ". " + danhSachNoiDung.get(viTriHienTai),
                TextToSpeech.QUEUE_FLUSH, null, luotDoc) == TextToSpeech.SUCCESS;
        if (!thanhCong) { luotDoc = null; huyLuotLichSu(); }
        return thanhCong;
    }

    @Override public void hienThiDiemDemo(int id) {
        int chiSo = danhSachId.indexOf(id);
        if (chiSo < 0) return;
        dungThuyetMinh();
        viTriHienTai = chiSo;
        hienThiDiem();
    }

    @Override public boolean phatDiemTuDong(int id) {
        if (tts == null || !sanSangDoc || !getLifecycle().getCurrentState().isAtLeast(
                androidx.lifecycle.Lifecycle.State.RESUMED)) return false;
        int chiSo = danhSachId.indexOf(id);
        if (chiSo < 0) return false;
        dungThuyetMinh();
        viTriHienTai = chiSo;
        hienThiDiem();
        return docDiemHienTai();
    }

    private void ketThucDoc(String id, boolean thanhCong) {
        runOnUiThread(() -> {
            if (id == null || !id.equals(luotDoc) || isDestroyed()) return;
            luotDoc = null;
            if (huongDan != null) huongDan.docXong(thanhCong);
        });
    }

    @Override public void dungDocTour() { dungThuyetMinh(); }

    @Override public void xinQuyenTour() { xinQuyenConDao(); }

    private void dungThuyetMinh() {
        huyLuotLichSu();
        dongKhungThuyetMinh();
        luotDoc = null;
        if (tts != null) {
            tts.stop();
        }
    }

    private int dp(int giaTri) {
        return (int) (giaTri * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override protected void dungAmThanhThamQuan() {
        if (huongDan != null) huongDan.dung("Đã chuyển sang nghe thủ công.");
        dungThuyetMinh();
    }

    @Override
    protected void onStop() {
        canNapLai = true;
        if (huongDan != null) huongDan.dung("Đã rời màn hình; AUTO và âm thanh đã dừng. Bấm GPS thật hoặc Demo để bắt đầu lượt mới.");
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
