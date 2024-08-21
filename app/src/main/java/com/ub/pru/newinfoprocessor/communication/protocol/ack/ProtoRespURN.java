package com.ub.pru.newinfoprocessor.communication.protocol.ack;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;
import com.ub.pru.newinfoprocessor.utils.Utils;

public class ProtoRespURN implements IProto {

    private static final String TAG = "ProtoRespURN";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_RES_URN;
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

        //ack 송부

        //Ack메시지 송부
        TransportPacket packet = null;
        packet = mTransport.getAckSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_ACK);
        packet.setSeq(transportPacket.getSeq());

        mTransport.setAckSendQueue(packet);


        //수신 urn 처리
        Log.i(TAG, "ProtoRespURN Processing ++");

        int info_urn = 0;
        int radio_urn = 0;

        info_urn = Utils.byte2Int(transportPacket.getPayload());
        radio_urn = Settings.Global.getInt
                (mContext.getContentResolver(), "ub.login", 0);

        if ( info_urn == radio_urn ) {
            mTransport.setInfoConnectState(mTransport.getId());
            Log.d(TAG, "URN match success Set Accord (true)");
        }


        return true;
    }


}
