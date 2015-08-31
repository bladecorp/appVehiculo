package com.proyecto.vehiculo.appvehiculo.Enum;

/**
 * Created by Mou on 27/08/2015.
 */
public enum TipoMensajeEnum {

    ALARMA(1),
    GENERAL(2);

    private int id;

    private TipoMensajeEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
