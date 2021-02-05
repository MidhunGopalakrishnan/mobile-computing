package com.example.covid19selfevaluationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.covid19selfevaluationapp.service.AccelerometerService;

public class RespiratoryTrackerActivity extends AppCompatActivity {
    private static final String TAG = "AccelerometerService";
    private AccelerometerResultReceiver results;
    private TextView rateValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory_tracker);

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
        super.onReceiveResult(resultCode, resultData);
        rateValue = (TextView) findViewById(R.id.textView3);

        //check status and fetch results here
        if(resultCode == RESULT_OK && resultData!=null){
            Log.d(TAG, "onReceiveResult: " + resultData.getString("zValue"));
            rateValue.setText(resultData.getString("zValue"));
        }
        else {
            // no data from sensor. Set the result as no data from Sensor
            rateValue.setText("No Data");
        }
    }
}

}
