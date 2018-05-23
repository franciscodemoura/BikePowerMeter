package chico.bikepowermeter.dataprocessing;

/**
 * Created by chico on 21/06/2015. Uhu!
 */
public abstract class AudioDataConsumerThread extends Thread implements AudioDataConsumer{
    private volatile int mSamplerRate;
    private final short [] mBuffer;
    private volatile int mDataStart;
    private volatile int mDataAmountAvailable;
    private volatile int mFormerDataStart;
    private volatile boolean mNewStart = true;

    public AudioDataConsumerThread(final int buffer_size){
        mBuffer = new short[buffer_size];
        mDataStart = 0;
        mDataAmountAvailable = 0;
        mFormerDataStart = 0;
    }

    @Override
    final public void setParameters(final int sample_rate){
        mSamplerRate = sample_rate;
    }

    @Override
    final public void putData(final short [] buffer, final int samples){
        if(!isAlive()){
            start();
        }

        reserveBufferSpace(samples);

        int cursor;
        synchronized (this) {
            cursor = (mDataStart + mDataAmountAvailable) % mBuffer.length;
        }

        final int s2 = cursor + samples - mBuffer.length;
        if(s2 >= 0){
            final int s1 = samples - s2;

            for (int i = 0; i < s1; i++) {
                mBuffer[cursor] = buffer[i];
                cursor++;
            }

            cursor = 0;

            for (int i = 0; i < s2; i++) {
                mBuffer[cursor] = buffer[i];
                cursor++;
            }
        }
        else {
            for (int i = 0; i < samples; i++) {
                mBuffer[cursor] = buffer[i];
                cursor++;
            }
        }

        synchronized (this) {
            mDataAmountAvailable += samples;
            notify();
        }
    }

    @Override
    public void finish(){
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void reserveBufferSpace(int samples){
        while(true) {

            final int data_start;
            final int data_available;

            synchronized (this) {
                data_start = mDataStart;
                data_available = mDataAmountAvailable;
            }

            final int space_available = mBuffer.length - data_available;
            final int space_missing = samples - space_available;

            if (space_missing <= 0) {
                break;
            }

            synchronized (this){
                if(mDataStart == data_start){
                    mDataStart = (mDataStart + space_missing) % mBuffer.length;
                    mDataAmountAvailable -= space_missing;
                    break;
                }
            }
        }
    }

    private synchronized int waitForData() throws InterruptedException {
        while(mDataAmountAvailable == 0){
            wait();
        }
        return mDataAmountAvailable;
    }

    private int getDataStart(){
        synchronized (this) {
            mFormerDataStart = mDataStart;
        }
        return mFormerDataStart;
    }

    final protected boolean consumeDataAndValidate(final int data_amount_consumed){
        mNewStart = true;
        synchronized (this){
            if(mDataStart == mFormerDataStart){
                mDataStart = (mDataStart + data_amount_consumed) % mBuffer.length;
                mDataAmountAvailable -= data_amount_consumed;
                return true;
            }
            else{
                return false;
            }
        }
    }

    @Override
    public void run(){
        while(!isInterrupted()) {

            final int data_start = getDataStart();
            final int data_available;
            try {
                data_available = waitForData();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            workOnData(
                    mBuffer,
                    data_start,
                    (data_start + data_available) % mBuffer.length,
                    data_available,
                    mSamplerRate,
                    mNewStart
            );

            mNewStart = false;
        }
    }

    public abstract void workOnData(
            final short [] buffer,
            final int start,
            final int end,
            final int available,
            final int sample_rate,
            final boolean new_start
    );
}
