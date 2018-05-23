package chico.bikepowermeter.dataprocessing;

/**
 * Created by chico on 22/06/2015. Uhu!
 */
public interface AudioDataConsumer {
    void setParameters(int sample_rate);
    void putData(short [] buffer, int samples);
    void finish();
}
