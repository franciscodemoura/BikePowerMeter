package chico.bikepowermeter.dataprocessing.signalanalysis;

/**
 * Created by chico on 16/08/2015. Uhu!
 */
public interface FFTAndAnalysisResultListener {

    class Result{
        public final int index;
        public final float cyclePosition;
        public final float mainFrequency;
        public final int probableGear;

        Result(
                final int index,
                final float cycle_position,
                final float main_frequency,
                final int probable_gear
        ){
            this.index = index;
            cyclePosition = cycle_position;
            mainFrequency = main_frequency;
            probableGear = probable_gear;
        }
    }

    void putResult(
            int index,
            float cycle_position,
            float main_frequency,
            final int probable_gear
    );
    void setNumberOfExpectedResults(int expected_results);
    void waitForResults();
    Result readResult(int index);
}
