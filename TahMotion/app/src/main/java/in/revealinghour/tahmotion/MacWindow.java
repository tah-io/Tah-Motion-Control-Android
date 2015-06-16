package in.revealinghour.tahmotion;

import android.content.Context;

import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import bleservice.TAHble;

/**
 * Created by shail on 12/03/15.
 */

public class MacWindow extends Fragment {

    @Nullable
    Context context;
    ImageView imgMusic, imgPresentation;
    private SensorManager mSensorManager;
    String macWindow;

    private final SensorListener mListener = new SensorListener() {

        private final float[] mScale = new float[]{1.7f, 2, 0.5f};   // accel

        private float[] mPrev = new float[3];

        public void onSensorChanged(int sensor, float[] values) {
            if (!Music.ScreenVis) {
                boolean show = false;
                float[] diff = new float[3];

                for (int i = 0; i < 3; i++) {
                    diff[i] = Math.round(mScale[i] * (values[i] - mPrev[i]) * 0.37f);
                    if (Math.abs(diff[i]) > 0) {
                        show = true;
                    }
                    mPrev[i] = values[i];
                }

                long now = android.os.SystemClock.uptimeMillis();
                if (now - mLastGestureTime > 75) {
                    mLastGestureTime = 0;

                    float x = diff[0];
                    float y = diff[1];
                    float z = diff[2];
                    boolean gestX = Math.abs(x) > 3;
                    boolean gestY = Math.abs(y) > 3;

                    if (x >= 2 || x <= -2) {
                        if (x <= -3) {
                            Selector.mBluetoothLeService.TAHTrackPad(TAHble.Down);
                        }
                        if (x >= 3) {
                            Selector.mBluetoothLeService.TAHTrackPad(TAHble.Up);
                        }
                    } else {
                        if (y <= -4) {
                            Selector.mBluetoothLeService.TAHTrackPad(TAHble.Right);
                        }
                        if (y >= 4) {
                            Selector.mBluetoothLeService.TAHTrackPad(TAHble.Left);
                        }
                    }
                    if ((gestX || gestY) && !(gestX && gestY)) {
                        mLastGestureTime = now;
                    }
                }
            }
        }

        private long mLastGestureTime;

        public void onAccuracyChanged(int sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mac, container, false);
        macWindow = getArguments().getString("MAC");
        context = getActivity();
        imgMusic = (ImageView) view.findViewById(R.id.imgmusic);
        imgPresentation = (ImageView) view.findViewById(R.id.imgpresentation);
        if (macWindow.equals("mac")) {
            mSensorManager = (SensorManager) Selector.activity.getSystemService(Context.SENSOR_SERVICE);
        } else {
            imgMusic.setVisibility(View.GONE);
        }
// music button click
        imgMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Selector) getActivity()).changeFragment(new Music());
            }
        });
// presentation button click

        imgPresentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Selector) getActivity()).changeFragment(new Presentation());
            }
        });
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        int mask = 0;
//        mask |= SensorManager.SENSOR_ORIENTATION;
        mask |= SensorManager.SENSOR_ACCELEROMETER;
        if (macWindow.equals("mac")) {
            mSensorManager.registerListener(mListener, mask, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(mListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }


}
