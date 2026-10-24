package com.example.app_ck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Trình nghe thủ công dùng chung: tổng hợp TTS ra cache rồi tua thời gian âm thanh thật. */
final class KhungNgheThuyetMinh {
    private final BaseActivity activity;
    private final BottomSheetDialog dialog;
    private final View view;
    private final SeekBar tua;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService nen=Executors.newSingleThreadExecutor();
    private final AudioManager audio;
    private final AudioAttributes attrs=new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
    private AudioFocusRequest focus;
    private boolean giuFocus;
    private final AudioManager.OnAudioFocusChangeListener doiFocus = change -> main.post(() -> {
        if (change<0) {
            tiepSauTua=false;
            tamDung("Đã tạm dừng vì có âm thanh khác. Bấm Tiếp tục để nghe lại.");
            boFocus();
        }
    });
    private final BroadcastReceiver rutTaiNghe=new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent intent) {
            tamDung("Đã tạm dừng khi ngắt tai nghe.");
        }
    };
    private final List<File> files=new ArrayList<>();
    private final List<Integer> durations=new ArrayList<>();
    private final Map<String,File> dangGhi=new HashMap<>();
    private List<String> chunks;
    private TextToSpeech tts;
    private MediaPlayer player;
    private boolean ttsReady, ttsLoi, dangTao, muonPhat, dangTua, tiepSauTua, daHuy, daChuanBi;
    private int phien, doan, tong, nenDoan, viTriDung;
    private String noiDung, luotTao;
    private final Runnable quaHan=this::kiemTraQuaHan;
    private void kiemTraQuaHan() {
        if(muonPhat && dialog.isShowing())loi("Chuẩn bị âm thanh quá lâu. Kiểm tra giọng đọc hoặc kết nối rồi thử lại.");
    }

    KhungNgheThuyetMinh(BaseActivity activity) {
        this.activity=activity;
        audio=(AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT>=26) focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs).setOnAudioFocusChangeListener(doiFocus,main).build();
        ContextCompat.registerReceiver(activity,rutTaiNghe,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                ContextCompat.RECEIVER_NOT_EXPORTED);
        view=activity.getLayoutInflater().inflate(R.layout.sheet_nghe_thuyet_minh,null);
        dialog=new BottomSheetDialog(activity,R.style.ThemeOverlay_APP_CK_Player);
        dialog.setContentView(view);
        dialog.setOnShowListener(d->{
            float density=activity.getResources().getDisplayMetrics().density;
            int height=Math.min(Math.round(activity.getResources().getDisplayMetrics().heightPixels*0.8f),
                    Math.round(580*density));
            view.getLayoutParams().height=height;
            view.requestLayout();
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            dialog.getBehavior().setSkipCollapsed(true);
            dialog.getBehavior().setMaxWidth(Math.round(600*density));
            dialog.getBehavior().setShouldRemoveExpandedCorners(false);
        });
        dialog.setOnDismissListener(d->{if(!dialog.isShowing())donLuot();});
        view.findViewById(R.id.ngheDong).setOnClickListener(v->dong());
        view.findViewById(R.id.nghePhatTamDung).setOnClickListener(v->{
            if (daChuanBi && player!=null && player.isPlaying()) tamDung("Đã tạm dừng"); else phat();
        });
        view.findViewById(R.id.ngheDung).setOnClickListener(v->dung());
        tua=view.findViewById(R.id.ngheThanhTua);
        tua.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar s) {
                dangTua=true; tiepSauTua=daChuanBi && player!=null && player.isPlaying();
                if(tiepSauTua)player.pause();
            }
            @Override public void onProgressChanged(SeekBar s,int value,boolean user) {
                if(user)chu(R.id.ngheDaPhat,thoiGian((int)((long)value*tong/1000)));
            }
            @Override public void onStopTrackingTouch(SeekBar s) {
                dangTua=false;
                taiTai((int)((long)s.getProgress()*tong/1000),tiepSauTua);
            }
        });
    }

    void mo(String ten,String bai,String diaDiem,Drawable anh,boolean phatNgay) {
        dong();
        noiDung=ten+". "+bai;
        chu(R.id.ngheTenDiem,ten); chu(R.id.ngheNoiDung,bai);
        chu(R.id.ngheDiaDiem,"Ảnh tổng quan · "+diaDiem);
        ImageView image=view.findViewById(R.id.ngheAnh);
        image.setImageDrawable(anh==null?null:anh.getConstantState()==null?anh:anh.getConstantState().newDrawable().mutate());
        resetDongHo();
        trangThai("Sẵn sàng nghe thuyết minh");
        dialog.show();
        if(phatNgay)phat();
    }

    private void phat() {
        if(daHuy || !dialog.isShowing() || dangTao)return;
        if(!durations.isEmpty()) {
            if(player!=null)batAmThanh(); else taiTai(viTriDung,true);
            return;
        }
        muonPhat=true;
        main.removeCallbacks(quaHan); main.postDelayed(quaHan,60000);
        if(tts==null) {
            trangThai("Đang chuẩn bị giọng đọc tiếng Việt…");
            view.findViewById(R.id.ngheChuanBi).setVisibility(View.VISIBLE);
            tts=new TextToSpeech(activity,status->{
                if(daHuy || tts==null)return;
                ttsReady=status==TextToSpeech.SUCCESS;
                if(ttsReady) {
                    int language=tts.setLanguage(new Locale("vi","VN"));
                    ttsReady=language!=TextToSpeech.LANG_MISSING_DATA && language!=TextToSpeech.LANG_NOT_SUPPORTED;
                    // Ưu tiên giọng đã cài để tổng hợp file không phải chờ mạng.
                    android.speech.tts.Voice hienTai=tts.getVoice();
                    if(ttsReady && (hienTai==null || hienTai.isNetworkConnectionRequired()) && tts.getVoices()!=null) {
                        for(android.speech.tts.Voice voice:tts.getVoices()) {
                            if("vi".equals(voice.getLocale().getLanguage()) && !voice.isNetworkConnectionRequired()
                                    && (voice.getFeatures()==null || !voice.getFeatures().contains(
                                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))) {
                                tts.setVoice(voice); break;
                            }
                        }
                    }
                }
                ttsLoi=!ttsReady;
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) {}
                    @Override public void onDone(String id) { main.post(()->xongTep(id,true)); }
                    @Override public void onError(String id) { main.post(()->xongTep(id,false)); }
                    @Override public void onError(String id,int code) { main.post(()->xongTep(id,false)); }
                });
                if(muonPhat && dialog.isShowing()) {
                    if(ttsReady)taoAmThanh(); else loi("Chưa có giọng đọc tiếng Việt. Cài giọng đọc trong cài đặt thiết bị rồi mở lại màn hình.");
                }
            });
        } else if(ttsReady)taoAmThanh();
        else if(ttsLoi)loi("Giọng đọc tiếng Việt chưa sẵn sàng. Kiểm tra cài đặt giọng đọc của thiết bị.");
    }

    private void taoAmThanh() {
        if(dangTao || !muonPhat)return;
        dangTao=true;
        chunks=DoanAmThanh.chia(noiDung,Math.min(3000,TextToSpeech.getMaxSpeechInputLength()));
        doan=0;
        view.findViewById(R.id.nghePhatTamDung).setEnabled(false);
        view.findViewById(R.id.ngheChuanBi).setVisibility(View.VISIBLE);
        taoDoan();
    }

    private void taoDoan() {
        if(!dangTao)return;
        if(doan==chunks.size()) { docThoiLuong(); return; }
        trangThai("Đang chuẩn bị âm thanh · "+(doan+1)+"/"+chunks.size());
        try {
            File file=File.createTempFile("thuyet_minh_",".wav",activity.getCacheDir());
            files.add(file); luotTao=file.getAbsolutePath(); dangGhi.put(luotTao,file);
            if(tts.synthesizeToFile(chunks.get(doan),null,file,luotTao)!=TextToSpeech.SUCCESS)
                loi("Chưa tạo được âm thanh. Bấm Nghe thuyết minh để thử lại.");
        } catch(Exception e) { loi("Không tạo được âm thanh tạm. Hãy kiểm tra dung lượng thiết bị và thử lại."); }
    }

    private void xongTep(String id,boolean ok) {
        File file=dangGhi.remove(id);
        if(id==null || !id.equals(luotTao) || !dangTao) {
            if(file!=null)file.delete();
            return;
        }
        luotTao=null;
        main.removeCallbacks(quaHan); main.postDelayed(quaHan,60000);
        if(!ok) { loi("Không tổng hợp được giọng đọc. Bấm Nghe thuyết minh để thử lại."); return; }
        doan++; taoDoan();
    }

    private void docThoiLuong() {
        final int token=phien;
        List<File> banSao=new ArrayList<>(files);
        nen.execute(()->{
            List<Integer> ketQua=new ArrayList<>();
            try {
                for(File file:banSao) {
                    MediaMetadataRetriever reader=new MediaMetadataRetriever();
                    try {
                        reader.setDataSource(file.getAbsolutePath());
                        int ms=Integer.parseInt(reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        if(ms<=0)throw new IllegalStateException("Tệp không có thời lượng");
                        ketQua.add(ms);
                    } finally { reader.release(); }
                }
                main.post(()->{
                    if(token!=phien || daHuy || !dialog.isShowing())return;
                    durations.addAll(ketQua); tong=0; for(int ms:durations)tong+=ms;
                    dangTao=false;
                    main.removeCallbacks(quaHan);
                    view.findViewById(R.id.ngheChuanBi).setVisibility(View.GONE);
                    chu(R.id.ngheTongThoiGian,thoiGian(tong));
                    taiTai(0,muonPhat);
                });
            } catch(Exception e) {
                main.post(()->{if(token==phien && !daHuy)loi("Không mở được âm thanh. Bấm Nghe thuyết minh để thử lại.");});
            }
        });
    }

    private void taiTai(int position,boolean play) {
        if(durations.isEmpty())return;
        thaPlayer();
        int[] target=DoanAmThanh.tim(durations,position); doan=target[0];
        nenDoan=0; for(int i=0;i<doan;i++)nenDoan+=durations.get(i);
        viTriDung=Math.min(position,tong);
        tua.setEnabled(false);
        view.findViewById(R.id.nghePhatTamDung).setEnabled(false);
        MediaPlayer moi=new MediaPlayer(); player=moi;
        int token=phien;
        try {
            moi.setAudioAttributes(attrs);
            moi.setDataSource(files.get(doan).getAbsolutePath());
            moi.setOnPreparedListener(p->{
                if(token!=phien || player!=p)return;
                daChuanBi=true;
                p.setOnSeekCompleteListener(done->{
                    if(player!=done || token!=phien)return;
                    capNhatSanSang(play);
                });
                if(target[1]>0)p.seekTo(target[1]); else capNhatSanSang(play);
            });
            moi.setOnCompletionListener(p->{
                if(player!=p || token!=phien)return;
                if(doan+1<files.size())taiTai(nenDoan+durations.get(doan),true);
                else {
                    viTriDung=tong; thaPlayer(); boFocus();
                    tua.setProgress(1000); chu(R.id.ngheDaPhat,thoiGian(tong));
                    chu(R.id.nghePhatTamDung,"↻  Nghe lại"); trangThai("Đã nghe hết bài thuyết minh");
                    viTriDung=0;
                }
            });
            moi.setOnErrorListener((p,what,extra)->{
                if(player==p && token==phien)loi("Không phát được âm thanh. Bạn có thể thử lại.");
                return true;
            });
            moi.prepareAsync();
        } catch(Exception e) { loi("Không mở được bài thuyết minh. Bạn có thể thử lại."); }
    }

    private void capNhatSanSang(boolean play) {
        tua.setEnabled(true); view.findViewById(R.id.nghePhatTamDung).setEnabled(true);
        if(play)batAmThanh(); else {chu(R.id.nghePhatTamDung,"▶  Tiếp tục"); trangThai("Đã tạm dừng");}
        capNhatTienTrinh();
    }

    private void batAmThanh() {
        if(player==null)return;
        if(!xinFocus()) { trangThai("Chưa thể phát lúc này. Hãy thử lại sau khi kết thúc âm thanh khác."); return; }
        player.start(); chu(R.id.nghePhatTamDung,"Ⅱ  Tạm dừng"); trangThai("Đang phát thuyết minh");
        main.removeCallbacks(nhip); main.post(nhip);
    }

    private void tamDung(String lyDo) {
        if(!daChuanBi || player==null || !player.isPlaying())return;
        player.pause(); main.removeCallbacks(nhip); boFocus();
        chu(R.id.nghePhatTamDung,"▶  Tiếp tục"); trangThai(lyDo); capNhatTienTrinh();
    }

    private final Runnable nhip=new Runnable() {
        @Override public void run() {
            if(player==null)return;
            capNhatTienTrinh();
            if(player!=null && player.isPlaying())main.postDelayed(this,250);
        }
    };
    private void capNhatTienTrinh() {
        if(!daChuanBi || player==null || tong<=0 || dangTua)return;
        int pos=Math.min(tong,nenDoan+player.getCurrentPosition());
        viTriDung=pos;
        tua.setProgress((int)((long)pos*1000/tong)); chu(R.id.ngheDaPhat,thoiGian(pos));
    }

    private void dung() {
        muonPhat=false;
        if(dangTao || durations.isEmpty()) {
            donLuot();
        } else {thaPlayer(); boFocus();}
        resetDongHo(); tua.setEnabled(!durations.isEmpty());
        trangThai("Đã dừng · Bấm Nghe thuyết minh để nghe từ đầu");
    }

    private void resetDongHo() {
        viTriDung=0; dangTua=false;
        tua.setProgress(0); tua.setEnabled(false);
        chu(R.id.ngheDaPhat,"00:00");
        chu(R.id.ngheTongThoiGian,tong>0?thoiGian(tong):"--:--");
        chu(R.id.nghePhatTamDung,"▶  Nghe thuyết minh");
        view.findViewById(R.id.nghePhatTamDung).setEnabled(true);
        view.findViewById(R.id.ngheChuanBi).setVisibility(View.GONE);
    }
    private void loi(String text) { donLuot(); resetDongHo(); trangThai(text); }
    private void chu(int id,String text) { ((TextView)view.findViewById(id)).setText(text); }
    private void trangThai(String text) {chu(R.id.ngheTrangThai,text);}
    private String thoiGian(int ms) {int s=ms/1000;return String.format(Locale.ROOT,"%02d:%02d",s/60,s%60);}

    private boolean xinFocus() {
        if(giuFocus)return true;
        int result=Build.VERSION.SDK_INT>=26?audio.requestAudioFocus(focus)
                :audio.requestAudioFocus(doiFocus,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        giuFocus=result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED; return giuFocus;
    }
    private void boFocus() {
        if(!giuFocus)return;
        giuFocus=false;
        if(Build.VERSION.SDK_INT>=26)audio.abandonAudioFocusRequest(focus); else audio.abandonAudioFocus(doiFocus);
    }
    private void thaPlayer() {
        main.removeCallbacks(nhip);
        daChuanBi=false;
        if(player!=null) {player.release();player=null;}
    }
    private void donLuot() {
        main.removeCallbacks(quaHan);
        phien++; muonPhat=false; dangTao=false; luotTao=null;
        if(tts!=null)tts.stop();
        thaPlayer(); boFocus();
        for(File f:files)f.delete(); files.clear(); durations.clear(); tong=0;
    }
    void dong() {
        donLuot();
        if(dialog.isShowing())dialog.dismiss();
    }
    void giaiPhong() {
        dong(); daHuy=true;
        if(tts!=null){tts.shutdown();tts=null;}
        activity.unregisterReceiver(rutTaiNghe);
        nen.shutdown();
    }
}
