package in.revealinghour.tahmotion;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import bleservice.TAHble;

/**
 * Created by shail on 29/04/15.
 */

public class Music extends Fragment implements SensorEventListener, SeekBar.OnSeekBarChangeListener {

    ImageView mReverse, mNext, mPlayPause;
    Context context;
    //sensor
    private SensorManager mSensorManager;
    SeekBar seekVolControl;
    public static boolean ScreenVis = false;
    //sensor init for tilt tilt will detect using magnetometer sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    int temp = 4;
    int temp1 = -4;
    ImageView volUp, volDown, volControl;
    boolean btnHold = false;
    RelativeLayout volStatus, manualVolCon;
    boolean ismGeomagnetic = false;
    boolean VolUp = false;
    boolean VolDown = false;
    int temps = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_cantrol, container, false);
        context = getActivity();
        manualVolCon = (RelativeLayout) view.findViewById(R.id.manualVolume);
        manualVolCon.setVisibility(View.GONE);
        seekVolControl = (SeekBar) view.findViewById(R.id.seekbarvolcon);
        volUp = (ImageView) view.findViewById(R.id.upred);
        volDown = (ImageView) view.findViewById(R.id.downred);
        volControl = (ImageView) view.findViewById(R.id.imgvolumecantrol);
        volStatus = (RelativeLayout) view.findViewById(R.id.volumestatus);
        volStatus.setVisibility(View.GONE);
        seekVolControl.getProgressDrawable().setColorFilter(Color.parseColor("#29b6f6"), PorterDuff.Mode.SRC_IN);
        seekVolControl.getThumb().setTint(Color.parseColor("#ffffff"));
        seekVolControl.setOnSeekBarChangeListener(this);


        mReverse = (ImageView) view.findViewById(R.id.imgreverse);
        mNext = (ImageView) view.findViewById(R.id.imgforword);
        mPlayPause = (ImageView) view.findViewById(R.id.imgplaypause);

        // Setup the sensors
        sensorManager = (SensorManager) Selector.activity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Log.d("", "accelerometer is null");
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            ismGeomagnetic = false;
            manualVolCon.setVisibility(View.VISIBLE);
        } else {
            ismGeomagnetic = true;
            manualVolCon.setVisibility(View.GONE);
        }


        //play pause music
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Selector.mBluetoothLeService.TahKeyPress(TAHble.KEY_SPACE);
            }
        });
        //previous song
        mReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Selector.mBluetoothLeService.TahKeyPress(TAHble.KEY_LEFT_ARROW);
            }
        });
        //next song
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Selector.mBluetoothLeService.TahKeyPress(TAHble.KEY_RIGHT_ARROW);
            }
        });

        //hold button to change volume
        volControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (ismGeomagnetic) {
                        btnHold = true;
                        volStatus.setVisibility(View.VISIBLE);
                    }

                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    btnHold = false;
                    volStatus.setVisibility(View.GONE);
                    return true;
                } else
                    return false;
            }
        });
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        ScreenVis = true;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);


    }

    @Override
    public void onPause() {
        super.onPause();
        ScreenVis = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        ScreenVis = false;
        super.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG, "onSensorChanged()");
        if (event.values == null) {
            Log.w("", "event.values is null");
            return;
        }
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
            default:
                Log.w("", "Unknown sensor type " + sensorType);
                return;
        }
        if (mGravity == null) {
            // Log.w("", "mGravity is null");
            return;
        }
        if (mGeomagnetic == null) {
            // Log.w("", "mGeomagnetic is null");
            return;
        }
        float R[] = new float[9];
        if (!SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic)) {
            Log.w("", "getRotationMatrix() failed");
            return;
        }

        float orientation[] = new float[9];
        SensorManager.getOrientation(R, orientation);
        // Orientation contains: azimuth, pitch and roll - we'll use roll
        float roll = orientation[2];
        int rollDeg = (int) Math.round(Math.toDegrees(roll));

        long now = android.os.SystemClock.uptimeMillis();
        if (now - mLastGestureTime > 300) {
            mLastGestureTime = 0;
            if (btnHold) {
                if (rollDeg > 1) {
                    VolUp(true);
                    if (rollDeg > temp) {
                        temp = rollDeg;
                        Selector.mBluetoothLeService.TahKeyPress(TAHble.VolumeUp);
                    }
                } else {
                    VolUp(false);
                    temp = 0;
                }
//
                if (rollDeg < -1) {
                    VolDown(true);
                    if (rollDeg < temp1) {
                        temp1 = rollDeg;
                        Selector.mBluetoothLeService.TahKeyPress(TAHble.VolumeDown);
                    }

                } else {
                    VolDown(false);
                    temp1 = 0;
                }
            }
            mLastGestureTime = now;

        }


    }

    private long mLastGestureTime;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void VolUp(final boolean start) {
        Animation animation = new AlphaAnimation(2, 0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        if (start) {
            volUp.startAnimation(animation);
        } else {
            volUp.clearAnimation();
            volUp.setVisibility(View.VISIBLE);
            animation.reset();
        }
    }

    public void VolDown(final boolean start) {
        Animation animation = new AlphaAnimation(2, 0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        if (start) {
            volDown.startAnimation(animation);
        } else {
            volDown.clearAnimation();
            volDown.setVisibility(View.VISIBLE);
            animation.reset();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekbarvolcon:

                long now = android.os.SystemClock.uptimeMillis();
                if (now - mLastGestureTime > 200) {
                    mLastGestureTime = 0;
                    int curVal = Integer.parseInt(String.valueOf(progress));
                    if (curVal >= temps) {
                        Selector.mBluetoothLeService.TahKeyPress(TAHble.VolumeUp);
                    } else {
                        Selector.mBluetoothLeService.TahKeyPress(TAHble.VolumeDown);
                    }
                    temps = curVal;
                    mLastGestureTime = now;

                }
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
