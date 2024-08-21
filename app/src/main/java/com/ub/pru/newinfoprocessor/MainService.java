package com.ub.pru.newinfoprocessor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.viewbinding.BuildConfig;

import com.ub.pru.newinfoprocessor.communication.ConnectionManager;
import com.ub.pru.newinfoprocessor.utils.Define;

public class MainService extends Service {

    private static final String TAG = "MainService";
    private static final String DEF_EXTRA_TYPE = "type";
    private static final String DEF_EXTRA_START = "start";
    public static final String DEF_SERIAL = "uart";
    public static final String DEF_BLUETOOTH = "bluetooth";
    private BluetoothAdapter mBtAdapter;
    private ConnectionManager mConnMgr;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MainService onCreate ++");

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.i(TAG, " \n"
                + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"
                + "   Create Service - " + BuildConfig.LIBRARY_PACKAGE_NAME + "\n"
                + ", type : " + BuildConfig.BUILD_TYPE + ")\n"
                + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        mConnMgr = new ConnectionManager();
        mConnMgr.init(this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "InfoProcessorService onStartCommand");
        boolean start = false;
        String type = "";

        if(intent != null && intent.hasExtra(DEF_EXTRA_TYPE)){
            type = intent.getStringExtra(DEF_EXTRA_TYPE);
            start = intent.getBooleanExtra(DEF_EXTRA_START, false);

            Log.d(TAG, "onStartCommand : type = " + type + ", start = " + start);

            //현재 운용중인 Connection과 동일한 intent가 들어오면 팅겨낸다.
            if(start == mConnMgr.isStarted() && type.equals(mConnMgr.getConnectorType())){
                Log.w(TAG,"ignored onStartCommand - type : " + type + ", start : " + start);
                return START_STICKY;
            }

            //시리얼 Connection 운용중에 BT Start가 날라와도 팅겨낸다.
            if(type.equals(DEF_BLUETOOTH)){
                if(mConnMgr.isStarted() && mConnMgr.getConnectorType() == DEF_SERIAL){
                    Log.e(TAG,"ignored onStartCommand by Serial - type : " + type + ", start : " + start);
                    return START_STICKY;
                }
            }

            //Connection start 명령이 들어오면 Connector stop
            mConnMgr.stopConnector();

            if(!start && type.equals(DEF_SERIAL)){
                type = DEF_BLUETOOTH;
            }
            if(start){
                mConnMgr.startConnector(type);
            }
        }
        return START_STICKY;
    }


    @Override public void onDestroy() {
        super.onDestroy();

        mConnMgr.deInit();
        mConnMgr = null;
        Log.e(TAG, " \n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "   Destroy Service - " + BuildConfig.LIBRARY_PACKAGE_NAME + "\n"
                + ", type : " + BuildConfig.BUILD_TYPE + ")\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

}