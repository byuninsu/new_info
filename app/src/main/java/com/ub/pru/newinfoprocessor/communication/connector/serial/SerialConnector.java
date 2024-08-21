package com.ub.pru.newinfoprocessor.communication.connector.serial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connector.IConnector;
import com.ub.pru.newinfoprocessor.utils.Define;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialConnector implements IConnector {

    private static final String TAG = "SerialConnector";
    private AtomicBoolean mInitialized = new AtomicBoolean(false);
    private long mId;
    private Context mContext;
    private Listener mListener;
    public static final String CONNECTOR_NAME = "uart";
    private SerialManager mSerialManager;
    private SerialPort mSerialPort;

    private final UartTransportConfig mTransportConfig =
            new UartTransportConfig(
                    "ttyMSM2",      // name
                    115200,        //baud
                    8,              //data bits
                    Define.SERIAL_CONFIG_ONE_STOP_BIT,   //stop bits
                    Define.SERIAL_CONFIG_NO_PARITY,      //parity
                    5000,           //read timeout
                    1000            //write timeout
            );


    @SuppressLint("WrongConstant")
    public void init(Context context, Listener listener) {
        Log.d(TAG, "SerialConnector initialize ++ ");

        if ( !mInitialized.compareAndSet(false, true) ) {
            Log.e(TAG, "already initialized");
            return;
        }
        mId = SystemClock.uptimeMillis();
        mContext = context;
        mListener = listener;
    }

    @Override public void deInit() {
        Log.d(TAG, "deinitialize ++");

        if ( !mInitialized.compareAndSet(true, false) ) {
            Log.e(TAG, "already deinitialized");
            return;
        }

        stop();

        mListener = null;
        mContext = null;

        Log.d(TAG, "deinitialize --");
    }

    @Override public String getName() {
        return CONNECTOR_NAME;
    }


    public void start() {
        Log.i(TAG, "SerialConnector start +");

        mSerialManager = new SerialManager();
        mSerialManager.create(mContext);

        if ( mSerialManager.getMatchedPath(mTransportConfig.name()) == null ) {
            Log.e(TAG, "create : CANNOT FIND port " + mTransportConfig.name());
        }

        try {
            mSerialPort = mSerialManager.openSerialPort(
                    mTransportConfig.name(),
                    mTransportConfig.buad(),
                    mTransportConfig.readTimeout()
            );

            Log.i(TAG, "create : initialize port : "+ mTransportConfig.name());


            mListener.onConnected(
                    new InfoTransport(
                            mSerialPort.getInputStream(),
                            mSerialPort.getOutputStream(),
                            true,
                            Define.CONNECTION_STATE_UART,mContext
                    ));

        }
        catch ( IOException e ) {
            e.printStackTrace();
            Log.e(TAG,"SerialConnector Connect error !");
            mListener.onConnectionError();
        }

    }

    public void stop() {
        Log.i(TAG, "UartConnectionThread stop ++");

        try {
            if ( mSerialPort != null ) {
                mSerialPort.close();
                mSerialPort = null;
            }
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }

        Log.i(TAG, "UartConnectionThread stop --");


    }


    public class UartTransportConfig {
        private static final int DEFAULT_BAUD_RATE = 2000000;
        private static final int DEFAULT_DATA_BITS = 8;

        private String mName = "ttyMSM1";
        private int mBaudRate = DEFAULT_BAUD_RATE;
        private int mDataBits = DEFAULT_DATA_BITS;
        private int mStopBits = Define.SERIAL_CONFIG_ONE_STOP_BIT;
        private int mParity = Define.SERIAL_CONFIG_NO_PARITY;

        private int mReadTimeout;
        private int mWriteTimeout;

        public UartTransportConfig(String name, int baud, int dataBits, int stopBits, int parity,
                                   int readTimeout, int writeTimeout) {
            mName = name;
            mBaudRate = baud;
            mDataBits = dataBits;
            mStopBits = stopBits;
            mParity = parity;

            mReadTimeout = readTimeout;
            mWriteTimeout = writeTimeout;
        }

        public String name() {
            return mName;
        }

        public int buad() {
            return mBaudRate;
        }

        public int readTimeout() {
            return mReadTimeout;
        }

        @NonNull @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("[");
            sb.append("baud = ").append(mBaudRate);
            sb.append(", data bits = ").append(mDataBits);
            sb.append(", stop bits = ").append(mStopBits);
            sb.append(", parity = ").append(mParity);
            sb.append(", read TO = ").append(mReadTimeout);
            sb.append(", write TO = ").append(mWriteTimeout);
            sb.append("]");

            return sb.toString();
        }
    }

}
