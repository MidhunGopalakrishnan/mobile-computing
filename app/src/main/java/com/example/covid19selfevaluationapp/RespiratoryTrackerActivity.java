package com.example.covid19selfevaluationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.covid19selfevaluationapp.service.AccelerometerService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class RespiratoryTrackerActivity extends AppCompatActivity  {
    private static final String TAG = "AccelerometerService";
    private AccelerometerResultReceiver results;
    private TextView rateValue;
    private ArrayList<Entry> zValues = new ArrayList<Entry>();
    private ArrayList<Float> deltaValuesList = new ArrayList<Float>();
    private static final int samplingLimit = 450;
    private HashMap<String,Float> symptomsMap = new HashMap<String,Float>();
    Button startCapture;

    public int getRespTestValue() {
        return respTestValue;
    }

    public void setRespTestValue(int respTestValue) {
        this.respTestValue = respTestValue;
    }

    int respTestValue = 0;

    private LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory_tracker);

        symptomsMap = (HashMap)(getIntent().getSerializableExtra("SymptomsMap"));

    }

    @Override
    public void onBackPressed() {
        //stop accelerometer service if not stopped yet
        Intent serviceIntent = new Intent(RespiratoryTrackerActivity.this, AccelerometerService.class);
        stopService(serviceIntent);

        Intent intent = new Intent();
        intent.putExtra("SymptomsMap",symptomsMap);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }


    public void onStartCapture(View view) {
        // call accelerometer service
        Log.d(TAG, "onStartCapture: Starting Service");
        Intent serviceIntent = new Intent(RespiratoryTrackerActivity.this, AccelerometerService.class);
        results = new AccelerometerResultReceiver(new Handler());
        serviceIntent.putExtra(Intent.EXTRA_RESULT_RECEIVER,results);
        startCapture = findViewById(R.id.button5);
        startCapture.setEnabled(false);
        //clear graph and start
        lineChart = (LineChart) findViewById(R.id.linechart);
        lineChart.clear();
        setRespTestValue(0);
        startService(serviceIntent);
    }

    public void stopCaptureFromSensor() {
        onStopCapture(findViewById(R.id.button5).getRootView());
        startCapture.setEnabled(true);
    }

    public void onStopCapture(View view) {
        // stop accelerometer service
        Log.d(TAG, "**********************************************************onStopCapture: Stopping Service ***********************************************************");
        Intent serviceIntent = new Intent(RespiratoryTrackerActivity.this, AccelerometerService.class);
        boolean whetherStopped = stopService(serviceIntent);
        Log.d(TAG, "*************************************************************onStopCapture: Whether Stopped or not = "+ whetherStopped + " ***********************************");
    }
// extend Result Receiver and fetch results
    public class AccelerometerResultReceiver extends ResultReceiver {

        private int i =0;
        private int respiratoryRate =0;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public AccelerometerResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        //symptomsMap.put("Respiratory Rate",18f) ; //TODO
        super.onReceiveResult(resultCode, resultData);
        rateValue = (TextView) findViewById(R.id.textView5);

        //check status and fetch results here
        if(resultCode == RESULT_OK && resultData!=null){

            lineChart = (LineChart) findViewById(R.id.linechart);

           // Log.d(TAG, "onReceiveResult: " + resultData.getString("zValue"));
            //if you restart capture reset i to 0
            if(i>449) {
                i=0;
                lineChart.clear();
                deltaValuesList.clear();
                zValues.clear();
            }
            if(i%10 ==0) {
                rateValue.setText(" " + i + "/450");
            }

            //test chart


            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.getDescription().setEnabled(false);

            Float newValue = Float.valueOf(resultData.getString("zValue"));
            Float oldValue =0f;
            if(zValues.size()!=0) {
                oldValue = (Float) (zValues.get(zValues.size() - 1).getY());
            }
            Float deltaChangeValue = newValue - oldValue;
            if(deltaChangeValue>= 0.3 || deltaChangeValue <= -0.3) {
                respiratoryRate++;
                setRespTestValue(getRespTestValue()+1);
            }
            if(i!=0) {
                deltaValuesList.add(deltaChangeValue);
            }
            else {
                deltaValuesList.add(0f);
            }
            if(deltaValuesList.size()== 449) {
//                rateValue.setText(String.valueOf(Math.floor((respiratoryRate*60)/45))+ " BPM");
//                symptomsMap.put("Respiratory Rate", (float) Math.floor((respiratoryRate*60)/45));
                rateValue.setText(String.valueOf(Math.floor((getRespTestValue()*60)/45))+ " BPM");
                symptomsMap.put("Respiratory Rate", (float) Math.floor((getRespTestValue()*60)/45));
                stopCaptureFromSensor();
            }
            else {
                zValues.add(new Entry(i, newValue));

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                LineDataSet lineDataSet1 = new LineDataSet(zValues, " Data from Sensor ");
                lineDataSet1.setDrawCircles(false);
                lineDataSet1.setColor(Color.BLUE);

                dataSets.add(lineDataSet1);

                lineChart.setData(new LineData(dataSets));

                // if i reaches 450, reset i to 0
//            if(i == 449) {
//                i=0;
//                for (int j=0;j < 449;j++){
//                    lineChart.getLineData().getDataSetByIndex(0).removeEntry(j);
//                }
//                dataSets.clear();
//                lineChart.notifyDataSetChanged();
//                lineChart.invalidate();
//                lineChart.clearValues();
//                lineChart.clear();
//
//            }else {
//                i = i+1;
//            }
                if (lineDataSet1.getEntryCount() >= samplingLimit - 1) {
                    lineDataSet1.removeFirst();
                    for (int j = 0; j < lineDataSet1.getEntryCount(); j++) {
                        Entry entryToChange = lineDataSet1.getEntryForIndex(j);
                        entryToChange.setX(entryToChange.getX() + 1);
                    }
                }

                //refresh graph
                // lineChart.moveViewToX(lineDataSet1.getEntryCount());
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();

                i++;
            }
        }
        else {
            // no data from sensor. Set the result as no data from Sensor
            rateValue.setText("No Data");
        }
    }
}

}
