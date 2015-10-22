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
    private static final int VERDE = 1;
    private static final int ROJO = 2;

    private TextView tvProximidad, tvGiro, tvCarga, tvIzq, tvDer, tvOrigen;
    private Button btnProximidad, btnGiro, btnCarga;
    private SensorManager mSensorManager;
    private boolean encProximidad, encGiro, encCarga, SmsEnviado, yaExisteLectura, esValorModificado;
    private ImageView imagen;
    private float grados = 0f;
    private IntentFilter filtroConectado;
    private IntentFilter filtroDesconectado;
    private PowerManager.WakeLock mWakeLock;
    private float primeraLectura, izquierda, derecha;
    private Sensor proximidad;
    private MenuItem itemSMS, itemNoSMS;
    private BroadcastReceiver receptorCarga;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); Toast.makeText(this, "Entró a OnCreate", Toast.LENGTH_SHORT).show();
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
        btnProximidad.setBackgroundColor(establecerColor(ROJO));
        btnCarga.setBackgroundColor(establecerColor(ROJO));
        btnGiro.setBackgroundColor(establecerColor(ROJO));
        encProximidad=false;encGiro=false;encCarga=false;SmsEnviado=true;yaExisteLectura=false;
     //   registrarSensores();
    }

    public void registrarSensores(){
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

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ddd");
        this.mWakeLock.acquire();

        receptorCarga =
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
        filtroConectado = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        filtroDesconectado = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receptorCarga, filtroConectado);
        registerReceiver(receptorCarga, filtroDesconectado);
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
                    btnProximidad.setBackgroundColor(establecerColor(ROJO));
                    btnProximidad.setText("Apagado");
                    RevisionSensores.guardarProximidad(this, encProximidad);
                }else{
                    encProximidad=true;
                    btnProximidad.setBackgroundColor(establecerColor(VERDE));
                    btnProximidad.setText("Encendido");
                    RevisionSensores.guardarProximidad(this, encProximidad);
                }
                break;
            case R.id.btnCarga:
                if(encCarga){
                    encCarga=false;
                    btnCarga.setText("Apagado");
                    tvCarga.setText("Sin Estatus");
                    btnCarga.setBackgroundColor(establecerColor(ROJO));
                    RevisionSensores.guardarCarga(this, encCarga);
                }else{
                    encCarga=true;
                    btnCarga.setText("Encendido");
                    btnCarga.setBackgroundColor(establecerColor(VERDE));
                    RevisionSensores.guardarCarga(this, encCarga);
                }
                break;
            case R.id.btnGiro:
                if(encGiro){
                    encGiro=false;
                    btnGiro.setText("Apagado");
                    btnGiro.setBackgroundColor(establecerColor(ROJO));
                    RevisionSensores.guardarOrientacion(this, encGiro);
                    yaExisteLectura=false;
                }else{
                    encGiro=true;
                    btnGiro.setText("Encendido");
                    btnGiro.setBackgroundColor(establecerColor(VERDE));
                    RevisionSensores.guardarOrientacion(this, encGiro);
                }
                break;
        }
    }


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
            btnGiro.setBackgroundColor(establecerColor(ROJO));btnGiro.setText("Apagado");
            btnCarga.setBackgroundColor(establecerColor(ROJO));btnCarga.setText("Apagado");
            btnProximidad.setBackgroundColor(establecerColor(ROJO));btnProximidad.setText("Apagado");
        }
        if (id == R.id.menu_encender) {
            encCarga=true;encGiro=true;encProximidad=true;
            btnGiro.setBackgroundColor(establecerColor(VERDE));btnGiro.setText("Encendido");
            btnCarga.setBackgroundColor(establecerColor(VERDE));btnCarga.setText("Encendido");
            btnProximidad.setBackgroundColor(establecerColor(VERDE));btnProximidad.setText("Encendido");
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

    public int establecerColor(int color){
        if(color == VERDE){
            return Color.rgb(43, 157, 112);
        }else{
            return Color.rgb(201, 108, 108);
        }
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
        encCarga=false;encGiro=false;encProximidad=false;yaExisteLectura=false;
        btnGiro.setBackgroundColor(establecerColor(ROJO));btnGiro.setText("Apagado");
        btnCarga.setBackgroundColor(establecerColor(ROJO));btnCarga.setText("Apagado");
        btnProximidad.setBackgroundColor(establecerColor(ROJO));btnProximidad.setText("Apagado");

        SmsEnviado=true;
        itemSMS.setVisible(false);
        itemNoSMS.setVisible(true);

        if(receptorCarga != null) {
            unregisterReceiver(receptorCarga);
            receptorCarga = null;
            Toast.makeText(this,"Se borró registro Receptor Carga",Toast.LENGTH_SHORT).show();
        }

        mSensorManager.unregisterListener(this);

        if(mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
            Toast.makeText(this,"Se liberó Despertador",Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this,"Entró a OnStop",Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        Toast.makeText(this, "Entró a OnPostResume", Toast.LENGTH_SHORT).show();
        registrarSensores();
        Toast.makeText(this, "Se registraron sensores", Toast.LENGTH_SHORT).show();
        super.onPostResume();
    }
}
