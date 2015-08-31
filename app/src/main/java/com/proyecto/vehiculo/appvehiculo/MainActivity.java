package com.proyecto.vehiculo.appvehiculo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.proyecto.vehiculo.appvehiculo.Enum.SensoresEnum;
import com.proyecto.vehiculo.appvehiculo.Utilerias.RevisionSensores;

import java.util.Random;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, SensorEventListener {

    private static final String TELEFONO_TWILIO = "5549998734";

    TextView tvProximidad, tvGiro, tvCarga, tvIzq, tvDer, tvOrigen;
    Button btnProximidad, btnGiro, btnCarga;
    SensorManager mSensorManager;
    boolean encProximidad, encGiro, encCarga, SmsEnviado, yaExisteLectura, esValorModificado;
    private ImageView imagen;
    private float grados = 0f;
    IntentFilter filtroConectado;
    IntentFilter filtroDesconectado;
    private PowerManager.WakeLock mWakeLock;
    private float primeraLectura, izquierda, derecha;
    Sensor proximidad;
    MenuItem itemSMS, itemNoSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvProximidad = (TextView)findViewById(R.id.tvProximidad);
        tvGiro = (TextView)findViewById(R.id.tvGiro);
        tvCarga = (TextView)findViewById(R.id.tvCarga);
        tvIzq = (TextView)findViewById(R.id.izquierda);
        tvDer = (TextView)findViewById(R.id.derecha);
        tvOrigen = (TextView)findViewById(R.id.original);
        imagen = (ImageView)findViewById(R.id.imagen);
        btnProximidad = (Button)findViewById(R.id.btnProximidad);
        btnGiro = (Button)findViewById(R.id.btnGiro);
        btnCarga = (Button)findViewById(R.id.btnCarga);
        btnProximidad.setOnClickListener(this);
        btnGiro.setOnClickListener(this);
        btnCarga.setOnClickListener(this);
        btnProximidad.setBackgroundColor(Color.rgb(201, 108, 108));
        btnCarga.setBackgroundColor(Color.rgb(201, 108, 108));
        btnGiro.setBackgroundColor(Color.rgb(201, 108, 108));
        encProximidad=false;encGiro=false;encCarga=false;SmsEnviado=true;yaExisteLectura=false;

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null){
            proximidad = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mSensorManager.registerListener(this,proximidad,
                    SensorManager.SENSOR_DELAY_NORMAL);
            tvProximidad.setText(String.valueOf(proximidad.getMaximumRange()));
        }
        else {
            tvProximidad.setText("N/D");
            btnProximidad.setEnabled(false);
            encProximidad=false;
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            tvGiro.setText("N/D");
            btnGiro.setEnabled(false);
            encGiro=false;
        }

        filtroConectado = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        filtroDesconectado = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receptorCarga, filtroConectado);
        registerReceiver(receptorCarga, filtroDesconectado);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ddd");
        this.mWakeLock.acquire();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(encProximidad) {
                int valorProximidad = (int) event.values[0];
                tvProximidad.setText(String.valueOf(event.values[0]));
                if (valorProximidad == 0 && !SmsEnviado) {
                    enviarAlarma(SensoresEnum.PROXIMIDAD.getId(),-1);
                }
            }
        }
        if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            if (encGiro) {
                float datos = Math.round(event.values[0]);
                tvGiro.setText(String.valueOf(datos));
                if(!yaExisteLectura){
                    primeraLectura = datos;
                    esValorModificado=false;
                    izquierda = primeraLectura - 25;
                    derecha = primeraLectura +25;
                    if((primeraLectura + 25 ) >= 360){
                        float dif = 360 - primeraLectura;
                        derecha = 25 - dif;
                        esValorModificado=true;
                    }
                    if((primeraLectura - 25) <= 0){
                        float dif = 25 - primeraLectura;
                        izquierda = 360 - dif;
                        esValorModificado=true;
                    }
                    yaExisteLectura=true;
                    tvOrigen.setText("Origen: "+primeraLectura);
                    tvIzq.setText("Izq: "+izquierda);
                    tvDer.setText("Der: "+derecha);
                }
                if(yaExisteLectura){
                    if(esValorModificado){
                        if((datos < izquierda && datos > 180) || (datos > derecha && datos < 180)){
                            enviarAlarma(SensoresEnum.MOVIMIENTO.getId(), datos);
                        }
                    }else if((datos < izquierda || datos > derecha)) {
                        enviarAlarma(SensoresEnum.MOVIMIENTO.getId(), datos);
                    }
                }
                RotateAnimation ra = new RotateAnimation(grados,
                        -datos, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

                // how long the animation will take place
                ra.setDuration(210);
                // set the animation after the end of the reservation status
                ra.setFillAfter(true);
                // Start the animation
                imagen.startAnimation(ra);
                grados = -datos;

            }
        }
    }

    private void enviarAlarma(Integer idSensor, float lectura){
        if(idSensor != null && !SmsEnviado) {
            SmsEnviado=true;
            String mensaje = "1;"+idSensor;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(TELEFONO_TWILIO, null, mensaje, null, null);
            Toast.makeText(this, "Enviando Mensaje de Alarma", Toast.LENGTH_SHORT).show();
            if(lectura != -1){
                Toast.makeText(this, "Lectura Alarma: "+lectura, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnProximidad:
                if(encProximidad){
                    encProximidad=false;
                    btnProximidad.setBackgroundColor(Color.rgb(201, 108, 108));
                    btnProximidad.setText("Apagado");
                    RevisionSensores.guardarProximidad(this, encProximidad);
                }else{
                    encProximidad=true;
                    btnProximidad.setBackgroundColor(Color.rgb(43, 157, 112));
                    btnProximidad.setText("Encendido");
                    RevisionSensores.guardarProximidad(this, encProximidad);
                }
                break;
            case R.id.btnCarga:
                if(encCarga){
                    encCarga=false;
                    btnCarga.setText("Apagado");
                    tvCarga.setText("Sin Estatus");
                    btnCarga.setBackgroundColor(Color.rgb(201, 108, 108));
                    RevisionSensores.guardarCarga(this, encCarga);
                }else{
                    encCarga=true;
                    btnCarga.setText("Encendido");
                    btnCarga.setBackgroundColor(Color.rgb(43, 157, 112));
                    RevisionSensores.guardarCarga(this, encCarga);
                }
                break;
            case R.id.btnGiro:
                if(encGiro){
                    encGiro=false;
                    btnGiro.setText("Apagado");
                    btnGiro.setBackgroundColor(Color.rgb(201, 108, 108));
                    RevisionSensores.guardarOrientacion(this, encGiro);
                    yaExisteLectura=false;
                }else{
                    encGiro=true;
                    btnGiro.setText("Encendido");
                    btnGiro.setBackgroundColor(Color.rgb(43, 157, 112));
                    RevisionSensores.guardarOrientacion(this, encGiro);
                }
                break;
        }
    }

    private BroadcastReceiver receptorCarga =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(encCarga) {
                                    enviarAlarma(SensoresEnum.CARGA.getId(),-1);
                                    tvCarga.setText("Desconectado");
                                }
                            }
                        });
                    }
                    if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(encCarga) {
                                    tvCarga.setText("Conectado");
                                }
                            }
                        });
                    }

                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        itemSMS = menu.findItem(R.id.menu_sms);
        itemNoSMS = menu.findItem(R.id.menu_no_sms);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_apagar) {
            encCarga=false;encGiro=false;encProximidad=false;yaExisteLectura=false;
            btnGiro.setBackgroundColor(Color.rgb(201, 108, 108));btnGiro.setText("Apagado");
            btnCarga.setBackgroundColor(Color.rgb(201, 108, 108));btnCarga.setText("Apagado");
            btnProximidad.setBackgroundColor(Color.rgb(201, 108, 108));btnProximidad.setText("Apagado");
        }
        if (id == R.id.menu_encender) {
            encCarga=true;encGiro=true;encProximidad=true;
            btnGiro.setBackgroundColor(Color.rgb(43, 157, 112));btnGiro.setText("Encendido");
            btnCarga.setBackgroundColor(Color.rgb(43, 157, 112));btnCarga.setText("Encendido");
            btnProximidad.setBackgroundColor(Color.rgb(43, 157, 112));btnProximidad.setText("Encendido");
        }
        if(id == R.id.menu_sms){
            SmsEnviado=true;
            itemSMS.setVisible(false);
            itemNoSMS.setVisible(true);
        }
        if(id == R.id.menu_no_sms){
            SmsEnviado=false;
            itemSMS.setVisible(true);
            itemNoSMS.setVisible(false);
        }
        RevisionSensores.guardarOrientacion(this, encGiro);
        RevisionSensores.guardarCarga(this, encCarga);
        RevisionSensores.guardarProximidad(this, encProximidad);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this,"Entró a OnDestroy",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receptorCarga);
        mSensorManager.unregisterListener(this);
        this.mWakeLock.release();
        Toast.makeText(this,"Entró a OnStop",Toast.LENGTH_SHORT).show();
        super.onStop();
    }
}
