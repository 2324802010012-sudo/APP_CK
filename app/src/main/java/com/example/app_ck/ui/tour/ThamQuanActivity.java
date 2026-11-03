package com.example.app_ck.ui.tour;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import android.view.View;
import com.example.app_ck.ui.common.AnhDiaDiem;
/** Tour dùng chung: ID điểm và tầng được đọc từ SQLite, độc lập thứ tự danh sách. */
public class ThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {
    private final List<DiemThamQuan> cacDiem = new ArrayList<>();
    private final LinkedHashMap<Integer, String> cacTang = new LinkedHashMap<>();
    private AppDatabase dbHelper;
    private int idDiaDiem, idDangChon = -1, tangDangXem;
    private boolean daTaoGiaoDien;
    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc, nutTruoc, nutTiep;
    private LinearLayout danhSachKhuVuc;
    private ProgressBar tienTrinh;
    private HuongDanTour huongDan;
    private CauHinhAutoDiSan cauHinhAuto;
    private TextToSpeech tts;
    private boolean sanSangDoc;
    private String luotDoc;
    private boolean canNapLai;

    private final androidx.activity.result.ActivityResultLauncher<String[]> quyenTour =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (huongDan != null) {
                            huongDan.nhanQuyen(Boolean.TRUE.equals(
                                    result.get(android.Manifest.permission.ACCESS_FINE_LOCATION)));
                        }
                    });

    @Override public void xinQuyenTour() {
        quyenTour.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    protected int diaDiemMacDinh() { return 0; }
    protected int diemMacDinh() { return -1; }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        idDiaDiem = getIntent().getIntExtra("viTriDiaDiem", diaDiemMacDinh());
        if (chuyenSangTourRieng()) return;
        setContentView(R.layout.activity_tham_quan);
        dbHelper = new AppDatabase(this);
        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        danhSachKhuVuc = findViewById(R.id.tqDanhSachKhuVuc);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
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
                ? savedInstanceState.getInt("tangDangXem", diem == null ? 0 : diem.tang)
                : diem == null ? 0 : diem.tang;
        if (!cacTang.containsKey(tangDangXem) && !cacTang.isEmpty()) {
            tangDangXem = cacTang.keySet().iterator().next();
        }
        daTaoGiaoDien = true;
        hienThiKhuVuc();
        khoiTaoTts();
        taoBangHuongDan();
        apDungKhoangTrongHeThong();
    }

    /** Giữ các tour có AUTO riêng; địa điểm mới dùng danh sách/sơ đồ dùng chung. */
    private boolean chuyenSangTourRieng() {
        if (getClass() != ThamQuanActivity.class) return false;
        Intent intent = DieuHuongThamQuan.taoIntent(this, idDiaDiem,
                getIntent().getIntExtra("idMarker", -1));
        if (intent.getComponent() == null
                || ThamQuanActivity.class.getName().equals(intent.getComponent().getClassName())) return false;
        if (!getIntent().hasExtra("idMarker")) intent.removeExtra("idMarker");
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
        return true;
    }

    private boolean taiDuLieu() {
        try {
            cacDiem.clear();
            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(dbHelper.getReadableDatabase(), idDiaDiem));
            // Dung chung cau hinh AUTO SUB doc tu SQLite nhu cac tour di san cu.
            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(
                    dbHelper.getReadableDatabase(), idDiaDiem);
            cacTang.clear();
            cacTang.putAll(KhoDuLieuThamQuan.cacTang(dbHelper.getReadableDatabase(), idDiaDiem));
            for (DiemThamQuan diem : cacDiem) {
                if (!cacTang.containsKey(diem.tang)) cacTang.put(diem.tang, "Khu vực " + diem.tang);
            }
            TextView tenDiaDiem = findViewById(R.id.tqTenDiaDiem);
            ImageView anh = findViewById(R.id.tqAnhDiaDiem);
            tenDiaDiem.setText("Địa điểm tham quan");
            anh.setImageDrawable(null);
            for (DiaDiem diaDiem : dbHelper.danhSachDiaDiem()) {
                if (diaDiem.id != idDiaDiem) continue;
                tenDiaDiem.setText(diaDiem.ten);
                AnhDiaDiem.hien(anh, diaDiem.tenAnh);
                break;
            }
            return true;
        } catch (Exception ex) {
            android.util.Log.e("TOUR_SQLITE", "Không đọc được dữ liệu tham quan", ex);
            Toast.makeText(this, "Không thể đọc dữ liệu tham quan.", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (!daTaoGiaoDien || !canNapLai) return;
        canNapLai = false;
        if (isFinishing()) return;
        if (!taiDuLieu()) return;
        if (!cacTang.containsKey(tangDangXem) && !cacTang.isEmpty()) {
            DiemThamQuan diem = timDiem(idDangChon);
            tangDangXem = diem == null ? cacTang.keySet().iterator().next() : diem.tang;
        }
        hienThiKhuVuc();
        // Nap lai cau hinh sau khi Admin cap nhat; khong tiep tuc AUTO cu.
        if (huongDan != null) {
            huongDan.dung("");
        }
        taoBangHuongDan();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("idDiemDangChon", idDangChon);
        state.putInt("tangDangXem", tangDangXem);
        super.onSaveInstanceState(state);
    }

    private DiemThamQuan timDiem(int id) {
        for (DiemThamQuan diem : cacDiem) if (diem.id == id) return diem;
        return null;
    }

    private int viTriDiem(int id) {
        for (int i = 0; i < cacDiem.size(); i++) if (cacDiem.get(i).id == id) return i;
        return -1;
    }

    private int soHienThi(DiemThamQuan diem) {
        return diem.soHienThi > 0 ? diem.soHienThi : diem.id;
    }

    private String tenTang(int tang) {
        String ten = cacTang.get(tang);
        return ten == null || ten.trim().isEmpty() ? "Khu vực " + tang : ten;
    }

    private void hienThiKhuVuc() {
        DiemThamQuan diem = timDiem(idDangChon);
        if (diem == null || diem.tang != tangDangXem) {
            idDangChon = -1;
            for (DiemThamQuan ungVien : cacDiem) {
                if (ungVien.tang == tangDangXem) {
                    diem = ungVien;
                    idDangChon = diem.id;
                    break;
                }
            }
            if (idDangChon == -1) diem = null;
        }
        // Đọc lại bản ghi được chọn để tên và bài nghe luôn cùng nguồn SQLite.
        if (diem != null) {
            diem = KhoDuLieuThamQuan.lay(dbHelper.getReadableDatabase(), idDiaDiem, diem.id);
        }
        int viTri = diem == null ? -1 : viTriDiem(diem.id);
        tenKhuVuc.setText(diem == null ? "Chưa có điểm tham quan" : diem.ten);

// Dong bo anh rieng cua Marker dang chon
        ImageView anhDiem = findViewById(
                R.id.tqAnhDiemThanhPhan
        );

        String tenAnhDiem = diem == null
                ? ""
                : diem.tenAnh;

        boolean coAnh = false;

        if (tenAnhDiem != null
                && !tenAnhDiem.trim().isEmpty()) {

            if (tenAnhDiem.startsWith("anh_may:")) {
                String tenFile = tenAnhDiem.substring(8);

                if (!tenFile.isEmpty()
                        && !tenFile.contains("/")
                        && !tenFile.contains("\\")
                        && !tenFile.contains("..")) {

                    java.io.File file = new java.io.File(
                            new java.io.File(
                                    getFilesDir(),
                                    "anh_dia_diem"
                            ),
                            tenFile
                    );

                    coAnh = file.isFile();
                }

            } else {
                coAnh = getResources().getIdentifier(
                        tenAnhDiem,
                        "drawable",
                        getPackageName()
                ) != 0;
            }
        }

        if (coAnh) {
            AnhDiaDiem.hien(anhDiem, tenAnhDiem);
            anhDiem.setVisibility(View.VISIBLE);
        } else {
            anhDiem.setImageDrawable(null);
            anhDiem.setVisibility(View.GONE);
        }

        moTaKhuVuc.setText(diem == null
                ? "Nội dung tham quan cho khu vực này đang được bổ sung."
                : diem.noiDung == null || diem.noiDung.trim().isEmpty() ? diem.moTa : diem.noiDung);
        ganNguonThongTin(idDiaDiem, diem == null ? 0 : diem.id);
        findViewById(R.id.tqNutPhat).setEnabled(diem != null);
        demKhuVuc.setText(viTri < 0 ? "0/" + cacDiem.size()
                : "Điểm " + (viTri + 1) + "/" + cacDiem.size());
        tienTrinh.setProgress(viTri < 0 ? 0 : (viTri + 1) * 100 / cacDiem.size());
        nutTruoc.setEnabled(viTri > 0);
        nutTiep.setEnabled(viTri >= 0 && viTri < cacDiem.size() - 1);
        nutTruoc.setAlpha(nutTruoc.isEnabled() ? 1f : .35f);
        nutTiep.setAlpha(nutTiep.isEnabled() ? 1f : .35f);
        danhSachKhuVuc.removeAllViews();
        taoSoDo();
        taoDanhSach();
    }

    private void taoSoDo() {
        HorizontalScrollView thanhCuon = new HorizontalScrollView(this);
        thanhCuon.setHorizontalScrollBarEnabled(false);
        LinearLayout thanhTang = new LinearLayout(this);
        thanhTang.setOrientation(LinearLayout.HORIZONTAL);
        thanhCuon.addView(thanhTang);
        danhSachKhuVuc.addView(thanhCuon);
        for (int tang : cacTang.keySet()) {
            boolean dangXem = tangDangXem == tang;
            TextView nutTang = taoNhan(tenTang(tang), 13, dangXem);
            nutTang.setGravity(Gravity.CENTER);
            nutTang.setPadding(dp(14), dp(12), dp(14), dp(12));
            nutTang.setTextColor(dangXem ? Color.WHITE : Color.parseColor("#24382D"));
            nutTang.setBackground(nen(dangXem ? "#4F7C5A" : "#EEF4EA",
                    dangXem ? "#4F7C5A" : "#E6EDE5"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(7);
            lp.bottomMargin = dp(10);
            thanhTang.addView(nutTang, lp);
            nutTang.setOnClickListener(v -> {
                if (tangDangXem != tang) {
                    dungAutoVaNgheThuCong();
                    tangDangXem = tang;
                    hienThiKhuVuc();
                }
            });
        }
        AnhSoDoTour.themSoDo(this, danhSachKhuVuc, idDiaDiem, tangDangXem,
                idDangChon, id -> chonDiem(id, true));
    }

    private void taoDanhSach() {
        TextView tieuDe = taoNhan("Các điểm tham quan · " + tenTang(tangDangXem), 17, true);
        tieuDe.setPadding(dp(4), dp(8), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);
        for (DiemThamQuan diem : cacDiem) {
            if (diem.tang != tangDangXem) continue;
            boolean chon = diem.id == idDangChon;
            String cheDo = cauHinhAuto == null ? "Nghe thu cong"
                    : cauHinhAuto.nhanCheDo(diem.id);
            TextView the = taoNhan(soHienThi(diem) + ".  " + diem.ten
                    + "\n" + cheDo, 14, chon);
            the.setPadding(dp(14), dp(16), dp(14), dp(16));
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setTextColor(chon ? Color.WHITE : Color.parseColor("#24382D"));
            the.setBackground(nen(chon ? "#4F7C5A" : "#EEF4EA", chon ? "#4F7C5A" : "#E6EDE5"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, lp);
            the.setOnClickListener(v -> chonDiem(diem.id, true));
        }
    }

    private void chuyenTheoThuTu(int buoc) {
        int viTri = viTriDiem(idDangChon);
        int moi = viTri + buoc;
        if (viTri >= 0 && moi >= 0 && moi < cacDiem.size())
            chonDiem(cacDiem.get(moi).id, true);
    }

    private void chonDiem(int id, boolean moTrinhNghe) {
        DiemThamQuan diem = timDiem(id);
        if (diem == null) return;
        boolean autoDangPhat = huongDan != null && huongDan.dangPhatTuDong();
        if (autoDangPhat && idDangChon == diem.id) return;
        xacNhanChuyenDiem(autoDangPhat, diem.ten, () -> {
            dungAutoVaNgheThuCong();
            idDangChon = diem.id;
            tangDangXem = diem.tang;
            hienThiKhuVuc();
            if (moTrinhNghe) moKhungThuyetMinh(false);
        });
    }

    /** Tai su dung dung HuongDanTour tu cac tour Hue/Van Mieu/Con Dao. */
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
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {
                batDauLichSu(id);
            }
            @Override public void onDone(String id) {
                ketThucDoc(id, true);
            }
            @Override public void onError(String id) {
                ketThucDoc(id, false);
            }
            @Override public void onError(String id, int errorCode) {
                ketThucDoc(id, false);
            }
        });
    }

    private void ketThucDoc(String id, boolean thanhCong) {
        runOnUiThread(() -> {
            if (isDestroyed() || id == null || !id.equals(luotDoc)) return;
            luotDoc = null;
            if (huongDan != null) huongDan.docXong(thanhCong);
        });
    }

    /** DEMO chi cap nhat man hinh, KHONG ghi vi tri mo phong vao SQLite. */
    @Override public void hienThiDiemDemo(int id) {
        DiemThamQuan diem = timDiem(id);
        if (diem == null) return;
        dongKhungThuyetMinh();
        idDangChon = diem.id;
        tangDangXem = diem.tang;
        hienThiKhuVuc();
    }

    /** GPS/DEMO tu luong cu goi phat bai cua ID diem tu SQLite. */
    @Override public boolean phatDiemTuDong(int id) {
        if (!sanSangDoc || tts == null || isFinishing() || isDestroyed()
                || !getLifecycle().getCurrentState().isAtLeast(
                androidx.lifecycle.Lifecycle.State.RESUMED)) return false;
        DiemThamQuan diem = timDiem(id);
        if (diem == null) return false;
        // Lay noi dung moi nhat do Admin luu, khong lay bai tinh trong Java.
        diem = KhoDuLieuThamQuan.lay(
                dbHelper.getReadableDatabase(), idDiaDiem, id);
        if (diem == null) return false;
        String noiDung = diem.noiDung == null || diem.noiDung.trim().isEmpty()
                ? diem.moTa : diem.noiDung;
        if (noiDung == null || noiDung.trim().isEmpty()) return false;
        hienThiDiemDemo(id);
        luotDoc = "auto_sub_" + idDiaDiem + "_" + id + "_" + System.nanoTime();
        chuanBiLichSu(luotDoc, idDiaDiem, id,
                huongDan == null ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu());
        int ketQua = tts.speak(diem.ten + ". " + noiDung,
                TextToSpeech.QUEUE_FLUSH, null, luotDoc);
        if (ketQua != TextToSpeech.SUCCESS) {
            luotDoc = null;
            huyLuotLichSu();
            return false;
        }
        return true;
    }

    @Override public void dungDocTour() {
        dungThuyetMinh();
    }

    private void dungAutoVaNgheThuCong() {
        if (huongDan != null) huongDan.dung("Da chuyen sang nghe thu cong.");
        dungThuyetMinh();
    }

    private int dp(float giaTri) {
        return Math.round(giaTri * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable nen(String mau, String vien) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor(mau));
        d.setCornerRadius(dp(12));
        d.setStroke(dp(1), Color.parseColor(vien));
        return d;
    }

    private TextView taoNhan(String s, int size, boolean dam) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor("#24382D"));
        t.setTypeface(null, dam ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    private void dungThuyetMinh() {
        // Chuyen che do/dung tour thi huy luot doc cu va lich su dang cho.
        luotDoc = null;
        huyLuotLichSu();
        dongKhungThuyetMinh();
        if (tts != null) tts.stop();
    }

    @Override protected void dungAmThanhThamQuan() {
        dungAutoVaNgheThuCong();
    }

    @Override protected void onStop() {
        canNapLai = true;
        if (huongDan != null) huongDan.dung("");
        dungThuyetMinh();
        super.onStop();
    }

    @Override protected void onDestroy() {
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
