package com.tienda.servicio;

import com.tienda.modelo.Categoria;
import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioProductos;
import com.tienda.util.GeneradorId;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Lógica del catálogo: búsqueda con filtros, orden y alta de productos.
 * Entrega la lista completa filtrada; el corte por página lo hace
 * {@code Paginador} en la interfaz (corte puro, testeable).
 */
public class ServicioProductos {

    private final RepositorioProductos repositorioProductos;
    private final RepositorioCategorias repositorioCategorias;

    public ServicioProductos(RepositorioProductos repositorioProductos,
                             RepositorioCategorias repositorioCategorias) {
        this.repositorioProductos = repositorioProductos;
        this.repositorioCategorias = repositorioCategorias;
    }

    /**
     * Busca productos aplicando los criterios. El texto busca (sin distinguir
     * mayúsculas) en el título <b>o</b> la descripción; el rango de precio es
     * inclusivo; un criterio null se ignora.
     */
    public List<Producto> buscar(CriteriosBusqueda criterios) {
        String texto = criterios.texto() == null ? "" : criterios.texto().trim().toLowerCase(Locale.ROOT);
        return repositorioProductos.leerTodos().stream()
                .filter(p -> texto.isEmpty()
                        || p.getTitulo().toLowerCase(Locale.ROOT).contains(texto)
                        || p.getDescripcion().toLowerCase(Locale.ROOT).contains(texto))
                .filter(p -> criterios.categoriaId() == null
                        || p.getCategoriaId().equals(criterios.categoriaId()))
                .filter(p -> criterios.precioMin() == null || p.getPrecio() >= criterios.precioMin())
                .filter(p -> criterios.precioMax() == null || p.getPrecio() <= criterios.precioMax())
                .sorted(ordenDe(criterios.orden()))
                .toList();
    }

    private Comparator<Producto> ordenDe(CriterioOrden orden) {
        return switch (orden == null ? CriterioOrden.RELEVANCIA : orden) {
            case NOMBRE_ASC -> Comparator.comparing(Producto::getTitulo, String.CASE_INSENSITIVE_ORDER);
            case PRECIO_ASC -> Comparator.comparingDouble(Producto::getPrecio);
            case PRECIO_DESC -> Comparator.comparingDouble(Producto::getPrecio).reversed();
            case RELEVANCIA -> Comparator.comparing(Producto::getId); // estable, según orden del archivo
        };
    }

    /**
     * Publica un producto nuevo vinculado al vendedor en sesión.
     *
     * @throws ExcepcionNegocio si falta el título, el precio/stock no son
     *                          válidos o la categoría no existe (debe ser una
     *                          categoría aprobada, elegida del selector).
     */
    public Producto crear(DatosProducto datos, String vendedorId) {
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw new ExcepcionNegocio("El título es obligatorio");
        }
        if (datos.precio() <= 0) {
            throw new ExcepcionNegocio("El precio debe ser mayor que cero");
        }
        if (datos.stock() < 0) {
            throw new ExcepcionNegocio("El stock no puede ser negativo");
        }
        repositorioCategorias.buscarPorId(datos.categoriaId())
                .orElseThrow(() -> new ExcepcionNegocio("La categoría seleccionada no existe"));

        Producto producto = new Producto(
                GeneradorId.generar("P"),
                datos.titulo().trim(),
                datos.descripcion() == null ? "" : datos.descripcion().trim(),
                datos.precio(),
                datos.stock(),
                datos.categoriaId(),
                vendedorId,
                LocalDate.now().toString());
        repositorioProductos.guardar(producto);
        return producto;
    }

    /** Actualiza el stock de un producto (valida que no sea negativo). */
    public void actualizarStock(String idProducto, int nuevoStock) {
        if (nuevoStock < 0) {
            throw new ExcepcionNegocio("El stock no puede ser negativo");
        }
        Producto producto = obtenerPorId(idProducto)
                .orElseThrow(() -> new ExcepcionNegocio("El producto no existe"));
        producto.setStock(nuevoStock);
        repositorioProductos.guardar(producto);
    }

    /** Descuenta stock tras validar disponibilidad (usado por el checkout). */
    public void descontarStock(String idProducto, int cantidad) {
        Producto producto = obtenerPorId(idProducto)
                .orElseThrow(() -> new ExcepcionNegocio("El producto no existe"));
        if (producto.getStock() < cantidad) {
            throw new ExcepcionNegocio("Stock insuficiente de \"" + producto.getTitulo() + "\"");
        }
        producto.setStock(producto.getStock() - cantidad);
        repositorioProductos.guardar(producto);
    }

    public Optional<Producto> obtenerPorId(String id) {
        return repositorioProductos.buscarPorId(id);
    }

    /** Nombre de la categoría de un producto (para tarjetas y tablas). */
    public String nombreCategoria(String categoriaId) {
        return repositorioCategorias.buscarPorId(categoriaId)
                .map(Categoria::getNombre)
                .orElse("—");
    }

    public int contar() {
        return repositorioProductos.leerTodos().size();
    }

    /** Criterios de búsqueda del catálogo (null = sin filtro). */
    public record CriteriosBusqueda(String texto, String categoriaId,
                                    Double precioMin, Double precioMax, CriterioOrden orden) {
    }

    /** Datos de entrada para publicar un producto. */
    public record DatosProducto(String titulo, String descripcion, double precio,
                                int stock, String categoriaId) {
    }
}
