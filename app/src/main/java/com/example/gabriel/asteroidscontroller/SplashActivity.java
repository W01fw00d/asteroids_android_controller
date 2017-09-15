package com.example.gabriel.asteroidscontroller;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    static protected Sender_TCP sender;

    protected TextView status_view;

    protected Button button;

    protected void setStatus(String text){

        status_view.setText(text);
    }

    protected void activateButton(){

        button.setClickable(true);
    }

    public static Handler UIHandler;

    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        status_view = (TextView)findViewById(R.id.status);

        button = (Button)findViewById(R.id.button);

        tryToConnect();
    }

    protected void tryToConnect(){

        //button.setClickable(false);

        sender = new Sender_TCP(this);
        sender.execute("0", null, "0");
    }

    protected void startJoystick(){

        startActivity(new Intent(SplashActivity.this,
                JoyStickActivity.class));
    }
}
