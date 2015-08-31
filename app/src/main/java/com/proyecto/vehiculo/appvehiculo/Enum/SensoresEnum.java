package com.proyecto.vehiculo.appvehiculo.Enum;

/**
 * Created by Mou on 27/08/2015.
 */
public enum SensoresEnum {

    PROXIMIDAD(1),
    MOVIMIENTO(2),
    CARGA(3);

    private int id;

    SensoresEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
