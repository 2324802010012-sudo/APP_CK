package com.example.app_ck;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import android.view.Gravity;
import android.view.GestureDetector;
import androidx.appcompat.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;


public class VanMieuThamQuanActivity extends BaseActivity implements HuongDanTour.ManHinh {

    private static final String[] TEN = {
            "Phương Đình – Hồ Văn",
            "Bia Hạ Mã",
            "Nghi Môn",
            "Cổng Văn Miếu",
            "Cổng Đại Trung",
            "Khuê Văn Các",
            "Nhà bia Tiến sĩ",
            "Giếng Thiên Quang",
            "Cổng Đại Thành",
            "Điện Đại Thành",
            "Cổng Thái Học",
            "Nhà Thái Học",
            "Lầu Trống",
            "Lầu Chuông",
            "Cổng ra",
            "Nhà Bát Giác – Vườn Giám"
    };

    private static final String[] MO_TA = {
            "Phương Đình là công trình nằm trong khu vực Hồ Văn, thuộc quần thể Văn Miếu – Quốc Tử Giám.",
            "Bia Hạ Mã nhắc người đến di tích thể hiện sự tôn kính đối với không gian Văn Miếu.",
            "Nghi Môn là một trong những công trình trên trục không gian tham quan của di tích.",
            "Cổng Văn Miếu là cổng dẫn vào khu Nội tự, mở đầu không gian kiến trúc chính.",
            "Cổng Đại Trung thuộc khu Nhập đạo, là công trình trên trục chính dẫn vào các lớp sân trong.",
            "Khuê Văn Các là công trình kiến trúc nổi bật, gắn với biểu tượng văn hóa và truyền thống hiếu học Hà Nội.",
            "Nhà bia Tiến sĩ là nơi trưng bày các bia ghi danh những người đỗ đại khoa trong các kỳ thi thời quân chủ.",
            "Giếng Thiên Quang nằm trong khu Vườn bia Tiến sĩ, tạo khoảng không gian trung tâm của khu vực này.",
            "Cổng Đại Thành dẫn vào khu Đại Thành, nơi có các công trình thờ tự quan trọng.",
            "Điện Đại Thành là công trình trung tâm của khu Đại Thành, gắn với truyền thống tôn sư trọng đạo.",
            "Cổng Thái Học là lối chuyển tiếp sang khu Thái Học của quần thể di tích.",
            "Nhà Thái Học nằm trong khu Thái Học, giới thiệu các giá trị truyền thống giáo dục Việt Nam.",
            "Lầu Trống là công trình thuộc khu Thái Học, cùng các kiến trúc khác tạo nên tổng thể hài hòa.",
            "Lầu Chuông thuộc khu Thái Học, là một trong những điểm được đánh dấu trên sơ đồ di tích.",
            "Cổng ra là vị trí kết thúc tuyến điểm đánh số trong sơ đồ tham quan chính thức.",
            "Nhà Bát Giác nằm trong Vườn Giám, thuộc không gian mở rộng của quần thể di tích."
    };

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
        AnhSoDoThamQuan.ganAnhDaiDien(this, "anh_van_mieu");

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
        viTri = Math.max(0, Math.min(idDiem - 1, tenDiem.size() - 1));

        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai == TextToSpeech.SUCCESS && tts != null) {
                int ketQua = tts.setLanguage(new Locale("vi", "VN"));
                sanSangDoc = ketQua != TextToSpeech.LANG_MISSING_DATA
                        && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        cauHinhAuto = CauHinhAutoDiSan.vanMieu(tenDiem);
        hienThi();
        huongDan = new HuongDanTour(this, findViewById(R.id.tqKhungHuongDan),
                cauHinhAuto);
        caiDatTheoDoiDoc();
        apDungKhoangTrongHeThong();
    }

    private boolean taiDiemTuSQLite() {
        try (TaiKhoanDb troGiup = new TaiKhoanDb(this)) {
            SQLiteDatabase db = troGiup.getWritableDatabase();

            // Bảng riêng: không trùng khóa ID 1..25 của Dinh Độc Lập.
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS van_mieu_diem (" +
                            "id INTEGER PRIMARY KEY," +
                            "ten TEXT NOT NULL," +
                            "mo_ta TEXT NOT NULL)"
            );

            for (int i = 0; i < TEN.length; i++) {
                ContentValues giaTri = new ContentValues();
                giaTri.put("id", i + 1);
                giaTri.put("ten", TEN[i]);
                giaTri.put("mo_ta", MO_TA[i]);
                db.insertWithOnConflict(
                        "van_mieu_diem",
                        null,
                        giaTri,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }

            KhoNoiDungThamQuan.dongBo(db, 4);
            tenDiem.clear();
            moTaDiem.clear();

            try (Cursor cursor = db.rawQuery(
                    "SELECT ten, mo_ta FROM van_mieu_diem ORDER BY id ASC",
                    null
            )) {
                while (cursor.moveToNext()) {
                    tenDiem.add(cursor.getString(0));
                    moTaDiem.add(cursor.getString(1));
                }
            }

            return tenDiem.size() == TEN.length;

        } catch (Exception e) {
            android.util.Log.e("VAN_MIEU_SQLITE", "Lỗi dữ liệu", e);
            return false;
        }
    }

    private void hienThi() {
        tenKhuVuc.setText(tenDiem.get(viTri));
        moTaKhuVuc.setText(moTaDiem.get(viTri));
        ganNguonThongTin(4, viTri + 1);
        demKhuVuc.setText("Điểm " + (viTri + 1) + "/" + tenDiem.size());
        tienTrinh.setProgress((viTri + 1) * 100 / tenDiem.size());

        nutTruoc.setEnabled(viTri > 0);
        nutTruoc.setAlpha(viTri > 0 ? 1f : 0.35f);
        nutTiep.setEnabled(viTri < tenDiem.size() - 1);
        nutTiep.setAlpha(viTri < tenDiem.size() - 1 ? 1f : 0.35f);

        danhSachKhuVuc.removeAllViews();
        AnhSoDoThamQuan.themSoDo(
                this,
                danhSachKhuVuc,
                "so_do_van_mieu_chinh_thuc",
                "Sơ đồ Văn Miếu – Quốc Tử Giám",
                "van_mieu_toa_do",
                viTri + 1,
                idDiem -> {
                    chuyenDiem(idDiem - 1);
                }
        );

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
                    + "\n" + cauHinhAuto.nhanCheDo(i + 1));
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
        if (id < 1 || id > tenDiem.size()) return;
        dungThuyetMinh();
        viTri = id - 1;
        hienThi();
    }

    @Override public boolean phatDiemTuDong(int id) {
        if (!sanSangDoc || tts == null || id < 1 || id > tenDiem.size()
                || !getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) return false;
        hienThiDiemDemo(id);
        return docDiemHienTai();
    }

    private boolean docDiemHienTai() {
        luotDoc = "van_mieu_" + (viTri + 1) + "_" + System.nanoTime();
        chuanBiLichSu(luotDoc, 4, viTri + 1, huongDan == null ? KhoLichSu.THU_CONG : huongDan.cheDoLichSu());
        boolean ok = tts.speak(tenDiem.get(viTri) + ". " + moTaDiem.get(viTri),
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

    /** Xử lý ảnh và Marker trong cùng màn hình Văn Miếu. */
    private static final class AnhSoDoThamQuan {
        private AnhSoDoThamQuan() {
        }

        public interface ChonDiem {
            void chon(int idDiem);
        }

        private static int dp(Activity activity, float giaTri) {
            return Math.round(
                    giaTri * activity.getResources().getDisplayMetrics().density
            );
        }

        private static int timAnh(Activity activity, String tenAnh) {
            return activity.getResources().getIdentifier(
                    tenAnh,
                    "drawable",
                    activity.getPackageName()
            );
        }

        public static void ganAnhDaiDien(Activity activity, String tenAnh) {
            ImageView anh = activity.findViewById(R.id.tqAnhDiaDiem);
            if (anh == null) {
                return;
            }
            int idAnh = timAnh(activity, tenAnh);
            if (idAnh != 0) {
                anh.setImageResource(idAnh);
                anh.setScaleType(ImageView.ScaleType.CENTER_CROP);
                anh.setVisibility(View.VISIBLE);
            } else {
                anh.setImageDrawable(null);
                anh.setBackgroundColor(Color.parseColor("#EAF0E8"));
            }
        }

        /**
         * bangToaDo có các cột id, x_ty_le, y_ty_le.
         * Tọa độ trong khoảng [0,1] tương ứng với ảnh đang hiển thị.
         * Không tự suy đoán hoặc tự sinh tọa độ Marker.
         */
        public static void themSoDo(
                Activity activity,
                LinearLayout khung,
                String tenAnh,
                String tieuDe,
                String bangToaDo,
                int idDangChon,
                ChonDiem xuLyChon
        ) {
            if (khung == null) {
                return;
            }

            TextView nhan = new TextView(activity);
            nhan.setText("⌖  " + tieuDe);
            nhan.setTextSize(18);
            nhan.setTextColor(Color.parseColor("#24382D"));
            nhan.setTypeface(null, Typeface.BOLD);
            nhan.setPadding(0, dp(activity, 16), 0, dp(activity, 10));
            khung.addView(nhan);

            int idAnh = timAnh(activity, tenAnh);
            if (idAnh == 0) {
                TextView thieu = new TextView(activity);
                thieu.setText(
                        "Chưa có ảnh sơ đồ được xác minh. " +
                                "Danh sách và thuyết minh bên dưới vẫn hoạt động."
                );
                thieu.setTextColor(Color.parseColor("#647568"));
                thieu.setTextSize(13);
                thieu.setPadding(
                        dp(activity, 14),
                        dp(activity, 16),
                        dp(activity, 14),
                        dp(activity, 16)
                );
                thieu.setBackgroundColor(Color.parseColor("#EEF4EA"));
                khung.addView(thieu);
                return;
            }

            ArrayList<DiemMarker> cacDiem = new ArrayList<>();
            try (TaiKhoanDb helper = new TaiKhoanDb(activity)) {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL(
                        "CREATE TABLE IF NOT EXISTS " + bangToaDo + " (" +
                                "id INTEGER PRIMARY KEY, " +
                                "x_ty_le REAL NOT NULL, " +
                                "y_ty_le REAL NOT NULL)"
                );
                // Tâm các số trên ảnh hiện tại 1672×941; không phải GPS.
                // Chỉ chuyển tọa độ mặc định cũ, giữ điểm đã chỉnh thủ công trong debug.
                if ("van_mieu_toa_do".equals(bangToaDo)) {
                    float[][] viTriMau = {
                            {278f/1672f,414f/941f}, {514f/1672f,534f/941f},
                            {523f/1672f,427f/941f}, {619f/1672f,440f/941f},
                            {805f/1672f,457f/941f}, {955f/1672f,463f/941f},
                            {1067f/1672f,544f/941f}, {1067f/1672f,464f/941f},
                            {1120f/1672f,466f/941f}, {1246f/1672f,460f/941f},
                            {1321f/1672f,481f/941f}, {1466f/1672f,477f/941f},
                            {1483f/1672f,567f/941f}, {1486f/1672f,407f/941f},
                            {1335f/1672f,618f/941f}, {688f/1672f,218f/941f}
                    };
                    float[][] viTriCu = {
                            {238f/1129f,282f/643f}, {384f/1129f,358f/643f},
                            {389f/1129f,292f/643f}, {447f/1129f,302f/643f},
                            {564f/1129f,309f/643f}, {658f/1129f,315f/643f},
                            {728f/1129f,368f/643f}, {728f/1129f,317f/643f},
                            {762f/1129f,315f/643f}, {840f/1129f,309f/643f},
                            {886f/1129f,327f/643f}, {976f/1129f,323f/643f},
                            {984f/1129f,379f/643f}, {989f/1129f,278f/643f},
                            {893f/1129f,406f/643f}, {490f/1129f,163f/643f}
                    };
                    db.beginTransaction();
                    try {
                        for (int i = 0; i < viTriMau.length; i++) {
                            ContentValues cv = new ContentValues();
                            cv.put("id", i + 1);
                            cv.put("x_ty_le", viTriMau[i][0]);
                            cv.put("y_ty_le", viTriMau[i][1]);
                            db.insertWithOnConflict(
                                    bangToaDo,
                                    null,
                                    cv,
                                    SQLiteDatabase.CONFLICT_IGNORE
                            );
                            db.update(bangToaDo, cv,
                                    "id=? AND ABS(x_ty_le-?)<0.00001 AND ABS(y_ty_le-?)<0.00001",
                                    new String[]{String.valueOf(i+1), String.valueOf(viTriCu[i][0]),
                                            String.valueOf(viTriCu[i][1])});
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }

                try (Cursor cursor = db.rawQuery(
                        "SELECT id,x_ty_le,y_ty_le FROM " + bangToaDo +
                                " ORDER BY id ASC",
                        null
                )) {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(0);
                        float x = cursor.getFloat(1);
                        float y = cursor.getFloat(2);
                        if (id > 0 && x >= 0 && x <= 1 && y >= 0 && y <= 1) {
                            cacDiem.add(new DiemMarker(id, x, y));
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e(
                        "SO_DO_THAM_QUAN",
                        "Lỗi đọc vị trí Marker",
                        e
                );
            }

            SoDoTrongTrang soDo = new SoDoTrongTrang(
                    activity,
                    idAnh,
                    cacDiem,
                    idDangChon,
                    xuLyChon
            );
            SoDoThuPhongLayout.themVao(khung, soDo, dp(activity, 385));

            TextView ghiChu = new TextView(activity);
            ghiChu.setText(
                    cacDiem.isEmpty()
                            ? "Chưa có tọa độ Marker được xác minh trên ảnh sơ đồ này."
                            : "Chạm vào một Marker đánh số trên sơ đồ để chọn điểm. " +
                            "Nếu muốn căn lại tọa độ, hãy nhấn giữ khi chạy bản Debug."
            );
            if ((activity.getApplicationInfo().flags
                    & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                ghiChu.append("\nChế độ căn điểm: chọn một điểm trong "
                        + "danh sách, nhấn giữ lên đúng vị trí trên sơ đồ "
                        + "và xác nhận lưu vào SQLite.");
            }
            ghiChu.setTextSize(12);
            ghiChu.setTextColor(Color.parseColor("#647568"));
            ghiChu.setPadding(0, dp(activity, 9), 0, dp(activity, 16));
            khung.addView(ghiChu);
        }

        private static final class DiemMarker {
            final int id;
            final float x;
            final float y;

            DiemMarker(int id, float x, float y) {
                this.id = id;
                this.x = x;
                this.y = y;
            }
        }

        private static final class SoDoTrongTrang extends View {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF vungAnh = new RectF();
            private final ArrayList<DiemMarker> danhSach;
            private final int idHienTai;
            private final ChonDiem xuLyChon;
            private final Bitmap anh;
            private final float matDo;
            private final GestureDetector boNhanCuChi;

            SoDoTrongTrang(
                    Context context,
                    int anhId,
                    ArrayList<DiemMarker> danhSach,
                    int idHienTai,
                    ChonDiem xuLyChon
            ) {
                super(context);
                BitmapFactory.Options tuyChonAnh = new BitmapFactory.Options();
                tuyChonAnh.inScaled = false;
                this.anh = BitmapFactory.decodeResource(context.getResources(), anhId, tuyChonAnh);
                this.danhSach = danhSach;
                this.idHienTai = idHienTai;
                this.xuLyChon = xuLyChon;
                this.matDo = context.getResources().getDisplayMetrics().density;
                setClickable(true);
                setContentDescription("Sơ đồ tham quan có Marker tương tác");

                boNhanCuChi = new GestureDetector(
                        context,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }

                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                chonMarkerTai(e.getX(), e.getY());
                                performClick();
                                return true;
                            }

                            @Override
                            public void onLongPress(MotionEvent e) {
                                if ((getContext().getApplicationInfo().flags
                                        & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                                    xacNhanLuuMarker(e.getX(), e.getY());
                                }
                            }
                        }
                );
            }

            private float dp(float giaTri) {
                return giaTri * matDo;
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawColor(Color.parseColor("#F1F4EE"));
                if (anh == null) {
                    return;
                }
                float tyLe = Math.min(
                        (float) getWidth() / anh.getWidth(),
                        (float) getHeight() / anh.getHeight()
                );
                float rong = anh.getWidth() * tyLe;
                float cao = anh.getHeight() * tyLe;
                vungAnh.set(
                        (getWidth() - rong) / 2f,
                        (getHeight() - cao) / 2f,
                        (getWidth() + rong) / 2f,
                        (getHeight() + cao) / 2f
                );
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                canvas.drawBitmap(anh, null, vungAnh, paint);

                for (DiemMarker diem : danhSach) {
                    float x = vungAnh.left + diem.x * vungAnh.width();
                    float y = vungAnh.top + diem.y * vungAnh.height();
                    boolean chon = diem.id == idHienTai;
                    float tiLeMarker = vungAnh.width() / 1672f;
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(x, y, 16f * tiLeMarker, paint);
                    paint.setColor(Color.parseColor(chon ? "#E8A600" : "#087B5A"));
                    canvas.drawCircle(x, y, 13f * tiLeMarker, paint);
                    paint.setColor(Color.WHITE);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(10f * tiLeMarker);
                    canvas.drawText(
                            String.valueOf(diem.id),
                            x,
                            y - (paint.ascent() + paint.descent()) / 2f,
                            paint
                    );
                }
            }

            // Chạm ngắn: chọn Marker đã được lưu.
            // Nhấn giữ (chỉ bản DEBUG): căn điểm đang chọn lên sơ đồ thật.
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                boolean daXuLy = boNhanCuChi.onTouchEvent(event);
                return daXuLy || event.getAction() == MotionEvent.ACTION_UP;
            }

            private void chonMarkerTai(float xCham, float yCham) {
                float ganNhat = dp(28) * dp(28);
                int idDuocChon = -1;

                for (DiemMarker diem : danhSach) {
                    float x = vungAnh.left + diem.x * vungAnh.width();
                    float y = vungAnh.top + diem.y * vungAnh.height();
                    float dx = xCham - x;
                    float dy = yCham - y;
                    float khoangCach = dx * dx + dy * dy;

                    if (khoangCach <= ganNhat) {
                        ganNhat = khoangCach;
                        idDuocChon = diem.id;
                    }
                }

                if (idDuocChon > 0 && xuLyChon != null) {
                    xuLyChon.chon(idDuocChon);
                }
            }

            private void xacNhanLuuMarker(float xCham, float yCham) {
                if (anh == null || vungAnh.isEmpty()
                        || !vungAnh.contains(xCham, yCham)) {
                    return;
                }

                final float x = (xCham - vungAnh.left) / vungAnh.width();
                final float y = (yCham - vungAnh.top) / vungAnh.height();
                if (x < 0 || x > 1 || y < 0 || y > 1) {
                    return;
                }

                new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                        .setTitle("Đặt Marker " + idHienTai)
                        .setMessage("Lưu vị trí đang nhấn cho điểm số "
                                + idHienTai + " trên ảnh sơ đồ? "
                                + "Chỉ xác nhận khi đúng địa điểm trên sơ đồ gốc.")
                        .setPositiveButton("Lưu", (dialog, which) -> {
                            try (TaiKhoanDb helper =
                                         new TaiKhoanDb(getContext())) {
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.execSQL(
                                        "CREATE TABLE IF NOT EXISTS "
                                                + "van_mieu_toa_do ("
                                                + "id INTEGER PRIMARY KEY, "
                                                + "x_ty_le REAL NOT NULL, "
                                                + "y_ty_le REAL NOT NULL)"
                                );

                                ContentValues giaTri = new ContentValues();
                                giaTri.put("id", idHienTai);
                                giaTri.put("x_ty_le", x);
                                giaTri.put("y_ty_le", y);
                                db.insertWithOnConflict(
                                        "van_mieu_toa_do",
                                        null,
                                        giaTri,
                                        SQLiteDatabase.CONFLICT_REPLACE
                                );

                                Toast.makeText(getContext(),
                                        "Đã lưu Marker " + idHienTai,
                                        Toast.LENGTH_SHORT).show();

                                if (xuLyChon != null) {
                                    xuLyChon.chon(idHienTai);
                                }
                            } catch (Exception ex) {
                                android.util.Log.e("VAN_MIEU_MARKER",
                                        "Không lưu được tọa độ", ex);
                                Toast.makeText(getContext(),
                                        "Không thể lưu Marker.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public boolean performClick() {
                super.performClick();
                return true;
            }
        }
    }
}
