package com.example.app_ck;

import android.speech.tts.TextToSpeech;
import java.util.List;

public interface NguonThamQuanTuDong {
    final class Diem {
        public final int id, tang;
        public final String ten;
        public Diem(int id, String ten, int tang) { this.id = id; this.ten = ten; this.tang = tang; }
    }
    int idDiaDiemTuDong();
    List<Diem> cacDiemTuDong();
    int idDiemDangChon();
    void chonDiemTuDong(int id);
    TextToSpeech mayDocTuDong();
    boolean sanSangTuDong();
}
