package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoUsePowerSupply implements IProto {

    private static final String TAG = "ProtoUnusedPowerSupply";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_UNITED_POWER_SUPPLY_MODE;
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

    public boolean processing() {
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


        // 0 -> 능동제어배분 ON : 통합전원 사용

        Log.i(TAG, "ProtoUsePowerSupply Processing ++");

        Settings.Global.putInt(
                mContext.getContentResolver(),
                "ub.power_control.by_info", 0);

        return true;
    }

}
