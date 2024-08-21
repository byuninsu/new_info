/*
 * Copyright (c) 2004. All rights reserved by UBridge Co., Ltd.
 * Created by imeans on 2023-1-28.
 */
package com.ub.pru.newinfoprocessor.communication.connector.serial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SerialManager {
    private static final String TAG = "SerialManager";

    private static final String SERVICE_NAME = "serial";

    private Object mInstance;
    private String[] mPortPaths;

    @SuppressLint("WrongConstant") public void create(Context context) {
        mInstance = context.getSystemService(SERVICE_NAME);
        mPortPaths = getSerialPorts();
    }

    private String[] getSerialPorts() {
        if (mInstance == null) {
            Log.e(TAG, "instance is null");
            return null;
        }

        try {
            return (String[]) mInstance.getClass().
                    getMethod("getSerialPorts", new Class[0]).
                    invoke(mInstance);
        }
        catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }
        catch ( InvocationTargetException e ) {
            e.printStackTrace();
        }
        catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        }


        return null;
    }

    public String getMatchedPath(String name) {
        for ( String path : mPortPaths ) {
            if ( path.contains(name) ) {
                return path;
            }
        }

        return null;
    }

    /**
     * Open Serial port
     *
     * @param path tty path (e.g /dev/ttyMSM1)
     * @param speed baudrate of serial
     *              Speed must be one of
     *              50, 75, 110, 134, 150, 200, 300, 600, 1200, 1800, 2400, 4800, 9600,
     *              19200, 38400, 57600, 115200, 230400, 460800, 500000, 576000, 921600,
     *              1000000, 1152000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000
     * @param readTimeout in milliseconds (if readTimeout is 0, it's same as not using timeout)
     * @return Return matched SerialPort instance
     * @throws IOException
     */
    public SerialPort openSerialPort(String path, int speed, int readTimeout) throws IOException {
        if (mInstance == null) {
            Log.e(TAG, "instance is null");
            return null;
        }

        path = getMatchedPath(path);
        if (path == null) {
            Log.e(TAG, "port path is not exist - " + path);
            return null;
        }

        try {
            Object object = mInstance.getClass().
                    getMethod("openSerialPort", new Class[]{String.class, int.class, int.class}).
                    invoke(mInstance, path, speed, readTimeout);

            if ( object == null ) {
                Log.e(TAG, "openSerialPort - fail!!");
                return null;
            }

            return new SerialPort(object);
        }
        catch ( InvocationTargetException e ) {
            e.printStackTrace();
        }
        catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }
        catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
