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
    private int backPressedCount = 0;

    private ImageView home_bulb_image;
    private ImageView home_fan_image;

    private ImageButton home_fan_speed1, home_fan_speed2, home_fan_speed3, home_fan_speed4, home_fan_speed5;
  //  private final WebSocketConnection mConnection = new WebSocketConnection();

    private String BULB_STATE = "TL_OFF";
    private String FAN_STATE = "FAN_OFF";

    private boolean webSocketConnected = false;

    private String FAN_PREV_STATE = "FAN_OFF";
    private String[] parsed_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // start();

        home_bulb_image = (ImageView) findViewById(R.id.home_bulb_image);

        home_fan_image = (ImageView) findViewById(R.id.home_fan_image);
        home_fan_speed1 = (ImageButton) findViewById(R.id.home_fan_speed1);
        home_fan_speed2 = (ImageButton) findViewById(R.id.home_fan_speed2);
        home_fan_speed3 = (ImageButton) findViewById(R.id.home_fan_speed3);
        home_fan_speed4 = (ImageButton) findViewById(R.id.home_fan_speed4);
        home_fan_speed5 = (ImageButton) findViewById(R.id.home_fan_speed5);

        home_bulb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    if (BULB_STATE == "TL_ON")
                        changeBulb(false, true);
                    else
                        changeBulb(true, true);
                } else {

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

                    }
                } else {
                    if (webSocketConnected) {
                        changeFanSpeed(0, true);
                    } else {

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

                }
            }
        });
        home_fan_speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(2, true);
                } else {

                }
            }
        });
        home_fan_speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(3, true);
                } else {

                }
            }
        });
        home_fan_speed4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(4, true);
                } else {

                }
            }
        });
        home_fan_speed5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketConnected) {
                    changeFanSpeed(5, true);
                } else {

                }
            }
        });
    }


    public void changeBulb(boolean state, boolean controlling) {
        if (controlling == true) {
            String BULB_STATE_TEMP = "";
            if (state == false) {
                BULB_STATE_TEMP = "TL_OFF";
                BULB_STATE = "TL_OFF";
                //mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            }
            else {
                BULB_STATE_TEMP = "TL_ON";
                BULB_STATE = "TL_ON";
                //mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            }

        } if (state == true) {
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            BULB_STATE = "TL_ON";
        } else if (state == false) {
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            BULB_STATE = "TL_OFF";
        }
    }

    public void changeFanSpeed(int speed, boolean controlling) {
        if (controlling == true) {
            if (speed == 0) {
                FAN_PREV_STATE = "FAN_OFF";
                FAN_STATE = "FAN_OFF";
              //  mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            } else if (speed == 1) {
                FAN_PREV_STATE = "FAN_ON_1";
                FAN_STATE = "FAN_ON_1";
            //    mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            } else if (speed == 2) {
                FAN_PREV_STATE = "FAN_ON_2";
                FAN_STATE = "FAN_ON_2";
          //      mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            } else if (speed == 3) {
                FAN_PREV_STATE = "FAN_ON_3";
                FAN_STATE = "FAN_ON_3";
        //        mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            } else if (speed == 4) {
                FAN_PREV_STATE = "FAN_ON_4";
                FAN_STATE = "FAN_ON_4";
     //           mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            } else if (speed == 5) {
                FAN_PREV_STATE = "FAN_ON_5";
                FAN_STATE = "FAN_ON_5";
      //          mConnection.sendTextMessage("Web-CTRL-"+BULB_STATE+"-"+FAN_STATE);
            }
        } if (speed == 0) {
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

    @Override
    public void onBackPressed() {
        if (backPressedCount % 6 == 0) {
            Toast.makeText(this, "Use Home Button to exit!!", Toast.LENGTH_SHORT).show();
            backPressedCount++;
        } else if (backPressedCount > 10000) {
            backPressedCount = 0;
        }
    }

//    public void start() {
//
//        final String wsuri = "ws://52.14.147.118:9090";
//
//        try {
//            mConnection.connect(wsuri, new WebSocketConnectionHandler() {
//
//                @Override
//                public void onOpen() {
//                    webSocketConnected = true;
//                    mConnection.sendTextMessage("Android-LOGIN");
//                }
//
//                @Override
//                public void onTextMessage(String payload) {
//                    parsed_data = payload.split("-");
//                    int size = parsed_data.length;
//                    if (size != 0){
//                        if (new String("CTRL").equals(parsed_data[0])) {
//                            if (new String("TL_OFF").equals(parsed_data[1])) changeBulb(false, false);
//                            else if (new String("TL_ON").equals(parsed_data[1])) changeBulb(true, false);
//                            if (new String("FAN_OFF").equals(parsed_data[2])) changeFanSpeed(0, false);
//                            else if (new String("FAN_ON_1").equals(parsed_data[2])) changeFanSpeed(1, false);
//                            else if (new String("FAN_ON_2").equals(parsed_data[2])) changeFanSpeed(2, false);
//                            else if (new String("FAN_ON_3").equals(parsed_data[2])) changeFanSpeed(3, false);
//                            else if (new String("FAN_ON_4").equals(parsed_data[2])) changeFanSpeed(4, false);
//                            else if (new String("FAN_ON_5").equals(parsed_data[2])) changeFanSpeed(5, false);
//                        }
//                    }
//                }
//
//                @Override
//                public void onClose(int code, String reason) {
//                    webSocketConnected = false;
//                }
//            });
//        } catch (WebSocketException e) {
//
//        }
//    }
}
