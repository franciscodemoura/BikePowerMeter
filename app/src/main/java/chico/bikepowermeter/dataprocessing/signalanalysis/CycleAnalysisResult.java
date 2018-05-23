package chico.bikepowermeter.dataprocessing.signalanalysis;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chico on 30/08/2015. Uhu!
 */
public class CycleAnalysisResult implements Parcelable {

    public static class Measure {
        public final float frequency;
        public final float relativeTime;
        public final int probableGear;

        public Measure(
                final float freq,
                final float time,
                final int probable_gear
        ){
            frequency = freq;
            relativeTime = time;
            probableGear = probable_gear;
        }
    }

    private final float cycleTime;
    private final Measure [] measures;
    private final long globalTime;

    public CycleAnalysisResult(
            final float cycle_time,
            final int results,
            final long time
    ){
        cycleTime = cycle_time;
        measures = new Measure[results];
        globalTime = time;
    }

    private CycleAnalysisResult(final Parcel in){
        cycleTime = in.readFloat();
        measures = new Measure[in.readInt()];
        globalTime = in.readLong();
        for(int i=0; i<measures.length; i++){
            measures[i] =
                    new Measure(
                        in.readFloat(),
                        in.readFloat(),
                        in.readInt()
                    );
        }
    }

    public Measure getMeasure(final int index){
        return measures[index];
    }

    public void setMeasure(final int index, final Measure measure){
        measures[index] = measure;
    }

    public int getNumberOfMeasures(){
        return measures.length;
    }

    public float getCycleTime(){
        return cycleTime;
    }

    public float getGlobalTime(){
        return globalTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(cycleTime);
        dest.writeInt(measures.length);
        dest.writeLong(globalTime);
        for(Measure m : measures){
            dest.writeFloat(m.frequency);
            dest.writeFloat(m.relativeTime);
            dest.writeInt(m.probableGear);
        }
    }

    public static final Parcelable.Creator<CycleAnalysisResult> CREATOR =
            new Parcelable.Creator<CycleAnalysisResult>() {

                public CycleAnalysisResult createFromParcel(Parcel in) {
                    return new CycleAnalysisResult(in);
                }

                public CycleAnalysisResult[] newArray(int size) {
                    return new CycleAnalysisResult[size];
                }
            };
}
