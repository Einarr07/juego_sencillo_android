package com.juegoandroid.juego_sencillo;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Rect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.widget.TextView;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView spaceship;
    private float spaceshipX, spaceshipY;
    private int screenWidth, screenHeight;
    private List<ImageView> meteoritos;
    private int puntos;
    private float velocidadMeteoritos = 40; // Velocidad de meteoritos
    private int tamañoMinimoMeteorito = 150; // Tamaño mínimo del meteorito en píxeles
    private int tamañoMaximoMeteorito = 400; // Tamaño máximo del meteorito en píxeles
    private boolean colisionConMeteorito = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        spaceship = findViewById(R.id.naveEspacial);
        spaceshipX = spaceship.getY();
        spaceshipY = spaceship.getX();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        meteoritos = new ArrayList<>();
        puntos = 0;

        generarMeteoritos();
        sumarPuntosAutomaticamente(); // Comenzar a sumar puntos automáticamente cada 5 segundos
    }

    private void generarMeteoritos() {
        for (int i = 0; i < 2; i++) { // Cantidad de meteoritos fija en 2 para simplificar
            ImageView meteorito = new ImageView(this);
            meteorito.setImageResource(R.drawable.meteorito);

            int tamañoMeteorito = tamañoMinimoMeteorito + (int) (Math.random() * (tamañoMaximoMeteorito - tamañoMinimoMeteorito));

            meteorito.setScaleType(ImageView.ScaleType.FIT_XY);
            meteorito.setAdjustViewBounds(true);
            meteorito.setMaxWidth(tamañoMeteorito);
            meteorito.setMaxHeight(tamañoMeteorito);

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = (int) (Math.random() * (screenWidth - tamañoMeteorito));
            params.topMargin = 0;
            meteorito.setLayoutParams(params);
            ((ConstraintLayout) findViewById(R.id.layout)).addView(meteorito);
            meteoritos.add(meteorito);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float velocidadX = -x * 100;
        float velocidadY = y * 100;

        spaceshipX += velocidadX;
        spaceshipY += velocidadY;

        if (spaceshipX < 0) spaceshipX = 0;
        if (spaceshipX > screenWidth - spaceship.getWidth()) spaceshipX = screenWidth - spaceship.getWidth();
        if (spaceshipY < 0) spaceshipY = 0;
        if (spaceshipY > screenHeight - spaceship.getHeight()) spaceshipY = screenHeight - spaceship.getHeight();

        spaceship.setX(spaceshipX);
        spaceship.setY(spaceshipY);

        for (ImageView meteorito : meteoritos) {
            float nuevaPosicionY = meteorito.getY() + velocidadMeteoritos;
            meteorito.setY(nuevaPosicionY);

            if (nuevaPosicionY > screenHeight) {
                meteorito.setY(0);
                meteorito.setX((float) Math.random() * (screenWidth - meteorito.getWidth()));
            }

            if (colision(spaceship, meteorito)) {
                gameOver();
                return;
            }
        }
    }

    private void sumarPuntosAutomaticamente() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!colisionConMeteorito) {
                    puntos += 10; // Sumar 10 puntos cada 5 segundos
                    actualizarPuntos();
                }
                handler.postDelayed(this, 3000); // Ejecutar de nuevo después de 5 segundos
            }
        }, 5000); // Comenzar después de 5 segundos
    }

    private void actualizarPuntos() {
        TextView textViewPuntos = findViewById(R.id.textViewPuntos);
        textViewPuntos.setText("Puntos: " + puntos);
    }

    private void gameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("☠️☠️☠️☠️ Game Over ️☠️☠️☠️");
        builder.setMessage("¡Has perdido! Tu nave ha sido destruida por un meteorito.\n\nPuntos obtenidos: " + puntos);
        builder.setPositiveButton("Intentar de nuevo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reiniciarJuego();
            }
        });
        builder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        sensorManager.unregisterListener(this);
    }

    private boolean colision(View view1, View view2) {
        Rect rect1 = new Rect((int) view1.getX(), (int) view1.getY(), (int) (view1.getX() + view1.getWidth()), (int) (view1.getY() + view1.getHeight()));
        Rect rect2 = new Rect((int) view2.getX(), (int) view2.getY(), (int) (view2.getX() + view2.getWidth()), (int) (view2.getY() + view2.getHeight()));

        rect1.inset(view1.getWidth() / 4, view1.getHeight() / 4);
        rect2.inset(view2.getWidth() / 4, view2.getHeight() / 4);

        if (rect1.intersect(rect2)) {
            colisionConMeteorito = true;
            return true;
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No es necesario hacer nada aquí
    }

    private void reiniciarJuego() {
        puntos = 0;
        actualizarPuntos();
        eliminarMeteoritos();
        generarMeteoritos();
        colisionConMeteorito = false;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sumarPuntosAutomaticamente();
    }

    private void eliminarMeteoritos() {
        for (ImageView meteorito : meteoritos) {
            ((ConstraintLayout) findViewById(R.id.layout)).removeView(meteorito);
        }
        meteoritos.clear();
    }
}
