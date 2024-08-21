package com.ub.pru.newinfoprocessor.communication.protocol.d2d;

import android.content.Context;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoEraseInfoprocessor implements IProto {

    private static final String TAG = "ProtoEraseInfoprocessor";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_ERASE_PHONE;
    private InfoTransport mTransport;

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mContext = context;
        mTransport = transport;
    }

    @Override public void deInit() {
        mTransport = null;
        mContext = null;
    }

    @Override public byte getType() {
        return mType;
    }


    @Override public boolean processing() {
        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {
        //데이터 Transport로 송부
        TransportPacket packet = null;
        packet = mTransport.getSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_CMD);
        packet.setSeq(mTransport.getSeq());

        mTransport.setSendQueue(packet);

        return false;
    }


}
