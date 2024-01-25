package com.chris.delesteur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

//import kotlinx.coroutines.channels.Receive;

public class main_screen extends AppCompatActivity {

    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    InputStream inputStream;

    Intent intent;
    String macAddress;

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Timer timer = new Timer();

    Timer timer2 = new Timer();

    String elecInfo = "";

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Get the Intent object
        intent = getIntent();
        //Retrieve data from the Bundle
        macAddress = intent.getStringExtra("macAddress");

        System.out.println("++++Mac address ::: " + macAddress);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("++BT Devise :" + bluetoothAdapter.getRemoteDevice(macAddress));
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);


        //CONNECT BLUETOOTH IN SEPARATE THREAD
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(main_screen.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT > 31) {
                        ActivityCompat.requestPermissions(main_screen.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                        return;
                    }
                }

                if (ActivityCompat.checkSelfPermission(main_screen.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT > 31) {
                        ActivityCompat.requestPermissions(main_screen.this,
                                new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
                        return;
                    }
                }

                try {
                    //BLUETOOTH SOCKET FOR THE COMMUNICATION
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    System.out.println("+++ Etat de la conn 1 : " + bluetoothSocket.isConnected());
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();

                    System.out.println("+++ Etat de la conn 2 : " + bluetoothSocket.isConnected());

                    inputStream = bluetoothSocket.getInputStream();
                    inputStream.skip(inputStream.available()); //Make sure to clean data left in the buffer

//                    System.out.println(bluetoothSocket.getInputStream());

                    // Received Data Format
                    // Ex. L1A30xL1M15xL1SON_L2A30...
                    // with x as separator

                    String[] data = new String[1];
                    String[] splicedDataAll;
                    String[] splicedDataInLine;

//                    System.out.println("+++Data existance : "+inputStream.available());

//                    if(inputStream.read() > 0) { //if anny data in serial
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    final int[] byteRead = new int[1];


                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {

                            while (true) {
                                try {
                                    if (!((byteRead[0] = inputStream.read()) != -1)) break;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                byteArrayOutputStream.write(byteRead[0]);
//                                System.out.println("+++byte array : "+byteArrayOutputStream);


                                if (byteRead[0] == 10 || byteRead[0] == 32) {
                                    elecInfo = String.valueOf(byteArrayOutputStream);
                                    byteArrayOutputStream.reset();
                                    break;
                                }

                            }

//                            inputStream.close();

                        }
                    }, 0, 2000);

//                    data[0] = new String(byteArrayOutputStream.toByteArray());
//                    byteArrayOutputStream.close();

//                    System.out.println("++++ Data2 : " + data[0]);
//                    }

                    // SEND MSG OBJECT
                    outputStream = bluetoothSocket.getOutputStream();
                    Log.d("Message", "Conneted to " + bluetoothDevice.getName());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(main_screen.this, "Bluetooth successfully connected", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //********************************************
                    // RECEIVE DATA
//                    inputStream = bluetoothSocket.getInputStream();
//                    inputStream.skip(inputStream.available()); //Make sure to clean data left in the buffer


                } catch (IOException e) {
                    Log.d("Message", "Turn on bluetooth and restart the app ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(main_screen.this, "Please connect to HC-05 Device", Toast.LENGTH_SHORT).show();
                        }
                    });
                    throw new RuntimeException(e);
                }
            }
        }).start();


        // RECEIVE DATA IN DIFFERENT THREAD
        //CONNECT BLUETOOTH IN SEPARATE THREAD
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(main_screen.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT > 31) {
                        ActivityCompat.requestPermissions(main_screen.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                        return;
                    }
                }

                //BLUETOOTH SOCKET FOR THE COMMUNICATION
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

                    // SEND MSG OBJECT
                    outputStream = bluetoothSocket.getOutputStream();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("+++ Etat de la conn 2 : " + bluetoothSocket.isConnected());

                TextView line1_actual = (TextView) findViewById(R.id.line1_actual);
                TextView line1_max = (TextView) findViewById(R.id.line1_max);
                TextView line1_status = (TextView) findViewById(R.id.line1_status);
                Switch switch1 = (Switch) findViewById(R.id.switch1);

                TextView line2_actual = (TextView) findViewById(R.id.line2_actual);
                TextView line2_max = (TextView) findViewById(R.id.line2_max);
                TextView line2_status = (TextView) findViewById(R.id.line2_status);
                Switch switch2 = (Switch) findViewById(R.id.switch2);

                TextView line3_actual = (TextView) findViewById(R.id.line3_actual);
                TextView line3_max = (TextView) findViewById(R.id.line3_max);
                TextView line3_status = (TextView) findViewById(R.id.line3_status);
                Switch switch3 = (Switch) findViewById(R.id.switch3);


                // NOW COMMUNICATION

                timer2.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {

                        System.out.println(elecInfo);



                        String[] splicedDataAll;

                        if(elecInfo.length() < 50 ){

                        }
                        else {
                            splicedDataAll = elecInfo.split("x");
                            System.out.println("index "+splicedDataAll[0]);
                            System.out.println("len : "+elecInfo.length());
//
//
                            int i = 0;
                            for (String spliced : splicedDataAll){
//                                // Data Structure is
//                                // L1A30xL1M15xL1SON

                                if (i==0){
                                    //For Line 1

                                    int L1A_index = splicedDataAll[0].indexOf("L1A");
                                    int L1M_index = splicedDataAll[0].indexOf("L1M");
                                    int L1S_index = splicedDataAll[0].indexOf("L1S");

                                    if(L1A_index != -1 && L1M_index != -1){

                                        System.out.println("split "+spliced);

                                        float A = Float.parseFloat(spliced.substring((L1A_index+3), L1M_index));
                                        float M = Float.parseFloat(spliced.substring((L1M_index+3), L1S_index));
                                        String S = spliced.substring((L1S_index+3));

                                        if(S.equals("0")) {
                                            S = "OFF";
                                        } else {
                                            S="ON";
                                        }

                                        String finalS = S;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                line1_actual.setText(""+A+" A");
                                                line1_max.setText(""+M+" A");
                                                line1_status.setText(finalS);

//                                                if(finalS.equals("OFF")){
//                                                    switch1.setChecked(false);
//                                                } else switch1.setChecked(true);

                                                switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                                                        if (isChecked){
                                                            System.out.println("S1 isChecked : "+isChecked);
                                                            switch1.setChecked(true);

                                                            String str = "S11";
                                                            byte[] bytes = str.getBytes();
                                                            try {
//                                                                outputStream.write(bytes);
                                                                outputStream.write(52);
                                                            } catch (IOException e) {
                                                                throw new RuntimeException(e);
                                                            }
                                                        } else ;//switch1.setChecked(false);
                                                    }
                                                });
                                            }
                                        });

                                    }

                                }

                                if (i==1){
                                    //For Line 2
                                    int L2A_index = splicedDataAll[1].indexOf("L2A");
                                    int L2M_index = splicedDataAll[1].indexOf("L2M");
                                    int L2S_index = splicedDataAll[1].indexOf("L2S");

                                    if(L2A_index != -1 && L2M_index != -1){

                                        System.out.println("split "+spliced);

                                        float A = Float.parseFloat(spliced.substring((L2A_index+3), L2M_index));
                                        float M = Float.parseFloat(spliced.substring((L2M_index+3), L2S_index));
                                        String S = spliced.substring((L2S_index+3));

                                        if(S.equals("0")) {
                                            S = "OFF";
                                        } else {
                                            S="ON";
                                        }

                                        String finalS = S;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                line2_actual.setText(""+A+" A");
                                                line2_max.setText(""+M+" A");
                                                line2_status.setText(finalS);

                                                if(finalS.equals("OFF")){
                                                    switch2.setChecked(false);
                                                } else switch2.setChecked(true);
                                            }
                                        });

                                    }
                                }

                                if (i==2){
                                    //For Line 3
                                    int L3A_index = splicedDataAll[2].indexOf("L3A");
                                    int L3M_index = splicedDataAll[2].indexOf("L3M");
                                    int L3S_index = splicedDataAll[2].indexOf("L3S");

                                    int last_index = spliced.indexOf("\n");

                                    if(L3A_index != -1 && L3M_index != -1){

                                        System.out.println("split "+spliced);

                                        float A = Float.parseFloat(spliced.substring((L3A_index+3), L3M_index));
                                        float M = Float.parseFloat(spliced.substring((L3M_index+3), L3S_index));
                                        String S = spliced.substring((L3S_index+3), last_index);

                                        System.out.println("S "+S);

                                        if(S.equals("0")) {
                                            S = "OFF";
                                        } else {
                                            S="ON";
                                        }

                                        String finalS = S;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                line3_actual.setText(""+A+" A");
                                                line3_max.setText(""+M+" A");
                                                line3_status.setText(finalS);

                                                if(finalS.equals("OFF")){
                                                    switch3.setChecked(false);
                                                } else switch3.setChecked(true);
                                            }
                                        });

                                    }
                                }

                                i++;
                            }
//
//
                        }




//                        // Receive data
//                        try {
//                            inputStream = bluetoothSocket.getInputStream();
//                            inputStream.skip(inputStream.available()); //Make sure to clean data left in the buffer
//
////                    System.out.println(bluetoothSocket.getInputStream());
//
//                            // Received Data Format
//                            // Ex. L1A30xL1M15xL1SON_L2A30...
//                            // with x as separator
//
//                            String data;

//
//                            if(inputStream.read() > 0) { //if anny data in serial
//                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                                int byteRead;
//                                while ((byteRead = inputStream.read()) != -1){
//                                    byteArrayOutputStream.write(byteRead);
//                                }
//                                inputStream.close();
//
//                                data = new String(byteArrayOutputStream.toByteArray());
//                                byteArrayOutputStream.close();
//
//                                System.out.println("++++ Data : "+data);
//
//
//                                int i = 1;
//                                for (String spliced : splicedDataAll){
//                                    // Data Structure is
//                                    // L1A30xL1M15xL1SON
//
//                                    if (i==1){
//                                        //For Line 1
//                                        float A = Float.parseFloat(spliced.substring(2,5));
//                                        float M = Float.parseFloat(spliced.substring(8,11));
//                                        String S = spliced.substring(14);
//
//                                        line1_actual.setText(""+A);
//                                        line1_max.setText(""+M);
//                                        line1_status.setText(S);
//                                    }
//
//                                    if (i==2){
//                                        //For Line 2
//                                        float A = Float.parseFloat(spliced.substring(2,5));
//                                        float M = Float.parseFloat(spliced.substring(8,11));
//                                        String S = spliced.substring(14);
//
//                                        line2_actual.setText(""+A);
//                                        line2_max.setText(""+M);
//                                        line2_status.setText(S);
//                                    }
//
//                                    if (i==3){
//                                        //For Line 3
//                                        float A = Float.parseFloat(spliced.substring(2,5));
//                                        float M = Float.parseFloat(spliced.substring(8,11));
//                                        String S = spliced.substring(14);
//
//                                        line3_actual.setText(""+A);
//                                        line3_max.setText(""+M);
//                                        line3_status.setText(S);
//                                    }
//
//                                    i++;
//                                }
//
//
//
//                            }
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }


                    }
                }, 0, 2000);

            }
        }).start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}