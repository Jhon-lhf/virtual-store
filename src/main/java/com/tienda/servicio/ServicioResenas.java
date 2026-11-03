package com.tienda.servicio;

import com.tienda.modelo.Producto;
import com.tienda.modelo.Resena;
import com.tienda.persistencia.RepositorioProductos;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de reseñas: cada usuario deja <b>una</b> reseña por producto
 * (volver a reseñar actualiza la suya). Las reseñas viven embebidas en el
 * {@link Producto}; el promedio mostrado se calcula desde esa lista.
 */
public class ServicioResenas {

    private final RepositorioProductos repositorioProductos;

    public ServicioResenas(RepositorioProductos repositorioProductos) {
        this.repositorioProductos = repositorioProductos;
    }

    /**
     * Crea o actualiza la reseña del autor sobre el producto.
     *
     * @throws ExcepcionNegocio si el producto no existe, la calificación no
     *                          está entre 1 y 5, o el comentario queda vacío.
     */
    public void dejarResena(String productoId, String autorId, String autorNombre,
                            int calificacion, String comentario) {
        if (calificacion < 1 || calificacion > 5) {
            throw new ExcepcionNegocio("La calificación debe estar entre 1 y 5 estrellas");
        }
        if (comentario == null || comentario.isBlank()) {
            throw new ExcepcionNegocio("Escribe un comentario para tu reseña");
        }
        Producto producto = repositorioProductos.buscarPorId(productoId)
                .orElseThrow(() -> new ExcepcionNegocio("El producto no existe"));

        Resena resena = new Resena(autorId, autorNombre, calificacion,
                comentario.trim(), LocalDate.now().toString());
        List<Resena> resenas = producto.getResenas();
        resenas.removeIf(r -> r.getAutorId().equals(autorId));
        resenas.add(resena);
        repositorioProductos.guardar(producto);
    }

    /** Reseña del autor sobre el producto, si existe. */
    public java.util.Optional<Resena> resenaDe(String productoId, String autorId) {
        return repositorioProductos.buscarPorId(productoId)
                .flatMap(p -> p.getResenas().stream()
                        .filter(r -> r.getAutorId().equals(autorId))
                        .findFirst());
    }

    /** Reseñas del producto, la más reciente primero. */
    public List<Resena> listarPorProducto(String productoId) {
        return repositorioProductos.buscarPorId(productoId)
                .map(p -> List.copyOf(p.getResenas()))
                .orElse(List.of());
    }
}
