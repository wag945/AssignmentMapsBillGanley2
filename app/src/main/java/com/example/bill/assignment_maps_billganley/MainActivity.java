package com.example.bill.assignment_maps_billganley;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextLat;
    private EditText editTextLong;
    private EditText editTextLocation;

    private Button buttonNavigate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextLat = (EditText) findViewById(R.id.edit_text_lat);
        editTextLong = (EditText) findViewById(R.id.edit_text_long);
        editTextLocation = (EditText) findViewById(R.id.edit_text_location);

        buttonNavigate = (Button) findViewById(R.id.button_navigate);

        buttonNavigate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        // Broadcast Receiver
//        Intent explicitIntent = new Intent(this, BroadcastReceiverMap.class);
        Double latitude = Double.valueOf(editTextLat.getText().toString());
        Double longitude = Double.valueOf(editTextLong.getText().toString());
        String location = editTextLong.getText().toString();

//        explicitIntent.putExtra("LATITUDE", latitude);
//        explicitIntent.putExtra("LONGITUDE", longitude);
//        explicitIntent.putExtra("LOCATION", location);
//
//        sendBroadcast(explicitIntent);

        // Navigating to MapActivity
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        intent.putExtra("LOCATION", location);

        startActivity(intent);
    }

}
