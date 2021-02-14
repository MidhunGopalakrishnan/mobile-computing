package com.example.covid19selfevaluationapp;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;

public class SymptomLoggingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "Symptom Activity";
    private HashMap<String,Float> symptomsMap = new HashMap<String,Float>();
    String selectedSymptom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //populate HashMap to use later
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_logging);

        symptomsMap = (HashMap)(getIntent().getSerializableExtra("SymptomsMap"));
        RatingBar ratings = (RatingBar)findViewById(R.id.ratingBar1);
        if(symptomsMap.get("Nausea")!=null){
            ratings.setRating(symptomsMap.get("Nausea"));}
        else {
            ratings.setRating(0);
        }

        Spinner symptoms = (Spinner) findViewById(R.id.symptoms_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.symptoms_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptoms.setAdapter(adapter);
        symptoms.setOnItemSelectedListener(this);

        Button submitButton = (Button) findViewById(R.id.button7);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //capture rating

                Log.d(TAG, "onClick : New rating is "+ ratings.getRating());
                // insert/update HashMap
                symptomsMap.put(selectedSymptom, ratings.getRating());
                Log.d(TAG, "onClick : Debug Break point");
                //show user that record saved
                Toast.makeText(getApplicationContext(),"Rating saved for "+ selectedSymptom + "!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("SymptomsMap",symptomsMap);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedSymptom = (String) parent.getItemAtPosition(position);
        Log.d(TAG, "onItemSelected: Selected Symptom = " + selectedSymptom);
        RatingBar ratings = (RatingBar) findViewById(R.id.ratingBar1);
        //reset rating to new symptom. If not in HashMap,set to zero
        if ((symptomsMap.get(selectedSymptom)) != null) {
            ratings.setRating(symptomsMap.get(selectedSymptom));
        } else {
            ratings.setRating(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}