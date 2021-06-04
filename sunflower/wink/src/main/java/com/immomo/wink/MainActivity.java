package com.immomo.wink;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setBackgroundColor(Color.BLACK);

        TextView textView = findViewById(R.id.textView);
//        textView.setText("Wink worked!!!");
        textView.setText("NAME : " + new Test111().getAAA());
    }
}