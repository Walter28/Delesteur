package com.chris.delesteur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton start = (ImageButton) findViewById(R.id.started_btn);

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            // TODO Auto-generated method stub
                startFingerPrint();
            }
        });
    }

    private void startFingerPrint() {
        Intent startFingerPrint= new Intent(this, FP_Scanner.class);
        startActivity(startFingerPrint);
    }
}