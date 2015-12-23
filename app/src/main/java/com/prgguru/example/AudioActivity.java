package com.prgguru.example;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

/**
 * Created by axll on 06/12/2015.
 * Módulo de gravação de áudio
 * contém os métodos obrigatórios para captura, gravação, pausa e reprodução
 */
public class AudioActivity extends Activity {
    //objetos da classe
    private static final String LOG_TAG = "AudioRecord";
    private static String mFileName = null;
    //setar botões correspondentes à activity
    private MediaRecorder mRecorder = null;
    //objeto view para gerenciamento da view correspondente a ação
    View audioView = null;
    //objeto chronometer para gerenciamento do cronômetro
    private Chronometer myChronometer;
    //strings para armazenamendo dos caminhos dos arquivos de áudio
    public String file1;
    public String file2;
    String[] audioUris;
    //long para guardar o estado do cronômetro
    private long chronometerTimeSpent = 0;
    private long chronometerTimeSpentPlaying = 0;
    //constantes de auxílio de gerenciamento
    private int RECORD_STATE_FIRST_RECORD = 1;
    private int RECORD_STATE_SECOND_RECORD = 2;
    private int RECORD_STATE_APPEND = 3;
    private int RECORD_STATE = 0;
    //objeto responsável por gerenciar a gravação de mídia
    private MediaPlayer mPlayer = null;
    //controle de execução
    private boolean alreadyPlayed = false;
    //controle de conteúdo da galeria
    private List<audioObjects> audios = new ArrayList<audioObjects>();

    /*
    Métodos Override do sistema Android
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_activity_record);
        //capturando a view atual para gerenciamento da interface nas outras funções
        audioView = this.getCurrentFocus();
        //mantendo a tela ligada ao executar essa activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //instanciando para não gerar NULLPOINTEREXCEPTION
        mPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mPlayer != null)
            stopPlaying(this.audioView);
        if (mRecorder != null)
            stopRecording(this.audioView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            pauseRecord(this.audioView);
        }

        if (mPlayer != null) {
            pausePlaying(this.audioView);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        audioView = this.getCurrentFocus();

    }

    public void setCurrentFilename(String filename) {
        this.mFileName = filename;
    }

    public String getCurrentFilename() {
        return this.mFileName;
    }

    //métodos obrigatórios
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording(audioView);
        }
    }

    //reprodução
    private void onPlay(boolean start) {
        if (start) {
            startPlaying(this.audioView);
        } else {
            stopPlaying(this.audioView);
        }
    }//commit

    //inicia reprodução
    public void startPlaying(View v) {
        //iniciando o cronômetro
        if (chronometerTimeSpentPlaying == 0) {
            setContentView(R.layout.audio_activity_playing);
            myChronometer = (Chronometer) findViewById(R.id.chronometerplayer);
            myChronometer.setBase(SystemClock.elapsedRealtime());
            myChronometer.start();
            try {
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(getCurrentFilename());
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        myChronometer.stop();
                        setContentView(R.layout.audio_activity_paused);
                        alreadyPlayed = true;
                        myChronometer = (Chronometer) findViewById(R.id.chronometerrecord);
                        myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpent);
                        mPlayer.release();
                        mPlayer = null;
                    }
                });
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
        //resumindo o cronômetro
        else if (alreadyPlayed) {
            setContentView(R.layout.audio_activity_playing);
            myChronometer = (Chronometer) findViewById(R.id.chronometerplayer);
            myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpentPlaying);
            myChronometer.start();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    myChronometer.stop();
                    setContentView(R.layout.audio_activity_paused);
                    alreadyPlayed = true;
                    myChronometer = (Chronometer) findViewById(R.id.chronometerrecord);
                    myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpent);
                    mPlayer.release();
                    mPlayer = null;
                    chronometerTimeSpentPlaying = 0;
                }
            });

        }

    }

    public void pausePlaying(View v) {
        mPlayer.pause();
        chronometerTimeSpentPlaying = myChronometer.getBase() - SystemClock.elapsedRealtime();
        myChronometer.stop();
        setContentView(R.layout.audio_activity_pausedplay);
        myChronometer = (Chronometer) findViewById(R.id.chronometerplayerpaused);
        myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpentPlaying);
        this.alreadyPlayed = true;
    }

    //para reprodução
    public void stopPlaying(View v) {
        chronometerTimeSpentPlaying = myChronometer.getBase() - SystemClock.elapsedRealtime();
        myChronometer.stop();
        setContentView(R.layout.audio_activity_paused);
        this.alreadyPlayed = true;
        myChronometer = (Chronometer) findViewById(R.id.chronometerrecord);
        myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpent);
        mPlayer.release();
        mPlayer = null;
    }

    //inicia gravação
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        file1 = AudioFilePath();
        setCurrentFilename(file1);
        mRecorder.setOutputFile(file1);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    //Pausa a gravação
    public void resumeRecording(int state) {
        switch (state) {
            case 1://RECORD PAUSE
                //pausando o cronômetro
                chronometerTimeSpent = myChronometer.getBase() - SystemClock.elapsedRealtime();
                myChronometer.stop();
                //parando a gravação
                mRecorder.stop();
                //liberando os recursos de gravação
                mRecorder.release();
                //mudando o layout para a aparência de PAUSED
                setContentView(R.layout.audio_activity_paused);
                myChronometer = (Chronometer) findViewById(R.id.chronometerrecord);
                myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpent);
                mRecorder = null;
                break;
            case 2://RECORD RESUME
                //resumindo o cronômetro
                setContentView(R.layout.audio_activity_recording);
                myChronometer = (Chronometer) findViewById(R.id.chronometerrecord);
                myChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeSpent);
                myChronometer.start();
                //iniciando uma nova gravação com um arquivo diferente
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                file2 = AudioFilePath();
                mRecorder.setOutputFile(file2);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "prepare() failed");
                }
                mRecorder.start();
                break;
            case 3://RECORD APPEND
                //aqui unimos os arquivos m4a para gerar o efeito pause-----------------------------
                //pausando o cronometro
                chronometerTimeSpent = myChronometer.getBase() - SystemClock.elapsedRealtime();
                myChronometer.stop();
                //adicionando o caminho completo dos arquivos a concatenar num array de strings
                audioUris = new String[2];
                audioUris[0] = file1;
                audioUris[1] = file2;
                //encapsulando os dados de acordo com a API ISOParser/MP4Parser
                List<Movie> inMovies = new ArrayList<Movie>();
                //adicionando os caminhos no arrayist
                try {
                    inMovies.add(MovieCreator.build(audioUris[0]));
                    inMovies.add(MovieCreator.build(audioUris[1]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //linkedList contendo todos os arquivos de audio
                List<Track> audioTracks = new LinkedList<Track>();
                //adicionando os arquivos de audio
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {
                            audioTracks.add(t);
                        }
                    }
                }

                Movie result = new Movie();
                //concatenando os objetos
                if (audioTracks.size() > 0) {
                    try {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //construindo o arquivo final
                Container out = new DefaultMp4Builder().build(result);
                //diretório padrão de armazenamento de audios do DataSIG
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), "DataSIG_AUDIO");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("DataSIG", "failed to create directory");
                    }
                }
                //criando a TAG de áudio de acordo com o tempo
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                try {
                    FileChannel fc = new RandomAccessFile(mediaStorageDir.getAbsolutePath() +
                            File.separator + "DataSIG_AUDIO" + timeStamp + ".mp4", "rw").getChannel();
                    out.writeContainer(fc);
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File fp = new File(file1);
                fp.delete();
                fp = new File(file2);
                fp.delete();
                file1 = mediaStorageDir.getAbsolutePath() +
                        File.separator + "DataSIG_AUDIO" + timeStamp + ".mp4";
                setCurrentFilename(file1);
                break;
            //fim case 3----------------------------------------------------------------------------
        }

    }

    //para gravação
    public void stopRecording(View v) {

        if (mPlayer != null) {
            mPlayer.stop();
        }

        switch (RECORD_STATE) {
            /*
            Caso 1: nenhuma pausa foi feita;
                    O arquivo é finalizado normalmente.
             */
            case 0:
                RECORD_STATE = 0;
                myChronometer.stop();
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                setContentView(R.layout.audio_activity_record);
                break;
            /*
            Caso 2: a aplicação está com a gravação pausada pela primeira vez;
                    O arquivo já foi finalizado.
                    Não há o que fazer.
             */
            case 1:
                RECORD_STATE = 0;
                setContentView(R.layout.audio_activity_record);
                break;
            /*
            Caso 3: a gravação foi resumida de uma pausa;
                    é feito o MERGE dos arquivos;
                    o arquivo é então finalizado;
                    A aplicação volta ao seu estado inicial de gravação;
             */
            case 2:
                pauseRecord(v);
                RECORD_STATE = 0;
                setContentView(R.layout.audio_activity_record);
                break;
        }
        Toast.makeText(getApplicationContext(), "Gravação armazenada!", Toast.LENGTH_LONG).show();
        SystemClock.sleep(1000);
    }

    public void RecordButton(View v) {
        boolean mStartRecording = true;

        onRecord(mStartRecording);
        if (mStartRecording) {
            setContentView(R.layout.audio_activity_recording);
            //cronometro  só pode ser chamado quando a activity que o contém for a principal
            myChronometer = (Chronometer) findViewById(R.id.chronometerrecord);
            myChronometer.setBase(SystemClock.elapsedRealtime());
            myChronometer.start();
        } else {
            Toast.makeText(getApplicationContext(), "Gravação parada!", Toast.LENGTH_LONG).show();
        }
        mStartRecording = !mStartRecording;
    }


    //captura o caminho completo do storage e cria um nome para o arquivo
    public String AudioFilePath() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "DataSIG_AUDIO");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("DataSIG", "failed to create directory");
                return null;
            }
        }
        File nomediafile = new File(mediaStorageDir.getPath() + File.separator + ".nomedia");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "AUDIO_" + timeStamp + ".mp4");

        return mediaFile.getAbsolutePath();
    }


    //Volta a tela inicial
    public void callHomeActivity(View view) {
        Intent objIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(objIntent);
    }

    public void pauseRecord(View v) {
        switch (RECORD_STATE) {
            /*
            Caso 1: PAUSE
                O usuário aperta o botão para pausar;
                a gravação é PARADA e o arquivo finalizado
             */
            case 0:
                resumeRecording(RECORD_STATE_FIRST_RECORD);
                RECORD_STATE = 1;
                break;
            /*
            Caso 2: RESUME
                O usuário já apertou o botão de pausar e aperta novamente
                para resumir a gravação;
                É iniciada uma NOVA gravação com um nome de arquivo diferente
                 da primeira (file2);
             */
            case 1:
                resumeRecording(RECORD_STATE_SECOND_RECORD);
                RECORD_STATE = 2;
                break;
            /*
            Caso 3: RESUME -> PAUSE
                O usuário já resumiu a gravação e pausou novamente;
                É feito o MERGE das gravações 1 e 2, e são salvas
                no arquivo file1;
                Então voltamos ao estado em que a gravação será parada
                como se fosse a primeira gravação (file1);
             */
            case 2:
                resumeRecording(RECORD_STATE_FIRST_RECORD);
                resumeRecording(RECORD_STATE_APPEND);
                RECORD_STATE = 1;
                break;
        }
    }

    /*
    Método para deletar a composição da gravação corrente
     */
    public void deleteAudio(View v) {
        File fp;
        //para a execução da gravação caso esteja tocando
        if (mPlayer != null) {
            mPlayer.stop();
        }

        switch (RECORD_STATE) {
            case 0:
                fp = new File(file1);
                fp.delete();
                Toast.makeText(getApplicationContext(), "Gravação descartada!", Toast.LENGTH_LONG).show();
                SystemClock.sleep(1000);
                setContentView(R.layout.audio_activity_record);
                break;
            case 1:
                fp = new File(file1);
                fp.delete();
                Toast.makeText(getApplicationContext(), "Gravação descartada!", Toast.LENGTH_LONG).show();
                SystemClock.sleep(1000);
                setContentView(R.layout.audio_activity_record);
                break;
            case 2:
                fp = new File(file1);
                fp.delete();
                fp = new File(file2);
                fp.delete();
                Toast.makeText(getApplicationContext(), "Gravação descartada!", Toast.LENGTH_LONG).show();
                SystemClock.sleep(1000);
                setContentView(R.layout.audio_activity_record);
                break;
        }
    }

    /*
    Método de gerenciamento da galeria de áudios gravados
     */
    public void gallery(View v) {
        //setando o layout correto
        setContentView(R.layout.audio_activity_gallery);
        //capturando a lista de arquivos da pasta [TEMPORÁRIO]
        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "DataSIG_AUDIO");
        ;
        File[] listOfFiles = folder.listFiles();
        //tentativa de capturar a duração dos áudios
        //[TEMPORÁRIO- Correto será incluir no ato da gravação no BD interno]
        mPlayer = new MediaPlayer();
        long millis;
        String time;
        //adicionando a lista de arquivos
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                try {
                    mPlayer.setDataSource(listOfFiles[i].getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                millis = mPlayer.getDuration();
                long second = (millis / 1000) % 60;
                long minute = (millis / (1000 * 60)) % 60;
                long hour = (millis / (1000 * 60 * 60)) % 24;
                time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
                audios.add(new audioObjects(listOfFiles[i].getName(), "" + time, "12/12"));
            }
        }
        //encapsulando o arraylist com os nomes dos arquivos ao ArrayAdapter
        ArrayAdapter<audioObjects> adapter = new myAudiosAdapter();
        ListView list = (ListView) findViewById(R.id.GallerylistView);
        //... e setando o array ao ListView
        list.setAdapter(adapter);
        //cirando as opções de onClick no ListView
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                audioObjects clickedAudio = audios.get(position);
                String message = "You clicked position " + position
                        + " Which is car make " + clickedAudio.getFilename();
                Toast.makeText(AudioActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
    Adapter de áudio para direcionar o conteúdo do arrayAdapter
    à interface ListView de forma que cada célula da list view
    contenha as informações corretamente encapsuladas.
     */
    private class myAudiosAdapter extends ArrayAdapter<audioObjects> {
        public myAudiosAdapter() {
            super(AudioActivity.this, R.layout.audio_gallery_item_list, audios);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.audio_gallery_item_list, parent, false);
            }

            // Find the car to work with.
            audioObjects currentAudio = audios.get(position);

            // Fill the view
            ImageView imageView = (ImageView) itemView.findViewById(R.id.item_icon);
            imageView.setImageResource(R.drawable.play_icon_256);

            // Make:
            TextView makeText = (TextView) itemView.findViewById(R.id.item_txtMake);
            makeText.setText(currentAudio.getFilename());

            // Year:
            TextView yearText = (TextView) itemView.findViewById(R.id.item_txtYear);
            yearText.setText(currentAudio.getFileLenght());

            // Condition:
            TextView condionText = (TextView) itemView.findViewById(R.id.item_txtCondition);
            condionText.setText(currentAudio.getCreationDate());

            return itemView;
        }

    }
}
