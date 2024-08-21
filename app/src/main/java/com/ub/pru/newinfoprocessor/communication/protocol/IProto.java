package com.ub.pru.newinfoprocessor.communication.protocol;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;

public interface IProto {
    void init(Context context, InfoTransport transport, D2DTransport d2DTransport);
    void deInit();
    byte getType();
    boolean processing();
    boolean processing(TransportPacket transportPacket);
}
