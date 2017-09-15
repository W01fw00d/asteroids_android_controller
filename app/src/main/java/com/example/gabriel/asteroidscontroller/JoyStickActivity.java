package com.example.gabriel.asteroidscontroller;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JoyStickActivity extends AppCompatActivity {

    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5;

    TextView score, lives;

    JoyStickClass js;

    Sender_TCP sender;

    int last_direction = 0;

    public static Handler UIHandler;

    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    protected void setScoreText(String text){

        //score = (TextView)findViewById(R.id.score);
        score.setText(text);
    }

    protected void setLivesText(String text){

        //lives = (TextView) findViewById(R.id.lives);
        lives.setText(text);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick);

        score = (TextView)findViewById(R.id.score);
        lives = (TextView) findViewById(R.id.lives);

        sender = SplashActivity.sender;

        sender.assignJoystick(this);

        /*
        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);
        */

        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext(), layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {

                String action = "release_";

                if(arg1.getAction() == MotionEvent.ACTION_BUTTON_PRESS //ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    /*
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    textView2.setText("Y : " + String.valueOf(js.getY()));
                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));
                    */
                    action = "press_";

                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    /*
                    textView1.setText("X :");
                    textView2.setText("Y :");
                    textView3.setText("Angle :");
                    textView4.setText("Distance :");
                    textView5.setText("Direction :");
                    */
                    action = "release_";

                    //System.out.println(action);
                }

                int direction = js.get8Direction();

                //Si ha cambiado de dirección, es como si hiciera release a la anterior dirección
                if (direction != last_direction ){

                    last_direction = direction;

                    if(direction == JoyStickClass.STICK_UP) {
                        //textView5.setText("Direction : Up");

                        sender.sendKey("release_left");
                        sender.sendKey("release_right");

                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        //textView5.setText("Direction : Up Right");

                        sender.sendKey("release_left");

                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        //textView5.setText("Direction : Right");

                        sender.sendKey("release_up");
                        sender.sendKey("release_left");

                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        //textView5.setText("Direction : Left");

                        sender.sendKey("release_up");
                        sender.sendKey("release_right");

                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        //textView5.setText("Direction : Up Left");

                        sender.sendKey("release_right");
                    }
                }

                if(direction == JoyStickClass.STICK_UP) {
                    //textView5.setText("Direction : Up");

                    sender.sendKey(action + "up");

                } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                    //textView5.setText("Direction : Up Right");

                    sender.sendKey(action + "up");
                    sender.sendKey(action + "right");

                } else if(direction == JoyStickClass.STICK_RIGHT) {
                    //textView5.setText("Direction : Right");

                    sender.sendKey(action + "right");

                } else if(direction == JoyStickClass.STICK_LEFT) {
                    //textView5.setText("Direction : Left");

                    sender.sendKey(action + "left");

                } else if(direction == JoyStickClass.STICK_UPLEFT) {
                    //textView5.setText("Direction : Up Left");

                    sender.sendKey(action + "up");
                    sender.sendKey(action + "left");

                    /*
                    // Si está en el centro o en ningún sitio, release todas las direcciones.
                } else if(direction == JoyStickClass.STICK_NONE) {

                    action = "release_";

                    sender.sendKey(action + "up");
                    sender.sendKey(action + "left");
                    sender.sendKey(action + "right");
                   */

                }else{

                    action = "release_";

                    sender.sendKey(action + "up");
                    sender.sendKey(action + "left");
                    sender.sendKey(action + "right");
                }

                js.drawStick(arg1);

                return true;
            }
        });

        final ImageView button1 = (ImageView)this.findViewById(R.id.fire_button);

        button1.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {

                String action = "release_";

                if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    action = "press_";

                    setImageCustom(button1, R.drawable.red_pushed);

                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.fire);

                    mp.start();

                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    action = "release_";

                    setImageCustom(button1, R.drawable.red_unpushed);
                }

                sender.sendKey(action + "fire");

                return true;
            }
        });

        final ImageView button2 = (ImageView)this.findViewById(R.id.hyper_button);

        button2.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {

                String action = "release_";

                if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    action = "press_";

                    setImageCustom(button2, R.drawable.blue_pushed);

                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    action = "release_";

                    setImageCustom(button2, R.drawable.blue_unpushed);
                }

                sender.sendKey(action + "enter");

                return true;
            }
        });

        final ImageView button3 = (ImageView)this.findViewById(R.id.pause_button);

        button3.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {

                String action = "release_";

                if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    action = "press_";

                    setImageCustom(button3, R.drawable.green_pushed);

                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    action = "release_";

                    setImageCustom(button3, R.drawable.green_unpushed);
                }

                sender.sendKey(action + "pause");

                return true;
            }
        });

        final ImageView button4 = (ImageView)this.findViewById(R.id.exit_button);

        button4.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {

                String action = "release_";

                if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    action = "press_";

                    setImageCustom(button4, R.drawable.red_pushed);

                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    action = "release_";

                    setImageCustom(button4, R.drawable.red_unpushed);
                }

                sender.sendKey(action + "exit");

                killController();

                return true;
            }
        });

    }

    private void setImageCustom(ImageView button, int image){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setImageDrawable(getResources().getDrawable(image, getApplicationContext().getTheme()));
        } else {
            button.setImageDrawable(getResources().getDrawable(image));
        }
    }

    protected void killController(){

        startActivity(new Intent(JoyStickActivity.this,
                GameOverActivity.class));

        System.out.println("NEW ACTIVITY");

        this.finish();
    }

}
