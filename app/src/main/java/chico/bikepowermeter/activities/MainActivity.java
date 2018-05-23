package chico.bikepowermeter.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import chico.bikepowermeter.PowerMeterInterface;
import chico.bikepowermeter.R;
import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;
import chico.bikepowermeter.services.PowerMeterService;
import chico.bikepowermeter.services.service_helpers.GlobalParameters;


public class MainActivity extends Activity {

    private PowerMeterInterface mInterface;
    private TextView mCadenceView;
    private TextView mPulseDeviationTextView;
    private DrawingView mDrawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCadenceView = (TextView) findViewById(R.id.cadence);
        mPulseDeviationTextView = (TextView) findViewById(R.id.pulse_deviation);
        ((FrameLayout)findViewById(R.id.drawing_frame)).addView(mDrawingView = new DrawingView(this));

        ((Button)findViewById(R.id.up_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalParameters.getInstance().
                        setPulseDetectionRelativePulseDeviation(
                                GlobalParameters.getInstance().
                                        getPulseDetectionRelativePulseDeviation() + 1
                        );
                updatePulseDeviationText();
            }
        });

        ((Button)findViewById(R.id.down_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalParameters.getInstance().
                        setPulseDetectionRelativePulseDeviation(
                                GlobalParameters.getInstance().
                                        getPulseDetectionRelativePulseDeviation() - 1
                        );
                updatePulseDeviationText();
            }
        });

        updatePulseDeviationText();

        bindService(
                PowerMeterService.makeIntent(this),
                mServiceConnection,
                BIND_AUTO_CREATE
        );

        servicePolling.run();
    }

    private void updatePulseDeviationText(){
        mPulseDeviationTextView.setText(
                String.valueOf(
                        GlobalParameters.getInstance().
                                getPulseDetectionRelativePulseDeviation()
                )
        );
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(mServiceConnection);
        new Handler().removeCallbacks(servicePolling);
    }

    private Runnable servicePolling =
            new Runnable() {
                @Override
                public void run() {
                    if(mInterface != null) {
                        try {
                            final CycleAnalysisResult raw_data = mInterface.getRawCycleData();
                            float m = 0.0f;
                            if(raw_data != null) {
                                for (int i = 0; i < raw_data.getNumberOfMeasures(); i++) {
                                    m = Math.max(m, raw_data.getMeasure(i).frequency);
                                }
                            }
                            mCadenceView.setText(String.valueOf(mInterface.getCadence()) + " --- " + String.valueOf(m));
                            mDrawingView.setData(raw_data);
                            mDrawingView.invalidate();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    new Handler().postDelayed(this, 50);
                }
            };

    private final ServiceConnection mServiceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                     mInterface = PowerMeterInterface.Stub.asInterface(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mInterface = null;
                }
            };
}
