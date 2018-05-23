package chico.bikepowermeter.dataprocessing.signalanalysis;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chico.bikepowermeter.services.service_helpers.GlobalParameters;

/**
 * Created by chico on 05/07/2015. Uhu!
 */
public class CycleAnalyser {

    public static final int FFT_SIZE = 8192;
    public static final int HALF_FFT_SIZE = FFT_SIZE/2;

    private final float [] mBuffer;
    private int mCursor = 0;
    private final FFT.PrecomputedData mFFTPrecomputedDAta = new FFT.PrecomputedData(FFT_SIZE);
    private final FFTAndAnalysisResultListener mResultListener = new BasicFFTAndAnalysisResultListener();
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final int [] mGearTeeth;
    private final int mSampleFrequency;

    public CycleAnalyser(final float max_cycle_time){
        mSampleFrequency = GlobalParameters.getInstance().getSampleFrequency();
        mBuffer = new float[(int)(mSampleFrequency * max_cycle_time + 1.0f)];
        mGearTeeth = new int[GlobalParameters.getInstance().getGearCount()];
        for(int i=0; i<mGearTeeth.length; i++){
            mGearTeeth[i] = GlobalParameters.getInstance().getGearTeeth(i);
        }
    }

    public boolean putData(final float data){
        if(mCursor >= mBuffer.length){
            return false;
        }
        mBuffer[mCursor] = data;
        mCursor++;
        return true;
    }

    public void reset(){
        mCursor = 0;
    }

    public CycleAnalysisResult analyse(){

        final int probe_point4 = mCursor / 2;
        int probe_point2 = probe_point4 / 2;
        int probe_point1 = probe_point2 / 2;
        final int min_interval_between_probes = probe_point1 / 2;
        int probe_point6 = mCursor - probe_point2;
        int probe_point7 = mCursor - probe_point1;
        final int probe_point3 = probe_point2 + probe_point1;
        final int probe_point5 = probe_point6 - probe_point1;

        if(probe_point1 < HALF_FFT_SIZE){
            probe_point1 = HALF_FFT_SIZE;
        }
        if(mCursor - probe_point7 < HALF_FFT_SIZE){
            probe_point7 = mCursor - HALF_FFT_SIZE;
        }

        if(probe_point1 >= probe_point2){
            probe_point2 = probe_point1;
            probe_point1 = -1;
        }
        else{
            if(probe_point2 - probe_point1 < min_interval_between_probes){
                probe_point1 = -1;
            }
        }

        if(probe_point7 <= probe_point6){
            probe_point6 = probe_point7;
            probe_point7 = -1;
        }
        else{
            if(probe_point7 - probe_point6 < min_interval_between_probes){
                probe_point7 = -1;
            }
        }

        int result_index = 0;
        final List<Runnable> fft_runnable_chunks = new LinkedList<>();

        final float cycle_time = (float) mCursor / (float) mSampleFrequency;

        final float[] expected_gear_frequencies = new float[mGearTeeth.length];
        for(int i=0; i<mGearTeeth.length; i++){
            final float freq = (float)mGearTeeth[i] / cycle_time;
            expected_gear_frequencies[i] = freq;
        }

        if(probe_point1 >= 0){
            final ChunkFFTAndAnalysisRunnable chunk1 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point1,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk1);
            result_index++;
        }
        {
            final ChunkFFTAndAnalysisRunnable chunk2 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point2,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk2);
            result_index++;
        }
        {
            final ChunkFFTAndAnalysisRunnable chunk3 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point3,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk3);
            result_index++;
        }
        {
            final ChunkFFTAndAnalysisRunnable chunk4 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point4,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk4);
            result_index++;
        }
        {
            final ChunkFFTAndAnalysisRunnable chunk5 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point5,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk5);
            result_index++;
        }
        {
            final ChunkFFTAndAnalysisRunnable chunk6 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point6,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk6);
            result_index++;
        }
        if(probe_point7 >= 0){
            final ChunkFFTAndAnalysisRunnable chunk7 =
                    new ChunkFFTAndAnalysisRunnable(
                            FFT_SIZE,
                            mBuffer,
                            mCursor,
                            probe_point7,
                            result_index,
                            mFFTPrecomputedDAta,
                            mResultListener,
                            expected_gear_frequencies
                    );
            fft_runnable_chunks.add(chunk7);
            result_index++;
        }

        mResultListener.setNumberOfExpectedResults(result_index);
        runFFTChunks(fft_runnable_chunks);
        mResultListener.waitForResults();

        final CycleAnalysisResult results = new CycleAnalysisResult(
                cycle_time,
                result_index,
                System.currentTimeMillis()
        );
        for(int i=0; i<result_index; i++){
            final FFTAndAnalysisResultListener.Result result = mResultListener.readResult(i);
            results.setMeasure(
                    result.index,
                    new CycleAnalysisResult.Measure(
                            result.mainFrequency,
                            result.cyclePosition,
                            result.probableGear
                    )
            );
        }

        return results;
    }

    void runFFTChunks(final List<Runnable> fft_runnable_chunks){
        for(final Runnable r : fft_runnable_chunks){
            mExecutor.submit(r);
        }
    }
}
