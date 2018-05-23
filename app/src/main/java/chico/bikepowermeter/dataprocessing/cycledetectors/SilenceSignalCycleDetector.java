package chico.bikepowermeter.dataprocessing.cycledetectors;

/**
 * Created by chico on 28/06/2015. Uhu!
 */
public class SilenceSignalCycleDetector extends CycleDetector {

    public enum CycleEvent{
        START,
        END
    }

    private final float mValueThreshold;
    private final int mCountThreshold;
    private long mCount = 0;
    private CycleEvent mLastEvent = CycleEvent.END;
    private float mSample1 = 0.0f;
    private float mSample2 = 0.0f;

    public SilenceSignalCycleDetector(
            final float value_threshold,
            final int count_threshold
    ){
        mValueThreshold = value_threshold;
        mCountThreshold = count_threshold;
    }

    @Override
    public boolean putData(final float sample){

        if(mSample2 >= 0.0f && mSample2 >= mSample1 && mSample2 >= sample || mSample2 <=0.0f && mSample2 <= mSample1 && mSample2 <= sample) {

            if (mLastEvent == CycleEvent.START) {
                if (Math.abs(mSample2) <= mValueThreshold) {
                    mCount++;
                    if (mCount >= mCountThreshold) {
                        mCount = 0;
                        mLastEvent = CycleEvent.END;
                        return false;
                    }
                } else {
                    mCount--;
                    if (mCount < 0) {
                        mCount = 0;
                    }
                }
            } else {
                if (Math.abs(mSample2) > mValueThreshold) {
                    mCount++;
                    if (mCount >= 3) {
                        mCount = 0;
                        mLastEvent = CycleEvent.START;
                        return true;
                    }
                } else {
                    mCount--;
                    if (mCount < 0) {
                        mCount = 0;
                    }
                }
            }
        }

        mSample1 = mSample2;
        mSample2 = sample;

        return false;
    }
}
