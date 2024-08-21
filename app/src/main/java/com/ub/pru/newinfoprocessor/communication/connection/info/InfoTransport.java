package com.ub.pru.newinfoprocessor.communication.connection.info;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ub.pru.newinfoprocessor.utils.Define;
import com.ub.pru.newinfoprocessor.utils.ProducerConsumerQueue;
import com.ub.pru.newinfoprocessor.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class InfoTransport {

    private static final String TAG = "Transport";

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private long mId;

    private TransportListener mListener;

    private ProducerConsumerQueue<TransportPacket> mSendQueue;
    private ProducerConsumerQueue<TransportPacket> mAckSendQueue;
    private ProducerConsumerQueue<TransportPacket> mReceiveQueue;

    private ReadThread mReadThread;
    private SendThread mSendThread;
    private AckSendThread mAckSendThread;
    private ParseThread mParseThread;
    private boolean mReceiveConfirm = false;
    private Object mSendLock = new Object();

    private boolean mIsKeepAlive;
    private Context mContext;
    private ArrayList<Short> mReceiveSequenceList = new ArrayList<Short>(Define.NUM_20);
    private ArrayList<Short> mSendSequenceList = new ArrayList<Short>(Define.NUM_5);
    private int mListIndex = 0;
    private long mLastReceivedTime = 0;
    private int mSeq = 0;

    private KeepAliveHandler mKeepAliveHandler;

    public InfoTransport(InputStream inputStream, OutputStream outputstream,
                         boolean isKeepAlive, long Id, Context context) {
        mInputStream = inputStream;
        mOutputStream = outputstream;
        mIsKeepAlive = isKeepAlive;
        mId = Id;
        mContext = context;
    }

    public int getSendQueueSize() {
        return mSendQueue.sizeOfConsumer();
    }

    public void registerListener(TransportListener listener) {
        mListener = listener;
    }

    public long getId() {
        return mId;
    }

    public interface TransportListener {
        void onReceiveError(InfoTransport transport);
        void onReceiveData(TransportPacket packet);
        void onReceiveTimeout(InfoTransport transport);
        void onConnectInfo();
        void onCheckConnecting();
    }

    public TransportPacket getSendQueue() {
        try {
            return mSendQueue.prepare();
        }
        catch ( InterruptedException e ) {
            Log.d(TAG, "queue ERROR !, prepare ");
            e.printStackTrace();
        }
        return null;
    }

    public void setSendQueue(TransportPacket packet) {

        packet.build();

        try {
            mSendQueue.produce(packet);
        }
        catch ( InterruptedException e ) {
            Log.d(TAG, "queue ERROR !, produce ");
            e.printStackTrace();
        }
    }

    public TransportPacket getAckSendQueue() {
        try {
            return mAckSendQueue.prepare();
        }
        catch ( InterruptedException e ) {
            Log.d(TAG, "queue ERROR !, prepare ");
            e.printStackTrace();
        }
        return null;
    }

    public void setAckSendQueue(TransportPacket packet) {

        packet.build();

        try {
            mAckSendQueue.produce(packet);
        }
        catch ( InterruptedException e ) {
            Log.d(TAG, "queue ERROR !, produce ");
            e.printStackTrace();
        }
    }

    public void setInfoConnectState(long value) {
        Settings.Global.putInt(mContext.getContentResolver(),
                "ub.info_conn_state", (int) value);
    }


    public byte[] getSeq() {
        byte[] intToByte = new byte[Define.SEQUENCE_VALUE_BYTE_ALLOCATE];

        if ( mSeq >= Define.SEQUENCE_VALUE_MAX_SEQUENCE_SIZE ) {
            mSeq = 0;
        }

        mSeq++;

        intToByte[0] |= (byte) ((mSeq & 0xFF00) >> Define.SEQUENCE_VALUE_FIRST_BYTE_STANDARD);
        intToByte[1] |= (byte) (mSeq & 0xFF);

        return intToByte;
    }


    public void start() {
        Log.v(TAG, "start ++");

        //mReceiveSequenceList 빈데이터 삽입
        for ( int i = 0; i < Define.NUM_20; i++ ) {
            mReceiveSequenceList.add((short) 0);
        }

        //mSendSequenceList 빈데이터 삽입
        for ( int i = 0; i < Define.NUM_20; i++ ) {
            mSendSequenceList.add((short) 0);
        }

        Log.d(TAG, "mSendSequenceList SIZE : " + mSendSequenceList.size());

        // queue
        try {
            // register ackSend queue
            mAckSendQueue = new ProducerConsumerQueue<>("send", 30);
            for ( int i = 0; i < mAckSendQueue.capacity(); i++ ) {
                mAckSendQueue.register(new TransportPacket());
            }

            // register send queue
            mSendQueue = new ProducerConsumerQueue<>("send", 30);
            for ( int i = 0; i < mSendQueue.capacity(); i++ ) {
                mSendQueue.register(new TransportPacket());
            }

            // register receive queue
            mReceiveQueue = new ProducerConsumerQueue<>("rcv", 30);
            for ( int i = 0; i < mReceiveQueue.capacity(); i++ ) {
                mReceiveQueue.register(new TransportPacket());
            }
        }
        catch ( InterruptedException e ) {
            e.printStackTrace();
        }

        if ( mInputStream == null ) {
            Log.e(TAG, "start InputStream failed!!");
            return;
        }

        if ( mOutputStream == null ) {
            try {
                mInputStream.close();
                mInputStream = null;
                Log.e(TAG, "start OutputStream failed!!");
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
            return;
        }

        // receive Thread
        mParseThread = new ParseThread();
        mParseThread.start();

        //read Thread
        mReadThread = new ReadThread();
        mReadThread.start();

        // send Thread
        mSendThread = new SendThread();
        mSendThread.start();

        // ackSend Thread
        mAckSendThread = new AckSendThread();
        mAckSendThread.start();

        if ( mIsKeepAlive ) {
            mLastReceivedTime = SystemClock.uptimeMillis();
            //KeepAliveHandler 초기화 및 시작
            mKeepAliveHandler = new KeepAliveHandler();
            mKeepAliveHandler.sendEmptyMessageDelayed(
                    KeepAliveHandler.MSG_CHECK_KEEP_ALIVE, KeepAliveHandler.DELAY_TIME_ONE_THOUSAND);
        }


        Log.v(TAG, "start --");
    }

    public void stop() {

        Log.v(TAG, "stop ++");

        // receive thread
        if ( mReadThread != null ) {
            mReadThread.forceStop();
            mReadThread = null;
        }

        if ( mParseThread != null ) {
            mParseThread.forceStop();
            mParseThread = null;
        }

        // send thread
        if ( mSendThread != null ) {
            mSendThread.forceStop();
            mSendThread = null;
        }

        // ackSend thread
        if ( mAckSendThread != null ) {
            mAckSendThread.forceStop();
            mAckSendThread = null;
        }

        destroyStream();

        if ( mSendQueue != null ) {
            mSendQueue.clear();
            mSendQueue = null;
        }

        if ( mAckSendQueue != null ) {
            mAckSendQueue.clear();
            mAckSendQueue = null;
        }

        if ( mReceiveQueue != null ) {
            mReceiveQueue.clear();
            mReceiveQueue = null;
        }

        mListener = null;

        mLastReceivedTime = 0;

        if ( mKeepAliveHandler != null ) {
            Log.v(TAG, "mKeepAliveHandler stop ++");
            if ( mKeepAliveHandler.hasMessages(KeepAliveHandler.MSG_CHECK_KEEP_ALIVE) ) {
                mKeepAliveHandler.removeMessages(KeepAliveHandler.MSG_CHECK_KEEP_ALIVE);
            }
            if ( mKeepAliveHandler.hasMessages(KeepAliveHandler.MSG_SEND_KEEP_ALIVE_MSG) ) {
                mKeepAliveHandler.removeMessages(KeepAliveHandler.MSG_SEND_KEEP_ALIVE_MSG);
            }
            mKeepAliveHandler = null;
        }

        Log.v(TAG, "stop --");
    }

    protected void destroyStream() {
        Log.v(TAG, "destroyStream ++");

        try {
            if ( mInputStream != null ) {
                mInputStream.close();
            }

            if ( mOutputStream != null ) {
                mOutputStream.close();
            }

        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }

        Log.v(TAG, "destroyStream --");
    }


    private class ReadThread extends Thread {
        private final String TAG = InfoTransport.TAG + ".read";

        private boolean mIsRunning = false;

        public boolean isRunning() {
            return mIsRunning;
        }

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        public void forceStop() {
            Log.d(TAG, "forceStop ++");

            if ( isRunning() ) {
                setRunning(false);
                interrupt();
            }

            try {
                join();
            }
            catch ( InterruptedException e ) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "forceStop --");
        }

        @Override
        public void run() {
            super.run();

            Log.d(TAG, "run ++");

            byte[] buffer = new byte[Define.PACKET_SIZE_READ_THREAD];
            int length = 0;
            int offset;
            TransportPacket packet = null;
            boolean isFirst = true;

            setRunning(true);

            try {
                while ( isRunning() ) {

                    length = mInputStream.read(buffer, 0, buffer.length);

                    if ( length < 0 ) {
                        Log.e(TAG, "no more data because the end of the file has been reached. (ret :" + length + ")");

                        mListener.onReceiveError(InfoTransport.this);

                        break;
                    }
                    if ( length == 0 ) {
                        continue;
                    }

                    if ( isFirst ) {
                        mListener.onConnectInfo();
                        isFirst = false;
                    }

                    Log.d(TAG, "READ BYTE : " + Utils.byteArrayToHex(buffer));
                    Log.d(TAG, "READ BYTE LENGTH : " + length);

                    mLastReceivedTime = SystemClock.uptimeMillis();

                    //SOF,EOF분석 기본 파싱, offset을 설정하여 읽어온 패킷의 길이만큼 파싱될때까지 while
                    offset = 0;

                    while ( offset < length ) {
                        //prepare data

                        if ( packet == null ) {
                            packet = mReceiveQueue.prepare();
                            packet.clear();
                        }

                        offset += packet.parse(buffer, offset, length);

                        if ( packet.isCompleted() ) {
                            Log.d(TAG, "READ Packet parsing complete : " + Utils.byteArrayToHex(packet.getData()));

                            mReceiveQueue.produce(packet);
                            packet = null;
                        }

                    }

                }
            }
            catch ( IOException | InterruptedException e ) {
                Log.e(TAG, "read Thread IOException !!");
                mListener.onReceiveError(InfoTransport.this);
                e.printStackTrace();
            }

            try {
                if ( mInputStream != null ) {
                    mInputStream.close();
                    mInputStream = null;
                }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }

            setRunning(false);

            Log.d(TAG, "run --");
        }
    }


    private class ParseThread extends Thread {
        private final String TAG = InfoTransport.TAG + ".parseThread";

        private boolean mIsRunning = false;

        public boolean isRunning() {
            return mIsRunning;
        }

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        public void forceStop() {
            Log.d(TAG, "forceStop ++");

            if ( isRunning() ) {
                setRunning(false);
                interrupt();
            }

            try {
                join();
            }
            catch ( InterruptedException e ) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "forceStop --");
        }

        @Override
        public void run() {
            super.run();

            Log.d(TAG, "run ++");

            TransportPacket packet = null;

            setRunning(true);

            try {
                while ( isRunning() ) {
                    packet = mReceiveQueue.consume();

                        if ( packet.checkCRC() ) {
                            //수신받은 패킷이 기존에도 이미 받았다면 무시
                            if ( packet.getFlag() == Define.FLAG_PACKET_CMD ) {
                                if ( compareReceiveSequence(packet.getSeq()) ) {
                                    continue;
                                }else{
                                    mListener.onReceiveData(packet);
                                    setReceiveSequence(packet.getSeq());
                                }
                            }
                            //수신받은 패킷이 ACK메시지면 시퀀스 저장관리
                            else if ( packet.getFlag() == Define.FLAG_PACKET_ACK ) {
                                if(compareSendSequence(packet.getSeq())){
                                    mSendLock.notifyAll();
                                }
                            }
                        }
                        else {
                            Log.e(TAG, "packet CRC ERROR!!");
                        }

                    mReceiveQueue.recall(packet);
                }

            }
            catch ( InterruptedException e ) {
                Log.e(TAG, "parse Thread IOException !!");
                e.printStackTrace();
            }

            mSendQueue.recallAll();

            setRunning(false);

            Log.d(TAG, "run --");
        }
    }


    private class SendThread extends Thread {
        private final String TAG = InfoTransport.TAG + ".send";

        private boolean mIsRunning = false;

        public boolean isRunning() {
            return mIsRunning;
        }

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        public void forceStop() {
            Log.d(TAG, "forceStop ++");

            if ( isRunning() ) {
                setRunning(false);
                interrupt();
            }

            try {
                join();
            }
            catch ( InterruptedException e ) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "forceStop --");
        }

        @Override
        public void run() {
            super.run();
            byte[] data;


            Log.d(TAG, "run ++");

            TransportPacket packet;
            boolean isSendComplete = false;

            setRunning(true);

            try {
                while ( isRunning() ) {
                    isSendComplete = false;

                    packet = mSendQueue.consume();

                    data = packet.getData();

                    //재전송 시나리오

                    do {
                        Log.v(TAG, "SEND PACKET : " + Utils.byteArrayToHex(data));

                        synchronized ( mOutputStream ) {
                            mOutputStream.write(data);
                        }

                        synchronized ( mSendLock ){
                            mSendLock.wait(200);
                        }

                        setSendSequence(packet.getSeq());

                    } while ( mReceiveConfirm );



                    mSendQueue.recall(packet);

                }
            }
            catch ( InterruptedException | IOException e ) {
                Log.e(TAG, "send Thread IOException !!");

                mListener.onReceiveError(InfoTransport.this);

                e.printStackTrace();
            }
            mSendQueue.recallAll();

            setRunning(false);

            Log.d(TAG, "run --");
        }
    }


    private class AckSendThread extends Thread {
        private final String TAG = InfoTransport.TAG + ".AckSendThread";

        private boolean mIsRunning = false;

        public boolean isRunning() {
            return mIsRunning;
        }

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        public void forceStop() {
            Log.d(TAG, "forceStop ++");

            if ( isRunning() ) {
                setRunning(false);
                interrupt();
            }

            try {
                join();
            }
            catch ( InterruptedException e ) {
                Log.d(TAG, "interrupted...");
            }

            Log.d(TAG, "forceStop --");
        }

        @Override
        public void run() {
            super.run();
            byte[] data;

            Log.d(TAG, "run ++");

            TransportPacket packet;

            setRunning(true);

            try {
                while ( isRunning() ) {

                    packet = mAckSendQueue.consume();


                    data = packet.getData();

                    Log.v(TAG, "SEND PACKET : " + Utils.byteArrayToHex(data));
                    synchronized ( mOutputStream ) {
                        mOutputStream.write(data);
                    }

                    mAckSendQueue.recall(packet);

                }
            }
            catch ( InterruptedException | IOException e ) {
                Log.e(TAG, "send Thread IOException !!");

                mListener.onReceiveError(InfoTransport.this);

                e.printStackTrace();
            }

            mAckSendQueue.recallAll();

            setRunning(false);

            Log.d(TAG, "run --");
        }
    }

    private boolean compareReceiveSequence(byte[] seq) {
        return mReceiveSequenceList.contains(ByteBuffer.wrap(seq).getShort());
    }

    private boolean compareSendSequence(byte[] seq) {
        return mSendSequenceList.contains(ByteBuffer.wrap(seq).getShort());
    }


    private void setReceiveSequence(byte[] seq) {
        if(mListIndex >= mReceiveSequenceList.size()){
            mListIndex = 0;
        }
        mReceiveSequenceList.set(mListIndex, ByteBuffer.wrap(seq).getShort());

        mListIndex++;
    }

    private void setSendSequence(byte[] seq) {
        if(mListIndex >= mSendSequenceList.size()){
            mListIndex = 0;
        }
        mSendSequenceList.set(mListIndex, ByteBuffer.wrap(seq).getShort());

        mListIndex++;
    }


    private class KeepAliveHandler extends Handler {

        private static final int MSG_CHECK_KEEP_ALIVE = 0;
        private static final int MSG_SEND_KEEP_ALIVE_MSG = 1;
        private static final int DELAY_TIME_ONE_THOUSAND = 1000;

        @Override
        public void handleMessage(@NonNull Message msg) {
            if ( msg.what == MSG_CHECK_KEEP_ALIVE ) {
                if ( SystemClock.uptimeMillis() - mLastReceivedTime > Define.TIMEOUT_STANDARD_DISCONNECT_TIMEOUT ) {
                    Log.e(TAG, "5secons over since the last receiveTime, Timeout!! ");
                    mListener.onReceiveTimeout(InfoTransport.this);
                }
                else {
                    if ( SystemClock.uptimeMillis() - mLastReceivedTime > Define.TIMEOUT_STANDARD_READ_TIMEOUT ) {
                        Log.d(TAG, " ReadThread time over , Send KeepAliveMessage ");
                        mKeepAliveHandler.sendEmptyMessage(MSG_SEND_KEEP_ALIVE_MSG);
                    }
                }
                mKeepAliveHandler.sendEmptyMessageDelayed(
                        MSG_CHECK_KEEP_ALIVE, DELAY_TIME_ONE_THOUSAND);
            }
            else if ( msg.what == MSG_SEND_KEEP_ALIVE_MSG ) {
                if ( getSendQueueSize() == 0 ) {
                    mListener.onCheckConnecting();
                }
            }
        }
    }


}
