package com.ub.pru.newinfoprocessor.communication.connection.info;

import android.content.Context;
import android.util.Log;

import com.ub.pru.newinfoprocessor.communication.connection.IConnection;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoEraseRadio;
import com.ub.pru.newinfoprocessor.communication.protocol.cmd.ProtoKeepAlive;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoReceiveURN;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoReqBitResult;
import com.ub.pru.newinfoprocessor.communication.protocol.cmd.ProtoReqUrn;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoRespURN;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoSendDataToRadio;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoSetPowerSupply;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoUnusedPowerSupply;
import com.ub.pru.newinfoprocessor.communication.protocol.ack.ProtoUsePowerSupply;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.utils.Define;

import java.util.ArrayList;

public class InfoConnection implements IConnection {

    private static final String TAG = "Connection";
    private Context mContext;
    private InfoTransport mTransport;
    private ArrayList<IProto> mProtocolList = new ArrayList<>();
    private Listener mListener;
    private D2DTransport mD2DTransport;

    @Override public void init(Context context, InfoTransport transport, D2DTransport d2DTransport) {
        mContext = context;
        mTransport = transport;
        mD2DTransport = d2DTransport;
    }

    @Override
    public void deInit() {
        stop();
        mD2DTransport = null;
        mTransport = null;
        mContext = null;
    }

    public void start(Listener listener) {

        Log.v(TAG, "start ++");

        mListener = listener;

        //사전 정의된 ICD별로 Rcv 타입클래스 저장
        mProtocolList.add(new ProtoEraseRadio());
        mProtocolList.add(new ProtoReceiveURN());
        mProtocolList.add(new ProtoReqUrn());
        mProtocolList.add(new ProtoRespURN());
        mProtocolList.add(new ProtoSendDataToRadio());
        mProtocolList.add(new ProtoSetPowerSupply());
        mProtocolList.add(new ProtoUnusedPowerSupply());
        mProtocolList.add(new ProtoUsePowerSupply());
        mProtocolList.add(new ProtoReqBitResult());
        mProtocolList.add(new ProtoKeepAlive());

        for ( IProto protocol : mProtocolList ) {
            protocol.init(mContext, mTransport, mD2DTransport);
        }

        mTransport.registerListener(mTransportListener);
        mTransport.start();

        Log.v(TAG, "start --");
    }

    public void stop() {
        Log.v(TAG, "stop ++");

        mTransport.setInfoConnectState(Define.CONNECTION_STATE_DISCONNECTED);

        mTransport.stop();

        for ( IProto protocol : mProtocolList ) {
            protocol.deInit();
        }
        mProtocolList.clear();

        mListener = null;

        Log.v(TAG, "stop --");
    }

    private IProto getProtocol(byte type){
        for ( IProto protocol : mProtocolList ) {
            if ( protocol.getType() == type ) {
                return protocol;
            }
        }
        Log.w(TAG,  type + "  not exist in mProtocolList");
        return null;
    }

    private InfoTransport.TransportListener mTransportListener = new InfoTransport.TransportListener() {
        @Override public void onReceiveError(InfoTransport transport) {
            Log.e(TAG, "onReceiveError++");
            if ( mListener != null ) {
                mListener.onDisconnected(InfoConnection.this);
            }
        }

        @Override public void onReceiveTimeout(InfoTransport transport) {
            Log.e(TAG, "onReceiveTimeout++");
            if ( mListener != null ) {
                mListener.onTimeout(InfoConnection.this);
            }
        }

        @Override public void onConnectInfo() {
            getProtocol(Define.PROTOCOL_ID_REQ_URN).processing();
        }

        @Override public void onCheckConnecting() {
            getProtocol(Define.PROTOCOL_ID_KEEP_ALIVE).processing();
        }

        @Override public void onReceiveData(TransportPacket packet) {
            //수신된 패킷에 맞는 타입의 프로세스를 진행한다
            if ( packet.getFlag() != Define.FLAG_PACKET_ACK ) {
                getProtocol(packet.getType()).processing(packet);
            }
        }
    };


}
