package com.prgguru.example;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DBController  extends SQLiteOpenHelper {

	public DBController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
    }
	//Creates Table
	@Override
	public void onCreate(SQLiteDatabase database) {
		String query;
		query = "CREATE TABLE usuario ( id_usuario INTEGER PRIMARY KEY, login TEXT, isLogged INTEGER)";
		database.execSQL(query);
		query = "CREATE TABLE etiqueta ( userId INTEGER PRIMARY KEY, valor TEXT, label INTEGER, coordinates TEXT, udpateStatus TEXT, fk_id_usuario INTEGER, FOREIGN KEY (fk_id_usuario) REFERENCES usuario (id_usuario))";
		database.execSQL(query);
		query = "CREATE TABLE projeto ( id_projeto INTEGER PRIMARY KEY, nome_projeto TEXT, fk_id_usuario INTEGER, FOREIGN KEY (fk_id_usuario) REFERENCES usuario (id_usuario))";
		database.execSQL(query);
	}

	public void insertUsrProjects(String id_projeto, String nome_projeto, int fk_id_usuario){
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "INSERT OR REPLACE INTO projeto (id_projeto, nome_projeto, fk_id_usuario) VALUES (" + id_projeto + ",'" + nome_projeto + "'," + fk_id_usuario + ")";
		database.execSQL(query);
		database.close();
	}

	public String getProj(){
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "SELECT nome_projeto FROM projeto WHERE fk_id_usuario = " + getCurrentUserID();
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		if (cursor.moveToFirst()) {
			String username = cursor.getString(cursor.getColumnIndex("nome_projeto"));
			return username;
		}
		else{
			String notFound = "404";
			return notFound;
		}
	}

	public Boolean projectExists(String project){
		String username = "";
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "SELECT nome_projeto FROM `projeto` WHERE nome_projeto = '" + project + "'";
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		if (cursor.moveToFirst())
			username = cursor.getString(cursor.getColumnIndex("nome_projeto"));

		return username.equals(project);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		String query;
		query = "DROP TABLE IF EXISTS etiqueta";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS usuario";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS projeto";
		database.execSQL(query);
        onCreate(database);
	}
	/**
	 * Inserts User into SQLite DB
	 * @param queryValues
	 */
	//insertUser na verdade � insere etiqueta
	//a tag "label"
	public void insertUser(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("valor", queryValues.get("nome1"));
		values.put("label", 16);
		values.put("coordinates", queryValues.get("txtLat"));
		values.put("udpateStatus", "no");
		values.put("fk_id_usuario", getCurrentUser());
		database.insert("etiqueta", null, values);
		database.close();
		
		SQLiteDatabase database2 = this.getWritableDatabase();
		ContentValues values2 = new ContentValues();
		values2.put("valor", queryValues.get("nome2"));
		values2.put("label", 17);
		values2.put("coordinates", queryValues.get("txtLat"));
		values2.put("udpateStatus", "no");
		values2.put("fk_id_usuario", getCurrentUser());
		database2.insert("etiqueta", null, values2);
		database2.close();
	}

	public void insertH2O(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("valor", queryValues.get("nome1"));
		values.put("label", 1);
		values.put("coordinates", queryValues.get("txtLat"));
		values.put("udpateStatus", "no");
		values.put("fk_id_usuario", getCurrentUser());
		database.insert("etiqueta", null, values);
		database.close();

		SQLiteDatabase database2 = this.getWritableDatabase();
		ContentValues values2 = new ContentValues();
		values2.put("valor", queryValues.get("nome2"));
		values2.put("label", 2);
		values2.put("coordinates", queryValues.get("txtLat"));
		values2.put("udpateStatus", "no");
		values2.put("fk_id_usuario", getCurrentUser());
		database2.insert("etiqueta", null, values2);
		database2.close();
	}

	public void insertPicture(HashMap<String, String> queryValues){
		SQLiteDatabase database2 = this.getWritableDatabase();
		ContentValues values2 = new ContentValues();
		values2.put("valor", queryValues.get("nome1"));
		values2.put("label", 22);
		values2.put("coordinates", queryValues.get("txtLat"));
		values2.put("udpateStatus", "no");
		values2.put("fk_id_usuario", getCurrentUser());
		database2.insert("etiqueta", null, values2);
		database2.close();
	}

	public void insertPlant(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("valor", queryValues.get("nome1"));
		values.put("label", 18);
		values.put("coordinates", queryValues.get("txtLat"));
		values.put("udpateStatus", "no");
		values.put("fk_id_usuario", getCurrentUser());
		database.insert("etiqueta", null, values);
		database.close();

		SQLiteDatabase database2 = this.getWritableDatabase();
		ContentValues values2 = new ContentValues();
		values2.put("valor", queryValues.get("nome2"));
		values2.put("label", 19);
		values2.put("coordinates", queryValues.get("txtLat"));
		values2.put("udpateStatus", "no");
		values2.put("fk_id_usuario", getCurrentUser());
		database2.insert("etiqueta", null, values2);
		database2.close();

		SQLiteDatabase database3 = this.getWritableDatabase();
		ContentValues values3 = new ContentValues();
		values3.put("valor", queryValues.get("nome3"));
		values3.put("label", 20);
		values3.put("coordinates", queryValues.get("txtLat"));
		values3.put("udpateStatus", "no");
		values3.put("fk_id_usuario", getCurrentUser());
		database3.insert("etiqueta", null, values3);
		database3.close();
	}

	//Método responsável para registrar no banco local o usuário atual
	public void logInIntoDB(String userName, int id){
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "INSERT OR REPLACE INTO usuario(id_usuario,login,isLogged) VALUES("+ id +",'"+userName+"',"+1+")";
		database.execSQL(query);
		database.close();
	}

	public void updateDB(String userName, int id){
		SQLiteDatabase database = this.getWritableDatabase();
		String whereclause = "UPDATE usuario SET isLogged = 0 WHERE id_usuario = " + id;
		database.execSQL(whereclause);
		database.close();
	}

	public void logOutIntoDB(String userName, int id){
		SQLiteDatabase database = this.getWritableDatabase();
		String whereclause = "UPDATE usuario SET isLogged = 0 WHERE id_usuario = " + id;
		database.execSQL(whereclause);
		database.close();
	}

	//Método responsável para capturar do banco local quem é o usuário local
	public String getCurrentUser(){
		String query = "SELECT login FROM usuario WHERE isLogged = '1'";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		if (cursor.moveToFirst()) {
			String username = cursor.getString(cursor.getColumnIndex("login"));
			return username;
		}
		else{
			String notFound = "404";
			return notFound;
		}
	}

	public int getCurrentUserID(){
		String query = "SELECT id_usuario FROM usuario WHERE isLogged = '1'";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		if (cursor.moveToFirst()) {
			String username = cursor.getString(cursor.getColumnIndex("id_usuario"));
			return Integer.parseInt(username);
		}
		else{
			return 404;
		}
	}

	/**
	 * Get list of LABELS from SQLite DB as Array List
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getAllUsers() {
		ArrayList<HashMap<String, String>> wordList;

		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT  * FROM etiqueta";

		SQLiteDatabase database = this.getWritableDatabase();

		Cursor cursor = database.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("userId", cursor.getString(0));
				map.put("nome1", cursor.getString(1));

				if(cursor.getInt(2)==16){
					map.put("nome2", "coliformes");
				}
				else if(cursor.getInt(2)==17){
					map.put("nome2", "PH");
				}
				else if(cursor.getInt(2)==1){
					map.put("nome2", "H2O");
				}
				else if(cursor.getInt(2)==2){
					map.put("nome2", "CO2");
				}
				else if(cursor.getInt(2)==18){
					map.put("nome2", "TamCaule");
				}
				else if(cursor.getInt(2)==19){
					map.put("nome2", "CorFlor");
				}
				else if(cursor.getInt(2)==20){
					map.put("nome2", "numPetalas");
				}
				else if(cursor.getInt(2)==20){
					map.put("nome3", "numPetalas");
				}
				else if(cursor.getInt(2)==22){
					map.put("nome2", "foto");
				}

				map.put("txtLat", cursor.getString(3));
				wordList.add(map);
			} while (cursor.moveToNext());
		}
		database.close();
		return wordList;
	}

	public ArrayList<HashMap<String, String>> getAllProjects() {
		ArrayList<HashMap<String, String>> wordList;

		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT  nome_projeto FROM projeto WHERE fk_id_usuario =" + getCurrentUserID();

		SQLiteDatabase database = this.getWritableDatabase();

		Cursor cursor = database.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("nome_projeto", cursor.getString(0));
				wordList.add(map);
			} while (cursor.moveToNext());
		}
		database.close();
		return wordList;
	}

	//Método para a autenticação MD5 da senha (localmente)
	//Já retorna a string senha formatada como objeto JSON
	public String aouthLogin(String user, String senha){
		
		try {

			MessageDigest md = MessageDigest.getInstance("MD5");
		    md.update(senha.getBytes(),0,senha.length());
		    BigInteger i = new BigInteger(1, md.digest());
		    senha = String.format("%1$032X", i);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("senha", senha);
		wordList.add(map);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(wordList);
		
	}
	
	/**
	 * Compose JSON out of SQLite records
	 * @return
	 */
	/**A ideia do JSON e usar chave;valor para indexar as informações
	 * e utilizálas posteriormente no servidor, tendo pleno acesso
	 * das informações desejadas.
	 * */
	public String composeJSONfromSQLite(){
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT  * FROM etiqueta where udpateStatus = '"+"no"+"'"; //query para o DB interno!!!!
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    if (cursor.moveToFirst()) {
	        do {
	        	HashMap<String, String> map = new HashMap<String, String>();
	        	map.put("userId", cursor.getString(0));
	        	map.put("nome1", cursor.getString(1));
	        	wordList.add(map);
				if(cursor.getInt(2)==16){
					map.put("nome2", "coliformes");
				}
				else if(cursor.getInt(2)==17){
					map.put("nome2", "PH");
				}
				else if(cursor.getInt(2)==1){
					map.put("nome2", "H2O");
				}
				else if(cursor.getInt(2)==2){
					map.put("nome2", "CO2");
				}
				else if(cursor.getInt(2)==18){
					map.put("nome2", "TamCaule");
				}
				else if(cursor.getInt(2)==19){
					map.put("nome2", "CorFlor");
				}
				else if(cursor.getInt(2)==20){
					map.put("nome2", "numPetalas");
				}
				else if(cursor.getInt(2)==21){
					map.put("nome2", "fotos");
				}
	        	map.put("txtLat", cursor.getString(3));
	        } while (cursor.moveToNext());
	    }
	    database.close();
		Gson gson = new GsonBuilder().create();
		return gson.toJson(wordList);  // enviando OBJETO ao JSON
	}
	
	/**
	 * Get Sync status of SQLite
	 * @return
	 */
	public String getSyncStatus(){
	    String msg = null;
	    if(this.dbSyncCount() == 0){
	    	msg = "Coletas Sincronizadas com Sucesso!";
	    }else{
	    	msg = "(!) Objetos n�o sincronizados!\n";
	    }
	    return msg;
	}
	
	/**
	 * Get SQLite records that are yet to be Synced
	 * @return
	 */
	public int dbSyncCount(){
		int count = 0;
		String selectQuery = "SELECT  * FROM etiqueta where udpateStatus = '"+"no"+"'";
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    count = cursor.getCount();
	    database.close();
		return count;
	}
	
	//new
	public boolean userExists(String username){
		String result;
		String query =  "SELECT * FROM usuario where login ="+ username;
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(query, null);
		
		result = cursor.getString(0);

		return result.equals(username);
		
	}
	
	/**
	 * Update Sync status against each User ID
	 * @param id
	 * @param status
	 */
	public void updateSyncStatus(String id, String status){
		SQLiteDatabase database = this.getWritableDatabase();	 
		String updateQuery = "Update etiqueta set udpateStatus = '"+ status +"' where userId="+"'"+ id +"'";
		Log.d("query",updateQuery);		
		database.execSQL(updateQuery);
		database.close();
	}
}
