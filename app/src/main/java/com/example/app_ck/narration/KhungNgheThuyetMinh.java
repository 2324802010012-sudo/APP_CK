
package com.example.app_ck.narration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public final class KhungNgheThuyetMinh {

    // =====================================================
    // 1. KHAI BÁO
    // =====================================================

    private static final String TAG = "KhungNgheThuyetMinh";

    private static final long THOI_GIAN_CHO = 60000L;
    private static final long CHU_KY_CAP_NHAT = 250L;
    private static final long CHU_KY_LUU = 5000L;

    private final BaseActivity activity;
    private final BottomSheetDialog dialog;
    private final View view;

    // Thành phần giao diện

    private final SeekBar tua;

    private final ImageButton nutPhat;
    private final ImageButton nutLui;
    private final ImageButton nutToi;

    private final TextView nutTocDo;
    private final TextView nutNgheTiep;

    private final GiaoDienKhungNghe giaoDien;

    // Xử lý bất đồng bộ

    private final Handler main =
            new Handler(Looper.getMainLooper());

    private final ExecutorService nen =
            Executors.newSingleThreadExecutor();

    // Xử lý âm thanh

    private final AudioManager audio;

    private final AudioAttributes attrs =
            new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(
                            AudioAttributes.CONTENT_TYPE_SPEECH
                    )
                    .build();

    private AudioFocusRequest focus;

    private boolean giuFocus;

    private TextToSpeech tts;
    private MediaPlayer player;

    // Dữ liệu các đoạn âm thanh

    private final List<File> files = new ArrayList<>();

    private final List<Integer> durations =
            new ArrayList<>();

    private final Map<String, File> dangGhi =
            new HashMap<>();

    private List<String> chunks;

    // Trạng thái trình nghe

    private boolean ttsReady;
    private boolean ttsLoi;
    private boolean dangTao;
    private boolean muonPhat;
    private boolean dangTua;
    private boolean tiepSauTua;
    private boolean daHuy;
    private boolean daChuanBi;
    private boolean phatSauTai;
    private boolean phatSauTao;
    private boolean daNgheHet;
    private boolean coLuotDaPhat;

    private int phien;
    private int doan;
    private int tong;
    private int nenDoan;
    private int viTriDung;
    private int viTriYeuCau;
    private int theHeTts;

    private String noiDung = "";
    private String luotTao;

    // Dữ liệu địa điểm

    private int idDiaDiem;
    private int idDiem;

    private String cheDoLichSu =
            KhoLichSu.THU_CONG;

    private LuotNgheLichSu luotLichSu;

    // Tốc độ phát

    private static final float[] CAC_TOC_DO = {
            0.75f,
            1.0f,
            1.25f,
            1.5f,
            2.0f
    };

    private static final String[] TEN_TOC_DO = {
            "0.75×",
            "1.0×",
            "1.25×",
            "1.5×",
            "2.0×"
    };

    private int mucTocDo = 1;

    // Ghi nhớ tiến độ nghe

    private final TienDoNghe tienDo;



    private int tienDoCu;

    private long lanLuuCuoi;

    // Hiệu ứng sóng âm


    // Tác vụ định kỳ

    private final Runnable quaHan =
            this::kiemTraQuaHan;

    private final Runnable nhip =
            this::capNhatNhip;

    // =====================================================
    // 2. AUDIO FOCUS VÀ NGẮT TAI NGHE
    // =====================================================

    private final AudioManager.OnAudioFocusChangeListener doiFocus =
            change -> main.post(() -> {

                if (change < 0 && !daHuy) {

                    tamDungNgoai(
                            "Đã tạm dừng vì có âm thanh khác."
                    );
                }
            });

    private final BroadcastReceiver rutTaiNghe =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        Context context,
                        Intent intent
                ) {

                    if (!daHuy
                            && AudioManager.ACTION_AUDIO_BECOMING_NOISY
                            .equals(intent.getAction())) {

                        tamDungNgoai(
                                "Đã tạm dừng khi ngắt tai nghe."
                        );
                    }
                }
            };

    // =====================================================
    // 3. KHỞI TẠO
    // =====================================================

    public KhungNgheThuyetMinh(BaseActivity activity) {

        this.activity = activity;

        audio = (AudioManager) activity.getSystemService(
                Context.AUDIO_SERVICE
        );

        tienDo = new TienDoNghe(activity);

        if (Build.VERSION.SDK_INT >= 26) {

            focus = new AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN
            )
                    .setAudioAttributes(attrs)
                    .setOnAudioFocusChangeListener(
                            doiFocus,
                            main
                    )
                    .build();
        }

        ContextCompat.registerReceiver(
                activity,
                rutTaiNghe,
                new IntentFilter(
                        AudioManager.ACTION_AUDIO_BECOMING_NOISY
                ),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        view = activity.getLayoutInflater().inflate(
                R.layout.sheet_nghe_thuyet_minh,
                null
        );

        dialog = new BottomSheetDialog(
                activity,
                R.style.ThemeOverlay_APP_CK_Player
        );

        dialog.setContentView(view);

        // Ánh xạ giao diện

        tua = view.findViewById(R.id.ngheThanhTua);

        nutPhat = view.findViewById(
                R.id.nghePhatTamDung
        );

        nutLui = view.findViewById(R.id.ngheLui10);

        nutToi = view.findViewById(R.id.ngheToi10);

        nutTocDo = view.findViewById(R.id.ngheTocDo);

        nutNgheTiep = view.findViewById(R.id.ngheTiep);

        giaoDien = new GiaoDienKhungNghe(activity, dialog, view);

        giaoDien.cauHinhDialog(() -> { luuTienDo(); donLuot(); });

        ganSuKien();

        resetDongHo();
    }

    // =====================================================
    // 5. SỰ KIỆN GIAO DIỆN
    // =====================================================

    private void ganSuKien() {
        // Mở nội dung thuyết minh
        view.findViewById(R.id.ngheDocNoiDung)
                .setOnClickListener(v -> { if (!daHuy) giaoDien.moNoiDungThuyetMinh(); });
        // Đóng trình nghe

        view.findViewById(R.id.ngheDong)
                .setOnClickListener(v -> dong());

        // Giữ sự kiện nguồn thông tin để tương thích
        // với cấu trúc cũ, dù hiện tại đã ẩn trong XML.

        view.findViewById(R.id.ngheNguonThongTin)
                .setOnClickListener(v ->
                        activity.moNguonThongTin()
                );

        // Phát / Tạm dừng

        nutPhat.setOnClickListener(v -> {

            if (dangPhat()) {

                tamDung("Đã tạm dừng");

            } else {

                phat();
            }
        });

        // Dừng

        view.findViewById(R.id.ngheDung)
                .setOnClickListener(v -> dung());

        // Lùi 10 giây

        nutLui.setOnClickListener(v ->
                tuaTuongDoi(-10000)
        );

        // Tới 10 giây

        nutToi.setOnClickListener(v ->
                tuaTuongDoi(10000)
        );

        // Điều chỉnh tốc độ

        nutTocDo.setOnClickListener(v ->
                moMenuTocDo()
        );

        // Tiếp tục từ vị trí đã lưu

        nutNgheTiep.setOnClickListener(v ->
                ngheTuViTriDaLuu()
        );

        // Giữ ID văn bản cũ để tương thích

        TextView bai = view.findViewById(
                R.id.ngheNoiDung
        );

        TextView moRong = view.findViewById(
                R.id.ngheMoRong
        );

        moRong.setOnClickListener(v -> {

            boolean dangRutGon =
                    bai.getMaxLines() == 3;

            bai.setMaxLines(
                    dangRutGon
                            ? Integer.MAX_VALUE
                            : 3
            );

            bai.setEllipsize(
                    dangRutGon
                            ? null
                            : TextUtils.TruncateAt.END
            );

            moRong.setText(
                    dangRutGon
                            ? "Thu gọn"
                            : "Đọc thêm"
            );
        });

        ganSuKienThanhTua();
    }

    // =====================================================
    // 6. XỬ LÝ THANH TUA
    // =====================================================

    private void ganSuKienThanhTua() {

        tua.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar s
                    ) {

                        if (!s.isEnabled()) return;

                        capNhatTienTrinh();

                        tiepSauTua = dangPhat();

                        dangTua = true;

                        if (tiepSauTua && player != null) {

                            try {

                                player.pause();

                            } catch (RuntimeException e) {

                                loi("Không thể tạm dừng âm thanh.");
                                return;
                            }
                        }

                        main.removeCallbacks(nhip);

                        giaoDien.dungSongAm();

                        nutPhat.setEnabled(false);
                        nutLui.setEnabled(false);
                        nutToi.setEnabled(false);
                    }

                    @Override
                    public void onProgressChanged(
                            SeekBar s,
                            int value,
                            boolean user
                    ) {

                        if (!user || tong <= 0) return;

                        int position = (int) (
                                (long) value * tong / 1000
                        );

                        chu(
                                R.id.ngheDaPhat,
                                thoiGian(position)
                        );

                        // Hỗ trợ điều khiển bằng bàn phím
                        // hoặc công cụ trợ năng.

                        if (!dangTua && s.isEnabled()) {

                            taiTai(position, dangPhat());
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar s
                    ) {

                        if (!dangTua) return;

                        dangTua = false;

                        int position = (int) (
                                (long) s.getProgress()
                                        * tong / 1000
                        );

                        taiTai(
                                position,
                                tiepSauTua
                        );
                    }
                }
        );
    }

    // =====================================================
    // 7. MỞ TRÌNH NGHE
    // =====================================================

    public void mo(
            String ten,
            String bai,
            String diaDiem,
            Drawable anh,
            boolean phatNgay,
            int idDiaDiem,
            int idDiem
    ) {

        mo(
                ten,
                bai,
                diaDiem,
                anh,
                phatNgay,
                idDiaDiem,
                idDiem,
                KhoLichSu.THU_CONG
        );
    }

    public void mo(
            String ten,
            String bai,
            String diaDiem,
            Drawable anh,
            boolean phatNgay,
            int idDiaDiem,
            int idDiem,
            String cheDo
    ) {

        if (daHuy
                || activity.isFinishing()
                || activity.isDestroyed()) {

            return;
        }

        // Đóng bài cũ trước khi mở bài mới

        dong();

        this.idDiaDiem = idDiaDiem;
        this.idDiem = idDiem;

        this.cheDoLichSu = cheDo == null
                ? KhoLichSu.THU_CONG
                : cheDo;

        String tenAnToan = ten == null
                ? ""
                : ten.trim();

        String baiAnToan = bai == null
                ? ""
                : bai;

        noiDung = baiAnToan.trim().isEmpty()
                ? ""
                : tenAnToan + ". " + baiAnToan;

        // Hiển thị tiêu đề

        chu(
                R.id.ngheTenDiem,
                tenAnToan
        );

        // Văn bản được giữ lại cho TTS và kiểm thử,
        // nhưng không hiện trên giao diện mới.

        chu(
                R.id.ngheNoiDung,
                baiAnToan
        );

        TextView vanBan = view.findViewById(
                R.id.ngheNoiDung
        );

        vanBan.setMaxLines(3);

        vanBan.setEllipsize(
                TextUtils.TruncateAt.END
        );

        view.findViewById(R.id.ngheMoRong)
                .setVisibility(View.GONE);

        // Ưu tiên ảnh riêng của điểm thành phần

        boolean coAnhDiem = false;

        if (idDiaDiem >= 0 && idDiem > 0) {

            try (AppDatabase helper =
                         new AppDatabase(activity)) {

                DiemThamQuan diem =
                        KhoDuLieuThamQuan.lay(
                                helper.getReadableDatabase(),
                                idDiaDiem,
                                idDiem
                        );

                Bitmap bitmap = diem == null
                        ? null
                        : AnhDiaDiem.doc(
                        activity,
                        diem.tenAnh
                );

                if (bitmap != null) {

                    anh = new BitmapDrawable(
                            activity.getResources(),
                            bitmap
                    );

                    coAnhDiem = true;
                }

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Không đọc được ảnh điểm tham quan",
                        e
                );
            }
        }

        // Không hiển thị tên địa điểm hai lần

        capNhatTenDiaDiem(
                tenAnToan,
                diaDiem,
                coAnhDiem
        );

        // Hiển thị ảnh

        ImageView image = view.findViewById(
                R.id.ngheAnh
        );

        if (anh == null) {

            image.setScaleType(
                    ImageView.ScaleType.CENTER_INSIDE
            );

            image.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

        } else {

            image.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            Drawable.ConstantState state =
                    anh.getConstantState();

            image.setImageDrawable(
                    state == null
                            ? anh
                            : state.newDrawable().mutate()
            );
        }

        image.setContentDescription(
                "Hình ảnh " + tenAnToan
        );

        resetDongHo();

        lanLuuCuoi = 0;

        docTienDoDaLuu();

        capNhatTocDo();

        trangThai("Sẵn sàng nghe thuyết minh");

        dialog.show();

        // Nếu đã có tiến độ nghe thủ công,
        // ưu tiên cho người dùng tự lựa chọn.

        if (tienDoCu >= 3000) {

            trangThai(
                    "Chọn nghe tiếp hoặc nghe từ đầu"
            );

        } else if (phatNgay) {

            phat();
        }
    }

    // =====================================================
    // 8. HIỂN THỊ TÊN ĐỊA ĐIỂM
    // =====================================================

    private void capNhatTenDiaDiem(
            String ten,
            String diaDiem,
            boolean coAnhDiem
    ) {

        TextView diaDiemPhu = view.findViewById(
                R.id.ngheDiaDiem
        );

        String tenDiaDiem = diaDiem == null
                ? ""
                : diaDiem.trim();

        boolean laTongQuan =
                idDiem == 0
                        || TextUtils.equals(
                        ten.trim(),
                        tenDiaDiem
                );

        if (laTongQuan) {

            diaDiemPhu.setText(
                    "Thuyết minh tổng quan"
            );

            diaDiemPhu.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ui_ic_audio,
                    0,
                    0,
                    0
            );

        } else {

            String noiDungPhu;

            if (tenDiaDiem.isEmpty()) {

                noiDungPhu = "Điểm tham quan";

            } else {

                noiDungPhu = tenDiaDiem;
            }

            if (!coAnhDiem) {

                noiDungPhu += " · Ảnh tổng quan";
            }

            diaDiemPhu.setText(noiDungPhu);

            diaDiemPhu.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ui_ct_location,
                    0,
                    0,
                    0
            );
        }
    }

    // =====================================================
    // 9. PHÁT THUYẾT MINH
    // =====================================================

    private void phat() {

        if (daHuy
                || !dialog.isShowing()
                || dangTao
                || muonPhat
                || dangTua) {

            return;
        }

        if (TextUtils.isEmpty(noiDung)) {

            trangThai("Chưa có nội dung thuyết minh.");

            return;
        }

        // Nếu đã nghe hết, phát lại từ đầu

        if (daNgheHet) {

            daNgheHet = false;

            viTriDung = 0;

            coLuotDaPhat = false;

            luotLichSu = null;
        }

        if (luotLichSu == null) {

            luotLichSu = new LuotNgheLichSu(
                    activity,
                    idDiaDiem,
                    idDiem,
                    cheDoLichSu
            );
        }

        // Nếu đã có file âm thanh

        if (!durations.isEmpty()) {

            if (daChuanBi && player != null) {

                batAmThanh();

            } else if (player == null) {

                taiTai(viTriDung, true);
            }

            return;
        }

        // Nếu chưa có âm thanh thì tạo từ TTS

        muonPhat = true;

        phatSauTao = true;

        nutPhat.setEnabled(false);

        capNhatNutTua(false);

        view.findViewById(R.id.ngheChuanBi)
                .setVisibility(View.VISIBLE);

        trangThai("Đang chuẩn bị giọng đọc tiếng Việt…");

        main.removeCallbacks(quaHan);

        main.postDelayed(
                quaHan,
                THOI_GIAN_CHO
        );

        if (tts == null || ttsLoi) {

            khoiTaoTts();

        } else if (ttsReady) {

            taoAmThanh();
        }
    }

    // =====================================================
    // 10. KHỞI TẠO TEXT TO SPEECH
    // =====================================================

    private void khoiTaoTts() {

        if (tts != null) {

            tts.shutdown();

            tts = null;
        }

        ttsReady = false;

        ttsLoi = false;

        final int token = ++theHeTts;

        tts = new TextToSpeech(
                activity,
                status -> main.post(() -> {

                    if (daHuy
                            || tts == null
                            || token != theHeTts) {

                        return;
                    }

                    ttsReady =
                            status == TextToSpeech.SUCCESS;

                    if (ttsReady) {

                        int language = tts.setLanguage(
                                new Locale("vi", "VN")
                        );

                        ttsReady =
                                language != TextToSpeech.LANG_MISSING_DATA
                                        && language
                                        != TextToSpeech.LANG_NOT_SUPPORTED;

                        if (ttsReady) {

                            chonGiongTiengViet();
                        }
                    }

                    ttsLoi = !ttsReady;

                    tts.setOnUtteranceProgressListener(
                            new UtteranceProgressListener() {

                                @Override
                                public void onStart(String id) {
                                }

                                @Override
                                public void onDone(String id) {

                                    main.post(() ->
                                            xongTep(id, true)
                                    );
                                }

                                @Override
                                public void onError(String id) {

                                    main.post(() ->
                                            xongTep(id, false)
                                    );
                                }

                                @Override
                                public void onError(
                                        String id,
                                        int code
                                ) {

                                    main.post(() ->
                                            xongTep(id, false)
                                    );
                                }
                            }
                    );

                    if (muonPhat && dialog.isShowing()) {

                        if (ttsReady) {

                            taoAmThanh();

                        } else {

                            loi(
                                    "Chưa có giọng đọc tiếng Việt. "
                                            + "Kiểm tra cài đặt giọng đọc."
                            );
                        }
                    }
                })
        );
    }

    // =====================================================
    // 11. ƯU TIÊN GIỌNG ĐỌC TIẾNG VIỆT CỤC BỘ
    // =====================================================

    private void chonGiongTiengViet() {

        if (tts == null) return;

        android.speech.tts.Voice hienTai =
                tts.getVoice();

        if (hienTai != null
                && !hienTai.isNetworkConnectionRequired()) {

            return;
        }

        if (tts.getVoices() == null) return;

        for (android.speech.tts.Voice voice
                : tts.getVoices()) {

            boolean laTiengViet =
                    "vi".equals(
                            voice.getLocale().getLanguage()
                    );

            boolean khongCanMang =
                    !voice.isNetworkConnectionRequired();

            boolean daCai =
                    voice.getFeatures() == null
                            || !voice.getFeatures().contains(
                            TextToSpeech.Engine
                                    .KEY_FEATURE_NOT_INSTALLED
                    );

            if (laTiengViet && khongCanMang && daCai) {

                if (tts.setVoice(voice)
                        == TextToSpeech.SUCCESS) {

                    break;
                }
            }
        }
    }

    // =====================================================
    // 12. TỔNG HỢP ÂM THANH
    // =====================================================

    private void taoAmThanh() {

        if (dangTao
                || !muonPhat
                || !ttsReady
                || tts == null) {

            return;
        }

        dangTao = true;

        chunks = DoanAmThanh.chia(
                noiDung,
                Math.min(
                        3000,
                        TextToSpeech.getMaxSpeechInputLength()
                )
        );

        doan = 0;

        nutPhat.setEnabled(false);

        view.findViewById(R.id.ngheChuanBi)
                .setVisibility(View.VISIBLE);

        taoDoan();
    }

    private void taoDoan() {

        if (!dangTao || daHuy) return;

        if (doan == chunks.size()) {

            docThoiLuong();

            return;
        }

        trangThai(
                "Đang chuẩn bị âm thanh · "
                        + (doan + 1)
                        + "/"
                        + chunks.size()
        );

        try {

            File file = File.createTempFile(
                    "thuyet_minh_",
                    ".wav",
                    activity.getCacheDir()
            );

            files.add(file);

            luotTao = file.getAbsolutePath();

            dangGhi.put(luotTao, file);

            int ketQua = tts.synthesizeToFile(
                    chunks.get(doan),
                    null,
                    file,
                    luotTao
            );

            if (ketQua != TextToSpeech.SUCCESS) {

                dangGhi.remove(luotTao);

                loi(
                        "Không tạo được âm thanh. Vui lòng thử lại."
                );
            }

        } catch (Exception e) {

            Log.e(TAG, "Lỗi tổng hợp âm thanh", e);

            loi("Không tạo được tệp âm thanh tạm.");
        }
    }

    private void xongTep(
            String id,
            boolean ok
    ) {

        if (id == null) return;

        File file = dangGhi.remove(id);

        // Bỏ qua phản hồi của lượt đã hủy

        if (daHuy
                || !id.equals(luotTao)
                || !dangTao) {

            if (file != null && !files.contains(file)) {

                file.delete();
            }

            return;
        }

        luotTao = null;

        main.removeCallbacks(quaHan);

        main.postDelayed(
                quaHan,
                THOI_GIAN_CHO
        );

        if (!ok) {

            loi(
                    "Không tổng hợp được giọng đọc. Vui lòng thử lại."
            );

            return;
        }

        doan++;

        taoDoan();
    }

    // =====================================================
    // 13. ĐỌC THỜI LƯỢNG ÂM THANH THẬT
    // =====================================================

    private void docThoiLuong() {

        final int token = phien;

        List<File> banSao =
                new ArrayList<>(files);

        nen.execute(() -> {

            List<Integer> ketQua =
                    new ArrayList<>();

            try {

                for (File file : banSao) {

                    MediaMetadataRetriever reader =
                            new MediaMetadataRetriever();

                    try {

                        reader.setDataSource(
                                file.getAbsolutePath()
                        );

                        String metadata =
                                reader.extractMetadata(
                                        MediaMetadataRetriever
                                                .METADATA_KEY_DURATION
                                );

                        int ms = Integer.parseInt(metadata);

                        if (ms <= 0) {

                            throw new IllegalStateException(
                                    "Tệp không có thời lượng"
                            );
                        }

                        ketQua.add(ms);

                    } finally {

                        reader.release();
                    }
                }

                main.post(() -> {

                    if (token != phien
                            || daHuy
                            || !dialog.isShowing()) {

                        return;
                    }

                    durations.clear();

                    durations.addAll(ketQua);

                    tong = 0;

                    for (int ms : durations) {

                        tong += ms;
                    }

                    if (tong <= 0) {

                        loi(
                                "Không đọc được thời lượng âm thanh."
                        );

                        return;
                    }

                    dangTao = false;

                    muonPhat = false;

                    main.removeCallbacks(quaHan);

                    view.findViewById(R.id.ngheChuanBi)
                            .setVisibility(View.GONE);

                    chu(
                            R.id.ngheTongThoiGian,
                            thoiGian(tong)
                    );

                    int position = gioiHanViTri(
                            viTriYeuCau
                    );

                    viTriYeuCau = 0;

                    taiTai(position, phatSauTao);
                });

            } catch (Exception e) {

                Log.e(TAG, "Lỗi đọc thời lượng", e);

                main.post(() -> {

                    if (token == phien && !daHuy) {

                        loi("Không mở được tệp âm thanh.");
                    }
                });
            }
        });
    }

    // =====================================================
    // 14. TẢI VÀ TUA ÂM THANH
    // =====================================================

    private void taiTai(
            int position,
            boolean play
    ) {

        if (daHuy
                || !dialog.isShowing()
                || durations.isEmpty()
                || tong <= 0) {

            return;
        }

        thaPlayer();

        position = gioiHanViTri(position);

        daNgheHet = false;

        // Xác định đoạn âm thanh tương ứng
        // với thời gian trong toàn bộ bài.

        int[] target = DoanAmThanh.tim(
                durations,
                position
        );

        doan = target[0];

        nenDoan = 0;

        for (int i = 0; i < doan; i++) {

            nenDoan += durations.get(i);
        }

        viTriDung = position;

        phatSauTai = play;

        capNhatViTriHienThi(position);

        capNhatNutTua(false);

        nutPhat.setEnabled(false);

        trangThai("Đang chuẩn bị phát…");

        int token = phien;

        try {

            MediaPlayer moi = new MediaPlayer();

            player = moi;

            moi.setAudioAttributes(attrs);

            moi.setDataSource(
                    files.get(doan).getAbsolutePath()
            );

            moi.setOnPreparedListener(p -> {

                if (daHuy
                        || token != phien
                        || player != p) {

                    return;
                }

                p.setOnSeekCompleteListener(done -> {

                    if (player != done
                            || token != phien
                            || daHuy) {

                        return;
                    }

                    capNhatSanSang(phatSauTai);
                });

                if (target[1] > 0) {

                    try {

                        p.seekTo(target[1]);

                    } catch (RuntimeException e) {

                        loi("Không thể tua âm thanh.");
                    }

                } else {

                    capNhatSanSang(phatSauTai);
                }
            });

            moi.setOnCompletionListener(p -> {

                if (player != p
                        || token != phien
                        || daHuy
                        || dangTua) {

                    return;
                }

                // Nếu còn đoạn tiếp theo

                if (doan + 1 < files.size()) {

                    taiTai(
                            nenDoan + durations.get(doan),
                            true
                    );

                } else {

                    hoanThanhBaiNghe();
                }
            });

            moi.setOnErrorListener((p, what, extra) -> {

                if (player == p
                        && token == phien
                        && !daHuy) {

                    loi(
                            "Không phát được âm thanh. Vui lòng thử lại."
                    );
                }

                return true;
            });

            moi.prepareAsync();

        } catch (Exception e) {

            Log.e(TAG, "Lỗi MediaPlayer", e);

            loi("Không mở được bài thuyết minh.");
        }
    }

    private void capNhatSanSang(boolean play) {

        daChuanBi = true;

        nutPhat.setEnabled(true);

        capNhatNutTua(true);

        if (play) {

            batAmThanh();

        } else {

            boFocus();

            capNhatNutPhat(
                    R.drawable.ui_nghe_play,
                    "Tiếp tục"
            );

            trangThai("Đã tạm dừng");
        }

        capNhatTienTrinh();
    }

    // =====================================================
    // 15. BẮT ĐẦU PHÁT
    // =====================================================

    private void batAmThanh() {

        if (player == null
                || !daChuanBi
                || !dialog.isShowing()
                || daHuy) {

            return;
        }

        if (!xinFocus()) {

            capNhatNutPhat(
                    R.drawable.ui_nghe_play,
                    "Tiếp tục"
            );

            trangThai(
                    "Chưa thể phát lúc này. Vui lòng thử lại."
            );

            return;
        }

        try {

            player.start();

            // Điều chỉnh tốc độ sau khi bắt đầu phát

            if (!apDungTocDo()) {

                return;
            }

            if (luotLichSu == null) {

                luotLichSu = new LuotNgheLichSu(
                        activity,
                        idDiaDiem,
                        idDiem,
                        cheDoLichSu
                );
            }

            // Chỉ ghi lịch sử khi âm thanh bắt đầu.
            // Không tạo lượt trùng khi tiếp tục hoặc tua.

            luotLichSu.batDau(activity);

            coLuotDaPhat = true;

            daNgheHet = false;

            tienDoCu = 0;

            nutNgheTiep.setVisibility(View.GONE);

            capNhatNutPhat(
                    R.drawable.ui_nghe_pause,
                    "Tạm dừng"
            );

            trangThai("Đang phát thuyết minh");

            giaoDien.batSongAm();

            main.removeCallbacks(nhip);

            main.post(nhip);

        } catch (RuntimeException e) {

            Log.e(TAG, "Không phát được âm thanh", e);

            loi("Không thể phát âm thanh.");
        }
    }

    // =====================================================
    // 16. TẠM DỪNG
    // =====================================================

    private boolean dangPhat() {

        if (!daChuanBi || player == null) {

            return false;
        }

        try {

            return player.isPlaying();

        } catch (IllegalStateException e) {

            return false;
        }
    }

    private void tamDung(String lyDo) {

        if (!dangPhat()) return;

        try {

            player.pause();

        } catch (RuntimeException e) {

            loi("Không thể tạm dừng âm thanh.");

            return;
        }

        main.removeCallbacks(nhip);

        boFocus();

        giaoDien.dungSongAm();

        capNhatTienTrinh();

        luuTienDo();

        capNhatNutPhat(
                R.drawable.ui_nghe_play,
                "Tiếp tục"
        );

        trangThai(lyDo);
    }

    // Tạm dừng khi xảy ra sự kiện bên ngoài

    private void tamDungNgoai(String lyDo) {

        if (daHuy || !dialog.isShowing()) return;

        tiepSauTua = false;

        phatSauTai = false;

        phatSauTao = false;

        if (dangPhat()) {

            tamDung(lyDo);

        } else {

            boFocus();

            giaoDien.dungSongAm();
        }
    }

    // =====================================================
    // 17. TUA TỚI / LÙI 10 GIÂY
    // =====================================================

    private void tuaTuongDoi(int thayDoi) {

        if (tong <= 0
                || durations.isEmpty()
                || !tua.isEnabled()
                || dangTua) {

            return;
        }

        boolean play = dangPhat();

        capNhatTienTrinh();

        int hienTai = daNgheHet
                ? tong
                : viTriDung;

        int moi = gioiHanViTri(
                hienTai + thayDoi
        );

        taiTai(moi, play);
    }

    private int gioiHanViTri(int position) {

        return Math.max(
                0,
                Math.min(
                        position,
                        Math.max(0, tong - 1)
                )
        );
    }

    private void capNhatNutTua(boolean enabled) {

        tua.setEnabled(enabled);

        nutLui.setEnabled(enabled);

        nutToi.setEnabled(enabled);

        nutLui.setAlpha(
                enabled ? 1f : 0.35f
        );

        nutToi.setAlpha(
                enabled ? 1f : 0.35f
        );
    }

    // =====================================================
    // 18. ĐIỀU CHỈNH TỐC ĐỘ PHÁT
    // =====================================================

    private void moMenuTocDo() {

        PopupMenu menu = new PopupMenu(
                activity,
                nutTocDo
        );

        for (int i = 0; i < TEN_TOC_DO.length; i++) {

            menu.getMenu().add(
                            1,
                            i,
                            i,
                            TEN_TOC_DO[i]
                    )
                    .setCheckable(true)
                    .setChecked(i == mucTocDo);
        }

        menu.getMenu().setGroupCheckable(
                1,
                true,
                true
        );

        menu.setOnMenuItemClickListener(item -> {

            int index = item.getItemId();

            if (index < 0
                    || index >= CAC_TOC_DO.length) {

                return false;
            }

            mucTocDo = index;

            capNhatTocDo();

            // Không tự phát khi người dùng
            // thay đổi tốc độ lúc đang tạm dừng.

            if (dangPhat()) {

                apDungTocDo();
            }

            return true;
        });

        menu.show();
    }

    private void capNhatTocDo() {

        nutTocDo.setText(
                TEN_TOC_DO[mucTocDo]
        );

        nutTocDo.setContentDescription(
                "Tốc độ phát " + TEN_TOC_DO[mucTocDo]
        );
    }

    private boolean apDungTocDo() {

        if (player == null || !daChuanBi) {

            return false;
        }

        try {

            PlaybackParams params =
                    player.getPlaybackParams();

            params.setSpeed(
                    CAC_TOC_DO[mucTocDo]
            );

            params.setPitch(1f);

            player.setPlaybackParams(params);

            return true;

        } catch (RuntimeException e) {

            // Quay về tốc độ thường nếu thiết bị
            // không hỗ trợ tốc độ đang chọn.

            mucTocDo = 1;

            capNhatTocDo();

            try {

                player.setPlaybackParams(
                        new PlaybackParams()
                                .allowDefaults()
                                .setSpeed(1f)
                                .setPitch(1f)
                );

                Toast.makeText(
                        activity,
                        "Thiết bị chưa hỗ trợ tốc độ này.",
                        Toast.LENGTH_SHORT
                ).show();

                return true;

            } catch (RuntimeException ex) {

                loi(
                        "Không thể điều chỉnh tốc độ phát."
                );

                return false;
            }
        }
    }

    // =====================================================
    // 19. CẬP NHẬT TIẾN TRÌNH
    // =====================================================

    private void capNhatNhip() {

        if (daHuy || !dangPhat()) return;

        capNhatTienTrinh();

        long hienTai =
                SystemClock.elapsedRealtime();

        // Lưu tiến độ mỗi 5 giây

        if (hienTai - lanLuuCuoi >= CHU_KY_LUU) {

            luuTienDo();

            lanLuuCuoi = hienTai;
        }

        if (dangPhat()) {

            main.postDelayed(
                    nhip,
                    CHU_KY_CAP_NHAT
            );
        }
    }

    private void capNhatTienTrinh() {

        if (!daChuanBi
                || player == null
                || tong <= 0
                || dangTua) {

            return;
        }

        try {

            viTriDung = Math.max(
                    0,
                    Math.min(
                            tong,
                            nenDoan
                                    + player.getCurrentPosition()
                    )
            );

            capNhatViTriHienThi(viTriDung);

        } catch (IllegalStateException e) {

            Log.w(
                    TAG,
                    "Chưa đọc được vị trí âm thanh",
                    e
            );
        }
    }

    private void capNhatViTriHienThi(
            int position
    ) {

        int progress = tong <= 0
                ? 0
                : (int) (
                (long) position * 1000 / tong
        );

        tua.setProgress(progress);

        chu(
                R.id.ngheDaPhat,
                thoiGian(position)
        );
    }

    // =====================================================
    // 20. ĐỌC TIẾN ĐỘ ĐÃ LƯU
    // =====================================================

    private void docTienDoDaLuu() {
        tienDoCu = tienDo.mo(idDiaDiem, idDiem, cheDoLichSu, noiDung);
        capNhatNutNgheTiep();
    }

    private void luuTienDo() {
        if (!tienDo.dungPhien() || !coLuotDaPhat || daNgheHet) return;
        capNhatTienTrinh();
        tienDo.luu(viTriDung);
    }

    private void xoaTienDo() {
        tienDo.xoa();
        tienDoCu = 0;
        capNhatNutNgheTiep();
    }

    private void capNhatNutNgheTiep() {

        if (tienDoCu >= 3000) {

            nutNgheTiep.setText(
                    "Tiếp tục từ "
                            + thoiGian(tienDoCu)
            );

            nutNgheTiep.setVisibility(
                    View.VISIBLE
            );

        } else {

            nutNgheTiep.setVisibility(
                    View.GONE
            );
        }
    }

    // =====================================================
    // 22. TIẾP TỤC TỪ VỊ TRÍ ĐÃ LƯU
    // =====================================================

    private void ngheTuViTriDaLuu() {

        if (!tienDo.dungPhien() || tienDoCu <= 0) {

            return;
        }

        int position = tienDoCu;

        tienDoCu = 0;

        capNhatNutNgheTiep();

        if (!durations.isEmpty()) {

            // Nếu nội dung đã được tổng hợp
            // thì phát ngay tại vị trí đã lưu.

            if (luotLichSu == null) {

                luotLichSu = new LuotNgheLichSu(
                        activity,
                        idDiaDiem,
                        idDiem,
                        cheDoLichSu
                );
            }

            taiTai(position, true);

        } else {

            // Nếu chưa có âm thanh,
            // tạo file trước rồi tua đến vị trí đã lưu.

            viTriYeuCau = position;

            if (dangTao || muonPhat) {

                phatSauTao = true;

            } else {

                phat();
            }
        }
    }

    // =====================================================
    // 24. KHI NGHE HẾT BÀI
    // =====================================================

    private void hoanThanhBaiNghe() {

        daNgheHet = true;

        coLuotDaPhat = false;

        viTriDung = tong;

        thaPlayer();

        boFocus();

        xoaTienDo();

        tua.setProgress(1000);

        chu(
                R.id.ngheDaPhat,
                thoiGian(tong)
        );

        nutPhat.setEnabled(true);

        capNhatNutTua(true);

        // Chuyển icon sang nghe lại

        capNhatNutPhat(
                R.drawable.ui_nghe_replay,
                "Nghe lại"
        );

        trangThai(
                "Đã nghe hết bài thuyết minh"
        );

        luotLichSu = null;
    }

    // =====================================================
    // 25. DỪNG THUYẾT MINH
    // =====================================================

    private void dung() {

        coLuotDaPhat = false;

        luotLichSu = null;

        muonPhat = false;

        dangTua = false;

        tiepSauTua = false;

        phatSauTai = false;

        phatSauTao = false;

        viTriYeuCau = 0;

        xoaTienDo();

        if (dangTao || durations.isEmpty()) {

            donLuot();

        } else {

            thaPlayer();

            boFocus();
        }

        resetDongHo();

        capNhatNutTua(
                !durations.isEmpty()
        );

        trangThai(
                "Đã dừng · Nghe lại từ đầu"
        );
    }

    // =====================================================
    // 26. ĐẶT LẠI TRẠNG THÁI
    // =====================================================

    private void resetDongHo() {

        viTriDung = 0;

        viTriYeuCau = 0;

        daNgheHet = false;

        dangTua = false;

        tua.setProgress(0);

        capNhatNutTua(false);

        chu(
                R.id.ngheDaPhat,
                "00:00"
        );

        chu(
                R.id.ngheTongThoiGian,
                tong > 0
                        ? thoiGian(tong)
                        : "--:--"
        );

        capNhatNutPhat(
                R.drawable.ui_nghe_play,
                "Phát thuyết minh"
        );

        nutPhat.setEnabled(true);

        view.findViewById(R.id.ngheChuanBi)
                .setVisibility(View.GONE);

        giaoDien.dungSongAm();
    }

    // =====================================================
    // 27. QUẢN LÝ AUDIO FOCUS
    // =====================================================

    private boolean xinFocus() {

        if (giuFocus) return true;

        if (audio == null) return false;

        int result;

        if (Build.VERSION.SDK_INT >= 26) {

            result = audio.requestAudioFocus(focus);

        } else {

            result = audio.requestAudioFocus(
                    doiFocus,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );
        }

        giuFocus =
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        return giuFocus;
    }

    private void boFocus() {

        if (!giuFocus || audio == null) {

            return;
        }

        giuFocus = false;

        if (Build.VERSION.SDK_INT >= 26) {

            audio.abandonAudioFocusRequest(focus);

        } else {

            audio.abandonAudioFocus(doiFocus);
        }
    }

    // =====================================================
    // 28. XỬ LÝ QUÁ HẠN VÀ LỖI
    // =====================================================

    private void kiemTraQuaHan() {

        if ((muonPhat || dangTao)
                && dialog.isShowing()) {

            loi(
                    "Chuẩn bị âm thanh quá lâu. "
                            + "Kiểm tra giọng đọc rồi thử lại."
            );
        }
    }

    private void loi(String text) {

        luuTienDo();

        boolean canKhoiTaoLai =
                tts != null && !ttsReady;

        donLuot();

        if (canKhoiTaoLai) {

            theHeTts++;

            tts.shutdown();

            tts = null;

            ttsLoi = false;

            ttsReady = false;
        }

        resetDongHo();

        docTienDoDaLuu();

        trangThai(text);
    }

    // =====================================================
    // 29. GIẢI PHÓNG MEDIAPLAYER
    // =====================================================

    private void thaPlayer() {

        main.removeCallbacks(nhip);

        giaoDien.dungSongAm();

        daChuanBi = false;

        if (player != null) {

            MediaPlayer cu = player;

            player = null;

            cu.release();
        }
    }

    // =====================================================
    // 30. DỌN DỮ LIỆU LƯỢT NGHE
    // =====================================================

    private void donLuot() {

        luotLichSu = null;

        main.removeCallbacks(quaHan);

        phien++;

        muonPhat = false;

        dangTao = false;

        dangTua = false;

        phatSauTai = false;

        phatSauTao = false;

        tiepSauTua = false;

        coLuotDaPhat = false;

        luotTao = null;

        viTriYeuCau = 0;

        if (tts != null) {

            tts.stop();
        }

        thaPlayer();

        boFocus();

        // Xóa các file âm thanh tạm

        for (File file : files) {

            file.delete();
        }

        files.clear();

        durations.clear();

        chunks = null;

        tong = 0;

        doan = 0;

        nenDoan = 0;

        viTriDung = 0;
    }

    // =====================================================
    // 31. ĐÓNG TRÌNH NGHE
    // =====================================================

    public void dong() {

        if (daHuy) return;

        giaoDien.dongNoiDungThuyetMinh();

        luuTienDo();

        donLuot();

        if (dialog.isShowing()) {

            dialog.dismiss();
        }
    }

    // =====================================================
    // 32. GIẢI PHÓNG TOÀN BỘ TÀI NGUYÊN
    // =====================================================

    public void giaiPhong() {

        if (daHuy) return;

        dong();

        daHuy = true;

        theHeTts++;

        if (tts != null) {

            tts.shutdown();

            tts = null;
        }

        for (File file : dangGhi.values()) {

            file.delete();
        }

        dangGhi.clear();

        activity.unregisterReceiver(
                rutTaiNghe
        );

        nen.shutdownNow();

        main.removeCallbacksAndMessages(null);
    }

    // =====================================================
    // 33. HÀM TIỆN ÍCH GIAO DIỆN
    // =====================================================

    private void capNhatNutPhat(
            int icon,
            String moTa
    ) {

        nutPhat.setImageResource(icon);

        nutPhat.setContentDescription(moTa);
    }

    private void chu(
            int id,
            String text
    ) {

        TextView textView =
                view.findViewById(id);

        textView.setText(text);
    }

    private void trangThai(String text) {

        chu(
                R.id.ngheTrangThai,
                text
        );
    }

    private String thoiGian(int ms) {

        int giay = Math.max(
                0,
                ms / 1000
        );

        return String.format(
                Locale.ROOT,
                "%02d:%02d",
                giay / 60,
                giay % 60
        );
    }

}
