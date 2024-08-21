/*
 * Copyright (c) 2004. All rights reserved by UBridge Co., Ltd.
 * Created by imeans on 2023-1-28.
 */
package com.ub.pru.newinfoprocessor.communication.connector.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

public class SerialPort {
    private static final String TAG = "SerialPort";

    private Object mInstance;

    public SerialPort(Object object) {
        mInstance = object;
    }

    /*
     * Closes the serial port
     */
    public void close() throws IOException {
        if (mInstance == null) {
            throw new IOException(
                    "This port appears to have been shutdown or disconnected.");
        }

        try {
            mInstance.getClass().
                    getMethod("close", new Class[0]).
                    invoke(mInstance);
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

        mInstance = null;
    }

    public InputStream getInputStream() throws IOException {
        if (mInstance == null) {
            throw new IOException(
                    "This port appears to have been shutdown or disconnected.");
        }

        try {
            try {
                return (InputStream) mInstance.getClass().
                        getMethod("getInputStream", new Class[0]).
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
        }
        catch ( IllegalArgumentException e ) {
            e.printStackTrace();
        }

        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        if (mInstance == null) {
            throw new IOException(
                    "This port appears to have been shutdown or disconnected.");
        }

        try {
            return (OutputStream) mInstance.getClass().
                    getMethod("getOutputStream", new Class[0]).
                    invoke(mInstance);
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
