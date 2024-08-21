package com.ub.pru.newinfoprocessor.communication.connection.info;

import android.util.Log;

import com.ub.pru.newinfoprocessor.utils.Define;
import com.ub.pru.newinfoprocessor.utils.Utils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransportPacket {

    private static final String TAG = "TransportPacket";
    private AtomicBoolean mCompleted = new AtomicBoolean(false);
    private Header mHeader = new Header();
    private Sof mSof = new Sof();
    private ByteBuffer mData;
    private ByteBuffer mPayloadBuffer;
    private ByteBuffer mTailBuffer = ByteBuffer.allocate(Define.NUM_3);
    private byte mFlag;
    private byte mType;
    private byte[] mSeq;
    private byte[] defSof = {0x3b, (byte) 0x9f};
    private byte[] defEof = {(byte) 0x23, (byte) 0x7f};
    private int mPacketLength = 0;
    private int mPayloadSize = 0;
    private boolean isPayloadComplete;
    private boolean isTailComplete;

    public byte getType() {
        return mType;
    }

    public void setType(byte mType) {
        this.mType = mType;
    }

    public byte[] getSeq() {
        return mSeq;
    }

    public void setSeq(byte[] mSeq) {
        this.mSeq = mSeq;
    }

    public byte[] getPayload() {
        return mPayloadBuffer.array();
    }

    public void setPayload(byte[] mPayload) {
        this.mPayloadBuffer = ByteBuffer.wrap(mPayload);
    }

    public void setData(ByteBuffer data) {
        mData = data;
    }

    public byte[] getData() {
        return mData.array();
    }

    public void setFlag(byte falg) {
        mFlag = falg;
    }

    public byte getFlag() {
        return mFlag;
    }

    public boolean isCompleted() {
        return mCompleted.get();
    }


    private class Sof {
        private ByteBuffer mSofBuffer = ByteBuffer.allocate(Define.PACKET_LENGTH_SOF);
        private AtomicBoolean mCompleted = new AtomicBoolean(false);

        public void clear() {
            mCompleted.set(false);
            mSofBuffer.clear();
        }

        public boolean isCompleted() {
            return mCompleted.get();
        }

        public ByteBuffer getBuffer() {
            return mSofBuffer;
        }

        public int size() {
            return mSofBuffer.limit();
        }

        public int parse(byte[] buffer, int offset, int length) {
            int parsedBytes = 0;

            for ( int i = 0; i < length - offset; i++ ) {

                if ( buffer[offset + i] == defSof[0] ) {
                    mSofBuffer.put(buffer[offset + i]);
                    if ( buffer[offset + i + 1] == defSof[1] ) {
                        mSofBuffer.put(buffer[offset + i + 1]);

                        Log.d(TAG, "FIND SOF !  mSofBuffer : " + Utils.byteArrayToHex(mSofBuffer.array()));

                        mSofBuffer.flip();

                        parsedBytes = i + 2;
                        mCompleted.set(true);
                        break;
                    }
                    else {
                        clear();
                    }
                }

            }

            Log.d(TAG, "return parsedBytes : " + parsedBytes);

            return parsedBytes;
        }
    }


    private class Header {

        private ByteBuffer mHeaderBuffer = ByteBuffer.allocate(Define.PACKET_LENGTH_HEADER);
        private AtomicBoolean mCompleted = new AtomicBoolean(false);
        private int mPacketSize = 0;

        public void clear() {
            mCompleted.set(false);
            mHeaderBuffer.clear();
            mPacketSize = 0;
        }

        public boolean isCompleted() {
            return mCompleted.get();
        }

        public ByteBuffer getBuffer() {
            return mHeaderBuffer;
        }

        public int size() {
            return mHeaderBuffer.limit();
        }

        public int parse(byte[] buffer, int offset, int length) {
            int parsedBytes = 0;

            //parse header
            if ( !mSof.isCompleted() ) {
                parsedBytes += mSof.parse(buffer, offset, length);
                if ( !mSof.isCompleted() ) {
                    return parsedBytes;
                }
            }

            //copy payload
            int remain_src_size = length - (offset + parsedBytes);
            int remain_dst_size = Define.PACKET_LENGTH_HEADER_WITHOUT_SOF;
            int copy_size = Math.min(remain_src_size, remain_dst_size);
            Log.d(TAG, " Header remain_dst_size : " + remain_dst_size + ",  copy_size : " + copy_size);

            Log.d(TAG, " Header put mSof.getBuffer() : " + Utils.byteArrayToHex(mSof.getBuffer().array()));

            mHeaderBuffer.put(mSof.getBuffer());
            mHeaderBuffer.put(buffer, offset + parsedBytes, copy_size);
            parsedBytes += copy_size;

            //completed
            if ( mHeaderBuffer.position() >= Define.PACKET_LENGTH_HEADER_WITHOUT_SOF ) {
                mCompleted.set(true);
                mHeaderBuffer.flip();

                Log.d(TAG, " Header parse completed : " + Utils.byteArrayToHex(mHeaderBuffer.array()));

                //FLAG
                setFlag(mHeaderBuffer.array()[4]);

                //TYPE
                setType(mHeaderBuffer.array()[5]);

                //SEQ
                setSeq(Utils.copyBytes(mHeaderBuffer.array(), Define.NUM_6, Define.NUM_2));

                //LENGTH
                mPacketSize = getLength(mHeaderBuffer.array());
                mPayloadSize = mPacketSize - Define.PACKET_LENGTH_WITHOUT_PAYLOAD;
                mPacketLength = mPacketSize + defSof.length + Define.PACKET_LENGTH_LENGTHPACKET + defEof.length;
            }

            return parsedBytes;
        }
    }

    public void clear() {

        Log.d(TAG, "TransportPacket clear() ++ ");
        mCompleted.set(false);
        mSof.clear();
        mHeader.clear();
        mPayloadSize = 0;
        mPacketLength = 0;

        if ( mPayloadBuffer != null ) {
            mPayloadBuffer.clear();
            mPayloadBuffer = null;
            isPayloadComplete = false;
            Log.d(TAG, "TransportPacket clear(), mPayloadBuffer.clear() !! ");
        }

        if ( mTailBuffer != null ) {
            mTailBuffer.clear();
            isTailComplete = false;
        }

        if ( mData != null ) {
            mData.clear();
            mData = null;
        }
    }

    public int parse(byte[] buffer, int offset, int length) {
        int parsedBytes = 0;

        //parse header
        if ( !mHeader.isCompleted() ) {
            parsedBytes += mHeader.parse(buffer, offset, length);
            if ( !mHeader.isCompleted() ) {
                return parsedBytes;
            }
        }

        //////////////////////////////////////////////////////
        // parse payload
        //////////////////////////////////////////////////////
        if ( parsedBytes < length ) {
            //allocate buffer
            Log.d(TAG, " payload parse mPacketLength : " + mPacketLength + ",   mHeader.size() : " + mHeader.size() + ",  Define.PACKET_LENGTH_TAIL : " + Define.PACKET_LENGTH_TAIL);
            if ( mPayloadBuffer == null || mPayloadBuffer.capacity() < mPayloadSize ) {
                Log.d(TAG, "mPayloadBuffer allocate :  : " + (mPacketLength - mHeader.size() - mTailBuffer.limit()));
                mPayloadBuffer = ByteBuffer.allocate(mPacketLength - (mHeader.size() + Define.PACKET_LENGTH_TAIL));
            }

            //copy payload
            int remain_src_size = length - (offset + parsedBytes);
            int payLoad_copy_size = mPayloadSize - mPayloadBuffer.position();
            int copy_size = Math.min(remain_src_size, payLoad_copy_size);

            int remain_tail_src_size = remain_src_size - copy_size;
            int tail_dst_size = Define.PACKET_LENGTH_TAIL - mTailBuffer.position();
            int tail_copy_size = Math.min(tail_dst_size,remain_tail_src_size);

            Log.d(TAG, " payload remain_src_size : " + remain_src_size + ",   payLoad_size : " + payLoad_copy_size + ",  copy_size : " + copy_size);

            if ( payLoad_copy_size > 0 ) {
                mPayloadBuffer.put(buffer, offset + parsedBytes, copy_size);


                parsedBytes += copy_size;

                Log.d(TAG, "mPayloadBuffer.position() : "+ mPayloadBuffer.position());

                if (mPayloadBuffer.position() == mPayloadSize) {
                    Log.d(TAG, "mPayloadBuffer isPayloadComplete !!  PAYLOAD : "+ Utils.byteArrayToHex(mPayloadBuffer.array()));
                    mPayloadBuffer.flip();
                   isPayloadComplete = true;
                }
            }else{
                isPayloadComplete = true;
            }
            if (isPayloadComplete && tail_copy_size > 0 ) {
                mTailBuffer.put(buffer, offset + parsedBytes, tail_copy_size);


                if (mTailBuffer.position() == Define.PACKET_LENGTH_TAIL){
                    mTailBuffer.flip();
                    isTailComplete = true;
                }

                parsedBytes += tail_copy_size;
            }


            //completed
            if ( (mPayloadBuffer == null || isPayloadComplete)  && isTailComplete ) {
                receiveBuild();

                mCompleted.set(true);
            }
        }
        return parsedBytes;
    }

    public int getLength(byte[] data) {
        int length = 0;

        if ( data.length >= Define.PACKET_LENGTH_HEADER_WITHOUT_SOF ) {
            length = Utils.getLength(data);
        }

        return length;
    }

    public boolean checkCRC() {
        int byteSum = 0;
        int packetCheckSum = 0;
        int totalCheckSum = 0;
        String hexToTotalCheckSum = "";

        for ( int i = 0; i < Define.PACKET_LENGTH_HEADER_WITHOUT_SOF; i++ ) {
            byteSum += (mHeader.getBuffer().array()[i + 2] & 0xff);
        }

        if ( mPayloadBuffer != null ) {
            for ( byte temp : mPayloadBuffer.array() ) {
                byteSum += (temp & 0xff);
            }
        }

        packetCheckSum = (getData()[getData().length - Define.NUM_3] & 0xFF);
        totalCheckSum = (byteSum + packetCheckSum);
        hexToTotalCheckSum = Integer.toHexString(totalCheckSum);

        if ( !hexToTotalCheckSum.substring(hexToTotalCheckSum.length() - Define.NUM_2).equals("00") ) {
            Log.d(TAG, "TransportPacket Err checkSum!!,  Byte :  " + Utils.byteArrayToHex(mData.array()));

            return false;
        }
        else {
            return true;
        }
    }

    public void build() {
        if ( mPayloadBuffer == null ) {
            mData = ByteBuffer.allocate(Define.PACKET_LENGTH_NONE_PAYLOAD);
        }
        else {
            mData = ByteBuffer.allocate(Define.PACKET_LENGTH_NONE_PAYLOAD
                    + mPayloadBuffer.limit());
        }
        //SOF
        mData.put(defSof);
        //LENGTH
        if ( mPayloadBuffer == null ) {
            mData.put(Utils.getLength(Define.PACKET_LENGTH_WITHOUT_PAYLOAD));
        }
        else {
            mData.put(Utils.getLength(Define.PACKET_LENGTH_WITHOUT_PAYLOAD
                    + mPayloadBuffer.limit()));
        }
        //FLAG
        mData.put(mFlag);
        //TYPE
        mData.put(mType);
        //SEQ
        mData.put(mSeq);
        //PAYLOAD
        if ( mPayloadBuffer != null ) {
            mData.put(mPayloadBuffer);
        }
        //CHECKSUM
        mData.put(Utils.getCheckSum(mData));
        //EOF
        mData.put(defEof);
    }

    public void receiveBuild() {
        if ( mPayloadBuffer == null ) {
            mData = ByteBuffer.allocate(Define.PACKET_LENGTH_NONE_PAYLOAD);
        }
        else {
            Log.d(TAG, "mPayloadBuffer Not Null mPayloadBuffer.limit() : " + mPayloadBuffer.limit());
            mData = ByteBuffer.allocate(Define.PACKET_LENGTH_NONE_PAYLOAD
                    + mPayloadBuffer.limit());
        }
        //HEADER
        if ( mHeader.size() > 0 ) {
            mData.put(mHeader.getBuffer());
        }
        //PAYLOAD
        if ( mPayloadBuffer != null ) {
            Log.d(TAG, "mPayloadBuffer put mPayloadBuffer.array() : " + Utils.byteArrayToHex(mPayloadBuffer.array()));
            mData.put(mPayloadBuffer);
        }

        if ( mTailBuffer.limit() > 0 ) {
            mData.put(mTailBuffer);
        }
    }

}
