package com.juegoandroid.juego_sencillo;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Rect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView spaceship;
    private float spaceshipX, spaceshipY;
    private int screenWidth, screenHeight;
    private List<ImageView> meteoritos;
    private int puntos;
    private float velocidadMeteoritos = 15; // Ajusta la velocidad según tus necesidades
    private int tamañoMinimoMeteorito = 150; // Tamaño mínimo del meteorito en píxeles
    private int tamañoMaximoMeteorito = 300; // Tamaño máximo del meteorito en píxeles

    private int cantidadMeteoritos = 5; // Cantidad de meteoritos que deseas mostrar en pantalla
    private int intervaloMinimo = 1000; // Intervalo mínimo de tiempo en milisegundos
    private int intervaloMaximo = 3000; // Intervalo máximo de tiempo en milisegundos


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
    }

    private void generarMeteoritos() {
        for (int i = 0; i < cantidadMeteoritos; i++) {
            ImageView meteorito = new ImageView(this);
            meteorito.setImageResource(R.drawable.meteorito);

            // Generar un tamaño aleatorio para el meteorito dentro del rango especificado
            int tamañoMeteorito = tamañoMinimoMeteorito + (int) (Math.random() * (tamañoMaximoMeteorito - tamañoMinimoMeteorito));

            // Ajustar la escala de la imagen del meteorito
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
        float velocidadX = -x * 5; // Ajusta la velocidad según tus necesidades
        float velocidadY = y * 5; // Ajusta la velocidad según tus necesidades

        spaceshipX += velocidadX;
        spaceshipY += velocidadY;

        // Limitar los movimientos de la nave dentro de los límites de la pantalla
        if (spaceshipX < 0) spaceshipX = 0;
        if (spaceshipX > screenWidth - spaceship.getWidth()) spaceshipX = screenWidth - spaceship.getWidth();
        if (spaceshipY < 0) spaceshipY = 0;
        if (spaceshipY > screenHeight - spaceship.getHeight()) spaceshipY = screenHeight - spaceship.getHeight();

        spaceship.setX(spaceshipX);
        spaceship.setY(spaceshipY);

        // Mover los meteoritos hacia abajo
        for (ImageView meteorito : meteoritos) {
            float nuevaPosicionY = meteorito.getY() + velocidadMeteoritos;
            meteorito.setY(nuevaPosicionY);

            // Verificar si el meteorito está fuera de la pantalla y generarlo nuevamente en la parte superior
            if (nuevaPosicionY > screenHeight) {
                meteorito.setY(0);
                meteorito.setX((float) Math.random() * (screenWidth - meteorito.getWidth()));
            }

            // Verificar colisiones con los meteoritos
            if (colision(spaceship, meteorito)) {
                // Colisión detectada, puedes hacer algo aquí como mostrar un mensaje de game over
            }
        }
    }

    private boolean colision(View view1, View view2) {
        Rect rect1 = new Rect((int) view1.getX(), (int) view1.getY(), (int) (view1.getX() + view1.getWidth()), (int) (view1.getY() + view1.getHeight()));
        Rect rect2 = new Rect((int) view2.getX(), (int) view2.getY(), (int) (view2.getX() + view2.getWidth()), (int) (view2.getY() + view2.getHeight()));
        return rect1.intersect(rect2);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No es necesario hacer nada aquí
    }
}
