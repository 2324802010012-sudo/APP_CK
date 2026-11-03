package com.example.app_ck.ui.admin;

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

/**
 * So do tham quan: pinch zoom, keo de di chuyen, cham de dat Marker.
 * X/Y luon la toa do chuan hoa tren anh GOC (0..1), khong phu thuoc zoom.
 */
public final class SoDoChonMarkerView extends View {
    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 8f;

    private final Bitmap bitmap;
    private final Paint anhPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF vungAnh = new RectF();
    private final ScaleGestureDetector boPhongTo;
    private final int nguongKeo;

    private double x = Double.NaN;
    private double y = Double.NaN;
    private float zoom = 1f;
    private float dichX = 0f;
    private float dichY = 0f;
    private float diemX, diemY, batDauX, batDauY;
    private boolean daKeo, daDungNhieuNgon;
    private Runnable onMarkerChangedListener;

    public SoDoChonMarkerView(Context context, Bitmap bitmap) {
        super(context);
        if (bitmap == null) throw new IllegalArgumentException("Bitmap khong duoc null");
        this.bitmap = bitmap;
        this.nguongKeo = ViewConfiguration.get(context).getScaledTouchSlop();
        setBackgroundColor(Color.rgb(239, 244, 239));

        boPhongTo = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float zoomCu = zoom;
                        float zoomMoi = gioiHan(zoomCu * detector.getScaleFactor(),
                                MIN_ZOOM, MAX_ZOOM);
                        float heSo = zoomMoi / zoomCu;
                        if (heSo == 1f) return true;

                        // Co dinh vi tri duoi hai ngon tay trong khi zoom.
                        float tamX = getWidth() / 2f;
                        float tamY = getHeight() / 2f;
                        dichX = heSo * dichX + (1f - heSo) * (detector.getFocusX() - tamX);
                        dichY = heSo * dichY + (1f - heSo) * (detector.getFocusY() - tamY);
                        zoom = zoomMoi;
                        gioiHanDiChuyen();
                        invalidate();
                        return true;
                    }
                });
    }

    public void setMarker(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || x < 0 || x > 1 || y < 0 || y > 1) return;
        this.x = x;
        this.y = y;
        invalidate();
        if (onMarkerChangedListener != null) onMarkerChangedListener.run();
    }

    public boolean hasMarker() {
        return Double.isFinite(x) && Double.isFinite(y);
    }

    public double getXRatio() { return x; }
    public double getYRatio() { return y; }
    public void setOnMarkerChangedListener(Runnable listener) {
        onMarkerChangedListener = listener;
    }

    public void zoomIn() { doiZoom(1.5f); }
    public void zoomOut() { doiZoom(1f / 1.5f); }
    public void resetZoom() {
        zoom = 1f;
        dichX = dichY = 0f;
        invalidate();
    }

    private void doiZoom(float heSoMongMuon) {
        float zoomMoi = gioiHan(zoom * heSoMongMuon, MIN_ZOOM, MAX_ZOOM);
        float heSoThucTe = zoomMoi / zoom;
        dichX *= heSoThucTe;
        dichY *= heSoThucTe;
        zoom = zoomMoi;
        gioiHanDiChuyen();
        invalidate();
    }

    private static float gioiHan(float n, float min, float max) {
        return Math.max(min, Math.min(max, n));
    }

    private float tyLeAnhCoBan() {
        if (getWidth() == 0 || getHeight() == 0) return 0f;
        return Math.min(getWidth() / (float) bitmap.getWidth(),
                getHeight() / (float) bitmap.getHeight());
    }

    private void gioiHanDiChuyen() {
        float coBan = tyLeAnhCoBan();
        if (coBan == 0f) return;
        float rong = bitmap.getWidth() * coBan * zoom;
        float cao = bitmap.getHeight() * coBan * zoom;
        float gioiHanX = Math.max(0f, (rong - getWidth()) / 2f);
        float gioiHanY = Math.max(0f, (cao - getHeight()) / 2f);
        dichX = gioiHan(dichX, -gioiHanX, gioiHanX);
        dichY = gioiHan(dichY, -gioiHanY, gioiHanY);
    }

    private void capNhatVungAnh() {
        float coBan = tyLeAnhCoBan();
        if (coBan == 0f) {
            vungAnh.setEmpty();
            return;
        }
        float rong = bitmap.getWidth() * coBan * zoom;
        float cao = bitmap.getHeight() * coBan * zoom;
        float trai = (getWidth() - rong) / 2f + dichX;
        float tren = (getHeight() - cao) / 2f + dichY;
        vungAnh.set(trai, tren, trai + rong, tren + cao);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gioiHanDiChuyen();
        capNhatVungAnh();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap.isRecycled() || getWidth() <= 0 || getHeight() <= 0) return;
        capNhatVungAnh();
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(bitmap, null, vungAnh, anhPaint);

        if (hasMarker()) {
            float px = vungAnh.left + (float) x * vungAnh.width();
            float py = vungAnh.top + (float) y * vungAnh.height();
            float density = getResources().getDisplayMetrics().density;
            markerPaint.setColor(Color.WHITE);
            canvas.drawCircle(px, py, 17f * density, markerPaint);
            markerPaint.setColor(Color.rgb(35, 116, 75));
            canvas.drawCircle(px, py, 13f * density, markerPaint);
            markerPaint.setColor(Color.WHITE);
            markerPaint.setTextAlign(Paint.Align.CENTER);
            markerPaint.setTextSize(16f * density);
            markerPaint.setFakeBoldText(true);
            canvas.drawText("1", px, py + 5f * density, markerPaint);
            markerPaint.setFakeBoldText(false);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        boPhongTo.onTouchEvent(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                diemX = batDauX = event.getX();
                diemY = batDauY = event.getY();
                daKeo = false;
                daDungNhieuNgon = false;
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                daDungNhieuNgon = true;
                daKeo = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && !boPhongTo.isInProgress()) {
                    float px = event.getX();
                    float py = event.getY();
                    if (Math.hypot(px - batDauX, py - batDauY) > nguongKeo)
                        daKeo = true;
                    if (daKeo && zoom > 1f) {
                        dichX += px - diemX;
                        dichY += py - diemY;
                        gioiHanDiChuyen();
                        invalidate();
                    }
                    diemX = px;
                    diemY = py;
                }
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                daDungNhieuNgon = true;
                daKeo = true;
                return true;

            case MotionEvent.ACTION_UP:
                if (!daKeo && !daDungNhieuNgon && !boPhongTo.isInProgress()) {
                    capNhatVungAnh();
                    float px = event.getX();
                    float py = event.getY();
                    if (vungAnh.contains(px, py)) {
                        setMarker(
                                gioiHan((px - vungAnh.left) / vungAnh.width(), 0f, 1f),
                                gioiHan((py - vungAnh.top) / vungAnh.height(), 0f, 1f)
                        );
                        performClick();
                    }
                }
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;

            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
