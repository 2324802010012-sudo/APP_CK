package com.example.app_ck.ui.tour;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import com.example.app_ck.data.model.DiemThamQuan;

/** Bộ vẽ Huế giữ phép biến đổi và gesture riêng, không phụ thuộc Activity tham quan. */
public final class SoDoHueView extends View {

    private Bitmap anh;
    private final java.util.List<DiemThamQuan> cacMarker;
    private final java.util.function.IntConsumer chonMarker;
    private int idDangChon;

    private final Paint but = new Paint(
            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final RectF vungAnh = new RectF();

    private final int nguongKeo;
    private final ScaleGestureDetector boThuPhong;

    private float zoom = 1f;
    private float dx = 0f, dy = 0f;
    private float xTruoc, yTruoc, xBanDau, yBanDau;

    private boolean daKeo, daDaChamHaiNgon;

    public SoDoHueView(Context context, Bitmap bitmap,
            java.util.List<DiemThamQuan> markers, int idDangChon,
            java.util.function.IntConsumer chonMarker) {

        super(context);

        anh = bitmap;
        this.cacMarker = new java.util.ArrayList<>(markers);
        this.idDangChon = idDangChon;
        this.chonMarker = chonMarker;


        nguongKeo = ViewConfiguration.get(context)
                .getScaledTouchSlop();

        boThuPhong = new ScaleGestureDetector(
                context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScale(ScaleGestureDetector d) {

                        zoom = Math.max(
                                1f,
                                Math.min(5f, zoom * d.getScaleFactor())
                        );

                        gioiHan();
                        invalidate();

                        return true;
                    }
                }
        );

        setClickable(true);

        setContentDescription(
                "Sơ đồ Hoàng thành Huế có "
                        + cacMarker.size() + " Marker tương tác"
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawColor(Color.parseColor("#F1F4EE"));

        if (anh == null || anh.isRecycled()
                || getWidth() <= 0 || getHeight() <= 0) {

            return;
        }

        float tiLe = Math.min(
                getWidth() / (float) anh.getWidth(),
                getHeight() / (float) anh.getHeight()
        );

        float w = tiLe * anh.getWidth();
        float h = tiLe * anh.getHeight();

        vungAnh.set(
                (getWidth() - w) / 2f,
                (getHeight() - h) / 2f,
                (getWidth() + w) / 2f,
                (getHeight() + h) / 2f
        );

        canvas.save();

        canvas.clipRect(0, 0, getWidth(), getHeight());

        canvas.translate(
                getWidth() / 2f + dx,
                getHeight() / 2f + dy
        );

        canvas.scale(zoom, zoom);

        canvas.translate(
                -getWidth() / 2f,
                -getHeight() / 2f
        );

        but.setStyle(Paint.Style.FILL);
        but.setColor(Color.WHITE);

        canvas.drawBitmap(anh, null, vungAnh, but);

        DiemThamQuan diemChon = null;

        for (DiemThamQuan diem : cacMarker) {

            if (diem.id == idDangChon) {

                diemChon = diem;

            } else {

                veMarker(canvas, diem, false);
            }
        }

        if (diemChon != null) {

            veMarker(canvas, diemChon, true);
        }

        canvas.restore();
    }


    private void veMarker(
            Canvas canvas,
            DiemThamQuan diem,
            boolean dangChon) {

        float x = vungAnh.left
                + (float) diem.x * vungAnh.width();

        float y = vungAnh.top
                + (float) diem.y * vungAnh.height();

        GiaoDienSoDo.veMarker(
                getContext(),
                canvas,
                but,
                x,
                y,
                diem.soHienThi > 0 ? diem.soHienThi : diem.id,
                dangChon,
                false,
                zoom
        );
    }


    public void chonMarker(int id) {
        idDangChon = id;
        invalidate();
    }

    public void giaiPhongAnh() {
        Bitmap cu = anh;
        anh = null;
        if (cu != null && !cu.isRecycled()) cu.recycle();
    }

    public void doiZoom(float heSo) {

        zoom = Math.max(1f, Math.min(5f, zoom * heSo));

        gioiHan();
        invalidate();
    }

    public void datLai() {

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

        return (x - getWidth() / 2f - dx) / zoom
                + getWidth() / 2f;
    }

    private float veYGoc(float y) {

        return (y - getHeight() / 2f - dy) / zoom
                + getHeight() / 2f;
    }

    private void chanCuon(boolean chan) {

        if (getParent() != null) {

            getParent().requestDisallowInterceptTouchEvent(chan);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (anh == null || anh.isRecycled()) return false;

        boThuPhong.onTouchEvent(e);

        switch (e.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                xBanDau = xTruoc = e.getX();
                yBanDau = yTruoc = e.getY();

                daKeo = false;
                daDaChamHaiNgon = false;

                chanCuon(true);

                return true;

            case MotionEvent.ACTION_POINTER_DOWN:

                daDaChamHaiNgon = true;

                chanCuon(true);

                return true;

            case MotionEvent.ACTION_MOVE:

                if (e.getPointerCount() > 1) {

                    daDaChamHaiNgon = true;

                    return true;
                }

                if (Math.abs(e.getX() - xBanDau) > nguongKeo
                        || Math.abs(e.getY() - yBanDau) > nguongKeo) {

                    daKeo = true;
                }

                if (zoom > 1f && !daDaChamHaiNgon) {

                    dx += e.getX() - xTruoc;
                    dy += e.getY() - yTruoc;

                    gioiHan();
                    invalidate();

                } else if (zoom <= 1f && daKeo) {

                    chanCuon(false);
                }

                xTruoc = e.getX();
                yTruoc = e.getY();

                return true;

            case MotionEvent.ACTION_POINTER_UP:

                daDaChamHaiNgon = true;

                return true;

            case MotionEvent.ACTION_UP:

                if (!daKeo && !daDaChamHaiNgon) {

                    chonTai(
                            veXGoc(e.getX()),
                            veYGoc(e.getY())
                    );

                    performClick();
                }

                chanCuon(false);

                return true;

            case MotionEvent.ACTION_CANCEL:

                chanCuon(false);

                return true;

            default:

                return true;
        }
    }


    private void chonTai(float xCham, float yCham) {

        int idChon = -1;

        float radius = GiaoDienSoDo.banKinhCham(
                getContext(),
                zoom
        );

        float kcNhoNhat = radius * radius;

        for (DiemThamQuan diem : cacMarker) {

            float x = vungAnh.left
                    + (float) diem.x * vungAnh.width();

            float y = vungAnh.top
                    + (float) diem.y * vungAnh.height();

            float dx = xCham - x;

            float dy = yCham - y;

            float kc = dx * dx + dy * dy;

            if (kc <= kcNhoNhat) {

                kcNhoNhat = kc;

                idChon = diem.id;
            }
        }

        if (idChon > 0) {

            if (chonMarker != null) chonMarker.accept(idChon);
        }
    }
    @Override
    protected void onSizeChanged(
            int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        gioiHan();

        invalidate();
    }
    @Override
    public boolean performClick() {

        super.performClick();

        return true;
    }
}
