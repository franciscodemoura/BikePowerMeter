// PowerMeterInterface.aidl
package chico.bikepowermeter;

import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;

interface PowerMeterInterface {
    float getCadence();
    int [] getGear();
    CycleAnalysisResult getRawCycleData();
}
