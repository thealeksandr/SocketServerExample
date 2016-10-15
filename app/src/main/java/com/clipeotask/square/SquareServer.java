package com.clipeotask.square;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Aleksandr Nikiforov on 10/14/16.
 */

public class SquareServer extends NanoHTTPD {

    private OnKeyListener mOnKeyListener;
    private SocketServer mSocketServer;
    private String mIp;


    SquareServer(OnKeyListener onKeyListener, String ip) throws IOException {
        super(8080);
        start(0, false);
        mOnKeyListener = onKeyListener;
        mIp = ip;
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (mSocketServer != null) {
            mSocketServer.stop();
        }
        mSocketServer = new SocketServer(9090, mOnEventListener);
        try {
            mSocketServer.start(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String msg = "<html><head>";
        msg += "<script>\n" +
                "  var connection = new WebSocket('ws://" + mIp + ":9090');\n" +
                "\n" +
                "connection.onopen = function () {\n" +
                "  connection.send('Ping GAGA');\n" +
                "};\n" +
                "document.onkeydown = checkKey;\n" +
                "\n" +
                " window.onbeforeunload = function() {\n" +
                "    websocket.onclose = function () {};\n" +
                "    websocket.close()\n" +
                "};" +
                "function checkKey(e) {\n" +
                "\n" +
                "    e = e || window.event;\n" +
                "\n" +
                "    if (e.keyCode == '38') {\n" +
                "\n" +
                "  connection.send('2');\n" +
                "    }\n" +
                "    else if (e.keyCode == '40') {\n" +
                "        connection.send('1');\n" +
                "    }\n" +
                "    else if (e.keyCode == '37') {\n" +
                "       connection.send('3');\n" +
                "    }\n" +
                "    else if (e.keyCode == '39') {\n" +
                "       connection.send('4');\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "</script>"
                +"</head>";
        msg += "<body><h1>Use arrows to control square on your Android device.</h1>\n";
        return newFixedLengthResponse(msg + "</body></html>\n");
    }

    private SocketServer.OnEventListener mOnEventListener = new SocketServer.OnEventListener() {
        @Override
        public void onEvent(final String event) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendEventToUI(event);
                }
            });

        }
    };

    private void sendEventToUI(String event) {
        if (mOnKeyListener == null) {
            return;
        }
        int eventKey;
        try {
            eventKey = Integer.valueOf(event);
        } catch (NumberFormatException e) {
            return;
        }

        switch (eventKey) {
            case 1:
                mOnKeyListener.onDown();
                break;
            case 2:
                mOnKeyListener.onUp();
                break;
            case 3:
                mOnKeyListener.onLeft();
                break;
            case 4:
                mOnKeyListener.onRight();
                break;
            default:
        }
    }

}
