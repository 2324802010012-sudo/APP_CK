package com.example.app_ck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * APP_CK - Tham quan Đại Nội Huế.
 * Sơ đồ tương tác được vẽ trong activity_tham_quan.xml; không có Activity bản đồ riêng.
 * Tọa độ dưới đây là tọa độ tương đối ĐƯỢC CĂN THEO ẢNH SƠ ĐỒ
 * https://img.vevin.vn/Img/Medias/10-04-2025/So-do-dai-noi.jpg
 * Không phải tọa độ GPS, không được áp dụng với ảnh sơ đồ khác.
 */
public class DaiNoiHueThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {

    private static final String BANG_DIEM = "dai_noi_hue_diem";
    private static final String BANG_TOA_DO = "dai_noi_hue_toa_do";
    private static final String TEN_ANH_SO_DO = "so_do_dai_noi_hue";

    // ID | Tên điểm | Nội dung thuyết minh biên soạn theo kiến thức di sản.
    // Các đoạn văn không phải trích nguyên văn của cơ quan quản lý di tích.
    private static final String[][] NOI_DUNG = {
            {"Ngọ Môn",
                    "Chào mừng quý khách đến với Ngọ Môn, cổng chính phía nam của Hoàng thành Huế. " +
                            "Công trình được xây dựng vào năm 1833, dưới thời vua Minh Mạng, trên vị trí cửa thành cũ. " +
                            "Phía dưới là nền đài bằng gạch đá với những lối đi được tổ chức theo thứ bậc nghi lễ. " +
                            "Phía trên là lầu Ngũ Phụng, nổi bật với hệ mái và kiến trúc cung đình tinh xảo. " +
                            "Ngọ Môn không chỉ là cửa ra vào mà còn là sân khấu của những nghi lễ quan trọng " +
                            "trong đời sống chính trị triều Nguyễn. Từ vị trí này, du khách có thể hình dung " +
                            "trục không gian uy nghi dẫn vào Điện Thái Hòa. Khi tham quan, hãy quan sát sự đối xứng " +
                            "của cổng, màu sắc mái ngói và mối liên hệ giữa thành, hào nước và sân phía trước."},
            {"Điện Thái Hòa",
                    "Điện Thái Hòa là một trong những công trình trung tâm của Hoàng thành Huế, " +
                            "gắn với quyền lực hoàng đế và các nghi lễ trang trọng của triều Nguyễn. " +
                            "Điện được xây dựng từ đầu thế kỷ mười chín và có những lần tu sửa trong lịch sử. " +
                            "Đây là nơi diễn ra các buổi đại triều, lễ đăng quang và nhiều nghi thức quan trọng. " +
                            "Không gian bên trong được tổ chức nhằm tôn vinh vị trí của nhà vua, với hệ thống " +
                            "cột, trang trí rồng và những chi tiết mỹ thuật cung đình. " +
                            "Nhìn từ Ngọ Môn, Điện Thái Hòa nằm trên trục chính, tạo thành trình tự nghi lễ " +
                            "từ ngoài vào trong. Khi đứng tại đây, quý khách hãy chú ý đến sân Đại Triều Nghi " +
                            "và cách kiến trúc thể hiện tôn ti, trật tự của triều đình."},
            {"Nền điện Cần Chánh",
                    "Trước mặt quý khách là khu vực nền điện Cần Chánh, nằm phía sau Điện Thái Hòa, " +
                            "thuộc không gian Tử Cấm Thành xưa. Dưới triều Nguyễn, điện Cần Chánh phục vụ " +
                            "các hoạt động làm việc và những cuộc thiết triều thường ngày của hoàng đế. " +
                            "Công trình chính không còn nguyên vẹn sau những biến động lịch sử thế kỷ hai mươi. " +
                            "Vì vậy, khi tham quan hôm nay, du khách chủ yếu tìm hiểu vị trí nền điện, " +
                            "quy hoạch trục cung đình và các dấu tích liên quan. " +
                            "Đây là một điểm dừng quan trọng để hiểu rằng giá trị di sản không chỉ nằm ở " +
                            "những tòa nhà còn đứng vững, mà còn ở các nền móng, tư liệu và ký ức lịch sử. " +
                            "Việc nghiên cứu, bảo tồn khu vực này giúp tái hiện cấu trúc vận hành của hoàng cung."},
            {"Điện Kiến Trung",
                    "Điện Kiến Trung tọa lạc ở phía bắc Tử Cấm Thành, nổi bật bởi sự kết hợp " +
                            "giữa kiến trúc cung đình truyền thống và những yếu tố kiến trúc phương Tây. " +
                            "Công trình gắn với giai đoạn cuối triều Nguyễn, đặc biệt là thời vua Khải Định " +
                            "và vua Bảo Đại. Sau khi bị hư hại nặng trong chiến tranh, điện đã được phục hồi " +
                            "dựa trên hoạt động nghiên cứu, khảo sát và tư liệu lịch sử. " +
                            "Khi tham quan, quý khách có thể nhận thấy sự khác biệt giữa hình thức của điện " +
                            "với những công trình gỗ truyền thống như Điện Thái Hòa. " +
                            "Đây là minh chứng cho sự biến đổi của mỹ thuật và đời sống hoàng cung " +
                            "trong bối cảnh giao lưu văn hóa đầu thế kỷ hai mươi."},
            {"Duyệt Thị Đường",
                    "Duyệt Thị Đường là nhà hát cung đình trong khu vực Tử Cấm Thành, gắn với " +
                            "đời sống văn hóa và nghệ thuật biểu diễn dưới triều Nguyễn. " +
                            "Không gian sân khấu từng phục vụ nhà vua, hoàng gia và các dịp lễ trong cung. " +
                            "Những loại hình nghệ thuật như tuồng cung đình giúp người xem hiểu thêm " +
                            "về âm nhạc, trang phục, hóa trang và nghi thức biểu diễn truyền thống. " +
                            "Không giống các điện dùng để thiết triều, công trình này thể hiện một mặt " +
                            "khác của đời sống hoàng cung: nhu cầu thưởng thức nghệ thuật và bảo trợ văn hóa. " +
                            "Khi đến đây, du khách hãy quan sát bố cục sân khấu, khu vực khán giả " +
                            "và các yếu tố trang trí đặc trưng của nhà hát cung đình."},
            {"Thái Bình Lâu",
                    "Thái Bình Lâu là không gian thư giãn, đọc sách và ngắm cảnh trong khu vực " +
                            "Tử Cấm Thành Huế. Công trình gắn với đời sống riêng của hoàng đế và " +
                            "không khí tĩnh lặng của nội cung. Kiến trúc thanh nhã, những mảng trang trí " +
                            "tỉ mỉ và cảnh quan xung quanh tạo ra cảm nhận khác biệt so với khu thiết triều. " +
                            "Tên gọi Thái Bình gợi ước vọng về sự yên ổn, thư thái. " +
                            "Khi khám phá điểm dừng này, quý khách có thể chú ý đến tỷ lệ công trình, " +
                            "cửa, mái và cách thiên nhiên hiện diện trong kiến trúc cung đình. " +
                            "Thái Bình Lâu cho thấy Hoàng thành không chỉ là trung tâm chính trị, " +
                            "mà còn là nơi diễn ra đời sống thường nhật của hoàng gia."},
            {"Vườn Cơ Hạ",
                    "Vườn Cơ Hạ là khu vườn cảnh trong không gian Hoàng thành Huế, " +
                            "nơi yếu tố thiên nhiên được kết hợp với đường dạo, hồ nước và kiến trúc. " +
                            "Các khu vườn cung đình thường mang ý nghĩa thẩm mỹ và tinh thần, " +
                            "tạo không gian ngắm cảnh, nghỉ ngơi và thưởng ngoạn thi ca. " +
                            "Khác với những trục nghi lễ rộng lớn, vườn chú trọng sự chuyển tiếp giữa " +
                            "cây xanh, mặt nước và các công trình nhỏ. " +
                            "Khi quan sát sơ đồ, quý khách sẽ thấy Vườn Cơ Hạ nằm về phía đông bắc " +
                            "của tuyến tham quan. Hãy dành thời gian nhận diện những khoảng xanh " +
                            "và hiểu thêm vai trò của cảnh quan trong quy hoạch hoàng cung."},
            {"Cung Diên Thọ",
                    "Cung Diên Thọ là quần thể kiến trúc dành cho Hoàng thái hậu trong Hoàng thành Huế. " +
                            "Đây là nơi ở và sinh hoạt của nhiều bậc mẫu hậu triều Nguyễn qua các thời kỳ. " +
                            "Cung có các thành phần như Diên Thọ chính điện, Tạ Trường Du, " +
                            "lầu Tịnh Minh, hệ thống cổng và bình phong. " +
                            "Không gian cung điện gắn với lễ nghi, đời sống gia đình và sự tôn kính " +
                            "dành cho bậc cao niên trong hoàng tộc. " +
                            "Nghệ thuật trang trí sành sứ, hoa văn và các chi tiết kiến trúc " +
                            "là những giá trị nổi bật mà du khách có thể tìm hiểu. " +
                            "Vị trí cung nằm phía tây của trục trung tâm Hoàng thành."},
            {"Cung Trường Sanh",
                    "Cung Trường Sanh nằm về phía tây bắc của Hoàng thành Huế, " +
                            "thuộc nhóm không gian gắn với đời sống của các bậc mẫu hậu và nội cung. " +
                            "Tên gọi Trường Sanh thể hiện ước vọng trường thọ. " +
                            "Trải qua nhiều giai đoạn, cảnh quan và một số công trình trong khu vực " +
                            "đã thay đổi, nhưng tổng thể vẫn gợi lên cách tổ chức không gian nghỉ ngơi " +
                            "với sân vườn và kiến trúc truyền thống. " +
                            "Khi tham quan, quý khách có thể so sánh không khí yên tĩnh ở đây " +
                            "với sự trang nghiêm của Điện Thái Hòa. " +
                            "Đó cũng là cách nhận ra sự phân chia công năng rất rõ trong quy hoạch Hoàng thành Huế."},
            {"Hậu hồ",
                    "Hậu hồ là vùng cảnh quan mặt nước ở phía bắc khu vực Hoàng thành, " +
                            "được thể hiện trên sơ đồ tham quan gần Điện Kiến Trung và Vườn Cơ Hạ. " +
                            "Trong quy hoạch cung đình, hồ nước và cây xanh không chỉ làm đẹp cảnh quan " +
                            "mà còn góp phần điều hòa không gian và tạo ra các góc nhìn thư thái. " +
                            "Điểm dừng này giúp du khách hiểu cách các kiến trúc sư thời xưa " +
                            "kết hợp những khu chức năng nghi lễ, sinh hoạt và sân vườn. " +
                            "Khi xem bản đồ, hãy chú ý mối liên hệ giữa mặt nước, các tuyến đường " +
                            "và những công trình nằm ở vùng rìa Tử Cấm Thành. " +
                            "Đây là điểm giới thiệu cảnh quan, không phải một tòa điện riêng biệt."}
    };

    // Căn trên ảnh ngang so_do_dai_noi_hue.jpg (2048×1366), không phải GPS.
    // Các điểm 1–5, 7–10 theo nhãn/công trình trên ảnh; Thái Bình Lâu
    // căn theo cụm công trình ở phía bắc Duyệt Thị Đường, cần đối chiếu thực địa.
    private static final float[][] TOA_DO_TREN_SO_DO = {
            {0.453f, 0.856f}, // 1 Ngọ Môn
            {0.451f, 0.627f}, // 2 Điện Thái Hòa
            {0.448f, 0.454f}, // 3 Nền điện Cần Chánh
            {0.445f, 0.217f}, // 4 Điện Kiến Trung
            {0.569f, 0.461f}, // 5 Duyệt Thị Đường
            {0.532f, 0.353f}, // 6 Khu Thái Bình Lâu, vị trí ước lượng trên ảnh
            {0.694f, 0.351f}, // 7 Vườn Cơ Hạ
            {0.201f, 0.364f}, // 8 Cung Diên Thọ
            {0.201f, 0.245f}, // 9 Cung Trường Sanh
            {0.676f, 0.208f}  // 10 Hậu hồ
    };

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
        cauHinhAuto = CauHinhAutoDiSan.hue(danhSachTen);
        caiDatNut();
        khoiTaoTTS();
        taoGiaoDienSoDo();
        hienThiDiem();
        huongDan = new HuongDanTour(this, findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto);
        caiDatTheoDoiDoc();
        apDungKhoangTrongHeThong();
    }

    private void anhXa() {
        ((TextView) findViewById(R.id.tqTenDiaDiem)).setText("Đại Nội Huế");
        ImageView anhDaiDien = findViewById(R.id.tqAnhDiaDiem);
        int anh = getResources().getIdentifier("anh_dai_noi_hue", "drawable", getPackageName());
        if (anh != 0) {
            anhDaiDien.setImageResource(anh);
            anhDaiDien.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            anhDaiDien.setImageDrawable(null);
            anhDaiDien.setBackgroundColor(Color.parseColor("#EEF4EA"));
        }
        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        khungDanhSach = findViewById(R.id.tqDanhSachKhuVuc);
    }

    private boolean napSQLite() {
        try (TaiKhoanDb helper = new TaiKhoanDb(this)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_DIEM + " (" +
                    "id INTEGER PRIMARY KEY, ten TEXT NOT NULL, mo_ta TEXT NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + BANG_TOA_DO + " (" +
                    "id INTEGER PRIMARY KEY, x_ty_le REAL NOT NULL, y_ty_le REAL NOT NULL)");
            for (int i = 0; i < NOI_DUNG.length; i++) {
                ContentValues thongTin = new ContentValues();
                thongTin.put("id", i + 1);
                thongTin.put("ten", NOI_DUNG[i][0]);
                thongTin.put("mo_ta", NOI_DUNG[i][1]);
                db.insertWithOnConflict(BANG_DIEM, null, thongTin, SQLiteDatabase.CONFLICT_IGNORE);

                ContentValues viTri = new ContentValues();
                viTri.put("id", i + 1);
                viTri.put("x_ty_le", TOA_DO_TREN_SO_DO[i][0]);
                viTri.put("y_ty_le", TOA_DO_TREN_SO_DO[i][1]);
                // Tọa độ marker do ứng dụng quản lý: cập nhật cả máy đã lưu tọa độ ảnh vuông cũ.
                db.insertWithOnConflict(BANG_TOA_DO, null, viTri, SQLiteDatabase.CONFLICT_REPLACE);
            }
            KhoNoiDungThamQuan.dongBo(db, 2);
            danhSachId.clear();
            danhSachTen.clear();
            danhSachMoTa.clear();
            try (Cursor c = db.rawQuery("SELECT id,ten,mo_ta FROM " + BANG_DIEM +
                    " WHERE id BETWEEN 1 AND 10 ORDER BY id", null)) {
                while (c.moveToNext()) {
                    danhSachId.add(c.getInt(0));
                    danhSachTen.add(c.getString(1));
                    danhSachMoTa.add(c.getString(2));
                }
            }
            cacMarker.clear();
            try (Cursor c = db.rawQuery("SELECT id,x_ty_le,y_ty_le FROM " + BANG_TOA_DO +
                    " WHERE id BETWEEN 1 AND 10 ORDER BY id", null)) {
                while (c.moveToNext()) {
                    float x = c.getFloat(1);
                    float y = c.getFloat(2);
                    if (x >= 0f && x <= 1f && y >= 0f && y <= 1f) {
                        cacMarker.add(new DiemMarker(c.getInt(0), x, y));
                    }
                }
            }
            return !danhSachId.isEmpty();
        } catch (Exception ex) {
            Log.e("DAI_NOI_HUE", "Lỗi dữ liệu SQLite", ex);
            return false;
        }
    }

    private void caiDatNut() {
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
        nutTruoc.setOnClickListener(v -> chonChiSo(viTriHienTai - 1));
        nutTiep.setOnClickListener(v -> chonChiSo(viTriHienTai + 1));
    }

    private void taoGiaoDienSoDo() {
        khungDanhSach.removeAllViews();
        TextView tieuDe = new TextView(this);
        tieuDe.setText("⌖  Sơ đồ tương tác Đại Nội Huế");
        tieuDe.setTextColor(Color.parseColor("#233D2C"));
        tieuDe.setTypeface(null, Typeface.BOLD);
        tieuDe.setTextSize(18);
        tieuDe.setPadding(dp(3), dp(12), dp(3), dp(12));
        khungDanhSach.addView(tieuDe);

        int anh = getResources().getIdentifier(TEN_ANH_SO_DO, "drawable", getPackageName());
        if (anh == 0) {
            TextView thieuAnh = new TextView(this);
            thieuAnh.setText("Chưa có ảnh so_do_dai_noi_hue.jpg trong res/drawable. " +
                    "Hãy sử dụng đúng sơ đồ tham quan Hoàng cung Huế đã đối chiếu; " +
                    "các tọa độ Marker không áp dụng cho ảnh khác.");
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
        nhan.setText("10 địa điểm tham quan và thuyết minh");
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
        final int id;
        final float x, y;
        DiemMarker(int id, float x, float y) {
            this.id = id;
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
            setContentDescription("Sơ đồ Hoàng thành Huế có 10 Marker tương tác");
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
                canvas.drawText(String.valueOf(diem.id), x,
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
        boolean ok = tts.speak(danhSachTen.get(viTriHienTai) + ". " + danhSachMoTa.get(viTriHienTai),
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
