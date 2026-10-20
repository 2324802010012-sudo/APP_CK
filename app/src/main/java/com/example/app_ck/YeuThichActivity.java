package com.example.app_ck;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
public class YeuThichActivity extends BaseActivity {
    private static final String[] TEN={"Dinh Độc Lập","Bảo tàng Chứng tích Chiến tranh","Bưu điện Trung tâm Sài Gòn"};
    private LinearLayout g;
    @Override protected void onCreate(Bundle b) { super.onCreate(b); hienThi(); }
    @Override protected void onResume() { super.onResume(); hienThi(); }
    private void hienThi() {
        g=DangNhapActivity.khungGiaoDien(this,"Địa điểm yêu thích","Danh sách được lưu riêng cho tài khoản của bạn");
        if(!PhienDangNhap.daDangNhap(this)) {
            DangNhapActivity.nut(g,"Đăng nhập",()->startActivity(new Intent(this,DangNhapActivity.class)));
            return;
        }
        TextView user=new TextView(this); user.setText("Tài khoản: "+PhienDangNhap.ten(this)); g.addView(user);
        ArrayList<Integer> ds;
        try(TaiKhoanDb db=new TaiKhoanDb(this)) { ds=db.danhSachYeuThich(PhienDangNhap.id(this)); }
        if(ds.isEmpty()) { TextView empty=new TextView(this); empty.setText("Bạn chưa lưu địa điểm nào."); empty.setPadding(0,30,0,20); g.addView(empty); }
        for(int index:ds) {
            if(index<0 || index>=TEN.length) continue;
            DangNhapActivity.nut(g,"♡  "+TEN[index],()->{
                Intent i=new Intent(this,ChiTietDiaDiemActivity.class); i.putExtra("viTriDiaDiem",index); startActivity(i);
            });
            DangNhapActivity.nut(g,"Bỏ yêu thích: "+TEN[index],()->new AlertDialog.Builder(this)
                    .setMessage("Bỏ "+TEN[index]+" khỏi yêu thích?")
                    .setPositiveButton("Bỏ",(d,w)->{ try(TaiKhoanDb db=new TaiKhoanDb(this)) { db.datYeuThich(PhienDangNhap.id(this),index,false); } hienThi(); })
                    .setNegativeButton("Hủy",null).show());
        }
        DangNhapActivity.nut(g,"Đăng xuất",()->new AlertDialog.Builder(this).setMessage("Bạn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất",(d,w)->{PhienDangNhap.dangXuat(this);hienThi();})
                .setNegativeButton("Hủy",null).show());
    }
}
