package com.ub.pru.newinfoprocessor.communication.connector.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ub.pru.newinfoprocessor.MainService;

public class BtStateReceiver extends BroadcastReceiver {

    private static final String TAG = "BtStateReceiver";

    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if( BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) ){
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch ( state ){
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "Bluetooth down !");
                    Intent offIntent = new Intent(context, MainService.class);
                    offIntent.putExtra("type","bluetooth");
                    offIntent.putExtra("start", false);

                    context.startService(offIntent);
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "Bluetooth on !");

                    Intent onIntent = new Intent(context, MainService.class);
                    onIntent.putExtra("type","bluetooth");
                    onIntent.putExtra("start", true);

                    context.startService(onIntent);
                    break;
            }
        }
    }
}
