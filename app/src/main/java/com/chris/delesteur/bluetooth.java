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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class bluetooth extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    boolean canContinue;
    BluetoothAdapter bluetoothAdapter;

    private String macAddress = "";
    int nbOfPairedDevices = 0;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        if(!bluetoothAdapter.isEnabled()){
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        } else canContinue=true;

        if (ActivityCompat.checkSelfPermission(bluetooth.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT > 31){
                ActivityCompat.requestPermissions(bluetooth.this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                return;
            }
        }

        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        nbOfPairedDevices = pairedDevice.size();

//        System.out.println("Can continue : "+canContinue);

        for (BluetoothDevice device : pairedDevice){
            System.out.println("Name-> " + device.getName() + " " + "Mac-> " + device.getAddress());

            // create text view for each bounded device
            String textViewName = device.getName();
            TextView textView = new TextView(this);

            textView.setText(device.getName());
            textView.setTextSize(20);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 30);
            textView.setLayoutParams(params);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startMainScreenActivity(device.getAddress());
                }
            });

            LinearLayout layout = (LinearLayout) findViewById(R.id.boundedDevice);
            layout.addView(textView);

            if (device.getName().equals("HC-06")) {
                macAddress = device.getAddress();
                System.out.println("Mac-> " + macAddress);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed here it is 1
        if (requestCode == 1){
            if(resultCode!=-1){
                canContinue = false;
                finish();
            } else {
                canContinue = true;
            }
        }
    }

    private void startMainScreenActivity(String macAddress) {
        Bundle bundle = new Bundle();
        bundle.putString("macAddress", macAddress);

        Intent startMainScreen= new Intent(this, main_screen.class);
        startMainScreen.putExtras(bundle);
        startActivity(startMainScreen);
    }
}