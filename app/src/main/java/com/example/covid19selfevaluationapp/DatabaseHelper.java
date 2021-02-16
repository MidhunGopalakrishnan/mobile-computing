package com.example.covid19selfevaluationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "PERSON_ACTIVITY";
    //private static final String COL1 = "PERSON_ID";
    private static final String COL2 = "HEART_RATE";
    private static final String COL3 = "RESPIRATORY_RATE";
    private static final String COL4 = "NAUSEA";
    private static final String COL5 = "HEADACHE";
    private static final String COL6 = "DIARRHEA";
    private static final String COL7 = "SOAR_THROAT";
    private static final String COL8 = "FEVER";
    private static final String COL9 = "MUSCLE_ACHE";
    private static final String COL10 = "LOSS_OF_SMELL_OR_TASTE";
    private static final String COL11 = "COUGH";
    private static final String COL12 = "SHORTNESS_OF_BREATH";
    private static final String COL13 = "FEELING_TIRED";

    public DatabaseHelper(@Nullable Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+ TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COL2 +" REAL, "+
                COL3 + " REAL, "+
                COL4 + " REAL, "+
                COL5 + " REAL, "+
                COL6 + " REAL, "+
                COL7 + " REAL, "+
                COL8 + " REAL, "+
                COL9 + " REAL, "+
                COL10 + " REAL, "+
                COL11 + " REAL, "+
                COL12 + " REAL, "+
                COL13 + " REAL);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean createRecord(HashMap<String,Float> dataMap) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2,(dataMap.get("Heart Rate")));
        contentValues.put(COL3,(dataMap.get("Respiratory Rate")));
        contentValues.put(COL4,(dataMap.get("Nausea")!=null)? dataMap.get("Nausea"): 0f);
        contentValues.put(COL5,(dataMap.get("Headache")!=null)? dataMap.get("Headache"): 0f);
        contentValues.put(COL6,(dataMap.get("Diarrhea")!=null)? dataMap.get("Diarrhea"): 0f);
        contentValues.put(COL7,(dataMap.get("Soar Throat")!=null)? dataMap.get("Soar Throat"): 0f);
        contentValues.put(COL8,(dataMap.get("Fever")!=null)? dataMap.get("Fever"): 0f);
        contentValues.put(COL9,(dataMap.get("Muscle Ache")!=null)? dataMap.get("Muscle Ache"): 0f);
        contentValues.put(COL10,(dataMap.get("Loss of Smell or Taste")!=null)? dataMap.get("Loss of Smell or Taste"): 0f);
        contentValues.put(COL11,(dataMap.get("Cough")!=null)? dataMap.get("Cough"): 0f);
        contentValues.put(COL12,(dataMap.get("Shortness of Breath")!=null)? dataMap.get("Shortness of Breath"): 0f);
        contentValues.put(COL13,(dataMap.get("Feeling Tired")!=null)? dataMap.get("Feeling Tired"): 0f);


        Log.d(TAG,"createRecord: Record created successfully in table "+TABLE_NAME);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if (result == -1){
            db.close();
            return false;
        } else {
            db.close();
            return true;
        }

    }

}
