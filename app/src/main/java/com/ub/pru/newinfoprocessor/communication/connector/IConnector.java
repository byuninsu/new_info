package com.ub.pru.newinfoprocessor.communication.connector;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;

public interface IConnector {
    void init(Context context, Listener listener);
    void deInit();
    void start();
    void stop();
    String getName();

    interface  Listener{
        void onConnected(InfoTransport infoTransport);
        void onConnectionError();
    }

}
