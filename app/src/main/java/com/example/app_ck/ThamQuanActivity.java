package com.example.app_ck;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
public class ThamQuanActivity extends BaseActivity implements NguonThamQuanTuDong {
    @Override public int idDiaDiemTuDong() { return 0; }
    @Override public java.util.List<NguonThamQuanTuDong.Diem> cacDiemTuDong() {
        java.util.List<NguonThamQuanTuDong.Diem> ds = new java.util.ArrayList<>();
        for (DuLieuDinhDocLap.DiemThamQuan d : diemDinh)
            ds.add(new NguonThamQuanTuDong.Diem(d.id, d.ten, d.tang));
        return ds;
    }
    @Override public int idDiemDangChon() { return diemDinh.get(viTriKhuVuc).id; }
    @Override public void chonDiemTuDong(int id) {
        for (int i = 0; i < diemDinh.size(); i++) if (diemDinh.get(i).id == id) { chuyenKhuVuc(i); return; }
    }
    @Override public TextToSpeech mayDocTuDong() { return tts; }
    @Override public boolean sanSangTuDong() { return ttsSanSang; }
    // Màn hình này chỉ phụ trách 25 Marker Dinh Độc Lập.
    // Bảo tàng sử dụng màn hình riêng với 13 Marker.

    private static final String[] TEN_THE_GIOI_THIEU = {
            "Cổng chính và khuôn viên", "Phòng Khánh tiết",
            "Phòng họp Hội đồng Nội các", "Khu hầm"
    };

    // Thứ tự: Tầng 1, Tầng 2, Tầng 3, Tầng 4, Tầng hầm, Tầng trệt.
    private static final String[] TEN_TANG = {
            "Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng hầm", "Tầng trệt"
    };
    private static final int[] ANH_TANG = {
            R.drawable.so_do_tang_1, R.drawable.so_do_tang_2,
            R.drawable.so_do_tang_3, R.drawable.so_do_tang_4,
            R.drawable.so_do_tang_ham, R.drawable.so_do_tang_tret
    };

    // Số trên sơ đồ (1–25) được lưu dưới dạng chỉ số 0–24.

    private int viTriKhuVuc;
    private int tangDangXem = 0;

    // 25 MARKER DUOC NAP TU SQLITE
    private final List<DuLieuDinhDocLap.DiemThamQuan>
            diemDinh = new ArrayList<>();

    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc, nutTruoc, nutTiep, nutPhat;
    private LinearLayout danhSachKhuVuc;
    private ProgressBar tienTrinh;
    private TextToSpeech tts;
    private boolean ttsSanSang = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int idDiaDiem = getIntent().getIntExtra("viTriDiaDiem", 0);
        if (idDiaDiem == 1) {
            // Bảo tàng Chứng tích Chiến tranh: 13 Marker, 3 tầng.
            android.content.Intent intent = new android.content.Intent(
                    this, BaoTangThamQuanActivity.class);
            if (getIntent().hasExtra("idMarker")) {
                intent.putExtra("idMarker", getIntent().getIntExtra("idMarker", -1));
            }
            intent.putExtra("viTriKhuVuc",
                    getIntent().getIntExtra("viTriKhuVuc", 0));
            startActivity(intent);
            finish();
            return;
        }

        if (idDiaDiem == 2) {
            android.content.Intent intent = new android.content.Intent(
                    this, DaiNoiHueThamQuanActivity.class);
            if (getIntent().hasExtra("idMarker")) {
                intent.putExtra("idMarker", getIntent().getIntExtra("idMarker", 1));
            }
            startActivity(intent);
            finish();
            return;
        }

        if (idDiaDiem != 0) {
            // Không tự chuyển sang Dinh Độc Lập nếu Activity mới chưa có.
            Toast.makeText(this,
                    "Sơ đồ tham quan cho địa điểm này đang được bổ sung.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_tham_quan);

        // Chỉ nạp 25 Marker Dinh Độc Lập từ SQLite.
        if (!taiMarkerTuSQLite()) {
            Toast.makeText(this, "SQLite chưa có đủ 25 Marker Dinh Độc Lập.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int chiSoTruyen = getIntent().getIntExtra("viTriKhuVuc", 0);
        int idMarker = getIntent().getIntExtra("idMarker", -1);
        if (idMarker >= 1 && idMarker <= diemDinh.size()) {
            viTriKhuVuc = idMarker - 1;
        } else {
            viTriKhuVuc = timMarkerTuTheGioiThieu(chiSoTruyen);
        }

        ((TextView) findViewById(R.id.tqTenDiaDiem)).setText("Dinh Độc Lập");
        ((ImageView) findViewById(R.id.tqAnhDiaDiem))
                .setImageResource(R.drawable.anh_dinh_doc_lap);
        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        nutPhat = findViewById(R.id.tqNutPhat);
        danhSachKhuVuc = findViewById(R.id.tqDanhSachKhuVuc);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
        nutTruoc.setOnClickListener(v -> chuyenKhuVuc(viTriKhuVuc - 1));
        nutTiep.setOnClickListener(v -> chuyenKhuVuc(viTriKhuVuc + 1));
        nutPhat.setOnClickListener(v -> phatThuyetMinh());
        findViewById(R.id.tqNutDung).setOnClickListener(v -> dungThuyetMinh());

        tangDangXem = diemDinh.get(viTriKhuVuc).tang - 1;
        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS && tts != null) {
                int ketQua = tts.setLanguage(new Locale("vi", "VN"));
                ttsSanSang = ketQua != TextToSpeech.LANG_MISSING_DATA
                        && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
        hienThiKhuVuc();
    }

    // Tìm đúng Marker theo tên hiện có trong SQLite, tránh ánh xạ sai điểm Nội các.
    // Chỉ số 0 là "Cổng chính và khuôn viên" (điểm mở đầu tham quan).
    private int timMarkerTuTheGioiThieu(int chiSoThe) {
        if (chiSoThe <= 0 || chiSoThe >= TEN_THE_GIOI_THIEU.length) {
            return 0;
        }

        String tenCanTim = chuanHoaTen(TEN_THE_GIOI_THIEU[chiSoThe]);
        for (int i = 0; i < diemDinh.size(); i++) {
            String tenMarker = chuanHoaTen(diemDinh.get(i).ten);
            if (tenMarker.equals(tenCanTim)
                    || tenMarker.contains(tenCanTim)
                    || tenCanTim.contains(tenMarker)) {
                return i;
            }
        }

        // Có thể một số marker sử dụng tên rút gọn trên sơ đồ.
        String tuKhoa = chiSoThe == 1 ? "khanh tiet"
                : chiSoThe == 2 ? "noi cac" : "ham";
        for (int i = 0; i < diemDinh.size(); i++) {
            if (chuanHoaTen(diemDinh.get(i).ten).contains(tuKhoa)) {
                return i;
            }
        }

        android.util.Log.w("KIEM_TRA_SQLITE",
                "Khong thay Marker tuong ung voi: " + TEN_THE_GIOI_THIEU[chiSoThe]);
        Toast.makeText(this,
                "Chưa xác định được điểm tương ứng. Đã mở sơ đồ từ điểm đầu tiên.",
                Toast.LENGTH_SHORT).show();
        return 0;
    }

    private String chuanHoaTen(String giaTri) {
        if (giaTri == null) return "";
        String s = java.text.Normalizer.normalize(
                giaTri.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replaceAll("\\s+", " ").trim();
    }

    private int dp(int giaTri) {
        return (int) (giaTri * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable nen(int mau, int vien) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(mau);
        d.setCornerRadius(dp(12));
        d.setStroke(dp(1), vien);
        return d;
    }

    private void hienThiKhuVuc() {
        thongBaoDoiDiemThamQuan();
        int soDiem = diemDinh.size();
        DuLieuDinhDocLap.DiemThamQuan diem = diemDinh.get(viTriKhuVuc);
        tenKhuVuc.setText(diem.ten);
        moTaKhuVuc.setText(layMoTaDinh(diem));
        demKhuVuc.setText("Điểm số " + diem.id + "/25");

        tienTrinh.setProgress((viTriKhuVuc + 1) * 100 / soDiem);
        nutTruoc.setEnabled(viTriKhuVuc > 0);
        nutTiep.setEnabled(viTriKhuVuc < soDiem - 1);
        nutTruoc.setAlpha(viTriKhuVuc > 0 ? 1f : .35f);
        nutTiep.setAlpha(viTriKhuVuc < soDiem - 1 ? 1f : .35f);
        nutPhat.setText("▶  Nghe thuyết minh");
        danhSachKhuVuc.removeAllViews();
        taoSoDoDinhDocLap();
        taoDanhSachDiemDinh();
    }

    private String layMoTaDinh(
            DuLieuDinhDocLap.DiemThamQuan diem
    ) {

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            String noiDung =
                    db.layThuyetMinhTheoId(
                            diem.id
                    );

            android.util.Log.d(
                    "KIEM_TRA_SQLITE",
                    "Thuyet minh Marker "
                            + diem.id
                            + ": "
                            + (noiDung == null
                            ? "KHONG CO DU LIEU"
                            : "DOC THANH CONG")
            );

            if (noiDung != null &&
                    !noiDung.trim().isEmpty()) {

                return noiDung;
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "ThamQuanActivity",
                    "Loi doc thuyet minh SQLite",
                    e
            );
        }

        return "Chưa có nội dung thuyết minh "
                + "cho điểm tham quan này.";
    }


    private void taoDanhSachDiemDinh() {
        TextView tieuDe = taoNhan("Các điểm trên " + TEN_TANG[tangDangXem], 17, true);
        tieuDe.setPadding(dp(4), dp(8), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);
        for (DuLieuDinhDocLap.DiemThamQuan diem : diemDinh) {
            if (diem.tang != tangDangXem + 1) continue;
            int chiSo = diem.id - 1;
            boolean chon = chiSo == viTriKhuVuc;
            TextView the = taoNhan(diem.id + ".  " + diem.ten, 14, chon);
            the.setPadding(dp(14), dp(16), dp(14), dp(16));
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setTextColor(chon ? Color.WHITE : Color.parseColor("#24382D"));
            the.setBackground(nen(Color.parseColor(chon ? "#4F7C5A" : "#EEF4EA"),
                    Color.parseColor(chon ? "#4F7C5A" : "#E6EDE5")));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, lp);
            the.setOnClickListener(v -> chuyenKhuVuc(chiSo));
        }
    }

    private TextView taoNhan(String s, int size, boolean dam) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor("#24382D"));
        t.setTypeface(null, dam ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    private void taoSoDoDinhDocLap() {
        TextView tieuDe = taoNhan("⌖  Sơ đồ tham quan Dinh Độc Lập", 18, true);
        tieuDe.setPadding(dp(4), dp(12), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);

        HorizontalScrollView thanhCuon = new HorizontalScrollView(this);
        thanhCuon.setHorizontalScrollBarEnabled(false);
        LinearLayout thanhTang = new LinearLayout(this);
        thanhTang.setOrientation(LinearLayout.HORIZONTAL);
        thanhCuon.addView(thanhTang);
        danhSachKhuVuc.addView(thanhCuon);

        for (int i = 0; i < TEN_TANG.length; i++) {
            final int tang = i;
            TextView nutTang = taoNhan(TEN_TANG[i], 13, tangDangXem == i);
            nutTang.setGravity(Gravity.CENTER);
            nutTang.setPadding(dp(14), dp(12), dp(14), dp(12));
            boolean dangXem = tangDangXem == i;
            nutTang.setTextColor(dangXem ? Color.WHITE : Color.parseColor("#24382D"));
            nutTang.setBackground(nen(
                    Color.parseColor(dangXem ? "#4F7C5A" : "#EEF4EA"),
                    Color.parseColor(dangXem ? "#4F7C5A" : "#E6EDE5")));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(7);
            lp.bottomMargin = dp(10);
            thanhTang.addView(nutTang, lp);
            nutTang.setOnClickListener(v -> {
                // Chon tang moi: dong bo luon Marker dang chon voi tang do.
                // Neu khong, ten phong va so do se thuoc hai tang khac nhau.
                if (tangDangXem != tang) {
                    int diemDauTien = -1;
                    for (int j = 0; j < diemDinh.size(); j++) {
                        if (diemDinh.get(j).tang == tang + 1) {
                            diemDauTien = j;
                            break;
                        }
                    }
                    if (diemDauTien < 0) {
                        Toast.makeText(this, "Tầng này chưa có điểm tham quan.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dungThuyetMinh();
                    tangDangXem = tang;
                    viTriKhuVuc = diemDauTien;
                }
                hienThiKhuVuc();
            });
        }

        SoDoAnhView soDo = new SoDoAnhView();
        // Mỗi ảnh được cắt riêng; độ cao 390dp giúp đọc được nhãn.
        SoDoThuPhongLayout.themVao(danhSachKhuVuc, soDo, dp(390));

        TextView ghiChu = taoNhan(
                "Chạm vào số trên sơ đồ để chọn phòng. " +
                        "Cổng chính và khuôn viên không được đánh số trên sơ đồ này.", 12, false);
        ghiChu.setTextColor(Color.parseColor("#647568"));
        ghiChu.setPadding(dp(4), dp(9), dp(4), dp(18));
        danhSachKhuVuc.addView(ghiChu);
    }

    private class SoDoAnhView extends AppCompatImageView {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        SoDoAnhView() {
            super(ThamQuanActivity.this);
            setImageResource(ANH_TANG[tangDangXem]);
            setScaleType(ScaleType.FIT_XY);
            setBackgroundColor(Color.parseColor("#621017"));
            setClickable(true);
            setContentDescription("Sơ đồ " + TEN_TANG[tangDangXem] + " với các marker tương tác");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            for (DuLieuDinhDocLap.DiemThamQuan marker : diemDinh) {
                if (marker.tang != tangDangXem + 1 || !marker.coToaDo()) continue;
                int khuVuc = marker.id - 1;
                float x = marker.x * w;
                float y = marker.y * h;
                boolean chon = khuVuc == viTriKhuVuc;
                paint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, dp(15), paint);
                paint.setColor(Color.parseColor(chon ? "#E8A600" : "#087B5A"));
                canvas.drawCircle(x, y, dp(12), paint);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(dp(10));
                canvas.drawText(String.valueOf(marker.id), x,
                        y - (paint.ascent() + paint.descent()) / 2, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) return true;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float x = event.getX(), y = event.getY();
                float nganNhat = dp(26) * dp(26);
                int idGanNhat = -1;
                // Chọn marker GẦN NHẤT để các phòng nằm sát nhau vẫn thao tác được.
                for (DuLieuDinhDocLap.DiemThamQuan marker : diemDinh) {
                    if (marker.tang != tangDangXem + 1 || !marker.coToaDo()) continue;
                    float dx = x - marker.x * getWidth();
                    float dy = y - marker.y * getHeight();
                    float kc = dx * dx + dy * dy;
                    if (kc < nganNhat) {
                        nganNhat = kc;
                        idGanNhat = marker.id;
                    }
                }
                performClick();
                if (idGanNhat > 0) chuyenKhuVuc(idGanNhat - 1);
                return true;
            }
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }

    private void chuyenKhuVuc(int moi) {
        if (moi < 0 || moi >= diemDinh.size()) return;
        dungThuyetMinh();
        viTriKhuVuc = moi;
        tangDangXem = diemDinh.get(moi).tang - 1;
        hienThiKhuVuc();
    }

    private void phatThuyetMinh() {

        if (tts == null || !ttsSanSang) {
            Toast.makeText(
                    this,
                    "Thiết bị chưa hỗ trợ giọng đọc tiếng Việt hoặc đang khởi tạo.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String noiDung;
        DuLieuDinhDocLap.DiemThamQuan d = diemDinh.get(viTriKhuVuc);
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            noiDung = db.layThuyetMinhTheoId(d.id);
        } catch (Exception e) {
            android.util.Log.e("ThamQuanActivity",
                    "Lỗi đọc thuyết minh SQLite", e);
            noiDung = null;
        }
        if (noiDung == null || noiDung.trim().isEmpty()) {
            Toast.makeText(this, "Marker chưa có nội dung thuyết minh.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Phát giọng nói tiếng Việt
        tts.speak(
                noiDung,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "dinh_doc_lap_" + viTriKhuVuc
        );
    }


    // Dừng thuyết minh
    private void dungThuyetMinh() {
        if (tts != null) {
            tts.stop();
        }
    }

    // Dừng giọng đọc khi rời màn hình
    @Override
    protected void onStop() {
        dungThuyetMinh();
        super.onStop();
    }

    // Giải phóng Text-to-Speech
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        super.onDestroy();
    }

    // NAP 25 MARKER TU CO SO DU LIEU SQLITE
    private boolean taiMarkerTuSQLite() {

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            ArrayList<DuLieuDinhDocLap.DiemThamQuan>
                    danhSach = db.layDiemDinhDocLap();

            android.util.Log.d(
                    "KIEM_TRA_SQLITE",
                    "So Marker doc tu SQLite: "
                            + danhSach.size()
            );

            // KIEM TRA DU 25 MARKER
            if (danhSach.size() != 25) {
                return false;
            }

            // KIEM TRA ID, TANG VA TOA DO
            for (int i = 0; i < danhSach.size(); i++) {

                DuLieuDinhDocLap.DiemThamQuan diem =
                        danhSach.get(i);

                if (diem.id != i + 1) {
                    return false;
                }

                if (diem.tang < 1 || diem.tang > 6) {
                    return false;
                }

                if (!diem.coToaDo()) {
                    return false;
                }
            }

            diemDinh.clear();
            diemDinh.addAll(danhSach);
            android.util.Log.d(
                    "KIEM_TRA_SQLITE",
                    "Da nap Marker SQLite vao so do"
            );
            return true;

        } catch (Exception e) {

            android.util.Log.e(
                    "ThamQuanActivity",
                    "Khong doc duoc 25 Marker tu SQLite",
                    e
            );

            return false;
        }
    }

}
