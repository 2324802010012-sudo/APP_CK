
package com.example.app_ck.ui.tour;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public final class SoDoThuPhongLayout extends FrameLayout {

    private final ScaleGestureDetector boThuPhong;

    private final int nguongKeo;

    private float zoom = 1f;

    private float dx, dy;
    private float xTruoc, yTruoc;
    private float xDau, yDau;

    private boolean daKeo;
    private boolean daHaiNgon;
    private boolean daHuyCham;

    public SoDoThuPhongLayout(Context context) {

        super(context);

        setClipChildren(true);

        nguongKeo = ViewConfiguration.get(context)
                .getScaledTouchSlop();

        boThuPhong = new ScaleGestureDetector(
                context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScale(
                            ScaleGestureDetector detector) {

                        doiZoom(detector.getScaleFactor());

                        return true;
                    }
                }
        );
    }

    public static SoDoThuPhongLayout themVao(
            LinearLayout cha,
            View soDo,
            int chieuCao) {

        Context context = cha.getContext();

        SoDoThuPhongLayout khung =
                new SoDoThuPhongLayout(context);

        khung.addView(
                soDo,
                new FrameLayout.LayoutParams(-1, -1)
        );

        GiaoDienSoDo.themVao(
                cha,
                khung,
                chieuCao,
                () -> khung.doiZoom(1f / 1.5f),
                khung::datLai,
                () -> khung.doiZoom(1.5f)
        );

        return khung;
    }

    public float mucZoom() {

        return zoom;
    }

    public void doiZoom(float heSo) {

        zoom = Math.max(1f, Math.min(5f, zoom * heSo));

        gioiHan();

        capNhatHinhVe();
    }

    public void datLai() {

        zoom = 1f;

        dx = 0f;
        dy = 0f;

        capNhatHinhVe();
    }

    private void capNhatHinhVe() {

        for (int i = 0; i < getChildCount(); i++) {

            getChildAt(i).invalidate();
        }

        invalidate();
    }

    private void gioiHan() {

        float bienX = (zoom - 1f) * getWidth() / 2f;

        float bienY = (zoom - 1f) * getHeight() / 2f;

        dx = Math.max(-bienX, Math.min(bienX, dx));

        dy = Math.max(-bienY, Math.min(bienY, dy));
    }

    @Override
    protected void onSizeChanged(
            int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        gioiHan();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        int luu = canvas.save();

        canvas.translate(
                getWidth() / 2f + dx,
                getHeight() / 2f + dy
        );

        canvas.scale(zoom, zoom);

        canvas.translate(
                -getWidth() / 2f,
                -getHeight() / 2f
        );

        super.dispatchDraw(canvas);

        canvas.restoreToCount(luu);
    }

    // Chuyen toa do cham ve he toa do so do goc

    private void guiChoSoDo(
            MotionEvent event,
            boolean huy) {

        MotionEvent banSao = MotionEvent.obtain(event);

        Matrix maTran = new Matrix();

        maTran.setValues(new float[]{

                1f / zoom,
                0,
                getWidth() / 2f
                        - (getWidth() / 2f + dx) / zoom,

                0,
                1f / zoom,
                getHeight() / 2f
                        - (getHeight() / 2f + dy) / zoom,

                0, 0, 1
        });

        banSao.transform(maTran);

        if (huy) {

            banSao.setAction(MotionEvent.ACTION_CANCEL);
        }

        super.dispatchTouchEvent(banSao);

        banSao.recycle();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        int hanhDong = event.getActionMasked();

        if (hanhDong == MotionEvent.ACTION_DOWN) {

            xDau = xTruoc = event.getX();

            yDau = yTruoc = event.getY();

            daKeo = false;
            daHaiNgon = false;
            daHuyCham = false;

            if (getParent() != null) {

                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }

        if (event.getPointerCount() > 1) {

            daHaiNgon = true;
        }

        if (hanhDong == MotionEvent.ACTION_MOVE
                && Math.hypot(
                event.getX() - xDau,
                event.getY() - yDau
        ) > nguongKeo) {

            daKeo = true;
        }

        if ((daKeo || daHaiNgon) && !daHuyCham) {

            guiChoSoDo(event, true);

            daHuyCham = true;
        }

        boThuPhong.onTouchEvent(event);

        if (hanhDong == MotionEvent.ACTION_MOVE
                && !daHaiNgon
                && daKeo) {

            if (zoom > 1f) {

                dx += event.getX() - xTruoc;

                dy += event.getY() - yTruoc;

                gioiHan();

                invalidate();

            } else if (getParent() != null) {

                getParent()
                        .requestDisallowInterceptTouchEvent(false);
            }
        }

        if (!daHuyCham) {

            guiChoSoDo(event, false);
        }

        if ((zoom > 1f || daHaiNgon)
                && getParent() != null) {

            getParent()
                    .requestDisallowInterceptTouchEvent(true);
        }

        xTruoc = event.getX();

        yTruoc = event.getY();

        if (hanhDong == MotionEvent.ACTION_UP
                || hanhDong == MotionEvent.ACTION_CANCEL) {

            if (getParent() != null) {

                getParent()
                        .requestDisallowInterceptTouchEvent(false);
            }
        }

        return true;
    }
}
