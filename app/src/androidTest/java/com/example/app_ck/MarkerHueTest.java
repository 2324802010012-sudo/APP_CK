package com.example.app_ck;

import com.example.app_ck.ui.tour.DaiNoiHueThamQuanActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MarkerHueTest {
    private View tim(View v) {
        if ("Sơ đồ Hoàng thành Huế có 10 Marker tương tác".contentEquals(
                v.getContentDescription()==null ? "" : v.getContentDescription())) return v;
        if(v instanceof ViewGroup) for(int i=0;i<((ViewGroup)v).getChildCount();i++) {
            View ketQua=tim(((ViewGroup)v).getChildAt(i));
            if(ketQua!=null) return ketQua;
        }
        return null;
    }

    @Test public void chonDungMuoiDiemSauKhiPhongVaKeoAnhNgang() {
        float[][] moc={{.453f,.856f},{.451f,.627f},{.448f,.454f},{.445f,.217f},
                {.569f,.461f},{.532f,.353f},{.694f,.351f},{.201f,.364f},{.201f,.245f},{.676f,.208f}};
        String[] ten={"Ngọ Môn","Điện Thái Hòa","Nền điện Cần Chánh","Điện Kiến Trung",
                "Duyệt Thị Đường","Thái Bình Lâu","Vườn Cơ Hạ","Cung Diên Thọ","Cung Trường Sanh","Hậu hồ"};
        try(ActivityScenario<DaiNoiHueThamQuanActivity> s=ActivityScenario.launch(DaiNoiHueThamQuanActivity.class)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            for(float zoom:new float[]{1,3}) for(int i=0;i<moc.length;i++) {
                final int id=i;
                s.onActivity(a -> {
                    View view=tim(a.findViewById(android.R.id.content));
                    assertNotNull(view);
                    int w=view.getWidth(),h=view.getHeight();
                    float scale=Math.min(w/2048f,h/1366f);
                    float x=(w-2048*scale)/2+moc[id][0]*2048*scale;
                    float y=(h-1366*scale)/2+moc[id][1]*1366*scale;
                    try {
                        java.lang.reflect.Field z=view.getClass().getDeclaredField("zoom"); z.setAccessible(true); z.setFloat(view,zoom);
                        java.lang.reflect.Field dx=view.getClass().getDeclaredField("dx"); dx.setAccessible(true); dx.setFloat(view,-(x-w/2f)*zoom);
                        java.lang.reflect.Field dy=view.getClass().getDeclaredField("dy"); dy.setAccessible(true); dy.setFloat(view,-(y-h/2f)*zoom);
                        java.lang.reflect.Method gioiHan=view.getClass().getDeclaredMethod("gioiHan"); gioiHan.setAccessible(true); gioiHan.invoke(view);
                        Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(bitmap)); bitmap.recycle();
                        x=(x-w/2f)*zoom+w/2f+dx.getFloat(view);
                        y=(y-h/2f)*zoom+h/2f+dy.getFloat(view);
                    } catch(ReflectiveOperationException e) { throw new AssertionError(e); }
                    assertTrue(x>=0 && x<=w && y>=0 && y<=h);
                    long t=SystemClock.uptimeMillis();
                    MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
                    MotionEvent up=MotionEvent.obtain(t,t+40,MotionEvent.ACTION_UP,x,y,0);
                    view.dispatchTouchEvent(down); view.dispatchTouchEvent(up);
                    down.recycle();up.recycle();
                    assertEquals(ten[id],((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                });
            }
        }
    }
}
