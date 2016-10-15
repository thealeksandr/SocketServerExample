package com.clipeotask.square;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private SquareServer mServer;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private View mView;
    private TextView mTextView;
    private int mHeight;
    private int mWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock("SquareLock");
        mWifiLock.acquire();
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "SquareScreenLock");
        mWakeLock.acquire();

        final String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        mTextView = (TextView) findViewById(R.id.address_text_view);
        mView = findViewById(R.id.square_view);
        final View rootView = findViewById(R.id.square_field);
        setMessageUI(ip);
        runServer(ip);


        ViewTreeObserver vtObserver = rootView.getViewTreeObserver();
        vtObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mHeight = rootView.getHeight();
                mWidth = rootView.getWidth();
            }
        });

    }

    private void runServer(String ip) {
        try {
            mServer = new SquareServer(mOnKeyListener, ip);
            mServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setMessageUI(String ip) {
        mTextView.setText("Open " + ip + ":8080 " + "in browser to control square");
    }

    private OnKeyListener mOnKeyListener = new OnKeyListener() {
        @Override
        public void onUp() {
            if (mView.getY() - 10 >= 0) {
                mView.setY(mView.getY() - 10);
            }
        }

        @Override
        public void onDown() {
            if (mView.getY() + 10 + mView.getHeight() <= mHeight) {
                mView.setY(mView.getY() + 10);
            }
        }

        @Override
        public void onLeft() {
            if (mView.getX() - 10 >= 0) {
                mView.setX(mView.getX() - 10);
            }
        }

        @Override
        public void onRight() {
            if (mView.getX() + 10 + mView.getWidth() <= mWidth) {
                mView.setX(mView.getX() + 10);
            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWifiLock.release();
        mWakeLock.release();
        mServer.stop();
    }
}
