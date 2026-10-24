package com.example.app_ck;

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
public class MarkerVanMieuTest {
    private View tim(View v) {
        if("Sơ đồ tham quan có Marker tương tác".contentEquals(
                v.getContentDescription()==null ? "" : v.getContentDescription())) return v;
        if(v instanceof ViewGroup) for(int i=0;i<((ViewGroup)v).getChildCount();i++) {
            View k=tim(((ViewGroup)v).getChildAt(i));if(k!=null)return k;
        }
        return null;
    }

    @Test public void chamDung16SoTrenAnhSauPhongVaKeo() {
        int[][] moc={{278,414},{514,534},{523,427},{619,440},{805,457},{955,463},
                {1067,544},{1067,464},{1120,466},{1246,460},{1321,481},{1466,477},
                {1483,567},{1486,407},{1335,618},{688,218}};
        String[] ten={"Phương Đình – Hồ Văn","Bia Hạ Mã","Nghi Môn","Cổng Văn Miếu",
                "Cổng Đại Trung","Khuê Văn Các","Nhà bia Tiến sĩ","Giếng Thiên Quang",
                "Cổng Đại Thành","Điện Đại Thành","Cổng Thái Học","Nhà Thái Học",
                "Lầu Trống","Lầu Chuông","Cổng ra","Nhà Bát Giác – Vườn Giám"};
        try(ActivityScenario<VanMieuThamQuanActivity> s=ActivityScenario.launch(VanMieuThamQuanActivity.class)) {
            for(float zoom:new float[]{1,3}) for(int i=0;i<moc.length;i++) {
                final int id=i;
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                s.onActivity(a -> {
                    View view=tim(a.findViewById(android.R.id.content)); assertNotNull(view);
                    SoDoThuPhongLayout khung=(SoDoThuPhongLayout)view.getParent();
                    int w=view.getWidth(),h=view.getHeight();
                    float scale=Math.min(w/1672f,h/941f);
                    float x=(w-1672*scale)/2+moc[id][0]*scale;
                    float y=(h-941*scale)/2+moc[id][1]*scale;
                    khung.datLai();khung.doiZoom(zoom);
                    try {
                        java.lang.reflect.Field dx=SoDoThuPhongLayout.class.getDeclaredField("dx");dx.setAccessible(true);dx.setFloat(khung,-(x-w/2f)*zoom);
                        java.lang.reflect.Field dy=SoDoThuPhongLayout.class.getDeclaredField("dy");dy.setAccessible(true);dy.setFloat(khung,-(y-h/2f)*zoom);
                        java.lang.reflect.Method gioiHan=SoDoThuPhongLayout.class.getDeclaredMethod("gioiHan");gioiHan.setAccessible(true);gioiHan.invoke(khung);
                        Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(bitmap));bitmap.recycle();
                        x=(x-w/2f)*zoom+w/2f+dx.getFloat(khung);
                        y=(y-h/2f)*zoom+h/2f+dy.getFloat(khung);
                    } catch(ReflectiveOperationException e) {throw new AssertionError(e);}
                    assertTrue(x>=0 && x<=w && y>=0 && y<=h);
                    long t=SystemClock.uptimeMillis();
                    MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
                    MotionEvent up=MotionEvent.obtain(t,t+40,MotionEvent.ACTION_UP,x,y,0);
                    khung.dispatchTouchEvent(down);khung.dispatchTouchEvent(up);
                    down.recycle();up.recycle();
                    assertEquals(ten[id],((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                });
            }
        }
    }
}
