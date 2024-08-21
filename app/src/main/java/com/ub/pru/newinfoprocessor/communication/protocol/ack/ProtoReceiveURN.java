package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.communication.protocol.cmd.ProtoSendURN;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoReceiveURN implements IProto {

    private static final String TAG = "ProtoReceiveURN";

    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_RECEIVE_URN_REQ;
    private InfoTransport mTransport;
    private D2DTransport mD2DTransport;
    ProtoSendURN protoSendURN = new ProtoSendURN();

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mContext = context;
        mTransport = transport;
        mD2DTransport = d2DTransport;

        protoSendURN.init(mContext, mTransport, mD2DTransport);
    }

    @Override public void deInit() {
        if(protoSendURN != null){
            protoSendURN.deInit();
            protoSendURN = null;
        }
        mTransport = null;
        mD2DTransport = null;
        mContext = null;
    }

    @Override public byte getType() {
        return mType;
    }

    @Override public boolean processing() {
        return false;
    }

    @Override public boolean processing(TransportPacket transportPacket) {

        //Ack메시지 송부
        TransportPacket packet = null;
        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());
        mTransport.setAckSendQueue(packet);

        //URN 송부
        protoSendURN.processing(transportPacket);

        return true;
    }


}
