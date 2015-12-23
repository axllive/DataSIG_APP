package com.prgguru.example;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/*
Classe responsável pela validação de login de usuário
Consulta o usuário e a palavra passe do mesmo no banco online
Além disso recupera as informações dos projetos do usuário no servidor
*/
public class LoginActivity extends ActionBarActivity {

    DBController controller = new DBController(this);

    private EditText login;
    private EditText password;
    private static String CURRENT_USER = "currentUser";


    //onResume o app verifica se existe usuário logado
    //o método do controller "getCurrentUser" retorna o login do usuario conectado
    //ou "404" se não existe usuário conectado
    //isso evita que ao retornar do descanso ao app ele volte a tela de login
    //invés da tela inicial


    //no onCreate o app inicia os campos de texto
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText) findViewById(R.id.editText1);
        password = (EditText) findViewById(R.id.editText2);

        login.setHorizontallyScrolling(true);
        password.setHorizontallyScrolling(true);

    }

    @Override
    protected void onPause(){
        super.onPause();
        //se necessário
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(controller.getCurrentUser().equals("404"))) {
            setContentView(R.layout.activity_main);
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //método de validação de login
    /*
        O método utiliza o container do tipo Params para armazenar as informações
        que serão enviadas via POST junto a conversão JSON que é feita usando o
        método da classe DBController aouthLogin;
        No lado do servidor, no endereço especificado existe o método authLogin.php
        que recebe e processa esses dados;
        Neste mesmo método são feitas chamadas de updateUser para armazenar no BD interno
        qual usuário está conectado, e getProj responsável por capturar as informações de
        quais projetos o usuário pertence.
     */
    public void authLogin(View v) {
        final String loginString;
        String senha;
        final int TIME_OUT = 1000;
        loginString = login.getText().toString();
        senha = password.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("usersJSON", controller.aouthLogin(loginString, senha));
        client.post("http://nbcgib.uesc.br/datasig/php/authLogin.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject arr = new JSONObject(response);
                    if (arr.get("login").toString().equals(loginString)) {
                        //salvando o usuário atual no DB interno
                        updateUser(loginString);
                        getProj(loginString);
                        Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //trocando a activity
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        }, TIME_OUT);
                    } else {
                        Toast.makeText(getApplicationContext(), "Usuário ou senha incorretos!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Um erro ocorreu!" + e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // TODO Auto-generated method stub

                Toast.makeText(getApplicationContext(), "Ops! Algo deu errado error" + statusCode + "  " + error.toString() + "  " + content, Toast.LENGTH_LONG).show();

            }
        });
    }

    //Armazena no banco local a informação que o corrente usuário está conectado
    public void updateUser(final String login) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("login", login);
        wordList.add(map);
        Gson gson = new GsonBuilder().create();
        params.put("usersJSON", gson.toJson(wordList));
        client.post("http://nbcgib.uesc.br/datasig/php/getUserId.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject arr = new JSONObject(response);
                    if (arr != null) {
                        int id = arr.getInt("id_usuario");
                        controller.logInIntoDB(login, id);
                    } else {
                        Toast.makeText(getApplicationContext(), "Ops! Algo deu errado", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // TODO Auto-generated method stub

            }
        });
    }

    //Recupera a lista de projetos no servidor e armazena localmente no banco de dados
    public void getProj(final String login) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("login", login);
        wordList.add(map);
        Gson gson = new GsonBuilder().create();
        params.put("usersJSON", gson.toJson(wordList));
        client.post("http://nbcgib.uesc.br/datasig/php/getAlluserProj.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    int id = controller.getCurrentUserID();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = (JSONObject) arr.get(i);
                        controller.insertUsrProjects(obj.get("id_projeto").toString(), obj.get("nome_projeto").toString(), id);
                    }
                    Toast.makeText(getApplicationContext(), "Sincronização Completa!", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), response + e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), statusCode + error.toString() + content, Toast.LENGTH_LONG).show();
            }
        });
    }


}
