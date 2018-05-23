package chico.bikepowermeter.dataprocessing;

import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;

/**
 * Created by chico on 28/06/2015. Uhu!
 */
public interface SignalProcessingResultListener {
    void putCycleProfile(CycleAnalysisResult result);
}
