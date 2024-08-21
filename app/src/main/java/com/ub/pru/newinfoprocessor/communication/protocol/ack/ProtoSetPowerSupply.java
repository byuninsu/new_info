package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoSetPowerSupply implements IProto {

    private static final String TAG = "ProtoSetPowerSupply";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_POWER_CONTROL_BY_D2D;
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
        //Ack메시지 송부
        TransportPacket packet = null;

        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());

        mTransport.setAckSendQueue(packet);



        // -1 -> 능동전원배분 OFF (전원제어 권한 정보처리기 -> 개인무전기)

        Log.i(TAG, "ProtoSetPowerSupply Processing ++");

        Settings.Global.putInt(
                mContext.getContentResolver(),
                "ub.power_control.by_info", -1);

        return true;
    }


}
