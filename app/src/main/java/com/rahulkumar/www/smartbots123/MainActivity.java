package com.rahulkumar.www.smartbots123;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {
    //All Websocket vaRIABLES and functions are commented... - For Archisman Sarkar
    // (See the functions first, it has comments that might be helpful...)
    private int backPressedCount = 0;
    private ProgressDialog pd;
    private ImageView home_bulb_image;
    private ImageView home_fan_image;
    private ToggleButton home_BTToggle;
    private ImageButton home_fan_speed1, home_fan_speed2, home_fan_speed3, home_fan_speed4, home_fan_speed5;
    private Handler handler = new Handler();

    // Hard Code usename , address and password
    //private String username = null, password = null, address = null;
    //private String[] data_parsed = null;
    // private WebSocketConnection mConnection = new WebSocketConnection();
    private String BULB_STATE = null;
    private String FAN_STATE = null;

    private boolean webSocketConnected = false;

    // BLe variables
    public static boolean BLEConnected = false;
    //Alive Sensing Code
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private final int SIGNAL_THRESHHOLD = -85;
    private ScanCallback scanCallback;
    private Handler scanHandler = new Handler();
    private Timer timer = new Timer();
    private int signal_strength[] = {
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10,
            SIGNAL_THRESHHOLD + 10};
    private int sig_st_count = 0;
    private boolean checkBLE = false;
    private boolean mScanning = false;
    private long SCAN_PERIOD = 1000, UPDATE_PERIOD = 1050;
    private final String TAG = "MainActivity";
    private static final int REQUEST_APP_SETTINGS = 168;
    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
            /* ETC.. */
    };
    private int DEVICE_STATE_TIMEOUT = 5000;
    private String FAN_PREV_STATE = "FAN_OFF";
    private boolean takeAliveState = true;
    private int DISCONNECT_STRENGTH = -100;
    private int NO_OF_SIGNALS = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pd = new ProgressDialog(this);
        pd.show();
        pd.setMessage("Receiving Device States!! Please Wait...");
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                if (pd.isShowing())
                    UnAuthenticateUser("Cannot Receieve Device States!! Please check your network connection and try again...");
            }
        }, DEVICE_STATE_TIMEOUT);

//            LoginToServer();

        home_bulb_image = (ImageView) findViewById(R.id.home_bulb_image);

        home_fan_image = (ImageView) findViewById(R.id.home_fan_image);
        home_fan_speed1 = (ImageButton) findViewById(R.id.home_fan_speed1);
        home_fan_speed2 = (ImageButton) findViewById(R.id.home_fan_speed2);
        home_fan_speed3 = (ImageButton) findViewById(R.id.home_fan_speed3);
        home_fan_speed4 = (ImageButton) findViewById(R.id.home_fan_speed4);
        home_fan_speed5 = (ImageButton) findViewById(R.id.home_fan_speed5);


        home_BTToggle = (ToggleButton) findViewById(R.id.home_BTToggle);


        home_bulb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    if (BULB_STATE == "TL_ON")
                        changeBulb(false, true);
                    else
                        changeBulb(true, true);
                } else {
                    if (!isWiFiON()) {
                        try {
                            enableWifi();
                        } catch (Exception e) {
                            Log.d(TAG, "WebSocket Closed!!!");
                        }
                    } else {
                        AttemptLogin();
                    }
                }
            }
        });

        home_fan_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FAN_STATE == "FAN_OFF") {
                    if (webSocketConnected) {
                        if (FAN_PREV_STATE.equals("FAN_ON_1"))
                            changeFanSpeed(1, true);
                        else if (FAN_PREV_STATE.equals("FAN_ON_2"))
                            changeFanSpeed(2, true);
                        else if (FAN_PREV_STATE.equals("FAN_ON_3"))
                            changeFanSpeed(3, true);
                        else if (FAN_PREV_STATE.equals("FAN_ON_4"))
                            changeFanSpeed(4, true);
                        else if (FAN_PREV_STATE.equals("FAN_ON_5"))
                            changeFanSpeed(5, true);
                        else
                            changeFanSpeed(3, true);
                    } else {
                        if (!isWiFiON()) {
                            try {
                                enableWifi();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        } else {
                            AttemptLogin();
                        }
                    }
                } else {
                    if (webSocketConnected) {
                        changeFanSpeed(0, true);
                    } else {
                        if (!isWiFiON()) {
                            try {
                                enableWifi();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        } else {
                            AttemptLogin();
                        }
                    }
                }
            }
        });
        home_fan_speed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(1, true);
                } else {
                    if (!isWiFiON()) {
                        try {
                            enableWifi();
                        } catch (Exception e) {
                            Log.d(TAG, "WebSocket Closed!!!");
                        }
                    } else {
                        AttemptLogin();
                    }
                }
            }
        });
        home_fan_speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(2, true);
                } else {
                    if (!isWiFiON()) {
                        try {
                            enableWifi();
                        } catch (Exception e) {
                            Log.d(TAG, "WebSocket Closed!!!");
                        }
                    } else {
                        AttemptLogin();
                    }
                }
            }
        });
        home_fan_speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(3, true);
                } else {
                    if (!isWiFiON()) {
                        try {
                            enableWifi();
                        } catch (Exception e) {
                            Log.d(TAG, "WebSocket Closed!!!");
                        }
                    } else {
                        AttemptLogin();
                    }
                }
            }
        });
        home_fan_speed4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(4, true);
                } else {
                    if (!isWiFiON()) {
                        try {
                            enableWifi();
                        } catch (Exception e) {
                            Log.d(TAG, "WebSocket Closed!!!");
                        }
                    } else {
                        AttemptLogin();
                    }
                }
            }
        });
        home_fan_speed5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(5, true);
                } else {
                    if (!isWiFiON()) {
                        try {
                            enableWifi();
                        } catch (Exception e) {
                            Log.d(TAG, "WebSocket Closed!!!");
                        }
                    } else {
                        AttemptLogin();
                    }
                }
            }
        });
    }


    public void changeBulb(boolean state, boolean controlling) {
        if (controlling == true) {
            String BULB_STATE_TEMP = "";
            if (state == false)
                BULB_STATE_TEMP = "TL_OFF";
            else
                BULB_STATE_TEMP = "TL_ON";

            //mConnection.sendTextMessage(encryption("CTRL-" + username + "-" + BULB_STATE_TEMP + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));
        } else if (state == true) {
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            BULB_STATE = "TL_ON";
        } else if (state == false) {
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            BULB_STATE = "TL_OFF";
        }
    }

    public void changeFanSpeed(int speed, boolean controlling) {
        if (controlling == true) {
            String FAN_STATE_TEMP = "";
            if (speed == 0) {
                FAN_STATE_TEMP = "FAN_OFF";
            } else if (speed == 1) {
                FAN_STATE_TEMP = "FAN_ON_1";
            } else if (speed == 2) {
                FAN_STATE_TEMP = "FAN_ON_2";
            } else if (speed == 3) {
                FAN_STATE_TEMP = "FAN_ON_3";
            } else if (speed == 4) {
                FAN_STATE_TEMP = "FAN_ON_4";
            } else if (speed == 5) {
                FAN_STATE_TEMP = "FAN_ON_5";
            }
            //mConnection.sendTextMessage(encryption("CTRL-" + username + "-" + BULB_STATE + "-" + FAN_STATE_TEMP + "-" + transfer_session, shared_aes_encryption_key));
        } else if (speed == 0) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan);

            home_fan_speed1.setBackgroundResource(R.color.fan_low);
            home_fan_speed2.setBackgroundResource(R.color.fan_low);
            home_fan_speed3.setBackgroundResource(R.color.fan_low);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_OFF";
        } else if (speed == 1) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan1);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_low);
            home_fan_speed3.setBackgroundResource(R.color.fan_low);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_1";
        } else if (speed == 2) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan2);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_low);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_2";
        } else if (speed == 3) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan3);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_high3);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_3";
        } else if (speed == 4) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan4);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_high3);
            home_fan_speed4.setBackgroundResource(R.color.fan_high4);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_4";
        } else if (speed == 5) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan5);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_high3);
            home_fan_speed4.setBackgroundResource(R.color.fan_high4);
            home_fan_speed5.setBackgroundResource(R.color.fan_high5);

            FAN_STATE = "FAN_ON_5";
        }
    }

    public boolean isWiFiON() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            return false;
        }
        return true;
    }

    public boolean isBTON() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            return true;
        }
        return false;
    }

    public void enableBT() {
        if (!isBTON()) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.enable();
        }
    } //Do nothing if BT is enabled

    public void enableWifi() {
        if (!isWiFiON()) {
            //mConnection.disconnect();
            Toast.makeText(this, "Connecting WiFi!!", Toast.LENGTH_SHORT).show();
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
        }
    }   //Do nothing if WiFi is enabled

//    public void LoginToServer() {
//        final String wsuri = "ws://10.124.195.9:80";
//
//        try {
//            mConnection.connect(wsuri, new WebSocketConnectionHandler() {
//
//                @Override
//                public void onOpen() {
//                    webSocketConnected = true;
//
//                    shared_aes_encryption_key = shared_key_generator();
//                    try {
//                        mConnection.sendTextMessage(encryptCrypto("LOGI-" + username + "-" + password + "-" + shared_aes_encryption_key));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (InvalidKeySpecException e) {
//                        e.printStackTrace();
//                    } catch (NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                    } catch (InvalidKeyException e) {
//                        e.printStackTrace();
//                    } catch (BadPaddingException e) {
//                        e.printStackTrace();
//                    } catch (IllegalBlockSizeException e) {
//                        e.printStackTrace();
//                    } catch (NoSuchPaddingException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//
//                        mConnection.sendTextMessage(encryptCrypto("ENQ-" + username + "-" + shared_aes_encryption_key));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (InvalidKeySpecException e) {
//                        e.printStackTrace();
//                    } catch (NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                    } catch (InvalidKeyException e) {
//                        e.printStackTrace();
//                    } catch (BadPaddingException e) {
//                        e.printStackTrace();
//                    } catch (IllegalBlockSizeException e) {
//                        e.printStackTrace();
//                    } catch (NoSuchPaddingException e) {
//                        e.printStackTrace();
//                    }
//
//                    webSocketConnected = true;
//                }
//
//                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//                @Override
//                public void onTextMessage(String payload) {
//                    String decrypted_data = decryption(payload, shared_aes_encryption_key);
//                    if (decrypted_data != null) {
//                        data_parsed = decrypted_data.split("-");
//                        int size = data_parsed.length;
//
//                        if (data_parsed[0].equals(String.valueOf("VERIFY"))) {
//                            if (data_parsed[1].equals(String.valueOf("True"))) {
//                                if ((size > 2) && data_parsed[2].equals(String.valueOf("STATUS"))) {
//                                    if (data_parsed[3].equals(String.valueOf("TL_ON"))) {
//                                        changeBulb(true, false);
//                                    } else if (data_parsed[3].equals(String.valueOf("TL_OFF"))) {
//                                        changeBulb(false, false);
//                                    }
//
//
//                                    if (data_parsed[4].equals("FAN_OFF")) {
//                                        changeFanSpeed(0, false);
//                                    } else if (data_parsed[4].equals("FAN_ON_1")) {
//                                        changeFanSpeed(1, false);
//                                    } else if (data_parsed[4].equals("FAN_ON_2")) {
//                                        changeFanSpeed(2, false);
//                                    } else if (data_parsed[4].equals("FAN_ON_3")) {
//                                        changeFanSpeed(3, false);
//                                    } else if (data_parsed[4].equals("FAN_ON_4")) {
//                                        changeFanSpeed(4, false);
//                                    } else if (data_parsed[4].equals("FAN_ON_5")) {
//                                        changeFanSpeed(5, false);
//                                    }
//                                    if (takeAliveState == true)
//                                        FAN_PREV_STATE = data_parsed[4];
//
//                                } else if ((size > 2) && data_parsed[2].equals("BLEMAC")) {
//                                    address = data_parsed[3].substring(0, 17);
//                                    sharedPreferences = getSharedPreferences(USER_INFO, Context.MODE_PRIVATE);
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    editor.putString(ADDRESS, address);
//                                    editor.apply();
//                                    mConnection.sendTextMessage(encryption("sessionRequest-" + username, shared_aes_encryption_key));
//                                    scanBLE(address);
//                                }
//
//                            } else if (new String("False").equals(data_parsed[1])) {
//                                /**-- To-Do --*/
//                                UnAuthenticateUser("Username or Password incorrect!!");
//                            }
//                        } else if (data_parsed[0].equals(String.valueOf("NOTIFY"))) { // User already SignedIn
//                            //Do Nothing
//                        } else if (data_parsed[0].equals("session")) {
//                            transfer_session = data_parsed[1];
//                            mConnection.sendTextMessage(encryption("STATUS-" + username + "-" + transfer_session, shared_aes_encryption_key));
//                        } else if (data_parsed[0].equals("ERROR")) {
//                            //Toast.makeText(MainActivity.this, data_parsed[1], Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//
//                @Override
//                public void onClose(int code, String reason) {
//                    /** To-Do */
//                    mConnection.disconnect();
//                    webSocketConnected = false;
//                    UnAuthenticateUser("WiFi disconnected!!!");
//                }
//            });
//        } catch (WebSocketException e) {
//            UnAuthenticateUser("Cannot connect to server, please check network connectivity!!");
//        }
//    }

    private void UnAuthenticateUser(String text){
        //Closes app , takes to home screen
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void AttemptLogin() {
        //mConnection = new WebSocketConnection();
        pd = new ProgressDialog(MainActivity.this);
        pd.show();
        //LoginToServer();
        pd.setMessage("Receiving Device States!! Please Wait...");
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pd.isShowing()) {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Cannot login to server, check connectivity", Toast.LENGTH_SHORT).show();
                }
            }
        }, 8000);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean checkBLE(final String mDeviceAddress) {
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,
                    "BLUETOOTH_LE not supported in this device!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Get BluetoothAdapter and BluetoothLeScanner.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "bluetoothManager.getAdapter()==null",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            enableBT();
            return false;
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result.equals(null)) {
                    signal_strength[sig_st_count] = DISCONNECT_STRENGTH;
                    sig_st_count++;
                    if (sig_st_count == NO_OF_SIGNALS)
                        sig_st_count = 0;
                } else if (result.getDevice().getAddress().equals(mDeviceAddress)) {
                    signal_strength[sig_st_count] = result.getRssi();
                    sig_st_count++;
                    if (sig_st_count == NO_OF_SIGNALS)
                        sig_st_count = 0;
                } else {
                    signal_strength[sig_st_count] = DISCONNECT_STRENGTH;
                    sig_st_count++;
                    if (sig_st_count == NO_OF_SIGNALS)
                        sig_st_count = 0;
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (ScanResult result : results) {
                    if (result.equals(null)) {
                        signal_strength[sig_st_count] = DISCONNECT_STRENGTH;
                        sig_st_count++;
                        if (sig_st_count == NO_OF_SIGNALS)
                            sig_st_count = 0;
                    } else if (result.getDevice().getAddress().equals(mDeviceAddress)) {
                        signal_strength[sig_st_count] = result.getRssi();
                        sig_st_count++;
                        if (sig_st_count == NO_OF_SIGNALS)
                            sig_st_count = 0;
                    } else {
                        signal_strength[sig_st_count] = DISCONNECT_STRENGTH;
                        sig_st_count++;
                        if (sig_st_count == NO_OF_SIGNALS)
                            sig_st_count = 0;
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                scanLeDevice(true);
                signal_strength[sig_st_count] = DISCONNECT_STRENGTH;
                sig_st_count++;
                if (sig_st_count == NO_OF_SIGNALS)
                    sig_st_count = 0;
            }
        };

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            scanHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(scanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothLeScanner.startScan(scanCallback);

        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(scanCallback);
        }

        return avSignal(signal_strength);
    }

    private int avSignal(int[] signal_strength) {
        int av = 0;
        for (int i = 0; i < NO_OF_SIGNALS; i++) {
            av += signal_strength[i];
        }
        return (av / NO_OF_SIGNALS);
    }

    //Not checked
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scanBLE(String address) {
        //Checking Permission
        if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            goToSettings();
                            break;

                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("We need permission to access your Location in order to enable SmartSensing, please give the permissions by selecting YES")
                    .setPositiveButton("Yes", dialogClickListener);

            AlertDialog alert = builder.create();
            alert.show();

        }

        while (checkBLE == false) {
            checkBLE = checkBLE(address);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isBTON()) {
                            scanLeDevice(true);

                            if (avSignal(signal_strength) < SIGNAL_THRESHHOLD) {
                                takeAliveState = false;
                                if (BLEConnected == true) {
                                    //mConnection.sendTextMessage(encryption("CTRL-" + username + "-" + "TL_OFF" + "-" + "FAN_OFF" + "-" + transfer_session, shared_aes_encryption_key));
                                }
                                BLEConnected = false;
                                home_BTToggle.setChecked(false);
                            } else {
                                takeAliveState = true;
                                if (BLEConnected == false) {
                                    // mConnection.sendTextMessage(encryption("CTRL-" + username + "-" + "TL_ON" + "-" + FAN_PREV_STATE + "-" + transfer_session, shared_aes_encryption_key));
                                }
                                BLEConnected = true;
                                home_BTToggle.setChecked(true);
                            }
                        }
                    }
                });
            }
        }, 0, UPDATE_PERIOD);
    }

    // Check scanCallback for signal_strength
    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                return false;
        return true;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_APP_SETTINGS) {
            if (hasPermissions(requiredPermissions)) {
                Toast.makeText(this, "All permissions granted! Thank You", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions not granted!! please grant all permissions...", Toast.LENGTH_LONG).show();
                goToSettings();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        if (isBTON()) {
            if (checkBLE == true)
                scanLeDevice(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
        if (isBTON()) {
            if (checkBLE == true)
                scanLeDevice(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedCount % 6 == 0) {
            Toast.makeText(this, "Use Home Button to exit!!", Toast.LENGTH_SHORT).show();
            backPressedCount++;
        } else if (backPressedCount > 10000) {
            backPressedCount = 0;
        }
    }
}
