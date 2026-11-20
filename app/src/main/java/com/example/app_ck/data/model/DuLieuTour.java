package com.example.app_ck.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dữ liệu một lần đọc tour; không giữ Cursor, SQLiteDatabase hoặc Activity. */
public final class DuLieuTour {
    public final DiaDiem diaDiem;
    public final List<DiemThamQuan> cacDiem;
    public final Map<Integer, String> cacTang;
    public final Map<Integer, String> anhSoDo;

    public DuLieuTour(DiaDiem diaDiem, List<DiemThamQuan> cacDiem,
                     Map<Integer, String> cacTang, Map<Integer, String> anhSoDo) {
        this.diaDiem = diaDiem;
        this.cacDiem = Collections.unmodifiableList(new ArrayList<>(cacDiem));
        this.cacTang = Collections.unmodifiableMap(new LinkedHashMap<>(cacTang));
        this.anhSoDo = Collections.unmodifiableMap(new LinkedHashMap<>(anhSoDo));
    }

    public DiemThamQuan timDiem(int id) {
        int viTri = viTriDiem(id);
        return viTri < 0 ? null : cacDiem.get(viTri);
    }

    public int viTriDiem(int id) {
        for (int i = 0; i < cacDiem.size(); i++) {
            if (cacDiem.get(i).id == id) return i;
        }
        return -1;
    }

    public DiemThamQuan diemDauTang(int tang) {
        for (DiemThamQuan diem : cacDiem) {
            if (diem.tang == tang) return diem;
        }
        return null;
    }

    public String tenTang(int tang) {
        String ten = cacTang.get(tang);
        return ten == null || ten.trim().isEmpty() ? "Khu vực " + tang : ten;
    }
}
