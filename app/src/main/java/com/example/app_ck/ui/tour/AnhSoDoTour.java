package com.example.app_ck.ui.tour;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Hiển thị sơ đồ theo metadata SQLite; điểm không có marker vẫn nằm trong danh sách. */
public final class AnhSoDoTour {
    private AnhSoDoTour() {}

    public interface ChonDiem { void chon(int id); }

    private static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static void ganAnhDaiDien(Activity activity, String tenAnh) {
        ImageView anh = activity.findViewById(R.id.tqAnhDiaDiem);
        int id = activity.getResources().getIdentifier(tenAnh, "drawable", activity.getPackageName());
        if (id != 0) {
            anh.setImageResource(id);
            anh.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            anh.setImageDrawable(null);
            anh.setBackgroundColor(Color.parseColor("#EAF0E8"));
        }
    }

    public static void themSoDo(Activity activity, LinearLayout khung, int place, int tang,
                                int idDangChon, ChonDiem chon) {
        String tenAnh;
        List<DiemThamQuan> markers = new ArrayList<>();
        try (AppDatabase helper = new AppDatabase(activity)) {
            android.database.sqlite.SQLiteDatabase db = helper.getReadableDatabase();
            tenAnh = KhoDuLieuThamQuan.anhSoDo(db, place, tang);
            for (DiemThamQuan d : KhoDuLieuThamQuan.danhSach(db, place)) {
                if (d.tang == tang && d.coMarker()) markers.add(d);
            }
        }
        TextView nhan = new TextView(activity);
        nhan.setText("⌖  Sơ đồ tham quan");
        nhan.setTextSize(18);
        nhan.setTypeface(null, Typeface.BOLD);
        nhan.setTextColor(Color.parseColor("#24382D"));
        nhan.setPadding(0, dp(activity, 16), 0, dp(activity, 10));
        khung.addView(nhan);

        int idAnh = tenAnh.isEmpty() ? 0 : activity.getResources().getIdentifier(
                tenAnh, "drawable", activity.getPackageName());
        if (idAnh == 0) {
            TextView thieu = new TextView(activity);
            thieu.setText("Chưa có ảnh sơ đồ. Bạn vẫn chọn điểm trong danh sách để xem và nghe thuyết minh.");
            thieu.setTextColor(Color.parseColor("#647568"));
            thieu.setPadding(dp(activity, 14), dp(activity, 16), dp(activity, 14), dp(activity, 16));
            khung.addView(thieu);
            return;
        }
        SoDoTrongTrang view = new SoDoTrongTrang(activity, idAnh, markers, idDangChon, chon);
        SoDoThuPhongLayout.themVao(khung, view, dp(activity, place == 3 ? 440 : 385));
        TextView ghiChu = new TextView(activity);
        ghiChu.setText(markers.isEmpty()
                ? "Chưa có tọa độ marker. Bạn có thể chọn điểm trong danh sách."
                : "Chạm vào marker để chọn điểm. Tọa độ trên sơ đồ độc lập với GPS.");
        ghiChu.setTextSize(12);
        ghiChu.setTextColor(Color.parseColor("#647568"));
        ghiChu.setPadding(0, dp(activity, 9), 0, dp(activity, 16));
        khung.addView(ghiChu);
    }

    private static final class SoDoTrongTrang extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF vungAnh = new RectF();
        private final List<DiemThamQuan> markers;
        private final int idDangChon;
        private final ChonDiem chon;
        private final Bitmap anh;
        private final GestureDetector cuChi;

        SoDoTrongTrang(Context context, int anhId, List<DiemThamQuan> markers,
                       int idDangChon, ChonDiem chon) {
            super(context);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            anh = BitmapFactory.decodeResource(context.getResources(), anhId, options);
            this.markers = markers;
            this.idDangChon = idDangChon;
            this.chon = chon;
            setClickable(true);
            setContentDescription("Sơ đồ tham quan có Marker tương tác");
            cuChi = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onDown(MotionEvent e) { return true; }
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    chonTai(e.getX(), e.getY());
                    performClick();
                    return true;
                }
            });
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.parseColor("#F1F4EE"));
            if (anh == null) return;
            float scale = Math.min(getWidth() / (float) anh.getWidth(), getHeight() / (float) anh.getHeight());
            float w = anh.getWidth() * scale, h = anh.getHeight() * scale;
            vungAnh.set((getWidth() - w) / 2, (getHeight() - h) / 2,
                    (getWidth() + w) / 2, (getHeight() + h) / 2);
            canvas.drawBitmap(anh, null, vungAnh, paint);
            for (DiemThamQuan d : markers) {
                float x = vungAnh.left + (float) d.x * w;
                float y = vungAnh.top + (float) d.y * h;
                boolean selected = d.id == idDangChon;
                float radius = (d.diaDiemId == 3 ? (selected ? 10 : 8) : 16) * scale;
                paint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, radius, paint);
                paint.setColor(Color.parseColor(selected ? "#E8A600" : "#087B5A"));
                canvas.drawCircle(x, y, radius * 13 / 16, paint);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setTextSize(radius * 10 / 16);
                canvas.drawText(String.valueOf(d.soHienThi > 0 ? d.soHienThi : d.id), x,
                        y - (paint.ascent() + paint.descent()) / 2, paint);
            }
        }

        private void chonTai(float x, float y) {
            if (!vungAnh.contains(x, y)) return;
            double distance = Math.pow(dp(getContext(), 28), 2);
            DiemThamQuan nearest = null;
            for (DiemThamQuan d : markers) {
                double dx = x - vungAnh.left - d.x * vungAnh.width();
                double dy = y - vungAnh.top - d.y * vungAnh.height();
                double candidate = dx * dx + dy * dy;
                if (candidate <= distance) { distance = candidate; nearest = d; }
            }
            if (nearest != null && chon != null) chon.chon(nearest.id);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            return cuChi.onTouchEvent(event) || event.getAction() == MotionEvent.ACTION_UP;
        }

        @Override public boolean performClick() { super.performClick(); return true; }
    }
}
