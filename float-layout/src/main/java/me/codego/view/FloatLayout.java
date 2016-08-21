package me.codego.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by mengxn on 16-8-18.
 */
public class FloatLayout extends FrameLayout {

    private float lastX, lastY;
    private int touchSlop;

    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;
    private ValueAnimator stickyAnimator;

    private boolean isFloatTop = false;

    private static final int STICKY_DURATION = 300;


    public FloatLayout(Context context) {
        this(context, null);
    }

    public FloatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        displayMetrics = getResources().getDisplayMetrics();

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = isFloatTop ? WindowManager.LayoutParams.TYPE_SYSTEM_ERROR : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.y = 0;
        params.x = 0;

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getRawX();
                lastY = ev.getRawY();
                cancelStickyAnim();
                return false;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getX() - lastX) > touchSlop || Math.abs(ev.getY() - lastY) > touchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                move(event.getRawX(), event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
                animMoveToEdge(event);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int[] outLocation = new int[2];
        getLocationOnScreen(outLocation);
        int newX = outLocation[0] > displayMetrics.heightPixels/2 ? displayMetrics.widthPixels : 0;
        move(newX, outLocation[1]);
    }

    private void move(float x, float y) {
        params.x = (int) (x - getWidth()/2);
        params.y = (int) (y - getHeight()/2);
        windowManager.updateViewLayout(this, params);
    }

    private void animMoveToEdge(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        int newX = isHalfScreenWidth(x) ? displayMetrics.widthPixels : 0;
        animMoveX(x, newX, y);
    }

    private void animMoveX(float fromX, float toX, final float baseY) {
        stickyAnimator = ValueAnimator.ofFloat(fromX, toX).setDuration(STICKY_DURATION);
        stickyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                move(((float) animator.getAnimatedValue()), baseY);
            }
        });
        stickyAnimator.start();
    }

    private void cancelStickyAnim() {
        if (stickyAnimator != null && stickyAnimator.isRunning()) {
            stickyAnimator.cancel();
        }
    }

    private boolean isHalfScreenWidth(float x) {
        return x - displayMetrics.widthPixels / 2 > 0;
    }

    private boolean isFloat;

    public void show() {
        try {
            //如果已添加,则更新view
            if (isFloat) {
                windowManager.updateViewLayout(this, params);
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(getContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        return;
                    } else {
                        //绘ui代码, 这里说明6.0系统已经有权限了
                        windowManager.addView(this, params);
                    }
                }
            }
            isFloat = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hide() {
        if (isFloat) {
            windowManager.removeView(this);
        }
    }

    public boolean isFloatTop() {
        return isFloatTop;
    }

    public void setFloatTop(boolean floatTop) {
        isFloatTop = floatTop;
    }


}
