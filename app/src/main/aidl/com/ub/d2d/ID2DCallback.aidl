// IMilstdCallback.aidl
package com.ub.d2d;

// Declare any non-default types here with import statements

interface ID2DCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onDataReceived(in byte[] data, in int len);

    void onCallStateChanged(in int incoming_calls, in int outgoing_call);
    void onLocationUpdated(in long timestamp, in int urn, in String mgrs);
    void onRemoteErase();
}