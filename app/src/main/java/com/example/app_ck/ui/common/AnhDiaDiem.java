package com.example.app_ck.ui.common;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

/** Hỗ trợ tên drawable cũ và ảnh do quản trị viên chọn trên thiết bị. */
public final class AnhDiaDiem {
    private static final String PREFIX = "anh_may:";
    public static final String KHONG_ANH = "khong_anh";
    private AnhDiaDiem() {}

    private static File tep(Context context,String tenAnh) {
        if(tenAnh==null || !tenAnh.startsWith(PREFIX)) return null;
        String name=tenAnh.substring(PREFIX.length());
        if(name.isEmpty() || name.contains("/") || name.contains("\\") || name.contains("..")) return null;
        return new File(new File(context.getFilesDir(),"anh_dia_diem"),name);
    }

    /** Ảnh được tham chiếu từ SQLite có thể là drawable hoặc tệp quản trị đã nhập. */
    public static android.graphics.Bitmap doc(
            Context context,
            String tenAnh
    ) {

        if (tenAnh == null
                || tenAnh.trim().isEmpty()
                || KHONG_ANH.equals(tenAnh)) {
            return null;
        }

        try {
            BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
            File file=tep(context,tenAnh);
            int id=file==null ? context.getResources().getIdentifier(tenAnh==null ? "" : tenAnh,"drawable",context.getPackageName()) : 0;
            if(file==null && id==0) return null;
            options.inJustDecodeBounds=true;
            if(file!=null) BitmapFactory.decodeFile(file.getPath(),options);
            else BitmapFactory.decodeResource(context.getResources(),id,options);
            options.inSampleSize=1;
            while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>2048) options.inSampleSize*=2;
            options.inJustDecodeBounds=false;
            return file!=null ? BitmapFactory.decodeFile(file.getPath(),options) : BitmapFactory.decodeResource(context.getResources(),id,options);
        } catch(RuntimeException e) {
            android.util.Log.e("AnhDiaDiem","Không đọc được ảnh",e);return null;
        }
    }

    public static String luuTuMay(Context context, Uri uri) throws Exception {
        File folder = new File(context.getFilesDir(), "anh_dia_diem");
        if (!folder.exists() && !folder.mkdirs()) throw new Exception("Không tạo được thư mục ảnh");
        String name = UUID.randomUUID().toString() + ".jpg";
        File file = new File(folder, name);
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(file)) {
            if (in == null) throw new Exception("Không đọc được ảnh");
            BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(in,null,options);
            options.inSampleSize=1;
            while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>2048) options.inSampleSize*=2;
            options.inJustDecodeBounds=false;
            android.graphics.Bitmap image;
            try(InputStream again=context.getContentResolver().openInputStream(uri)) {
                image=BitmapFactory.decodeStream(again,null,options);
            }
            if (image == null) throw new Exception("Ảnh không hợp lệ");
            try {
                if (!image.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out))
                    throw new Exception("Không thể lưu ảnh");
            } finally { image.recycle(); }
        } catch (Exception e) { file.delete(); throw e; }
        return PREFIX + name;
    }

    // Gan anh neu tai nguyen ton tai va doc duoc.
    public static boolean ganNeuCo(
            ImageView anh,
            String tenAnh
    ) {

        if (anh == null
                || tenAnh == null
                || tenAnh.trim().isEmpty()
                || KHONG_ANH.equals(tenAnh)) {
            return false;
        }

        try {

            // Anh do Admin chon tu thiet bi.
            if (tenAnh.startsWith(PREFIX)) {

                android.graphics.Bitmap bitmap =
                        doc(anh.getContext(), tenAnh);

                if (bitmap == null) {
                    return false;
                }

                anh.setImageBitmap(bitmap);

                return true;
            }

            // Anh co san trong drawable.
            int idAnh = anh.getResources().getIdentifier(
                    tenAnh,
                    "drawable",
                    anh.getContext().getPackageName()
            );

            if (idAnh == 0) {
                return false;
            }

            anh.setImageResource(idAnh);

            return true;

        } catch (RuntimeException e) {

            android.util.Log.e(
                    "AnhDiaDiem",
                    "Khong hien thi duoc anh: " + tenAnh,
                    e
            );

            return false;
        }
    }

    // Hien thi anh, hoac anh thay the neu bi loi.
    public static void hien(
            ImageView anh,
            String tenAnh
    ) {

        if (anh == null) return;

        if (!ganNeuCo(anh, tenAnh)) {

            anh.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }
}
