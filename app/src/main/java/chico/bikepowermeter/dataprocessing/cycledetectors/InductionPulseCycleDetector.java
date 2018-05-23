package chico.bikepowermeter.dataprocessing.cycledetectors;

import chico.bikepowermeter.services.service_helpers.GlobalParameters;

/**
 * Created by chico on 30/06/2015. Uhu!
 */
public class InductionPulseCycleDetector extends CycleDetector {

    private float mAvgSlope = 0.0f;
    private float mPreviousSample = 32000.0f;

    public InductionPulseCycleDetector(){
    }

    @Override
    public boolean putData(float sample) {
        final float slope = Math.abs(sample - mPreviousSample);
        mPreviousSample = sample;
        final float avg_forgetting_rate = GlobalParameters.getInstance().getPulseDetectionAvgForgettingRate();
        mAvgSlope = (1.0f - avg_forgetting_rate) * mAvgSlope + avg_forgetting_rate * slope;
        return slope > mAvgSlope * GlobalParameters.getInstance().getPulseDetectionRelativePulseDeviation();
    }
}
