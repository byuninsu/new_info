package com.ub.pru.newinfoprocessor.communication.protocol.cmd;

import android.content.Context;
import android.provider.Settings;

import com.ub.pru.newinfoprocessor.communication.connection.info.InfoTransport;
import com.ub.pru.newinfoprocessor.communication.connection.info.TransportPacket;
import com.ub.pru.newinfoprocessor.communication.connection.d2d.D2DTransport;
import com.ub.pru.newinfoprocessor.communication.protocol.IProto;
import com.ub.pru.newinfoprocessor.utils.Define;

import java.nio.ByteBuffer;

public class ProtoRespBitResult implements IProto {

    private static final String TAG = "ProtoReceiveURN";

    private static final int BIT_BUF_SIZE = 6;
    private static final byte P_BIT = 0x01;
    private static final byte C_BIT = 0x02;
    private static final byte I_BIT = 0x03;
    private static final int BIT_RESULT_SUCCESS = 0;
    private static final int BIT_RESULT_ERROR = 1;
    private Context mContext;
    private byte mType = Define.PROTOCOL_ID_RES_BIT_RESULT;
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


    /* bit : 0x00(정상), 0x01(오류), 0x02(알수없음) */
    private ByteBuffer getBitPayload(String bitType) {
        ByteBuffer bitBuf = ByteBuffer.allocate(BIT_BUF_SIZE);
        byte batteryBit = BIT_RESULT_ERROR;

        // GPS 모듈 진단:
        bitBuf.put(getBitIntegerToByte(bitType + "gps_module"));

        // 메모리 진단:
        bitBuf.put(getBitIntegerToByte(bitType + "storage"));

        // 배터리 진단(배터리 잔량 및 온도):
        batteryBit = getBitIntegerToByte(bitType + "battery");
        if ( batteryBit == (byte) 0x00 ) {
            int batteryLevel = getBitIntegerToByte(bitType + "battery_level");
            int temperature = getBitIntegerToByte(bitType + "battery_temperature");

            // Battery Level is Integer to HexByte:
            bitBuf.put((byte) (batteryLevel & 0xFF));
            bitBuf.put((byte) (temperature & 0xFF));
        }
        else {
            int temperature = getBitIntegerToByte(bitType + "battery_temperature");

            bitBuf.put((byte) 0xFF);
            bitBuf.put((byte) (temperature & 0xFF));
        }

        // 블루투스 진단:
        bitBuf.put(getBitIntegerToByte(bitType + "bluetooth"));

        // 통신중계부 진단:
        bitBuf.put(getBitIntegerToByte(bitType + "d2d"));

        bitBuf.flip();

        return bitBuf;
    }

    private byte getBitIntegerToByte(String key) {
        byte value = (byte) Settings.Global.getInt(
                mContext.getContentResolver(), key, BIT_RESULT_ERROR);

        return value;
    }

    @Override public boolean processing() {
        return true;
    }

    @Override public boolean processing(TransportPacket transportPacket) {
        ByteBuffer buffer = ByteBuffer.allocate(Define.PACKET_LENGTH_PAYLOAD_BIT_RESPONSE);

        //keepAlive 송부
        TransportPacket packet = null;
        packet = mTransport.getSendQueue();

        packet.setType(mType);
        packet.setFlag(Define.FLAG_PACKET_CMD);
        packet.setSeq(mTransport.getSeq());

        buffer.put(transportPacket.getPayload());

        // 진단 타입에 따른 진단결과 추가
        if ( transportPacket.getPayload()[0] == P_BIT ) {
            //초기 진단
            buffer.put(getBitPayload("ub.p_bit."));
        }
        else if ( transportPacket.getPayload()[0] == C_BIT ) {
            //주기적 진단
            buffer.put(getBitPayload("ub.c_bit."));
        }
        else if ( transportPacket.getPayload()[0] == I_BIT ) {
            //사용자 진단
            buffer.put(getBitPayload("ub.i_bit."));
        }

        mTransport.setSendQueue(packet);

        return true;
    }

}
