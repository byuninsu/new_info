package com.ub.pru.newinfoprocessor.communication.protocol.cmd;

import android.content.Context;
import android.provider.Settings;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;
import com.ub.pru.newinfoprocessor.utils.Utils;

public class ProtoReqUrn implements IProto {

    private static final String TAG = "ProtoReqUrn";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_REQ_URN;
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
        int urn = Settings.Global.getInt(mContext.getContentResolver(),
                "ub.login", 0);

        //Ack메시지 송부
        TransportPacket packet = null;

        packet = mTransport.getSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_CMD);
        packet.setSeq(mTransport.getSeq());
        packet.setPayload(Utils.Int2Byte(urn));

        mTransport.setSendQueue(packet);
        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {
        return false;
    }
}
