package com.android.settings.nightshift;

import android.content.Context;
import android.preference.SeekBarPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import com.android.settings.R;

public class NightShiftSeekBarPreference extends SeekBarPreference{

    private static final String TAG = "NightShiftSeekBarPreference";

    private SeekBar mSeekBar;
    private SeekBarNightShift mNightShifter;

    public NightShiftSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                       int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.preference_night_shift_slider);
        init();
    }

    public NightShiftSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NightShiftSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NightShiftSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mSeekBar = (SeekBar) view.findViewById(com.android.internal.R.id.seekbar);
        init();
    }

    public void init() {
        if (mSeekBar == null) return;
        if (mNightShifter == null) {
            mNightShifter = new SeekBarNightShift(getContext());
        }
        mNightShifter.start();
        mNightShifter.setSeekBar(mSeekBar);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
    }

    public void updateNightShift(boolean enabled) {
        mNightShifter.enableSettings(enabled);
    }

    interface CallBack {
        public void enableSettings(boolean enable);
    }
}