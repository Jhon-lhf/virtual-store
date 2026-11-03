package com.tienda;

import com.tienda.modelo.Categoria;
import com.tienda.modelo.Deseos;
import com.tienda.modelo.OrigenCategoria;
import com.tienda.modelo.Producto;
import com.tienda.modelo.Resena;
import com.tienda.modelo.Usuario;
import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioDeseos;
import com.tienda.persistencia.RepositorioProductos;
import com.tienda.persistencia.RepositorioUsuarios;
import com.tienda.util.GeneradorId;
import com.tienda.util.HashClave;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos demo del primer arranque: si {@code data/} está vacío, siembra 3
 * usuarios, 4 categorías y 12 productos para que la aplicación no se vea
 * desierta. Si borras los JSON, la app re-siembra al arrancar.
 *
 * <p>Cuentas demo: {@code admin/admin123}, {@code cliente/cli123},
 * {@code vendedor/ven123}.</p>
 */
public final class Semilla {

    private Semilla() {
    }

    public static void ejecutarSiNecesario(RepositorioUsuarios repoUsuarios,
                                           RepositorioProductos repoProductos,
                                           RepositorioCategorias repoCategorias,
                                           RepositorioDeseos repoDeseos) {
        if (!repoUsuarios.leerTodos().isEmpty()) {
            return;
        }
        String hoy = LocalDate.now().toString();

        // --- Categorías (entidades; los productos las referencian por ID) ---
        Map<String, String> idsCategorias = new LinkedHashMap<>();
        for (String nombre : new String[]{"Tecnologia", "Ropa", "Hogar", "Libros"}) {
            Categoria categoria = new Categoria(
                    GeneradorId.generar("C"), nombre, hoy, OrigenCategoria.SEMILLA);
            repoCategorias.guardar(categoria);
            idsCategorias.put(nombre, categoria.getId());
        }

        // --- Usuarios demo (uno por rol) ---
        repoUsuarios.guardar(new Usuario(GeneradorId.generar("U"), "admin",
                HashClave.calcular("admin123"), "Administrador Demo",
                "admin@tienda.com", com.tienda.modelo.Rol.ADMINISTRADOR, true));
        repoUsuarios.guardar(new Usuario(GeneradorId.generar("U"), "cliente",
                HashClave.calcular("cli123"), "Cliente Demo",
                "cliente@tienda.com", com.tienda.modelo.Rol.CLIENTE, true));
        repoUsuarios.guardar(new Usuario(GeneradorId.generar("U"), "vendedor",
                HashClave.calcular("ven123"), "Vendedor Demo",
                "vendedor@tienda.com", com.tienda.modelo.Rol.VENDEDOR, true));

        String idVendedor = repoUsuarios.buscarPorNombreUsuario("vendedor").orElseThrow().getId();
        String idCliente = repoUsuarios.buscarPorNombreUsuario("cliente").orElseThrow().getId();
        String idAdmin = repoUsuarios.buscarPorNombreUsuario("admin").orElseThrow().getId();

        // --- Productos demo (12, coherentes con los mockups del prototipo) ---
        Object[][] productos = {
                {"Laptop Pro 14", "Portátil ligero con pantalla de alta resolución y 16 GB de RAM.", 999.00, 8, "Tecnologia"},
                {"Smartphone Galaxy X", "Pantalla AMOLED de 6.7\" y cámara triple de 108 MP.", 599.00, 15, "Tecnologia"},
                {"Auriculares Inalámbricos Aero", "Cancelación activa de ruido y 30 h de batería.", 79.99, 3, "Tecnologia"},
                {"Monitor 27\" 4K UHD", "Panel IPS con 99% sRGB y 144 Hz.", 299.50, 6, "Tecnologia"},
                {"Teclado Mecánico RGB", "Switches rojos, reposamuñecas y retroiluminación por tecla.", 129.90, 12, "Tecnologia"},
                {"Camiseta Premium de Algodón", "100% algodón peinado, corte regular.", 24.99, 40, "Ropa"},
                {"Chaqueta Impermeable Trek", "Costuras selladas y capucha ajustable.", 89.90, 4, "Ropa"},
                {"Cafetera Espresso Barista", "20 bares, vaporizador para leche.", 189.99, 7, "Hogar"},
                {"Lámpara de Escritorio LED", "3 temperaturas de color y puerto USB.", 34.50, 20, "Hogar"},
                {"Silla Ergonómica Pro", "Respaldo lumbar, ajuste de altura y ruedas silenciosas.", 420.00, 10, "Hogar"},
                {"El Alquimista (bolsillo)", "Novela de Paulo Coelho.", 14.99, 25, "Libros"},
                {"Código Limpio", "Manual de artesanía del software.", 39.90, 3, "Libros"}
        };
        List<String> idsProductos = new ArrayList<>();
        for (Object[] fila : productos) {
            Producto producto = new Producto(
                    GeneradorId.generar("P"),
                    (String) fila[0],
                    (String) fila[1],
                    (Double) fila[2],
                    (Integer) fila[3],
                    idsCategorias.get(fila[4]),
                    idVendedor,
                    hoy);
            repoProductos.guardar(producto);
            idsProductos.add(producto.getId());
        }

        // --- Reseñas demo (embebidas en los productos; una por autor) ---
        resenar(repoProductos, idsProductos.get(0), idCliente, "Cliente Demo", 5,
                "La mejor compra que he hecho este año, llegó rápido y funciona perfecto.", hoy);
        resenar(repoProductos, idsProductos.get(2), idCliente, "Cliente Demo", 4,
                "Muy cómodos y la batería dura lo prometido.", hoy);
        resenar(repoProductos, idsProductos.get(7), idAdmin, "Administrador Demo", 5,
                "El espresso sale con buen cuerpo. Recomendada.", hoy);
        resenar(repoProductos, idsProductos.get(11), idCliente, "Cliente Demo", 5,
                "Lectura obligatoria para quien programa en equipo.", hoy);

        // --- Lista de deseos demo del cliente ---
        Deseos deseos = new Deseos(idCliente);
        deseos.agregar(idsProductos.get(3));
        deseos.agregar(idsProductos.get(9));
        repoDeseos.guardar(deseos);
    }

    /** Agrega una reseña al producto indicado y lo persiste. */
    private static void resenar(RepositorioProductos repoProductos, String productoId,
                                String autorId, String autorNombre, int calificacion,
                                String comentario, String fecha) {
        Producto producto = repoProductos.buscarPorId(productoId).orElseThrow();
        producto.getResenas().add(new Resena(autorId, autorNombre, calificacion, comentario, fecha));
        repoProductos.guardar(producto);
    }
}
