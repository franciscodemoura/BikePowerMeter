package chico.bikepowermeter.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import chico.bikepowermeter.dataprocessing.AudioDataConsumer;
import chico.bikepowermeter.dataprocessing.AudioReaderThread;
import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;
import chico.bikepowermeter.dataprocessing.cycledetectors.InductionPulseCycleDetector;
import chico.bikepowermeter.dataprocessing.cycledetectors.SilenceSignalCycleDetector;
import chico.bikepowermeter.dataprocessing.SignalProcessingMaster;
import chico.bikepowermeter.PowerMeterInterface;
import chico.bikepowermeter.services.service_helpers.GlobalParameters;
import chico.bikepowermeter.services.service_helpers.InformationBuilderAndProvider;

/**
 * Created by chico on 27/06/2015. Uhu!
 */
public class PowerMeterService extends Service{

    private final int mReaderBufferSize = GlobalParameters.READER_BUFFER_SIZE;
    private final int mConsumerBufferSize = GlobalParameters.CONSUMER_BUFFER_SIZE;
    private final float mMinCycleTime = GlobalParameters.MIN_CYCLE_TIME;

    private AudioReaderThread mAudioReader;
    private AudioDataConsumer mDataConsumer;
    private InformationBuilderAndProvider mInformationCenter;

    private final PowerMeterInterface.Stub mInterface =
            new PowerMeterInterface.Stub(){
                @Override
                public float getCadence() throws RemoteException {
                    return mInformationCenter.getCadence();
                }

                @Override
                public int [] getGear() throws RemoteException {
                    return mInformationCenter.getGears();
                }

                @Override
                public CycleAnalysisResult getRawCycleData() throws RemoteException {
                    return mInformationCenter.getRawData();
                }
            };

    @Override
    public IBinder onBind(final Intent intent) {
        return mInterface.asBinder();
    }

    @Override
    public boolean onUnbind(final Intent intent){
        return false;
    }

    @Override
    public void onCreate(){
        mAudioReader =
                new AudioReaderThread(
                        mReaderBufferSize
                );

        mInformationCenter =
                new InformationBuilderAndProvider();

        mDataConsumer =
                new SignalProcessingMaster(
                        mConsumerBufferSize,
                        mMinCycleTime,
                        mInformationCenter
                );

        ((SignalProcessingMaster) mDataConsumer).addCycleDetector(
                new SilenceSignalCycleDetector(
                        GlobalParameters.SILENCE_DETECTOR_VALUE_THRESHOLD,
                        GlobalParameters.SILENCE_DETECTOR_COUNT_THRESHOLD
                )
        );
        ((SignalProcessingMaster) mDataConsumer).addCycleDetector(
                new InductionPulseCycleDetector()
        );

        mAudioReader.addAudioDataConsumer(mDataConsumer);
        mAudioReader.start();
    }

    @Override
    public void onDestroy(){
        if(mAudioReader != null) {
            mAudioReader.interrupt();
            try {
                mAudioReader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mAudioReader = null;
        mDataConsumer = null;
    }

    public static Intent makeIntent(final Context context){
        return new Intent(context, PowerMeterService.class);
    }}
