package me.codego.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import me.codego.view.FloatLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addFloatView(View view) {
        FloatLayout floatLayout = new FloatLayout(getApplicationContext());
        Button button = new Button(this);
        button.setText("A");
        floatLayout.addView(button);
        floatLayout.show();
    }
}
