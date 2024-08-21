package com.ub.pru.newinfoprocessor.communication;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoConnection;
import com.ub.pru.newinfoprocessor.communication.connection.IConnection;
import com.ub.pru.newinfoprocessor.communication.connector.IConnector;
import com.ub.pru.newinfoprocessor.communication.connector.bt.BtConnector;
import com.ub.pru.newinfoprocessor.communication.connector.serial.SerialConnector;
import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DConnection;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;

import java.util.ArrayList;

public class ConnectionManager {
    private static final String TAG = "ConnectionManager";
    public static final String DEF_BLUETOOTH = "bluetooth";
    public static final String DEF_SERIAL = "uart";
    public String mConnectorType = "";
    private ArrayList<IConnection> mConnectionList = new ArrayList<>();
    private Context mContext;
    private PostHandler mHandler = new PostHandler();
    private IConnector mConnector;

    public void init(Context context) {
        Log.d(TAG, "ConnectionManager init++");

        mContext = context;

        mHandler = new PostHandler();
    }

    public void deInit(){
        Log.d(TAG, "ConnectionManager deInit++");

        stopConnector();
        mContext = null;
        mHandler = null;
    }

    public boolean isStarted() {
        return mConnector != null;
    }

    public String getConnectorType() {
        if ( !isStarted() ) {
            return null;
        }
        return mConnector.getName();
    }

    public void startConnector(String type) {

        Log.v(TAG, "startConnector++");

        if (type.equals(DEF_BLUETOOTH)) {
            mConnector = new BtConnector();
        } else if (type.equals(DEF_SERIAL)) {
            mConnector = new SerialConnector();
        } else {
            Log.e(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "  Unknown Connection Type!!!\n" +
                    "   - type : "+ type + "\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }

        mConnectorType = type;

        mConnector.init(mContext, new IConnector.Listener() {
            @Override public void onConnected(InfoTransport infoTransport) {
                Message message = mHandler.obtainMessage(PostHandler.MSG_ID_START_CONNECTION, infoTransport);
                mHandler.sendMessage(message);
            }

            @Override public void onConnectionError() {
                mHandler.sendEmptyMessage(PostHandler.MSG_ID_STOP_CONNECTION);
            }
        });
        mConnector.start();

        Log.v(TAG, "startConnector --");
    }

    public void stopConnector() {
        Log.v(TAG, "stop ++");

        for ( IConnection connection : mConnectionList ) {
            connection.deInit();
        }
        mConnectionList.clear();

        if( mConnector != null ){
            mConnector.deInit();
            mConnector = null;
        }

        if ( mHandler != null ) {
            mHandler.clear();
        }

        Log.v(TAG, "stop --");
    }


    private void createConnection(InfoTransport transport) {
        Log.v(TAG, "createConnection++");

        //d2dTransport 생성
        D2DTransport d2DTransport = new D2DTransport();

        //Connection,d2dConnection 생성
        InfoConnection infoConnection = new InfoConnection();
        D2DConnection d2DConnection = new D2DConnection();

        mConnectionList.add(infoConnection);
        mConnectionList.add(d2DConnection);

        for ( IConnection connection : mConnectionList ) {
            connection.init(mContext, transport, d2DTransport);
        }

        infoConnection.start(new IConnection.Listener() {
            @Override public void onDisconnected(IConnection connection) {
                mHandler.sendEmptyMessage(PostHandler.MSG_ID_RESTART_CONNECTOR);
            }

            @Override public void onTimeout(InfoConnection connection) {
                mHandler.sendEmptyMessage(PostHandler.MSG_ID_RESTART_CONNECTOR);
            }
        });
        d2DConnection.start(new IConnection.Listener() {
            @Override public void onDisconnected(IConnection connection) {
                Log.e(TAG, "D2DConnection onDisconnected !!");
            }

            @Override public void onTimeout(InfoConnection connection) {
                Log.e(TAG, "D2DConnection onTimeout !!");
            }
        });

        Log.v(TAG, "createConnection --");
    }


    private class PostHandler extends Handler {
        public static final int MSG_ID_START_CONNECTION = 1;
        public static final int MSG_ID_STOP_CONNECTION = 2;
        public static final int MSG_ID_RESTART_CONNECTOR = 3;

        public void clear() {
            removeMessages(MSG_ID_START_CONNECTION);
            removeMessages(MSG_ID_STOP_CONNECTION);
            removeMessages(MSG_ID_RESTART_CONNECTOR);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if ( msg.what == MSG_ID_START_CONNECTION ) {
                Log.v(TAG, "MSG_ID_START_CONNECTION");
                createConnection((InfoTransport) msg.obj);
            }
            else if ( msg.what == MSG_ID_STOP_CONNECTION ) {
                Log.v(TAG, "MSG_ID_STOP_CONNECTION");
                stopConnector();
            }
            else if ( msg.what == MSG_ID_RESTART_CONNECTOR ) {
                Log.v(TAG, "MSG_ID_RESTART_CONNECTOR");
                stopConnector();
                startConnector(mConnectorType);
            }

        }
    }

}
