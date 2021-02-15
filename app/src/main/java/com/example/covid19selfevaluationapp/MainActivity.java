package com.example.covid19selfevaluationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.getDefaultSize;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2000;
    private HashMap<String,Float> symptomsMap = new HashMap<String,Float>();
    public static final int VIDEO_REQUEST_CODE = 99;
    public static final int ACCELEROMETER_REQUEST_CODE = 88;
    public static final int SYMPTOM_REQUEST_CODE = 77;
    Uri videoUri;
    String filePath;
    VideoView videoView;
    com.example.covid19selfevaluationapp.DatabaseHelper databaseHelper = new DatabaseHelper(this);
    HeartRateMeasure measureHeartRate;
    private ArrayList<Float> redValuesList = new ArrayList<Float>();
    TextView heartRate ;

    public int getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    int heartRateFinal = 0;
    int currentFrame =0;
    int totalFrames =0;

    public int getHeartRateFinal() {
        return heartRateFinal;
    }

    public void setHeartRateFinal(int heartRateFinal) {
        this.heartRateFinal = heartRateFinal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // on button click navigate to respiratory measurement page
        Button measureRespiratoryRateButton = (Button) findViewById(R.id.button);
        measureRespiratoryRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RespiratoryTrackerActivity.class);
                intent.putExtra("SymptomsMap",symptomsMap);
                startActivityForResult(intent,ACCELEROMETER_REQUEST_CODE);
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }
        });

        Button symptomButton = (Button) findViewById(R.id.button3);
        symptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SymptomLoggingActivity.class);
                intent.putExtra("SymptomsMap",symptomsMap);
                startActivityForResult(intent,SYMPTOM_REQUEST_CODE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode ==ACCELEROMETER_REQUEST_CODE){
            if(resultCode == RESULT_OK) {
                symptomsMap = (HashMap)data.getSerializableExtra("SymptomsMap");
            }
        }
        else if(requestCode ==SYMPTOM_REQUEST_CODE){
            if(resultCode == RESULT_OK) {
                symptomsMap = (HashMap)data.getSerializableExtra("SymptomsMap");
            }
        }
        else if (requestCode == VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult : Video recorded");
                //switch off flash light
                try {
                    switchOffFlash();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
//                File videoFile = new File(getExternalFilesDir(Environment.getStorageDirectory().getAbsolutePath()),"videocapture.mp4");
//                videoUri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".provider",videoFile);
                videoView = findViewById(R.id.videoView);
                videoView.setVideoURI(videoUri);
                MediaController mediaController = new MediaController(this);
               // mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.start();
                Log.d(TAG, "onActivityResult : Video Path "+filePath);

                // logic to calculate heart rate
                calculateHeartRate();

            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "read onActivityResult : Video Cancelled");
            } else {
                Log.d(TAG, "onActivityResult : Failed to record video");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void calculateHeartRate() {
        measureHeartRate = new HeartRateMeasure();
        measureHeartRate.execute(videoUri);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void startVideoCapture(View view) throws CameraAccessException {
        if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
            invokeCamera();
        } else {
            String[] permissionRequest = {Manifest.permission.CAMERA};
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                try {
                    invokeCamera();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this,"Unable to invoke camera without permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void invokeCamera() throws CameraAccessException {
        Log.d(TAG, "invokeCamera :  Video File Path " + getExternalFilesDir(Environment.getStorageDirectory().getAbsolutePath()));
        File videoFile = new File(getExternalFilesDir(Environment.getStorageDirectory().getAbsolutePath()),"videocapture.mp4");
        videoUri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".provider",videoFile);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,videoUri);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if(intent.resolveActivity(getPackageManager())!= null) {
            //switch on flash
            switchOnFlash();
            startActivityForResult(intent, VIDEO_REQUEST_CODE);
        }
    }

    private void switchOnFlash() throws CameraAccessException {
        CameraManager cm = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            cm.setTorchMode(cm.getCameraIdList()[0],true);
        }
    }

    private void switchOffFlash() throws CameraAccessException {
        CameraManager cm = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            cm.setTorchMode(cm.getCameraIdList()[0],false);
        }
    }

    public void uploadToDB(View view) {
        boolean insertionSuccess = databaseHelper.createRecord(symptomsMap);
        if(insertionSuccess) {
            Toast.makeText(getApplicationContext(), "Data uploaded to database successfully", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Data upload failed", Toast.LENGTH_LONG).show();
        }
    }

    private class HeartRateMeasure extends AsyncTask<Uri,Float,Float>{

        @Override
        protected void onPostExecute(Float aFloat) {
            super.onPostExecute(aFloat);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            updateMainActivity(values[0],values[1]);
            super.onProgressUpdate(values);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected Float doInBackground(Uri... uris) {
            try {
                //get video file
                File videoPath = getExternalFilesDir(Environment.getStorageDirectory().getAbsolutePath());
                File videoFile = new File(videoPath,"videocapture.mp4");
                Uri fileUri = Uri.parse(videoFile.toString());
                MediaPlayer m = MediaPlayer.create(getBaseContext(),fileUri);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoFile.getAbsolutePath());

                int totalVideoTime = m.getDuration(); // in milli seconds TODO : change it from 5 seconds to 45 seconds after testing
                int samplingRate = 10; // take 10 samples per second. ie. max of 450 samples in 45 seconds duration
                int numberOfFrames = (int)Math.floor((totalVideoTime/1000)*samplingRate);
                int sampleFrameTime = 100000;
                int heartRateInner =1;
                heartRate = (TextView) findViewById(R.id.textView6);
                setTotalFrames(numberOfFrames);
                // get frame at this time
                for(int i = 1 ; i<numberOfFrames;i++) {
                    float redIntensity =0f;
                    Bitmap bitmapImage = retriever.getFrameAtTime(sampleFrameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    sampleFrameTime = sampleFrameTime+100000; // increment by 100ms for next frame
                    Log.d(TAG, "doInBackground : Got frame");

                    //get center frame like 40x40 pixels and compute red
                    for(int x = 500; x<=600; x++){
                        for(int y = 960; y<=1060;y++){
                            redIntensity+= Color.red(bitmapImage.getPixel(x,y)); //add to the frames red intensity
                        }
                    }
                    // move value of red to ArrayList
                    float lastValue ;
                    if(redValuesList.size()!=0){
                    lastValue = redValuesList.get(redValuesList.size()-1);

                    if((Math.abs(lastValue-redIntensity))>10){
                        heartRateInner++;
                    }
                    }
                    redValuesList.add(redIntensity);
                    Log.d(TAG, "doInBackground: Red intensity values "+ redIntensity);
                    //Log.d(TAG, redValuesList.toString());
                   // onProgressUpdate((float) i, (float) numberOfFrames);
                    setCurrentFrame(i);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            heartRate.setText("Processing "+getCurrentFrame()+"/"+getTotalFrames()+ " frames");
                           // heartRate.setTextColor(Color.BLACK);
                        }
                    });
                }
                // check arrayList and do logic to compute heart rate and add to map

                int value = (int)Math.floor(((heartRateInner * 60000) / totalVideoTime));
                symptomsMap.put("Heart Rate", Float.valueOf((float) value));
               // Log.d(TAG, "doInBackground: Heart Rate is "+ value + " BPM");



                setHeartRateFinal(value);
                retriever.release();

                runOnUiThread(new Runnable() {
                    public void run() {
                        heartRate.setText(getHeartRateFinal()+ " BPM");
                        //heartRate.setTextColor(Color.BLACK);
                    }
                });


            } catch (Exception e) {
                Log.d(TAG, "doInBackground : Error from Heart Measure function");
                e.printStackTrace();
            }


            return null;
        }

    }

    private void updateMainActivity(Float value1, Float value2) {
        heartRate = (TextView) findViewById(R.id.textView6);
   //     heartRate.setText("Processing frame "+value1 +" out of "+ value2+ " frames");
    }


}