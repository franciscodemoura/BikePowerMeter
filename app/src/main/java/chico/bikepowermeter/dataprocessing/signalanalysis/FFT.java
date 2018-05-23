package chico.bikepowermeter.dataprocessing.signalanalysis;

import chico.bikepowermeter.services.service_helpers.GlobalParameters;

/**
 * Created by chico on 08/08/2015. Uhu!
 */

public class FFT {

    public static class PrecomputedData {
        public final double [] cosTable;
        public final float [] windowTable;
        public final int [] bitMirror;
        public final int levels;

        public PrecomputedData(final int N){
            cosTable = new double[N];
            final double w1 = 2.0*Math.PI/(double)N;
            for(int i=0; i<N; i++){
                cosTable[i] = Math.cos(w1*(double)i);
            }

            windowTable = new float[N];
            final float w2 = 2.0f*(float)Math.PI/(float)(N-1);
            for(int i=0; i<N; i++){
                windowTable[i] = 0.5f * (1.0f - (float)Math.cos(w2*(float)i));
            }

            int N_aux = N;
            int levels_aux = 0;
            while(N_aux > 1){
                N_aux = N_aux >> 1;
                levels_aux++;
            }

            levels = levels_aux;

            bitMirror = new int[N];
            for(int i=0; i<N; i++){
                int r = 0, t = i;
                for(int j=0; j<levels_aux; j++){
                    r = (r << 1) | (t & 0x1);
                    t = t >> 1;
                }
                bitMirror[i] = r;
            }
        }
    }


    private final int N;
    private final float [] x;
    private final double [] Xr;
    private final double [] Xi;
    private final PrecomputedData precomputedData;
    private float sampleFrequency;

    public FFT(
            final float [] x,
            final PrecomputedData precomputedData,
            final float sample_frequency,
            final boolean windowing
    ){
        this.x = x;
        this.N = x.length;
        this.Xr = new double[N];
        this.Xi = new double[N];
        this.precomputedData = precomputedData;
        this.sampleFrequency = sample_frequency;
        if(windowing){
            for(int i=0; i<N; i++){
                x[i] *= precomputedData.windowTable[i];
            }
        }
    }

    public void doFFT(){

        for(int i=0; i<N; i++){
            Xr[i] = x[precomputedData.bitMirror[i]];
            Xi[i] = 0.0;
        }

        final int d = N/4;
        int mask1 = 0;
        int mask2 = 0x7FFF;

        for(int j = precomputedData.levels - 1; j >= 0; j--) {

            for (int m = 0; m < N / 2; m++) {

                final int k = m & mask1;
                final int t = k << j;
                final double r = precomputedData.cosTable[t];
                final double i = precomputedData.cosTable[(d + t) & (precomputedData.cosTable.length - 1)];

                final int s = (m & mask2) << 1;
                final int q = s + (1 << (precomputedData.levels - j - 1));

                final double R = Xr[q + k];
                final double I = Xi[q + k];
                final double nr = r * R - i * I;
                final double ni = r * I + i * R;
                Xr[q + k] = Xr[s + k] - nr;
                Xi[q + k] = Xi[s + k] - ni;
                Xr[s + k] += nr;
                Xi[s + k] += ni;
            }

            mask1 = (mask1 << 1) | 1;
            mask2 = mask2 << 1;
        }
    }

    public int getSize(){
        return N;
    }

    public double [] getRealVector(){
        return Xr;
    }

    public double [] getImaginaryVector(){
        return Xi;
    }

    public float frequencyFromIndex(final float index){
        return index * sampleFrequency / (float)N;
    }

    public float indexFromFrequency(final float frequency){
        return frequency * (float)N / sampleFrequency;
    }

    public float fineTuneMaxFrequencyIndex(final int index){
        final float step = GlobalParameters.FINE_TUNE_MAX_FREQUENCY_STEP;

        double mag1 = Xr[index]*Xr[index] + Xi[index]*Xi[index];
        double mag2 = mag1;
        float index1 = index;
        float index2 = index;

        for(float i = (float)(index) + step; i < (float)(index+1) - step/2.0f; i += step){
            final double mag = evaluateFrequencySquaredMagnitudeByIndex(i);
            if(mag <= mag1){
                break;
            }
            else{
                mag1 = mag;
                index1 = i;
            }
        }

        for(float i = (float)(index) - step; i > (float)(index-1) + step/2.0f; i -= step){
            final double mag = evaluateFrequencySquaredMagnitudeByIndex(i);
            if(mag <= mag2){
                break;
            }
            else{
                mag2 = mag;
                index2 = i;
            }
        }

        return mag1 > mag2 ? index1 : index2;
    }

    private double evaluateFrequencySquaredMagnitudeByIndex(float index){
        final double w = -2.0*Math.PI / (double)N * (double)index;

        double ar = 0.0;
        double ai = 0.0;

        for(int n=0; n<N; n++){
            final double t = (double)n * w;
            final double r = Math.cos(t) * x[n];
            final double i = Math.sin(t) * x[n];
            ar += r;
            ai += i;
        }

        return ar*ar + ai*ai;
    }
}
