package com.immomo.wink;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.immomo.litebuild.mylibrary.TestLibJava;
import com.immomo.wink.utils.Tools;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setBackgroundColor(Color.BLACK);

        TextView textView = findViewById(R.id.textView);
        textView.setText(Tools.getTitle() + ">>>" + TestLibJava.getVersion());

        textView.setOnClickListener((v)->{
            Toast.makeText(this, "xxx", Toast.LENGTH_SHORT).show();
        });
    }
}