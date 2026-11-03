package com.example.app_ck.ui.admin;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.Locale;

/** UC08.8: Quan ly noi dung thuyet minh; tai/sua/luu tren SQLite v15 hien co. */
public class QuanLyThuyetMinhActivity extends BaseActivity {
    private static final String TAG = "ADMIN_THUYET_MINH";
    private static final int GREEN = Color.rgb(44, 115, 81);
    private static final int TEXT = Color.rgb(32, 56, 42);

    private final ArrayList<DiaDiem> places = new ArrayList<>();
    private final ArrayList<DiemThamQuan> points = new ArrayList<>();
    private Spinner placeSpinner, pointSpinner;
    private EditText editor;
    private TextView subTitle, count;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean settingSelectors = false;
    private int selectedPlaceId = -1, selectedPointId = 0;
    private String loadedText = "";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!hasAdminPermission()) { finish(); return; }
        buildScreen();
        loadPlaces();
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

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private GradientDrawable bg(int fill) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill); d.setCornerRadius(dp(14)); return d;
    }

    private TextView label(String value) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextColor(TEXT); v.setTypeface(null, Typeface.BOLD);
        v.setTextSize(14); v.setPadding(0, dp(16), 0, dp(7));
        return v;
    }

    private Button button(String title, boolean primary) {
        Button b = new Button(this);
        b.setText(title); b.setAllCaps(false);
        b.setTextColor(primary ? Color.WHITE : GREEN);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                primary ? GREEN : Color.rgb(227, 240, 229)));
        return b;
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(246, 248, 245));
        setContentView(root);
        apDungKhoangTrongHeThong();

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(10), dp(18), dp(6));
        TextView back = new TextView(this);
        back.setText("‹"); back.setTextSize(34); back.setGravity(Gravity.CENTER);
        back.setTextColor(GREEN); back.setContentDescription("Quay lại");
        header.addView(back, new LinearLayout.LayoutParams(dp(44), dp(50)));
        back.setOnClickListener(v -> confirmDiscard(this::finish));
        TextView heading = new TextView(this);
        heading.setText("Quản lý nội dung thuyết minh");
        heading.setTextColor(TEXT); heading.setTextSize(19);
        heading.setTypeface(null, Typeface.BOLD);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(header);

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        root.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(20), dp(8), dp(20), dp(26));
        scroller.addView(body);

        TextView help = new TextView(this);
        help.setText("Chọn địa điểm và bài thuyết minh. Thay đổi sẽ được lưu vào SQLite và dùng khi tham quan.");
        help.setTextColor(Color.rgb(105, 124, 110)); help.setTextSize(13);
        body.addView(help);

        body.addView(label("Địa điểm du lịch"));
        placeSpinner = new Spinner(this);
        placeSpinner.setBackground(bg(Color.WHITE));
        placeSpinner.setPadding(dp(10), dp(4), dp(10), dp(4));
        body.addView(placeSpinner, new LinearLayout.LayoutParams(-1, dp(52)));

        body.addView(label("Nội dung cần chỉnh sửa"));
        pointSpinner = new Spinner(this);
        pointSpinner.setBackground(bg(Color.WHITE));
        pointSpinner.setPadding(dp(10), dp(4), dp(10), dp(4));
        body.addView(pointSpinner, new LinearLayout.LayoutParams(-1, dp(52)));

        subTitle = new TextView(this);
        subTitle.setTextSize(13); subTitle.setTextColor(GREEN);
        subTitle.setPadding(0, dp(14), 0, dp(4));
        body.addView(subTitle);

        body.addView(label("Nội dung thuyết minh"));
        editor = new EditText(this);
        editor.setTextColor(TEXT); editor.setTextSize(15);
        editor.setHint("Nhập nội dung thuyết minh...");
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editor.setMinLines(9); editor.setMaxLines(22);
        editor.setPadding(dp(14), dp(12), dp(14), dp(12));
        editor.setBackground(bg(Color.WHITE));
        body.addView(editor, new LinearLayout.LayoutParams(-1, -2));

        count = new TextView(this);
        count.setTextSize(12); count.setTextColor(Color.GRAY);
        count.setPadding(0, dp(8), 0, dp(8));
        body.addView(count);
        editor.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int f) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { updateCount(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });

        LinearLayout audio = new LinearLayout(this);
        audio.setOrientation(LinearLayout.HORIZONTAL);
        Button listen = button("▶ Nghe thử", false);
        Button stop = button("■ Dừng đọc", false);
        audio.addView(listen, new LinearLayout.LayoutParams(0, dp(52), 1));
        LinearLayout.LayoutParams stopLp = new LinearLayout.LayoutParams(0, dp(52), 1);
        stopLp.leftMargin = dp(9);
        audio.addView(stop, stopLp);
        body.addView(audio);
        listen.setOnClickListener(v -> preview());
        stop.setOnClickListener(v -> stopTts());

        Button save = button("Lưu nội dung thuyết minh", true);
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1, dp(56));
        saveLp.topMargin = dp(16); body.addView(save, saveLp);
        save.setOnClickListener(v -> saveNarration());

        TextView note = new TextView(this);
        note.setText("Bài giới thiệu địa điểm chính dùng trường mô tả của địa điểm; bài của từng Marker dùng trường nội dung thuyết minh. Nguồn tham khảo được quản lý riêng, không chèn URL vào giọng đọc.");
        note.setTextSize(12); note.setTextColor(Color.GRAY);
        note.setPadding(0, dp(16), 0, 0); body.addView(note);

        placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingSelectors || position < 0 || position >= places.size()) return;
                int nextId = places.get(position).id;
                if (nextId == selectedPlaceId) return;
                confirmDiscard(() -> { selectedPlaceId = nextId; loadPoints(); },
                        () -> restorePlaceSelection());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (settingSelectors || selectedPlaceId < 0) return;
                if (position < 0 || position > points.size()) return;
                int nextId = position == 0 ? 0 : points.get(position - 1).id;
                if (nextId == selectedPointId) return;
                confirmDiscard(() -> { selectedPointId = nextId; loadNarration(); },
                        () -> restorePointSelection());
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            placeSpinner.setAdapter(adapter);
            selectedPlaceId = places.isEmpty() ? -1 : places.get(0).id;
            settingSelectors = false;
            loadPoints();
        } catch (Exception ex) {
            Log.e(TAG, "Cannot load places", ex);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pointSpinner.setAdapter(adapter);
        selectedPointId = 0;
        settingSelectors = false;
        loadNarration();
    }

    private void loadNarration() {
        stopTts();
        String content = "";
        String name = "";
        try (AppDatabase db = new AppDatabase(this)) {
            SQLiteDatabase sql = db.getReadableDatabase();
            if (selectedPlaceId < 0) { editor.setEnabled(false); editor.setText(""); subTitle.setText("Chưa có địa điểm"); loadedText = ""; return; }
            if (selectedPointId == 0) {
                for (DiaDiem d : db.danhSachDiaDiem()) if (d.id == selectedPlaceId) {
                    content = d.moTa; name = d.ten; break;
                }
            } else {
                DiemThamQuan p = KhoDuLieuThamQuan.lay(sql, selectedPlaceId, selectedPointId);
                if (p == null) { toast("Điểm tham quan không còn tồn tại"); return; }
                content = p.noiDung; name = p.ten;
            }
            editor.setEnabled(true);
            loadedText = content == null ? "" : content;
            editor.setText(loadedText);
            editor.setSelection(editor.length());
            subTitle.setText((selectedPointId == 0 ? "Địa điểm chính: " : "Điểm thành phần: ") + name);
            updateCount();
        } catch (Exception ex) {
            Log.e(TAG, "Cannot load narration", ex);
            toast("Không tải được nội dung thuyết minh");
        }
    }

    private void updateCount() { if (count != null && editor != null) count.setText(editor.length() + " ký tự"); }
    private boolean dirty() { return editor != null && !editor.getText().toString().equals(loadedText); }
    private void confirmDiscard(Runnable proceed) { confirmDiscard(proceed, null); }
    private void confirmDiscard(Runnable proceed, Runnable cancel) {
        if (!dirty()) { proceed.run(); return; }
        new AlertDialog.Builder(this)
                .setTitle("Nội dung chưa được lưu")
                .setMessage("Bạn muốn bỏ các thay đổi để chuyển màn hình?")
                .setNegativeButton("Tiếp tục sửa", (dialog, which) -> { if (cancel != null) cancel.run(); })
                .setPositiveButton("Bỏ thay đổi", (dialog, which) -> proceed.run())
                .setOnCancelListener(dialog -> { if (cancel != null) cancel.run(); })
                .show();
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
            if (saved) { loadedText = value; editor.setText(value); toast("Đã lưu nội dung thuyết minh"); }
            else toast("Không lưu được nội dung thuyết minh");
        } catch (Exception e) { Log.e(TAG, "Cannot save narration", e); toast("Lỗi khi lưu thuyết minh"); }
    }

    private void setupTts() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) {
                ttsReady = false;
                toast("Thiết bị chưa khởi tạo được TextToSpeech"); return;
            }
            if (tts == null) return;
            int code = tts.setLanguage(new Locale("vi", "VN"));
            ttsReady = code != TextToSpeech.LANG_MISSING_DATA
                    && code != TextToSpeech.LANG_NOT_SUPPORTED;
            if (!ttsReady) toast("Thiết bị chưa có giọng đọc tiếng Việt");
        });
    }
    private void preview() {
        if (!ttsReady || tts == null) { toast("Giọng đọc tiếng Việt chưa sẵn sàng"); return; }
        String value = editor.getText().toString().trim();
        if (value.isEmpty()) { toast("Chưa có nội dung để nghe thử"); return; }
        tts.stop();
        tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "admin_preview_" + selectedPlaceId + "_" + selectedPointId);
    }
    private void stopTts() { if (tts != null) tts.stop(); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
    @Override protected void onStop() { stopTts(); super.onStop(); }
    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        super.onDestroy();
    }
}
