package com.lightmsg.activity.msgdesign.rost;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lightmsg.R;

public class SlipBladeView extends View {
    
    private static final String TAG = SlipBladeView.class.getSimpleName();
    private OnItemClickListener mOnItemClickListener;
    String[] SB = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z", "#"};
    int choose = -1;
    Paint paint = new Paint();
    boolean showBkg = false;
    private PopupWindow mPopupWindow;
    private TextView mPopupText;
    private Handler handler = new Handler();

    public SlipBladeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlipBladeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlipBladeView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(TAG, "onDraw()..."+canvas+", "+choose);
        
        if (showBkg) {
            canvas.drawColor(Color.parseColor("#00000000"));
        }

        int height = getHeight();
        int width = getWidth();
        float singleHeight = (float)height / (float)SB.length;
        Log.v(TAG, "onDraw()...singleHeight="+singleHeight);
        for (int i = 0; i < SB.length; i++) {
            paint.setColor(Color.DKGRAY);
            //paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(26);
            //paint.setFakeBoldText(true);
            paint.setAntiAlias(true);
            if (i == choose) {
                //paint.setColor(Color.parseColor("#3399ff"));
                //paint.setColor(getResources().getColor(R.color.roster_slipblade_text_color));
                paint.setTextSize(50);
            }
            float xPos = width / 2 - paint.measureText(SB[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            Log.v(TAG, "onDraw()...xPos="+xPos+", yPos="+yPos);
            canvas.drawText(SB[i], xPos, yPos, paint);
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.v(TAG, "dispatchTouchEvent()..."+event+", "+getHeight());
        
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final int c = (int) (y / getHeight() * SB.length);
        Log.v(TAG, "dispatchTouchEvent()...c="+c);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            showBkg = true;
            if (oldChoose != c) {
                if (c >= 0 && c < SB.length) {
                    performItemClicked(c);
                    choose = c;
                    invalidate();
                }
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (oldChoose != c) {
                if (c >= 0 && c < SB.length) {
                    performItemClicked(c);
                    choose = c;
                    invalidate();
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            showBkg = false;
            choose = -1;
            dismissPopup();
            invalidate();
            break;
        }
        return true;
    }

    private void showPopup(int item) {
        if (mPopupWindow == null) {
            float popTextSize = getResources().getDimension(R.dimen.slipblade_popup_text_size);
            float popSize = getResources().getDimension(R.dimen.slipblade_popup_size);
            int textColor = getResources().getColor(R.color.white);
            int bgColor = getResources().getColor(R.color.gray);

            handler.removeCallbacks(dismissRunnable);
            mPopupText = new TextView(getContext());
            mPopupText.setBackgroundColor(bgColor);
            mPopupText.setTextColor(textColor);
            mPopupText.setTextSize(popTextSize);
            mPopupText.setGravity(Gravity.CENTER_HORIZONTAL
                    | Gravity.CENTER_VERTICAL);
            
            mPopupWindow = new PopupWindow(mPopupText, (int)popSize, (int)popSize);
            //mPopupWindow = new PopupWindow(mPopupText, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            //mPopupWindow = new PopupWindow(mPopupText, 150, 150);
        }

        String text = SB[item];
        /*if (item == SB.length-1) {
            text = "#";
        } else {
            text = Character.toString((char) ('A' + item - 1));
        }*/
        
        mPopupText.setText(text);
        if (mPopupWindow.isShowing()) {
            mPopupWindow.update();
        } else {
            mPopupWindow.showAtLocation(getRootView(),
                    Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        }
    }

    private void dismissPopup() {
        handler.postDelayed(dismissRunnable, 800);
    }

    Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPopupWindow != null) {
                mPopupWindow.dismiss();
            }
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void performItemClicked(int item) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(SB[item]);
            showPopup(item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String s);
    }

}
