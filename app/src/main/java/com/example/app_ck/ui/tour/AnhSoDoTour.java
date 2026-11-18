
package com.example.app_ck.ui.tour;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.ui.common.AnhDiaDiem;

import java.util.ArrayList;
import java.util.List;

public final class AnhSoDoTour {

    private AnhSoDoTour() {
    }

    public interface ChonDiem {

        void chon(int id);
    }

    private static int dp(Context context, float value) {

        return Math.round(value * context.getResources()
                .getDisplayMetrics().density);
    }

    public static void ganAnhDaiDien(
            Activity activity,
            String tenAnh) {

        ImageView anh = activity.findViewById(
                R.id.tqAnhDiaDiem
        );

        AnhDiaDiem.hien(anh, tenAnh);

        anh.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );
    }

    // Uu tien anh so do sach neu co ban thay the

    public static Bitmap docAnhSoDo(
            Context context,
            String tenAnh) {

        if (tenAnh == null || tenAnh.trim().isEmpty()) {

            return null;
        }

        // Chi ap dung cho tai nguyen drawable.
        // Khong thay doi anh Admin nhap tu may.

        if (tenAnh.matches("[a-z][a-z0-9_]*")
                && !tenAnh.endsWith("_sach")) {

            Bitmap anhSach = AnhDiaDiem.doc(
                    context,
                    tenAnh + "_sach"
            );

            if (anhSach != null) {

                return anhSach;
            }
        }

        return AnhDiaDiem.doc(context, tenAnh);
    }

    // =====================================================
    // HIEN THI SO DO TU SQLITE
    // =====================================================

    public static void themSoDo(
            Activity activity,
            LinearLayout khung,
            int place,
            int tang,
            int idDangChon,
            ChonDiem chon) {

        String tenAnh;

        List<DiemThamQuan> markers = new ArrayList<>();

        try (AppDatabase helper = new AppDatabase(activity)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            tenAnh = KhoDuLieuThamQuan.anhSoDo(
                    db, place, tang
            );

            for (DiemThamQuan diem :
                    KhoDuLieuThamQuan.danhSach(db, place)) {

                if (diem.tang == tang && diem.coMarker()) {

                    markers.add(diem);
                }
            }
        }

        TextView nhan = new TextView(activity);

        nhan.setText("Sơ đồ tham quan");

        nhan.setTextSize(17);

        nhan.setTypeface(
                Typeface.create(
                        "sans-serif-medium",
                        Typeface.NORMAL
                )
        );

        nhan.setTextColor(
                Color.parseColor("#182D22")
        );

        nhan.setPadding(
                0,
                dp(activity, 8),
                0,
                dp(activity, 12)
        );

        khung.addView(nhan);

        Bitmap anh = docAnhSoDo(activity, tenAnh);

        if (anh == null) {

            TextView thieu = new TextView(activity);

            thieu.setText(
                    "Chưa có sơ đồ. Bạn vẫn có thể "
                            + "chọn điểm trong tab Danh sách."
            );

            thieu.setTextSize(14);

            thieu.setTextColor(
                    Color.parseColor("#687B6E")
            );

            thieu.setPadding(
                    dp(activity, 12),
                    dp(activity, 20),
                    dp(activity, 12),
                    dp(activity, 20)
            );

            khung.addView(thieu);

            return;
        }

        SoDoTrongTrang view = new SoDoTrongTrang(
                activity,
                anh,
                markers,
                idDangChon,
                chon
        );

        float density = activity.getResources()
                .getDisplayMetrics().density;

        float manHinhDp = activity.getResources()
                .getDisplayMetrics().heightPixels / density;

        float chieuCao = Math.max(
                240f,
                Math.min(
                        place == 3 ? 440f : 385f,
                        manHinhDp * 0.45f
                )
        );

        SoDoThuPhongLayout.themVao(
                khung,
                view,
                dp(activity, chieuCao)
        );

        if (markers.isEmpty()) {

            TextView thongBao = new TextView(activity);

            thongBao.setText(
                    "Chưa có marker. Hãy chọn điểm từ danh sách."
            );

            thongBao.setTextSize(12);

            thongBao.setTextColor(
                    Color.parseColor("#687B6E")
            );

            khung.addView(thongBao);
        }
    }

    // =====================================================
    // CAP NHAT MARKER KHONG DAT LAI ZOOM
    // =====================================================

    public static void capNhatMarker(
            View khung,
            int idDangChon) {

        if (khung instanceof SoDoTrongTrang) {

            SoDoTrongTrang soDo = (SoDoTrongTrang) khung;

            soDo.idDangChon = idDangChon;

            soDo.invalidate();

            if (soDo.getParent() instanceof View) {

                ((View) soDo.getParent()).invalidate();
            }

            return;
        }

        if (khung instanceof ViewGroup) {

            ViewGroup group = (ViewGroup) khung;

            for (int i = 0; i < group.getChildCount(); i++) {

                capNhatMarker(
                        group.getChildAt(i),
                        idDangChon
                );
            }
        }
    }

    // =====================================================
    // VIEW VE SO DO
    // =====================================================

    private static final class SoDoTrongTrang extends View {

        private final Paint paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
                        | Paint.FILTER_BITMAP_FLAG
        );

        private final RectF vungAnh = new RectF();

        private final List<DiemThamQuan> markers;

        private final ChonDiem chon;

        private final Bitmap anh;

        private final GestureDetector cuChi;

        private int idDangChon;

        SoDoTrongTrang(
                Context context,
                Bitmap bitmap,
                List<DiemThamQuan> markers,
                int idDangChon,
                ChonDiem chon) {

            super(context);

            this.anh = bitmap;

            this.markers = markers;

            this.idDangChon = idDangChon;

            this.chon = chon;

            setClickable(true);

            setContentDescription(
                    "Sơ đồ tham quan có marker tương tác"
            );

            cuChi = new GestureDetector(
                    context,
                    new GestureDetector.SimpleOnGestureListener() {

                        @Override
                        public boolean onDown(MotionEvent e) {

                            return true;
                        }

                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {

                            chonTai(e.getX(), e.getY());

                            performClick();

                            return true;
                        }
                    }
            );
        }

        private float layZoom() {

            if (getParent() instanceof SoDoThuPhongLayout) {

                return ((SoDoThuPhongLayout) getParent())
                        .mucZoom();
            }

            return 1f;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            canvas.drawColor(
                    Color.parseColor("#F1F4EE")
            );

            if (anh == null
                    || anh.isRecycled()
                    || getWidth() <= 0
                    || getHeight() <= 0) {

                return;
            }

            // Giu dung ti le anh goc

            float scale = Math.min(
                    getWidth() / (float) anh.getWidth(),
                    getHeight() / (float) anh.getHeight()
            );

            float w = anh.getWidth() * scale;

            float h = anh.getHeight() * scale;

            vungAnh.set(
                    (getWidth() - w) / 2f,
                    (getHeight() - h) / 2f,
                    (getWidth() + w) / 2f,
                    (getHeight() + h) / 2f
            );

            paint.setColor(Color.WHITE);

            paint.setAlpha(255);

            canvas.drawBitmap(
                    anh, null, vungAnh, paint
            );

            DiemThamQuan diemChon = null;

            for (DiemThamQuan diem : markers) {

                if (diem.id == idDangChon) {

                    diemChon = diem;

                } else {

                    veMarker(canvas, diem, false);
                }
            }

            // Marker dang chon luon duoc ve sau cung

            if (diemChon != null) {

                veMarker(canvas, diemChon, true);
            }
        }

        private void veMarker(
                Canvas canvas,
                DiemThamQuan diem,
                boolean dangChon) {

            float x = vungAnh.left
                    + (float) diem.x * vungAnh.width();

            float y = vungAnh.top
                    + (float) diem.y * vungAnh.height();

            int so = diem.soHienThi > 0
                    ? diem.soHienThi
                    : diem.id;

            GiaoDienSoDo.veMarker(
                    getContext(),
                    canvas,
                    paint,
                    x,
                    y,
                    so,
                    dangChon,
                    diem.diaDiemId == 3,
                    layZoom()
            );
        }

        // =================================================
        // CHON MARKER GAN NHAT
        // =================================================

        private void chonTai(float x, float y) {

            float radius = GiaoDienSoDo.banKinhCham(
                    getContext(), layZoom()
            );

            float khoangCach = radius * radius;

            DiemThamQuan ganNhat = null;

            for (DiemThamQuan diem : markers) {

                float markerX = vungAnh.left
                        + (float) diem.x * vungAnh.width();

                float markerY = vungAnh.top
                        + (float) diem.y * vungAnh.height();

                float dx = x - markerX;

                float dy = y - markerY;

                float kc = dx * dx + dy * dy;

                if (kc <= khoangCach) {

                    khoangCach = kc;

                    ganNhat = diem;
                }
            }

            if (ganNhat != null && chon != null) {

                chon.chon(ganNhat.id);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            return cuChi.onTouchEvent(event)
                    || event.getAction()
                    == MotionEvent.ACTION_UP;
        }

        @Override
        public boolean performClick() {

            super.performClick();

            return true;
        }
    }
}
