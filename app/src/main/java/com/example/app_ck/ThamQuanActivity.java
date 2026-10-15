package com.example.app_ck;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

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

    private int viTriDiaDiem;
    private int viTriKhuVuc = 0;
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
        viTriKhuVuc = getIntent().getIntExtra("viTriKhuVuc", 0);
        if (viTriKhuVuc < 0 || viTriKhuVuc >= TEN_KHU_VUC[viTriDiaDiem].length) viTriKhuVuc = 0;

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

        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS && tts != null) {
                int ketQua = tts.setLanguage(new Locale("vi", "VN"));
                ttsSanSang = ketQua != TextToSpeech.LANG_MISSING_DATA &&
                        ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
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
        d.setCornerRadius(dp(14));
        d.setStroke(dp(1), vien);
        return d;
    }

    private void hienThiKhuVuc() {
        String[] ds = TEN_KHU_VUC[viTriDiaDiem];
        tenKhuVuc.setText(ds[viTriKhuVuc]);
        moTaKhuVuc.setText(MO_TA[viTriDiaDiem][viTriKhuVuc]);
        demKhuVuc.setText("Điểm " + (viTriKhuVuc + 1) + "/" + ds.length);
        int phanTram = (viTriKhuVuc + 1) * 100 / ds.length;
        tienTrinh.setProgress(phanTram);
        nutTruoc.setEnabled(viTriKhuVuc > 0);
        nutTiep.setEnabled(viTriKhuVuc < ds.length - 1);
        nutTruoc.setAlpha(viTriKhuVuc > 0 ? 1f : 0.35f);
        nutTiep.setAlpha(viTriKhuVuc < ds.length - 1 ? 1f : 0.35f);
        nutPhat.setText("▶  Nghe thuyết minh");

        danhSachKhuVuc.removeAllViews();
        for (int i = 0; i < ds.length; i++) {
            final int chiSo = i;
            TextView the = new TextView(this);
            the.setText((i + 1) + ".  " + ds[i]);
            the.setTextSize(14);
            the.setTypeface(null, i == viTriKhuVuc ? Typeface.BOLD : Typeface.NORMAL);
            the.setTextColor(Color.parseColor(
                    i == viTriKhuVuc ? "#FFFFFF" : "#24382D"
            ));

            the.setBackground(nen(
                    Color.parseColor(i == viTriKhuVuc ? "#4F7C5A" : "#EEF4EA"),
                    Color.parseColor(i == viTriKhuVuc ? "#4F7C5A" : "#E6EDE5")
            ));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, p);
            the.setOnClickListener(v -> chuyenKhuVuc(chiSo));
        }
    }

    private void chuyenKhuVuc(int moi) {
        if (moi < 0 || moi >= TEN_KHU_VUC[viTriDiaDiem].length) return;
        dungThuyetMinh();
        viTriKhuVuc = moi;
        hienThiKhuVuc();
    }

    private void phatThuyetMinh() {
        if (tts == null || !ttsSanSang) {
            Toast.makeText(this, "Thiết bị chưa hỗ trợ giọng đọc tiếng Việt hoặc đang khởi tạo.", Toast.LENGTH_LONG).show();
            return;
        }
        String noiDung = TEN_KHU_VUC[viTriDiaDiem][viTriKhuVuc] + ". " +
                MO_TA[viTriDiaDiem][viTriKhuVuc];
        tts.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, "khu_vuc_" + viTriKhuVuc);
    }

    private void dungThuyetMinh() {
        if (tts != null) tts.stop();
    }

    @Override
    protected void onStop() {
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
