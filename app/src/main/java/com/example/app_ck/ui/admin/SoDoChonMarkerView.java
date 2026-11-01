package com.example.app_ck.ui.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/** Vẽ ảnh sơ đồ với FIT_CENTER và chạm để lấy X/Y chuẩn hóa trong vùng ảnh. */
public final class SoDoChonMarkerView extends View {
    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF anh = new RectF();
    private double x = Double.NaN, y = Double.NaN;
    private Runnable onMarkerChangedListener;

    public SoDoChonMarkerView(Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
        setBackgroundColor(Color.rgb(239, 244, 239));
    }
    public void setMarker(double x, double y) { this.x = x; this.y = y; invalidate(); }
    public boolean hasMarker() { return Double.isFinite(x) && Double.isFinite(y); }
    public double getXRatio() { return x; }
    public double getYRatio() { return y; }
    public void setOnMarkerChangedListener(Runnable listener) { onMarkerChangedListener = listener; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap.isRecycled() || getWidth() <= 0 || getHeight() <= 0) return;
        float bw = bitmap.getWidth(), bh = bitmap.getHeight();
        float scale = Math.min(getWidth()/bw, getHeight()/bh);
        float w = bw * scale, h = bh * scale;
        float l = (getWidth()-w)/2f, t = (getHeight()-h)/2f;
        anh.set(l, t, l+w, t+h);
        canvas.drawBitmap(bitmap, null, anh, paint);
        if (hasMarker()) {
            float px = anh.left + (float)x*anh.width();
            float py = anh.top + (float)y*anh.height();
            paint.setColor(Color.WHITE); canvas.drawCircle(px,py,17,paint);
            paint.setColor(Color.rgb(35,116,75)); canvas.drawCircle(px,py,13,paint);
            paint.setColor(Color.WHITE); paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(17); paint.setFakeBoldText(true);
            canvas.drawText("1", px, py+6, paint);
            paint.setFakeBoldText(false); paint.setTextAlign(Paint.Align.LEFT);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) return true;
        if (event.getActionMasked() != MotionEvent.ACTION_UP || anh.isEmpty())
            return super.onTouchEvent(event);
        float px=event.getX(), py=event.getY();
        if (!anh.contains(px,py)) return true;
        x = Math.max(0,Math.min(1,(px-anh.left)/anh.width()));
        y = Math.max(0,Math.min(1,(py-anh.top)/anh.height()));
        invalidate(); performClick();
        if (onMarkerChangedListener != null) onMarkerChangedListener.run();
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
