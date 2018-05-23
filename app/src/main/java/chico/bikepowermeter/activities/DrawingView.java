package chico.bikepowermeter.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ImageView;

import chico.bikepowermeter.dataprocessing.signalanalysis.CycleAnalysisResult;

/**
 * Created by chico on 10/10/2015. Uhu!
 */
public class DrawingView extends ImageView {

    private CycleAnalysisResult mData = null;

    public DrawingView(final Context context){
        super(context);
    }

    public void setData(CycleAnalysisResult mData) {
        this.mData = mData;
    }

    protected void onDraw(final Canvas canvas){
        super.onDraw(canvas);

        if(mData == null){
            return;
        }

        final Paint paint = new Paint();

        final int w = getWidth();
        final int h = getHeight();

        paint.setStrokeWidth(8.0f);

        float x1 = 0.0f, y1 = 0.0f;
        for(int i=0; i<mData.getNumberOfMeasures(); i++){
            final CycleAnalysisResult.Measure measure = mData.getMeasure(i);
            final float x2 = measure.relativeTime * w;
            final float y2 = h - measure.frequency / 250.0f * h;
            if(i > 0) {
                canvas.drawLine(x1,y1,x2,y2,paint);
            }
            x1 = x2; y1 = y2;
        }

        paint.setStrokeWidth(2.0f);

        for(int i=0; i<mData.getNumberOfMeasures(); i++){
            final CycleAnalysisResult.Measure measure = mData.getMeasure(i);
            final float x2 = measure.relativeTime * w;
            final float y2 = h - (measure.probableGear * 0.8f*h + 0.1f*h);
            if(i > 0) {
                canvas.drawLine(x1,y1,x2,y2,paint);
            }
            x1 = x2; y1 = y2;
        }
    }
}
