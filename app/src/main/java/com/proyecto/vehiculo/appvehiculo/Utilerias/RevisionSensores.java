package com.proyecto.vehiculo.appvehiculo.Utilerias;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Mou on 27/08/2015.
 */
public class RevisionSensores {

    public static boolean obtenerProximidad(Context contexto){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(contexto);
        return sp.getBoolean("proximidad",false);
    }

    public static void guardarProximidad(Context contexto, boolean esActivado){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(contexto);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("proximidad",esActivado);
        editor.commit();
    }

    public static boolean obtenerOrientacion(Context contexto){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(contexto);
        return sp.getBoolean("orientacion",false);
    }

    public static void guardarOrientacion(Context contexto, boolean esActivado){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(contexto);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("orientacion",esActivado);
        editor.commit();
    }

    public static boolean obtenerCarga(Context contexto){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(contexto);
        return sp.getBoolean("carga",false);
    }

    public static void guardarCarga(Context contexto, boolean esActivado){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(contexto);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("carga",esActivado);
        editor.commit();
    }


}
