package com.example.app_ck.util;

import java.text.Normalizer;
import java.util.Locale;

/** Chuẩn hóa tiếng Việt dùng chung, độc lập Android và điều kiện lọc của mỗi màn hình. */
public final class VanBanTimKiem {
    private VanBanTimKiem() {}

    public static String boDau(String text) {
        return Normalizer.normalize(text == null ? "" : text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').trim();
    }

    public static String chuanHoa(String text) {
        return boDau(text).replaceAll("\\s+", " ");
    }
}
