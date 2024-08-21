package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoUnusedPowerSupply implements IProto {

    private static final String TAG = "ProtoUnusedPowerSupply";

    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_SELF_POWER_SUPPLY_MODE;
    private InfoTransport mInfoTransport;

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mContext = context;
        mInfoTransport = transport;
    }

    @Override public void deInit() {
        mInfoTransport = null;
        mContext = null;
    }

    @Override public byte getType() {
        return mType;
    }

    @Override
    public boolean processing() {
        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {
        //Ack메시지 송부
        TransportPacket packet = null;

        packet = mInfoTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());

        mInfoTransport.setAckSendQueue(packet);

        // 1 -> 능동제어배분 ON : 자체전원 사용

        Log.i(TAG, "ProtoUnusedPowerSupply Processing ++");

        Settings.Global.putInt(
                mContext.getContentResolver(),
                "ub.power_control.by_info", 1);

        return true;
    }

}
