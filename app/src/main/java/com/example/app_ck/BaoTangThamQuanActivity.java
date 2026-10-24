package com.example.app_ck;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

/** Sơ đồ 13 điểm dựa theo ảnh do người dùng cung cấp. Không dùng làm định vị trong nhà. */
public class BaoTangThamQuanActivity extends BaseActivity {
    private static final String[] TEN_TANG = {"Tầng trệt / Ngoài trời", "Lầu 1", "Lầu 2"};
    private static final int[] ANH = {
            R.drawable.so_do_bao_tang_0,
            R.drawable.so_do_bao_tang_1,
            R.drawable.so_do_bao_tang_2
    };
    private static final class Diem {
        int id, tang;
        float x, y;
        String ten, noiDung;
        Diem(int id, int tang, float x, float y, String ten, String noiDung) {
            this.id=id; this.tang=tang; this.x=x; this.y=y; this.ten=ten; this.noiDung=noiDung;
        }
    }
    private final ArrayList<Diem> ds = new ArrayList<>();
    private int tang=0, idDangChon=10;
    private LinearLayout danhSach, thanhTang;
    private ScrollView cuonTrang;
    private TextView tieuDe, moTa, demKhuVuc, nutTruoc, nutTiep;
    private ProgressBar tienTrinh;
    private SoDoView soDo;
    private TaiKhoanDb dbHelper;

    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new TaiKhoanDb(this);
        try {
            SQLiteDatabase db=dbHelper.getWritableDatabase();
            khoiTaoDuLieuNeuChuaCo(db);
            KhoNoiDungThamQuan.dongBo(db, 1);
            docDuLieu(db);
        } catch(Exception ex) {
            android.util.Log.e("BAO_TANG_SQLITE", "Khong tai duoc marker", ex);
            Toast.makeText(this,"Không thể đọc dữ liệu bảo tàng.",Toast.LENGTH_LONG).show();
            finish(); return;
        }
        if(ds.size()!=13) { Toast.makeText(this,"Bảng bảo tàng cần có 13 Marker.",Toast.LENGTH_LONG).show(); finish(); return; }
        int idYeuCau = getIntent().getIntExtra("idMarker", -1);
        if(idYeuCau>=1 && idYeuCau<=13) idDangChon=idYeuCau;
        for(Diem d:ds) if(d.id==idDangChon) {tang=d.tang; break;}
        taoGiaoDien();
        hienThi();
    }

    private static void khoiTaoDuLieuNeuChuaCo(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS bao_tang_marker (id INTEGER PRIMARY KEY, tang INTEGER NOT NULL, x REAL NOT NULL, y REAL NOT NULL, ten TEXT NOT NULL, noi_dung TEXT NOT NULL)");
        db.beginTransaction();
        try {
            for(Object[] p:DU_LIEU_MAU) {
                db.execSQL("INSERT OR IGNORE INTO bao_tang_marker(id,tang,x,y,ten,noi_dung) VALUES(?,?,?,?,?,?)",p);
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
    // Dữ liệu khởi tạo lưu vào SQLite; chỉnh sửa các bản ghi trong DB sẽ không bị ghi đè.
    private static final Object[][] DU_LIEU_MAU = {
            {1, 2, 0.74231, 0.16142, "Những sự thật lịch sử", "Không gian trưng bày Những sự thật lịch sử giới thiệu tư liệu và hiện vật giúp khách tham quan tìm hiểu bối cảnh lịch sử chiến tranh."},
            {2, 2, 0.81154, 0.91339, "Hồi niệm", "Bộ sưu tập ảnh của các phóng viên chiến trường đã chết trong chiến tranh Đông Dương, tưởng nhớ những người ghi lại chiến tranh qua ống kính."},
            {3, 2, 0.30385, 0.79921, "Việt Nam – Chiến tranh và Hòa bình", "Bộ ảnh tư liệu của nhiếp ảnh gia Nhật Bản Ishikawa Bunyo về Việt Nam, chiến tranh và hòa bình."},
            {4, 2, 0.17308, 0.55118, "Chất độc da cam trong chiến tranh Việt Nam", "Bộ sưu tập ảnh tư liệu của nhiếp ảnh gia Nhật Bản Goro Nakamura về hậu quả chất độc da cam trong chiến tranh Việt Nam."},
            {5, 2, 0.87692, 0.45669, "Bồ câu trắng – Phòng giáo dục thiếu nhi", "Phòng giáo dục thiếu nhi Bồ câu trắng hướng đến giáo dục hòa bình và nâng cao nhận thức về những tác động của chiến tranh."},
            {6, 1, 0.29344, 0.84739, "Tội ác chiến tranh xâm lược", "Khu trưng bày các tư liệu, hình ảnh về tội ác chiến tranh và những mất mát mà chiến tranh gây ra."},
            {7, 1, 0.72587, 0.15261, "Hậu quả chất độc da cam ở Việt Nam", "Các tư liệu giúp tìm hiểu những hậu quả lâu dài của chất độc da cam đối với con người và môi trường tại Việt Nam."},
            {8, 1, 0.87645, 0.46185, "Hậu quả chất độc da cam qua tranh thiếu nhi", "Tranh vẽ của thiếu nhi phản ánh cách các em nhìn nhận hậu quả chất độc da cam và khát vọng hòa bình."},
            {9, 1, 0.13127, 0.45783, "Phòng họp", "Khu vực phòng họp của bảo tàng được đánh dấu số chín trên sơ đồ."},
            {10, 0, 0.47368, 0.18651, "Thế giới ủng hộ Việt Nam kháng chiến", "Khu trưng bày thể hiện sự ủng hộ của bạn bè quốc tế đối với Việt Nam trong thời kỳ 1954 đến 1975."},
            {11, 0, 0.27789, 0.26786, "Phòng triển lãm ngắn ngày", "Không gian trưng bày các triển lãm chuyên đề và triển lãm ngắn ngày của bảo tàng."},
            {12, 0, 0.06947, 0.70833, "Chế độ lao tù trong chiến tranh", "Khu vực tìm hiểu về chế độ lao tù trong chiến tranh xâm lược Việt Nam thông qua các tư liệu và hiện vật."},
            {13, 0, 0.53684, 0.76190, "Trưng bày ngoài trời", "Không gian ngoài trời trưng bày các phương tiện và hiện vật quân sự, giúp du khách quan sát hiện vật ở quy mô thực tế."},
    };
    private void docDuLieu(SQLiteDatabase db) {
        ds.clear();
        try(Cursor c=db.rawQuery("SELECT id,tang,x,y,ten,noi_dung FROM bao_tang_marker ORDER BY id",null)) {
            while(c.moveToNext()) ds.add(new Diem(c.getInt(0),c.getInt(1),c.getFloat(2),c.getFloat(3),c.getString(4),c.getString(5)));
        }
    }
    private TextView text(String s, int sp) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(Color.parseColor("#24382D"));
        v.setTextSize(sp);
        v.setIncludeFontPadding(true);
        return v;
    }

    private GradientDrawable nenBoGoc(String mau, String vien, int banKinh) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor(mau));
        d.setCornerRadius(dp(banKinh));
        d.setStroke(dp(1), Color.parseColor(vien));
        return d;
    }

    // Sử dụng đúng XML tham quan dùng chung cho mọi địa điểm.
    // Giữ nguyên sơ đồ 3 tầng, 13 Marker và dữ liệu SQLite.
    private void taoGiaoDien() {
        setContentView(R.layout.activity_tham_quan);

        TextView tenDiaDiem = findViewById(R.id.tqTenDiaDiem);
        tenDiaDiem.setText("Bảo tàng Chứng tích Chiến tranh");

        ImageView anhDau = findViewById(R.id.tqAnhDiaDiem);
        anhDau.setImageResource(R.drawable.anh_bao_tang_chung_tich);

        tieuDe = findViewById(R.id.tqTenKhuVuc);
        moTa = findViewById(R.id.tqMoTaKhuVuc);
        demKhuVuc = findViewById(R.id.tqDemKhuVuc);
        tienTrinh = findViewById(R.id.tqTienTrinh);
        nutTruoc = findViewById(R.id.tqNutTruoc);
        nutTiep = findViewById(R.id.tqNutTiep);

        LinearLayout noiDungDanhSach =
                findViewById(R.id.tqDanhSachKhuVuc);

        findViewById(R.id.tqNutQuayLai)
                .setOnClickListener(v -> finish());
        nutTruoc.setOnClickListener(v -> chuyenDiem(-1));
        nutTiep.setOnClickListener(v -> chuyenDiem(1));

        // Sơ đồ tích hợp ngay trong phần danh sách của layout chung.
        TextView tieuDeSoDo = text("Sơ đồ tương tác bảo tàng", 18);
        tieuDeSoDo.setTypeface(null, Typeface.BOLD);
        addMargin(noiDungDanhSach, tieuDeSoDo, 8);

        thanhTang = new LinearLayout(this);
        thanhTang.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tangLp =
                new LinearLayout.LayoutParams(-1, dp(52));
        tangLp.topMargin = dp(12);
        noiDungDanhSach.addView(thanhTang, tangLp);

        soDo = new SoDoView(this);
        SoDoThuPhongLayout.themVao(noiDungDanhSach, soDo, dp(385));

        TextView ghiChu = text(
                "Chạm vào số Marker trên sơ đồ để xem thông tin. "
                        + "Sơ đồ không thay thế định vị trong nhà.",
                12
        );
        ghiChu.setTextColor(Color.parseColor("#647568"));
        addMargin(noiDungDanhSach, ghiChu, 9);

        // Danh sách thay đổi theo tầng; không xóa khung sơ đồ.
        danhSach = new LinearLayout(this);
        danhSach.setOrientation(LinearLayout.VERTICAL);
        addMargin(noiDungDanhSach, danhSach, 12);
    }

    private void addMargin(LinearLayout parent, View view, int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(topDp);
        parent.addView(view, lp);
    }

    private void chuyenDiem(int buoc) {
        int idMoi = idDangChon + buoc;
        if (idMoi < 1 || idMoi > 13) return;
        chon(idMoi);
    }

    private void veThanhTang() {
        thanhTang.removeAllViews();
        String[] nhan = {"Tầng trệt", "Lầu 1", "Lầu 2"};
        for (int i = 0; i < nhan.length; i++) {
            final int chiSo = i;
            boolean chon = tang == i;
            TextView button = text(nhan[i], 13);
            button.setTypeface(null, chon ? Typeface.BOLD : Typeface.NORMAL);
            button.setTextColor(chon ? Color.WHITE : Color.parseColor("#4F6554"));
            button.setGravity(Gravity.CENTER);
            button.setBackground(nenBoGoc(chon ? "#4F7C5A" : "#FFFFFF",
                    chon ? "#4F7C5A" : "#FFFFFF", 12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            if (i != nhan.length - 1) lp.rightMargin = dp(4);
            thanhTang.addView(button, lp);
            button.setOnClickListener(v -> { dung(); tang = chiSo; hienThi(); });
        }
    }
    private Diem hienTai() {for(Diem d:ds)if(d.id==idDangChon)return d;return null;}
    private void chon(int id) {
        dung();idDangChon=id;
        Diem d=hienTai();if(d!=null)tang=d.tang;
        hienThi();
        moKhungThuyetMinh(false);
    }
    private void hienThi() {
        if (soDo == null) return;
        Diem cur = hienTai();
        if (cur != null && cur.tang != tang) {
            for (Diem d : ds) if (d.tang == tang) { idDangChon = d.id; break; }
        }
        cur = hienTai();
        tieuDe.setText(cur == null ? "" : cur.id + ". " + cur.ten);
        moTa.setText(cur == null ? "" : cur.noiDung);
        if (cur != null) ganNguonThongTin(1, cur.id);
        demKhuVuc.setText("Điểm " + idDangChon + "/13");
        tienTrinh.setProgress(idDangChon * 100 / 13);
        nutTruoc.setEnabled(idDangChon > 1);
        nutTiep.setEnabled(idDangChon < 13);
        nutTruoc.setAlpha(idDangChon > 1 ? 1f : .35f);
        nutTiep.setAlpha(idDangChon < 13 ? 1f : .35f);
        veThanhTang();
        soDo.setTang(tang);
        danhSach.removeAllViews();
        TextView label = text("Các điểm tham quan · " + TEN_TANG[tang], 16);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(dp(3), dp(3), dp(3), dp(13));
        danhSach.addView(label);
        for (Diem d : ds) if (d.tang == tang) {
            boolean chon = d.id == idDangChon;
            TextView item = text(d.id + ".  " + d.ten, 14);
            item.setTypeface(null, chon ? Typeface.BOLD : Typeface.NORMAL);
            item.setTextColor(chon ? Color.WHITE : Color.parseColor("#24382D"));
            item.setPadding(dp(14), dp(15), dp(14), dp(15));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setBackground(nenBoGoc(chon ? "#4F7C5A" : "#EEF4EA",
                    chon ? "#4F7C5A" : "#E6EDE5", 12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(8);
            danhSach.addView(item, lp);
            item.setOnClickListener(v -> chon(d.id));
        }
        soDo.invalidate();
    }

    private void dung() {dongKhungThuyetMinh();}
    @Override protected void dungAmThanhThamQuan() {

        dung();
    }

    @Override protected void onStop(){dung();super.onStop();}
    @Override protected void onDestroy(){dung();if(dbHelper!=null)dbHelper.close();super.onDestroy();}

    private final class SoDoView extends View {
        private Bitmap image;
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF area=new RectF();
        SoDoView(Context ctx){super(ctx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setClickable(true);}
        void setTang(int t){
            if (getParent() instanceof SoDoThuPhongLayout) {
                ((SoDoThuPhongLayout) getParent()).datLai();
            }
            if(image!=null)image.recycle();
            image=BitmapFactory.decodeResource(getResources(),ANH[t]);invalidate();
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);c.drawColor(Color.WHITE);
            if(image==null)return;
            float scale=Math.min((float)getWidth()/image.getWidth(),(float)getHeight()/image.getHeight());
            float w=image.getWidth()*scale,h=image.getHeight()*scale;
            area.set((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);
            c.drawBitmap(image,null,area,p);
            for (Diem diem : ds) {
                if (diem.tang != tang) {
                    continue;
                }
                float x = area.left + diem.x * area.width();
                float y = area.top + diem.y * area.height();
                boolean dangChon = diem.id == idDangChon;
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                c.drawCircle(x, y, dp(15), p);
                p.setColor(Color.parseColor(
                        dangChon ? "#E8A600" : "#087B5A"));
                c.drawCircle(x, y, dp(12), p);
                p.setColor(Color.WHITE);
                p.setTypeface(Typeface.create(
                        Typeface.DEFAULT, Typeface.BOLD));
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(dp(10));
                c.drawText(String.valueOf(diem.id), x,
                        y - (p.ascent() + p.descent()) / 2, p);
            }
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN)return true;
            if(e.getAction()==MotionEvent.ACTION_UP){
                performClick();float min=dp(32)*dp(32);int target=-1;
                for(Diem d:ds)if(d.tang==tang){
                    float x=area.left+d.x*area.width(),y=area.top+d.y*area.height();
                    float dx=e.getX()-x,dy=e.getY()-y,dist=dx*dx+dy*dy;
                    if(dist<min){min=dist;target=d.id;}
                }
                if(target!=-1)chon(target);return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
