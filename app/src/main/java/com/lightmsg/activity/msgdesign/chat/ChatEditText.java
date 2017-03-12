package com.lightmsg.activity.msgdesign.chat;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lightmsg.R;


/**
 * ChatEditText is a thin veneer over AppCompatEditText that configures itself
 * to be editable.
 *
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/text.html">Text Fields</a>
 * guide.</p>
 * <p>
 * <b>XML attributes</b>
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes},
 */
public class ChatEditText extends AppCompatEditText implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = ChatEditText.class.getSimpleName();
    
    public ChatEditText(Context context) {
        this(context, null);
    }

    public ChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        //setOnTouchListener(this);
        //setOnClickListener(this);
        //setOnLongClickListener(this);
    }

    public ChatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //setOnTouchListener(this);
        //setOnClickListener(this);
        //setOnLongClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "onTouchEvent(), event=" + event);
        //printStack();
        boolean ret = super.onTouchEvent(event);
        Log.v(TAG, "onTouchEvent(), super ret=" + ret);
        return ret;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v(TAG, "onTouch(), "+v+"event=" + event);
        //printStack();
        return false;
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG, "onClick(), v=" + v);
        printStack();
    }
    
    @Override
    public boolean onLongClick(View v) {
        Log.v(TAG, "onLongClick(), v=" + v);
        //printStack();
        return false;
    }
    
    private void printStack() {
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}