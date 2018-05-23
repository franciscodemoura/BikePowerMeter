package chico.bikepowermeter.dataprocessing.signalanalysis;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chico on 16/08/2015. Uhu!
 */
public class BasicFFTAndAnalysisResultListener implements FFTAndAnalysisResultListener {

    private int expectedResults;
    private final List<Result> results = new LinkedList<>();

    @Override
    public synchronized void putResult(
            final int index,
            final float cycle_position,
            final float main_frequency,
            final int probable_gear
    ){
        results.add(
                new Result(
                        index,
                        cycle_position,
                        main_frequency,
                        probable_gear
                )
        );
        notify();
    }

    @Override
    public void setNumberOfExpectedResults(final int expected_results){
        expectedResults = expected_results;
        results.clear();
    }

    @Override
    public synchronized void waitForResults(){
        while(results.size() < expectedResults){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Result readResult(final int index){
        for(final Result r : results){
            if(r.index == index){
                return r;
            }
        }
        return null;
    }
}
