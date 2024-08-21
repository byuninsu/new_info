package com.ub.pru.newinfoprocessor.communication.protocol.d2d;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoDataControl implements IProto {

    private static final String TAG = "ProtoDataControl";
    private byte mType = Define.PROTOCOL_ID_DATA_CONTROL;
    private InfoTransport mTransport;

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mTransport = transport;
    }

    @Override public void deInit() {
        mTransport = null;
    }

    @Override public byte getType() {
        return mType;
    }

    @Override public boolean processing() {
        return false;
    }

    @Override public boolean processing(TransportPacket transportPacket) {

        //데이터 Transport로 송부
        TransportPacket packet = null;
        packet = mTransport.getSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_CMD);
        packet.setSeq(mTransport.getSeq());
        packet.setPayload(transportPacket.getPayload());

        mTransport.setSendQueue(packet);

        return true;
    }


}
