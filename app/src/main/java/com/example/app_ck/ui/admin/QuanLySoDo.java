package com.example.app_ck.ui.admin;

import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Quản trị tầng/sơ đồ cục bộ; cùng nguồn dữ liệu với chọn marker và tour. */
final class QuanLySoDo {
    private final BaseActivity activity;
    private final ActivityResultLauncher<String> chonAnh;
    private Uri anhMoi;
    private ImageView xemTruoc;
    private AlertDialog bieuMau;
    QuanLySoDo(BaseActivity activity) {
        this.activity=activity;
        chonAnh=activity.registerForActivityResult(new ActivityResultContracts.GetContent(),uri->{
            if(uri!=null && bieuMau!=null && bieuMau.isShowing()) {
                anhMoi=uri;xemTruoc.setImageURI(uri);
            }
        });
    }
    void mo(int place,Runnable lamMoi) {
        if(place<0) {bao("Hãy chọn địa điểm trước");return;}
        try(AppDatabase h=new AppDatabase(activity)) {
            LinkedHashMap<Integer,String> tang=KhoDuLieuThamQuan.cacTang(h.getReadableDatabase(),place);
            ArrayList<Integer> ids=new ArrayList<>(tang.keySet());
            ArrayList<String> labels=new ArrayList<>();labels.add("+ Thêm tầng/khu vực");
            for(int id:ids) labels.add(id+" · "+tang.get(id));
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                    .setTitle("Sơ đồ và tầng/khu vực")
                    .setItems(labels.toArray(new String[0]),(d,i)->sua(place,i==0 ? null : ids.get(i-1),tang,lamMoi))
                    .setNegativeButton("Đóng",null).show();
        } catch(Exception e) {bao("Không tải được sơ đồ");}
    }
    private void sua(int place,Integer floor,LinkedHashMap<Integer,String> floors,Runnable lamMoi) {
        String anh="";
        if(floor!=null) try(AppDatabase h=new AppDatabase(activity)) {
            anh=KhoDuLieuThamQuan.anhSoDo(h.getReadableDatabase(),place,floor);
        } catch(Exception e) {bao("Không tải được sơ đồ");return;}
        final String anhCu=anh;
        anhMoi=null;
        LinearLayout form=new LinearLayout(activity);form.setOrientation(LinearLayout.VERTICAL);
        int pad=Math.round(20*activity.getResources().getDisplayMetrics().density);form.setPadding(pad,pad,pad,pad);
        EditText ma=new EditText(activity);ma.setHint("Số tầng/khu vực");ma.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        ma.setText(floor==null ? "" : ""+floor);ma.setEnabled(floor==null);form.addView(ma);
        EditText ten=new EditText(activity);ten.setHint("Tên tầng/khu vực");ten.setText(floor==null ? "" : floors.get(floor));form.addView(ten);
        xemTruoc=new ImageView(activity);xemTruoc.setScaleType(ImageView.ScaleType.FIT_CENTER);
        AnhDiaDiem.hien(xemTruoc,anhCu);form.addView(xemTruoc,new LinearLayout.LayoutParams(-1,pad*8));
        Button chon=new Button(activity);chon.setText("Chọn ảnh sơ đồ trên thiết bị");form.addView(chon);
        chon.setOnClickListener(v->chonAnh.launch("image/*"));
        bieuMau=new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(floor==null ? "Thêm tầng/khu vực" : "Sửa sơ đồ")
                .setView(form).setNegativeButton("Hủy",null).setPositiveButton("Lưu",null)
                .setNeutralButton(floor==null ? "Đóng" : "Xóa tầng/khu vực",null).create();
        bieuMau.setOnShowListener(dialog->{
            bieuMau.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                int id;
                try{id=Integer.parseInt(ma.getText().toString().trim());}
                catch(NumberFormatException e){ma.setError("Nhập số nguyên hợp lệ");return;}
                if(floor==null && floors.containsKey(id)){ma.setError("Tầng/khu vực đã tồn tại");return;}
                if(ten.getText().toString().trim().isEmpty()){ten.setError("Nhập tên tầng/khu vực");return;}
                try(AppDatabase h=new AppDatabase(activity)) {
                    String image=anhMoi==null ? anhCu : AnhDiaDiem.luuTuMay(activity,anhMoi);
                    if(!KhoDuLieuThamQuan.luuSoDo(activity,h.getWritableDatabase(),place,id,ten.getText().toString(),image)) {
                        bao("Không lưu được sơ đồ");return;
                    }
                    bieuMau.dismiss();lamMoi.run();bao("Đã lưu sơ đồ");
                } catch(Exception e){bao("Không lưu được sơ đồ hoặc ảnh");}
            });
            if(floor!=null) bieuMau.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                            .setTitle("Xóa tầng/khu vực?")
                            .setMessage("Chỉ xóa được tầng/khu vực không còn điểm tham quan.")
                            .setNegativeButton("Giữ lại",null).setPositiveButton("Xóa",(d,i)->{
                                try(AppDatabase h=new AppDatabase(activity)) {
                                    if(!KhoDuLieuThamQuan.xoaSoDo(activity,h.getWritableDatabase(),place,floor)) {
                                        bao("Tầng/khu vực còn điểm tham quan hoặc không còn tồn tại");return;
                                    }
                                    bieuMau.dismiss();lamMoi.run();
                                } catch(Exception e){bao("Không xóa được tầng/khu vực");}
                            }).show());
        });
        bieuMau.show();
    }
    private void bao(String text){Toast.makeText(activity,text,Toast.LENGTH_LONG).show();}
}
