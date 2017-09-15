package com.example.gabriel.asteroidscontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class GameOverActivity extends AppCompatActivity {

    protected Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        button = (Button)findViewById(R.id.button);
    }

    protected void restartActivity(){

        startActivity(new Intent(GameOverActivity.this,
                SplashActivity.class));
    }
}
