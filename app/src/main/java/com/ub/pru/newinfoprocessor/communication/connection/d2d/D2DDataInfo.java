package com.ub.pru.newinfoprocessor.communication.connection.d2d;

import com.ub.pru.newinfoprocessor.utils.Utils;

import java.util.Arrays;

public class D2DDataInfo {

    private int mSrcURN;
    private int mDstURN;
    private int mType;
    private byte[] mData;
    private int mDataLength;

    public D2DDataInfo setSrcUrn(int urn) {
        mSrcURN = urn;
        return this;
    }

    public int getSrcUrn() {
        return mSrcURN;
    }

    public D2DDataInfo setDstUrn(int urn) {
        mDstURN = urn;
        return this;
    }

    public int getDstUrn() {
        return mDstURN;
    }

    public D2DDataInfo setType(int type) {
        mType = type;
        return this;
    }

    public int getType() {
        return mType;
    }

    public D2DDataInfo setData(byte[] data, int offset, int length) {
        if (mData == null || mData.length < length ) {
            mData = new byte[length];
        }

        mDataLength = length;

        System.arraycopy (data, offset, mData, 0, length);

        return this;
    }

    public byte[] getData() {
        return mData;
    }

    public int getDataLength() {
        return mDataLength;
    }

    @Override public String toString() {
        return "D2DDataInfo{" +
                "mSrcURN=" + mSrcURN +
                ", mDstURN=" + mDstURN +
                ", mType=" + mType +
                ", mData=" + Utils.byteArrayToHex(getData(), 0, getDataLength()) +
                ", mDataLength=" + mDataLength +
                '}';
    }
}
