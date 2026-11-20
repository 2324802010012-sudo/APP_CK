package com.example.app_ck.ui.admin;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoDiaDiem;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.Locale;

/** UC08.8: Quan ly noi dung thuyet minh; tai/sua/luu tren SQLite v15 hien co. */
public class QuanLyThuyetMinhActivity extends BaseActivity {
    private static final String TAG = "ADMIN_THUYET_MINH";

    private final ArrayList<DiaDiem> places = new ArrayList<>();
    private final ArrayList<DiemThamQuan> points = new ArrayList<>();
    private Spinner placeSpinner, pointSpinner;
    private EditText editor;
    private TextView subTitle, count, saveState, audioState;
    private Button listen, stop, save;
    private String previewSession, lastUtterance;
    private long previewNumber;
    private TextToSpeech tts;
    private final java.util.concurrent.ExecutorService ttsWorker =
            java.util.concurrent.Executors.newSingleThreadExecutor();
    private boolean ttsReady = false;
    private boolean settingSelectors = false;
    private int selectedPlaceId = -1, selectedPointId = 0;
    private String loadedText = "";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!hasAdminPermission()) { finish(); return; }
        buildScreen();
        AdminGiaoDien.apDung(this);
        if (state != null) {
            selectedPlaceId = state.getInt("admin_place", -1);
            selectedPointId = state.getInt("admin_point", 0);
        }
        loadPlaces();
        if (state != null && editor.isEnabled()) {
            loadedText = state.getString("admin_loaded", loadedText);
            editor.setText(state.getString("admin_draft", loadedText));
            updateCount();
        }
        setupTts();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { confirmDiscard(() -> { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }); }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (!hasAdminPermission()) { finish(); }
    }

    private boolean hasAdminPermission() {
        try (AppDatabase db = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, db.getReadableDatabase());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Permission check failed", e);
            Toast.makeText(this, "Không có quyền quản trị", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void buildScreen() {
        setContentView(R.layout.activity_quan_ly_thuyet_minh);
        apDungKhoangTrongHeThong();
        AdminGiaoDien.hoTroBanPhim(this);
        AdminGiaoDien.header(this, "Quản lý thuyết minh", "Biên tập và nghe thử nội dung",
                () -> confirmDiscard(this::finish));
        placeSpinner = findViewById(R.id.tmDiaDiem);
        pointSpinner = findViewById(R.id.tmDiem);
        editor = findViewById(R.id.tmNoiDung);
        subTitle = findViewById(R.id.tmDoiTuong);
        count = findViewById(R.id.tmDemKyTu);
        saveState = findViewById(R.id.tmTrangThaiLuu);
        audioState = findViewById(R.id.tmTrangThaiTts);
        listen = findViewById(R.id.tmNgheThu);
        stop = findViewById(R.id.tmDung);
        save = findViewById(R.id.tmLuu);
        editor.setEnabled(false);
        save.setEnabled(false);
        editor.setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            if (action == android.view.MotionEvent.ACTION_DOWN
                    && (editor.canScrollVertically(1) || editor.canScrollVertically(-1)))
                view.getParent().requestDisallowInterceptTouchEvent(true);
            if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL)
                view.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });
        editor.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int f) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { updateCount(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        listen.setOnClickListener(v -> preview());
        stop.setOnClickListener(v -> stopTts());
        save.setOnClickListener(v -> saveNarration());
        placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingSelectors || position < 0 || position >= places.size()) return;
                int nextId = places.get(position).id;
                if (nextId == selectedPlaceId) return;
                confirmDiscard(() -> { selectedPlaceId = nextId; selectedPointId = 0; loadPoints(); },
                        QuanLyThuyetMinhActivity.this::restorePlaceSelection);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingSelectors || selectedPlaceId < 0 || position < 0 || position > points.size()) return;
                int nextId = position == 0 ? 0 : points.get(position - 1).id;
                if (nextId == selectedPointId) return;
                confirmDiscard(() -> { selectedPointId = nextId; loadNarration(); },
                        QuanLyThuyetMinhActivity.this::restorePointSelection);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void restorePlaceSelection() {
        for (int i = 0; i < places.size(); i++) if (places.get(i).id == selectedPlaceId) {
            settingSelectors = true; placeSpinner.setSelection(i); settingSelectors = false; return;
        }
    }
    private void restorePointSelection() {
        int index = 0;
        for (int i = 0; i < points.size(); i++) if (points.get(i).id == selectedPointId) index = i + 1;
        settingSelectors = true; pointSpinner.setSelection(index); settingSelectors = false;
    }

    private void loadPlaces() {
        try (AppDatabase db = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, db.getReadableDatabase());
            places.clear(); places.addAll(db.danhSachDiaDiem());
            ArrayList<String> names = new ArrayList<>();
            for (DiaDiem d : places) names.add(d.ten);
            if (places.isEmpty()) names.add("Chưa có địa điểm");
            settingSelectors = true;
            ArrayAdapter<String> adapter = AdminGiaoDien.adapter(this, names);
            placeSpinner.setAdapter(adapter);
            boolean found = false;
            for (DiaDiem place : places) if (place.id == selectedPlaceId) found = true;
            if (!found) { selectedPlaceId = places.isEmpty() ? -1 : places.get(0).id; selectedPointId = 0; }
            restorePlaceSelection();
            placeSpinner.setEnabled(!places.isEmpty());
            settingSelectors = false;
            loadPoints();
        } catch (Exception ex) {
            Log.e(TAG, "Cannot load places", ex);
            subTitle.setText("Không tải được địa điểm. Hãy quay lại và thử lại.");
            updateCount();
            toast("Không tải được danh sách địa điểm");
        }
    }

    private void loadPoints() {
        stopTts();
        points.clear();
        ArrayList<String> names = new ArrayList<>();
        names.add("Giới thiệu địa điểm chính");
        if (selectedPlaceId >= 0) {
            try (AppDatabase db = new AppDatabase(this)) {
                points.addAll(KhoDuLieuThamQuan.danhSach(db.getReadableDatabase(), selectedPlaceId));
                for (DiemThamQuan p : points) names.add("Marker " + p.id + " · " + p.ten);
            } catch (Exception ex) {
                Log.e(TAG, "Cannot load points", ex); toast("Không tải được điểm tham quan");
            }
        }
        settingSelectors = true;
        ArrayAdapter<String> adapter = AdminGiaoDien.adapter(this, names);
        pointSpinner.setAdapter(adapter);
        boolean found = selectedPointId == 0;
        for (DiemThamQuan point : points) if (point.id == selectedPointId) found = true;
        if (!found) selectedPointId = 0;
        restorePointSelection();
        pointSpinner.setEnabled(selectedPlaceId >= 0);
        settingSelectors = false;
        loadNarration();
    }

    private void loadNarration() {
        stopTts();
        String content = "";
        String name = "";
        try (AppDatabase db = new AppDatabase(this)) {
            SQLiteDatabase sql = db.getReadableDatabase();
            if (selectedPlaceId < 0) {
                loadedText = ""; editor.setEnabled(false); editor.setText("");
                subTitle.setText("Chưa có địa điểm để biên tập"); updateCount(); return;
            }
            if (selectedPointId == 0) {
                DiaDiem d = KhoDiaDiem.lay(sql, selectedPlaceId);
                if (d == null) throw new IllegalStateException("Địa điểm không còn tồn tại");
                content = d.moTa; name = d.ten;
            } else {
                DiemThamQuan p = KhoDuLieuThamQuan.lay(sql, selectedPlaceId, selectedPointId);
                if (p == null) throw new IllegalStateException("Điểm không còn tồn tại");
                content = p.noiDung; name = p.ten;
            }
            editor.setEnabled(true);
            loadedText = content == null ? "" : content;
            editor.setText(loadedText);
            editor.setSelection(0);
            editor.setError(null);
            subTitle.setText(name);
            ((TextView) findViewById(R.id.tmLoaiBai)).setText(
                    selectedPointId == 0 ? "Giới thiệu địa điểm chính" : "Điểm thành phần · Mã " + selectedPointId);
            updateCount();
        } catch (Exception ex) {
            Log.e(TAG, "Cannot load narration", ex);
            editor.setEnabled(false);
            subTitle.setText("Không tải được bài thuyết minh. Hãy chọn lại địa điểm.");
            updateCount();
            toast("Không tải được nội dung thuyết minh");
        }
    }

    private void updateCount() {
        if (count == null || editor == null) return;
        count.setText(java.text.NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(editor.length()) + " ký tự");
        saveState.setText(!editor.isEnabled() ? "Chưa có nội dung để chỉnh sửa" : dirty() ? "Chưa lưu" : "Đã lưu");
        saveState.setTextColor(getColor(dirty() ? R.color.admin_primary : R.color.admin_secondary));
        save.setEnabled(selectedPlaceId >= 0 && editor.isEnabled());
        listen.setEnabled(ttsReady && editor.isEnabled() && editor.length() > 0);
    }
    private boolean dirty() { return editor != null && !editor.getText().toString().equals(loadedText); }
    private void confirmDiscard(Runnable proceed) { confirmDiscard(proceed, null); }
    private void confirmDiscard(Runnable proceed, Runnable cancel) {
        if (!dirty()) { proceed.run(); return; }
        AdminGiaoDien.hoiBoThayDoi(this, proceed, cancel);
    }

    private void saveNarration() {
        if (!hasAdminPermission()) { finish(); return; }
        if (selectedPlaceId < 0) { toast("Chưa có địa điểm để lưu"); return; }
        String value = editor.getText().toString().trim();
        if (value.isEmpty()) { editor.setError("Nội dung không được để trống"); editor.requestFocus(); return; }
        try (AppDatabase db = new AppDatabase(this)) {
            SQLiteDatabase sql = db.getWritableDatabase();
            boolean saved = KhoDuLieuThamQuan.luuThuyetMinh(
                    this, sql, selectedPlaceId, selectedPointId, value);
            if (saved) {
                int cursor = Math.max(0, editor.getSelectionStart());
                loadedText = value;
                if (!value.equals(editor.getText().toString())) editor.setText(value);
                editor.setSelection(Math.min(cursor, editor.length()));
                updateCount();
                toast("Đã lưu nội dung thuyết minh");
            }
            else toast("Không lưu được nội dung thuyết minh");
        } catch (Exception e) { Log.e(TAG, "Cannot save narration", e); toast("Lỗi khi lưu thuyết minh"); }
    }

    private void setupTts() {
        tts = new TextToSpeech(this, status -> editor.post(() -> {
            if (isDestroyed() || tts == null) return;
            if (status != TextToSpeech.SUCCESS) {
                ttsReady = false;
                audioState.setText("Không khởi tạo được giọng đọc trên thiết bị này");
                updateCount(); return;
            }
            TextToSpeech engine = tts;
            // Gọi engine có thể chờ dịch vụ giọng đọc; không khóa luồng nhập văn bản.
            ttsWorker.execute(() -> {
                int code;
                try { code = engine.setLanguage(new Locale("vi", "VN")); }
                catch (RuntimeException e) { Log.w(TAG, "Cannot prepare Vietnamese TTS", e); code = TextToSpeech.LANG_NOT_SUPPORTED; }
                boolean ready = code != TextToSpeech.LANG_MISSING_DATA && code != TextToSpeech.LANG_NOT_SUPPORTED;
                runOnUiThread(() -> {
                    if (isDestroyed() || tts != engine) return;
                    ttsReady = ready;
                    audioState.setText(ready ? "Sẵn sàng nghe nội dung đang soạn" : "Thiết bị chưa có giọng đọc tiếng Việt");
                    updateCount();
                });
            });
        }));
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override public void onStart(String id) { showAudioState(id, "Đang phát", false); }
            @Override public void onDone(String id) {
                runOnUiThread(() -> { if (id != null && id.equals(lastUtterance)) showAudioState(id, "Đã đọc xong", true); });
            }
            @Override public void onError(String id) { showAudioState(id, "Không phát được nội dung. Hãy thử lại.", true); }
        });
    }

    private void showAudioState(String id, String message, boolean finished) {
        runOnUiThread(() -> {
            if (isDestroyed() || previewSession == null || id == null || !id.startsWith(previewSession)) return;
            audioState.setText(message);
            stop.setEnabled(!finished);
            if (finished) {
                previewSession = null; lastUtterance = null;
                if (tts != null) tts.stop();
            }
        });
    }

    private void preview() {
        if (!ttsReady || tts == null) { toast("Giọng đọc tiếng Việt chưa sẵn sàng"); return; }
        String value = editor.getText().toString().trim();
        if (value.isEmpty()) { editor.setError("Nhập nội dung để nghe thử"); editor.requestFocus(); return; }
        stopTts();
        java.util.List<String> parts = com.example.app_ck.narration.DoanAmThanh.chia(value, TextToSpeech.getMaxSpeechInputLength());
        previewSession = "admin_preview_" + (++previewNumber) + "_";
        lastUtterance = previewSession + (parts.size() - 1);
        audioState.setText("Đang chuẩn bị phát…");
        stop.setEnabled(true);
        for (int i = 0; i < parts.size(); i++) {
            int result = tts.speak(parts.get(i), i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                    null, previewSession + i);
            if (result == TextToSpeech.ERROR) {
                stopTts(); audioState.setText("Không phát được nội dung. Hãy thử lại."); return;
            }
        }
    }

    private void stopTts() {
        previewSession = null; lastUtterance = null;
        if (tts != null && ttsReady) tts.stop();
        if (stop != null) stop.setEnabled(false);
        if (audioState != null && ttsReady) audioState.setText("Đã dừng");
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
    @Override protected void onStop() { stopTts(); super.onStop(); }
    @Override protected void onDestroy() {
        TextToSpeech engine = tts;
        tts = null;
        ttsReady = false;
        if (engine != null) ttsWorker.execute(engine::shutdown);
        ttsWorker.shutdown();
        super.onDestroy();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("admin_place", selectedPlaceId);
        state.putInt("admin_point", selectedPointId);
        state.putString("admin_loaded", loadedText);
        state.putString("admin_draft", editor.getText().toString());
    }
}
