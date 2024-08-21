package com.ub.pru.newinfoprocessor.communication.protocol.cmd;

import static com.ub.pru.newinfoprocessor.utils.Utils.Int2Byte;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

public class ProtoSendURN implements IProto {

    private static final String TAG = "ProtoSendURN";
    private static final String SETTINGS_LOGIN_URN = "ub.login";
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_SEND_URN;
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
        return false;
    }

    @Override public boolean processing(TransportPacket transportPacket) {

        //현재 접속 URN 가져오기
        int radioUrn = Settings.Global.getInt(mContext.getContentResolver(),
                SETTINGS_LOGIN_URN,1);


        //패킷 가져와서 송부
        TransportPacket packet = null;
        packet = mTransport.getSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_CMD);
        packet.setSeq(mTransport.getSeq());
        packet.setPayload(Int2Byte(radioUrn));

        mTransport.setSendQueue(packet);

        return true;
    }


}
