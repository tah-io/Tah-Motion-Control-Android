package in.revealinghour.tahmotion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by shail on 29/04/15.
 */

public class Music extends Fragment {

    ImageView mReverse, mNext, mPlayPause;
    Context context;
    //sensor
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_cantrol, container, false);
        context = getActivity();
//        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
//
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        mReverse = (ImageView) view.findViewById(R.id.imgreverse);
        mNext = (ImageView) view.findViewById(R.id.imgforword);
        mPlayPause = (ImageView) view.findViewById(R.id.imgplaypause);
        //play pause music
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //previous song
        mReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //next song
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;

    }


}
