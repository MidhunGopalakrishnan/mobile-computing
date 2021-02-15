package com.example.covid19selfevaluationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.covid19selfevaluationapp.service.AccelerometerService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RespiratoryTrackerActivity extends AppCompatActivity  {
    private static final String TAG = "AccelerometerService";
    private AccelerometerResultReceiver results;
    private TextView rateValue;
    private ArrayList<Entry> zValues = new ArrayList<Entry>();
    private int i =0;
    private static final int samplingLimit = 450;
    private HashMap<String,Float> symptomsMap = new HashMap<String,Float>();

    private LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory_tracker);

        symptomsMap = (HashMap)(getIntent().getSerializableExtra("SymptomsMap"));

    }

    @Override
    public void onBackPressed() {
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
        startService(serviceIntent);
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
        symptomsMap.put("Respiratory Rate",18f) ; //TODO
        super.onReceiveResult(resultCode, resultData);
        rateValue = (TextView) findViewById(R.id.textView5);

        //check status and fetch results here
        if(resultCode == RESULT_OK && resultData!=null){
            Log.d(TAG, "onReceiveResult: " + resultData.getString("zValue"));
            rateValue.setText(resultData.getString("zValue"));

            //test chart
            lineChart = (LineChart) findViewById(R.id.linechart);

            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.getDescription().setEnabled(false);

            Float newValue = Float.valueOf(resultData.getString("zValue"));
            if(zValues.size()!=0) {
                Float oldValue = (Float) zValues.get(zValues.size() - 1).getData();
            }
            zValues.add(new Entry(i,newValue));

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
            if (lineDataSet1.getEntryCount() >= samplingLimit-1) {
                lineDataSet1.removeFirst();
                for (int j=0; j<lineDataSet1.getEntryCount(); j++) {
                    Entry entryToChange = lineDataSet1.getEntryForIndex(j);
                    entryToChange.setX(entryToChange.getX() +1);
                }
                i=0;

            } else {
                i = i+1;
            }


            //refresh graph
           // lineChart.moveViewToX(lineDataSet1.getEntryCount());
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();


        }
        else {
            // no data from sensor. Set the result as no data from Sensor
            rateValue.setText("No Data");
        }
    }
}

}
