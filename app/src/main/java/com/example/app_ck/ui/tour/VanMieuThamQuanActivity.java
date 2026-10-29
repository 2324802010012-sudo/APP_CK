package com.example.app_ck.ui.tour;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
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


public class VanMieuThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {

    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> noiDungDiem = new ArrayList<>();
    private final ArrayList<String> tenDiem = new ArrayList<>();
    private final ArrayList<String> moTaDiem = new ArrayList<>();

    private TextView tenKhuVuc;
    private TextView moTaKhuVuc;
    private TextView demKhuVuc;
    private TextView nutTruoc;
    private TextView nutTiep;
    private LinearLayout danhSachKhuVuc;
    private ProgressBar tienTrinh;
    private HuongDanTour huongDan;
    private CauHinhAutoDiSan cauHinhAuto;
    private String luotDoc;
    private boolean canNapLai;
    private final androidx.activity.result.ActivityResultLauncher<String[]> quyenTour =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (huongDan != null) huongDan.nhanQuyen(Boolean.TRUE.equals(
                                result.get(android.Manifest.permission.ACCESS_FINE_LOCATION)));
                    });

    @Override public void xinQuyenTour() {
        quyenTour.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void dungAutoThuCong() {
        if (huongDan != null) huongDan.dung("Đã chuyển sang nghe thủ công.");
    }

    private TextToSpeech tts;
    private boolean sanSangDoc = false;
    private int viTri = 0;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_tham_quan);

        ((TextView) findViewById(R.id.tqTenDiaDiem))
                .setText("Văn Miếu – Quốc Tử Giám");

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        danhSachKhuVuc = findViewById(R.id.tqDanhSachKhuVuc);
        tienTrinh = findViewById(R.id.tqTienTrinh);

        findViewById(R.id.tqNutQuayLai)
                .setOnClickListener(v -> finish());

        nutTruoc.setOnClickListener(v -> chuyenDiem(viTri - 1));
        nutTiep.setOnClickListener(v -> chuyenDiem(viTri + 1));

        if (!taiDiemTuSQLite()) {
            Toast.makeText(this, "Không đọc được dữ liệu Văn Miếu từ SQLite.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int idDiem = getIntent().getIntExtra("idMarker", 1);
        viTri = Math.max(0, danhSachId.indexOf(idDiem));

        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS && tts != null) {
                int ketQua = tts.setLanguage(new Locale("vi", "VN"));
                sanSangDoc = ketQua != TextToSpeech.LANG_MISSING_DATA
                        && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        hienThi();
        huongDan = new HuongDanTour(this, findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto);
        caiDatTheoDoiDoc();
        apDungKhoangTrongHeThong();
    }

    @Override protected void onResume() {
        super.onResume();
        if (!canNapLai) return;
        canNapLai = false;
        if (isFinishing() || danhSachId.isEmpty()) return;
        int id = danhSachId.get(viTri);
        if (huongDan != null) huongDan.dung("");
        if (!taiDiemTuSQLite()) { finish(); return; }
        viTri = Math.max(0, danhSachId.indexOf(id));
        hienThi();
        android.view.ViewGroup khung = findViewById(R.id.tqKhungHuongDan);
        khung.removeAllViews();
        huongDan = new HuongDanTour(this, khung, cauHinhAuto);
    }

    private boolean taiDiemTuSQLite() {
        try (AppDatabase troGiup = new AppDatabase(this)) {
            SQLiteDatabase db = troGiup.getReadableDatabase();
            cacDiem.clear();
            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(db, 4));
            danhSachId.clear();
            tenDiem.clear();
            moTaDiem.clear();
            noiDungDiem.clear();
            for (DiemThamQuan d : cacDiem) {
                danhSachId.add(d.id);
                tenDiem.add(d.ten);
                moTaDiem.add(d.moTa);
                noiDungDiem.add(d.noiDung);
            }
            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(db, 4);
            ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(cauHinhAuto.ten);
            AnhSoDoTour.ganAnhDaiDien(this, cauHinhAuto.tenAnh);
            return !danhSachId.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("VAN_MIEU_SQLITE", "Lỗi dữ liệu", e);
            return false;
        }
    }

    private void hienThi() {
        tenKhuVuc.setText(tenDiem.get(viTri));
        moTaKhuVuc.setText(moTaDiem.get(viTri));
        ganNguonThongTin(4, danhSachId.get(viTri));
        demKhuVuc.setText("Điểm " + (viTri + 1) + "/" + tenDiem.size());
        tienTrinh.setProgress((viTri + 1) * 100 / tenDiem.size());

        nutTruoc.setEnabled(viTri > 0);
        nutTruoc.setAlpha(viTri > 0 ? 1f : 0.35f);
        nutTiep.setEnabled(viTri < tenDiem.size() - 1);
        nutTiep.setAlpha(viTri < tenDiem.size() - 1 ? 1f : 0.35f);

        danhSachKhuVuc.removeAllViews();
        AnhSoDoTour.themSoDo(this, danhSachKhuVuc, 4, cacDiem.get(viTri).tang,
                danhSachId.get(viTri), id -> {
                    int index = danhSachId.indexOf(id);
                    if (index >= 0) chuyenDiem(index);
                });

        TextView tieuDe = new TextView(this);
        tieuDe.setText("Danh sách điểm tham quan");
        tieuDe.setTextColor(Color.parseColor("#24382D"));
        tieuDe.setTextSize(18);
        tieuDe.setTypeface(null, Typeface.BOLD);
        tieuDe.setPadding(dp(4), dp(16), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);

        for (int i = 0; i < tenDiem.size(); i++) {
            final int chiSo = i;
            boolean dangChon = i == viTri;

            TextView the = new TextView(this);
            the.setText((i + 1) + ".  " + tenDiem.get(i)
                    + "\n" + cauHinhAuto.nhanCheDo(danhSachId.get(i)));
            the.setTextSize(14);
            the.setTypeface(null, dangChon ? Typeface.BOLD : Typeface.NORMAL);
            the.setTextColor(dangChon ? Color.WHITE : Color.parseColor("#24382D"));
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setPadding(dp(14), dp(15), dp(14), dp(15));

            GradientDrawable nen = new GradientDrawable();
            nen.setCornerRadius(dp(12));
            nen.setColor(Color.parseColor(dangChon ? "#4F7C5A" : "#EEF4EA"));
            the.setBackground(nen);

            LinearLayout.LayoutParams thamSo = new LinearLayout.LayoutParams(-1, -2);
            thamSo.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, thamSo);

            the.setOnClickListener(v -> chuyenDiem(chiSo));
        }
    }

    private int dp(int giaTri) {
        return (int) (giaTri * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void chuyenDiem(int moi) {
        if (moi < 0 || moi >= tenDiem.size()) {
            return;
        }
        boolean autoDangPhat = huongDan != null && huongDan.dangPhatTuDong();
        if (autoDangPhat && moi == viTri) return;
        xacNhanChuyenDiem(autoDangPhat, tenDiem.get(moi), () -> {
            dungAutoThuCong();
            dungThuyetMinh();
            viTri = moi;
            hienThi();
            moKhungThuyetMinh(false);
        });
    }



    @Override public void hienThiDiemDemo(int id) {
        int index = danhSachId.indexOf(id);
        if (index < 0) return;
        dungThuyetMinh();
        viTri = index;
        hienThi();
    }

    @Override public boolean phatDiemTuDong(int id) {
        if (!sanSangDoc || tts == null || !danhSachId.contains(id)
                || !getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) return false;
        hienThiDiemDemo(id);
        return docDiemHienTai();
    }

    private boolean docDiemHienTai() {
        luotDoc = "van_mieu_" + danhSachId.get(viTri) + "_" + System.nanoTime();
        chuanBiLichSu(luotDoc, 4, danhSachId.get(viTri), huongDan == null ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu());
        boolean ok = tts.speak(tenDiem.get(viTri) + ". " + noiDungDiem.get(viTri),
                TextToSpeech.QUEUE_FLUSH, null, luotDoc) == TextToSpeech.SUCCESS;
        if (!ok) { luotDoc = null; huyLuotLichSu(); }
        return ok;
    }

    private void dungThuyetMinh() {
        huyLuotLichSu();
        dongKhungThuyetMinh();
        luotDoc = null;
        if (tts != null) tts.stop();
    }

    @Override public void dungDocTour() { dungThuyetMinh(); }

    private void caiDatTheoDoiDoc() {
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override public void onStart(String id) { batDauLichSu(id); }
            @Override public void onDone(String id) { ketThucDoc(id, true); }
            @Override public void onError(String id) { ketThucDoc(id, false); }
            @Override public void onError(String id, int maLoi) { ketThucDoc(id, false); }
        });
    }

    private void ketThucDoc(String id, boolean thanhCong) {
        runOnUiThread(() -> {
            if (isDestroyed() || id == null || !id.equals(luotDoc)) return;
            luotDoc = null;
            if (huongDan != null) huongDan.docXong(thanhCong);
        });
    }

    @Override protected void dungAmThanhThamQuan() {
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
