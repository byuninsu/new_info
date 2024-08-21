package com.ub.pru.newinfoprocessor.utils;

public class Define {

    //read Packet size
    public static final int PACKET_SIZE_READ_THREAD = 450;

    // protocol ID
    public static final byte PROTOCOL_ID_POWER_CONTROL_BY_D2D = (byte) 0x10;
    public static final byte PROTOCOL_ID_UNITED_POWER_SUPPLY_MODE = (byte) 0x11;
    public static final byte PROTOCOL_ID_SELF_POWER_SUPPLY_MODE = (byte) 0x12;
    public static final byte PROTOCOL_ID_ERASE_RADIO = (byte) 0x20;
    public static final byte PROTOCOL_ID_ERASE_PHONE = (byte) 0x21;
    public static final byte PROTOCOL_ID_TRANSMIT_DATA_TO_RADIO = (byte) 0x30;
    public static final byte PROTOCOL_ID_TRANSMIT_DATA_TO_INFOPROCESSOR = (byte) 0x31;
    public static final byte PROTOCOL_ID_REQ_BIT_RESULT = (byte) 0x40;
    public static final byte PROTOCOL_ID_RES_BIT_RESULT = (byte) 0x41;
    public static final byte PROTOCOL_ID_TEAM_MEMBER_INFO = (byte) 0x50;
    public static final byte PROTOCOL_ID_KEEP_ALIVE = (byte) 0x60;
    public static final byte PROTOCOL_ID_DATA_CONTROL = (byte) 0x70;
    public static final byte PROTOCOL_ID_REQ_URN = (byte) 0x80;
    public static final byte PROTOCOL_ID_RES_URN = (byte) 0x81;
    public static final byte PROTOCOL_ID_RECEIVE_URN_REQ = (byte) 0x82;
    public static final byte PROTOCOL_ID_SEND_URN = (byte) 0x83;

    // Serial Config (Stop Bits)
    public static final int SERIAL_CONFIG_ONE_STOP_BIT = 1;
    public static final int SERIAL_CONFIG_ONE_POINT_FIVE_STOP_BITS = 2;
    public static final int SERIAL_CONFIG_TWO_STOP_BITS = 3;

    // Serial Config(Parity Values)
    public static final int SERIAL_CONFIG_NO_PARITY = 0;
    public static final int SERIAL_CONFIG_ODD_PARITY = 1;
    public static final int SERIAL_CONFIG_EVEN_PARITY = 2;
    public static final int SERIAL_CONFIG_MARK_PARITY = 3;
    public static final int SERIAL_CONFIG_SPACE_PARITY = 4;

    // Packet Length
    public static final int PACKET_LENGTH_NONE_PAYLOAD = 11;
    public static final int PACKET_LENGTH_WITHOUT_PAYLOAD = 5;
    public static final int PACKET_LENGTH_PAYLOAD_BIT_RESPONSE = 7;
    public static final int PACKET_LENGTH_HEADER = 8;
    public static final int PACKET_LENGTH_SOF= 2;
    public static final int PACKET_LENGTH_TAIL = 3;
    public static final int PACKET_LENGTH_LENGTHPACKET= 2;
    public static final int PACKET_LENGTH_HEADER_WITHOUT_SOF= 6;

    //CONNECTION STATE
    public static final int CONNECTION_STATE_DISCONNECTED = 0;
    public static final int CONNECTION_STATE_BLUETOOTH = 1;
    public static final int CONNECTION_STATE_UART = 2;

    //GENERAL NUMBER
    public static final int NUM_2 = 2;
    public static final int NUM_3 = 3;
    public static final int NUM_4 = 4;
    public static final int NUM_5 = 5;
    public static final int NUM_6 = 6;
    public static final int NUM_7 = 7;
    public static final int NUM_8 = 8;
    public static final int NUM_9 = 9;
    public static final int NUM_20 = 20;
    public static final int NUM_1033 = 1033;

    //TIMEOUT_STANDARD
    public static final int TIMEOUT_STANDARD_READ_TIMEOUT = 3000;
    public static final int TIMEOUT_STANDARD_DISCONNECT_TIMEOUT = 5000;

    //Position
    public static final int POSITION_PAYLOAD = 8;

    //Flag packet
    public static final byte FLAG_PACKET_CMD = (byte) 0x70;
    public static final byte FLAG_PACKET_ACK = (byte) 0x80;

    //Sequence value
    public static final int SEQUENCE_VALUE_BYTE_ALLOCATE = 2;
    public static final int SEQUENCE_VALUE_FIRST_BYTE_STANDARD = 8;
    public static final int SEQUENCE_VALUE_MAX_SEQUENCE_SIZE = 65534;

}
