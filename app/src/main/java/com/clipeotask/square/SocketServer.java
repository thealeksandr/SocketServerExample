package com.clipeotask.square;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.iki.elonen.NanoWSD;

/**
 * Created by Aleksandr Nikiforov on 10/14/16.
 */

public class SocketServer extends NanoWSD {

    private static final Logger LOG = Logger.getLogger(SocketServer.class.getName());
    private OnEventListener mOnEventListener;

    public SocketServer(int port, OnEventListener onEventListener) {
        super(port);
        mOnEventListener = onEventListener;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new SquareWebSocket(this, handshake, mOnEventListener);
    }

    private static class SquareWebSocket extends WebSocket {

        private final SocketServer mServer;
        OnEventListener mOnEventListener;

        public SquareWebSocket(SocketServer server, IHTTPSession handshakeRequest,
                              OnEventListener onEventListener) {
            super(handshakeRequest);
            mServer = server;
            mOnEventListener = onEventListener;
        }

        @Override
        protected void onOpen() {
        }

        @Override
        protected void onClose(WebSocketFrame.CloseCode code, String reason,
                               boolean initiatedByRemote) {
            System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] "
                    + (code != null ? code : "UnknownCloseCode[" + code + "]")
                    + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            if (message != null && mOnEventListener != null)  {
                mOnEventListener.onEvent(message.getTextPayload());
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            System.out.println("P " + pong);
        }

        @Override
        protected void onException(IOException exception) {
            SocketServer.LOG.log(Level.SEVERE, "exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            System.out.println("R " + frame);
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            System.out.println("S " + frame);
        }
    }


    public interface OnEventListener {

        void onEvent(String event);

    }

}
