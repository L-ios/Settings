package com.android.settings.nightshift;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.VolumeSeekBarPreference;

/**
 * Created by lingyang on 3/24/16.
 */
public class NightShiftPreferenceFragment extends SettingsPreferenceFragment {

    private static final String TAG = "NightShiftPreferenceFragment";
    private static final String KEY_NIGHT_SHIFT = "night_shift";
    private static final String KEY_NIGHT_SHIFT_ENABLE = "night_shift_enable";
    private static final String KEY_NIGHT_SHIFT_VALUE = "night_shift_value";
    private static final String ENABLED = Settings.Secure.NIGHT_SHIFT_ENABLED;
    private static final String VALUE = Settings.Secure.NIGHT_SHIFT_VALUE;
    //todo need to find this default value;
    private static final float DEFAULT_VALUE = 0.7f;
    private Context mContext;

    private TwoStatePreference mNightShiftEnable;
    private NightShiftSeekBarPreference mNightShiftSeekBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        addPreferencesFromResource(R.xml.night_shift);

        final PreferenceCategory nightShit = (PreferenceCategory) findPreference(KEY_NIGHT_SHIFT);
        initNightShiftSeekBar(nightShit);
        initNightShiftEnable(nightShit);

        mNightShiftSeekBar.setDependency(KEY_NIGHT_SHIFT_ENABLE);
        initPreferences();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private void initPreferences() {
        //final float
    }

    private void initNightShiftSeekBar(PreferenceCategory root) {
        mNightShiftSeekBar = (NightShiftSeekBarPreference) root.findPreference(KEY_NIGHT_SHIFT_VALUE);
        //mNightShiftSeekBar.setEnabled(true);
    }

    private void initNightShiftEnable(PreferenceCategory root) {
        mNightShiftEnable = (TwoStatePreference) root.findPreference(KEY_NIGHT_SHIFT_ENABLE);
        if (mNightShiftEnable == null) {
            Log.i(TAG, "Preference not found: " + ENABLED);
        }

        mNightShiftEnable.setPersistent(false);
        updateNightShiftEnable();
        mNightShiftEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean val = (Boolean) newValue;
                boolean ret = Settings.Secure.putInt(getContentResolver(),
                        ENABLED,
                        val ? 1 : 0);
                mNightShiftSeekBar.updateNightShift(val);
                return ret;
            }
        });
    }
    private void updateNightShiftEnable() {
        if (mNightShiftEnable != null) {
            mNightShiftEnable.setChecked(Settings.Secure.getInt(getContentResolver(),
                    ENABLED,0) != 0);
        }
    }
}
