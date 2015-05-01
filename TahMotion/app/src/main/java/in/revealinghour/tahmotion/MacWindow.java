package in.revealinghour.tahmotion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by shail on 12/03/15.
 */
//http://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
//tilt
//http://stackoverflow.com/questions/20088549/how-to-detect-left-and-right-tilt-of-an-android-device-mounted-with-an-accelerom
//https://github.com/josejuansanchez/android-sensors-overview/blob/master/README.old.md
public class MacWindow extends Fragment implements SensorEventListener {

    @Nullable
    Context context;
    ImageView imgMusic, imgPresentation;
    public static boolean screenVisible = true;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 500;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mac, container, false);
        context = getActivity();
        imgMusic = (ImageView) view.findViewById(R.id.imgmusic);
        imgPresentation = (ImageView) view.findViewById(R.id.imgpresentation);
        mSensorManager = (SensorManager) Selector.activity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        mSensorManager.registerListener(this, mAccelerometer,

                SensorManager.SENSOR_DELAY_NORMAL);
        screenVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        screenVisible = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        screenVisible = false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        screenVisible = false;


    }


    @Override
    public void onSensorChanged(SensorEvent event) {

//        float[] values = event.values;
//
//        // Movement
//        float x = values[0];
//        float y = values[1];
//        float z = values[2];
//
//        /*float accelationSquareRoot = (x * x + y * y + z * z)
//                / (SensorManager.AXIS_X * SensorManager.AXIS_X);*/
//
//        //Log.d("Shakes","X: "+x+"  Y: "+y+"  Z: "+z);
//
//        long actualTime = System.currentTimeMillis();
//        if ((actualTime - lastUpdate) > 100) {
//            long diffTime = (actualTime - lastUpdate);
//            lastUpdate = actualTime;
//
//
//
//            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
//
//            if (speed > SHAKE_THRESHOLD) {
//                //Log.d("sensor", "shake detected w/ speed: " + speed);
//                //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
//                if (Round(x, 4) > 8.0000) {
//                    System.out.println("LEFTTTTTTTT");
//
//
//                } else if (Round(x, 4) < -8.0000) {
//                    System.out.println("RIGHTTTTTTTT");
//
//                } else if (Round(z, 4) < -0.0) {
//                    System.out.println("UPPPPPPPPPP");
//
//
//                } else if (Round(y, 4) < 1.0) {
//                    System.out.println("DOWNNNNNNN");
//
//                }
//
//            }
//            last_x = x;
//            last_y = y;
//            last_z = z;
//        }
    }


    //        float x = event.values[0];
//        float y = event.values[1];
//        float z = event.values[2];
//        long curTime = System.currentTimeMillis();
//        // only allow one update every 100ms.
//        if ((curTime - lastUpdate) > 100) {
//            long diffTime = (curTime - lastUpdate);
//            lastUpdate = curTime;
//
//           // System.out.println("deltaXzzzz==" + Math.round(z));
//
//
//
//            float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
//
//            // Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
//            if (speed > SHAKE_THRESHOLD) {
//                //Log.d("sensor", "shake detected w/ speed: " + speed);
//                //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
//                System.out.println("deltaXzzzz==" + Math.round(x));
//
//                if(Round(x,4)>10.0000){
//
//                    Toast.makeText(Selector.activity, "Right shake detected", Toast.LENGTH_SHORT).show();
//                }
//                else if(Round(x,4)<-10.0000){
//
//                    Toast.makeText(Selector.activity, "Left shake detected", Toast.LENGTH_SHORT).show();
//                }
//            }
//            last_x = x;
//            last_y = y;
//            last_z = z;
//        }
//}
//on sensor changed
    public static float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float) tmp / p;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
