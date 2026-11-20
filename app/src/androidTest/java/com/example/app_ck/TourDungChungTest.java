package com.example.app_ck;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.model.DuLieuTour;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.location.HuongDanTour;
import com.example.app_ck.ui.tour.DieuHuongThamQuan;
import com.example.app_ck.ui.tour.SoDoHueView;
import com.example.app_ck.ui.tour.ThamQuanActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TourDungChungTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test public void namDiaDiemDungMotActivityGiuDungIdVaTrangThaiKhiTaoLai() {
        for (int place = 0; place < 5; place++) {
            DuLieuTour tour;
            try (AppDatabase helper = new AppDatabase(context)) {
                tour = KhoDuLieuThamQuan.docTour(helper.getReadableDatabase(), place);
            }
            DiemThamQuan cuoi = tour.cacDiem.get(tour.cacDiem.size() - 1);
            Intent intent = DieuHuongThamQuan.taoIntent(context, place, cuoi.id);
            assertEquals(ThamQuanActivity.class.getName(), intent.getComponent().getClassName());
            assertEquals(place, intent.getIntExtra("viTriDiaDiem", -1));
            assertEquals(cuoi.id, intent.getIntExtra("idMarker", -1));
            try (ActivityScenario<ThamQuanActivity> scenario = ActivityScenario.launch(intent)) {
                scenario.onActivity(a -> {
                    assertEquals(tour.diaDiem.ten, ((TextView)a.findViewById(R.id.tqTenDiaDiem)).getText().toString());
                    assertEquals(cuoi.ten, ((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertEquals(cuoi.baiThuyetMinh(), ((TextView)a.findViewById(R.id.tqMoTaKhuVuc)).getText().toString());
                    assertNull(UiKiemThu.truong(a,"khungNghe"));
                    a.findViewById(R.id.tqTabDanhSach).performClick();
                });
                scenario.recreate();
                scenario.onActivity(a -> {
                    assertEquals(cuoi.ten, ((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertEquals(View.VISIBLE,a.findViewById(R.id.tqKhungDanhSach).getVisibility());
                    assertFalse(((HuongDanTour)UiKiemThu.truong(a,"huongDan")).dangPhatTuDong());
                });
            }
        }
    }

    @Test public void ngheNhanhChiChuyenVaMoBaiSauKhiDongYKhongGiuLenhSauOnStop() {
        for (int place = 0; place < 5; place++) {
            try (ActivityScenario<ThamQuanActivity> scenario = ActivityScenario.launch(
                    DieuHuongThamQuan.taoIntent(context,place,-1))) {
                final AlertDialog[] lenhCu = new AlertDialog[1];
                scenario.onActivity(a -> {
                    DuLieuTour tour = (DuLieuTour)UiKiemThu.truong(a,"duLieu");
                    DiemThamQuan dich = tour.cacDiem.get(1);
                    Object huongDan = UiKiemThu.truong(a,"huongDan");
                    gan(huongDan,"cheDo",2);
                    gan(huongDan,"dangDoc",true);
                    gan(a,"luotDoc","luot_dang_phat");
                    String tenCu = ((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString();
                    View nghe = UiKiemThu.timTheoMoTa(a.findViewById(R.id.tqDanhSachDiem),"Nghe " + dich.ten);
                    assertNotNull(nghe);
                    nghe.performClick();
                    AlertDialog hoi = (AlertDialog)UiKiemThu.truong(a,"hoiChuyenDiem");
                    assertNotNull(hoi);
                    assertEquals("luot_dang_phat",UiKiemThu.truong(a,"luotDoc"));
                    assertEquals(tenCu,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertNull(UiKiemThu.truong(a,"khungNghe"));
                    hoi.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(a -> {
                    DuLieuTour tour = (DuLieuTour)UiKiemThu.truong(a,"duLieu");
                    DiemThamQuan dich = tour.cacDiem.get(1);
                    assertEquals(tour.cacDiem.get(0).ten,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertEquals("luot_dang_phat",UiKiemThu.truong(a,"luotDoc"));
                    assertNull(UiKiemThu.truong(a,"khungNghe"));
                    View nghe = UiKiemThu.timTheoMoTa(a.findViewById(R.id.tqDanhSachDiem),"Nghe " + dich.ten);
                    nghe.performClick();
                    ((AlertDialog)UiKiemThu.truong(a,"hoiChuyenDiem")).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(a -> {
                    DuLieuTour tour = (DuLieuTour)UiKiemThu.truong(a,"duLieu");
                    DiemThamQuan dich = tour.cacDiem.get(1);
                    Object huongDan = UiKiemThu.truong(a,"huongDan");
                    assertEquals(dich.ten,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertFalse(((HuongDanTour)huongDan).dangPhatTuDong());
                    BottomSheetDialog khung = (BottomSheetDialog)UiKiemThu.truong(UiKiemThu.truong(a,"khungNghe"),"dialog");
                    assertTrue(khung.isShowing());
                    assertEquals(dich.ten,((TextView)khung.findViewById(R.id.ngheTenDiem)).getText().toString());
                    a.dongKhungThuyetMinh();
                    gan(huongDan,"cheDo",2);
                    gan(huongDan,"dangDoc",true);
                    a.findViewById(R.id.tqNutTruoc).performClick();
                    lenhCu[0] = (AlertDialog)UiKiemThu.truong(a,"hoiChuyenDiem");
                    assertTrue(lenhCu[0].isShowing());
                });
                scenario.moveToState(Lifecycle.State.CREATED);
                scenario.onActivity(a -> {
                    String ten = ((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString();
                    lenhCu[0].getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    assertEquals(ten,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertNull(UiKiemThu.truong(a,"luotDoc"));
                });
                scenario.moveToState(Lifecycle.State.RESUMED);
                scenario.onActivity(a -> lenhCu[0].getButton(AlertDialog.BUTTON_POSITIVE).performClick());
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(a -> {
                    assertFalse(((HuongDanTour)UiKiemThu.truong(a,"huongDan")).dangPhatTuDong());
                    assertNull(UiKiemThu.truong(a,"luotDoc"));
                    assertNull(UiKiemThu.truong(a,"hoiChuyenDiem"));
                    DuLieuTour tour = (DuLieuTour)UiKiemThu.truong(a,"duLieu");
                    assertEquals("Callback cũ không đổi điểm sau khi quay lại",tour.cacDiem.get(1).ten,
                            ((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertFalse(((BottomSheetDialog)UiKiemThu.truong(UiKiemThu.truong(a,"khungNghe"),"dialog")).isShowing());
                });
            }
        }
    }

    @Test public void hueGiuViewVaZoomKhiChonDiemKhacCungSoDo() {
        try (ActivityScenario<ThamQuanActivity> scenario = ActivityScenario.launch(
                DieuHuongThamQuan.taoIntent(context,2,1))) {
            scenario.onActivity(a -> {
                SoDoHueView view = timSoDo(a.findViewById(R.id.tqKhungSoDo));
                assertNotNull(view);
                view.doiZoom(3f);
                a.findViewById(R.id.tqNutTiep).performClick();
                assertSame(view,timSoDo(a.findViewById(R.id.tqKhungSoDo)));
                assertEquals(3f,(Float)UiKiemThu.truong(view,"zoom"),.001f);
                assertNull(UiKiemThu.truong(a,"khungNghe"));
                a.findViewById(R.id.tqTabDanhSach).performClick();
                a.findViewById(R.id.tqTabSoDo).performClick();
                assertSame(view,timSoDo(a.findViewById(R.id.tqKhungSoDo)));
                ((ViewGroup)view.getParent()).removeView(view);
                ((android.graphics.Bitmap)UiKiemThu.truong(view,"anh")).recycle();
                view.giaiPhongAnh();
                assertNull("View đã giải phóng không giữ bitmap đã recycle",UiKiemThu.truong(view,"anh"));
            });
        }
    }

    @Test public void diemConDaoKhongMarkerVanMoDungBaiThuCong() {
        try (ActivityScenario<ThamQuanActivity> scenario = ActivityScenario.launch(
                DieuHuongThamQuan.taoIntent(context,3,7))) {
            scenario.onActivity(a -> {
                DuLieuTour tour = (DuLieuTour)UiKiemThu.truong(a,"duLieu");
                DiemThamQuan diem = tour.timDiem(7);
                assertNotNull(diem);
                assertFalse(diem.coMarker());
                assertEquals(diem.ten,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                assertNull(UiKiemThu.truong(a,"khungNghe"));
                a.moKhungThuyetMinh(false);
                Object khung = UiKiemThu.truong(a,"khungNghe");
                BottomSheetDialog dialog = (BottomSheetDialog)UiKiemThu.truong(khung,"dialog");
                assertEquals(diem.ten,((TextView)dialog.findViewById(R.id.ngheTenDiem)).getText().toString());
                assertNull(UiKiemThu.truong(khung,"player"));
            });
        }
    }

    private SoDoHueView timSoDo(View view) {
        if (view instanceof SoDoHueView) return (SoDoHueView)view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                SoDoHueView tim = timSoDo(group.getChildAt(i));
                if (tim != null) return tim;
            }
        }
        return null;
    }

    private void gan(Object target, String field, Object value) {
        for (Class<?> cls = target.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(field);
                f.setAccessible(true);
                f.set(target,value);
                return;
            } catch (NoSuchFieldException e) {
                // Kiểm tra lớp cha.
            } catch (IllegalAccessException e) { throw new AssertionError(e); }
        }
        throw new AssertionError(field);
    }
}
