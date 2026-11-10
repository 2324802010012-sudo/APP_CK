package com.example.app_ck;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.widget.Spinner;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoNguonThongTin;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.admin.QuanLyDiemThamQuanActivity;
import com.example.app_ck.ui.tour.ThamQuanActivity;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SQLiteQuanTriFlowTest {
    private Context context;
    private long user,oldUser;
    private String username,oldName,placeName;
    private int place=-1;
    @Before public void setup() {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();UiKiemThu.bat(context);
        oldUser=PhienDangNhap.id(context);oldName=PhienDangNhap.ten(context);
        username="sqlite_ui_"+Long.toHexString(System.nanoTime());placeName="Địa điểm "+username;
        try(AppDatabase h=new AppDatabase(context)) {
            assertTrue(h.dangKy(username,"password123"));user=h.dangNhap(username,"password123");
            // Quyền fixture chỉ cho tài khoản thử vừa tạo.
            h.getWritableDatabase().execSQL("UPDATE tai_khoan SET vai_tro='ADMIN' WHERE id=?",new Object[]{user});
            PhienDangNhap.luu(context,user,username);
            ContentValues v=new ContentValues();v.put("ten",placeName);v.put("vi_do",10d);v.put("kinh_do",106d);
            assertTrue(KhoDuLieuThamQuan.luuKemNguon(context,h.getWritableDatabase(),true,-1,0,v,"",""));
            for(DiaDiem d:h.danhSachDiaDiem()) if(placeName.equals(d.ten)) place=d.id;
            assertTrue(place>=5);
        }
    }
    @After public void cleanup() {
        try(AppDatabase h=new AppDatabase(context)) {
            PhienDangNhap.luu(context,user,username);
            if(place>=0) {
                for(DiemThamQuan d:KhoDuLieuThamQuan.danhSach(h.getWritableDatabase(),place))
                    KhoDuLieuThamQuan.xoaDiem(context,h.getWritableDatabase(),place,d.id);
                KhoDuLieuThamQuan.xoaDiaDiem(context,h.getWritableDatabase(),place);
                h.getWritableDatabase().delete("nguon_thong_tin","ma LIKE ?",new String[]{"admin_diem_"+place+"_%"});
            }
            h.getWritableDatabase().delete("tai_khoan","id=?",new String[]{""+user});
        } finally {
            if(oldUser>0) PhienDangNhap.luu(context,oldUser,oldName);else PhienDangNhap.dangXuat(context);
            UiKiemThu.tat(context);
        }
    }
    @Test public void adminCreatesFloorAndPointThenNewPlaceTourReadsSameData() {
        int point;
        try(ActivityScenario<QuanLyDiemThamQuanActivity> s=ActivityScenario.launch(QuanLyDiemThamQuanActivity.class)) {
            s.onActivity(a->{
                Spinner spinner=a.findViewById(R.id.dtChonDiaDiem);
                for(int i=0;i<spinner.getCount();i++) if(placeName.equals(spinner.getItemAtPosition(i))) spinner.setSelection(i);
            });
            onView(withId(R.id.dtQuanLySoDo)).perform(click());
            onView(withText("+ Thêm tầng/khu vực")).perform(click());
            onView(withHint("Số tầng/khu vực")).perform(replaceText("7"),closeSoftKeyboard());
            onView(withHint("Tên tầng/khu vực")).perform(replaceText("Khu vực mới"),closeSoftKeyboard());
            onView(withText("Lưu")).perform(click());
            onView(withId(R.id.dtThemMoi)).perform(click());
            onView(withId(R.id.dtTen)).perform(scrollTo(),replaceText("Điểm mới từ quản trị"),closeSoftKeyboard());
            onView(withId(R.id.dtTang)).perform(scrollTo(),replaceText("7"),closeSoftKeyboard());
            onView(withId(R.id.dtThuyetMinh)).perform(scrollTo(),replaceText("Nội dung từ SQLite"),closeSoftKeyboard());
            onView(withHint("Tên tài liệu / cơ quan cung cấp")).perform(scrollTo(),replaceText("Nguồn riêng"),closeSoftKeyboard());
            onView(withHint("https://...")).perform(scrollTo(),replaceText("https://example.com/source"),closeSoftKeyboard());
            onView(withId(R.id.dtLuu)).perform(scrollTo(),click());
            onView(withId(R.id.dtThemMoi)).check(matches(isDisplayed()));
        }
        try(AppDatabase h=new AppDatabase(context)) {
            assertEquals("Khu vực mới",KhoDuLieuThamQuan.cacTang(h.getReadableDatabase(),place).get(7));
            java.util.ArrayList<DiemThamQuan> ds=KhoDuLieuThamQuan.danhSach(h.getReadableDatabase(),place);
            assertEquals(1,ds.size());point=ds.get(0).id;
            assertEquals("Nguồn riêng",KhoNguonThongTin.danhSach(h.getReadableDatabase(),place,point).get(0).ten);
        }
        Intent tour=new Intent(context,ThamQuanActivity.class).putExtra("viTriDiaDiem",place).putExtra("idMarker",point);
        try(ActivityScenario<ThamQuanActivity> s=ActivityScenario.launch(tour)) {
            onView(withId(R.id.tqTenDiaDiem)).check(matches(withText(placeName)));
            onView(withId(R.id.tqTenKhuVuc)).check(matches(withText("Điểm mới từ quản trị")));
            onView(withId(R.id.tqMoTaKhuVuc)).check(matches(withText("Nội dung từ SQLite")));
        }
    }
}
