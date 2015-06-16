

package bleservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import util.Constant;
/**
 * //
 //
 //  Created by Shailesh on 29/04/2015.
 //  Copyright (c) 2014 www.tah.io
 //  All rights reserved.

 */

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class TAHble extends Service {
    //////////////////ASCII CONSTANT/////////////////
    //key press
    public static int KEY_UP_ARROW = 218;
    public static int KEY_DOWN_ARROW = 217;
    public static int KEY_LEFT_ARROW = 216;
    public static int KEY_RIGHT_ARROW = 215;

    // Trakpad
    public static int Up = 256;
    public static int Down = 257;
    public static int Right = 258;
    public static int Left = 259;

    // Volume
    public static int VolumeUp = 260;
    public static int VolumeDown = 261;
// Tah Keyboard Modifiers

    public static int A = 65;
    public static int a = 97;
    public static int B = 66;
    public static int b = 98;
    public static int C = 67;
    public static int c = 99;
    public static int D = 68;
    public static int d = 100;
    public static int E = 69;
    public static int e = 101;
    public static int F = 70;
    public static int f = 102;
    public static int G = 71;
    public static int g = 103;
    public static int H = 72;
    public static int h = 104;
    public static int I = 73;
    public static int i = 105;
    public static int J = 74;
    public static int j = 106;
    public static int K = 75;
    public static int k = 107;
    public static int L = 76;
    public static int l = 108;
    public static int M = 77;
    public static int m = 109;
    public static int N = 78;
    public static int n = 110;
    public static int O = 79;
    public static int o = 111;
    public static int P = 80;
    public static int p = 112;
    public static int Q = 81;
    public static int q = 113;
    public static int R = 82;
    public static int r = 114;
    public static int S = 83;
    public static int s = 115;
    public static int T = 84;
    public static int t = 116;
    public static int U = 85;
    public static int u = 117;
    public static int V = 86;
    public static int v = 118;
    public static int W = 87;
    public static int w = 119;
    public static int X = 88;
    public static int x = 120;
    public static int Y = 89;
    public static int y = 121;
    public static int Z = 90;
    public static int z = 122;


    public static int KEY_LEFT_CTRL = 128;
    public static int KEY_LEFT_SHIFT = 129;
    public static int KEY_LEFT_ALT = 130;
    public static int KEY_LEFT_GUI = 131;
    public static int KEY_RIGHT_CTRL = 132;
    public static int KEY_RIGHT_SHIFT = 133;
    public static int KEY_RIGHT_ALT = 134;
    public static int KEY_RIGHT_GUI = 135;
    public static int KEY_SPACE = 32;
    public static int KEY_BACKSPACE = 178;
    public static int KEY_TAB = 179;
    public static int KEY_RETURN = 176;
    public static int KEY_ESC = 177;
    public static int KEY_INSERT = 209;
    public static int KEY_DELETE = 212;
    public static int KEY_PAGE_UP = 211;
    public static int KEY_PAGE_DOWN = 214;
    public static int KEY_HOME = 210;
    public static int KEY_END = 213;
    public static int KEY_CAPS_LOCK = 193;
    public static int KEY_F1 = 194;
    public static int KEY_F2 = 195;
    public static int KEY_F3 = 196;
    public static int KEY_F4 = 197;
    public static int KEY_F5 = 198;
    public static int KEY_F6 = 199;
    public static int KEY_F7 = 200;
    public static int KEY_F8 = 201;
    public static int KEY_F9 = 202;
    public static int KEY_F10 = 203;
    public static int KEY_F11 = 204;
    public static int KEY_F12 = 205;

    private final static String TAG = TAHble.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public static boolean sleepWakeup = false;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //status==connect=02 sleep and wakeup=60 disconnected=80
            if (status == 8) {
                sleepWakeup = true;
            } else {
                sleepWakeup = false;
            }
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }


    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        //using get string value
        String strdata = characteristic.getStringValue(0);
        intent.putExtra(EXTRA_DATA, new String(strdata));

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public TAHble getService() {
            return TAHble.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
// Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }
// We want to directly connect to the device, so we are setting the autoConnect
// parameter to false.

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

///////////////////////////////Tah pin control/////////////////////////////////////

    // write data without notification
    public boolean writeCharacteristic(String servicuid, String characteruid, String data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }


        BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(servicuid));
        BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(characteruid));

        characteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(characteristic);

    }

    //  write method with notification
    public boolean writeCharacteristicWithRes(String servicuid, String characteruid, String data, boolean notification) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }


        BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(servicuid));
        BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(characteruid));
        mBluetoothGatt.setCharacteristicNotification(characteristic, notification);
        characteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(characteristic);

    }

    //Tah digital write
    public boolean TahDigitalWrite(int Pin, int Value, boolean Notification) {
        try {
            String str1 = "0," + Pin + "," + Value + "R";
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            if (Notification) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, Notification);
            }
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    //Tah Analog  write
    public boolean TahAnalogWrite(int Pin, int Value, boolean Notification) {
        try {
            String str1 = "1," + Pin + "," + Value + "R";
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            if (Notification) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, Notification);
            }
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    //Tah servo  write
    public boolean TahServoWrite(int Pin, int Value, boolean Notification) {
        try {
            String str1 = "2," + Pin + "," + Value + "R";
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            if (Notification) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, Notification);
            }
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    //////////////// TAH Setting Control  /////////////////////////////

    //Tah Security type true=open false=secure
    public boolean setTAHSecurityType(boolean OpenSecure) {
        try {
            String str1;
            if (OpenSecure) {
                str1 = "AT+TYPE0";
            } else {
                str1 = "AT+TYPE2";
            }
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Set Tah Name
    public boolean setTahName(String DeviceName) {
        try {
            if (DeviceName == null && DeviceName.equals("")) {
                return false;
            } else {
                BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
                BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
                characteristic.setValue("AT+NAME" + DeviceName);
                return mBluetoothGatt.writeCharacteristic(characteristic);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //set Tah password if device security is open then this function set security mode to SECURE automatically
    public boolean setTahPassword(String Password) {
        try {
            if (Password == null && Password.equals("")) {
                return false;
            } else {
                setTAHSecurityType(false);
                Thread.sleep(100);
                BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
                BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
                characteristic.setValue("AT+PASS" + Password);
                return mBluetoothGatt.writeCharacteristic(characteristic);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Reset Tah settings
    public boolean updateSettings() {
        try {
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            characteristic.setValue("AT+RESET");
            return mBluetoothGatt.writeCharacteristic(characteristic);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    ///////////Tah Keyboard Control////////////////
//Tah key  press
    public boolean TahKeyPress(int Key) {
        try {
            String str1 = "0,0,0," + Key + "M";
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    //////////////// TAH Mouse Control ///////////////////
//Tah mouse move
    public boolean TahMouseMove(float XaXis, float YaXis, float Scroll) {
        try {
            String str1 = XaXis + "," + YaXis + "," + Scroll + "," + "0" + "M";
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
/////////////TAH track pad for MAC only i.e (nothing but motion)////////////////////////

    public boolean TAHTrackPad(int Swipe) {
        try {
            String str1 = "";
            if (Swipe == Up) {
                str1 = "0,0,0,256M";
            } else if (Swipe == Down) {
                str1 = "0,0,0,257M";
            } else if (Swipe == Right) {
                str1 = "0,0,0,258M";
            } else if (Swipe == Left) {
                str1 = "0,0,0,259M";
            }
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.ServiceUid));
            BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID.fromString(Constant.CharaUid));
            characteristic.setValue(str1);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
////////////////////////// We need to add following code in Activity or Fragment to get the broadcast data () i.e broadcast receiver /////////////////////////
//////////intent filter////////////
//    private static IntentFilter makeGattUpdateIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(TAHble.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(TAHble.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(TAHble.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(TAHble.ACTION_DATA_AVAILABLE);

//        return intentFilter;
//    }

    ////////////Receiver here//////////
//    // Handles various events fired by the Service.
//    // ACTION_GATT_CONNECTED: connected to a GATT server.
//    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
//    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
//    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
//    //                        or notification operations.
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, final Intent intent) {
//            final String action = intent.getAction();
//            if (TAHble.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(mConnected);
//            } else if (TAHble.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                if (TAHble.sleepWakeup) {
//                    updateConnectionState(mConnected);
//                }
//                //invalidateOptionsMenu();
//            } else if (TAHble.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                // Show all the supported services and characteristics on the user interface.
//                // displayGattServices(mBluetoothLeService.getSupportedGattServices());
//            } else if (TAHble.ACTION_DATA_AVAILABLE.equals(action)) {
//                // final String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toString();
//
//            }
//        }
//    };
}
