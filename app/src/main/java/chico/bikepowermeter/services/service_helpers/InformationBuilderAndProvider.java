package chico.bikepowermeter.services.service_helpers;

import java.util.concurrent.atomic.AtomicReference;

import chico.bikepowermeter.dataprocessing.SignalProcessingResultListener;
import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;

/**
 * Created by chico on 20/09/2015. Uhu!
 */
public class InformationBuilderAndProvider implements SignalProcessingResultListener {

    final AtomicReference<CycleAnalysisResult> mRawData = new AtomicReference<>();

    public InformationBuilderAndProvider() {
    }

    @Override
    public void putCycleProfile(final CycleAnalysisResult result) {
        mRawData.set(result);
    }

    public CycleAnalysisResult getRawData(){
        return mRawData.get();
    }

    public int [] getGears(){
        final int [] result = new int[1];
        result[0] = 0;
        return result;
    }

    public float getCadence(){
        if(mRawData.get() != null){
            return 1.0f / mRawData.get().getCycleTime() * 60.0f;
        }
        else{
            return 0.0f;
        }
    }
}
