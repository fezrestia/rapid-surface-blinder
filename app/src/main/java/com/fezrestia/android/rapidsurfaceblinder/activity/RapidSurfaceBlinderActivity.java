package com.fezrestia.android.rapidsurfaceblinder.activity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.rapidsurfaceblinder.RapidSurfaceBlinderConstants;
import com.fezrestia.android.rapidsurfaceblinder.R;
import com.fezrestia.android.rapidsurfaceblinder.control.RapidSurfaceBlinderController;

public class RapidSurfaceBlinderActivity extends PreferenceActivity {
    // Log tag.
    private static final String TAG = "RapidSurfaceBlinderActivity";

    @Override
    public void onCreate(Bundle bundle) {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onCreate()");
        super.onCreate(null);

        // Add view finder anywhere preferences.
        addPreferencesFromResource(R.xml.preferences_rapid_surface_blinder);
    }

    @Override
    public void onResume() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onResume()");
        super.onResume();

        // Update preferences.
        applyCurrentPreferences();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        // NOP.
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        // NOP.
    }

    @Override
    public void onPause() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onPause()");
        super.onPause();
        // NOP.
    }

    @Override
    public void onDestroy() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onDestroy()");
        super.onDestroy();
        // NOP.
    }

    private void applyCurrentPreferences() {
        CheckBoxPreference enabled = (CheckBoxPreference)
                findPreference(RapidSurfaceBlinderConstants.KEY_ENABLED);
        enabled.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        if (RapidSurfaceBlinderController.getInstance().isOverlayActive()) {
            enabled.setChecked(true);
        } else {
            enabled.setChecked(false);
        }
    }

    private final OnPreferenceChangeListenerImpl mOnPreferenceChangeListener
            = new OnPreferenceChangeListenerImpl();
    private class OnPreferenceChangeListenerImpl
            implements  Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof CheckBoxPreference) {
                String key = preference.getKey();
                if (key == null) {
                    // NOP.
                    if (Log.IS_DEBUG) Log.logDebug(TAG, "CheckBox key == null");
                } else if (RapidSurfaceBlinderConstants.KEY_ENABLED.equals(key)) {

                    final boolean isChecked = ((Boolean) value).booleanValue();

                    if (isChecked) {
                        // Start.
                        RapidSurfaceBlinderController.LifeCycleTrigger.getInstance()
                                .requestStart(getApplicationContext());
                    } else {
                        // Remove.
                        RapidSurfaceBlinderController.LifeCycleTrigger.getInstance()
                                .requestStop(getApplicationContext());
                    }
                } else {
                    // NOP.
                    if (Log.IS_DEBUG) Log.logDebug(TAG, "Unexpected CheckBox preference.");
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    }
}
