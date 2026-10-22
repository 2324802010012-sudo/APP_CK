package com.example.app_ck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ConDaoThamQuanActivity extends BaseActivity {

    private static final String TEN_BANG = "con_dao_diem";

    private static final String[][] DU_LIEU_MAU = {
            {
                    "Trại Phú Hải",
                    "Trại Phú Hải là một trong những trại giam tiêu biểu thuộc Hệ thống Nhà tù Côn Đảo, gắn với lịch sử giam giữ và đấu tranh của các tù nhân."
            },
            {
                    "Trại Phú Sơn",
                    "Trại Phú Sơn là một di tích của hệ thống nhà tù, nơi giới thiệu lịch sử giam giữ và các cuộc đấu tranh tại Côn Đảo."
            },
            {
                    "Trại Phú Tường – Chuồng Cọp Pháp",
                    "Khu Chuồng Cọp Pháp thuộc Trại Phú Tường, là một địa điểm di tích phản ánh chế độ biệt giam khắc nghiệt."
            },
            {
                    "Trại Phú Bình – Chuồng Cọp Mỹ",
                    "Trại Phú Bình, còn gọi Chuồng Cọp Mỹ, ghi dấu một giai đoạn trong lịch sử nhà tù Côn Đảo."
            },
            {
                    "Trại Phú An",
                    "Trại Phú An thuộc quần thể di tích nhà tù, giúp tìm hiểu thêm về quá trình hoạt động của các trại giam."
            },
            {
                    "Nhà Chúa Đảo",
                    "Nhà Chúa Đảo gắn với bộ máy quản lý đảo và hệ thống nhà tù qua nhiều thời kỳ."
            },
            {
                    "Bảo tàng Côn Đảo",
                    "Bảo tàng Côn Đảo giới thiệu tư liệu, hình ảnh và hiện vật về lịch sử, thiên nhiên và con người Côn Đảo."
            }
    };

    private final ArrayList<Integer> danhSachId = new ArrayList<>();
    private final ArrayList<String> danhSachTen = new ArrayList<>();
    private final ArrayList<String> danhSachMoTa = new ArrayList<>();

    private int viTriHienTai = 0;
    private TextView tenKhuVuc;
    private TextView moTaKhuVuc;
    private TextView demKhuVuc;
    private TextView nutTruoc;
    private TextView nutTiep;
    private TextView nutPhat;
    private ProgressBar tienTrinh;
    private LinearLayout danhSachKhuVuc;
    private TextToSpeech tts;
    private boolean sanSangDoc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tham_quan);
        AnhSoDoThamQuan.ganAnhDaiDien(this, "anh_nha_tu_con_dao");
        anhXaGiaoDien();

        if (!taiDuLieuSQLite()) {
            Toast.makeText(this, "Không tải được dữ liệu Nhà tù Côn Đảo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int idDiem = getIntent().getIntExtra("idMarker", 1);
        for (int i = 0; i < danhSachId.size(); i++) {
            if (danhSachId.get(i) == idDiem) {
                viTriHienTai = i;
                break;
            }
        }

        khoiTaoThuyetMinh();
        caiDatSuKien();
        hienThiDiem();
    }

    private void anhXaGiaoDien() {
        ((TextView) findViewById(R.id.tqTenDiaDiem)).setText(
                "Hệ thống Nhà tù Côn Đảo"
        );

        tenKhuVuc = findViewById(R.id.tqTenKhuVuc);
        moTaKhuVuc = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);
        nutPhat = findViewById(R.id.tqNutPhat);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        danhSachKhuVuc = findViewById(R.id.tqDanhSachKhuVuc);
    }

    private boolean taiDuLieuSQLite() {
        try (TaiKhoanDb helper = new TaiKhoanDb(this)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + TEN_BANG + " (" +
                            "id INTEGER PRIMARY KEY, " +
                            "ten TEXT NOT NULL, " +
                            "mo_ta TEXT NOT NULL)"
            );

            for (int i = 0; i < DU_LIEU_MAU.length; i++) {
                ContentValues giaTri = new ContentValues();
                giaTri.put("id", i + 1);
                giaTri.put("ten", DU_LIEU_MAU[i][0]);
                giaTri.put("mo_ta", DU_LIEU_MAU[i][1]);
                db.insertWithOnConflict(
                        TEN_BANG,
                        null,
                        giaTri,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }

            try (Cursor cursor = db.rawQuery(
                    "SELECT id, ten, mo_ta FROM " + TEN_BANG + " ORDER BY id ASC",
                    null
            )) {
                danhSachId.clear();
                danhSachTen.clear();
                danhSachMoTa.clear();
                while (cursor.moveToNext()) {
                    danhSachId.add(cursor.getInt(0));
                    danhSachTen.add(cursor.getString(1));
                    danhSachMoTa.add(cursor.getString(2));
                }
            }
            return !danhSachId.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("ConDaoThamQuan", "Lỗi SQLite", e);
            return false;
        }
    }

    private void caiDatSuKien() {
        findViewById(R.id.tqNutQuayLai).setOnClickListener(v -> finish());
        nutTruoc.setOnClickListener(v -> chuyenDiem(viTriHienTai - 1));
        nutTiep.setOnClickListener(v -> chuyenDiem(viTriHienTai + 1));
        nutPhat.setOnClickListener(v -> phatThuyetMinh());
        findViewById(R.id.tqNutDung).setOnClickListener(v -> dungThuyetMinh());
    }

    private void hienThiDiem() {
        int soDiem = danhSachId.size();
        tenKhuVuc.setText(danhSachTen.get(viTriHienTai));
        moTaKhuVuc.setText(danhSachMoTa.get(viTriHienTai));
        demKhuVuc.setText("Điểm " + (viTriHienTai + 1) + "/" + soDiem);
        tienTrinh.setProgress((viTriHienTai + 1) * 100 / soDiem);
        nutTruoc.setEnabled(viTriHienTai > 0);
        nutTiep.setEnabled(viTriHienTai < soDiem - 1);
        nutTruoc.setAlpha(viTriHienTai > 0 ? 1f : 0.35f);
        nutTiep.setAlpha(viTriHienTai < soDiem - 1 ? 1f : 0.35f);
        nutPhat.setText("▶  Nghe thuyết minh");
        taoDanhSachDiem();
    }

    private void taoDanhSachDiem() {
        danhSachKhuVuc.removeAllViews();
        AnhSoDoThamQuan.themSoDo(
                this,
                danhSachKhuVuc,
                "so_do_nha_tu_con_dao",
                "Sơ đồ di tích Côn Đảo",
                "con_dao_toa_do",
                danhSachId.get(viTriHienTai),
                idDiem -> {
                    for (int i = 0; i < danhSachId.size(); i++) {
                        if (danhSachId.get(i) == idDiem) {
                            chuyenDiem(i);
                            break;
                        }
                    }
                }
        );
        TextView tieuDe = new TextView(this);
        tieuDe.setText("Danh sách điểm di tích Côn Đảo");
        tieuDe.setTextSize(18);
        tieuDe.setTypeface(null, Typeface.BOLD);
        tieuDe.setTextColor(Color.parseColor("#24382D"));
        tieuDe.setPadding(dp(4), dp(12), dp(4), dp(12));
        danhSachKhuVuc.addView(tieuDe);

        for (int i = 0; i < danhSachTen.size(); i++) {
            final int chiSo = i;
            boolean dangChon = i == viTriHienTai;
            TextView the = new TextView(this);
            the.setText(danhSachId.get(i) + ".  " + danhSachTen.get(i));
            the.setTextSize(14);
            the.setTypeface(null, dangChon ? Typeface.BOLD : Typeface.NORMAL);
            the.setTextColor(dangChon ? Color.WHITE : Color.parseColor("#24382D"));
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setPadding(dp(14), dp(16), dp(14), dp(16));

            GradientDrawable nen = new GradientDrawable();
            nen.setColor(Color.parseColor(dangChon ? "#4F7C5A" : "#EEF4EA"));
            nen.setCornerRadius(dp(12));
            the.setBackground(nen);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(8);
            danhSachKhuVuc.addView(the, lp);
            the.setOnClickListener(v -> chuyenDiem(chiSo));
        }
    }

    private void chuyenDiem(int chiSo) {
        if (chiSo < 0 || chiSo >= danhSachId.size()) {
            return;
        }
        dungThuyetMinh();
        viTriHienTai = chiSo;
        hienThiDiem();
    }

    private void khoiTaoThuyetMinh() {
        tts = new TextToSpeech(this, trangThai -> {
            if (trangThai != TextToSpeech.SUCCESS || tts == null) {
                return;
            }
            int ketQua = tts.setLanguage(new Locale("vi", "VN"));
            sanSangDoc = ketQua != TextToSpeech.LANG_MISSING_DATA
                    && ketQua != TextToSpeech.LANG_NOT_SUPPORTED;
        });
    }

    private void phatThuyetMinh() {
        if (tts == null || !sanSangDoc) {
            Toast.makeText(this, "Giọng đọc tiếng Việt chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        String noiDung = danhSachTen.get(viTriHienTai)
                + ". " + danhSachMoTa.get(viTriHienTai);
        tts.speak(
                noiDung,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "con_dao_" + danhSachId.get(viTriHienTai)
        );
    }

    private void dungThuyetMinh() {
        if (tts != null) {
            tts.stop();
        }
    }

    private int dp(int giaTri) {
        return (int) (giaTri * getResources().getDisplayMetrics().density + 0.5f);
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

    /** Xử lý ảnh và Marker trong cùng màn hình Côn Đảo. */
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

        /**
         * Sáu vị trí có thể đối chiếu bằng số trên ảnh bản đồ quy hoạch
         * 1366 x 970 do người dùng cung cấp. Tọa độ là vị trí ước lượng
         * theo tâm ký hiệu trên ảnh, không phải tọa độ GPS/định vị thực địa.
         * Bảo tàng Côn Đảo không có vị trí xác định trong bản đồ này.
         */
        private static final float[][] VI_TRI_TU_SO_DO_GOC = {
                {1f, 780f / 1366f, 594f / 970f}, // Trại Phú Hải - số 5
                {2f, 809f / 1366f, 573f / 970f}, // Trại Phú Sơn - số 6
                {3f, 924f / 1366f, 442f / 970f}, // Phú Tường - số 25
                {4f, 1114f / 1366f, 243f / 970f}, // Phú Bình - số 29
                {5f, 1050f / 1366f, 291f / 970f}, // Phú An - số 28
                {6f, 724f / 1366f, 645f / 970f}  // Nhà Chúa Đảo - số 3
        };

        private static void themMarkerCoDoiChieu(
                SQLiteDatabase db,
                Activity activity,
                int idAnh
        ) {
            BitmapFactory.Options tuyChon = new BitmapFactory.Options();
            tuyChon.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(
                    activity.getResources(),
                    idAnh,
                    tuyChon
            );

            if (tuyChon.outHeight <= 0) {
                return;
            }

            // Không gắn tọa độ của ảnh toàn cảnh vào một ảnh đã cắt khác.
            float tiLeAnh = (float) tuyChon.outWidth / tuyChon.outHeight;
            float tiLeGoc = 1366f / 970f;
            if (Math.abs(tiLeAnh - tiLeGoc) > 0.012f) {
                android.util.Log.w(
                        "CON_DAO_MARKER",
                        "Sơ đồ khác tỷ lệ ảnh gốc; không tự đặt Marker"
                );
                return;
            }

            for (float[] diem : VI_TRI_TU_SO_DO_GOC) {
                ContentValues giaTri = new ContentValues();
                giaTri.put("id", (int) diem[0]);
                giaTri.put("x_ty_le", diem[1]);
                giaTri.put("y_ty_le", diem[2]);
                db.insertWithOnConflict(
                        "con_dao_toa_do",
                        null,
                        giaTri,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }
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
                // Tự thêm những điểm đọc được trên đúng sơ đồ gốc.
                // Không ghi đè tọa độ đã lưu thủ công.
                themMarkerCoDoiChieu(db, activity, idAnh);

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
            khung.addView(
                    soDo,
                    new LinearLayout.LayoutParams(-1, dp(activity, 440))
            );

            TextView ghiChu = new TextView(activity);
            ghiChu.setText(
                    cacDiem.isEmpty()
                            ? "Chưa có tọa độ Marker được xác minh trên ảnh sơ đồ này."
                            : "Chạm vào Marker được đánh số để xem nội dung và nghe thuyết minh."
            );
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
                this.anh = BitmapFactory.decodeResource(
                        context.getResources(),
                        anhId
                );
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
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(x, y, dp(16), paint);
                    paint.setColor(Color.parseColor(chon ? "#E8A600" : "#087B5A"));
                    canvas.drawCircle(x, y, dp(13), paint);
                    paint.setColor(Color.WHITE);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(dp(10));
                    canvas.drawText(
                            String.valueOf(diem.id),
                            x,
                            y - (paint.ascent() + paint.descent()) / 2f,
                            paint
                    );
                }
            }

            // Chạm Marker đã lưu để xem thông tin; không hỗ trợ sửa vị trí.
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Cho phép nhận thao tác chạm Marker trong ScrollView.
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                boolean daXuLy = boNhanCuChi.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }

                return daXuLy
                        || event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP;
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

            @Override
            public boolean performClick() {
                super.performClick();
                return true;
            }
        }
    }

}