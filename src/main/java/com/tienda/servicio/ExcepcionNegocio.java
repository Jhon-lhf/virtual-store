package com.tienda.servicio;

/**
 * Excepción de regla de negocio o de persistencia: el mensaje está pensado
 * para mostrarse directamente al usuario en la interfaz.
 */
public class ExcepcionNegocio extends RuntimeException {

    public ExcepcionNegocio(String mensaje) {
        super(mensaje);
    }

    public ExcepcionNegocio(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
