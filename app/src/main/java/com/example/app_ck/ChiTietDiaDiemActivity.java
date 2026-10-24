package com.example.app_ck;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;
import java.util.ArrayList;
import android.util.Log;

public class ChiTietDiaDiemActivity extends BaseActivity {
    public static final String PHAT_TU_VI_TRI = "com.example.app_ck.PHAT_TU_VI_TRI";
    private boolean choPhatTuViTri, dangHienThi;
    private String luotDoc;


    private TextToSpeech tts;
    private boolean sanSangDoc = false;
    private int viTri;

    private TextView nutYeuThich;
    private TextView nutTimYeuThich;
    private final androidx.activity.result.ActivityResultLauncher<Intent> dangNhapDeLuu =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    ketQua -> {
                        if (ketQua.getResultCode() == RESULT_OK && PhienDangNhap.daDangNhap(this)) {
                            try (TaiKhoanDb db = new TaiKhoanDb(this)) {
                                db.datYeuThich(PhienDangNhap.id(this), viTri, true);
                                capNhatYeuThich();
                            } catch (Exception e) {
                                Toast.makeText(this, "Không thể lưu địa điểm. Vui lòng thử lại.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    // Năm địa điểm: ID 2 dành cho Đại Nội Huế.
    private final String[] TEN = {
            "Dinh Độc Lập",
            "Bảo tàng Chứng tích Chiến tranh",
            "Đại Nội Huế",
            "Hệ thống Nhà tù Côn Đảo",
            "Văn Miếu – Quốc Tử Giám"
    };

    private final String[] DANH_MUC = {
            "Văn hóa – lịch sử",
            "Bảo tàng – lịch sử",
            "Di sản – kiến trúc",
            "Di tích – lịch sử",
            "Văn hóa – lịch sử"
    };

    private final String[] DIA_CHI = {
            "135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh",
            "28 Võ Văn Tần, TP. Hồ Chí Minh",
            "Đại Nội Huế, đường 23 Tháng 8, Huế",
            "Khu di tích Nhà tù Côn Đảo, Côn Đảo, TP. Hồ Chí Minh",
            "58 Quốc Tử Giám, Hà Nội"
    };

    private final String[] MO_TA = {
            "Dinh Độc Lập là di tích lịch sử nổi bật, gắn liền với các sự kiện " +
                    "quan trọng trong lịch sử Việt Nam, gồm các phòng làm việc, phòng khánh tiết và khu hầm.",
            "Bảo tàng Chứng tích Chiến tranh trưng bày tư liệu, hình ảnh và " +
                    "hiện vật về chiến tranh cùng những tác động đối với con người.",
            "Đại Nội Huế là trung tâm kiến trúc cung đình triều Nguyễn, gồm Ngọ Môn, " +
                    "Điện Thái Hòa, Tử Cấm Thành và nhiều công trình lịch sử đặc sắc.",
            "Hệ thống di tích Nhà tù Côn Đảo lưu giữ dấu tích về lịch sử đấu tranh " +
                    "và các trại giam trong nhiều thời kỳ.",
            "Văn Miếu – Quốc Tử Giám là quần thể di tích lịch sử và văn hóa " +
                    "gắn liền với truyền thống giáo dục, khoa cử Việt Nam."
    };

    // Tên khu vực giới thiệu. Giữ nguyên các thẻ của những địa điểm còn lại.
    private static final String[][] DIEM = {
            {"Cổng chính và khuôn viên", "Phòng Khánh tiết",
                    "Phòng họp Hội đồng Nội các", "Khu hầm"},
            {"Khu trưng bày ngoài trời", "Không gian trưng bày chuyên đề",
                    "Khu tư liệu và hình ảnh"},
            {"Ngọ Môn", "Điện Thái Hòa", "Nền điện Cần Chánh",
                    "Điện Kiến Trung", "Duyệt Thị Đường", "Thái Bình Lâu",
                    "Vườn Cơ Hạ", "Cung Diên Thọ", "Cung Trường Sanh", "Hậu hồ"},
            tenDiemConDao(),
            {"Văn Miếu Môn", "Khuê Văn Các", "Giếng Thiên Quang",
                    "Khu Đại Thành", "Khu Thái Học"}
    };

    private static String[] tenDiemConDao() {
        String[] ten = new String[CauHinhAutoConDao.CAC_DIEM.size()];
        for (int i = 0; i < ten.length; i++) ten[i] = CauHinhAutoConDao.CAC_DIEM.get(i).ten;
        return ten;
    }

    // Ảnh dự phòng; các điểm mới dùng ảnh thay thế trung tính
    // cho đến khi bạn thêm đúng tệp drawable được khai báo trong SQLite.
    private final int[] ANH = {
            R.drawable.anh_dinh_doc_lap,
            R.drawable.anh_bao_tang_chung_tich,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_gallery
    };

    // Tọa độ tham khảo, dữ liệu từ SQLite sẽ ghi đè khi đọc thành công.
    private final double[][] TOA_DO = {
            {10.7770, 106.6953},
            {10.7795, 106.6921},
            {16.4698, 107.5799},
            {8.68355, 106.61029},
            {21.0290, 105.8359}
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_chi_tiet_dia_diem);
        apDungKhoangTrongHeThong();

        // Nhận vị trí địa điểm từ màn hình trước
        viTri = getIntent().getIntExtra("viTriDiaDiem", 0);

        if (viTri < 0 || viTri >= TEN.length || TEN[viTri] == null) {
            thongBao("Địa điểm không còn khả dụng.");
            finish();
            return;
        }

        // Ý định chỉ dùng cho lần mở đã được xác nhận, không phát lại khi quay về/xoay màn hình.
        choPhatTuViTri = state == null && getIntent().getBooleanExtra(PHAT_TU_VI_TRI, false);
        getIntent().removeExtra(PHAT_TU_VI_TRI);
        if (choPhatTuViTri) hienTrangThaiDoc("Đã xác nhận theo vị trí · Đang chuẩn bị giọng đọc…");

        // DOC THONG TIN DIA DIEM TU SQLITE TRUOC KHI HIEN THI
        taiChiTietTuSQLite();
        // Dùng đầy đủ danh mục điểm của bộ nội dung, cùng ID với các sơ đồ.
        java.util.List<DuLieuNoiDungThamQuan.Bai> cacDiem = KhoNoiDungThamQuan.lay(viTri, 0).cacDiem;
        DIEM[viTri] = new String[cacDiem.size()];
        for (int i = 0; i < cacDiem.size(); i++) {
            DIEM[viTri][i] = cacDiem.get(i).ten;
        }
        findViewById(R.id.nguonChiTiet).setOnClickListener(v -> NguonThongTin.hien(this, viTri, 0));

        // Ưu tiên ảnh Đại Nội Huế nếu đã thêm vào res/drawable.
        if (viTri == 2) {
            int anhHue = getResources().getIdentifier(
                    "anh_dai_noi_hue", "drawable", getPackageName());
            if (anhHue != 0) { ANH[2] = anhHue; }
        }

        // Hiển thị thông tin địa điểm
        ((ImageView) findViewById(R.id.anhChiTiet))
                .setImageResource(ANH[viTri]);

        ((TextView) findViewById(R.id.tenChiTiet))
                .setText(TEN[viTri]);

        ((TextView) findViewById(R.id.danhMucChiTiet))
                .setText(DANH_MUC[viTri]);

        ((TextView) findViewById(R.id.diaChiChiTiet))
                .setText("⌖  " + DIA_CHI[viTri]);

        ((TextView) findViewById(R.id.moTaChiTiet))
                .setText(MO_TA[viTri]);

        ((TextView) findViewById(R.id.soLuongAnh))
                .setText("▣  1/1");

        ((TextView) findViewById(R.id.gioMoCua))
                .setText("◷  Thông tin tham quan ↗");

        findViewById(R.id.gioMoCua).setOnClickListener(v -> NguonThongTin.hien(this, viTri, 0));

        // Quay lại
        findViewById(R.id.nutQuayLai)
                .setOnClickListener(v -> finish());

        // Chỉ đường đến địa điểm lớn bằng Google Maps
        findViewById(R.id.nutChiDuong)
                .setOnClickListener(v -> moChiDuong());

        // Xem vị trí trên Google Maps
        // Nút này cần được thêm vào file XML
        android.view.View nutBanDo =
                findViewById(R.id.nutXemBanDo);

        if (nutBanDo != null) {
            nutBanDo.setOnClickListener(v -> moXemBanDo());
        }

        // Tại chi tiết chỉ nghe chủ động; GPS/demo được theo dõi ở trang chủ.
        findViewById(R.id.nutNgheThuyetMinh).setOnClickListener(v -> docThuyetMinh());
        findViewById(R.id.nutPhatLon).setOnClickListener(v -> docThuyetMinh());
        findViewById(R.id.nutDungThuyetMinh).setOnClickListener(v -> dungThuyetMinh());

        // Bắt đầu tham quan các địa điểm nhỏ
        findViewById(R.id.nutBatDauThamQuan)
                .setOnClickListener(v ->
                        moManHinhThamQuan(0)
                );

        // Xem tất cả điểm tham quan
        findViewById(R.id.nutXemTatCaDiem)
                .setOnClickListener(v ->
                        moManHinhThamQuan(0)
                );

        // Thư viện ảnh
        findViewById(R.id.nutThuVien)
                .setOnClickListener(v -> moThuVien());

        findViewById(R.id.anhChiTiet)
                .setOnClickListener(v -> moThuVien());

        // Xem thêm / Thu gọn mô tả
        TextView moTa = findViewById(R.id.moTaChiTiet);

        findViewById(R.id.nutXemThem)
                .setOnClickListener(v -> {
                    boolean dangRutGon =
                            moTa.getMaxLines() == 3;

                    moTa.setMaxLines(
                            dangRutGon ? Integer.MAX_VALUE : 3
                    );

                    ((TextView) v).setText(
                            dangRutGon
                                    ? "Thu gọn  ⌃"
                                    : "Xem thêm  ⌄"
                    );
                });

        // Chức năng yêu thích
        nutYeuThich =
                findViewById(R.id.nutLuuYeuThich);

        nutTimYeuThich =
                findViewById(R.id.nutTimYeuThich);

        capNhatYeuThich();

        nutYeuThich.setOnClickListener(v ->
                doiYeuThich()
        );

        nutTimYeuThich.setOnClickListener(v ->
                doiYeuThich()
        );

        // Tạo danh sách các điểm tham quan nổi bật
        taoTheDiem();

        // Khởi tạo Text-to-Speech tiếng Việt
        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS &&
                    tts != null) {

                int ketQua = tts.setLanguage(
                        new Locale("vi", "VN")
                );

                sanSangDoc =
                        ketQua != TextToSpeech.LANG_MISSING_DATA
                                &&
                                ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
                if (sanSangDoc) phatSauXacNhanViTri();
                else if (choPhatTuViTri) {
                    choPhatTuViTri = false;
                    hienTrangThaiDoc("Chưa có giọng đọc tiếng Việt. Kiểm tra TTS rồi bấm Nghe thuyết minh.");
                }
            } else if (choPhatTuViTri) {
                choPhatTuViTri = false;
                hienTrangThaiDoc("Không khởi tạo được TTS. Bấm Nghe thuyết minh để thử lại.");
            }
        });
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) {
                runOnUiThread(() -> {
                    if (dangHienThi && id != null && id.equals(luotDoc)) {
                        luotDoc = null;
                        hienTrangThaiDoc("Đã đọc xong giới thiệu " + TEN[viTri] + ".");
                    }
                });
            }
            @Override public void onError(String id) {
                runOnUiThread(() -> {
                    if (dangHienThi && id != null && id.equals(luotDoc)) {
                        luotDoc = null;
                        hienTrangThaiDoc("Không phát được giọng đọc. Kiểm tra TTS và thử lại.");
                    }
                });
            }
        });
    }

    // DONG BO DIA DIEM HIEN TAI TU BANG dia_diem
    // GIU NGUYEN DU LIEU JAVA CU KHI TRUONG SQLITE CHUA CO
    private void taiChiTietTuSQLite() {
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            ArrayList<DiaDiem> danhSach = db.danhSachDiaDiem();

            for (DiaDiem diaDiem : danhSach) {
                if (diaDiem.id != viTri) {
                    continue;
                }

                if (diaDiem.ten != null &&
                        !diaDiem.ten.trim().isEmpty()) {
                    TEN[viTri] = diaDiem.ten;
                }

                if (diaDiem.diaChi != null &&
                        !diaDiem.diaChi.trim().isEmpty()) {
                    DIA_CHI[viTri] = diaDiem.diaChi;
                }

                if (diaDiem.moTa != null &&
                        !diaDiem.moTa.trim().isEmpty()) {
                    MO_TA[viTri] = diaDiem.moTa;
                }

// DOC DANH MUC TU SQLITE
                if (diaDiem.danhMuc != null &&
                        !diaDiem.danhMuc.trim().isEmpty()) {

                    DANH_MUC[viTri] = diaDiem.danhMuc;
                }


                if (Double.isFinite(diaDiem.viDo) &&
                        Double.isFinite(diaDiem.kinhDo) &&
                        diaDiem.viDo >= -90 &&
                        diaDiem.viDo <= 90 &&
                        diaDiem.kinhDo >= -180 &&
                        diaDiem.kinhDo <= 180) {
                    TOA_DO[viTri][0] = diaDiem.viDo;
                    TOA_DO[viTri][1] = diaDiem.kinhDo;
                }

                if (diaDiem.tenAnh != null &&
                        !diaDiem.tenAnh.trim().isEmpty()) {
                    int maAnh = getResources().getIdentifier(
                            diaDiem.tenAnh,
                            "drawable",
                            getPackageName()
                    );
                    if (maAnh != 0) {
                        ANH[viTri] = maAnh;
                    } else {
                        Log.w("KIEM_TRA_SQLITE",
                                "Khong tim thay anh: " + diaDiem.tenAnh);
                    }
                }

                Log.d("KIEM_TRA_SQLITE",
                        "Chi tiet doc SQLite thanh cong, id=" + viTri);
                return;
            }

            Log.w("KIEM_TRA_SQLITE",
                    "Khong tim thay dia diem id=" + viTri);
            thongBao("Chưa tìm thấy địa điểm trong SQLite. Đang dùng dữ liệu sẵn có.");
        } catch (Exception e) {
            Log.e("KIEM_TRA_SQLITE", "Loi doc chi tiet SQLite", e);
            thongBao("Không đọc được SQLite. Đang dùng dữ liệu sẵn có.");
        }
    }

    // Hiển thị thông báo
    private void thongBao(String noiDung) {
        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Xem vị trí của địa điểm trên Google Maps
    private void moXemBanDo() {

        String toaDo =
                TOA_DO[viTri][0] + "," +
                        TOA_DO[viTri][1];

        Uri uri = Uri.parse(
                "https://www.google.com/maps/search/" +
                        "?api=1&query=" + toaDo
        );

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri
        );

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            thongBao("Không thể mở bản đồ.");
        }
    }

    // Chỉ đường từ vị trí hiện tại đến địa điểm
    private void moChiDuong() {

        String toaDo =
                TOA_DO[viTri][0] + "," +
                        TOA_DO[viTri][1];

        Uri uri = Uri.parse(
                "https://www.google.com/maps/dir/" +
                        "?api=1&destination=" + toaDo
        );

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri
        );

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            thongBao(
                    "Không tìm thấy ứng dụng mở bản đồ."
            );
        }
    }

    private void hienTrangThaiDoc(String text) {
        TextView status = findViewById(R.id.trangThaiThuyetMinh);
        status.setVisibility(android.view.View.VISIBLE);
        status.setText(text);
    }

    private void phatSauXacNhanViTri() {
        if (!choPhatTuViTri || !dangHienThi || !sanSangDoc || isFinishing() || isDestroyed()) return;
        choPhatTuViTri = false;
        docThuyetMinh();
    }

    private void docThuyetMinh() {
        choPhatTuViTri = false;
        if (!sanSangDoc || tts == null) {
            hienTrangThaiDoc("Giọng đọc tiếng Việt chưa sẵn sàng. Hãy kiểm tra cài đặt TTS.");
            return;
        }
        luotDoc = "chi_tiet_" + viTri + "_" + android.os.SystemClock.elapsedRealtimeNanos();
        int result = tts.speak(TEN[viTri] + ". " + MO_TA[viTri], TextToSpeech.QUEUE_FLUSH, null, luotDoc);
        if (result == TextToSpeech.ERROR) {
            luotDoc = null;
            hienTrangThaiDoc("Không khởi động được giọng đọc. Bấm Nghe thuyết minh để thử lại.");
        } else hienTrangThaiDoc("Đang phát giới thiệu " + TEN[viTri] + ".");
    }

    private void dungThuyetMinh() {
        choPhatTuViTri = false;
        luotDoc = null;
        if (tts != null) tts.stop();
        TextView status = findViewById(R.id.trangThaiThuyetMinh);
        if (status.getVisibility() == android.view.View.VISIBLE) status.setText("Đã dừng thuyết minh.");
    }

    // Tạo các thẻ điểm tham quan nổi bật
    private void taoTheDiem() {

        LinearLayout ds =
                findViewById(R.id.danhSachDiemNoiBat);

        ds.removeAllViews();

        for (int i = 0; i < DIEM[viTri].length; i++) {

            final int chiSo = i;

            LinearLayout the = new LinearLayout(this);
            the.setOrientation(LinearLayout.VERTICAL);

            the.setPadding(
                    dp(10),
                    dp(10),
                    dp(10),
                    dp(10)
            );

            the.setBackgroundResource(
                    R.drawable.nen_tien_ich_xanh
            );

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            dp(170),
                            dp(158)
                    );

            lp.setMargins(0, 0, dp(10), 0);
            ds.addView(the, lp);

            // Ảnh của điểm tham quan
            ImageView anh = new ImageView(this);

            anh.setImageResource(ANH[viTri]);
            anh.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            the.addView(
                    anh,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(82)
                    )
            );

            // Tên điểm tham quan
            TextView ten = new TextView(this);

            ten.setText(DIEM[viTri][i]);
            ten.setTextColor(
                    Color.parseColor("#123247")
            );

            ten.setTextSize(13);
            ten.setTypeface(null, Typeface.BOLD);
            ten.setMaxLines(2);

            the.addView(ten);

            // Nút nghe giới thiệu
            TextView nghe = new TextView(this);

            nghe.setText("▶  Nghe giới thiệu");
            nghe.setTextColor(
                    Color.parseColor("#008E6C")
            );

            nghe.setTextSize(12);
            nghe.setGravity(Gravity.CENTER_VERTICAL);

            the.addView(nghe);

            // Chọn điểm thì mở màn hình tham quan
            the.setOnClickListener(v -> {
                moManHinhThamQuan(chiSo);
            });
        }
    }

    // Chuyển dp sang pixel
    private int dp(int value) {
        return (int) (
                value *
                        getResources().getDisplayMetrics().density
                        + 0.5f
        );
    }

    // Mở màn hình tham quan
    // Mở màn hình tham quan theo ID của từng địa điểm.
    // Giữ nguyên 25 Marker Dinh Độc Lập và 13 Marker Bảo tàng.
    private void moManHinhThamQuan(int chiSo) {

        dungThuyetMinh();

        Intent intent;

        switch (viTri) {

            case 0:
                intent = new Intent(
                        this,
                        ThamQuanActivity.class
                );

                intent.putExtra(
                        "viTriDiaDiem",
                        0
                );

                intent.putExtra(
                        "viTriKhuVuc",
                        chiSo
                );
                break;

            case 1:
                intent = new Intent(
                        this,
                        BaoTangThamQuanActivity.class
                );

                // Màn hình Bảo tàng tự chọn Marker mặc định.
                intent.putExtra(
                        "viTriKhuVuc",
                        chiSo
                );
                break;


            case 2:
                intent = new Intent(this, DaiNoiHueThamQuanActivity.class);
                intent.putExtra("idMarker", chiSo + 1);
                break;

            case 3:
                intent = new Intent(
                        this,
                        ConDaoThamQuanActivity.class
                );
                break;

            case 4:
                intent = new Intent(
                        this,
                        VanMieuThamQuanActivity.class
                );
                break;

            default:
                thongBao("Địa điểm tham quan chưa được hỗ trợ.");
                return;
        }

        intent.putExtra("idMarker", chiSo + 1);
        startActivity(intent);
    }

    // Hiển thị thư viện ảnh
    private void moThuVien() {

        ImageView anh = new ImageView(this);
        anh.setImageResource(ANH[viTri]);
        anh.setAdjustViewBounds(true);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Hình ảnh – " + TEN[viTri])
                .setView(anh)
                .setMessage(
                        "Hiện có 1 ảnh địa điểm. " +
                                "Chưa bổ sung phim tư liệu."
                )
                .setPositiveButton("Đóng", null)
                .show();
    }

    // Cập nhật giao diện yêu thích
    @Override
    protected void onResume() {
        super.onResume();
        dangHienThi = true;
        phatSauXacNhanViTri();

        if (nutYeuThich != null) {
            capNhatYeuThich();
        }
    }

    private void capNhatYeuThich() {

        boolean daLuu = false;

        if (PhienDangNhap.daDangNhap(this)) {

            try (TaiKhoanDb db = new TaiKhoanDb(this)) {

                daLuu = db.daYeuThich(
                        PhienDangNhap.id(this),
                        viTri
                );
            }
        }

        nutYeuThich.setText(
                daLuu ? "♥\nĐã thích" : "♡\nYêu thích"
        );

        nutTimYeuThich.setText(
                daLuu ? "♥" : "♡"
        );
    }

    // Thêm hoặc xóa điểm yêu thích
    private void doiYeuThich() {

        if (!PhienDangNhap.daDangNhap(this)) {

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Yêu cầu đăng nhập")
                    .setMessage(
                            "Bạn cần đăng nhập để thêm hoặc " +
                                    "xóa địa điểm yêu thích."
                    )
                    .setPositiveButton(
                            "Đăng nhập",
                            (d, w) -> dangNhapDeLuu.launch(
                                    new Intent(
                                            this,
                                            DangNhapActivity.class
                                    )
                            )
                    )
                    .setNegativeButton("Để sau", null)
                    .show();

            return;
        }

        boolean daLuu;

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            long id = PhienDangNhap.id(this);

            daLuu = db.daYeuThich(
                    id,
                    viTri
            );

            db.datYeuThich(
                    id,
                    viTri,
                    !daLuu
            );
        }

        capNhatYeuThich();

        thongBao(
                daLuu
                        ? "Đã bỏ yêu thích"
                        : "Đã thêm vào yêu thích"
        );
    }

    @Override
    protected void onStop() {
        dangHienThi = false;
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
