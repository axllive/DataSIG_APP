package com.prgguru.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Alex on 13/07/2015.
 */
public class CameraActivity extends Activity implements LocationListener {
    //Objetos da camera
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    //constante para tipo de dados
    public static final int MEDIA_TYPE_IMAGE = 1;
    //objeto para localização
    protected LocationManager locationManager;
    //objeto de controle do BD interno
    DBController controller = new DBController(this);
    //demais atributos
    public String localização;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //desenhando a interface para o posicionamento da câmera
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //iniciando objeto de localização
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //direcionando a view para o layout cameraLayout
        setContentView(R.layout.cameralayout);
        //try/catch para iniciar a camera (hardware)
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
        //se conseguiu, irá iniciar a view com os métodos obrigatórios da camera
        if (mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }
        //botão para fechar a tela
        ImageButton imgClose = (ImageButton) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
        //botão para ir diretamente a galeria padrão ou escolher o app apropriado
        ImageButton imgGallery = (ImageButton) findViewById(R.id.imgGallery);
        imgGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DEFAULT);
                intent.setType("image/*");
                startActivity(intent);
            }
        });
        /**
         * Método responsável pela ação ao tirar foto
         *
         */
        final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                String picturepath;
                File pictureFile;
                pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Toast.makeText(getApplicationContext(), "Algum erro ocorreu", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    //Aqui a operação de arquivo é feita, puramente java
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    picturepath = pictureFile.getAbsolutePath();
                    fos.write(data);
                    //esse trecho faz com que a imagem apareça na galeria padrão do dispositivo
                    MediaScannerConnection.scanFile(CameraActivity.this,
                            new String[]{picturepath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                }
                            });
                    fos.close();
                    //se possui conexão à internet....
                    if (hasActiveInternetConnection(CameraActivity.this)) {
                        //... envia a imagem
                        sendPic(decodeImage(picturepath));
                    } else {
                        //senão adiciona os dados no banco interno para futuro upload (?)
                        addNewUser(picturepath, localização);
                    }

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
        };
        /**
         * método onClick para captura de imagem
         */
        ImageButton captureButton = (ImageButton) findViewById(R.id.takepicture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //tirando a foto propriamente
                        mCamera.takePicture(null, null, mPicture);
                        //toast com um tempinho de delay para o usuario ler a mensagem antes de retornar a camera
                        Toast.makeText(getApplicationContext(), "Salvo na galeria do dispositivo", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //reiniciando o preview da camera
                                mCamera.stopPreview();
                                mCamera.startPreview();

                            }
                        }, 2000);

                    }
                }
        );

    }
     /**
     * Cria o arquivo e o diretorio para salvar a foto
     */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DataSIG_FOTO");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("DataSIG", "failed to create directory");
                return null;
            }
        }
        // Cria o nome do arquivo ano/mes/dia_hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Método de envio da foto como dados JSON
     * Utiliza o mesmo esquema dos outros tipos de dados
     * @param photo
     */
    public void sendPic(final String photo) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("foto", photo);
        wordList.add(map);
        Gson gson = new GsonBuilder().create();
        params.put("usersJSON", gson.toJson(wordList));
        client.post("http://nbcgib.uesc.br/datasig/php/photoupload.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error,String content) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), statusCode + error.toString(), Toast.LENGTH_LONG).show();

            }
        });
    }
    //armazena os dados da imagem no banco local
    public void addNewUser(String picturePath, String localização) {
        HashMap<String, String> queryValues = new HashMap<String, String>();
        queryValues.put("nome1", picturePath);
        queryValues.put("txtLat", localização);
        controller.insertPicture(queryValues);

    }

    /**
     * o bloco de métodos abaixo são gerenciais de localização
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        this.localização = location.getLatitude() + ", " + location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    //fim de métodos de localização
    //-----------------------------

    /**
     * Responsável por decodificar a imagem em string para envio ao servidor
     * @param picturepath
     * @return
     */
    public String decodeImage(String picturepath) {
        Bitmap bm = BitmapFactory.decodeFile(picturepath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byte_arr = baos.toByteArray();
        String bitmapEncoded = Base64.encodeToString(byte_arr, 0);
        return bitmapEncoded;
    }

    /**
     * método de checagem de disponibilidade de conexão
     * @param context
     * @return
     */
    public boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable(CameraActivity.this)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {

            }
        } else {

        }
        return false;
    }
    //método auxiliar para verificar a disponibilidade de rede
    private boolean isNetworkAvailable(CameraActivity cameraActivity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
