package com.fezrestia.android.rapidsurfaceblinder.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.rapidsurfaceblinder.RapidSurfaceBlinderApplication;
import com.fezrestia.android.rapidsurfaceblinder.R;

public class BlinderView extends FrameLayout {
    // Log tag.
    private static final String TAG = "BlinderView";

    // Root view.
    private FrameLayout mRootView = null;

    // UI.
    private View mBlinder = null;

    // Display coordinates.
    private int mDisplayLongLineLength = 0;
    private int mDisplayShortLineLength = 0;

    // Overlay window orientation.
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;

    // Window.
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowLayoutParams = null;

    // Animation handler.
    private BlinderStateChangeAnimationTask mStateChangeTask = null;
    // Animation refresh interval.
    private static final int ANIMATION_REFRESH_INTERVAL = 16;

    // Interaction flag.
    private static final int INTERACTIVE_FLAGS = 0 // Dummy
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            | WindowManager.LayoutParams.FLAG_FULLSCREEN
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            ;
    private static final int NOT_INTERACTIVE_FLAGS = 0 // Dummy
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            | WindowManager.LayoutParams.FLAG_FULLSCREEN
            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            ;

    // CONSTRUCTOR.
    public BlinderView(final Context context) {
        this(context, null);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    // CONSTRUCTOR.
    public BlinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    // CONSTRUCTOR.
    public BlinderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    /**
     * Initialize all of configurations.
     */
    public void initialize() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "initialize() : E");

        // Cache instance references.
        cacheInstances();

        // Window related.
        createWindowParameters();

        // Update UI.
        updateTotalUserInterface();

        if (Log.IS_DEBUG) Log.logDebug(TAG, "initialize() : X");
    }

    private void cacheInstances() {
        // Root.
        mRootView = (FrameLayout) findViewById(R.id.root);

        // UI.
        mBlinder = findViewById(R.id.blinder);

        // Animation.
        mStateChangeTask = new BlinderStateChangeAnimationTask();
    }

    private void createWindowParameters() {
        mWindowManager = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);

        mWindowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                NOT_INTERACTIVE_FLAGS,
                PixelFormat.TRANSLUCENT);
    }

    /**
     * Release all resources.
     */
    public void release() {
        mRootView = null;
        mBlinder = null;

        RapidSurfaceBlinderApplication.getUiThreadHandler().removeCallbacks(mStateChangeTask);
        mStateChangeTask = null;

        mWindowManager = null;
        mWindowLayoutParams = null;
    }

    /**
     * Add this view to WindowManager layer.
     */
    public void addToOverlayWindow() {
        // Window parameters.
        updateWindowParams();

        // Add to WindowManager.
        WindowManager winMng = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
        winMng.addView(this, mWindowLayoutParams);
    }

    private void updateWindowParams() {
        mWindowLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowLayoutParams.x = 0;
        mWindowLayoutParams.y = 0;

        switch (mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                mWindowLayoutParams.width = mDisplayLongLineLength;
                mWindowLayoutParams.height = mDisplayShortLineLength;
                break;

            case Configuration.ORIENTATION_PORTRAIT:
                mWindowLayoutParams.width = mDisplayShortLineLength;
                mWindowLayoutParams.height = mDisplayLongLineLength;
                break;

            default:
                throw new RuntimeException("Unexpected orientation.");
        }

        if (isAttachedToWindow()) {
            mWindowManager.updateViewLayout(this, mWindowLayoutParams);
        }
    }

    /**
     * Remove this view from WindowManager layer.
     */
    public void removeFromOverlayWindow() {
        // Remove from to WindowManager.
        WindowManager winMng = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
        winMng.removeView(this);
    }

    private void updateTotalUserInterface() {
        // Screen configuration.
        calculateScreenConfiguration();
        // Window layout.
        updateWindowParams();
        // Update layout.
        updateLayoutParams();
    }

    private void calculateScreenConfiguration() {
        // Get display size.
        Display display = mWindowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getRealSize(screenSize);
        final int width = screenSize.x;
        final int height = screenSize.y;
        mDisplayLongLineLength = Math.max(width, height);
        mDisplayShortLineLength = Math.min(width, height);

        // Update state.
        mOrientation = getContext().getResources().getConfiguration().orientation;
    }

    private void updateLayoutParams() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Log.IS_DEBUG) Log.logDebug(TAG,
                "onConfigurationChanged() : [Config=" + newConfig.toString());
        super.onConfigurationChanged(newConfig);

        // Stop animation.
        RapidSurfaceBlinderApplication.getUiThreadHandler().removeCallbacks(mStateChangeTask);

        // Update UI.
        updateTotalUserInterface();
    }

    public void enable() {
        mStateChangeTask.setTargetAlpha(1.0f);
        if (!mStateChangeTask.isActive()) {
            mStateChangeTask.setActive();
            RapidSurfaceBlinderApplication.getUiThreadHandler().post(mStateChangeTask);
        }

        // Update flag.
        mWindowLayoutParams.flags = INTERACTIVE_FLAGS;
        if (isAttachedToWindow()) {
            mWindowManager.updateViewLayout(this, mWindowLayoutParams);
        }
    }

    public void disable() {
        mStateChangeTask.setTargetAlpha(0.0f);
        if (!mStateChangeTask.isActive()) {
            mStateChangeTask.setActive();
            RapidSurfaceBlinderApplication.getUiThreadHandler().post(mStateChangeTask);
        }

        // Update flag.
        mWindowLayoutParams.flags = NOT_INTERACTIVE_FLAGS;
        if (isAttachedToWindow()) {
            mWindowManager.updateViewLayout(this, mWindowLayoutParams);
        }
    }

    private class BlinderStateChangeAnimationTask implements Runnable {
        private final float GAIN_P = 0.2f;

        private float mTargetAlpha = 1.0f;

        private float mLastDiff = 0.0f;

        boolean mIsActive = false;

        public void setTargetAlpha(float target) {
            mTargetAlpha = target;
        }

        public float getTargetAlpha() {
            return mTargetAlpha;
        }

        public void setActive() {
            mIsActive = true;
        }

        public boolean isActive() {
            return mIsActive;
        }

        @Override
        public void run() {
            if (mBlinder != null) {
                // Diff.
                float diff = mTargetAlpha - mBlinder.getAlpha();

                // Check.
                if (((int) (diff * 100)) == ((int) (mLastDiff * 100))) {
                    // Already convergent.

                    mBlinder.setAlpha(mTargetAlpha);

                    mIsActive = false;
                    return;
                } else {
                    // Next move.

                    float curAlpha = mBlinder.getAlpha();
                    curAlpha += diff * GAIN_P;

                    mBlinder.setAlpha(curAlpha);

                    if (isAttachedToWindow()) {
                        // Go to next.
                        RapidSurfaceBlinderApplication.getUiThreadHandler().postDelayed(
                                this,
                                ANIMATION_REFRESH_INTERVAL);
                    }
                }

                mLastDiff = diff;
            }
        }
    }
}
