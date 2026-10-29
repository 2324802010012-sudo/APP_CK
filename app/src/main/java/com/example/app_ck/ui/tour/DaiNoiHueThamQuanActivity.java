package com.example.app_ck.ui.tour;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.common.BaseActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * APP_CK - Tham quan Đại Nội Huế.
 * Sơ đồ tương tác được vẽ trong activity_tham_quan.xml; không có Activity bản đồ riêng.
 * Ảnh, tầng, nội dung và tọa độ marker được đọc từ SQLite.
 * Tọa độ marker là tỷ lệ trên ảnh sơ đồ, độc lập với GPS dùng cho AUTO.
 */
public class DaiNoiHueThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {

    private String tenAnhSoDo = "";
    private int tangSoDo = Integer.MIN_VALUE;
    private boolean canNapLai;
    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private final ArrayList<String> danhSachNoiDung = new ArrayList<>();
    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> danhSachTen = new ArrayList<>();
    private final ArrayList<String> danhSachMoTa = new ArrayList<>();
    private final ArrayList<DiemMarker> cacMarker = new ArrayList<>();

    private int viTriHienTai = 0;
    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc, nutTruoc, nutTiep;
    private ProgressBar tienTrinh;
    private LinearLayout khungDanhSach;
    private LinearLayout danhSachThe;
    private SoDoView soDo;
    private HuongDanTour huongDan;
    private CauHinhAutoDiSan cauHinhAuto;
    private String luotDoc;
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
    private boolean sanSangDoc;

    @Override
    protected void onCreate(Bundle trangThai) {
        super.onCreate(trangThai);
        setContentView(R.layout.activity_tham_quan);
        anhXa();
        if (!napSQLite()) {
            Toast.makeText(this, "Không đọc được dữ liệu Đại Nội Huế.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        int idMo = getIntent().getIntExtra("idMarker", 1);
        for (int i = 0; i < danhSachId.size(); i++) {
            if (danhSachId.get(i) == idMo) {
                viTriHienTai = i;
                break;
            }
        }
        caiDatNut();
        khoiTaoTTS();
        hienThiDiem();
        huongDan = new HuongDanTour(this, findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto);
        caiDatTheoDoiDoc();
        apDungKhoangTrongHeThong();
    }

    private void anhXa() {
        ((TextView) findViewById(R.id.tqTenDiaDiem)).setText("Đại Nội Huế");
        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        khungDanhSach = findViewById(R.id.tqDanhSachKhuVuc);
    }

    private boolean napSQLite() {
        try (AppDatabase helper = new AppDatabase(this)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            danhSachId.clear();
            danhSachTen.clear();
            danhSachMoTa.clear();
            danhSachNoiDung.clear();
            cacDiem.clear();
            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(db, 2));
            for (DiemThamQuan d : cacDiem) {
                danhSachId.add(d.id);
                danhSachTen.add(d.ten);
                danhSachMoTa.add(d.moTa);
                danhSachNoiDung.add(d.noiDung);
            }
            cauHinhAuto = CauHinhAutoDiSan.tuSQLite(db, 2);
            ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(cauHinhAuto.ten);
            AnhSoDoTour.ganAnhDaiDien(this, cauHinhAuto.tenAnh);
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
            if (d.tang == tang && d.coMarker()) cacMarker.add(new DiemMarker(d.id,
                    d.soHienThi > 0 ? d.soHienThi : d.id, (float) d.x, (float) d.y));
        }
        try (AppDatabase helper = new AppDatabase(this)) {
            tenAnhSoDo = KhoDuLieuThamQuan.anhSoDo(helper.getReadableDatabase(), 2, tang);
        }
        if (soDo != null) soDo.giaiPhongAnh();
        soDo = null;
        taoGiaoDienSoDo();
    }

    @Override protected void onResume() {
        super.onResume();
        if (!canNapLai) return;
        canNapLai = false;
        if (isFinishing() || danhSachId.isEmpty()) return;
        int id = danhSachId.get(viTriHienTai);
        if (huongDan != null) huongDan.dung("");
        if (!napSQLite()) { finish(); return; }
        viTriHienTai = Math.max(0, danhSachId.indexOf(id));
        tangSoDo = Integer.MIN_VALUE;
        hienThiDiem();
        android.view.ViewGroup khung = findViewById(R.id.tqKhungHuongDan);
        khung.removeAllViews();
        huongDan = new HuongDanTour(this, khung, cauHinhAuto);
    }

    private void caiDatNut() {
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
        nutTruoc.setOnClickListener(v -> chonChiSo(viTriHienTai - 1));
        nutTiep.setOnClickListener(v -> chonChiSo(viTriHienTai + 1));
    }

    private void taoGiaoDienSoDo() {
        khungDanhSach.removeAllViews();
        TextView tieuDe = new TextView(this);
        tieuDe.setText("⌖  Sơ đồ tương tác " + cauHinhAuto.ten);
        tieuDe.setTextColor(Color.parseColor("#233D2C"));
        tieuDe.setTypeface(null, Typeface.BOLD);
        tieuDe.setTextSize(18);
        tieuDe.setPadding(dp(3), dp(12), dp(3), dp(12));
        khungDanhSach.addView(tieuDe);

        int anh = getResources().getIdentifier(tenAnhSoDo, "drawable", getPackageName());
        if (anh == 0) {
            TextView thieuAnh = new TextView(this);
            thieuAnh.setText("Chưa có ảnh sơ đồ. Bạn vẫn chọn điểm trong danh sách để xem và nghe thuyết minh.");
            thieuAnh.setTextSize(13);
            thieuAnh.setTextColor(Color.parseColor("#9B4A24"));
            thieuAnh.setPadding(dp(12), dp(16), dp(12), dp(16));
            khungDanhSach.addView(thieuAnh);
        } else {
            soDo = new SoDoView(this, anh);
            khungDanhSach.addView(soDo, new LinearLayout.LayoutParams(-1, dp(400)));

            LinearLayout hangZoom = new LinearLayout(this);
            hangZoom.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams thamSoZoom = new LinearLayout.LayoutParams(-1, dp(48));
            thamSoZoom.topMargin = dp(8);
            khungDanhSach.addView(hangZoom, thamSoZoom);
            taoNutZoom(hangZoom, "−  Thu nhỏ", () -> soDo.doiZoom(1f / 1.5f));
            taoNutZoom(hangZoom, "↺  Đặt lại", soDo::datLai);
            taoNutZoom(hangZoom, "+  Phóng to", () -> soDo.doiZoom(1.5f));
        }

        TextView ghiChu = new TextView(this);
        ghiChu.setText("Chạm vào vòng tròn xanh có số để mở thuyết minh. " +
                "Dùng hai ngón tay để Zoom, kéo ảnh khi đã phóng to. " +
                "Tọa độ được căn theo ảnh sơ đồ nguồn, không phải GPS.");
        ghiChu.setTextColor(Color.parseColor("#667768"));
        ghiChu.setTextSize(12);
        ghiChu.setPadding(dp(2), dp(10), dp(2), dp(12));
        khungDanhSach.addView(ghiChu);

        TextView nhan = new TextView(this);
        nhan.setText(danhSachId.size() + " địa điểm tham quan và thuyết minh");
        nhan.setTextSize(17);
        nhan.setTypeface(null, Typeface.BOLD);
        nhan.setTextColor(Color.parseColor("#233D2C"));
        nhan.setPadding(dp(2), dp(8), dp(2), dp(12));
        khungDanhSach.addView(nhan);
        danhSachThe = new LinearLayout(this);
        danhSachThe.setOrientation(LinearLayout.VERTICAL);
        khungDanhSach.addView(danhSachThe);
    }

    private void taoNutZoom(LinearLayout hang, String chu, Runnable lenh) {
        TextView nut = new TextView(this);
        nut.setText(chu);
        nut.setTextSize(12);
        nut.setGravity(Gravity.CENTER);
        nut.setTypeface(null, Typeface.BOLD);
        nut.setTextColor(Color.parseColor("#255139"));
        GradientDrawable nen = new GradientDrawable();
        nen.setColor(Color.parseColor("#E9F1E8"));
        nen.setCornerRadius(dp(10));
        nut.setBackground(nen);
        LinearLayout.LayoutParams thamSo = new LinearLayout.LayoutParams(0, -1, 1f);
        thamSo.leftMargin = dp(4);
        hang.addView(nut, thamSo);
        nut.setOnClickListener(v -> lenh.run());
    }

    private void hienThiDiem() {
        if (danhSachId.isEmpty()) return;
        capNhatSoDo();
        tenKhuVuc.setText(danhSachTen.get(viTriHienTai));
        moTaKhuVuc.setText(danhSachMoTa.get(viTriHienTai));
        ganNguonThongTin(2, danhSachId.get(viTriHienTai));
        demKhuVuc.setText("Điểm " + (viTriHienTai + 1) + "/" + danhSachId.size());
        tienTrinh.setProgress((viTriHienTai + 1) * 100 / danhSachId.size());
        nutTruoc.setEnabled(viTriHienTai > 0);
        nutTiep.setEnabled(viTriHienTai < danhSachId.size() - 1);
        nutTruoc.setAlpha(viTriHienTai > 0 ? 1f : 0.35f);
        nutTiep.setAlpha(viTriHienTai < danhSachId.size() - 1 ? 1f : 0.35f);
        if (soDo != null) soDo.invalidate();
        if (danhSachThe == null) return;
        danhSachThe.removeAllViews();
        for (int i = 0; i < danhSachId.size(); i++) {
            final int chiSo = i;
            boolean duocChon = i == viTriHienTai;
            TextView the = new TextView(this);
            the.setText(danhSachId.get(i) + ".  " + danhSachTen.get(i)
                    + "\n" + cauHinhAuto.nhanCheDo(danhSachId.get(i)));
            the.setTextColor(duocChon ? Color.WHITE : Color.parseColor("#24382D"));
            the.setTypeface(null, duocChon ? Typeface.BOLD : Typeface.NORMAL);
            the.setTextSize(14);
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setPadding(dp(14), dp(15), dp(14), dp(15));
            GradientDrawable nen = new GradientDrawable();
            nen.setColor(Color.parseColor(duocChon ? "#347A5A" : "#EFF4EA"));
            nen.setCornerRadius(dp(10));
            the.setBackground(nen);
            LinearLayout.LayoutParams thamSo = new LinearLayout.LayoutParams(-1, -2);
            thamSo.bottomMargin = dp(7);
            danhSachThe.addView(the, thamSo);
            the.setOnClickListener(v -> chonChiSo(chiSo));
        }
    }

    private void chonChiSo(int chiSo) {
        if (chiSo < 0 || chiSo >= danhSachId.size()) return;
        boolean autoDangPhat = huongDan != null && huongDan.dangPhatTuDong();
        if (autoDangPhat && chiSo == viTriHienTai) return;
        xacNhanChuyenDiem(autoDangPhat, danhSachTen.get(chiSo), () -> {
            dungAutoThuCong();
            dungTTS();
            viTriHienTai = chiSo;
            hienThiDiem();
            moKhungThuyetMinh(false);
        });
    }

    private void chonMarker(int id) {
        for (int i = 0; i < danhSachId.size(); i++) {
            if (danhSachId.get(i) == id) {
                chonChiSo(i);
                return;
            }
        }
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

    private final class SoDoView extends View {
        private final Bitmap anh;
        private final Paint but = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF vungAnh = new RectF();
        private final float matDo;
        private final int nguongKeo;
        private final ScaleGestureDetector boThuPhong;
        private float zoom = 1f;
        private float dx = 0f, dy = 0f;
        private float xTruoc, yTruoc, xBanDau, yBanDau;
        private boolean daKeo, daDaChamHaiNgon;

        SoDoView(Context context, int anhId) {
            super(context);
            BitmapFactory.Options tuyChonAnh = new BitmapFactory.Options();
            tuyChonAnh.inScaled = false;
            anh = BitmapFactory.decodeResource(getResources(), anhId, tuyChonAnh);
            matDo = getResources().getDisplayMetrics().density;
            nguongKeo = ViewConfiguration.get(context).getScaledTouchSlop();
            boThuPhong = new ScaleGestureDetector(context,
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(ScaleGestureDetector d) {
                            zoom = Math.max(1f, Math.min(5f, zoom * d.getScaleFactor()));
                            gioiHan();
                            invalidate();
                            return true;
                        }
                    });
            setClickable(true);
            setContentDescription("Sơ đồ Hoàng thành Huế có " + cacMarker.size() + " Marker tương tác");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.parseColor("#F1F4EE"));
            if (anh == null || getWidth() <= 0 || getHeight() <= 0) return;
            float tiLe = Math.min(getWidth() / (float) anh.getWidth(),
                    getHeight() / (float) anh.getHeight());
            float w = tiLe * anh.getWidth(), h = tiLe * anh.getHeight();
            vungAnh.set((getWidth() - w) / 2f, (getHeight() - h) / 2f,
                    (getWidth() + w) / 2f, (getHeight() + h) / 2f);
            canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.translate(getWidth() / 2f + dx, getHeight() / 2f + dy);
            canvas.scale(zoom, zoom);
            canvas.translate(-getWidth() / 2f, -getHeight() / 2f);
            but.setStyle(Paint.Style.FILL);
            but.setColor(Color.WHITE);
            canvas.drawBitmap(anh, null, vungAnh, but);
            for (DiemMarker diem : cacMarker) {
                float x = vungAnh.left + diem.x * vungAnh.width();
                float y = vungAnh.top + diem.y * vungAnh.height();
                boolean dangChon = diem.id == danhSachId.get(viTriHienTai);
                // Kích thước theo pixel ảnh gốc, cùng biến đổi với ảnh khi zoom/kéo.
                float tiLeMarker = vungAnh.width() / 2048f;
                float banKinh = 14f * tiLeMarker;
                but.setShadowLayer(2 * tiLeMarker, 0, tiLeMarker, 0x66000000);
                but.setColor(Color.WHITE);
                canvas.drawCircle(x, y, banKinh, but);
                but.clearShadowLayer();
                but.setColor(Color.parseColor(dangChon ? "#E5A000" : "#087D59"));
                canvas.drawCircle(x, y, banKinh * 16f / 19f, but);
                but.setTextAlign(Paint.Align.CENTER);
                but.setTextSize(banKinh * 12f / 19f);
                but.setTypeface(Typeface.DEFAULT_BOLD);
                but.setColor(Color.WHITE);
                canvas.drawText(String.valueOf(diem.soHienThi), x,
                        y - (but.ascent() + but.descent()) / 2f, but);
            }
            canvas.restore();
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
            return (x - getWidth() / 2f - dx) / zoom + getWidth() / 2f;
        }
        private float veYGoc(float y) {
            return (y - getHeight() / 2f - dy) / zoom + getHeight() / 2f;
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
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    daDaChamHaiNgon = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (e.getPointerCount() > 1) {
                        daDaChamHaiNgon = true;
                        return true;
                    }
                    if (Math.abs(e.getX() - xBanDau) > nguongKeo ||
                            Math.abs(e.getY() - yBanDau) > nguongKeo) daKeo = true;
                    if (zoom > 1f && !daDaChamHaiNgon) {
                        dx += e.getX() - xTruoc;
                        dy += e.getY() - yTruoc;
                        gioiHan();
                        invalidate();
                    } else if (zoom <= 1f && daKeo) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    xTruoc = e.getX();
                    yTruoc = e.getY();
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    daDaChamHaiNgon = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!daKeo && !daDaChamHaiNgon) {
                        chonTai(veXGoc(e.getX()), veYGoc(e.getY()));
                        performClick();
                    }
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                default:
                    return true;
            }
        }

        private void chonTai(float xCham, float yCham) {
            if (!vungAnh.contains(xCham, yCham)) return;
            int idChon = -1;
            float kcNhoNhat = (32f * matDo / zoom) * (32f * matDo / zoom);
            for (DiemMarker d : cacMarker) {
                float x = vungAnh.left + d.x * vungAnh.width();
                float y = vungAnh.top + d.y * vungAnh.height();
                float kc = (xCham - x) * (xCham - x) + (yCham - y) * (yCham - y);
                if (kc <= kcNhoNhat) {
                    kcNhoNhat = kc;
                    idChon = d.id;
                }
            }
            if (idChon > 0) chonMarker(idChon);
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }

    private void khoiTaoTTS() {
        tts = new TextToSpeech(this, ketQua -> {
            if (ketQua != TextToSpeech.SUCCESS || tts == null) return;
            int trangThai = tts.setLanguage(new Locale("vi", "VN"));
            sanSangDoc = trangThai != TextToSpeech.LANG_MISSING_DATA
                    && trangThai != TextToSpeech.LANG_NOT_SUPPORTED;
        });
    }



    @Override public void hienThiDiemDemo(int id) {
        int index = danhSachId.indexOf(id);
        if (index < 0) return;
        dungTTS();
        viTriHienTai = index;
        hienThiDiem();
    }

    @Override public boolean phatDiemTuDong(int id) {
        if (!sanSangDoc || tts == null || !danhSachId.contains(id)
                || !getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) return false;
        hienThiDiemDemo(id);
        return docDiemHienTai();
    }

    private boolean docDiemHienTai() {
        luotDoc = "hue_" + danhSachId.get(viTriHienTai) + "_" + System.nanoTime();
        chuanBiLichSu(luotDoc, 2, danhSachId.get(viTriHienTai), huongDan == null ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu());
        boolean ok = tts.speak(danhSachTen.get(viTriHienTai) + ". " + danhSachNoiDung.get(viTriHienTai),
                TextToSpeech.QUEUE_FLUSH, null, luotDoc) == TextToSpeech.SUCCESS;
        if (!ok) { luotDoc = null; huyLuotLichSu(); }
        return ok;
    }

    private void dungTTS() {
        huyLuotLichSu();
        dongKhungThuyetMinh();
        luotDoc = null;
        if (tts != null) tts.stop();
    }

    @Override public void dungDocTour() { dungTTS(); }

    private int dp(int giaTri) {
        return Math.round(giaTri * getResources().getDisplayMetrics().density);
    }

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
