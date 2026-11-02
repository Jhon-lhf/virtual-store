package com.tienda.util;

import java.util.List;

/**
 * Corte puro de paginación: recibe la lista completa y devuelve la porción
 * correspondiente a una página, con {@code clamp} (si la página pedida no
 * existe, se ajusta a la última válida; nunca se queda el usuario en una
 * página inexistente).
 *
 * <p>El servicio entrega la lista completa; el corte vive aquí para que sea
 * código puro, fácil de probar sin interfaz.</p>
 */
public final class Paginador {

    /** Tamaños de página que ofrece el catálogo. */
    public static final int[] TAMANOS = {8, 12, 20};
    /** Tamaño de página por defecto. */
    public static final int TAMANO_DEFECTO = 12;

    private Paginador() {
    }

    /**
     * Corta {@code elementos} en páginas de {@code tamanoPagina} y devuelve la
     * {@code paginaSolicitada} ajustada al rango válido.
     *
     * @param elementos        lista completa (no nula).
     * @param paginaSolicitada página pedida (1-based; valores fuera de rango se ajustan).
     * @param tamanoPagina     cantidad de elementos por página (mínimo 1).
     */
    public static <T> Pagina<T> paginar(List<T> elementos, int paginaSolicitada, int tamanoPagina) {
        int tamano = Math.max(1, tamanoPagina);
        int totalPaginas = Math.max(1, (int) Math.ceil(elementos.size() / (double) tamano));
        int pagina = Math.min(Math.max(1, paginaSolicitada), totalPaginas);

        int desde = (pagina - 1) * tamano;
        int hasta = Math.min(desde + tamano, elementos.size());

        List<T> porcion = desde >= hasta ? List.of() : elementos.subList(desde, hasta);
        return new Pagina<>(porcion, pagina, totalPaginas, elementos.size());
    }
}
