package com.tienda.util;

import java.util.List;

/**
 * Resultado de un corte de paginación: elementos de la página solicitada más
 * los metadatos necesarios para pintar el paginador.
 *
 * @param <T> tipo de los elementos.
 */
public record Pagina<T>(List<T> elementos, int numero, int totalPaginas, int totalElementos) {
}
