package chico.bikepowermeter.dataprocessing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.ArrayList;
import java.util.List;

import chico.bikepowermeter.services.service_helpers.GlobalParameters;

/**
 * Created by chico on 21/06/2015. Uhu!
 */
public class AudioReaderThread extends Thread{

    private int mSampleFreq;
    private volatile boolean mRecordInitOk = true;
    private volatile AudioRecord mAudioRecorder = null;
    private final int mBufferSize;
    private final List<AudioDataConsumer> mAudioDataConsumers = new ArrayList<>();

    public AudioReaderThread(final int buffer_size){
        mBufferSize = buffer_size;
        final int sample_freqs [] = {GlobalParameters.PRIMARY_SAMPLE_FREQ, GlobalParameters.SECONDARY_SAMPLE_FREQ};

        mSampleFreq = 0;
        int min_buffer_size = 0;

        for(final int sf : sample_freqs) {
            min_buffer_size =
                    AudioRecord.getMinBufferSize(
                            sf,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                    );

            if (min_buffer_size != AudioRecord.ERROR_BAD_VALUE && min_buffer_size != AudioRecord.ERROR) {
                mSampleFreq = sf;
                break;
            }
        }

        if(mSampleFreq == 0){
            destroyRecordingSession();
        }
        else {
            GlobalParameters.getInstance().setSampleFrequency(mSampleFreq);
            mAudioRecorder =
                    new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            mSampleFreq,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            min_buffer_size * 10
                    );

            if (mAudioRecorder == null || mAudioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                destroyRecordingSession();
            }
        }
    }

    public void addAudioDataConsumer(final AudioDataConsumer audio_data_consumer){
        if(isRecordingOk()) {
            audio_data_consumer.setParameters(mSampleFreq);
            mAudioDataConsumers.add(audio_data_consumer);
        }
    }

    public boolean isRecordingOk(){
        return mRecordInitOk;
    }

    private void createRecordingSession(){
        mAudioRecorder.startRecording();
    }

    private void destroyRecordingSession(){
        mRecordInitOk = false;
        if(mAudioRecorder != null) {
            mAudioRecorder.stop();
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
    }

    @Override
    public void run(){

        if(isRecordingOk()) {

            createRecordingSession();

            final short[] buffer = new short[mBufferSize];
            while (!isInterrupted()) {
                final int actually_read = mAudioRecorder.read(buffer, 0, mBufferSize);
                for (AudioDataConsumer a : mAudioDataConsumers) {
                    a.putData(buffer, actually_read);
                }
            }

            destroyRecordingSession();

            for(AudioDataConsumer a : mAudioDataConsumers) {
                a.finish();
            }
            mAudioDataConsumers.clear();
        }
    }
}
