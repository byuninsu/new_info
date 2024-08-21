package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoEraseRadio implements IProto {

    private static final String TAG = "ProtoDataControl";

    private byte mType = Define.PROTOCOL_ID_ERASE_RADIO;
    private InfoTransport mTransport;
    private Context mContext;


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
        return false;
    }

    @Override
    public boolean processing(TransportPacket transportPacket) {
        Intent intent = new Intent();

        //Ack메시지 송부
        TransportPacket packet = null;
        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());

        mTransport.setAckSendQueue(packet);

        //전달받은 프로세스 진행
        Log.i(TAG, "ProtoEraseRadio Processing ++");

        // 원격소거 처리
        intent.setComponent(new ComponentName("com.ub.hwmanager", "com.ub.hwmanager.MainService"));
        intent.putExtra("factory_reset", true);
        mContext.startService(intent);

        return true;
    }

}
