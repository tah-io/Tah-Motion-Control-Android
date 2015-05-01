/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

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
            //connect=02 sleep and wakeup=60 disconnected 80
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

        // For all other profiles, writes the data formatted in HEX.
        //.getvalue get all values including raw value also like hex value of string
        //its better user .getstringvalue

//            //old logic
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }

        //using get string value
        String strdata = characteristic.getStringValue(0);
        intent.putExtra(EXTRA_DATA, new String(strdata));

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
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
        //        System.out.println("bond state=="+device.getBondState());
//        System.out.println("pairing confirmation state=="+device.setPairingConfirmation(true));
//        System.out.println("bond state=="+device.getBondState());
//        System.out.println("pairing confirmation state=="+device.setPairingConfirmation(true));
//        String pind="654321";
//        byte pin[]=pind.getBytes();
//        device.setPin(pin);
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

    // same write method with notification on
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
            if (Swipe == TahConstant.Up) {
                str1 = "0,0,0,256M";
            } else if (Swipe == TahConstant.Down) {
                str1 = "0,0,0,257M";
            } else if (Swipe == TahConstant.Right) {
                str1 = "0,0,0,258M";
            } else if (Swipe == TahConstant.Left) {
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

}
