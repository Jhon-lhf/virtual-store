package com.tienda.servicio;

/**
 * Orden con el que se presenta el catálogo.
 */
public enum CriterioOrden {
    /** Orden natural del archivo (orden de publicación). */
    RELEVANCIA("Relevancia"),
    NOMBRE_ASC("Nombre (A–Z)"),
    PRECIO_ASC("Precio (menor a mayor)"),
    PRECIO_DESC("Precio (mayor a menor)");

    private final String etiqueta;

    CriterioOrden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Etiqueta legible para mostrar en el combo de la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
