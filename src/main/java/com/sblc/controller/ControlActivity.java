package com.sblc.controller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;


public class ControlActivity extends AppCompatActivity
{
    ImageButton btnForward, btnBackward,  btnLeft, btnRight;
    Button btnDis,btnPhun;
    String address = null;
    BluetoothAdapter myBluetooth = null;
    boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);

        btnForward = findViewById(R.id.up_1);
        btnBackward = findViewById(R.id.down_1);
        btnLeft = findViewById(R.id.left_1);
        btnRight = findViewById(R.id.right_1);
        btnDis = findViewById(R.id.connect);
		btnPhun = findViewById(R.id.fire);
        
        btnForward.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                   case MotionEvent.ACTION_DOWN: run("u"); break;
                    case MotionEvent.ACTION_UP: run("s"); break;
            }

            return true;
        });
        
        btnBackward.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                    case MotionEvent.ACTION_DOWN: run("d"); break;
                    case MotionEvent.ACTION_UP: run("s"); break;
            }

            return true;
        });
        
        btnLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                    case MotionEvent.ACTION_DOWN: run("l"); break;
                    case MotionEvent.ACTION_UP: stop("b"); break;
            }

            return true;
        });
        
        btnRight.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                    case MotionEvent.ACTION_DOWN: run("r"); break;
                    case MotionEvent.ACTION_UP: stop("b"); break;
            }

            return true;
        });
        

        btnPhun.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                    case MotionEvent.ACTION_DOWN: run("p"); break;
                    case MotionEvent.ACTION_UP: stop("k"); break;
            }

            return true;
        });

        btnDis.setOnTouchListener((v, event) -> {
            Disconnect();
            return true;
        });
    }
    private void Disconnect()
    {
        MainActivity.ct.cancel();
        finish();
        System.exit(0);
    }

    private void run(String s)
    {
        MainActivity.ct.write(s.getBytes());
    }
    private void stop(String s)
    {
        MainActivity.ct.write(s.getBytes());
    }

}
