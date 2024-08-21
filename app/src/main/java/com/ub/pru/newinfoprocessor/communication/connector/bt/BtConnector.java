package com.ub.pru.newinfoprocessor.communication.connector.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connector.IConnector;
import com.ub.pru.newinfoprocessor.utils.Define;

import java.io.IOException;
import java.util.UUID;

public class BtConnector implements IConnector {

    private static final String TAG = "BtConnector";
    private static final String NAME_SECURE = "PRU";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String CONNECTOR_NAME = "bluetooth";

    private BluetoothAdapter mAdapter;
    private Context mContext;
    private AcceptThread mAcceptThread;
    private Listener mListener;
    BluetoothSocket mSocket;


    @Override public void init(Context context, IConnector.Listener listener) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mListener = listener;
    }

    @Override public void deInit() {
        Log.d(TAG, "deinitialize ++");

        stop();

        mListener = null;
        mContext = null;

        Log.d(TAG, "deinitialize --");
    }

    public void start(){
        Log.d(TAG, "start ++");
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        Log.d(TAG, "start --");
    }


    public void stop() {
        Log.v(TAG, "stop ++");

        if ( mAcceptThread != null ) {
            Log.v(TAG, "mAcceptThread stop ++");
            mAcceptThread.forceStop();
            mAcceptThread = null;
        }

        if(mSocket != null){
            try {
                mSocket.close();
                mSocket =null;
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "stop --");
    }

    @Override public String getName() {
        return CONNECTOR_NAME;
    }

    private class AcceptThread extends Thread {
        private final String mTAG = BtConnector.TAG + ".AcceptThread";
        private boolean mIsRunning = false;
        public boolean isRunning() {
            return mIsRunning;
        }
        public void setRunning(boolean running) {
            mIsRunning = running;
        }
        private BluetoothServerSocket mServerSocket;

        public void forceStop() {
            Log.d(TAG, "forceStop ++");

            if ( isRunning() ) {
                setRunning(false);

                try {
                    if ( mServerSocket != null) {
                        Log.i(TAG, "force disconnect!!");
                        mServerSocket.close();
                    }
                }
                catch ( IOException e ) {
                    e.printStackTrace();
                }
                interrupt();
            }

            try {
                join();
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "forceStop --");
        }


        @Override
        public void run() {
            super.run();

            Log.d(mTAG, "BtAcceptThread run ++");

            setRunning(true);

            try {
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_INSECURE);
            }
            catch ( IOException e ) {
                e.printStackTrace();
                mServerSocket = null;
            }

            if ( mServerSocket != null ) {
                while ( isRunning() ) {
                    Log.d(TAG, "BT Connect Waiting..");
                    try {
                        mSocket = mServerSocket.accept();

                        if(mSocket != null){
                            mListener.onConnected(
                                    new InfoTransport(
                                            mSocket.getInputStream(),
                                            mSocket.getOutputStream(),
                                            true,
                                            Define.CONNECTION_STATE_BLUETOOTH, mContext
                                    ));
                        }
                    }
                    catch ( IOException e ) {
                        Log.e(TAG, "BT ServerThread connection IOException!!");
                        mListener.onConnectionError();
                        e.printStackTrace();
                        break;
                    }
                }
            }
            else {
                Log.e(TAG, "BT ServerThread connection is null!");
            }

            setRunning(false);

            Log.d(mTAG, "BtAcceptThread run --");
        }
    }






}
