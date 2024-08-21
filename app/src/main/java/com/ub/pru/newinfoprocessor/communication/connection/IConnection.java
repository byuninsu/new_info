package com.ub.pru.newinfoprocessor.communication.connection;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.InfoConnection;
import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;

public interface IConnection {
    void start(Listener listener);
    void stop();
    void init(Context context, InfoTransport transport, D2DTransport d2DTransport);
    void deInit();

    public interface Listener {
        void onDisconnected(IConnection connection);
        void onTimeout(InfoConnection connection);
    }

}
