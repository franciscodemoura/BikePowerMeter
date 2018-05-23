package chico.bikepowermeter.dataprocessing.signalanalysis;

import chico.bikepowermeter.services.service_helpers.GlobalParameters;

/**
 * Created by chico on 16/08/2015. Uhu!
 */
public class ChunkFFTAndAnalysisRunnable implements Runnable{

    private final int N;
    private final float [] x;
    private final int x_length;
    private final int centerPoint;
    private final int result_index;
    private final FFT.PrecomputedData FFTPrecomputedData;
    private final FFTAndAnalysisResultListener resultListener;
    private final float [] chunk;
    private final float [] gearFrequencies;
    private final int mSampleFrequency;

    ChunkFFTAndAnalysisRunnable(
            final int fft_size,
            final float[] x,
            final int x_length,
            final int center_point,
            final int result_index,
            final FFT.PrecomputedData FFT_precomputed_data,
            FFTAndAnalysisResultListener result_listener,
            final float [] gear_frequencies
    )
    {
        N = fft_size;
        this.x = x;
        this.x_length = x_length;
        centerPoint = center_point;
        this.result_index = result_index;
        FFTPrecomputedData = FFT_precomputed_data;
        resultListener = result_listener;
        chunk = new float [N];
        gearFrequencies = gear_frequencies;
        mSampleFrequency = GlobalParameters.getInstance().getSampleFrequency();
    }

    @Override
    public void run() {
        final int start_point = centerPoint - N/2;
        final float p = (float)(centerPoint) / (float)(x_length-1);

        if(start_point < 0 || start_point + N > x_length){
            resultListener.putResult(
                    result_index,
                    p,
                    0.0f,
                    -1
            );
        }
        else {

            fillChunk(start_point);
            final FFT fft = new FFT(chunk, FFTPrecomputedData, mSampleFrequency, true);
            fft.doFFT();

            final float dominant_frequency =
                    fft.frequencyFromIndex(
                        getDominantFrequencyIndex(fft)
                    );

            final int probable_gear = getMostProbableGear(fft);

            resultListener.putResult(
                    result_index,
                    p,
                    dominant_frequency,
                    probable_gear
            );

        }
    }

    private void fillChunk(final int start_point){
        for(int i=0; i<N; i++){
            chunk[i] = x[start_point + i] * FFTPrecomputedData.windowTable[i];
        }
    }

    private float getDominantFrequencyIndex(final FFT fft){

        final int size = fft.getSize();
        final double [] real = fft.getRealVector();
        final double [] img = fft.getImaginaryVector();

        double max_mag = 0.0;
        int max_index = 0;
        for(int i=1; i<size/2; i++){
            final double mag = real[i]*real[i] + img[i]*img[i];
            if(mag > max_mag){
                max_mag = mag;
                max_index = i;
            }
        }

        return fft.fineTuneMaxFrequencyIndex(max_index);
    }

    private int getMostProbableGear(final FFT fft){

        final int size = fft.getSize();
        final double [] real = fft.getRealVector();
        final double [] img = fft.getImaginaryVector();

        int best_gear = -1;
        double best_mag = 0.0;

        for(int i=0; i<gearFrequencies.length; i++){
            final float index = fft.indexFromFrequency(gearFrequencies[i]);

            if(index > 2.0f && index < (float)(size-2)){

                final int i1 = (int)index;
                final int i2 = i1 + 1;
                final int i0 = i1 - 1;
                final int i3 = i2 + 1;

                final double mag0 = Math.sqrt(real[i0] * real[i0] + img[i0] * img[i0]);
                final double mag1 = Math.sqrt(real[i1] * real[i1] + img[i1] * img[i1]);
                final double mag2 = Math.sqrt(real[i2] * real[i2] + img[i2] * img[i2]);
                final double mag3 = Math.sqrt(real[i3] * real[i3] + img[i3] * img[i3]);

                final double range_coeff = GlobalParameters.GEAR_FREQUENCY_PROBE_RANGE_COEFFICIENT;
                double c0 = Math.exp( -(index - i0) * (index - i0) / range_coeff);
                double c1 = Math.exp( -(index - i1) * (index - i1) / range_coeff);
                double c2 = Math.exp( -(index - i2) * (index  -i2) / range_coeff);
                double c3 = Math.exp( -(index - i3) * (index - i3) / range_coeff);

                final double c_sum = c0 + c1 + c2 + c3;

                c0 /= c_sum;
                c1 /= c_sum;
                c2 /= c_sum;
                c3 /= c_sum;

                final double mag = (mag0*c0 + mag1*c1 + mag2*c2 + mag3*c3) / gearFrequencies[i];

                if(mag > best_mag){
                    best_mag = mag;
                    best_gear = i;
                }
            }
            else {
                return -1;
            }
        }

        return best_gear;
    }
}
