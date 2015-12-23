package com.prgguru.example;

import java.io.ByteArrayOutputStream;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends ActionBarActivity {
    //DB Class to perform DB related operations
    DBController controller = new DBController(this);
    //Progress Dialog Object
    ProgressDialog prgDialog;
    Button btn;
    Button username;
    Button sair;
    public static boolean isActiveActivity = false;
    private static String IS_ACTIVE = "activityControl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  //redirecionando para a activity de login, não a de coleta
        //controle de activity
        this.isActiveActivity = true;
        //Get User records from SQLite DB
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers(); //na verdade é a lista de etiquetas lançadas
        btn = (Button) findViewById(R.id.button1);
        username = (Button) findViewById(R.id.username);
        sair = (Button) findViewById(R.id.sair);

        username.setText(controller.getCurrentUser());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu

                PopupMenu popup = new PopupMenu(MainActivity.this, btn);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.dataoptionsmenu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent i;
                        switch (item.getItemId()) {
                            case R.id.ar:
                                i = new Intent(MainActivity.this, H2OCO2.class);
                                startActivity(i);
                                break;
                            case R.id.agua:
                                i = new Intent(MainActivity.this, PHCloriformes.class);
                                startActivity(i);
                                break;
                            case R.id.plant:
                                i = new Intent(MainActivity.this, addPlantData.class);
                                startActivity(i);
                                break;
                            case R.id.audio:
                                i = new Intent(MainActivity.this, AudioActivity.class);
                                startActivity(i);
                                break;
                            case R.id.photo:
                                i = new Intent(MainActivity.this, CameraActivity.class);
                                startActivity(i);
                                break;

                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

        sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.logOutIntoDB(controller.getCurrentUser(), controller.getCurrentUserID());
                Intent backwards = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(backwards);

            }
        });

        if (userList.size() != 0) {
            //Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, userList, R.layout.view_user_entry, new String[]{"userId", "nome1", "nome2"}, new int[]{R.id.userId, R.id.nome1, R.id.nome2});
            ListView myList = (ListView) findViewById(android.R.id.list);
            myList.setAdapter(adapter);
            //Display Sync status of SQLite DB
            Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();
        }
        //Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Synching SQLite Data with Remote MySQL DB. Please wait...");
        prgDialog.setCancelable(false);
    }

    //fim onCreate
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(IS_ACTIVE, isActiveActivity);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state members from saved instance
        isActiveActivity = savedInstanceState.getBoolean(IS_ACTIVE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //When Sync action button is clicked
        if (id == R.id.refresh) {
            //Sync SQLite DB data to remote MySQL DB
            syncSQLiteMySQLDB();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void syncSQLiteMySQLDB() {
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
        if (userList.size() != 0) {
            if (controller.dbSyncCount() != 0) {
                prgDialog.show();
                params.put("usersJSON", controller.composeJSONfromSQLite());
                client.post("http://nbcgib.uesc.br/datasig/php/insertuser.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        prgDialog.hide();

                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println(arr.length());
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                System.out.println(obj.get("id"));
                                System.out.println(obj.get("status"));
                                controller.updateSyncStatus(obj.get("id").toString(), obj.get("status").toString());
                            }
                            Toast.makeText(getApplicationContext(), "Sincroniza�‹o Completa!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Um erro ocorreu! [Server's JSON response might be invalid]!" + response, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub
                        prgDialog.hide();
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(), "Recurso n‹o encontrado", Toast.LENGTH_LONG).show();
                        } else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(), "Ops! Algo deu errado", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Erro inesperado. Sem internet" + error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Dados j‡ est‹o sincronizados", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sem dados no banco. Por favor, insira uma nova coleta", Toast.LENGTH_LONG).show();
        }
    }


}
