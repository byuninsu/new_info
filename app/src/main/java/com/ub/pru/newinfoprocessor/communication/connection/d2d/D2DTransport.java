package com.ub.pru.newinfoprocessor.communication.connection.d2d;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ub.d2d.ID2DCallback;
import com.ub.d2d.IRemoteService;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.utils.Define;
import com.ub.pru.newinfoprocessor.utils.ProducerConsumerQueue;
import com.ub.pru.newinfoprocessor.utils.Utils;

import java.nio.ByteBuffer;

public class D2DTransport {

    private static final String TAG = "D2DTransport";
    Intent mRestartIntent;
    PendingIntent mPendingIntent;
    Intent bindIntent;
    private Context mContext;
    private Connection mConnection = new Connection();
    private ID2DCallback mD2DCallback = new CallBack();
    D2DSendThread mD2DSendThread;
    D2DParseThread mD2DParseThread;
    private D2DTransportListener mListener;
    private ProducerConsumerQueue<D2DDataInfo> mSendQueue;
    private ProducerConsumerQueue<TransportPacket> mReceiveQueue;
    private IRemoteService mService;
    private static final int NUM_4 = 4;
    private static final int NUM_8 = 8;
    private static final int NUM_12 = 12;
    private byte DATA_RECEIVED = (byte) 0x10;
    private byte CALL_STATE = (byte) 0x20;
    private byte LOCATION_UPDATE = (byte) 0x30;
    private byte REMOTE_ERASE = (byte) 0x40;
    private TransportPacket mTransportPacket = new TransportPacket();

    public void start(Context context) {
        Log.d(TAG, "start");
        bindIntent = new Intent();

        // queue
        try {
            // register send queue
            mSendQueue = new ProducerConsumerQueue<>("send", 30);
            for ( int i = 0; i < mSendQueue.capacity(); i++ ) {
                mSendQueue.register(new D2DDataInfo());
            }

            // register receive queue
            mReceiveQueue = new ProducerConsumerQueue<>("rcv", 30);
            for ( int i = 0; i < mReceiveQueue.capacity(); i++ ) {
                mReceiveQueue.register(new TransportPacket());
            }
        }
        catch ( InterruptedException e ) {
            e.printStackTrace();
        }
        mContext = context;

        bindIntent.setClassName("com.ub.d2d", "com.ub.d2d.RemoteService");
        mContext.bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        mRestartIntent = new Intent(mContext, D2DTransport.class);
        mPendingIntent = PendingIntent.getService(mContext, 0, mRestartIntent, 0);

        mD2DSendThread = new D2DSendThread();
        mD2DSendThread.start();
        mD2DParseThread = new D2DParseThread();
        mD2DParseThread.start();
    }

    public void registerListener(D2DTransportListener listener) {
        mListener = listener;
    }

    public interface D2DTransportListener {
        void SendDataToInfoprocessor(TransportPacket transportPacket);
        void onReceiveError();
    }

    public void send(byte[] data) {
        try {
            Log.d(TAG, "TypeSendDataToRadio processing");

            D2DDataInfo d2DDataInfo = mSendQueue.prepare();
            ByteBuffer wrapData = ByteBuffer.wrap(data);

            d2DDataInfo.setSrcUrn(wrapData.getInt()).
                    setDstUrn(wrapData.getInt()).
                    setType(wrapData.getInt()).
                    setData(data, NUM_12, data.length - NUM_12);

            mSendQueue.produce(d2DDataInfo);
        }
        catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }


    public void stop() {

        Log.v(TAG, "stop ++");

        // receive thread
        if ( mD2DSendThread != null ) {
            mD2DSendThread.forceStop();
            mD2DSendThread = null;
        }

        if ( mD2DParseThread != null ) {
            mD2DParseThread.forceStop();
            mD2DParseThread = null;
        }

        if ( mSendQueue != null ) {
            mSendQueue.clear();
            mSendQueue = null;
        }

        if ( mReceiveQueue != null ) {
            mReceiveQueue.clear();
            mReceiveQueue = null;
        }

        try {
            mService.unregisterD2DCallback(mD2DCallback);
        }
        catch ( RemoteException e ) {
            e.printStackTrace();
        }

        mContext.unbindService(mConnection);

        Log.v(TAG, "stop --");
    }


    private class D2DSendThread extends Thread {
        private static final String TAG = "D2DSendThread";
        private boolean mIsRunning = false;

        public boolean isRunning() {
            return mIsRunning;
        }

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        private boolean isSend = false;

        public D2DSendThread() {
        }

        public void forceStop() {
            Log.d(TAG, "D2DSendThread forceStop ++");

            if ( isRunning() ) {
                setRunning(false);
                interrupt();
            }

            try {
                join();
            }
            catch ( InterruptedException e ) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "D2DSendThread forceStop --");
        }

        public void run() {
            Log.d(TAG, "D2DSendThread : run");

            D2DDataInfo mD2DDataInfo = null;

            setRunning(true);

            try {
                while ( isRunning() ) {
                    mD2DDataInfo = mSendQueue.consume();

                    Log.d(TAG, "D2DSendThread : reqDataSend : src = " + mD2DDataInfo);

                    isSend = mService.reqDataSend(mD2DDataInfo.getSrcUrn(), mD2DDataInfo.getDstUrn(),
                            mD2DDataInfo.getType(), mD2DDataInfo.getData(),
                            mD2DDataInfo.getDataLength());

                    Log.d(TAG, "send data from D2D APP : " + isSend);

                    mSendQueue.recall(mD2DDataInfo);
                }
            }
            catch ( RemoteException e ) {
                e.printStackTrace();
                Log.d(TAG, "D2DCommunicationService not activity");
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }

            setRunning(false);
            mSendQueue.recallAll();

        }
    }


    private class D2DParseThread extends Thread {
        private static final String TAG = "D2DParseThread";
        private boolean mIsRunning = false;

        public boolean isRunning() {
            return mIsRunning;
        }

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        private byte[] mBuf;

        private boolean isSend = false;


        public void forceStop() {
            Log.d(TAG, "D2DParseThread forceStop ++");

            if ( isRunning() ) {
                setRunning(false);
                interrupt();
            }
            try {
                join();
            }
            catch ( InterruptedException e ) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "D2DParseThread forceStop --");
        }

        public void run() {
            Log.d(TAG, "D2ParseThread : run");

            TransportPacket transportPacket = null;

            setRunning(true);

            try {
                while ( isRunning() ) {
                    transportPacket = mReceiveQueue.consume();

                    mListener.SendDataToInfoprocessor(transportPacket);

                    mReceiveQueue.recall(transportPacket);
                }
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }
            setRunning(false);
        }
    }


    private class Connection implements ServiceConnection {

        @Override public void onServiceConnected(ComponentName componentName, IBinder service) {
            if ( service != null ) {
                mService = IRemoteService.Stub.asInterface(service);
                Log.i(TAG, "D2DTransport Connect");

                try {
                    mService.registerD2DCallback(mD2DCallback);
                }
                catch ( RemoteException e ) {
                    e.printStackTrace();
                }
            }
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {
            if ( mService != null ) {
                Handler handler = null;

                Log.i(TAG, "D2DTransport Disconnect");

                try {
                    mService.unregisterD2DCallback(mD2DCallback);
                }
                catch ( RemoteException e ) {
                    e.printStackTrace();
                }

                stop();

                mListener.onReceiveError();

                Log.d(TAG, "D2DTransport Disconnected");

            }

        }
    }


    private class CallBack extends ID2DCallback.Stub {
        @Override public void onDataReceived(byte[] data, int len) throws RemoteException {

            Log.v(TAG, "Received d2d len = " + data.length + ", data = " + Utils.byteArrayToHex(data));

            try {
                mTransportPacket.clear();
                mTransportPacket = mReceiveQueue.prepare();
                mTransportPacket.setType(DATA_RECEIVED);
                mTransportPacket.setPayload(data);
                mReceiveQueue.produce(mTransportPacket);
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }

        }

        @Override public void onCallStateChanged(int incoming_calls, int outgoing_call) {
            byte[] call_idle = {0x02};
            byte[] call_active = {0x01};

            Log.v(TAG, "onCallStateChanged : " + incoming_calls);

            try {
                mTransportPacket.clear();
                mTransportPacket = mReceiveQueue.prepare();
                mTransportPacket.setType(CALL_STATE);

                if ( incoming_calls > 0 || outgoing_call == 1 ) {
                    mTransportPacket.setPayload(call_active);
                }
                else {
                    mTransportPacket.setPayload(call_idle);
                }

                mReceiveQueue.produce(mTransportPacket);
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }

        }

        @Override public void onLocationUpdated(long timestamp, int urn, String mgrs) {
            ByteBuffer bb = ByteBuffer.allocate(Define.NUM_8 + Define.NUM_4 + mgrs.length());

            Log.v(TAG, "onLocationUpdated");

            bb.putLong(timestamp);
            bb.putInt(urn);
            bb.put(mgrs.getBytes());

            try {
                mTransportPacket.clear();
                mTransportPacket = mReceiveQueue.prepare();
                mTransportPacket.setType(LOCATION_UPDATE);
                mTransportPacket.setPayload(bb.array());
                mReceiveQueue.produce(mTransportPacket);
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }

        @Override public void onRemoteErase() {
            Log.v(TAG, "onRemoteErase");

            try {
                mTransportPacket.clear();
                mTransportPacket = mReceiveQueue.prepare();
                mTransportPacket.setType(REMOTE_ERASE);
                mReceiveQueue.produce(mTransportPacket);
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }

    }
}
