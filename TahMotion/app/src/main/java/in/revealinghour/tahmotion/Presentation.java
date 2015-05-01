package in.revealinghour.tahmotion;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import bleservice.TahConstant;
import util.SwipeDetector;

/**
 * Created by adityamathur on 29/04/15.
 */
public class Presentation extends Fragment {
    Context context;
    RelativeLayout swipe;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.presentation, container, false);
        swipe = (RelativeLayout) view.findViewById(R.id.swiperelative);
        new SwipeDetector(swipe).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {

            @Override

            public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT) {
                    System.out.println("LEFT TO RIGHT");
                    Selector.mBluetoothLeService.TahKeyPress(TahConstant.KEY_RIGHT_ARROW);
                }
                if (swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
                    System.out.println("RIGHT TO LEFT");
                    Selector.mBluetoothLeService.TahKeyPress(TahConstant.KEY_LEFT_ARROW);
                }
                if (swipeType == SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM) {
                    System.out.println("TOP B");
                    Selector.mBluetoothLeService.TahKeyPress(TahConstant.KEY_DOWN_ARROW);
                }
                if (swipeType == SwipeDetector.SwipeTypeEnum.BOTTOM_TO_TOP) {
                    System.out.println("B TO T");
                    Selector.mBluetoothLeService.TahKeyPress(TahConstant.KEY_UP_ARROW);
                }

            }
        });
        context = getActivity();
        return view;

    }
}
