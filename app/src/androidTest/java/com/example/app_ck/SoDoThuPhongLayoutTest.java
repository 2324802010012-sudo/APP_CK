package com.example.app_ck;

import com.example.app_ck.ui.tour.SoDoThuPhongLayout;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SoDoThuPhongLayoutTest {
    private static final class SoDoGhiNhan extends View {
        float x, y;
        int soLanChon, soLanHuy;
        SoDoGhiNhan(Context context) { super(context); }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                x = e.getX(); y = e.getY(); soLanChon++;
            }
            if (e.getActionMasked() == MotionEvent.ACTION_CANCEL) soLanHuy++;
            return true;
        }
    }

    private void cham(SoDoThuPhongLayout khung, int action, float x, float y) {
        long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, action, x, y, 0);
        khung.dispatchTouchEvent(event);
        event.recycle();
    }

    private void haiNgon(SoDoThuPhongLayout khung, long batDau, long thoiDiem,
                         int action, float trai, float phai) {
        MotionEvent.PointerProperties[] thuocTinh = new MotionEvent.PointerProperties[2];
        MotionEvent.PointerCoords[] toaDo = new MotionEvent.PointerCoords[2];
        for (int i = 0; i < 2; i++) {
            thuocTinh[i] = new MotionEvent.PointerProperties();
            thuocTinh[i].id = i;
            thuocTinh[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
            toaDo[i] = new MotionEvent.PointerCoords();
            toaDo[i].x = i == 0 ? trai : phai;
            toaDo[i].y = 200;
            toaDo[i].pressure = 1;
            toaDo[i].size = 1;
        }
        MotionEvent event = MotionEvent.obtain(batDau, thoiDiem, action, 2,
                thuocTinh, toaDo, 0, 0, 1, 1, 0, 0,
                android.view.InputDevice.SOURCE_TOUCHSCREEN, 0);
        khung.dispatchTouchEvent(event);
        event.recycle();
    }

    @Test public void haiNgonPhongToKhongKichHoatChonMarker() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            SoDoThuPhongLayout khung = new SoDoThuPhongLayout(context);
            SoDoGhiNhan soDo = new SoDoGhiNhan(context);
            khung.addView(soDo, new android.widget.FrameLayout.LayoutParams(-1, -1));
            // Khoảng cách hai ngón vượt ngưỡng nhận diện theo mật độ của thiết bị thật.
            int size = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY);
            khung.measure(size, size);
            khung.layout(0, 0, 1000, 1000);
            long batDau = SystemClock.uptimeMillis();
            cham(khung, MotionEvent.ACTION_DOWN, 350, 200);
            haiNgon(khung, batDau, batDau + 20,
                    MotionEvent.ACTION_POINTER_DOWN | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                    350, 650);
            haiNgon(khung, batDau, batDau + 40, MotionEvent.ACTION_MOVE, 150, 850);
            haiNgon(khung, batDau, batDau + 60, MotionEvent.ACTION_MOVE, 50, 950);
            haiNgon(khung, batDau, batDau + 70, MotionEvent.ACTION_MOVE, 10, 990);
            haiNgon(khung, batDau, batDau + 80,
                    MotionEvent.ACTION_POINTER_UP | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                    10, 990);
            cham(khung, MotionEvent.ACTION_UP, 10, 200);
            assertEquals(0, soDo.soLanChon);
            assertEquals(1, soDo.soLanHuy);
            cham(khung, MotionEvent.ACTION_DOWN, 700, 500);
            cham(khung, MotionEvent.ACTION_UP, 700, 500);
            assertTrue("Pinch must enlarge the diagram: x=" + soDo.x,
                    soDo.x > 500 && soDo.x < 700);
        });
    }

    @Test public void zoomVaKeoVanChonDungToaDoGocVaDatLai() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            SoDoThuPhongLayout khung = new SoDoThuPhongLayout(context);
            SoDoGhiNhan soDo = new SoDoGhiNhan(context);
            khung.addView(soDo, new android.widget.FrameLayout.LayoutParams(-1, -1));
            int size = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY);
            khung.measure(size, size);
            khung.layout(0, 0, 400, 400);

            khung.doiZoom(2f);
            cham(khung, MotionEvent.ACTION_DOWN, 300, 250);
            cham(khung, MotionEvent.ACTION_UP, 300, 250);
            assertEquals(250f, soDo.x, .01f);
            assertEquals(225f, soDo.y, .01f);

            cham(khung, MotionEvent.ACTION_DOWN, 100, 100);
            cham(khung, MotionEvent.ACTION_MOVE, 200, 100);
            cham(khung, MotionEvent.ACTION_UP, 200, 100);
            assertEquals(1, soDo.soLanChon);
            assertEquals(1, soDo.soLanHuy);
            cham(khung, MotionEvent.ACTION_DOWN, 300, 250);
            cham(khung, MotionEvent.ACTION_UP, 300, 250);
            assertEquals(200f, soDo.x, .01f);
            assertEquals(225f, soDo.y, .01f);

            khung.datLai();
            cham(khung, MotionEvent.ACTION_DOWN, 300, 250);
            cham(khung, MotionEvent.ACTION_UP, 300, 250);
            assertEquals(300f, soDo.x, .01f);
            assertEquals(250f, soDo.y, .01f);
        });
    }

    @Test public void gioiHanZoomTuMotDenNamLan() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            SoDoThuPhongLayout khung = new SoDoThuPhongLayout(context);
            SoDoGhiNhan soDo = new SoDoGhiNhan(context);
            khung.addView(soDo, new android.widget.FrameLayout.LayoutParams(-1, -1));
            int size = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY);
            khung.measure(size, size);
            khung.layout(0, 0, 400, 400);
            khung.doiZoom(100f);
            cham(khung, MotionEvent.ACTION_DOWN, 300, 200);
            cham(khung, MotionEvent.ACTION_UP, 300, 200);
            assertEquals(220f, soDo.x, .01f);
            khung.doiZoom(.001f);
            cham(khung, MotionEvent.ACTION_DOWN, 300, 200);
            cham(khung, MotionEvent.ACTION_UP, 300, 200);
            assertEquals(300f, soDo.x, .01f);
        });
    }
}
