package chico.bikepowermeter.dataprocessing;

import java.util.ArrayList;
import java.util.List;

import chico.bikepowermeter.dataprocessing.cycledetectors.CycleDetector;
import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalyser;
import chico.bikepowermeter.services.service_helpers.GlobalParameters;
import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;

/**
 * Created by chico on 27/06/2015. Uhu!
 */
public class SignalProcessingMaster extends AudioDataConsumerThread {

    private final SignalProcessingResultListener mSignalProcessingResultListener;
    private final List<CycleDetector> mCycleDetectors = new ArrayList<>();
    private long mSampleCounter = 0;
    private long mLastSampleCount = 0;
    private final float mMinCycleTime;
    private final CycleAnalyser mCycleAnalyser = new CycleAnalyser(GlobalParameters.MAX_CYCLE_TIME);
    private boolean mCycleStarted = false;
    private final int mSampleFrequency;

    public SignalProcessingMaster(
            final int buffer_size,
            final float min_cycle_time,
            final SignalProcessingResultListener signal_processing_result_listener)
    {
        super(buffer_size);
        mMinCycleTime = min_cycle_time;
        mSignalProcessingResultListener = signal_processing_result_listener;
        mSampleFrequency = GlobalParameters.getInstance().getSampleFrequency();
    }
    
    public void addCycleDetector(final CycleDetector cycle_detector){
        mCycleDetectors.add(cycle_detector);
    }

    @Override
    public void workOnData(short[] buffer, int start, int end, int available, int sample_rate, boolean new_start) {
        List<CycleAnalysisResult> available_results = null;

        int j = start;
        for(int i=0; i<available; i++){
            short data = buffer[j];

            if(!mCycleAnalyser.putData(data)){
                mCycleStarted = false;
                mCycleAnalyser.reset();
                mCycleAnalyser.putData(data);
            }

            if(detectCycle(data)){
                if(mCycleStarted){
                    final CycleAnalysisResult results = mCycleAnalyser.analyse();
                    if(available_results == null){
                        available_results = new ArrayList<>();
                    }
                    available_results.add(results);
                }
                mCycleStarted = true;
                mCycleAnalyser.reset();
            }

            j++;
            if(j >= buffer.length){
                j = 0;
            }
        }

        if(consumeDataAndValidate(available)){
            if(available_results != null){
                for(final CycleAnalysisResult result : available_results){
                    mSignalProcessingResultListener.putCycleProfile(result);
                }
            }
        }
        else{
            mCycleStarted = false;
            mCycleAnalyser.reset();
        }
    }

    private boolean detectCycle(final float data){
        mSampleCounter++;

        boolean cycle = false;
        for(CycleDetector d : mCycleDetectors){
            final boolean c = d.putData(data);
            cycle = cycle || c;
        }

        if(cycle){
            final double elapsed_time = (double)(mSampleCounter - mLastSampleCount) / (double)mSampleFrequency;
            mLastSampleCount = mSampleCounter;
            if(elapsed_time > mMinCycleTime){
                return true;
            }
        }

        return false;
    }
}
