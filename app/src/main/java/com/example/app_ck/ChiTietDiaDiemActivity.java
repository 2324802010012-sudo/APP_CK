package com.example.app_ck;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ChiTietDiaDiemActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private boolean sanSangDoc = false;
    private int viTri;
    private TextView nutYeuThich, nutTimYeuThich;
    private static final String[] TEN = {"Dinh Độc Lập", "Bảo tàng Chứng tích Chiến tranh", "Bưu điện Trung tâm Sài Gòn"};
    private static final String[] DANH_MUC = {"Văn hóa – lịch sử", "Bảo tàng – lịch sử", "Kiến trúc – lịch sử"};
    private static final String[] DIA_CHI = {"135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh", "28 Võ Văn Tần, TP. Hồ Chí Minh", "2 Công xã Paris, TP. Hồ Chí Minh"};
    private static final String[] MO_TA = {
            "Dinh Độc Lập là một di tích lịch sử nổi bật, gắn liền với những sự kiện quan trọng trong lịch sử Việt Nam. Công trình có nhiều không gian tham quan như Phòng Khánh tiết, Phòng họp Hội đồng Nội các, các phòng làm việc và khu hầm. Du khách có thể tìm hiểu về kiến trúc và những câu chuyện lịch sử tại đây.",
            "Bảo tàng Chứng tích Chiến tranh trưng bày các tư liệu, hình ảnh và hiện vật về chiến tranh cùng những tác động đối với con người. Các không gian trưng bày được tổ chức theo nhiều chủ đề, giúp khách tham quan tìm hiểu lịch sử.",
            "Bưu điện Trung tâm Sài Gòn là một công trình kiến trúc tiêu biểu tại trung tâm Thành phố Hồ Chí Minh. Công trình nổi bật với mặt tiền, sảnh chính và những chi tiết trang trí nội thất đặc trưng."
    };
    private static final String[][] DIEM = {
            {"Cổng chính và khuôn viên", "Phòng Khánh tiết", "Phòng họp Hội đồng Nội các", "Khu hầm"},
            {"Khu trưng bày ngoài trời", "Không gian trưng bày chuyên đề", "Khu tư liệu và hình ảnh"},
            {"Mặt tiền công trình", "Sảnh chính", "Không gian giao dịch và kiến trúc nội thất"}
    };
    private static final int[] ANH = {R.drawable.anh_dinh_doc_lap, R.drawable.anh_bao_tang_chung_tich, R.drawable.anh_buu_dien_sai_gon};
    private static final double[][] TOA_DO = {{10.7770, 106.6953}, {10.7795, 106.6921}, {10.7799, 106.6999}};
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_chi_tiet_dia_diem);
        viTri = getIntent().getIntExtra("viTriDiaDiem", 0);
        if (viTri < 0 || viTri >= TEN.length) viTri = 0;
        ((ImageView)findViewById(R.id.anhChiTiet)).setImageResource(ANH[viTri]);
        ((TextView)findViewById(R.id.tenChiTiet)).setText(TEN[viTri]);
        ((TextView)findViewById(R.id.danhMucChiTiet)).setText(DANH_MUC[viTri]);
        ((TextView)findViewById(R.id.diaChiChiTiet)).setText("⌖  " + DIA_CHI[viTri]);
        ((TextView)findViewById(R.id.moTaChiTiet)).setText(MO_TA[viTri]);
        ((TextView)findViewById(R.id.soLuongAnh)).setText("▣  1/1");
        ((TextView)findViewById(R.id.gioMoCua)).setText("◷  Xem giờ tham quan");
        findViewById(R.id.gioMoCua).setOnClickListener(v -> thongBao("Vui lòng kiểm tra giờ mở cửa trên kênh chính thức trước khi đến."));
        findViewById(R.id.nutQuayLai).setOnClickListener(v -> finish());
        findViewById(R.id.nutChiDuong).setOnClickListener(v -> moChiDuong());
        findViewById(R.id.nutNgheThuyetMinh).setOnClickListener(v -> docThuyetMinh(MO_TA[viTri]));
        findViewById(R.id.nutPhatLon).setOnClickListener(v -> docThuyetMinh(MO_TA[viTri]));
        findViewById(R.id.nutDungThuyetMinh).setOnClickListener(v -> {if (tts != null) tts.stop();});
        findViewById(R.id.nutBatDauThamQuan).setOnClickListener(v -> moManHinhThamQuan(0));
        findViewById(R.id.nutXemTatCaDiem).setOnClickListener(v -> moManHinhThamQuan(0));
        findViewById(R.id.nutThuVien).setOnClickListener(v -> moThuVien());
        findViewById(R.id.anhChiTiet).setOnClickListener(v -> moThuVien());
        TextView moTa = findViewById(R.id.moTaChiTiet);
        findViewById(R.id.nutXemThem).setOnClickListener(v -> {
            boolean dangRutGon = moTa.getMaxLines() == 3;
            moTa.setMaxLines(dangRutGon ? Integer.MAX_VALUE : 3);
            ((TextView)v).setText(dangRutGon ? "Thu gọn  ⌃" : "Xem thêm  ⌄");
        });
        nutYeuThich = findViewById(R.id.nutLuuYeuThich);
        nutTimYeuThich = findViewById(R.id.nutTimYeuThich);
        capNhatYeuThich();
        nutYeuThich.setOnClickListener(v -> doiYeuThich());
        nutTimYeuThich.setOnClickListener(v -> doiYeuThich());
        taoTheDiem();
        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS && tts != null) {
                int k = tts.setLanguage(new Locale("vi", "VN"));
                sanSangDoc = k != TextToSpeech.LANG_MISSING_DATA && k != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
    }
    private void thongBao(String s) {Toast.makeText(this, s, Toast.LENGTH_SHORT).show();}
    private void moChiDuong() {
        String toaDo = TOA_DO[viTri][0] + "," + TOA_DO[viTri][1];
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + toaDo));
        try {startActivity(intent);} catch (ActivityNotFoundException e) {thongBao("Không tìm thấy ứng dụng mở bản đồ.");}
    }
    private void docThuyetMinh(String noiDung) {
        if (!sanSangDoc || tts == null) {thongBao("Chưa có giọng đọc tiếng Việt. Hãy kiểm tra phần giọng nói trên thiết bị."); return;}
        tts.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, "thuyet_minh_" + viTri);
    }
    private void taoTheDiem() {
        LinearLayout ds = findViewById(R.id.danhSachDiemNoiBat);
        for (int i = 0; i < DIEM[viTri].length; i++) {
            final int chiSo = i;
            LinearLayout the = new LinearLayout(this);
            the.setOrientation(LinearLayout.VERTICAL);
            the.setPadding(dp(10), dp(10), dp(10), dp(10));
            the.setBackgroundResource(R.drawable.nen_tien_ich_xanh);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(170), dp(158));
            lp.setMargins(0, 0, dp(10), 0);
            ds.addView(the, lp);
            ImageView anh = new ImageView(this);
            anh.setImageResource(ANH[viTri]); // Ảnh địa điểm chung; thay bằng ảnh từng phòng khi có dữ liệu
            anh.setScaleType(ImageView.ScaleType.CENTER_CROP);
            the.addView(anh, new LinearLayout.LayoutParams(-1, dp(82)));
            TextView ten = new TextView(this);
            ten.setText(DIEM[viTri][i]);
            ten.setTextColor(Color.parseColor("#123247"));
            ten.setTextSize(13);
            ten.setTypeface(null, Typeface.BOLD);
            ten.setMaxLines(2);
            the.addView(ten);
            TextView nghe = new TextView(this);
            nghe.setText("▶  Nghe giới thiệu");
            nghe.setTextColor(Color.parseColor("#008E6C"));
            nghe.setTextSize(12);
            nghe.setGravity(Gravity.CENTER_VERTICAL);
            the.addView(nghe);
            the.setOnClickListener(v -> moManHinhThamQuan(chiSo));
        }
    }
    private int dp(int value) {return (int)(value * getResources().getDisplayMetrics().density + .5f);}
    private void moDiem(int i) {
        String ten = DIEM[viTri][i];
        new AlertDialog.Builder(this).setTitle(ten)
                .setMessage("Điểm tham quan thuộc " + TEN[viTri] + ". Nội dung chuyên sâu sẽ được bổ sung theo dữ liệu thuyết minh của từng khu vực.")
                .setPositiveButton("Nghe giới thiệu", (d, w) -> docThuyetMinh(ten + ". Đây là một điểm tham quan thuộc " + TEN[viTri] + "."))
                .setNegativeButton("Đóng", null).show();
    }
    private void moManHinhThamQuan(int chiSo) {
        if (tts != null) tts.stop();
        Intent intent = new Intent(this, ThamQuanActivity.class);
        intent.putExtra("viTriDiaDiem", viTri);
        intent.putExtra("viTriKhuVuc", chiSo);
        startActivity(intent);
    }
    private void moDanhSachDiem() {
        new AlertDialog.Builder(this).setTitle("Tham quan " + TEN[viTri])
                .setItems(DIEM[viTri], (d, i) -> moDiem(i))
                .setNegativeButton("Đóng", null).show();
    }
    private void moThuVien() {
        ImageView anh = new ImageView(this);
        anh.setImageResource(ANH[viTri]);
        anh.setAdjustViewBounds(true);
        new AlertDialog.Builder(this).setTitle("Hình ảnh – " + TEN[viTri])
                .setView(anh).setMessage("Hiện có 1 ảnh địa điểm. Chưa bổ sung phim tư liệu.")
                .setPositiveButton("Đóng", null).show();
    }
    @Override protected void onResume() {
        super.onResume();
        if (nutYeuThich != null) capNhatYeuThich();
    }
    private void capNhatYeuThich() {
        boolean daLuu = false;
        if (PhienDangNhap.daDangNhap(this)) {
            try (TaiKhoanDb db = new TaiKhoanDb(this)) {
                daLuu = db.daYeuThich(PhienDangNhap.id(this), viTri);
            }
        }
        nutYeuThich.setText(daLuu ? "♥\nĐã thích" : "♡\nYêu thích");
        nutTimYeuThich.setText(daLuu ? "♥" : "♡");
    }
    private void doiYeuThich() {
        if (!PhienDangNhap.daDangNhap(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu đăng nhập")
                    .setMessage("Bạn cần đăng nhập để thêm hoặc xóa địa điểm yêu thích.")
                    .setPositiveButton("Đăng nhập", (d,w) ->
                            startActivity(new Intent(this, DangNhapActivity.class)))
                    .setNegativeButton("Để sau", null).show();
            return;
        }
        boolean daLuu;
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            long id = PhienDangNhap.id(this);
            daLuu = db.daYeuThich(id, viTri);
            db.datYeuThich(id, viTri, !daLuu);
        }
        capNhatYeuThich();
        thongBao(daLuu ? "Đã bỏ yêu thích" : "Đã thêm vào yêu thích");
    }

    @Override protected void onDestroy() {if (tts != null) {tts.stop(); tts.shutdown();} super.onDestroy();}
}
