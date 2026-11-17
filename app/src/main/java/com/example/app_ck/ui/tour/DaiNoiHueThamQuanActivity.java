
package com.example.app_ck.ui.tour;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
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
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.BoHienThiAnh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class DaiNoiHueThamQuanActivity extends BaseActivity
        implements HuongDanTour.ManHinh {

    private String tenAnhSoDo = "";
    private int tangSoDo = Integer.MIN_VALUE;
    private boolean canNapLai;

    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private final ArrayList<String> danhSachNoiDung = new ArrayList<>();
    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> danhSachTen = new ArrayList<>();
    private final ArrayList<String> danhSachMoTa = new ArrayList<>();
    private final ArrayList<DiemMarker> cacMarker = new ArrayList<>();

    private final LinkedHashMap<Integer, String> cacTang =
            new LinkedHashMap<>();

    private int viTriHienTai = 0;

    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc;
    private TextView nutTruoc, nutTiep;

    private ProgressBar tienTrinh;
    private LinearLayout khungDanhSach;

    private GiaoDienTour giaoDienTour;

    private SoDoView soDo;

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
    protected void onCreate(Bundle trangThai) {

        super.onCreate(trangThai);

        setContentView(R.layout.activity_tham_quan);

        giaoDienTour = new GiaoDienTour(this, trangThai);

        anhXa();

        if (!napSQLite()) {

            Toast.makeText(this,
                    "Không đọc được dữ liệu Đại Nội Huế.",
                    Toast.LENGTH_LONG).show();

            finish();

            return;
        }

        int idMo = trangThai == null
                ? getIntent().getIntExtra("idMarker", 1)
                : trangThai.getInt("idDiemDangChon", 1);

        viTriHienTai = Math.max(0, danhSachId.indexOf(idMo));

        caiDatNut();

        khoiTaoTTS();

        hienThiDiem();

        huongDan = new HuongDanTour(
                this,
                findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto
        );

        caiDatTheoDoiDoc();

        apDungKhoangTrongHeThong();
    }

    private void anhXa() {

        ((TextView) findViewById(R.id.tqTenDiaDiem))
                .setText("Đại Nội Huế");

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);

        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);

        tienTrinh = findViewById(R.id.tqTienTrinh);
        khungDanhSach = findViewById(R.id.tqKhungSoDo);
    }

    private boolean napSQLite() {

        try (AppDatabase helper = new AppDatabase(this)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            danhSachId.clear();
            danhSachTen.clear();
            danhSachMoTa.clear();
            danhSachNoiDung.clear();
            cacDiem.clear();
            cacTang.clear();

            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(db, 2));
            cacTang.putAll(KhoDuLieuThamQuan.cacTang(db, 2));

            for (DiemThamQuan d : cacDiem) {

                danhSachId.add(d.id);
                danhSachTen.add(d.ten);
                danhSachMoTa.add(d.moTa);

                String noiDung = d.noiDung == null
                        || d.noiDung.trim().isEmpty()
                        ? d.moTa : d.noiDung;

                danhSachNoiDung.add(
                        noiDung == null ? "" : noiDung
                );
            }

            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(db, 2);

            ((TextView) findViewById(R.id.tqTenDiaDiem))
                    .setText(cauHinhAuto.ten);

            AnhSoDoTour.ganAnhDaiDien(this, cauHinhAuto.tenAnh);

            tangSoDo = Integer.MIN_VALUE;

            return !danhSachId.isEmpty();

        } catch (Exception ex) {

            Log.e("DAI_NOI_HUE", "Lỗi dữ liệu SQLite", ex);

            return false;
        }
    }

    private void capNhatSoDo() {

        int tang = cacDiem.get(viTriHienTai).tang;

        if (tang == tangSoDo) return;

        tangSoDo = tang;

        cacMarker.clear();

        for (DiemThamQuan d : cacDiem) {

            if (d.tang == tang && d.coMarker()) {

                cacMarker.add(new DiemMarker(
                        d.id,
                        d.soHienThi > 0 ? d.soHienThi : d.id,
                        (float) d.x,
                        (float) d.y
                ));
            }
        }

        try (AppDatabase helper = new AppDatabase(this)) {

            tenAnhSoDo = KhoDuLieuThamQuan.anhSoDo(
                    helper.getReadableDatabase(), 2, tang
            );
        }

        khungDanhSach.removeAllViews();

        if (soDo != null) {

            soDo.giaiPhongAnh();

            soDo = null;
        }

        taoGiaoDienSoDo();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!canNapLai) return;

        canNapLai = false;

        if (isFinishing() || danhSachId.isEmpty()) return;

        int id = danhSachId.get(viTriHienTai);

        if (huongDan != null) huongDan.dung("");

        if (!napSQLite()) {

            finish();

            return;
        }

        viTriHienTai = Math.max(0, danhSachId.indexOf(id));

        hienThiDiem();

        ViewGroup khung = findViewById(R.id.tqKhungHuongDan);

        khung.removeAllViews();

        huongDan = new HuongDanTour(this, khung, cauHinhAuto);
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

    private void caiDatNut() {

        findViewById(R.id.tqNutQuayLai)
                .setOnClickListener(v -> finish());

        nutTruoc.setOnClickListener(v -> chonChiSo(viTriHienTai - 1));

        nutTiep.setOnClickListener(v -> chonChiSo(viTriHienTai + 1));
    }

    // GIAO DIEN SO DO


    private void taoGiaoDienSoDo() {

        TextView tieuDe = new TextView(this);

        tieuDe.setText("Sơ đồ tham quan");

        tieuDe.setTextSize(17);

        tieuDe.setTextColor(
                Color.parseColor("#182D22")
        );

        tieuDe.setTypeface(
                Typeface.create(
                        "sans-serif-medium",
                        Typeface.NORMAL
                )
        );

        tieuDe.setPadding(dp(3), dp(8), dp(3), dp(12));

        khungDanhSach.addView(tieuDe);

        Bitmap anh = AnhSoDoTour.docAnhSoDo(
                this,
                tenAnhSoDo
        );

        if (anh == null) {

            TextView thieu = new TextView(this);

            thieu.setText(
                    "Chưa có sơ đồ. Bạn vẫn có thể "
                            + "chọn điểm trong tab Danh sách."
            );

            thieu.setTextSize(14);

            thieu.setTextColor(
                    Color.parseColor("#687B6E")
            );

            thieu.setPadding(dp(12), dp(20), dp(12), dp(20));

            khungDanhSach.addView(thieu);

            return;
        }

        soDo = new SoDoView(this, anh);

        float manHinhDp =
                getResources().getDisplayMetrics().heightPixels
                        / getResources()
                        .getDisplayMetrics().density;

        int chieuCao = Math.round(
                Math.max(
                        240,
                        Math.min(400, manHinhDp * 0.45f)
                )
        );

        GiaoDienSoDo.themVao(
                khungDanhSach,
                soDo,
                dp(chieuCao),
                () -> soDo.doiZoom(1f / 1.5f),
                soDo::datLai,
                () -> soDo.doiZoom(1.5f)
        );
    }


    private void taoNutZoom(
            LinearLayout hang, String chu, Runnable lenh) {

        TextView nut = new TextView(this);

        nut.setText(chu);
        nut.setTextSize(12);
        nut.setGravity(Gravity.CENTER);

        nut.setTypeface(
                Typeface.create("sans-serif-medium", Typeface.NORMAL)
        );

        nut.setTextColor(Color.parseColor("#2C7351"));

        nut.setBackgroundResource(R.drawable.ui_ct_audio_bg);

        nut.setClickable(true);
        nut.setFocusable(true);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, -1, 1f);

        lp.leftMargin = dp(4);
        lp.rightMargin = dp(4);

        hang.addView(nut, lp);

        nut.setOnClickListener(v -> lenh.run());
    }

    private void hienThiDiem() {

        if (danhSachId.isEmpty()) return;

        capNhatSoDo();

        tenKhuVuc.setText(danhSachTen.get(viTriHienTai));

        String noiDung = danhSachNoiDung.get(viTriHienTai);

        moTaKhuVuc.setText(noiDung);

        BoHienThiAnh.hienAnhDiem(
                findViewById(R.id.tqAnhDiemThanhPhan),
                cacDiem.get(viTriHienTai).tenAnh
        );

        ganNguonThongTin(2, danhSachId.get(viTriHienTai));

        TextView nutPhat = findViewById(R.id.tqNutPhat);

        boolean coBai = !noiDung.trim().isEmpty();

        nutPhat.setEnabled(coBai);
        nutPhat.setAlpha(coBai ? 1f : 0.5f);
        nutPhat.setText(coBai
                ? "Nghe thuyết minh" : "Chưa có thuyết minh");

        demKhuVuc.setText(
                "Điểm " + (viTriHienTai + 1)
                        + "/" + danhSachId.size()
        );

        tienTrinh.setProgress(
                (viTriHienTai + 1) * 100 / danhSachId.size()
        );

        nutTruoc.setEnabled(viTriHienTai > 0);

        nutTiep.setEnabled(viTriHienTai < danhSachId.size() - 1);

        nutTruoc.setAlpha(nutTruoc.isEnabled() ? 1f : .35f);
        nutTiep.setAlpha(nutTiep.isEnabled() ? 1f : .35f);

        if (soDo != null) soDo.invalidate();

        giaoDienTour.capNhat(
                cacDiem,
                danhSachId.get(viTriHienTai),
                id -> cauHinhAuto != null
                        && cauHinhAuto.choPhepAuto(id),
                cacTang::get,
                id -> {

                    int index = danhSachId.indexOf(id);

                    if (index >= 0) chonChiSo(index);
                }
        );
    }


    private void chonChiSo(int chiSo) {

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

                    dungAutoThuCong();

                    dungTTS();

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

    private void chonMarker(int id) {

        int index = danhSachId.indexOf(id);

        if (index >= 0) chonChiSo(index);
    }

    private static final class DiemMarker {

        final int id, soHienThi;
        final float x, y;

        DiemMarker(int id, int soHienThi, float x, float y) {

            this.id = id;
            this.soHienThi = soHienThi;
            this.x = x;
            this.y = y;
        }
    }

    // VIEW SO DO RIENG CUA DAI NOI HUE

    private final class SoDoView extends View {

        private final Bitmap anh;

        private final Paint but = new Paint(
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        private final RectF vungAnh = new RectF();

        private final float matDo;
        private final int nguongKeo;
        private final ScaleGestureDetector boThuPhong;

        private float zoom = 1f;
        private float dx = 0f, dy = 0f;
        private float xTruoc, yTruoc, xBanDau, yBanDau;

        private boolean daKeo, daDaChamHaiNgon;

        SoDoView(Context context, Bitmap bitmap) {

            super(context);

            anh = bitmap;

            matDo = getResources().getDisplayMetrics().density;

            nguongKeo = ViewConfiguration.get(context)
                    .getScaledTouchSlop();

            boThuPhong = new ScaleGestureDetector(
                    context,
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                        @Override
                        public boolean onScale(ScaleGestureDetector d) {

                            zoom = Math.max(
                                    1f,
                                    Math.min(5f, zoom * d.getScaleFactor())
                            );

                            gioiHan();
                            invalidate();

                            return true;
                        }
                    }
            );

            setClickable(true);

            setContentDescription(
                    "Sơ đồ Hoàng thành Huế có "
                            + cacMarker.size() + " Marker tương tác"
            );
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            canvas.drawColor(Color.parseColor("#F1F4EE"));

            if (anh == null || anh.isRecycled()
                    || getWidth() <= 0 || getHeight() <= 0) {

                return;
            }

            float tiLe = Math.min(
                    getWidth() / (float) anh.getWidth(),
                    getHeight() / (float) anh.getHeight()
            );

            float w = tiLe * anh.getWidth();
            float h = tiLe * anh.getHeight();

            vungAnh.set(
                    (getWidth() - w) / 2f,
                    (getHeight() - h) / 2f,
                    (getWidth() + w) / 2f,
                    (getHeight() + h) / 2f
            );

            canvas.save();

            canvas.clipRect(0, 0, getWidth(), getHeight());

            canvas.translate(
                    getWidth() / 2f + dx,
                    getHeight() / 2f + dy
            );

            canvas.scale(zoom, zoom);

            canvas.translate(
                    -getWidth() / 2f,
                    -getHeight() / 2f
            );

            but.setStyle(Paint.Style.FILL);
            but.setColor(Color.WHITE);

            canvas.drawBitmap(anh, null, vungAnh, but);

            DiemMarker diemChon = null;

            for (DiemMarker diem : cacMarker) {

                if (diem.id == danhSachId.get(viTriHienTai)) {

                    diemChon = diem;

                } else {

                    veMarker(canvas, diem, false);
                }
            }

            if (diemChon != null) {

                veMarker(canvas, diemChon, true);
            }

            canvas.restore();
        }


        private void veMarker(
                Canvas canvas,
                DiemMarker diem,
                boolean dangChon) {

            float x = vungAnh.left
                    + diem.x * vungAnh.width();

            float y = vungAnh.top
                    + diem.y * vungAnh.height();

            GiaoDienSoDo.veMarker(
                    getContext(),
                    canvas,
                    but,
                    x,
                    y,
                    diem.soHienThi,
                    dangChon,
                    false,
                    zoom
            );
        }


        void giaiPhongAnh() {

            if (anh != null && !anh.isRecycled()) {

                anh.recycle();
            }
        }

        void doiZoom(float heSo) {

            zoom = Math.max(1f, Math.min(5f, zoom * heSo));

            gioiHan();
            invalidate();
        }

        void datLai() {

            zoom = 1f;
            dx = 0f;
            dy = 0f;

            invalidate();
        }

        private void gioiHan() {

            float bienX = Math.max(0f, (zoom - 1f) * getWidth() / 2f);

            float bienY = Math.max(0f, (zoom - 1f) * getHeight() / 2f);

            dx = Math.max(-bienX, Math.min(bienX, dx));

            dy = Math.max(-bienY, Math.min(bienY, dy));
        }

        private float veXGoc(float x) {

            return (x - getWidth() / 2f - dx) / zoom
                    + getWidth() / 2f;
        }

        private float veYGoc(float y) {

            return (y - getHeight() / 2f - dy) / zoom
                    + getHeight() / 2f;
        }

        private void chanCuon(boolean chan) {

            if (getParent() != null) {

                getParent().requestDisallowInterceptTouchEvent(chan);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {

            boThuPhong.onTouchEvent(e);

            switch (e.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    xBanDau = xTruoc = e.getX();
                    yBanDau = yTruoc = e.getY();

                    daKeo = false;
                    daDaChamHaiNgon = false;

                    chanCuon(true);

                    return true;

                case MotionEvent.ACTION_POINTER_DOWN:

                    daDaChamHaiNgon = true;

                    chanCuon(true);

                    return true;

                case MotionEvent.ACTION_MOVE:

                    if (e.getPointerCount() > 1) {

                        daDaChamHaiNgon = true;

                        return true;
                    }

                    if (Math.abs(e.getX() - xBanDau) > nguongKeo
                            || Math.abs(e.getY() - yBanDau) > nguongKeo) {

                        daKeo = true;
                    }

                    if (zoom > 1f && !daDaChamHaiNgon) {

                        dx += e.getX() - xTruoc;
                        dy += e.getY() - yTruoc;

                        gioiHan();
                        invalidate();

                    } else if (zoom <= 1f && daKeo) {

                        chanCuon(false);
                    }

                    xTruoc = e.getX();
                    yTruoc = e.getY();

                    return true;

                case MotionEvent.ACTION_POINTER_UP:

                    daDaChamHaiNgon = true;

                    return true;

                case MotionEvent.ACTION_UP:

                    if (!daKeo && !daDaChamHaiNgon) {

                        chonTai(
                                veXGoc(e.getX()),
                                veYGoc(e.getY())
                        );

                        performClick();
                    }

                    chanCuon(false);

                    return true;

                case MotionEvent.ACTION_CANCEL:

                    chanCuon(false);

                    return true;

                default:

                    return true;
            }
        }


        private void chonTai(float xCham, float yCham) {

            int idChon = -1;

            float radius = GiaoDienSoDo.banKinhCham(
                    getContext(),
                    zoom
            );

            float kcNhoNhat = radius * radius;

            for (DiemMarker diem : cacMarker) {

                float x = vungAnh.left
                        + diem.x * vungAnh.width();

                float y = vungAnh.top
                        + diem.y * vungAnh.height();

                float dx = xCham - x;

                float dy = yCham - y;

                float kc = dx * dx + dy * dy;

                if (kc <= kcNhoNhat) {

                    kcNhoNhat = kc;

                    idChon = diem.id;
                }
            }

            if (idChon > 0) {

                chonMarker(idChon);
            }
        }
        @Override
        protected void onSizeChanged(
                int w, int h, int oldw, int oldh) {

            super.onSizeChanged(w, h, oldw, oldh);

            gioiHan();

            invalidate();
        }
        @Override
        public boolean performClick() {

            super.performClick();

            return true;
        }
    }

    // TTS VA AUTO

    private void khoiTaoTTS() {

        tts = new TextToSpeech(this, ketQua -> {

            if (ketQua != TextToSpeech.SUCCESS || tts == null) return;

            int trangThai = tts.setLanguage(new Locale("vi", "VN"));

            sanSangDoc = trangThai != TextToSpeech.LANG_MISSING_DATA
                    && trangThai != TextToSpeech.LANG_NOT_SUPPORTED;
        });
    }

    @Override
    public void hienThiDiemDemo(int id) {

        int index = danhSachId.indexOf(id);

        if (index < 0) return;

        dungTTS();

        viTriHienTai = index;

        hienThiDiem();
    }

    @Override
    public boolean phatDiemTuDong(int id) {

        if (!sanSangDoc || tts == null || !danhSachId.contains(id)
                || !getLifecycle().getCurrentState()
                .isAtLeast(Lifecycle.State.RESUMED)) {

            return false;
        }

        hienThiDiemDemo(id);

        return docDiemHienTai();
    }

    private boolean docDiemHienTai() {

        if (tts == null || !sanSangDoc) return false;

        String noiDung = danhSachNoiDung.get(viTriHienTai);

        if (noiDung.trim().isEmpty()) return false;

        luotDoc = "hue_" + danhSachId.get(viTriHienTai)
                + "_" + System.nanoTime();

        chuanBiLichSu(
                luotDoc,
                2,
                danhSachId.get(viTriHienTai),
                huongDan == null
                        ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu()
        );

        boolean ok = tts.speak(
                danhSachTen.get(viTriHienTai) + ". " + noiDung,
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

    private void dungTTS() {

        huyLuotLichSu();
        dongKhungThuyetMinh();

        luotDoc = null;

        if (tts != null) tts.stop();
    }

    @Override
    public void dungDocTour() {

        dungTTS();
    }

    private int dp(int value) {

        return Math.round(value * getResources()
                .getDisplayMetrics().density);
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
        dungTTS();
    }

    @Override
    protected void onStop() {

        canNapLai = true;

        if (huongDan != null) huongDan.dung("");

        dungTTS();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {

            tts.stop();
            tts.shutdown();

            tts = null;
        }

        if (soDo != null) {

            soDo.giaiPhongAnh();
        }

        super.onDestroy();
    }
}
