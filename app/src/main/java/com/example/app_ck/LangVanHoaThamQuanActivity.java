package com.example.app_ck;

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

public class LangVanHoaThamQuanActivity extends BaseActivity {

    private static final String TEN_BANG = "lang_van_hoa_diem";

    private static final String[][] DU_LIEU_MAU = {
            {
                    "Tổng quan Khu các làng dân tộc",
                    "Khu các làng dân tộc thuộc Làng Văn hóa – Du lịch các dân tộc Việt Nam " +
                            "tại Đồng Mô, Sơn Tây, Hà Nội. Không gian được tổ chức thành bốn cụm " +
                            "nhằm giới thiệu kiến trúc, cảnh quan và các giá trị văn hóa truyền thống " +
                            "của cộng đồng các dân tộc Việt Nam."
            },
            {
                    "Cụm làng dân tộc I",
                    "Cụm I giới thiệu kiến trúc và không gian văn hóa của nhiều dân tộc " +
                            "vùng miền núi phía Bắc và Bắc Trung Bộ. Khách tham quan có thể tìm hiểu " +
                            "đặc điểm nhà ở, cảnh quan và đời sống văn hóa truyền thống được tái hiện."
            },
            {
                    "Cụm làng dân tộc II",
                    "Cụm II thể hiện văn hóa của các dân tộc thuộc vùng Trường Sơn " +
                            "và Tây Nguyên. Những không gian kiến trúc dân gian được giới thiệu " +
                            "nhằm giúp khách tìm hiểu nét đặc trưng của các cộng đồng cư dân."
            },
            {
                    "Cụm làng dân tộc III",
                    "Cụm III giới thiệu một số không gian văn hóa của các dân tộc " +
                            "ở Trung Bộ, Nam Tây Nguyên và Nam Bộ, trong đó có các cộng đồng " +
                            "Chăm, Khmer, Chơ Ro và Chu Ru."
            },
            {
                    "Cụm làng dân tộc IV",
                    "Cụm IV là một bộ phận trong quy hoạch Khu các làng dân tộc. " +
                            "Du khách tìm hiểu thêm sự đa dạng của không gian sinh hoạt " +
                            "và văn hóa cộng đồng các dân tộc Việt Nam."
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
        anhXaGiaoDien();

        if (!taiDuLieuSQLite()) {
            Toast.makeText(this, "Không tải được dữ liệu Làng Văn hóa.", Toast.LENGTH_LONG).show();
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
                "Làng Văn hóa – Du lịch các dân tộc Việt Nam"
        );

        // Chưa có ảnh thực tế được cấp quyền sử dụng.
        ImageView anh = findViewById(R.id.tqAnhDiaDiem);
        anh.setImageResource(android.R.drawable.ic_menu_gallery);

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
            android.util.Log.e("LangVanHoaThamQuan", "Lỗi SQLite", e);
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
        TextView tieuDe = new TextView(this);
        tieuDe.setText("Danh sách khu vực tham quan");
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
                "lang_van_hoa_" + danhSachId.get(viTriHienTai)
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
}
