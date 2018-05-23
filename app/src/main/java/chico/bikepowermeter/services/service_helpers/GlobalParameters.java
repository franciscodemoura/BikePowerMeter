package chico.bikepowermeter.services.service_helpers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chico on 10/09/2015. Uhu!
 */
public class GlobalParameters {
    public static final int SECONDARY_SAMPLE_FREQ = 44100;
    public static final int PRIMARY_SAMPLE_FREQ = 48000;
    public static final float MAX_CYCLE_TIME = 4.0f;
    public static final int READER_BUFFER_SIZE = 4096;
    public static final int CONSUMER_BUFFER_SIZE = 2*1024*1024;
    public static final float MIN_CYCLE_TIME = 0.3f;
    public static final double GEAR_FREQUENCY_PROBE_RANGE_COEFFICIENT = 1.5;
    public static final float FINE_TUNE_MAX_FREQUENCY_STEP = 0.1f;
    public static final float SILENCE_DETECTOR_VALUE_THRESHOLD = 3.0f;
    public static final int SILENCE_DETECTOR_COUNT_THRESHOLD = 64;


    private static GlobalParameters ourInstance = new GlobalParameters();

    public static GlobalParameters getInstance() {
        return ourInstance;
    }

    private final AtomicInteger mSampleFreq = new AtomicInteger(SECONDARY_SAMPLE_FREQ);
    private final AtomicIntegerArray mGearTeeth = new AtomicIntegerArray(new int [] {39, 52});
    private final AtomicInteger mPulseDetectionRelativePulseDeviation = new AtomicInteger(70);
    private final AtomicReference<Float> mPulseDetectionAvgForgettingRate = new AtomicReference<>(new Float(0.00001f));

    private GlobalParameters() {
    }

    public int getSampleFrequency(){
        return mSampleFreq.get();
    }

    public void setSampleFrequency(final int freq){
        mSampleFreq.set(freq);
    }

    public int getPulseDetectionRelativePulseDeviation(){
        return mPulseDetectionRelativePulseDeviation.get();
    }

    public void setPulseDetectionRelativePulseDeviation(final int dev){
        mPulseDetectionRelativePulseDeviation.set(dev);
    }

    public float getPulseDetectionAvgForgettingRate(){
        return mPulseDetectionAvgForgettingRate.get();
    }

    public void setPulseDetectionAvgForgettingRate(final float fr){
        mPulseDetectionAvgForgettingRate.set(fr);
    }

    public int getGearCount(){
        return mGearTeeth.length();
    }

    public int getGearTeeth(final int gear){
        return mGearTeeth.get(gear);
    }

    public void setGearTeeth(final int gear, final int teeth){
        mGearTeeth.set(gear,teeth);
    }
}
