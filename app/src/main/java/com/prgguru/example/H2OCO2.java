package com.prgguru.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by Alex on 14/07/2015.
 */
public class H2OCO2 extends Activity implements LocationListener {
    EditText nome1;
    EditText nome2;
    DBController controller = new DBController(this);
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;
    String lat;
    String provider;
    protected String latitude,longitude;
    protected boolean gps_enabled,network_enabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hdoisocodois);
        txtLat = (TextView) findViewById(R.id.textView2);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        if (! controller.projectExists("Coleta Agua") ){
            Toast.makeText(getApplicationContext(), "Infelizmente você não está cadastrado no projeto Coleta Água", Toast.LENGTH_LONG).show();
            Intent objIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(objIntent);
        }

        nome1 = (EditText) findViewById(R.id.nome1);
        nome2 = (EditText) findViewById(R.id.nome2);
    }

    /**
     * Called when Save button is clicked
     * @param view
     */
    public void addNewUser(View view) {
        HashMap<String, String> queryValues = new HashMap<String, String>();
        queryValues.put("nome1", nome1.getText().toString());
        queryValues.put("nome2", nome2.getText().toString());
        queryValues.put("txtLat", txtLat.getText().toString());

        if (nome1.getText().toString() != null
                && nome1.getText().toString().trim().length() != 0) {
            controller.insertH2O(queryValues);
            this.callHomeActivity(view);
        } else {
            Toast.makeText(getApplicationContext(), "Por favor, preencha todos os campos!",
                    Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Navigate to Home Screen
     * @param view
     */
    public void callHomeActivity(View view) {
        Intent objIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(objIntent);
    }

    /**
     * Called when Cancel button is clicked
     * @param view
     */
    public void cancelAddUser(View view) {
        this.callHomeActivity(view);
    }

    @Override
    public void onLocationChanged(Location location) {
        txtLat = (TextView) findViewById(R.id.textView2);
        txtLat.setText(location.getLatitude() + ", " + location.getLongitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");

    }
}
