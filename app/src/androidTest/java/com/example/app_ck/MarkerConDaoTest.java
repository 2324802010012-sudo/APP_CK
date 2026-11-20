package com.example.app_ck;

import com.example.app_ck.data.seed.CauHinhAutoConDao;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.ui.tour.ConDaoThamQuanActivity;

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
public class MarkerConDaoTest {
    private View timSoDo(View view) {
        if ("Sơ đồ tham quan có marker tương tác".contentEquals(
                view.getContentDescription()==null ? "" : view.getContentDescription())) return view;
        if (view instanceof ViewGroup) for (int i=0;i<((ViewGroup)view).getChildCount();i++) {
            View tim=timSoDo(((ViewGroup)view).getChildAt(i));
            if (tim!=null) return tim;
        }
        return null;
    }

    @Test public void chamDu18MarkerMoDungNoiDungKhongNhanSoSoDoLamId() {
        try (ActivityScenario<ConDaoThamQuanActivity> s=ActivityScenario.launch(ConDaoThamQuanActivity.class)) {
            for (CauHinhAutoConDao.Diem d:CauHinhAutoConDao.CAC_DIEM) {
                if(!d.coMarker()) continue;
                final DiemThamQuan mongDoi;
                try (AppDatabase helper = new AppDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext())) {
                    mongDoi = KhoDuLieuThamQuan.lay(helper.getReadableDatabase(),3,d.id);
                }
                assertNotNull(mongDoi);
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                s.onActivity(a -> {
                    View soDo=timSoDo(a.findViewById(R.id.tqKhungSoDo));
                    assertNotNull(soDo);
                    int w=soDo.getWidth(),h=soDo.getHeight();
                    assertTrue(w>0 && h>0);
                    Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                    soDo.draw(new Canvas(bitmap));
                    bitmap.recycle();
                    float scale=Math.min(w/1366f,h/970f);
                    float x=(w-1366*scale)/2+d.xAnh*1366*scale;
                    float y=(h-970*scale)/2+d.yAnh*970*scale;
                    long t=SystemClock.uptimeMillis();
                    MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
                    MotionEvent up=MotionEvent.obtain(t,t+50,MotionEvent.ACTION_UP,x,y,0);
                    soDo.dispatchTouchEvent(down);
                    soDo.dispatchTouchEvent(up);
                    down.recycle();up.recycle();
                    assertEquals("Số sơ đồ "+d.soSoDo,mongDoi.ten,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertEquals(mongDoi.baiThuyetMinh(),((TextView)a.findViewById(R.id.tqMoTaKhuVuc)).getText().toString());
                });
            }
        }
    }
}
