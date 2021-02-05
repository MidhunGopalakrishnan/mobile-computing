package com.example.covid19selfevaluationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // on button click navigate to respiratory measurement page
        Button measureRespiratoryRateButton = (Button) findViewById(R.id.button);
        measureRespiratoryRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RespiratoryTrackerActivity.class));
            }
        });
    }

    public void captureSymptoms(View view) {
        startActivity(new Intent(MainActivity.this, SymptomLoggingActivity.class));
    }
}