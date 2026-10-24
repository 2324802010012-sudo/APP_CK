package com.example.app_ck;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class DuLieuNoiDungThamQuanTest {
    @Test public void chuyenSangJavaGiuNguyenTatCaBaiVaNguon() throws Exception {
        assertEquals(5, DuLieuNoiDungThamQuan.DIA_DIEM.size());
        assertEquals(63, DuLieuNoiDungThamQuan.NGUON.size());
        List<String> cacTruong = new ArrayList<>();
        cacTruong.add(String.valueOf(DuLieuNoiDungThamQuan.PHIEN_BAN));
        cacTruong.add(DuLieuNoiDungThamQuan.NGAY_DOI_CHIEU);
        cacTruong.add(DuLieuNoiDungThamQuan.NGUYEN_TAC);
        for (Map.Entry<String, DuLieuNoiDungThamQuan.Nguon> e : DuLieuNoiDungThamQuan.NGUON.entrySet()) {
            cacTruong.add(e.getKey());
            cacTruong.add(e.getValue().ten);
            cacTruong.add(e.getValue().url);
            cacTruong.add(e.getValue().coQuan);
        }
        int soDiem = 0;
        for (DuLieuNoiDungThamQuan.Bai d : DuLieuNoiDungThamQuan.DIA_DIEM) {
            themBai(cacTruong, d);
            for (DuLieuNoiDungThamQuan.Bai p : d.cacDiem) {
                assertSame(p, DuLieuNoiDungThamQuan.lay(d.id, p.id));
                themBai(cacTruong, p);
                soDiem++;
            }
        }
        assertEquals(83, soDiem);
        byte[] bam = MessageDigest.getInstance("SHA-256").digest(
                String.join("\n", cacTruong).getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : bam) hex.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        // Mốc đối chiếu toàn bộ trường dữ liệu trước khi chuyển định dạng ngày 25/10/2026.
        assertEquals("6eea8a0e970b8fbce4c4c3bd9e0127ec1eef072f332a6d0e8ac2d40ab7e29abe", hex.toString());
    }

    private void themBai(List<String> ds, DuLieuNoiDungThamQuan.Bai bai) {
        ds.add(String.valueOf(bai.id));
        ds.add(bai.ten);
        ds.add(bai.gioiThieu);
        ds.add(bai.diemNhan);
        ds.add(bai.thamQuan);
        ds.add(bai.ghiChu);
        ds.add(String.join(",", bai.nguon));
        ds.add(String.valueOf(bai.cacDiem.size()));
    }
}
