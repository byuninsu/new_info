package com.ub.pru.newinfoprocessor.utils;

import android.util.Log;

import java.nio.ByteBuffer;

public class Utils {

    private static final String TAG = "Utils";

    private static final int ZERO = 0;
    private static final int FIRST = 1;
    private static final int SECOND = 2;
    private static final int THIRD = 3;
    private static final int ZERO_BASE = 24;
    private static final int FIRST_BASE = 16;
    private static final int SECOND_BASE = 8;
    private static final int THIRD_BASE = 0;
    public static final int DEF_SOF_LENGTH_SIZE = 2;
    public static final int DEF_EOF_LENGTH_SIZE = 2;


    public static String byteArrayToHex(byte[] a) {
        return byteArrayToHex(a, 0, a.length);
    }

    public static String byteArrayToHex(byte[] a, int offset, int length) {
        if ( length > 0 ) {
            StringBuilder sb = new StringBuilder();
            for ( int i=offset; i < (offset+length); i++) {
                sb.append(String.format("%02x ", a[i] & 0xff));
            }
            return sb.toString();
        }
        return "";
    }

    public static byte getCheckSum(ByteBuffer buf) {

        byte[] data = new byte[buf.position() - DEF_SOF_LENGTH_SIZE];
        byte csum = (byte) 0xff;

        System.arraycopy(buf.array(), DEF_SOF_LENGTH_SIZE, data, 0, buf.position()
                - DEF_EOF_LENGTH_SIZE);

        for ( byte b : data ) {
            csum += b;
        }

        return (byte) ~csum;
    }



    public static byte[] getLength(int len) {

        byte[] byteArray = new byte[SECOND];

        byteArray[ZERO] = (byte) (len >> SECOND_BASE);
        byteArray[FIRST] = (byte) (len);

        return byteArray;
    }

    public static int byte2Int(byte[] src) {
        int s1 = src[ZERO] & 0xFF;
        int s2 = src[FIRST] & 0xFF;
        int s3 = src[SECOND] & 0xFF;
        int s4 = src[THIRD] & 0xFF;

        return (s1 << ZERO_BASE) + (s2 << FIRST_BASE) + (s3 << SECOND_BASE) + (s4 << THIRD_BASE);
    }

    public static byte[] Int2Byte(int intValue) {

        byte[] tempByte = new byte[4];

        tempByte[0] = (byte)( (intValue >> 24) & 0xFF );
        tempByte[1] = (byte)( (intValue >> 16) & 0xFF );
        tempByte[2] = (byte)( (intValue >>  8) & 0xFF );
        tempByte[3] = (byte)( (intValue >>  0) & 0xFF );

        return tempByte;
    }

    public static int getLength(byte[] data) {
        int s1 = data[SECOND] & 0xFF;
        int s2 = data[THIRD] & 0xFF;

        return (s1 << SECOND_BASE) + (s2);
    }

    public static byte[] copyBytes(byte[] source, int startIndex, int length) {
        byte[] destination = new byte[length];
        System.arraycopy(source, startIndex, destination, 0, length);
        return destination;
    }

}
