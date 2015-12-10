package com.fezrestia.android.rapidsurfaceblinder.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.fezrestia.android.lib.interaction.InteractionEngine;
import com.fezrestia.android.rapidsurfaceblinder.control.RapidSurfaceBlinderController;
import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.rapidsurfaceblinder.RapidSurfaceBlinderApplication;
import com.fezrestia.android.rapidsurfaceblinder.R;

public class TriggerView extends RelativeLayout {
    // Log tag.
    private static final String TAG = "TriggerView";

    // Root view.
    private RelativeLayout mRootView = null;

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

    // Interaction.
    private InteractionEngine mInteractionEngine = null;

    // Size.
    private Rect mMinRect = new Rect();
    private Rect mMaxRect = new Rect();
    // Offset.
    private Point mMinOffset = new Point();
    private Point mMaxOffset = new Point();

    // CONSTRUCTOR.
    public TriggerView(final Context context) {
        this(context, null);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    // CONSTRUCTOR.
    public TriggerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    // CONSTRUCTOR.
    public TriggerView(Context context, AttributeSet attrs, int defStyle) {
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

        // Load setting.
        loadPreferences();

        // Window related.
        createWindowParameters();

        // Update UI.
        updateTotalUserInterface();

        if (Log.IS_DEBUG) Log.logDebug(TAG, "initialize() : X");
    }

    private void cacheInstances() {
        // Root.
        mRootView = (RelativeLayout) findViewById(R.id.root);

        // UI.
        mBlinder = findViewById(R.id.blinder);

        // Set touch interceptor.
        mInteractionEngine = new InteractionEngine(
                mRootView.getContext(),
                mRootView,
                0,
                0,//ViewConfiguration.get(getContext()).getScaledTouchSlop(),
                RapidSurfaceBlinderApplication.getUiThreadHandler());
        mInteractionEngine.setInteractionCallback(mInteractionCallbackImpl);
        mRootView.setOnTouchListener(mOnTouchListenerImpl);
    }

    private void loadPreferences() {
    }

    private void createWindowParameters() {
        mWindowManager = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);

        mWindowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                0 // Dummy
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        ,
                PixelFormat.TRANSLUCENT);
    }

    /**
     * Release all resources.
     */
    public void release() {
        if (mInteractionEngine != null) {
            mInteractionEngine.setInteractionCallback(null);
            mInteractionEngine.release();
            mInteractionEngine = null;
        }

        if (mRootView != null) {
            mRootView.setOnTouchListener(null);
            mRootView = null;
        }
        mBlinder = null;

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
        mWindowLayoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

        switch (mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE: {
                // Window offset on enabled.
                mMaxRect.set(0, 0, mDisplayLongLineLength, mDisplayShortLineLength);
                break;
            }

            case Configuration.ORIENTATION_PORTRAIT: {
                // Window offset on enabled.
                mMaxRect.set(0, 0, mDisplayShortLineLength, mDisplayLongLineLength);
                break;
            }
            default: {
                // Unexpected orientation.
                throw new IllegalStateException("Unexpected orientation.");
            }
        }
        mMaxOffset.set(0, 0);
        mMinRect.set(0, 0, 128, 128);
        mMinOffset.set(36, 36);

        mWindowLayoutParams.x = mMinOffset.x;
        mWindowLayoutParams.y = mMinOffset.y;
        mWindowLayoutParams.width = mMinRect.width();
        mWindowLayoutParams.height = mMinRect.height();

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
        display.getSize(screenSize);
        final int width = screenSize.x;
        final int height = screenSize.y;
        mDisplayLongLineLength = Math.max(width, height);
        mDisplayShortLineLength = Math.min(width, height);

        // Get display orientation.
        if (height < width) {
            mOrientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            mOrientation = Configuration.ORIENTATION_PORTRAIT;
        }
    }

    private void updateLayoutParams() {
        if (mBlinder != null) {
            ViewGroup.LayoutParams params = mBlinder.getLayoutParams();
            params.width = mMinRect.width();
            params.height = mMinRect.height();
            mBlinder.setLayoutParams(params);
        }
    }

    private final OnTouchListenerImpl mOnTouchListenerImpl = new OnTouchListenerImpl();
    private class OnTouchListenerImpl implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Use absolute position, because window position change affects view motion event.
            event.setLocation(event.getRawX(), event.getRawY());

            mInteractionEngine.onTouchEvent(event);
            return true;
        }
    }

    private final InteractionEngine.InteractionCallback mInteractionCallbackImpl
            = new TnteractionCallbackImpl();
    private class TnteractionCallbackImpl implements InteractionEngine.InteractionCallback {
        @Override
        public void onSingleTouched(Point point) {
            // NOP.
        }

        @Override
        public void onSingleMoved(Point currentPoint, Point lastPoint, Point downPoint) {
            // NOP.
        }

        @Override
        public void onSingleStopped(Point currentPoint, Point lastPoint, Point downPoint) {
            // NOP.
        }

        @Override
        public void onSingleReleased(Point point) {
            // NOP.
        }

        @Override
        public void onSingleCanceled() {
            // NOP.
        }

        @Override
        public void onDoubleTouched(Point point0, Point point1) {
            // NOP.
        }

        @Override
        public void onDoubleMoved(Point point0, Point point1) {
            // NOP.
        }

        @Override
        public void onDoubleScaled(float currentLength, float previousLength, float originalLength) {
            // NOP.
        }

        @Override
        public void onDoubleRotated(float degreeVsOrigin, float degreeVsLast) {
            // NOP.
        }

        @Override
        public void onSingleReleasedInDouble(Point release, Point remain) {
            // NOP.
        }

        @Override
        public void onDoubleCanceled() {
            // NOP.
        }

        @Override
        public void onOverTripleCanceled() {
            // NOP.
        }

        @Override
        public void onFling(MotionEvent event1, MotionEvent event2, float velocX, float velocY) {
            // NOP.
        }

        @Override
        public void onLongPress(MotionEvent event) {
            RapidSurfaceBlinderController.getInstance().disableBlinder();
        }

        @Override
        public void onShowPress(MotionEvent event) {
            // NOP.
        }

        @Override
        public void onSingleTapUp(MotionEvent event) {
            RapidSurfaceBlinderController.getInstance().enableBlinder();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Log.IS_DEBUG) Log.logDebug(TAG,
                "onConfigurationChanged() : [Config=" + newConfig.toString());
        super.onConfigurationChanged(newConfig);

        // Update UI.
        updateTotalUserInterface();
    }
}
