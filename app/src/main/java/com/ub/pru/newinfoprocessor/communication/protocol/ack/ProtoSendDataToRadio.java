package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoSendDataToRadio implements IProto {

    private static final String TAG = "ProtoSendDataToRadio";

    private byte mType = Define.PROTOCOL_ID_TRANSMIT_DATA_TO_RADIO;
    private D2DTransport mD2DTransport;
    private InfoTransport mTransport;

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mD2DTransport = d2DTransport;
        mTransport = transport;
    }

    @Override public void deInit() {
        mD2DTransport = null;
    }


    @Override public byte getType() {
        return mType;
    }


    @Override public boolean processing() {

        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {
        //Ack메시지 송부
        TransportPacket packet = null;

        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());

        mTransport.setAckSendQueue(packet);

        //d2d transport로 송부하여 타 무전기로 송신
        Log.i(TAG, "ProtoSendDataToRadio Processing ++");

        mD2DTransport.send(transportPacket.getPayload());
        return true;
    }

}
