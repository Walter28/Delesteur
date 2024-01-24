package com.chris.delesteur;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class FP_Scanner extends AppCompatActivity {
    Button cancel_btn, login_btn;
    private static final int REQUEST_ENABLE_BT = 1;
    boolean canContinue;
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fp_scanner);

        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        login_btn = (Button) findViewById(R.id.login_btn);

        checkBiometricSupported();

        // creatin a variable for our executor
        Executor executor = ContextCompat.getMainExecutor(this);

        //This will give us result of AUTH
        final BiometricPrompt biometricPrompt = new BiometricPrompt(FP_Scanner.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                //make sure the bt is on before the user access the page concerned
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

                if (ActivityCompat.checkSelfPermission(FP_Scanner.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT > 31){
                        ActivityCompat.requestPermissions(FP_Scanner.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                        return;
                    }
                }

                System.out.println("walter : "+bluetoothAdapter.isEnabled());
                if(bluetoothAdapter.isEnabled()) startBluetoothActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        // CREATING A VARIABLE FOR OUR PROMPTINFO
        // BIOMETRIC DIALOG

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("DELESTEUR LOGIN")
                .setDescription("Use your fingerprint to login ").setNegativeButtonText("Cancel").build();
        login_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // TODO Auto-generated method stub
                biometricPrompt.authenticate(promptInfo);

            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            // TODO Auto-generated method stub
                    finish();
                }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed here it is 1
        if (requestCode == 1){

            System.out.println("walter : "+resultCode);

            if(resultCode!=-1){
                canContinue = false;
//                finish();
                Toast.makeText(this, "Please connect first your bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("walter 2 "+bluetoothAdapter.isEnabled());
                canContinue = true;
                startBluetoothActivity();
            }
        }
    }

    private void startBluetoothActivity() {
        Intent startBluetooth= new Intent(this, bluetooth.class);
        startActivity(startBluetooth);
    }

    private void checkBiometricSupported() {
        String info = "";
        BiometricManager manager = BiometricManager.from(this);
        switch (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK
                | BiometricManager.Authenticators.BIOMETRIC_STRONG))
        {
            case BiometricManager.BIOMETRIC_SUCCESS:
                info = "App can authenticate using biometrics";
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                info = "No biometric features availavle on this device";
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                info = "Biometric features are currently unvailable";
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                info = "Need register at least one fingerprint";
                break;
            default:
                info = "Unknown cause";
                break;
        }

        Toast.makeText(this, info, Toast.LENGTH_LONG).show();

    }


}