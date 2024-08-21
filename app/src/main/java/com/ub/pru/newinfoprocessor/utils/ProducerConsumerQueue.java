package com.ub.pru.newinfoprocessor.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProducerConsumerQueue<E> {

    private static final String TAG = ProducerConsumerQueue.class.getSimpleName();
    private String mTAG;
    private int mCapacity;
    private BlockingQueue<E> mProducerPool;
    private BlockingQueue<E> mConsumerPool;

    public ProducerConsumerQueue(String tag, int capacity) {
        mTAG = tag;
        mCapacity = capacity;
        mProducerPool = new ArrayBlockingQueue<>(capacity);
        mConsumerPool = new ArrayBlockingQueue<>(capacity);
    }

    public int capacity() {
        return mCapacity;
    }

    public int register(E e) throws InterruptedException {
        mProducerPool.put(e);
        return mProducerPool.size();
    }

    public void clear() {
        mProducerPool.clear();
        mConsumerPool.clear();
    }

    public E prepare() throws InterruptedException {
        E e = mProducerPool.take();
        return e;
    }

    public void produce(E e) throws InterruptedException {
        mConsumerPool.put(e);
    }

    public E consume() throws InterruptedException {
        return consume(0);
    }

    public E consume(int timeout_ms) throws InterruptedException {
        E e = null;

        if ( timeout_ms > 0 ) {
            e = mConsumerPool.poll((long)timeout_ms, TimeUnit.MILLISECONDS);
        }
        else {
            e = mConsumerPool.take();
        }

        return e;
    }

    public void recall(E e) throws InterruptedException {
        mProducerPool.put(e);
    }

    public void recallAll() {
        E e;
        while ( mConsumerPool.isEmpty() == false ) {
            e = mConsumerPool.remove();
            if ( e != null ) {
                mProducerPool.offer(e);
            }
        }
    }

    public int sizeOfProducer() {
        return mProducerPool.size();
    }

    public int sizeOfConsumer() {
        return mConsumerPool.size();
    }
}
