package com.example.app_ck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import org.osmdroid.views.MapView;

/** Giữ thao tác kéo/chụm trên bản đồ khi đặt trong trang chào có thể cuộn. */
public class BanDoChaoMungView extends MapView {
    public BanDoChaoMungView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if (getParent() != null) {
            int action = event.getActionMasked();
            getParent().requestDisallowInterceptTouchEvent(
                    action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }
}
