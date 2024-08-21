package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.communication.protocol.cmd.ProtoRespBitResult;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoReqBitResult implements IProto {

    private static final String TAG = "ProtoReqBitResult";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_REQ_BIT_RESULT;
    private InfoTransport mTransport;
    private D2DTransport mD2DTransport;
    ProtoRespBitResult protoRespBitResult = new ProtoRespBitResult();

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mContext = context;
        mTransport = transport;
        mD2DTransport = d2DTransport;

        protoRespBitResult.init(mContext, mTransport, mD2DTransport);
    }

    @Override public void deInit() {
        if(protoRespBitResult != null){
            protoRespBitResult.deInit();
            protoRespBitResult = null;
        }
        mTransport = null;
        mD2DTransport = null;
        mContext = null;
    }


    @Override public byte getType() {
        return mType;
    }

    @Override public boolean processing() {
        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {

        //패킷 가져와서 송부
        TransportPacket packet = null;
        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());

        mTransport.setAckSendQueue(packet);

        //자가진단 결과 송부

        Log.i(TAG, "ProtoReqBitResult Processing ++");
        protoRespBitResult.processing();

        return true;
    }

}
