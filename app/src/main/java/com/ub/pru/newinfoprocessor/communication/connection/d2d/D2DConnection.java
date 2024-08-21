package com.ub.pru.newinfoprocessor.communication.connection.d2d;

import android.content.Context;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.IConnection;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.communication.protocol.d2d.ProtoDataControl;
import com.ub.pru.newinfoprocessor.communication.protocol.d2d.ProtoEraseInfoprocessor;
import com.ub.pru.newinfoprocessor.communication.protocol.d2d.ProtoSendDataToInfoprocessor;
import com.ub.pru.newinfoprocessor.communication.protocol.d2d.ProtoTeamMemberInfo;
import com.ub.pru.newinfoprocessor.utils.Define;

import java.util.ArrayList;

public class D2DConnection implements IConnection {

    private static final String TAG = "D2DConnection";

    private Context mContext;
    private D2DTransport mD2DTransport;
    private Listener mListener;
    private InfoTransport mTransport;
    final byte DATA_RECEIVED = (byte)0x10;
    final byte CALL_STATE = (byte)0x20;
    final byte LOCATION_UPDATE = (byte)0x30;
    final byte REMOTE_ERASE = (byte)0x40;
    private ArrayList<IProto> mProtocolList = new ArrayList<>();


    @Override
    public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mD2DTransport = d2DTransport;
        mContext = context;
        mTransport = transport;
    }

    @Override
    public void deInit() {
        stop();
        mD2DTransport = null;
        mTransport = null;
        mContext = null;
    }

    @Override
    public void start(Listener listener) {

        Log.v(TAG, "D2DConnection start ++");

        mListener = listener;

        mProtocolList.add(new ProtoSendDataToInfoprocessor());
        mProtocolList.add(new ProtoDataControl());
        mProtocolList.add(new ProtoTeamMemberInfo());
        mProtocolList.add(new ProtoEraseInfoprocessor());

        for ( IProto protocol : mProtocolList ) {
            protocol.init(mContext, mTransport, mD2DTransport);
        }

        mD2DTransport.registerListener(mD2DTransportListener);
        mD2DTransport.start(mContext);

        Log.v(TAG, "D2DConnection start --");
    }

    @Override
    public void stop() {
        Log.v(TAG, "stop ++");

        for ( IProto protocol : mProtocolList ) {
            protocol.deInit();
        }
        mProtocolList.clear();

        if(mD2DTransport != null){
            mD2DTransport.stop();
            mD2DTransport = null;
        }

        mListener = null;
        mContext = null;

        Log.v(TAG, "stop --");
    }

    private IProto getProtocol(byte type){
        IProto ret = null;

        for ( IProto protocol : mProtocolList ) {
            if ( protocol.getType() == type ) {
                ret = protocol;
                break;
            }
        }
        return ret;
    }


    private D2DTransport.D2DTransportListener mD2DTransportListener = new D2DTransport.D2DTransportListener() {

        @Override public void onReceiveError() {
            Log.e(TAG, "onReceiveError++");
            if ( mListener != null ) {
                mListener.onDisconnected( D2DConnection.this);
            }
        }

        @Override public void SendDataToInfoprocessor(TransportPacket transportPacket) {

            Log.d( TAG," d2dPacketInfo.getType() : "  +
                    String.format("%02x ", transportPacket.getType() & 0xff) );

            switch (transportPacket.getType()) {
                case DATA_RECEIVED :
                    getProtocol(Define.PROTOCOL_ID_TRANSMIT_DATA_TO_INFOPROCESSOR).
                            processing(transportPacket);
                    break;

                case CALL_STATE :
                    getProtocol(Define.PROTOCOL_ID_DATA_CONTROL).
                            processing(transportPacket);
                    break;

                case LOCATION_UPDATE :
                    getProtocol(Define.PROTOCOL_ID_TEAM_MEMBER_INFO).
                            processing(transportPacket);
                    break;

                case REMOTE_ERASE :
                    getProtocol(Define.PROTOCOL_ID_ERASE_PHONE).
                            processing(transportPacket);
                    break;
            }

        }

    };

}
