package com.lightmsg;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TabWidget;
import android.widget.TextView;

public class BadgeView extends TextView {

    public static final int POSITION_TOP_LEFT = 1;
    public static final int POSITION_TOP_RIGHT = 2;
    public static final int POSITION_BOTTOM_LEFT = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;

    private static final int DEFAULT_MARGIN_DIP = 5;
    //private static final int DEFAULT_LR_PADDING_DIP = 5;
    private static final int DEFAULT_CORNER_RADIUS_DIP = 8;
    private static final int DEFAULT_POSITION = POSITION_TOP_RIGHT;
    public static final int ORANGE = -26368;// 0xFFFF9900;
    public static final int RED_ORANGE = 0xFFFF6666;
    public static final int ORANGE_RED = 0xFFFF6600;
    public static final int LIGHT_ORANGE = 0xFFFF9966;
    private static final int DEFAULT_BADGE_COLOR = ORANGE_RED;//Color.GREEN;//Color.MAGENTA;//Color.RED;
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;

    private static Animation fadeIn;
    private static Animation fadeOut;

    private Context context;
    private View target;

    private int badgePosition;
    private int badgeMargin;
    private int badgeLeftMargin;
    private int badgeRightMargin;
    private int badgeTopMargin;
    private int badgeBottomMargin;
    private int badgeColor;

    private boolean isShown;

    private ShapeDrawable badgeBg;

    private int targetTabIndex;

    public BadgeView(Context context) {
        this(context, (AttributeSet) null, android.R.attr.textViewStyle);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }


    public BadgeView(Context context, View target) {
        this(context, null, android.R.attr.textViewStyle, target, 0);
    }


    public BadgeView(Context context, TabWidget target, int index) {
        this(context, null, android.R.attr.textViewStyle, target, index);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, null, 0);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyle, View target, int tabIndex) {
        super(context, attrs, defStyle);
        if (target != null)
            init(context, target, tabIndex);
    }

    private void init(Context context, View target, int tabIndex) {
        this.context = context;
        this.target = target;
        this.targetTabIndex = tabIndex;

        // apply defaults
        badgePosition = DEFAULT_POSITION;
        badgeMargin = dipToPixels(DEFAULT_MARGIN_DIP);
        badgeColor = DEFAULT_BADGE_COLOR;

        setTypeface(Typeface.DEFAULT_BOLD);

        //int paddingPixels = dipToPixels(DEFAULT_LR_PADDING_DIP);
        //setPadding(paddingPixels, 0, paddingPixels, 0);

        setTextColor(DEFAULT_TEXT_COLOR);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(200);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(200);

        isShown = false;

        if (this.target != null) {
            applyTo(this.target);
        } else {
            show();
        }

    }

    public static class CustFrameLayout extends FrameLayout implements Checkable {

        private boolean mChecked;

        public CustFrameLayout(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        public CustFrameLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
        }

        public CustFrameLayout(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            // TODO Auto-generated constructor stub
        }

        @Override
        public boolean isChecked() {
            // TODO Auto-generated method stub
            return mChecked;
        }

        @Override
        public void setChecked(boolean checked) {
            // TODO Auto-generated method stub
            if (mChecked != checked) {
                mChecked = checked;
                refreshDrawableState();
            }
        }

        @Override
        public void toggle() {
            // TODO Auto-generated method stub
            setChecked(!mChecked);
        }

    }

    private void applyTo(View target) {

        LayoutParams lp = target.getLayoutParams();
        ViewParent parent = target.getParent();
        CustFrameLayout container = new CustFrameLayout(context);

        if (target instanceof TabWidget) {

            // set target to the relevant tab child container
            target = ((TabWidget) target).getChildTabViewAt(targetTabIndex);
            this.target = target;

            ((ViewGroup) target).addView(container,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            this.setVisibility(View.GONE);
            container.addView(this);

        } else {

            // TODO verify that parent is indeed a ViewGroup
            ViewGroup group = (ViewGroup) parent;
            int index = group.indexOfChild(target);

            group.removeView(target);
            group.addView(container, index, lp);

            container.addView(target,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            this.setVisibility(View.GONE);
            container.addView(this);

            group.invalidate();

        } /*else {

            // TODO verify that parent is indeed a ViewGroup
            ViewGroup group = (ViewGroup) parent;
            int index = group.indexOfChild(target);

            group.removeView(target);
            group.addView(target, index,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            this.setVisibility(View.GONE);
            group.addView(this);
            group.invalidate();

        }*/

    }


    public void show() {
        show(false, null);
    }


    public void show(boolean animate) {
        show(animate, fadeIn);
    }


    public void show(Animation anim) {
        show(true, anim);
    }


    public void hide() {
        hide(false, null);
    }


    public void hide(boolean animate) {
        hide(animate, fadeOut);
    }


    public void hide(Animation anim) {
        hide(true, anim);
    }


    public void toggle() {
        toggle(false, null, null);
    }


    public void toggle(boolean animate) {
        toggle(animate, fadeIn, fadeOut);
    }


    public void toggle(Animation animIn, Animation animOut) {
        toggle(true, animIn, animOut);
    }

    private void show(boolean animate, Animation anim) {
        //if (getBackground() == null) {
        if (badgeBg == null) {
            badgeBg = getDefaultBackground();
        }
        setBackgroundDrawable(badgeBg);
        //}
        applyLayoutParams();

        if (animate) {
            this.startAnimation(anim);
        }
        this.setVisibility(View.VISIBLE);
        isShown = true;
    }

    private void hide(boolean animate, Animation anim) {
        this.setVisibility(View.GONE);
        if (animate) {
            this.startAnimation(anim);
        }
        isShown = false;
    }

    private void toggle(boolean animate, Animation animIn, Animation animOut) {
        if (isShown) {
            hide(animate && (animOut != null), animOut); 
        } else {
            show(animate && (animIn != null), animIn);
        }
    }


    public int increment(int offset) {
        CharSequence txt = getText();
        int i;
        if (txt != null) {
            try {
                i = Integer.parseInt(txt.toString());
            } catch (NumberFormatException e) {
                i = 0;
            }
        } else {
            i = 0;
        }
        i = i + offset;
        setText(String.valueOf(i));
        return i;
    }


    public int decrement(int offset) {
        return increment(-offset);
    }

    private ShapeDrawable getDefaultBackground() {

        /*int r = dipToPixels(DEFAULT_CORNER_RADIUS_DIP);
        float[] outerR = new float[] {r, r, r, r, r, r, r, r};

        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);*/
        OvalShape os = new OvalShape();
        ShapeDrawable drawable = new ShapeDrawable(os);

        drawable.getPaint().setColor(badgeColor);
        return drawable;

    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
                getDefaultSize(0, heightMeasureSpec));

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Log.v("BadgeView", "measureWidth="+width);
        Log.v("BadgeView", "measureHeight=" + height);

        width = getWidth();
        height = getHeight();
        Log.v("BadgeView", "width="+width);
        Log.v("BadgeView", "height="+height);

        Log.v("BadgeView", "width2="+MeasureSpec.getSize(widthMeasureSpec));
        Log.v("BadgeView", "height2="+MeasureSpec.getSize(heightMeasureSpec));

        // Make height equal to width, to make square
        if (getWidth() > 0)
        heightMeasureSpec = widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }*/

    private void applyLayoutParams() {

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        switch (badgePosition) {
        case POSITION_TOP_LEFT:
            lp.gravity = Gravity.LEFT | Gravity.TOP;
            if (badgeTopMargin != 0 || badgeLeftMargin != 0) {
                lp.setMargins(badgeLeftMargin, badgeTopMargin, 0, 0);
            } else {
                lp.setMargins(badgeMargin, badgeMargin, 0, 0);
            }
            break;
        case POSITION_TOP_RIGHT:
            lp.gravity = Gravity.RIGHT | Gravity.TOP;
            if (badgeTopMargin != 0 || badgeRightMargin != 0) {
                lp.setMargins(0, badgeTopMargin, badgeRightMargin, 0);
            } else {
                lp.setMargins(0, badgeMargin, badgeMargin, 0);
            }
            break;
        case POSITION_BOTTOM_LEFT:
            lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
            if (badgeLeftMargin != 0 || badgeBottomMargin != 0) {
                lp.setMargins(badgeLeftMargin, 0, 0, badgeBottomMargin);
            } else {
                lp.setMargins(badgeMargin, 0, 0, badgeMargin);
            }
            break;
        case POSITION_BOTTOM_RIGHT:
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            if (badgeRightMargin != 0 || badgeBottomMargin != 0) {
                lp.setMargins(0, 0, badgeRightMargin, badgeBottomMargin);
            } else {
                lp.setMargins(0, 0, badgeMargin, badgeMargin);
            }
            break;
        default:
            break;
        }

        setLayoutParams(lp);

    }

    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int pxToDp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public View getTarget() {
        return target;
    }

    public void setTarget(Context context, View v) {
        this.target = v;
        this.context = context;
        if (target != null) {
            init(context, target, 0);
        }
    }


    @Override
    public boolean isShown() {
        return isShown;
    }


    public int getBadgePosition() {
        return badgePosition;
    }


    public void setBadgePosition(int layoutPosition) {
        this.badgePosition = layoutPosition;
    }


    public int getBadgeMargin() {
        return badgeMargin;
    }


    public void setBadgeMargin(int badgeMargin) {
        this.badgeMargin = badgeMargin;
    }

    public void setBadgeLeftMargin(int badgeMargin) {
        this.badgeLeftMargin = badgeMargin;
    }
    public void setBadgeRightMargin(int badgeMargin) {
        this.badgeRightMargin = badgeMargin;
    }
    public void setBadgeTopMargin(int badgeMargin) {
        this.badgeTopMargin = badgeMargin;
    }
    public void setBadgeBottomMargin(int badgeMargin) {
        this.badgeBottomMargin = badgeMargin;
    }

    public int getBadgeBackgroundColor() {
        return badgeColor;
    }


    public void setBadgeBackgroundColor(int badgeColor) {
        this.badgeColor = badgeColor;
        badgeBg = getDefaultBackground();
    }

    private int dipToPixels(int dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return (int) px;
    }

}