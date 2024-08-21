package com.ub.pru.newinfoprocessor.communication.protocol.cmd;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoKeepAlive implements IProto {

    private static final String TAG = "ProtoKeepAlive";
    private byte mType = Define.PROTOCOL_ID_KEEP_ALIVE;
    private InfoTransport mTransport;

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mTransport = transport;
    }

    @Override public void deInit() {
        mTransport = null;
    }

    public  void setTransport(InfoTransport transport){
        mTransport = transport;
    }

    @Override public byte getType() {
        return mType;
    }

    @Override public boolean processing() {

        //keepAlive 송부
        TransportPacket packet = null;
        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_CMD);
        packet.setSeq(mTransport.getSeq());

        //keepAlive는 응답이 없어도 1회성으로만 보내기 때문에 ackSendQueue로 송부
        mTransport.setAckSendQueue(packet);

        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {
        return false;
    }


}
