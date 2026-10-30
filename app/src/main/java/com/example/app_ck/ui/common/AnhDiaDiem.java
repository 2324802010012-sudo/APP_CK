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
    private AnhDiaDiem() {}

    public static String luuTuMay(Context context, Uri uri) throws Exception {
        File folder = new File(context.getFilesDir(), "anh_dia_diem");
        if (!folder.exists() && !folder.mkdirs()) throw new Exception("Không tạo được thư mục ảnh");
        String name = UUID.randomUUID().toString() + ".jpg";
        File file = new File(folder, name);
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(file)) {
            if (in == null) throw new Exception("Không đọc được ảnh");
            android.graphics.Bitmap image = BitmapFactory.decodeStream(in);
            if (image == null) throw new Exception("Ảnh không hợp lệ");
            try {
                if (!image.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out))
                    throw new Exception("Không thể lưu ảnh");
            } finally { image.recycle(); }
        } catch (Exception e) { file.delete(); throw e; }
        return PREFIX + name;
    }

    public static void hien(ImageView view, String tenAnh) {
        Context context = view.getContext();
        if (tenAnh != null && tenAnh.startsWith(PREFIX)) {
            String filename = tenAnh.substring(PREFIX.length());
            if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
                view.setImageResource(android.R.drawable.ic_menu_gallery); return;
            }
            File file = new File(new File(context.getFilesDir(), "anh_dia_diem"), filename);
            if (file.isFile()) {
                android.graphics.Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) { view.setImageBitmap(bitmap); return; }
            }
        }
        int id = context.getResources().getIdentifier(tenAnh == null ? "" : tenAnh,
                "drawable", context.getPackageName());
        view.setImageResource(id == 0 ? android.R.drawable.ic_menu_gallery : id);
    }
}
