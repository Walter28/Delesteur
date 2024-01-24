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
import android.util.Log;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

//import kotlinx.coroutines.channels.Receive;

public class main_screen extends AppCompatActivity {

    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;
    InputStream inputStream;

    Intent intent;
    String macAddress;

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Get the Intent object
        intent = getIntent();
        //Retrieve data from the Bundle
        macAddress = intent.getStringExtra("macAddress");

        System.out.println("++++Mac address ::: "+macAddress);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("++BT Devise :"+bluetoothAdapter.getRemoteDevice(macAddress));
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);



        //CONNECT BLUETOOTH IN SEPARATE THREAD
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(main_screen.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT > 31){
                        ActivityCompat.requestPermissions(main_screen.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                        return;
                    }
                }

                if (ActivityCompat.checkSelfPermission(main_screen.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT > 31){
                        ActivityCompat.requestPermissions(main_screen.this,
                                new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
                        return;
                    }
                }

                try {
                    //BLUETOOTH SOCKET FOR THE COMMUNICATION
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    System.out.println("+++ Etat de la conn 1 : "+bluetoothSocket.isConnected());
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();

                    System.out.println("+++ Etat de la conn 2 : "+bluetoothSocket.isConnected());

                    inputStream = bluetoothSocket.getInputStream();
                    inputStream.skip(inputStream.available()); //Make sure to clean data left in the buffer

//                    System.out.println(bluetoothSocket.getInputStream());

                    // Received Data Format
                    // Ex. L1A30xL1M15xL1SON_L2A30...
                    // with x as separator

                    String data;
                    String[] splicedDataAll;
                    String[] splicedDataInLine;

//                    System.out.println("+++Data existance : "+inputStream.available());

//                    if(inputStream.read() > 0) { //if anny data in serial
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int byteRead;
//                    System.out.println("+++Data existance : "+inputStream.available());
                    while ((byteRead = inputStream.read()) != -1) {
                        byteArrayOutputStream.write(byteRead);
                        System.out.println("+++byte array : "+byteArrayOutputStream);
                    }
                    inputStream.close();

                    data = new String(byteArrayOutputStream.toByteArray());
                    byteArrayOutputStream.close();

                    System.out.println("++++ Data : " + data);
//                    }

                    // SEND MSG OBJECT
                    outputStream = bluetoothSocket.getOutputStream();
                    Log.d("Message", "Conneted to "+bluetoothDevice.getName());
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
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                // NOW COMMUNICATION
//
//                TextView line1_actual = (TextView) findViewById(R.id.line1_actual);
//                TextView line1_max = (TextView) findViewById(R.id.line1_max);
//                TextView line1_status = (TextView) findViewById(R.id.line1_status);
//                Switch switch1 = (Switch) findViewById(R.id.switch1);
//
//                TextView line2_actual = (TextView) findViewById(R.id.line2_actual);
//                TextView line2_max = (TextView) findViewById(R.id.line2_max);
//                TextView line2_status = (TextView) findViewById(R.id.line2_status);
//                Switch switch2 = (Switch) findViewById(R.id.switch2);
//
//                TextView line3_actual = (TextView) findViewById(R.id.line3_actual);
//                TextView line3_max = (TextView) findViewById(R.id.line3_max);
//                TextView line3_status = (TextView) findViewById(R.id.line3_status);
//
//
//                // Receive data
//                try {
//                    inputStream = bluetoothSocket.getInputStream();
//                    inputStream.skip(inputStream.available()); //Make sure to clean data left in the buffer
//
////                    System.out.println(bluetoothSocket.getInputStream());
//
//                    // Received Data Format
//                    // Ex. L1A30xL1M15xL1SON_L2A30...
//                    // with x as separator
//
//                    String data;
//                    String[] splicedDataAll;
//                    String[] splicedDataInLine;
//
//                    if(inputStream.read() > 0) { //if anny data in serial
//                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                        int byteRead;
//                        while ((byteRead = inputStream.read()) != -1){
//                            byteArrayOutputStream.write(byteRead);
//                        }
//                        inputStream.close();
//
//                        data = new String(byteArrayOutputStream.toByteArray());
//                        byteArrayOutputStream.close();
//
//                        System.out.println("++++ Data : "+data);

//                        splicedDataAll = data.split("_");
//                        int i = 1;
//                        for (String spliced : splicedDataAll){
//                            // Data Structure is
//                            // L1A30xL1M15xL1SON
//
//                            if (i==1){
//                                //For Line 1
//                                float A = Float.parseFloat(spliced.substring(2,5));
//                                float M = Float.parseFloat(spliced.substring(8,11));
//                                String S = spliced.substring(14);
//
//                                line1_actual.setText(""+A);
//                                line1_max.setText(""+M);
//                                line1_status.setText(S);
//                            }
//
//                            if (i==2){
//                                //For Line 2
//                                float A = Float.parseFloat(spliced.substring(2,5));
//                                float M = Float.parseFloat(spliced.substring(8,11));
//                                String S = spliced.substring(14);
//
//                                line2_actual.setText(""+A);
//                                line2_max.setText(""+M);
//                                line2_status.setText(S);
//                            }
//
//                            if (i==3){
//                                //For Line 3
//                                float A = Float.parseFloat(spliced.substring(2,5));
//                                float M = Float.parseFloat(spliced.substring(8,11));
//                                String S = spliced.substring(14);
//
//                                line3_actual.setText(""+A);
//                                line3_max.setText(""+M);
//                                line3_status.setText(S);
//                            }
//
//                            i++;
//                        }
//
//
//
//                    }
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//        }).start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}