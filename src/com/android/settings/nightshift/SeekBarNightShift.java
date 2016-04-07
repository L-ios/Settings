package com.android.settings.nightshift;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObservable;
import android.os.*;
import android.os.Process;
import android.provider.Settings;
import android.telecom.Log;
import android.util.Slog;
import android.widget.SeekBar;

/**
 * Created by lingyang on 3/28/16.
 */
public class SeekBarNightShift implements SeekBar.OnSeekBarChangeListener, Handler.Callback, NightShiftSeekBarPreference.CallBack {

    private final static String TAG = "SeekBarNightShift";

    private static final String ENABLED = Settings.Secure.NIGHT_SHIFT_ENABLED;
    private static final String VALUE = Settings.Secure.NIGHT_SHIFT_VALUE;

    private final static int MSG_CHANGE_COLOR = 1;
    private final static int MSG_ORIGIN_COLOR = 2;

    private Context mContext;
    private ContentResolver mContentResolver;
    private SeekBar mSeekBar;
    private Handler mHadler;
    private static final int DEFAULT_VALUE = 50;
    private static final int MAX_VALUE = 60;
    private static final int MIN_VALUE = 20;
    private final static int ORIGIN_COLOR_TEMPERATURE = 65;
    /*
     0        27                              50
     +-------------------+--------------------+--------65
     MIN_VALUE       mLastSeekBa         MAX_VALUE   SUNLIGHT
      */
    private int mColorTemperature = -1;
    private float ORIGIN_VALUE = 1f;
    private final static String BLUE_VALUE = "blue_value";
    private final static String GREEN_VALUE = "green_value";

    public SeekBarNightShift(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Message msg = new Message();
        msg.what = MSG_CHANGE_COLOR;
        Bundle data = new Bundle();
        float blue = computeBlueByColorTemperature(progress + MIN_VALUE);
        data.putFloat(BLUE_VALUE, blue);
        float green = computeGreenByColorTemperature(progress + MIN_VALUE);
        data.putFloat(GREEN_VALUE, green);
        msg.setData(data);
        if (mHadler != null) {
            mHadler.sendMessage(msg);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "fuck and fuck");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        saveColorTemperature(seekBar.getProgress());
        Settings.Secure.putInt(mContentResolver,
                VALUE, mColorTemperature);
        boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                ENABLED, 0) != 0;

        if (enabled) {
            return;
        } else {
            Message msg = new Message();
            msg.what = MSG_ORIGIN_COLOR;
            Bundle data = new Bundle();
            data.putFloat(BLUE_VALUE, ORIGIN_VALUE);
            data.putFloat(GREEN_VALUE, ORIGIN_VALUE);
            msg.setData(data);
            mHadler.sendMessage(msg);
        }
    }

    public void start() {
        HandlerThread thread = new HandlerThread(TAG + ".CallbackHandler");
        thread.start();
        mHadler = new Handler(thread.getLooper(), this);
        mColorTemperature = Settings.Secure.getInt(mContentResolver,
                VALUE, DEFAULT_VALUE);
    }

    public void stop() {
        mHadler = null;
    }

    public void setSeekBar(SeekBar seekBar) {
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(null);
        } else {
            mSeekBar = seekBar;
            mSeekBar.setOnSeekBarChangeListener(null);
        }

        mSeekBar.setMax(MAX_VALUE - MIN_VALUE);
        updateSeekBar();
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    protected void updateSeekBar() {
        if (mSeekBar == null) return;
        if (mColorTemperature < 0) {
            mSeekBar.setProgress(DEFAULT_VALUE - MIN_VALUE);
        } else {
            mSeekBar.setProgress(mColorTemperature  - MIN_VALUE);
        }
    }

    private void saveColorTemperature(int progress) {
        mColorTemperature = progress + MIN_VALUE;
    }

    @Override
    public boolean handleMessage(Message msg) {
        float[] m = null;
        final int what = msg.what;
        if (what == MSG_CHANGE_COLOR) {
            Bundle data = msg.getData();
            float blue = data.getFloat(BLUE_VALUE);
            float green = data.getFloat(GREEN_VALUE);
            m = buildColorMatrix(green, blue);
        } else if (what == MSG_ORIGIN_COLOR) {
            Bundle data = msg.getData();
            float green = data.getFloat(GREEN_VALUE);
            float blue = data.getFloat(BLUE_VALUE);
            m = buildColorMatrix(green, blue);
        }
        setColorTransform(m);
        return true;
    }

    private float[] buildColorMatrix(float green, float blue) {
        float[] m = {
                1,     0,    0, 0,
                0, green,    0, 0,
                0,     0, blue, 0,
                0,     0,    0, 1
        };
        return m;
    }

    private void setColorTransform(float[] m) {
        try {
            final IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                final Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (m != null) {
                    data.writeInt(1);
                    for (int i = 0; i < 16; i++) {
                        data.writeFloat(m[i]);
                    }
                } else {
                    data.writeInt(0);
                }
                flinger.transact(1015, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
        }
    }

    private float computeBlueByColorTemperature(int colorTemperature) {
        float blue;
        if (colorTemperature < 19) {
            blue = 0f;
        } else if (colorTemperature >= 66){
            blue = 1;
        } else {
            blue = (float) (Math.log(colorTemperature - 10) * 138.5177312231 - 305.0447927307) / 255f;
            if (blue > 1f) {
                blue = 1f;
            }
            if (blue < 0f) {
                blue = 0f;
            }
        }

        return blue;
    }

    private float computeGreenByColorTemperature(int colorTemperature) {
        float green;
        if (colorTemperature < 66) {
            green = (float)(99.4708025861 * Math.log(colorTemperature) - 161.1195681661) / 255f;
            if (green > 1f) {
                green = 1f;
            }
            if (green < 0f) {
                green = 0f;
            }
        } else {
            green = (float)(288.1221695283 * (Math.pow((colorTemperature - 60) , -0.0755148492))) / 255f;
            if (green > 1f) {
                green = 1f;
            }
            if (green < 0f) {
                green = 0f;
            }
        }
        return green;
    }

    @Override
    public void enableSettings(boolean enable) {
        Message msg = new Message();
        Bundle data;
        data = new Bundle();
        float green;
        float blue;
        if (enable) {
            msg.what = MSG_CHANGE_COLOR;
            green = computeGreenByColorTemperature(mColorTemperature);
            blue = computeBlueByColorTemperature(mColorTemperature);
        } else {
            msg.what = MSG_ORIGIN_COLOR;
            green = computeGreenByColorTemperature(DEFAULT_VALUE);
            blue = computeBlueByColorTemperature(DEFAULT_VALUE);
        }
        data.putFloat(GREEN_VALUE, green);
        data.putFloat(BLUE_VALUE, blue);
        msg.setData(data);
        if (mHadler != null) {
            mHadler.sendMessage(msg);
        }
    }
}
