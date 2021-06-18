package com.immomo.wink;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.immomo.wink.utils.Tools;
import com.immomo.wink.utils.ZZ;

@Route(path = "/com/Activity1")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setBackgroundColor(Color.BLACK);

        TextView textView = findViewById(R.id.textView);
        textView.setText(Tools.getTitle() + "22" + new ZZ().getKK());
//        textView.setText(Tools.getTitle() + "xx1");

        textView.setOnClickListener((v)->{
            Toast.makeText(this, "333" + new ZZ().getKK(), Toast.LENGTH_SHORT).show();
        });
    }
}