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

public class ThamQuanActivity extends AppCompatActivity {
    private static final String[] TEN_DIA_DIEM = {
            "Dinh Độc Lập", "Bảo tàng Chứng tích Chiến tranh", "Bưu điện Trung tâm Sài Gòn"
    };
    private static final int[] ANH_DIA_DIEM = {
            R.drawable.anh_dinh_doc_lap,
            R.drawable.anh_bao_tang_chung_tich,
            R.drawable.anh_buu_dien_sai_gon
    };
    private static final String[][] TEN_KHU_VUC = {
            {"Cổng chính và khuôn viên", "Phòng Khánh tiết", "Phòng họp Hội đồng Nội các", "Khu hầm"},
            {"Khu trưng bày ngoài trời", "Không gian trưng bày chuyên đề", "Khu tư liệu và hình ảnh"},
            {"Mặt tiền công trình", "Sảnh chính", "Không gian giao dịch và kiến trúc nội thất"}
    };
    private static final String[][] MO_TA = {
            {
                    "Khu vực cổng chính và khuôn viên là điểm bắt đầu hành trình khám phá Dinh Độc Lập. Tại đây, du khách có thể quan sát tổng thể kiến trúc công trình và không gian cảnh quan phía trước.",
                    "Phòng Khánh tiết là không gian được sử dụng cho các hoạt động đón tiếp và nghi lễ quan trọng. Du khách có thể quan sát cách tổ chức không gian, nội thất và phong cách kiến trúc của phòng.",
                    "Phòng họp Hội đồng Nội các là nơi gắn với các hoạt động hội họp và điều hành trong lịch sử công trình. Không gian này giúp du khách hình dung một phần hoạt động của bộ máy chính quyền thời kỳ trước năm 1975.",
                    "Khu hầm của Dinh Độc Lập có các không gian phục vụ công tác liên lạc và điều hành trong tình huống khẩn cấp. Đây là một trong những khu vực thu hút sự quan tâm của khách tham quan."
            },
            {
                    "Khu trưng bày ngoài trời giới thiệu một số hiện vật có kích thước lớn và cung cấp bối cảnh lịch sử cho hành trình tham quan bảo tàng.",
                    "Các không gian trưng bày chuyên đề sử dụng hình ảnh, hiện vật và tư liệu để giới thiệu những chủ đề liên quan đến chiến tranh và tác động đối với con người.",
                    "Khu tư liệu và hình ảnh giúp du khách tìm hiểu thêm các câu chuyện lịch sử qua nguồn tư liệu được trưng bày tại bảo tàng."
            },
            {
                    "Mặt tiền Bưu điện Trung tâm Sài Gòn gây ấn tượng bởi bố cục cân đối và các chi tiết kiến trúc đặc trưng. Đây là vị trí thích hợp để bắt đầu tìm hiểu về công trình.",
                    "Sảnh chính có không gian rộng và những chi tiết trang trí nổi bật. Du khách có thể quan sát cấu trúc mái, các quầy giao dịch và cách bố trí nội thất.",
                    "Không gian bên trong vẫn gắn với hoạt động bưu chính. Du khách có thể khám phá các chi tiết kiến trúc đồng thời tìm hiểu vai trò của công trình trong đời sống đô thị."
            }
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
    // Với hai địa điểm còn lại, chỉ số vẫn theo danh sách cũ.

    private int viTriDiaDiem;
    private int viTriKhuVuc;
    private int tangDangXem = 0;
    private final java.util.List<DuLieuDinhDocLap.DiemThamQuan> diemDinh =
            DuLieuDinhDocLap.layTatCaDiem();
    private TextView tenKhuVuc, moTaKhuVuc, demKhuVuc, nutTruoc, nutTiep, nutPhat;
    private LinearLayout danhSachKhuVuc;
    private ProgressBar tienTrinh;
    private TextToSpeech tts;
    private boolean ttsSanSang = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tham_quan);

        viTriDiaDiem = getIntent().getIntExtra("viTriDiaDiem", 0);
        if (viTriDiaDiem < 0 || viTriDiaDiem >= TEN_DIA_DIEM.length) viTriDiaDiem = 0;
        int chiSoTruyen = getIntent().getIntExtra("viTriKhuVuc", 0);
        if (viTriDiaDiem == 0) {
            // Chỉ số 4 mục cũ từ ChiTietDiaDiemActivity:
            // 0: Cổng -> bắt đầu số 1; 1: Khánh tiết -> số 4;
            // 2: Nội các -> số 1; 3: Khu hầm -> số 17.
            int[] chuyenDoi = {1, 4, 1, 17};
            if (chiSoTruyen < 0 || chiSoTruyen >= chuyenDoi.length) chiSoTruyen = 0;
            viTriKhuVuc = chuyenDoi[chiSoTruyen] - 1;
        } else {
            viTriKhuVuc = chiSoTruyen;
            if (viTriKhuVuc < 0 || viTriKhuVuc >= TEN_KHU_VUC[viTriDiaDiem].length)
                viTriKhuVuc = 0;
        }

        ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(TEN_DIA_DIEM[viTriDiaDiem]);
        ((ImageView) findViewById(R.id.tqAnhDiaDiem)).setImageResource(ANH_DIA_DIEM[viTriDiaDiem]);
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

        tangDangXem = viTriDiaDiem == 0 ? diemDinh.get(viTriKhuVuc).tang - 1 : 0;
        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS && tts != null) {
                int ketQua = tts.setLanguage(new Locale("vi", "VN"));
                ttsSanSang = ketQua != TextToSpeech.LANG_MISSING_DATA
                        && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
        hienThiKhuVuc();
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
        boolean dinh = viTriDiaDiem == 0;
        int soDiem = dinh ? diemDinh.size() : TEN_KHU_VUC[viTriDiaDiem].length;
        if (dinh) {
            DuLieuDinhDocLap.DiemThamQuan diem = diemDinh.get(viTriKhuVuc);
            tenKhuVuc.setText(diem.ten);
            moTaKhuVuc.setText(layMoTaDinh(diem));
            demKhuVuc.setText("Điểm số " + diem.id + "/25");
        } else {
            tenKhuVuc.setText(TEN_KHU_VUC[viTriDiaDiem][viTriKhuVuc]);
            moTaKhuVuc.setText(MO_TA[viTriDiaDiem][viTriKhuVuc]);
            demKhuVuc.setText("Điểm " + (viTriKhuVuc + 1) + "/" + soDiem);
        }
        // Đây là vị trí đang chọn, không phải % số điểm đã thực sự tham quan.
        tienTrinh.setProgress((viTriKhuVuc + 1) * 100 / soDiem);
        nutTruoc.setEnabled(viTriKhuVuc > 0);
        nutTiep.setEnabled(viTriKhuVuc < soDiem - 1);
        nutTruoc.setAlpha(viTriKhuVuc > 0 ? 1f : .35f);
        nutTiep.setAlpha(viTriKhuVuc < soDiem - 1 ? 1f : .35f);
        nutPhat.setText("▶  Nghe thuyết minh");
        danhSachKhuVuc.removeAllViews();
        if (dinh) {
            taoSoDoDinhDocLap();
            taoDanhSachDiemDinh();
        } else {
            taoDanhSachDiem(TEN_KHU_VUC[viTriDiaDiem]);
        }
    }


    private String layMoTaDinh(
            DuLieuDinhDocLap.DiemThamQuan diem
    ) {
        return NoiDungThuyetMinhDinhDocLap.layNoiDung(diem.id);
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
                tangDangXem = tang;
                hienThiKhuVuc();
            });
        }

        SoDoAnhView soDo = new SoDoAnhView();
        // Mỗi ảnh được cắt riêng; độ cao 390dp giúp đọc được nhãn.
        danhSachKhuVuc.addView(soDo,
                new LinearLayout.LayoutParams(-1, dp(390)));

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
            setScaleType(ImageView.ScaleType.FIT_XY);
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

    private void taoDanhSachDiem(String[] ds) {
        TextView tieuDe = taoNhan("Các điểm tham quan", 17, true);
        tieuDe.setPadding(dp(4), dp(8), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);

        for (int i = 0; i < ds.length; i++) {
            final int chiSo = i;
            boolean dangChon = i == viTriKhuVuc;
            TextView the = taoNhan((i + 1) + ".  " + ds[i], 14, dangChon);
            the.setPadding(dp(14), dp(16), dp(14), dp(16));
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setTextColor(Color.parseColor(dangChon ? "#FFFFFF" : "#24382D"));
            the.setBackground(nen(
                    Color.parseColor(dangChon ? "#4F7C5A" : "#EEF4EA"),
                    Color.parseColor(dangChon ? "#4F7C5A" : "#E6EDE5")));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, lp);
            the.setOnClickListener(v -> chuyenKhuVuc(chiSo));
        }
    }

    private void chuyenKhuVuc(int moi) {
        int soDiem = viTriDiaDiem == 0 ? diemDinh.size() : TEN_KHU_VUC[viTriDiaDiem].length;
        if (moi < 0 || moi >= soDiem) return;
        dungThuyetMinh();
        viTriKhuVuc = moi;
        if (viTriDiaDiem == 0) tangDangXem = diemDinh.get(moi).tang - 1;
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

        if (viTriDiaDiem == 0) {

            // Dinh Độc Lập: sử dụng dữ liệu của 25 Marker
            DuLieuDinhDocLap.DiemThamQuan d =
                    diemDinh.get(viTriKhuVuc);

            // Lấy nội dung thuyết minh theo số Marker
            noiDung =
                    NoiDungThuyetMinhDinhDocLap.layNoiDung(d.id);

        } else {

            // Giữ nguyên thuyết minh cho hai địa điểm còn lại
            noiDung =
                    TEN_KHU_VUC[viTriDiaDiem][viTriKhuVuc]
                            + ". "
                            + MO_TA[viTriDiaDiem][viTriKhuVuc];
        }

        // Phát giọng nói tiếng Việt
        tts.speak(
                noiDung,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "khu_vuc_" + viTriDiaDiem + "_" + viTriKhuVuc
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
}

