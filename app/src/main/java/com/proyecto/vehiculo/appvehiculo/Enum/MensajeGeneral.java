package com.proyecto.vehiculo.appvehiculo.Enum;

/**
 * Created by Mou on 27/08/2015.
 */
public enum MensajeGeneral {

    INFORMATIVO(1), //Solo lo envía el vehículo
    SUSPENDER_SERVICIO(2),
    INVITACION(3),
    REVISAR_SENSORES(4);

    private int id;

    private MensajeGeneral(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
