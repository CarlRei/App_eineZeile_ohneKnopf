package com.example.carlreichert.stakk_meth_1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Startseite extends AppCompatActivity implements View.OnClickListener {

    public Button btn1;
    public int uebergabeID = 1;
    String uebergabeText;
    public Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startseite);

        //readFile();

        btn1 = (Button) findViewById(R.id.button);
        btn1.setOnClickListener(this);
        setupSpinner();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.button:{
                Intent nextAct = new Intent(Startseite.this, Textseite.class);
                nextAct.putExtra("Uebergabetext", uebergabeText);
                startActivityForResult(nextAct, uebergabeID);
            }
        }

    }

    private void setupSpinner(){
        final String[] texte = {"glasbluetenfest", "test_text"};
        spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(Startseite.this, android.R.layout.simple_spinner_dropdown_item, texte);

        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int spinnerPos = spinner.getSelectedItemPosition();
                uebergabeText = texte[spinnerPos];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String readFile(String fileName){
        StringBuilder text = new StringBuilder();
        Resources res = this.getResources();
        int textId = res.getIdentifier(fileName, "raw", this.getPackageName());
        InputStream is = getApplicationContext().getResources().openRawResource(textId);
        BufferedReader bRead = new BufferedReader (new InputStreamReader(is));
        String line = "";

        try {
            while((line = bRead.readLine()) != null){
                text.append(line);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
