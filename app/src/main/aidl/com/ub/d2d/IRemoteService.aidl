// IRemoteService.aidl
package com.ub.d2d;

// Declare any non-default types here with import statements

import com.ub.d2d.ID2DCallback;

interface IRemoteService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean doDataSend(in byte type, in byte[] data);

    boolean registerD2DCallback(ID2DCallback callback);
    boolean unregisterD2DCallback(ID2DCallback callback);
    boolean reqDataSend(in int src_urn, in int dest_urn, in int type, in byte[] data, in int len);
}
