package edu.pitt.engineering.joygrabber;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {


    /**
     * Method calls for send command
     */
    public void sendCameraMove(String direction) {
        EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
        String m_ip_string = m_editText.getText().toString() + ":5000";
        new HttpRequestHandler().execute(m_ip_string, "/api/camera/" + direction, "GET");
    }

    /**
     * Semaphore used to handle HTTP request
     */
    public static Semaphore m_http_lock = new Semaphore(1, true);

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

//    /** TEST IP Address */
//    public static String TEST_IP = "http://192.168.43.200";

    private final Handler mHideHandler = new Handler();
    //private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            /*mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
        }
    };
    //private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);




        //WebView
        String port = ":8082";
        final WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        // Find EditText Value
        EditText m_editText = (EditText) findViewById(R.id.ip_address_edittext);
        String m_ip_string = m_editText.getText().toString();
        webView.loadUrl(m_ip_string + port);

        // Change ip edit text, reload web view
        m_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                webView.loadUrl(editable.toString());
            }
        });
//        webView.loadUrl("http://192.168.0.10:8082");

        //Joystick
        JoystickView joystick = (JoystickView) findViewById(R.id.joystick_view);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                boolean m_result = FullscreenActivity.m_http_lock.tryAcquire();
                if (!m_result) {
                    return;
                }

                int my_angle = 360 - angle + 90;
                if (my_angle<0) {
                    my_angle += 360;
                }
                if (my_angle>=360) {
                    my_angle -= 360;
                }

                if (my_angle <= 45) {
                    my_angle = 0;
                } else if (my_angle <= 135) {
                    my_angle = 45;
                } else if (my_angle <= 225) {
                    my_angle = 180;
                } else if (my_angle <= 315) {
                    my_angle = 315;
                } else {
                    my_angle = 0;
                }
//                int my_angle = angle;
                int my_strength = strength;

                /* Send actual request */
//                new HttpRequestHandler().execute(FullscreenActivity.TEST_IP, "/api/car/move", "GET",
                // Find EditText Value
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";

                new HttpRequestHandler().execute(m_ip_string, "/api/car/move", "GET",
                        "r", String.valueOf(my_strength),
                        "angle", String.valueOf(my_angle));
                Log.w("joybrabber", Integer.toString(angle) + "," + Integer.toString(strength));
            }
        });


        // Stop Button
        Button stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/stop", "Pressed stop button");

                /* Send actual request */
//                new HttpRequestHandler().execute(FullscreenActivity.TEST_IP, "/api/car/stop", "GET");
                // Find EditText Value
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";
                new HttpRequestHandler().execute(m_ip_string, "/api/car/stop", "GET");
            }
        });
        // Clamp Release Button
        Button release_button = (Button) findViewById(R.id.clamp_release);
        release_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/release", "Pressed release button");

                /* Send actual request */
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";
                new HttpRequestHandler().execute(m_ip_string, "/api/clamp/release", "GET");
            }
        });
        release_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";
                new HttpRequestHandler().execute(m_ip_string, "/api/clamp/release", "GET");
                return true;
            }
        });
        // Clamp Close Button
        Button grab_button = (Button) findViewById(R.id.clamp_grab);
        grab_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/grab", "Pressed grab button");

                /* Send actual request */
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";
                new HttpRequestHandler().execute(m_ip_string, "/api/clamp/close", "GET");
            }
        });
        grab_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /* Send actual request */
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";
                new HttpRequestHandler().execute(m_ip_string, "/api/clamp/close", "GET");
                return true;
            }
        });

        // Camera Up Button
        Button camera_up_button = (Button) findViewById(R.id.cam_up);
        camera_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/cam_up", "Pressed cam_up button");
                FullscreenActivity.this.sendCameraMove("up");
            }
        });
        camera_up_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FullscreenActivity.this.sendCameraMove("up");
                return true;
            }
        });
        // Camera Down Button
        Button camera_down_button = (Button) findViewById(R.id.cam_down);
        camera_down_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/cam_down", "Pressed cam_down button");
                FullscreenActivity.this.sendCameraMove("down");
            }
        });
        camera_down_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FullscreenActivity.this.sendCameraMove("down");
                return true;
            }
        });
        // Camera Left Button
        Button camera_left_button = (Button) findViewById(R.id.cam_left);
        camera_left_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/cam_left", "Pressed cam_left button");
                FullscreenActivity.this.sendCameraMove("left");
            }
        });
        camera_left_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FullscreenActivity.this.sendCameraMove("left");
                return true;
            }
        });
        // Camera Up Button
        Button camera_right_button = (Button) findViewById(R.id.cam_right);
        camera_right_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/cam_right", "Pressed cam_right button");
                FullscreenActivity.this.sendCameraMove("right");
            }
        });
        camera_right_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FullscreenActivity.this.sendCameraMove("right");
                return true;
            }
        });
        // Power Off Button
        Button poweroff = (Button) findViewById(R.id.power_off);
        poweroff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/power_off", "Pressed power_off button");
                EditText m_editText = (EditText)findViewById(R.id.ip_address_edittext);
                String m_ip_string = m_editText.getText().toString() + ":5000";
                new HttpRequestHandler().execute(m_ip_string, "/api/system/poweroff", "GET");
            }
        });
        // Reset Button
        Button reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("joygrabber/reset", "Pressed reset button");
                FullscreenActivity.this.sendCameraMove("reset");
            }
        });


        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        // Set timer that periodically reset the semaphore for HTTP request
        Timer m_timer = new Timer(true);
        TimerTask m_task = new TimerTask() {
            public void run() {
                if (FullscreenActivity.m_http_lock.availablePermits() < 1) {
                    FullscreenActivity.m_http_lock.release();
                }
            }
        };
        m_timer.schedule(m_task, 250, 250);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }*/

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.VISIBLE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        /*mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);*/
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
