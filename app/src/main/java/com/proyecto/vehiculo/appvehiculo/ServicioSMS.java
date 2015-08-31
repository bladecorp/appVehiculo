package com.proyecto.vehiculo.appvehiculo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.proyecto.vehiculo.appvehiculo.Enum.MensajeGeneral;
import com.proyecto.vehiculo.appvehiculo.Enum.TipoMensajeEnum;
import com.proyecto.vehiculo.appvehiculo.Utilerias.RevisionSensores;

public class ServicioSMS extends Service {

    private String telTwilio;
    private String mensajeRecibido;

    public ServicioSMS() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        despertarTelefono();
        telTwilio = intent.getStringExtra(ReceptorSMS.TELEFONO);
        mensajeRecibido = intent.getStringExtra(ReceptorSMS.SMS_RECIBIDO);
        Log.d("Servicio SMS", "TelTwilio: " + telTwilio + ", Mensaje: " + mensajeRecibido);
        Toast.makeText(this, "Inicia el servicio", Toast.LENGTH_SHORT).show();
        procesarMensaje();
        onDestroy();
        return super.onStartCommand(intent, flags, startId);
    }

    private void procesarMensaje(){
        String[] codigos = mensajeRecibido.split(";");
        if(validarMensaje(codigos)){
            boolean proximidad = RevisionSensores.obtenerProximidad(this);
            boolean orientacion = RevisionSensores.obtenerOrientacion(this);
            boolean carga = RevisionSensores.obtenerCarga(this);
            int sensoresAfectados = 0;
            String mensaje = "2;1";
            sensoresAfectados = proximidad?sensoresAfectados:sensoresAfectados + 1;
            sensoresAfectados = orientacion?sensoresAfectados:sensoresAfectados + 1;
            sensoresAfectados = carga?sensoresAfectados:sensoresAfectados + 1;
            mensaje += ";"+sensoresAfectados;
            mensaje += proximidad?"":";1";
            mensaje += orientacion?"":";2";
            mensaje += carga?"":";3";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(telTwilio, null, mensaje, null, null);
            Toast.makeText(this, "Contestando Mensaje Revisión de Sensores", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarMensaje(String[] codigos){
        if(codigos.length != 3){
            return false;
        }
        int tipoMensaje = Integer.parseInt(codigos[0]);
        int subMensaje = Integer.parseInt(codigos[1]);
        if(tipoMensaje != TipoMensajeEnum.GENERAL.getId()){
            return false;
        }
        if(subMensaje != MensajeGeneral.REVISAR_SENSORES.getId()){
            return false;
        }

        return true;
    }

    private void despertarTelefono(){
        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "bbbb");
        wl.acquire();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
