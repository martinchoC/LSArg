package com.lsarg.fflush.lsarg.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.lsarg.fflush.lsarg.R;
import com.lsarg.fflush.lsarg.Seña;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WordActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    Seña seña;

    private static final int RECOVERY_REQUEST = 1;
    private YouTubePlayerView youTubeView;
    private String YouTubeKey = "AIzaSyC0k5lY3IA0z2BJ4hTnnhDSyE8SSJB9dzg";

    @BindView(R.id.textViewWord) TextView nombre;

    @BindView(R.id.textView4) TextView configuracion;
    @BindView(R.id.textView5) TextView movimiento;
    @BindView(R.id.textView6) TextView orientacion;
    @BindView(R.id.textView7) TextView ubicacion;
    @BindView(R.id.textView8) TextView rasgosNoManuales;
    @BindView(R.id.textView9) TextView observacion;
    @BindView(R.id.imageView1) ImageView foto1;
    @BindView(R.id.imageView2) ImageView foto2;
    @BindView(R.id.imageView3) ImageView foto3;
    @BindView(R.id.imageView4) ImageView foto4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        ButterKnife.bind(this);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(YouTubeKey, this);

        Bundle i = getIntent().getExtras();
        seña = new Seña(i.getString("nombre"));
        seña.setNombre(i.getString("nombre"));
        seña.setRasgosNoManuales(i.getString("rasgosNoManuales"));
        seña.setUbicacion(i.getString("ubicacion"));
        seña.setObservacion(i.getString("observacion"));
        seña.setOrientacion(i.getString("orientacion"));
        seña.setMovimiento(i.getString("movimiento"));
        seña.setConfiguracion(i.getString("configuracion"));
        seña.setFoto1(i.getString("foto1"));
        seña.setFoto2(i.getString("foto2"));
        seña.setFoto3(i.getString("foto3"));
        seña.setFoto4(i.getString("foto4"));
        seña.setVideoFrente(i.getString("videoFrente"));
        seña.setVideoPerfil(i.getString("videoPerfil"));

        nombre.setText(seña.getNombre());

        configuracion.setText("Configuración: "+seña.getConfiguracion());
        movimiento.setText("Movimiento: "+seña.getMovimiento());
        orientacion.setText("Orientación: "+seña.getOrientacion());
        ubicacion.setText("Ubicación: "+seña.getUbicacion());
        rasgosNoManuales.setText("Rasgos no manuales: "+seña.getRasgosNoManuales());

        if(!seña.getObservacion().equals("null")) {
            observacion.setText("Observación: " + seña.getObservacion());
        }
        else
            observacion.setText("Observación: - ");

        if(seña.getFoto1()!=null){
            Picasso.with(this).load(seña.getFoto1()).into(foto1);
        }
        if(seña.getFoto2()!=null){
            Picasso.with(this).load(seña.getFoto2()).into(foto2);
        }
        if(seña.getFoto3()!=null){
            Picasso.with(this).load(seña.getFoto3()).into(foto3);
        }
        if(seña.getFoto4()!=null){
            Picasso.with(this).load(seña.getFoto4()).into(foto4);
        }



        /*
        System.out.println("Señia: "+seña.getNombre());
        System.out.println("Rasgos no manuales: "+seña.getRasgosNoManuales());
        System.out.println("Ubicacion: "+seña.getUbicacion());
        System.out.println("Observacion: "+seña.getObservacion());
        System.out.println("Orientacion: "+seña.getOrientacion());
        System.out.println("Movimiento: "+seña.getMovimiento());
        System.out.println("Configuracion: "+seña.getConfiguracion());
        System.out.println("Foto 1: "+seña.getFoto1());
        System.out.println("Foto 2: "+seña.getFoto2() );
        System.out.println("Foto 3: "+seña.getFoto3());
        System.out.println("Foto 4: "+seña.getFoto4());
        System.out.println("Video de frente: "+seña.getVideoFrente());
        System.out.println("Video de perfil: "+seña.getVideoPerfil());
        */
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            List<String> videoIds = new ArrayList<>();
            videoIds.add(seña.getVideoFrente());
            videoIds.add(seña.getVideoPerfil());
            player.cueVideos(videoIds); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            //String error = String.format(getString(R.string.player_error), errorReason.toString());
            //Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(YouTubeKey, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        return youTubeView;
    }
}
