package com.fezrestia.android.rapidsurfaceblinder.control;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.fezrestia.android.rapidsurfaceblinder.view.BlinderView;
import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.rapidsurfaceblinder.R;
import com.fezrestia.android.rapidsurfaceblinder.service.RapidSurfaceBlinderService;
import com.fezrestia.android.rapidsurfaceblinder.view.TriggerView;

public class RapidSurfaceBlinderController {
    // Log tag.
    private static final String TAG = "RapidSurfaceBlinderController";

    // Master context.
    private  Context mContext;

    // Singleton instance
    private static final RapidSurfaceBlinderController INSTANCE
                = new RapidSurfaceBlinderController();

    // Overlay view.
    private TriggerView mTriggerView = null;
    private BlinderView mBlinderView = null;

    /**
     * Life cycle trigger interface.
     */
    public static class LifeCycleTrigger {
        private static final String TAG = LifeCycleTrigger.class.getSimpleName();
        private static final LifeCycleTrigger INSTANCE = new LifeCycleTrigger();

        // CONSTRUCTOR.
        private LifeCycleTrigger() {
            // NOP.
        }

        /**
         * Get accessor.
         *
         * @return
         */
        public static LifeCycleTrigger getInstance() {
            return INSTANCE;
        }

        /**
         * Start.
         *
         * @param context
         */
        public void requestStart(Context context) {
            Intent service = new Intent(context, RapidSurfaceBlinderService.class);
            ComponentName component = context.startService(service);

            if (Log.IS_DEBUG) {
                if (component != null) {
                    Log.logDebug(TAG, "requestStart() : Component = " + component.toString());
                } else {
                    Log.logDebug(TAG, "requestStart() : Component = NULL");
                }
            }
        }

        /**
         * Stop.
         *
         * @param context
         */
        public void requestStop(Context context) {
            Intent service = new Intent(context, RapidSurfaceBlinderService.class);
            boolean isSuccess = context.stopService(service);

            if (Log.IS_DEBUG) Log.logDebug(TAG, "requestStop() : isSuccess = " + isSuccess);
        }
    }

    /**
     * CONSTRUCTOR.
     */
    private RapidSurfaceBlinderController() {
        // NOP.
    }

    /**
     * Get singleton controller instance.
     *
     * @return
     */
    public static synchronized RapidSurfaceBlinderController getInstance() {
        return INSTANCE;
    }

    /**
     * Start overlay view finder.
     *
     * @param context
     */
    public void start(Context context) {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "start() : E");

        if (mTriggerView != null) {
            // NOP. Already started.
            Log.logError(TAG, "Error. Already started.");
            return;
        }

        // Cache master context.
        mContext = context;

        // Create blinder view.
        mBlinderView = (BlinderView)
                LayoutInflater.from(context).inflate(
                R.layout.blinder_view, null);
        mBlinderView.initialize();
        // Add to window.
        mBlinderView.addToOverlayWindow();

        // Create overlay view.
        mTriggerView = (TriggerView)
                LayoutInflater.from(context).inflate(
                R.layout.trigger_view, null);
        mTriggerView.initialize();
        // Add to window.
        mTriggerView.addToOverlayWindow();

        if (Log.IS_DEBUG) Log.logDebug(TAG, "start() : X");
    }

    /**
     * Resume overlay view finder.
     */
    public void resume() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "resume() : E");
        // NOP.
        if (Log.IS_DEBUG) Log.logDebug(TAG, "resume() : X");
    }

    /**
     * Overlay UI is active or not.
     *
     * @return
     */
    public boolean isOverlayActive() {
        return (mTriggerView != null);
    }

    /**
     * Pause overlay view finder.
     */
    public void pause() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "pause() : E");
        // NOP.
        if (Log.IS_DEBUG) Log.logDebug(TAG, "pause() : X");
    }

    /**
     * Stop overlay view finder.
     */
    public void stop() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "stop() : E");

        if (mTriggerView == null) {
            // NOP. Already stopped.
            Log.logError(TAG, "Error. Already stopped.");
            return;
        }

        // Release references.
        mContext = null;
        if (mTriggerView != null) {
            mTriggerView.release();
            mTriggerView.removeFromOverlayWindow();
            mTriggerView = null;
        }
        if (mBlinderView != null) {
            mBlinderView.release();
            mBlinderView.removeFromOverlayWindow();
            mBlinderView = null;
        }

        if (Log.IS_DEBUG) Log.logDebug(TAG, "stop() : X");
    }

    public void enableBlinder() {
        mBlinderView.enable();
    }

    public void disableBlinder() {
        mBlinderView.disable();
    }
}
